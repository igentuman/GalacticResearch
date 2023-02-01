package igentuman.galacticresearch.common.tile;

import igentuman.galacticresearch.common.block.BlockTelescope;
import igentuman.galacticresearch.common.data.DimensionProvider;
import micdoodle8.mods.galacticraft.annotations.ForRemoval;
import micdoodle8.mods.galacticraft.annotations.ReplaceWith;
import micdoodle8.mods.galacticraft.api.entity.IDockable;
import micdoodle8.mods.galacticraft.api.tile.IFuelDock;
import micdoodle8.mods.galacticraft.api.tile.ILandingPadAttachable;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseElectricBlockWithInventory;
import micdoodle8.mods.galacticraft.core.tile.TileEntityLandingPad;
import micdoodle8.mods.miccore.Annotations;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Arrays;

public class TileMissionControlStation extends TileBaseElectricBlockWithInventory implements ISidedInventory, ILandingPadAttachable {

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public int dimension;

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public String researchedBodies = "";

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public String targetPlanet = "";

    public DimensionProvider dimensionProvider;

    private TileTelescope telescope;

    public Object attachedDock = null;


    public TileMissionControlStation() {
        super("container.mission_control_staion.name");
        this.storage.setMaxExtract(45.0F);
        this.inventory = NonNullList.withSize(1, ItemStack.EMPTY);
        dimensionProvider = new DimensionProvider(this);
    }

    public String[] getPlanetsForSatellite()
    {
        return getTelescope().getResearchedBodies();
    }

    public boolean isSatteliteRocketFound()
    {
        return true;
    }

    public boolean isRocketFound()
    {
        return true;
    }

    @Override
    public boolean canExtractItem(int slotID, ItemStack itemstack, EnumFacing side)
    {
        return slotID == 0;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
    {
        return slotID == 0 && ItemElectricBase.isElectricItem(itemstack.getItem());
    }

    @Override
    public ItemStack getBatteryInSlot()
    {
        return this.getStackInSlot(0);
    }



    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            if(getEnergyStoredGC() < 100) {
                return;
            }
        }
    }

    public TileTelescope getTelescope() {
        if(telescope == null) {
            TileEntity te = world.getTileEntity(pos.offset(world.getBlockState(pos).getValue(BlockTelescope.FACING).rotateY()));
            if(te != null && te instanceof TileTelescope) {
                telescope = (TileTelescope) te;
                return telescope;
            }
            te = world.getTileEntity(pos.offset(world.getBlockState(pos).getValue(BlockTelescope.FACING).rotateY().getOpposite()));
            if(te != null && te instanceof TileTelescope) {
                telescope = (TileTelescope) te;
                return telescope;
            }
        }
        return telescope;
    }

    public String[] getResearchedBodies()
    {
        return Arrays.stream(researchedBodies.split(",")).filter(val -> !val.isEmpty()).toArray(String[]::new);
    }

    public boolean shouldUseEnergy() {
        return !this.getDisabled(0);
    }

    public int[] getSlotsForFace(EnumFacing side) {
        return side != this.getElectricInputDirection() ? new int[]{0} : new int[0];
    }

    public EnumFacing getElectricInputDirection() {
        return world.getBlockState(pos).getValue(BlockTelescope.FACING).getOpposite();
    }

    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.dimension = nbt.getInteger("dimension");
        this.researchedBodies = nbt.getString("researchedBodies");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("dimension", this.dimension);
        nbt.setString("researchedBodies", this.researchedBodies);
        return nbt;
    }

    /** @deprecated */
    @Deprecated
    @ForRemoval(
            deadline = "4.1.0"
    )
    @ReplaceWith("byIndex()")
    public EnumFacing getFront() {
        return this.byIndex();
    }

    @Override
    public boolean canAttachToLandingPad(IBlockAccess iBlockAccess, BlockPos blockPos) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileEntityLandingPad;
    }

    public void setAttachedPad(IFuelDock pad) {
        this.attachedDock = pad;
    }

}
