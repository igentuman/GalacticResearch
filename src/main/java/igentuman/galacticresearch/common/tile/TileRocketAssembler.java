package igentuman.galacticresearch.common.tile;

import igentuman.galacticresearch.common.CommonProxy;
import igentuman.galacticresearch.common.block.BlockTelescope;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.recipe.INasaWorkbenchRecipe;
import micdoodle8.mods.galacticraft.api.transmission.NetworkType;
import micdoodle8.mods.galacticraft.core.blocks.BlockMachine2;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseElectricBlockWithInventory;
import micdoodle8.mods.galacticraft.core.recipe.NasaWorkbenchRecipe;
import micdoodle8.mods.galacticraft.core.tile.IMachineSides;
import micdoodle8.mods.galacticraft.core.tile.IMachineSidesProperties;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.miccore.Annotations;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TileRocketAssembler extends TileBaseElectricBlockWithInventory implements ISidedInventory, IMachineSides {

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public int processTicks = 0;
    private IMachineSides.MachineSidePack[] machineSides;
    private List<INasaWorkbenchRecipe> knownRecipes = new ArrayList<>();

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public int currentRecipe = 0;

    public TileRocketAssembler() {
        super("tile.rocket_assembler.name");
        this.storage.setMaxExtract(ConfigManagerCore.hardMode ? 40.0F : 20.0F);
        this.setTierGC(1);
        this.inventory = NonNullList.withSize(30, ItemStack.EMPTY);
    }

    public List<INasaWorkbenchRecipe> getAllRecipes() {
        if(knownRecipes.isEmpty()) {
            List<INasaWorkbenchRecipe> t1 = GalacticraftRegistry.getRocketT1Recipes();
            List<INasaWorkbenchRecipe> t2 = GalacticraftRegistry.getRocketT2Recipes();
            List<INasaWorkbenchRecipe> t3 = GalacticraftRegistry.getRocketT3Recipes();
            List<INasaWorkbenchRecipe> cargo = GalacticraftRegistry.getCargoRocketRecipes();
            List<INasaWorkbenchRecipe> mining = CommonProxy.getMiningRocketRecipes();
            List<INasaWorkbenchRecipe> satellite = CommonProxy.getSatelliteRocketRecipes();
            knownRecipes = new ArrayList<>();
            knownRecipes.addAll(t1);
            knownRecipes.addAll(t2);
            knownRecipes.addAll(t3);
            knownRecipes.addAll(cargo);
            knownRecipes.addAll(mining);
            knownRecipes.addAll(satellite);
        }
        return knownRecipes;
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
            if (this.canProcess()) {
                if (this.processTicks == 0) {
                    this.processTicks = 100;
                } else {
                    if (this.processTicks > 0) {
                        this.processTicks--;
                        if (this.processTicks == 0) {
                            this.doProcess();
                        }
                    }
                }
            } else {
                this.processTicks = 0;
            }
        }
    }

    private void doProcess() {
        inventory.set(1, getRecipe().getRecipeOutput());
        for(ItemStack input: getRecipe().getRecipeInput().values()) {
            int toShrink = input.getCount();
            for(int i = 2; i < inventory.size(); i++) {
                if(inventory.get(i).isItemEqual(input)) {
                    int avail = inventory.get(i).getCount();
                    inventory.get(i).shrink(toShrink);
                    if(avail < toShrink) {
                        toShrink -= avail;
                    } else {
                        break;
                    }
                }
            }
        }
    }

    private boolean canProcess() {
        if (!this.getStackInSlot(1).isEmpty()) {
            return false;
        }
        return recipeMatches();
    }

    //recipe matches() doesn't work for us
    public boolean recipeMatches() {
        HashMap<ItemStack, Integer> nestedInputs = new HashMap<>();
        for (ItemStack input : getRecipe().getRecipeInput().values()) {
            boolean isNew = true;
            for(ItemStack nested: nestedInputs.keySet()) {
                if(nested.isItemEqual(input)) {
                    nestedInputs.put(nested, nestedInputs.get(nested) + 1);
                    isNew = false;
                    break;
                }
            }
            if(isNew) {
                nestedInputs.put(input, 1);
            }
        }
        for(ItemStack input: nestedInputs.keySet()) {
            int left = nestedInputs.get(input);
            for(ItemStack inv: inventory) {
                if(inv.isItemEqual(input)) {
                    left -= inv.getCount();
                    if(left < 1) {
                        break;
                    }
                }
            }
            if(left > 0) {
                return false;
            }
        }
        return true;
    }

    private INasaWorkbenchRecipe getRecipe() {
        return getAllRecipes().get(currentRecipe);
    }

    public void nextRecipe()
    {
        currentRecipe++;
        currentRecipe = Math.min(currentRecipe, getAllRecipes().size() - 1);
    }

    public void prevRecipe()
    {
        currentRecipe--;
        currentRecipe = Math.max(currentRecipe, 0);
    }

    public IMachineSidesProperties getConfigurationType() {
        return BlockMachine2.MACHINESIDES_RENDERTYPE;
    }

    public IMachineSides.MachineSide[] listConfigurableSides() {
        return new IMachineSides.MachineSide[]{MachineSide.ELECTRIC_IN};
    }

    public IMachineSides.Face[] listDefaultFaces() {
        return new IMachineSides.Face[]{Face.LEFT, Face.RIGHT, Face.BOTTOM};
    }

    public synchronized IMachineSides.MachineSidePack[] getAllMachineSides() {
        if (this.machineSides == null) {
            this.initialiseSides();
        }

        return this.machineSides;
    }

    public void setupMachineSides(int length) {
        this.machineSides = new IMachineSides.MachineSidePack[length];
    }

    public EnumFacing getFront() {
        return this.byIndex();
    }

    public int[] getSlotsForFace(EnumFacing side) {
        if (side == EnumFacing.DOWN) {
            return new int[]{1};
        } else if(side == getFront().rotateY() || side == getFront().rotateYCCW() || side == EnumFacing.UP) {
            return new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29};
        }
        return new int[]{0};
    }

    public boolean shouldUseEnergy() {
        return !this.getDisabled(0);
    }

    public boolean canConnect(EnumFacing direction, NetworkType type) {
        if (direction != null && type == NetworkType.POWER) {
            return world.getBlockState(pos).getValue(BlockTelescope.FACING).rotateY() == direction
                    || world.getBlockState(pos).getValue(BlockTelescope.FACING).rotateYCCW() == direction;
        } else {
            return false;
        }
    }

    public boolean canInsertItem(int slotID, ItemStack itemstack, EnumFacing side) {
        if(side == EnumFacing.UP && slotID == 0) {
            return ItemElectricBase.isElectricItem(itemstack.getItem());
        }
        return side == getFront().rotateY() || side == getFront().rotateYCCW() || side == EnumFacing.UP;
    }

    public boolean canExtractItem(int slotID, ItemStack itemstack, EnumFacing side) {
        return side.equals(EnumFacing.DOWN);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.storage.setCapacity(nbt.getFloat("maxEnergy"));
        this.currentRecipe = nbt.getInteger("currentRecipe");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setFloat("maxEnergy", this.getMaxEnergyStoredGC());
        nbt.setInteger("currentRecipe", currentRecipe);
        return nbt;
    }

    public void switchRecipe(Integer val) {
        if(val > 0) {
            nextRecipe();
        } else {
            prevRecipe();
        }
        markDirty();
    }

    public ItemStack getResultItem() {
        INasaWorkbenchRecipe recipe = getAllRecipes().get(currentRecipe);
        return recipe.getRecipeOutput();
    }
}
