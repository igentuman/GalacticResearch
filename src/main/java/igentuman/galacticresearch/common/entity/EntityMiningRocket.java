package igentuman.galacticresearch.common.entity;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import micdoodle8.mods.galacticraft.api.prefab.entity.EntitySpaceshipBase;
import micdoodle8.mods.galacticraft.api.tile.IFuelDock;
import micdoodle8.mods.galacticraft.api.tile.ILandingPadAttachable;
import micdoodle8.mods.galacticraft.api.vector.BlockVec3Dim;
import micdoodle8.mods.galacticraft.api.world.IExitHeight;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.advancement.GCTriggers;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import micdoodle8.mods.galacticraft.core.network.PacketDynamic;
import micdoodle8.mods.galacticraft.core.tile.TileEntityLandingPad;
import micdoodle8.mods.galacticraft.core.tile.TileEntityTelemetry;
import micdoodle8.mods.galacticraft.core.util.CompatibilityManager;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.galacticraft.planets.mars.entities.EntityCargoRocket;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;

import static igentuman.galacticresearch.RegistryHandler.MINING_ROCKET;

public class EntityMiningRocket extends EntityCargoRocket implements IGRAutoRocket {

    public BlockPos mcsPos = new BlockPos(0,0,0);
    public BlockPos padPos = new BlockPos(0,0,0);
    public String mission = "";
    private boolean isMining = false;
    private int mineDelay = 10;
    private boolean miningDone = false;
    public EnumRocketType rocketType = EnumRocketType.INVENTORY54;

    public EntityMiningRocket(World par1World) {
        super(par1World);
        rocketType = EnumRocketType.INVENTORY54;
        super.rocketType = rocketType;
        this.setSize(0.98F, 2.0F);
    }
    public EntityMiningRocket(World par1World, double par2, double par4, double par6) {
        super(par1World, par2, par4, par6, EnumRocketType.INVENTORY54);
        this.rocketType = EnumRocketType.INVENTORY54;
        super.rocketType = rocketType;
        this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        this.setSize(0.98F, 2.0F);
    }

