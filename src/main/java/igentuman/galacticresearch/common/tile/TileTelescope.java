package igentuman.galacticresearch.common.tile;

import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.client.gui.GuiTelescope;
import igentuman.galacticresearch.common.block.BlockTelescope;
import igentuman.galacticresearch.sky.SkyModel;
import igentuman.galacticresearch.sky.body.ISkyBody;
import igentuman.galacticresearch.sky.body.Researchable;
import micdoodle8.mods.galacticraft.annotations.ForRemoval;
import micdoodle8.mods.galacticraft.annotations.ReplaceWith;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseElectricBlockWithInventory;
import micdoodle8.mods.miccore.Annotations;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Arrays;
import java.util.List;

public class TileTelescope extends TileBaseElectricBlockWithInventory implements ISidedInventory {

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public int xAngle = GuiTelescope.viewportSize/2;

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public int yAngle = GuiTelescope.viewportSize/2;

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public int dimension;

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public int movementAmplifier = 1;

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public int observationTime = 0;

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public String curObserveBody = "";

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public String researchedBodies = "";

    public int operationCooldown = 10;
    private int deltaX;
    private int deltaY;

    public TileTelescope() {
        super("container.telescope.name");
        this.storage.setMaxExtract(45.0F);
        this.inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    }

    public void rotateX(int x)
    {
        deltaX = x*10*movementAmplifier;
    }

    public void rotateY(int y)
    {
        deltaY = y*10*movementAmplifier;
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

    public void changeMultiplier()
    {
        movementAmplifier++;
        if(movementAmplifier>5) {
            movementAmplifier = 1;
        }
    }

    public void doMovement()
    {
        if(operationCooldown > 0) {
            operationCooldown--;
            return;
        }

        if (deltaX != 0) {
            if (deltaX > 0) {
                xAngle += movementAmplifier;
                deltaX-= movementAmplifier;
            } else {
                xAngle -= movementAmplifier;
                deltaX+= movementAmplifier;
            }
            xAngle = (int) Math.min(SkyModel.width - GuiTelescope.viewportSize*1.5, Math.max(GuiTelescope.viewportSize/2, xAngle));
        }
        if (deltaY != 0) {
            if (deltaY > 0) {
                yAngle += movementAmplifier;
                deltaY-= movementAmplifier;
            } else {
                yAngle -= movementAmplifier;
                deltaY+= movementAmplifier;
            }
            yAngle = (int) Math.min(SkyModel.height - GuiTelescope.viewportSize*1.5, Math.max(GuiTelescope.viewportSize/2, yAngle));
        }

        if(deltaX == 0 & deltaY == 0) {
            operationCooldown = 10;
        }
    }

    public boolean isBodyResearched(String name)
    {
        return Arrays.stream(researchedBodies.split(",")).anyMatch(val -> val.equals(name));
    }
    private int looseCounter = 100;

    public boolean looseCounterEnd()
    {
        observationTime--;
        looseCounter--;
        return looseCounter < 1;
    }

    public void observe()
    {
        List<Researchable> bodies = SkyModel.get().getObjectsToResearch(dimension);
        if(bodies == null) return;
        for (Researchable b: bodies) {
            if(Arrays.stream(ModConfig.researchSystem.default_researched_bodies).anyMatch(s -> s.equals(b.getBody().getName()))) {
                continue;
            }
            int x = b.getX();
            int y = b.getY();
            int padding = 25;
            if(
                    x > xAngle + padding && x < (xAngle + GuiTelescope.viewportSize-padding-b.getSize()) &&
                    y > yAngle + padding && y < (yAngle + GuiTelescope.viewportSize-padding-b.getSize())
               ) {
                if(!isBodyResearched(b.getBody().getName())) {
                    if(!curObserveBody.equals(b.getBody().getName())) {
                        observationTime = 0;
                        curObserveBody = b.getBody().getName();
                        looseCounter = 100;
                    }
                    observationTime++;
                    if(observationTime/20 >= ModConfig.researchSystem.required_observation_time) {
                        researchedBodies += ","+curObserveBody;
                        observationTime = 0;
                        curObserveBody = "";
                        markDirty();
                    }
                    return;
                }
            }
        }
        if(looseCounterEnd()) {
            observationTime = 0;
            curObserveBody = "";
        }
    }

    public int getObservationProgress()
    {
        if(curObserveBody.isEmpty()) {
            observationTime = 0;
            return 0;
        }
        return (int)(100/(float)ModConfig.researchSystem.required_observation_time*((float)observationTime/20));
    }

    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            if(getEnergyStoredGC() < 100) {
                return;
            }
            doMovement();
            observe();
        }

    }

    public String[] getResearchedBodiesArray()
    {
        return Arrays.stream(researchedBodies.split(",")).filter(val -> !val.isEmpty()).toArray(String[]::new);
    }

    public String getResearchedBodies()
    {
        return researchedBodies;
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
        this.xAngle = nbt.getInteger("xAngle");
        this.yAngle = nbt.getInteger("yAngle");
        this.movementAmplifier = nbt.getInteger("movementAmplifier");
        this.operationCooldown = nbt.getInteger("operationCooldown");
        this.observationTime = nbt.getInteger("observationTime");
        this.researchedBodies = nbt.getString("researchedBodies");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("dimension", this.dimension);
        nbt.setInteger("xAngle", this.xAngle);
        nbt.setInteger("yAngle", this.yAngle);
        nbt.setInteger("movementAmplifier", this.movementAmplifier);
        nbt.setInteger("operationCooldown", this.operationCooldown);
        nbt.setInteger("observationTime", this.observationTime);
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
}
