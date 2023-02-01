package igentuman.galacticresearch.common.container;

import igentuman.galacticresearch.common.tile.TileTelescope;
import micdoodle8.mods.galacticraft.api.item.IItemElectric;
import micdoodle8.mods.galacticraft.core.client.gui.container.GuiContainerGC;
import micdoodle8.mods.galacticraft.core.energy.EnergyDisplayHelper;
import micdoodle8.mods.galacticraft.core.energy.EnergyUtil;
import micdoodle8.mods.galacticraft.core.inventory.SlotSpecific;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ContainerTelescope extends Container {
    private final TileTelescope te;

    public ContainerTelescope(TileTelescope te) {
        this.te = te;
    }

    public ContainerTelescope(InventoryPlayer par1InventoryPlayer, TileTelescope tile) {
        this.te = tile;
        this.addSlotToContainer(new SlotSpecific(te, 0, 8, 163, IItemElectric.class));
        for(int var6 = 0; var6 < 9; ++var6) {
            this.addSlotToContainer(new Slot(par1InventoryPlayer, var6, 8 + var6 * 18, 182));
        }
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par1)
    {
        ItemStack var2 = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(par1);
        final int b = this.inventorySlots.size();

        if (slot != null && slot.getHasStack())
        {
            final ItemStack stack = slot.getStack();
            var2 = stack.copy();

            if (par1 == 0)
            {
                if (!this.mergeItemStack(stack, b - 36, b, true))
                {
                    return ItemStack.EMPTY;
                }
            } else
            {
                if (EnergyUtil.isElectricItem(stack.getItem()))
                {
                    if (!this.mergeItemStack(stack, 0, 1, false))
                    {
                        return ItemStack.EMPTY;
                    }
                } else
                {
                    if (par1 < b - 9)
                    {
                        if (!this.mergeItemStack(stack, b - 9, b, false))
                        {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.mergeItemStack(stack, b - 36, b - 9, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (stack.getCount() == 0)
            {
                slot.putStack(ItemStack.EMPTY);
            } else
            {
                slot.onSlotChanged();
            }

            if (stack.getCount() == var2.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(par1EntityPlayer, stack);
        }

        return var2;
    }

}
