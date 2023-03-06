package igentuman.galacticresearch.common.tile;

import com.mjr.extraplanets.tileEntities.blocks.TileEntityTier2LandingPad;
import com.mojang.authlib.GameProfile;
import galaxyspace.systems.SolarSystem.planets.overworld.tile.TileEntityAdvLandingPad;
import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.util.GrFakePlayer;
import micdoodle8.mods.galacticraft.api.entity.IDockable;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntitySpaceshipBase;
import micdoodle8.mods.galacticraft.api.tile.IDisableableMachine;
import micdoodle8.mods.galacticraft.api.tile.IFuelDock;
import micdoodle8.mods.galacticraft.api.tile.ILandingPadAttachable;
import micdoodle8.mods.galacticraft.api.transmission.tile.IConnector;
import micdoodle8.mods.galacticraft.core.Constants;
import micdoodle8.mods.galacticraft.core.GCBlocks;
import micdoodle8.mods.galacticraft.core.blocks.BlockMulti;
import micdoodle8.mods.galacticraft.core.blocks.BlockMulti.EnumBlockMultiType;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseElectricBlockWithInventory;
import micdoodle8.mods.galacticraft.core.inventory.IInventoryDefaults;
import micdoodle8.mods.galacticraft.core.tile.IMultiBlock;
import micdoodle8.mods.galacticraft.core.tile.TileEntityLandingPad;
import micdoodle8.mods.galacticraft.core.tile.TileEntityMulti;
import micdoodle8.mods.miccore.Annotations;
import micdoodle8.mods.miccore.Annotations.NetworkedField;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class TileLaunchpadTower extends TileBaseElectricBlockWithInventory implements ILandingPadAttachable, IMultiBlock, IDisableableMachine, IInventoryDefaults, ISidedInventory, IConnector
{


    private boolean initialised = false;
    private AxisAlignedBB renderAABB;
    private Object attachedDock;
    public float cPos = -0.5f;

    @NetworkedField(targetSide = Side.CLIENT)
    public boolean disabled = false;

    @NetworkedField(targetSide = Side.CLIENT)
    public int disableCooldown = 0;

    @NetworkedField(targetSide = Side.CLIENT)
    public boolean autoMount = false;

    @NetworkedField(targetSide = Side.CLIENT)
    private String padCords = "";

    @NetworkedField(targetSide = Side.CLIENT)
    public boolean hasRocket = false;
    private int mountCountdown = 10;


    public TileLaunchpadTower()
    {
        super("tile.launchpad_tower.name");
        this.storage.setMaxExtract(100);
        this.setTierGC(1);
        inventory = NonNullList.withSize(15, ItemStack.EMPTY);
    }

    public void handleAutomount()
    {
        if(!autoMount) return;
        if(getEnergyStoredGC() > 300) {
            if(mountCountdown > 40) {
                mount();
                mountCountdown = 0;
            }
            mountCountdown++;
        }
    }

    @Override
    public void update()
    {
        super.update();

        if (!this.initialised)
        {
            this.initialised = this.initialiseMultiTiles(this.getPos(), this.world);
        }

        if (!this.world.isRemote)
        {
            hasRocket = getRocket() != null;
            if (this.disableCooldown > 0)
            {
                this.disableCooldown--;
            }
            handleAutomount();
        } else {
            updateCpos();
        }
    }

    protected boolean initialiseMultiTiles(BlockPos pos, World world)
    {
        if (world.isRemote)
            this.onCreate(world, pos);

        List<BlockPos> positions = new ArrayList<>();
        this.getPositions(pos, positions);
        boolean result = true;
        for (BlockPos vecToAdd : positions)
        {
            TileEntity tile = world.getTileEntity(vecToAdd);
            if (tile instanceof TileEntityMulti)
            {
                ((TileEntityMulti) tile).mainBlockPosition = pos;
            } else
            {
                result = false;
            }
        }
        return result;
    }

    private void updateCpos()
    {
        if(hasRocket && cPos > -0.25f) {
            cPos-=0.05;
        }
        if(!hasRocket && cPos < 0.1F) {
            cPos+=0.05;
        }
    }

    @Override
    public boolean onActivated(EntityPlayer entityPlayer)
    {
        return false;    
    }

    @Override
    public void onCreate(World world, BlockPos placedPosition)
    {
        List<BlockPos> positions = new LinkedList<>();
        this.getPositions(placedPosition, positions);
        for(BlockPos position: positions) {
            ((BlockMulti) GCBlocks.fakeBlock).makeFakeBlock(world, position, getPos(), 0);
        }
    }



    @Override
    public EnumBlockMultiType getMultiType()
    {
        return EnumBlockMultiType.CRYO_CHAMBER;
    }

    @Override
    public void getPositions(BlockPos placedPosition, List<BlockPos> positions)
    {
        int buildHeight = this.world.getHeight() - 1;
        int y = placedPosition.getY();

        if (++y > buildHeight)
        {
            return;
        }
        positions.add(new BlockPos(placedPosition.getX(), y, placedPosition.getZ()));

    }

    @Override
    public void onDestroy(TileEntity callingBlock)
    {
        final BlockPos thisBlock = getPos();
        List<BlockPos> positions = new LinkedList<>();
        this.getPositions(thisBlock, positions);

        for (BlockPos pos : positions)
        {
            IBlockState stateAt = this.world.getBlockState(pos);

            if (stateAt.getBlock() == GCBlocks.fakeBlock && (EnumBlockMultiType) stateAt.getValue(BlockMulti.MULTI_TYPE) == EnumBlockMultiType.DISH_LARGE)
            {
                if (this.world.isRemote && this.world.rand.nextDouble() < 0.05D)
                {
                    FMLClientHandler.instance().getClient().effectRenderer.addBlockDestroyEffects(pos, this.world.getBlockState(pos));
                }
                this.world.setBlockToAir(pos);
            }
        }
        this.world.destroyBlock(thisBlock, true);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.storage.setCapacity(nbt.getFloat("maxEnergy"));
        this.padCords = nbt.getString("padCords");
        this.setDisabled(0, nbt.getBoolean("disabled"));
        this.disableCooldown = nbt.getInteger("disabledCooldown");
        this.autoMount = nbt.getBoolean("autoMount");
        this.initialised = false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setFloat("maxEnergy", this.getMaxEnergyStoredGC());
        nbt.setString("padCords", this.padCords);
        nbt.setBoolean("autoMount", this.autoMount);
        nbt.setInteger("disabledCooldown", this.disableCooldown);
        nbt.setBoolean("disabled", this.getDisabled(0));
        return nbt;
    }

    @Override
    public EnumSet<EnumFacing> getElectricalInputDirections()
    {
        return EnumSet.noneOf(EnumFacing.class);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        if (this.renderAABB == null)
        {
            this.renderAABB = new AxisAlignedBB(pos.add(-3, 0, -3), pos.add(3, 8, 3));
        }
        return this.renderAABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared()
    {
        return Constants.RENDERDISTANCE_MEDIUM;
    }

    @Override
    public void setDisabled(int index, boolean disabled)
    {
        if (this.disableCooldown == 0)
        {
            this.disabled = disabled;
            this.disableCooldown = 20;
        }
    }

    @Override
    public EnumFacing getFront() {
        return EnumFacing.SOUTH;
    }

    @Override
    public boolean getDisabled(int index)
    {
        return this.disabled;
    }

    public boolean shouldUseEnergy() {
        return !this.getDisabled(0);
    }

    public int getScaledElecticalLevel(int i)
    {
        return (int) Math.floor(this.getEnergyStoredGC() * i / this.getMaxEnergyStoredGC());
    }

    public boolean canInsertItem(int slotID, ItemStack itemstack, EnumFacing side) {
        return (slotID != 0 && (side.equals(EnumFacing.WEST)) && itemstack.getItem().getTranslationKey().contains("rocket") && slotID < 8) || (side.equals(EnumFacing.SOUTH) && ItemElectricBase.isElectricItem(itemstack.getItem()));
    }

    public boolean canExtractItem(int slotID, ItemStack itemstack, EnumFacing side) {
        return side.equals(EnumFacing.DOWN) || side.equals(EnumFacing.EAST);
    }

    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
    {
        return (slotID == 0 && ItemElectricBase.isElectricItem(itemstack.getItem())) || (slotID < 8 && itemstack.getItem().getTranslationKey().contains("rocket"));
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side)
    {
        switch (side) {
            case WEST:
                return new int[] {1,2,3,4,5,6,7};
            case EAST:
                return new int[] {8,9,10,11,12,13,14};
            case DOWN:
                return new int[] {8,9,10,11,12,13,14};
            case SOUTH:
                return new int[] {0};
        }
        return new int[]
        {0};
    }

    public void setAttachedPad(IFuelDock pad) {
        this.attachedDock = pad;
        BlockPos bp = ((TileEntity) attachedDock).getPos();
        padCords = bp.getX()+","+bp.getY()+","+bp.getZ();
    }

    public BlockPos getPadCords()
    {
        if(padCords.isEmpty()) {
            return new BlockPos(0,0,0);
        }
        String[] cords = padCords.split(",");
        return new BlockPos(Integer.parseInt(cords[0]), Integer.parseInt(cords[1]), Integer.parseInt(cords[2]));
    }

    private static boolean isGSPadTile(TileEntity tile) {
        boolean result = false;
        if(GalacticResearch.hooks.GalaxySpaceLoaded) {
            result = tile instanceof TileEntityAdvLandingPad;
        }
        return result;
    }

    private static boolean isEPPadTile(TileEntity tile) {
        boolean result = false;
        if(GalacticResearch.hooks.ExtraPlanetsLoaded) {
            result = tile instanceof TileEntityTier2LandingPad;
        }
        return result;
    }

    public EntitySpaceshipBase getRocket()
    {
        if (attachedDock instanceof TileEntityLandingPad)
        {
            TileEntityLandingPad pad = ((TileEntityLandingPad) attachedDock);
            IDockable rocket = pad.getDockedEntity();
            if (rocket instanceof EntitySpaceshipBase)
            {
                return (EntitySpaceshipBase) rocket;
            }
        } else if(isGSPadTile((TileEntity) attachedDock)) {
            TileEntityAdvLandingPad pad = ((TileEntityAdvLandingPad) attachedDock);
            IDockable rocket = pad.getDockedEntity();
            if (rocket instanceof EntitySpaceshipBase)
            {
                return (EntitySpaceshipBase) rocket;
            }
        } else if(!padCords.isEmpty()) {
            TileEntity dock = world.getTileEntity(getPadCords());
            if(dock instanceof TileEntityLandingPad) {
                setAttachedPad((IFuelDock) dock);
                return getRocket();
            }
        }
        return null;
    }

    @Override
    public boolean canAttachToLandingPad(IBlockAccess iBlockAccess, BlockPos blockPos) {
        TileEntity tile = world.getTileEntity(blockPos);
        boolean flag = tile instanceof TileEntityLandingPad;
        if(flag) {
            setAttachedPad((IFuelDock) tile);
        }
        return flag;
    }

    public void toggleAutomount() {
        autoMount = !autoMount;
        markDirty();
    }

    public void mount() {
        boolean hasRocket = false;
        int ts = 0;
        for(int i = 1; i < inventory.size()/2+1; i++) {
            if(!inventory.get(i).isEmpty()) {
                hasRocket = true;
                ts=i;
                break;
            }
        }
        if(!hasRocket) return;
        if(attachedDock == null) return;
        EntitySpaceshipBase rocket = getRocket();
        if(rocket != null) return;
        ItemStack toPlace = inventory.get(ts);
        IFuelDock dock = ((IFuelDock)attachedDock);
        FakePlayer fplayer = new FakePlayer((WorldServer) world, GrFakePlayer.getProfile());
        fplayer.setHeldItem(EnumHand.MAIN_HAND, toPlace);
        BlockPos padPos = ((TileEntity) attachedDock).getPos();
        EnumActionResult result = fplayer.interactionManager.processRightClickBlock(
                fplayer, world, toPlace, EnumHand.MAIN_HAND, padPos,EnumFacing.DOWN, (float)padPos.getX(), (float)padPos.getY(), (float)padPos.getZ());
        if(result.equals(EnumActionResult.SUCCESS)) {
            inventory.set(ts,ItemStack.EMPTY);
        }
        //dock.dockEntity(toPlace.getItem());
    }

    public void unmount() {
        boolean hasSpace = false;
        int ts = 0;
        for(int i = inventory.size()/2+1; i < inventory.size(); i++) {
            if(inventory.get(i).isEmpty()) {
                hasSpace = true;
                ts=i;
                break;
            }
        }
        if(!hasSpace) return;
        if(attachedDock == null) return;
        EntitySpaceshipBase rocket = getRocket();
        if(rocket == null) return;
        List<ItemStack> tmp = new ArrayList<>();
        List<ItemStack> drops = rocket.getItemsDropped(tmp);
        for(ItemStack st: drops) {
            if(st.getItem().getTranslationKey().contains("rocket")) {
                inventory.set(ts,st);
                rocket.setDead();
                return;
            }
        }
    }
}