    public int getFuelTankCapacity() {
        return 5000;
    }

    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(MINING_ROCKET, 1, 0);
    }

    public void updateMCSPos() {
        TileEntityLandingPad pad = (TileEntityLandingPad) getLandingPad();
        if(pad == null) return;
        for (ILandingPadAttachable te : pad.getConnectedTiles()) {
            if (te instanceof TileMissionControlStation) {
                mcsPos = ((TileMissionControlStation) te).getPos();
            }
        }
    }

    public void mineResources()
    {
        mineDelay--;
        if(mineDelay <= 0 && !miningDone) {
            ItemStack st = GalacticResearch.spaceMineProvider.mineBlock(mission);
            if(st.equals(ItemStack.EMPTY)) {
                if(GalacticResearch.spaceMineProvider.getMissions().get(mission) <= 0) {
                    miningDone = true;
                    return;
                }
            }
            addCargo(st, true);
            mineDelay = ModConfig.machines.mining_speed;
            addFuel(new FluidStack(Objects.requireNonNull(fuelTank.getFluid()), mineDelay-1), true);
        }
    }

    private boolean rocketSoundToStop;
    private boolean addToTelemetry;
    private ArrayList<BlockVec3Dim> telemetryList = new ArrayList();

    public boolean checkLaunchValidity() {
        this.statusMessageCooldown = 40;
        if (this.hasValidFuel()) {
            if (this.launchPhase == EnumLaunchPhase.UNIGNITED.ordinal() && !this.world.isRemote) {
                if (this.mission.isEmpty()) {
                    this.statusMessage = I18n.translateToLocal("gui.message.mission_empty");
                    this.statusColour = "§c";
                    return false;
                } else {
                    this.statusMessage = I18n.translateToLocal("gui.message.success.name");
                    this.statusColour = "§a";
                    return true;
                }
            } else {
                return false;
            }
        } else {
            this.destinationFrequency = -1;
            this.statusMessage = I18n.translateToLocal("gui.message.not_enough.name") + "#" + I18n.translateToLocal("gui.message.fuel.name");
            this.statusColour = "§c";
            return false;
        }
    }

    public void setLaunchPhase(EntitySpaceshipBase.EnumLaunchPhase phase) {
        if(phase.equals(EnumLaunchPhase.IGNITED)) {
            miningDone = false;
            isMining = false;
            if(mission.isEmpty()) {
                return;
            }
        }
        super.setLaunchPhase(phase);
    }

    @Override
    public void setAutolaunchSetting(EnumAutoLaunch setting) {
        autoLaunchSetting = setting;
    }

    protected void parentUpdate()
    {
        if (this.world.isRemote && this.addedToChunk && !CompatibilityManager.isCubicChunksLoaded) {
            Chunk chunk = this.world.getChunk(this.chunkCoordX, this.chunkCoordZ);
            int cx = MathHelper.floor(this.posX) >> 4;
            int cz = MathHelper.floor(this.posZ) >> 4;
            if (chunk.isLoaded() && this.chunkCoordX == cx && this.chunkCoordZ == cz) {
                boolean thisfound = false;
                ClassInheritanceMultiMap<Entity> mapEntities = chunk.getEntityLists()[this.chunkCoordY];

                for (Entity ent : mapEntities) {
                    if (ent == this) {
                        thisfound = true;
                        break;
                    }
                }

                if (!thisfound) {
                    chunk.addEntity(this);
                }
            }
        }

        if (this.launchPhase == EnumLaunchPhase.LANDING.ordinal() && this.hasValidFuel() && this.targetVec != null) {
            double yDiff = this.posY - this.getOnPadYOffset() - (double)this.targetVec.getY();
            this.motionY = Math.max(-2.0D, (yDiff - 0.04D) / -55.0D);
            double diff = this.posX - (double)this.targetVec.getX() - 0.5D;
            double motX;
            if (diff > 0.0D) {
                motX = Math.max(-0.1D, diff / -100.0D);
            } else if (diff < 0.0D) {
                motX = Math.min(0.1D, diff / -100.0D);
            } else {
                motX = 0.0D;
            }

            diff = this.posZ - (double)this.targetVec.getZ() - 0.5D;
            double motZ;
            if (diff > 0.0D) {
                motZ = Math.max(-0.1D, diff / -100.0D);
            } else if (diff < 0.0D) {
                motZ = Math.min(0.1D, diff / -100.0D);
            } else {
                motZ = 0.0D;
            }

            if (motZ == 0.0D && motX == 0.0D) {
                this.rotationPitch = 0.0F;
            } else {
                double angleYaw = Math.atan(motZ / motX);
                double signed = motX < 0.0D ? 50.0D : -50.0D;
                double anglePitch = Math.atan(Math.sqrt(motZ * motZ + motX * motX) / signed) * 100.0D;
                this.rotationYaw = (float)angleYaw * 57.295776F;
                this.rotationPitch = (float)anglePitch * 57.295776F;
            }

            if (yDiff > 1.0D && yDiff < 4.0D) {

                for (Object o : this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().offset(0.0D, -3.0D, 0.0D), EntitySpaceshipBase.rocketSelector)) {
                    if (o instanceof EntitySpaceshipBase) {
                        ((EntitySpaceshipBase) o).dropShipAsItem();
                        ((EntitySpaceshipBase) o).setDead();
                    }
                }
            }

            if (yDiff < 0.4D) {
                int yMin = MathHelper.floor(this.getEntityBoundingBox().minY - this.getOnPadYOffset() - 0.45D) - 2;
                int yMax = MathHelper.floor(this.getEntityBoundingBox().maxY) + 1;
                int zMin = MathHelper.floor(this.posZ) - 1;
                int zMax = MathHelper.floor(this.posZ) + 1;

                for(int x = MathHelper.floor(this.posX) - 1; x <= MathHelper.floor(this.posX) + 1; ++x) {
                    for(int z = zMin; z <= zMax; ++z) {
                        for(int y = yMin; y <= yMax; ++y) {
                            if (this.world.getTileEntity(new BlockPos(x, y, z)) instanceof IFuelDock) {
                                this.rotationPitch = 0.0F;
                                this.failRocket();
                            }
                        }
                    }
                }
            }
        }


        ++ticks;
        if (!this.world.isRemote) {
            this.setFlag(6, this.isGlowing());
        }

        this.onEntityUpdate();
        Iterator var1;
        if (this.addToTelemetry) {
            this.addToTelemetry = false;
            var1 = (new ArrayList(this.telemetryList)).iterator();

            while(var1.hasNext()) {
                BlockVec3Dim vec = (BlockVec3Dim)var1.next();
                TileEntity t1 = vec.getTileEntityNoLoad();
                if (t1 instanceof TileEntityTelemetry && !t1.isInvalid() && ((TileEntityTelemetry)t1).linkedEntity == this) {
                    ((TileEntityTelemetry)t1).addTrackedEntity(this);
                }
            }
        }

        Entity e;
        for(var1 = this.getPassengers().iterator(); var1.hasNext(); e.fallDistance = 0.0F) {
            e = (Entity)var1.next();
        }

        if (this.posY > (this.world.provider instanceof IExitHeight ? ((IExitHeight)this.world.provider).getYCoordinateToTeleport() : 1200.0D) && this.launchPhase != EntitySpaceshipBase.EnumLaunchPhase.LANDING.ordinal()) {
            this.onReachAtmosphere();
        }

        if (this.rollAmplitude > 0.0F) {
            --this.rollAmplitude;
        }

        if (this.shipDamage > 0.0F) {
            --this.shipDamage;
        }

        if (!this.world.isRemote) {
            if (this.posY < 0.0D) {
                this.setDead();
            } else if (this.posY > (this.world.provider instanceof IExitHeight ? ((IExitHeight)this.world.provider).getYCoordinateToTeleport() : 1200.0D) + (double)(this.launchPhase == EntitySpaceshipBase.EnumLaunchPhase.LANDING.ordinal() ? 355 : 100)) {
                var1 = this.getPassengers().iterator();

                while(var1.hasNext()) {
                    e = (Entity)var1.next();
                    if (e instanceof EntityPlayerMP) {
                        GCPlayerStats stats = GCPlayerStats.get(e);
                        if (stats.isUsingPlanetSelectionGui()) {
                            this.setDead();
                        }
                    } else {
                        this.setDead();
                    }
                }
            }

            if (this.timeSinceLaunch > 50.0F && this.onGround) {
                this.failRocket();
            }
        }

        if (this.launchPhase == EntitySpaceshipBase.EnumLaunchPhase.UNIGNITED.ordinal()) {
            this.timeUntilLaunch = this.getPreLaunchWait();
        }

        if (this.launchPhase >= EntitySpaceshipBase.EnumLaunchPhase.LAUNCHED.ordinal()) {
            ++this.timeSinceLaunch;
        } else {
            this.timeSinceLaunch = 0.0F;
        }

        if (this.timeUntilLaunch > 0 && this.launchPhase == EntitySpaceshipBase.EnumLaunchPhase.IGNITED.ordinal()) {
            --this.timeUntilLaunch;
        }

        AxisAlignedBB box = this.getEntityBoundingBox().grow(0.2D);
        List<?> var15 = this.world.getEntitiesWithinAABBExcludingEntity(this, box);
        if (var15 != null && !var15.isEmpty()) {
            for (Object o : var15) {
                Entity var17 = (Entity) o;
                if (this.getPassengers().contains(var17)) {
                    var17.applyEntityCollision(this);
                }
            }
        }

        if (this.timeUntilLaunch == 0 && this.launchPhase == EntitySpaceshipBase.EnumLaunchPhase.IGNITED.ordinal()) {
            this.setLaunchPhase(EntitySpaceshipBase.EnumLaunchPhase.LAUNCHED);
            this.onLaunch();
        }

        if (this.rotationPitch > 90.0F) {
            this.rotationPitch = 90.0F;
        }

        if (this.rotationPitch < -90.0F) {
            this.rotationPitch = -90.0F;
        }

        this.motionX = -(50.0D * Math.cos((double)this.rotationYaw / 57.29577951308232D) * Math.sin((double)this.rotationPitch * 0.01D / 57.29577951308232D));
        this.motionZ = -(50.0D * Math.sin((double)this.rotationYaw / 57.29577951308232D) * Math.sin((double)this.rotationPitch * 0.01D / 57.29577951308232D));
        if (this.launchPhase < EntitySpaceshipBase.EnumLaunchPhase.LAUNCHED.ordinal()) {
            this.motionX = this.motionY = this.motionZ = 0.0D;
        }

        if (this.world.isRemote) {
            this.setPosition(this.posX, this.posY, this.posZ);
            if (this.shouldMoveClientSide()) {
                this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            }
        } else {
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        }

        this.setRotation(this.rotationYaw, this.rotationPitch);
        if (this.world.isRemote) {
            this.setPosition(this.posX, this.posY, this.posZ);
        }

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (!this.world.isRemote && this.ticks % 3L == 0L) {
            GalacticraftCore.packetPipeline.sendToDimension(new PacketDynamic(this), this.world.provider.getDimension());
        }

        if (this.launchPhase >= EntitySpaceshipBase.EnumLaunchPhase.LAUNCHED.ordinal() && this.getPassengers().size() >= 1 && this.getPassengers().get(0) instanceof EntityPlayerMP) {
            GCTriggers.LAUNCH_ROCKET.trigger((EntityPlayerMP)this.getPassengers().get(0));
        }

        if (!this.world.isRemote) {
            if (this.statusMessageCooldown > 0) {
                --this.statusMessageCooldown;
            }
            this.lastStatusMessageCooldown = this.statusMessageCooldown;
        }

        if (this.launchPhase >= EnumLaunchPhase.IGNITED.ordinal()) {
            if (this.rocketSoundUpdater != null) {
                this.rocketSoundUpdater.update();
                this.rocketSoundToStop = true;
            }
        } else if (this.rocketSoundToStop) {
            this.stopRocketSound();
            if (this.rocketSoundUpdater != null) {
                FMLClientHandler.instance().getClient().getSoundHandler().stopSound((ISound)this.rocketSoundUpdater);
            }

            this.rocketSoundUpdater = null;
        }
    }

    public void onUpdate() {
        if(this.launchPhase != EnumLaunchPhase.LAUNCHED.ordinal() &&
                this.launchPhase != EnumLaunchPhase.IGNITED.ordinal() &&
                this.launchPhase != EnumLaunchPhase.LANDING.ordinal()) {
            updateMCSPos();
            padPos = getPosition();
            targetVec = padPos;
        }
        if (this.launchPhase >= EnumLaunchPhase.LAUNCHED.ordinal() && this.hasValidFuel()) {
            double motionScalar = this.timeSinceLaunch / 250.0F;
            motionScalar = Math.min(motionScalar, 1.0D);
            motionScalar *= 5.0D;
            if (this.launchPhase != EnumLaunchPhase.LANDING.ordinal() && motionScalar != 0.0D) {
                this.motionY = -motionScalar * Math.cos((double)(this.rotationPitch - 180.0F) / 57.29577951308232D);
            }

            double multiplier = 1.0D;
            if (this.world.provider instanceof IGalacticraftWorldProvider) {
                multiplier = ((IGalacticraftWorldProvider)this.world.provider).getFuelUsageMultiplier();
                if (multiplier <= 0.0D) {
                    multiplier = 1.0D;
                }
            }

            if (this.timeSinceLaunch % (float)MathHelper.floor(3.0D * (1.0D / multiplier)) == 0.0F) {
                this.removeFuel(1);
                if (!this.hasValidFuel()) {
                    this.stopRocketSound();
                }
            }
        } else if (!this.hasValidFuel() && this.getLaunched() && Math.abs(Math.sin(this.timeSinceLaunch / 1000.0F)) / 10.0D != 0.0D) {
            this.motionY -= Math.abs(Math.sin(this.timeSinceLaunch / 1000.0F)) / 20.0D;
        }

        if(isMining) {
            if(miningDone) {
                if (padPos.equals(new BlockPos(0, 0, 0))) {
                    setDead();
                    return;
                }
                if (launchPhase != EnumLaunchPhase.LANDING.ordinal() && launchPhase != EnumLaunchPhase.UNIGNITED.ordinal()) {
                    this.launchPhase = EnumLaunchPhase.LANDING.ordinal();
                    this.setPosition((float) padPos.getX() + 0.5F, padPos.getY() + 800, (float) padPos.getZ() + 0.5F);
                    this.motionY = -5;
                }
            } else {
                if(!world.isRemote) {
                    try {
                        mineResources();
                    } catch (NullPointerException ignored) {
                        isMining = true;
                        miningDone = true;
                    }
                    return; //stops rocket update while actually mining
                }
            }
        }

        try {
            parentUpdate();
        } catch (NullPointerException e) {
            GalacticResearch.instance.logger.log(Level.FATAL,e.getMessage());
            setDead();
            return;
        }


        if (this.rumble > 0.0F) {
            --this.rumble;
        }

        if (this.rumble < 0.0F) {
            ++this.rumble;
        }

        if (this.launchPhase >= EnumLaunchPhase.IGNITED.ordinal()) {
            this.performHurtAnimation();
            this.rumble = (float)this.rand.nextInt(3) - 3.0F;
        }

        int i;
        if (this.timeUntilLaunch >= 100) {
            i = Math.abs(this.timeUntilLaunch / 100);
        } else {
            i = 1;
        }

        if ((this.getLaunched() || this.launchPhase == EnumLaunchPhase.IGNITED.ordinal() && this.rand.nextInt(i) == 0) && !ConfigManagerCore.disableSpaceshipParticles && this.hasValidFuel() && this.world.isRemote) {
            this.spawnParticles(this.getLaunched());
        }
    }

    @Override
    public void onReachAtmosphere() {
        if (this.world.isRemote)
        {
            this.stopRocketSound();
            return;
        }
        TileMissionControlStation te = getMCS();
        if(te != null) {
            te.setMissionInfo(mission, 1);
        }
        isMining = true;
    }


    public void setMission(String name)
    {
        mission = name;
    }

    public TileMissionControlStation getMCS()
    {
        TileEntityLandingPad pad = (TileEntityLandingPad) getLandingPad();
        if(pad == null) {
            return (TileMissionControlStation) world.getTileEntity(mcsPos);
        }
        for (ILandingPadAttachable te : pad.getConnectedTiles()) {
            if (te instanceof TileMissionControlStation) {
                return  (TileMissionControlStation) te;
            }
        }
        return null;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        if (!this.world.isRemote) {
            nbt.setInteger("Type", this.rocketType.getIndex());
            nbt.setString("mission", mission);
            nbt.setBoolean("isMining", isMining);
            nbt.setIntArray("padPos", new int[] {padPos.getX(), mcsPos.getY(), mcsPos.getZ()});
            nbt.setIntArray("mcsPos", new int[] {mcsPos.getX(), mcsPos.getY(), mcsPos.getZ()});
            super.writeEntityToNBT(nbt);
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        this.rocketType = EnumRocketType.values()[nbt.getInteger("Type")];
        this.mission = nbt.getString("mission");
        this.isMining = nbt.getBoolean("isMining");
        int[] raw = nbt.getIntArray("mcsPos");
        try {
            this.mcsPos = new BlockPos(raw[0], raw[1], raw[2]);
            raw = nbt.getIntArray("padPos");
            this.padPos = new BlockPos(raw[0], raw[1], raw[2]);
            this.targetVec = padPos;
        } catch (IndexOutOfBoundsException ignored) {

        }
        super.readEntityFromNBT(nbt);
    }

    public EnumRocketType getType() {
        return this.rocketType;
    }

    public int getSizeInventory() {
        return this.rocketType == null ? 0 : this.rocketType.getInventorySpace();
    }

    public void onWorldTransferred(World world) {
        //this.setDead();
    }

    @Override
    public int getRocketTier() {
        return 2047483647;
    }
    @Override
    public int getPreLaunchWait() {
        return 5;
    }
    @Override
    public List<ItemStack> getItemsDropped(List<ItemStack> droppedItemList) {
        super.getItemsDropped(droppedItemList);
        ItemStack rocket = new ItemStack(MINING_ROCKET, 1, 0);
        rocket.setTagCompound(new NBTTagCompound());
        assert rocket.getTagCompound() != null;
        rocket.getTagCompound().setInteger("RocketFuel", this.fuelTank.getFluidAmount());
        droppedItemList.add(rocket);
        return droppedItemList;
    }
}
