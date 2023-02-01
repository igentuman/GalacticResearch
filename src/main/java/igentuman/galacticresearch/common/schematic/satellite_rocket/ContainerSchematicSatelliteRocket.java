package igentuman.galacticresearch.common.schematic.satellite_rocket;

import igentuman.galacticresearch.common.CommonProxy;
import micdoodle8.mods.galacticraft.api.recipe.INasaWorkbenchRecipe;
import micdoodle8.mods.galacticraft.core.inventory.SlotRocketBenchResult;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class ContainerSchematicSatelliteRocket extends Container
{

    public InventorySchematicSatelliteRocket craftMatrix = new InventorySchematicSatelliteRocket(this);
    public IInventory craftResult = new InventoryCraftResult();
    private final World world;

    public ContainerSchematicSatelliteRocket(InventoryPlayer par1InventoryPlayer, BlockPos pos)
    {
        int change = 27;
        this.world = par1InventoryPlayer.player.world;
        this.addSlotToContainer(new SlotRocketBenchResult(par1InventoryPlayer.player, this.craftMatrix, this.craftResult, 0, 142, 69 + change));
        int var6;
        int var7;
        int slotId = 1;
        // Cone
        this.addSlotToContainer(new SlotSchematicSatelliteRocket(this.craftMatrix, slotId++, 48, 9 + change, pos, par1InventoryPlayer.player));

        this.addSlotToContainer(new SlotSchematicSatelliteRocket(this.craftMatrix, slotId++, 48, 9 + 18 + change, pos, par1InventoryPlayer.player));

        // Body
        for (var6 = 1; var6 < 3; ++var6)
        {
            this.addSlotToContainer(new SlotSchematicSatelliteRocket(this.craftMatrix, slotId++, 39, -7 + var6 * 18 + 16 + 18 + change, pos, par1InventoryPlayer.player));
        }

        // Body Right
        for (var6 = 1; var6 < 3; ++var6)
        {
            this.addSlotToContainer(new SlotSchematicSatelliteRocket(this.craftMatrix, slotId++, 57, -7 + var6 * 18 + 16 + 18 + change, pos, par1InventoryPlayer.player));
        }

        // Left fins
        this.addSlotToContainer(new SlotSchematicSatelliteRocket(this.craftMatrix, slotId++, 30, 81 + change, pos, par1InventoryPlayer.player));

        // Engine
        this.addSlotToContainer(new SlotSchematicSatelliteRocket(this.craftMatrix, slotId++, 48, 81 + change, pos, par1InventoryPlayer.player));

        // Right fins
        this.addSlotToContainer(new SlotSchematicSatelliteRocket(this.craftMatrix, slotId, 66, 81 + change, pos, par1InventoryPlayer.player));

        change = 9;

        // Player inv:

        for (var6 = 0; var6 < 3; ++var6)
        {
            for (var7 = 0; var7 < 9; ++var7)
            {
                this.addSlotToContainer(new Slot(par1InventoryPlayer, var7 + var6 * 9 + 9, 8 + var7 * 18, 129 + var6 * 18 + change));
            }
        }

        for (var6 = 0; var6 < 9; ++var6)
        {
            this.addSlotToContainer(new Slot(par1InventoryPlayer, var6, 8 + var6 * 18, 18 + 169 + change));
        }

        this.onCraftMatrixChanged(this.craftMatrix);
    }

    @Override
    public void onContainerClosed(EntityPlayer par1EntityPlayer)
    {
        super.onContainerClosed(par1EntityPlayer);

        if (!this.world.isRemote)
        {
            for (int var2 = 1; var2 < this.craftMatrix.getSizeInventory(); ++var2)
            {
                final ItemStack var3 = this.craftMatrix.removeStackFromSlot(var2);

                if (!var3.isEmpty())
                {
                    par1EntityPlayer.entityDropItem(var3, 0.0F);
                }
            }
        }
    }
    @Nonnull
    public static ItemStack findMatchingSatelliteRocketRecipe(InventorySchematicSatelliteRocket inventoryRocketBench) {
        Iterator var1 = CommonProxy.getSatelliteRocketRecipes().iterator();

        INasaWorkbenchRecipe recipe;
        do {
            if (!var1.hasNext()) {
                return ItemStack.EMPTY;
            }

            recipe = (INasaWorkbenchRecipe)var1.next();
        } while(!recipe.matches(inventoryRocketBench));

        return recipe.getRecipeOutput();
    }

    @Override
    public void onCraftMatrixChanged(IInventory par1IInventory)
    {
        this.craftResult.setInventorySlotContents(0, findMatchingSatelliteRocketRecipe(this.craftMatrix));
    }

    @Override
    public boolean canInteractWith(EntityPlayer par1EntityPlayer)
    {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par1)
    {
        ItemStack var2 = ItemStack.EMPTY;
        final Slot var3 = this.inventorySlots.get(par1);

        if (var3 != null && var3.getHasStack())
        {
            final ItemStack var4 = var3.getStack();
            var2 = var4.copy();

            boolean done = false;
            if (par1 <= 9)
            {
                if (!this.mergeItemStack(var4, 13, 49, false))
                {
                    return ItemStack.EMPTY;
                }

                if (par1 == 0)
                {
                    var3.onSlotChange(var4, var2);
                }
            } else
            {
                for (int i = 1; i < 10; i++)
                {
                    Slot testSlot = this.inventorySlots.get(i);
                    if (!testSlot.getHasStack() && testSlot.isItemValid(var2))
                    {
                        if (!this.mergeOneItem(var4, i, i + 1, false))
                        {
                            return ItemStack.EMPTY;
                        }
                        break;
                    }
                }
            }

            if (var4.isEmpty())
            {
                var3.putStack(ItemStack.EMPTY);
            } else
            {
                var3.onSlotChanged();
            }

            if (var4.getCount() == var2.getCount())
            {
                return ItemStack.EMPTY;
            }

            var3.onTake(par1EntityPlayer, var4);
        }

        return var2;
    }

    protected boolean mergeOneItem(ItemStack par1ItemStack, int par2, int par3, boolean par4)
    {
        boolean flag1 = false;
        if (!par1ItemStack.isEmpty())
        {
            Slot slot;
            ItemStack slotStack;

            for (int k = par2; k < par3; k++)
            {
                slot = this.inventorySlots.get(k);
                slotStack = slot.getStack();

                if (slotStack.isEmpty())
                {
                    ItemStack stackOneItem = par1ItemStack.copy();
                    stackOneItem.setCount(1);
                    par1ItemStack.shrink(1);
                    slot.putStack(stackOneItem);
                    slot.onSlotChanged();
                    flag1 = true;
                    break;
                }
            }
        }

        return flag1;
    }
}
