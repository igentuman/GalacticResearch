package igentuman.galacticresearch.common.entity;

import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import micdoodle8.mods.galacticraft.api.entity.IRocketType;
import micdoodle8.mods.galacticraft.api.entity.IWorldTransferCallback;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.api.tile.ILandingPadAttachable;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.tile.TileEntityLandingPad;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import static igentuman.galacticresearch.RegistryHandler.SATELLITE_ROCKET;

public class EntitySatelliteRocket extends EntityAutoRocket implements IRocketType, IInventory, IWorldTransferCallback, IGRAutoRocket {
    public EnumRocketType rocketType;
    public float rumble;
    public BlockPos mcsPos = new BlockPos(0,0,0);
    public String mission;
    public int researchCounter = ModConfig.machines.satellite_mission_duration*20+100;
    public boolean isResearching = false;

    public EntitySatelliteRocket(World par1World) {
        super(par1World);
        this.setSize(0.98F, 2.0F);
    }

    public EntitySatelliteRocket(World par1World, double par2, double par4, double par6, EnumRocketType rocketType) {
        super(par1World, par2, par4, par6);
        this.rocketType = rocketType;
        this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        this.setSize(0.98F, 2.0F);
    }

    public int getFuelTankCapacity() {
        return 2000;
    }

    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(SATELLITE_ROCKET, 1, 0);
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

    @Override
    public void setAutolaunchSetting(EnumAutoLaunch setting) {
        autoLaunchSetting = setting;
    }

    public void onUpdate() {
        if(this.launchPhase != EnumLaunchPhase.LAUNCHED.ordinal() && this.launchPhase != EnumLaunchPhase.IGNITED.ordinal()) {
            updateMCSPos();
        }
        if (this.launchPhase >= EnumLaunchPhase.LAUNCHED.ordinal() && this.hasValidFuel()) {
            double motionScalar = (double)(this.timeSinceLaunch / 250.0F);
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
        } else if (!this.hasValidFuel() && this.getLaunched() && Math.abs(Math.sin((double)(this.timeSinceLaunch / 1000.0F))) / 10.0D != 0.0D) {
            this.motionY -= Math.abs(Math.sin((double)(this.timeSinceLaunch / 1000.0F))) / 20.0D;
        }
        if(isResearching) {
            if(researchCounter > 0) {
                researchCounter--;
            } else {
                setDead();
                return;
            }

        }
        super.onUpdate();
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

    protected boolean shouldMoveClientSide() {
        return true;
    }

    protected void spawnParticles(boolean launched) {
        double sinPitch = Math.sin((double)this.rotationPitch / 57.29577951308232D);
        double x1 = 2.0D * Math.cos((double)this.rotationYaw / 57.29577951308232D) * sinPitch;
        double z1 = 2.0D * Math.sin((double)this.rotationYaw / 57.29577951308232D) * sinPitch;
        double y1 = 2.0D * Math.cos((double)(this.rotationPitch - 180.0F) / 57.29577951308232D);
        double y;
        if (this.launchPhase == EnumLaunchPhase.LANDING.ordinal() && this.targetVec != null) {
            y = this.posY - (double)this.targetVec.getY();
            y = Math.max(y, 1.0D);
            x1 *= y / 60.0D;
            y1 *= y / 60.0D;
            z1 *= y / 60.0D;
        }

        y = this.prevPosY + (this.posY - this.prevPosY) - 0.4D;
        if (!this.isDead) {
            EntityLivingBase riddenByEntity = !this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof EntityLivingBase ? (EntityLivingBase)this.getPassengers().get(0) : null;
            GalacticraftCore.proxy.spawnParticle(this.getLaunched() ? "launchFlameLaunched" : "launchFlameIdle", new Vector3(this.posX + 0.2D - this.rand.nextDouble() / 10.0D + x1, y, this.posZ + 0.2D - this.rand.nextDouble() / 10.0D + z1), new Vector3(x1, y1, z1), new Object[]{riddenByEntity});
            GalacticraftCore.proxy.spawnParticle(this.getLaunched() ? "launchFlameLaunched" : "launchFlameIdle", new Vector3(this.posX - 0.2D + this.rand.nextDouble() / 10.0D + x1, y, this.posZ + 0.2D - this.rand.nextDouble() / 10.0D + z1), new Vector3(x1, y1, z1), new Object[]{riddenByEntity});
            GalacticraftCore.proxy.spawnParticle(this.getLaunched() ? "launchFlameLaunched" : "launchFlameIdle", new Vector3(this.posX - 0.2D + this.rand.nextDouble() / 10.0D + x1, y, this.posZ - 0.2D + this.rand.nextDouble() / 10.0D + z1), new Vector3(x1, y1, z1), new Object[]{riddenByEntity});
            GalacticraftCore.proxy.spawnParticle(this.getLaunched() ? "launchFlameLaunched" : "launchFlameIdle", new Vector3(this.posX + 0.2D - this.rand.nextDouble() / 10.0D + x1, y, this.posZ - 0.2D + this.rand.nextDouble() / 10.0D + z1), new Vector3(x1, y1, z1), new Object[]{riddenByEntity});
            GalacticraftCore.proxy.spawnParticle(this.getLaunched() ? "launchFlameLaunched" : "launchFlameIdle", new Vector3(this.posX + x1, y, this.posZ + z1), new Vector3(x1, y1, z1), new Object[]{riddenByEntity});
            GalacticraftCore.proxy.spawnParticle(this.getLaunched() ? "launchFlameLaunched" : "launchFlameIdle", new Vector3(this.posX + 0.2D + x1, y, this.posZ + z1), new Vector3(x1, y1, z1), new Object[]{riddenByEntity});
            GalacticraftCore.proxy.spawnParticle(this.getLaunched() ? "launchFlameLaunched" : "launchFlameIdle", new Vector3(this.posX - 0.2D + x1, y, this.posZ + z1), new Vector3(x1, y1, z1), new Object[]{riddenByEntity});
            GalacticraftCore.proxy.spawnParticle(this.getLaunched() ? "launchFlameLaunched" : "launchFlameIdle", new Vector3(this.posX + x1, y, this.posZ + 0.2D + z1), new Vector3(x1, y1, z1), new Object[]{riddenByEntity});
            GalacticraftCore.proxy.spawnParticle(this.getLaunched() ? "launchFlameLaunched" : "launchFlameIdle", new Vector3(this.posX + x1, y, this.posZ - 0.2D + z1), new Vector3(x1, y1, z1), new Object[]{riddenByEntity});
        }

    }

    public void decodePacketdata(ByteBuf buffer) {
        this.rocketType = EnumRocketType.values()[buffer.readInt()];
        super.decodePacketdata(buffer);
        this.posX = buffer.readDouble() / 8000.0D;
        this.posY = buffer.readDouble() / 8000.0D;
        this.posZ = buffer.readDouble() / 8000.0D;
    }

    public void getNetworkedData(ArrayList<Object> list) {
        if (!this.world.isRemote) {
            list.add(this.rocketType != null ? this.rocketType.getIndex() : 0);
            super.getNetworkedData(list);
            list.add(this.posX * 8000.0D);
            list.add(this.posY * 8000.0D);
            list.add(this.posZ * 8000.0D);
        }
    }

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
    }

    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        return false;
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
            isResearching = true;
        }
        return null;
    }

    protected void writeEntityToNBT(NBTTagCompound nbt) {
        if (!this.world.isRemote) {
            nbt.setInteger("Type", this.rocketType.getIndex());
            nbt.setIntArray("mcsPos", new int[] {mcsPos.getX(), mcsPos.getY(), mcsPos.getZ()});
            super.writeEntityToNBT(nbt);
        }
    }

    protected void readEntityFromNBT(NBTTagCompound nbt) {
        this.rocketType = EnumRocketType.values()[nbt.getInteger("Type")];
        int[] raw = nbt.getIntArray("mcsPos");
        this.mcsPos = new BlockPos(raw[0],raw[1], raw[2]);
        super.readEntityFromNBT(nbt);
    }

    public EnumRocketType getType() {
        return this.rocketType;
    }

    public int getSizeInventory() {
        return this.rocketType == null ? 0 : this.rocketType.getInventorySpace();
    }

    public void onWorldTransferred(World world) {
        this.setDead();
    }

    public int getRocketTier() {
        return 1147483647;
    }

    public int getPreLaunchWait() {
        return 5;
    }

    public List<ItemStack> getItemsDropped(List<ItemStack> droppedItemList) {
        super.getItemsDropped(droppedItemList);
        ItemStack rocket = new ItemStack(SATELLITE_ROCKET, 1, 0);
        rocket.setTagCompound(new NBTTagCompound());
        rocket.getTagCompound().setInteger("RocketFuel", this.fuelTank.getFluidAmount());
        droppedItemList.add(rocket);
        return droppedItemList;
    }

    public boolean isPlayerRocket() {
        return false;
    }

    public double getOnPadYOffset() {
        return -0.05D;
    }

    public float getRenderOffsetY() {
        return -0.1F;
    }
}
