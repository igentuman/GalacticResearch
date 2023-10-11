package igentuman.galacticresearch.common.container;

import igentuman.galacticresearch.common.tile.TileRocketAssembler;
import micdoodle8.mods.galacticraft.api.item.IItemElectric;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.inventory.SlotSpecific;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnaceOutput;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ContainerRocketAssembler extends Container
{
    private TileRocketAssembler tileEntity;

    public ContainerRocketAssembler(InventoryPlayer par1InventoryPlayer, TileRocketAssembler tileEntity)
    {
        this.tileEntity = tileEntity;

        // Battery
        this.addSlotToContainer(new SlotSpecific(tileEntity, 0, 80, 98, ItemElectricBase.class));

        // result
        this.addSlotToContainer(new SlotFurnaceOutput(par1InventoryPlayer.player, tileEntity, 1, 145, 49));

        int index = 2;
        for(int var6 = 0; var6 < 4; var6++) {
            for(int var7 = 0; var7 < 7; var7++) {
                this.addSlotToContainer(new Slot(tileEntity, index++, 9 + var7 * 18, 21 + var6 * 18));
            }
        }

        index = 0;
        for(int var6 = 0; var6 < 3; var6++) {
            for(int var7 = 0; var7 < 9; var7++) {
                this.addSlotToContainer(new Slot(par1InventoryPlayer, index++, 8 + var7 * 18, 119 + var6 * 18));
            }
        }

        for(int var6 = 0; var6 < 9; var6++) {
            this.addSlotToContainer(new Slot(par1InventoryPlayer, index++, 8 + var6 * 18, 177));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer par1EntityPlayer)
    {
        return this.tileEntity.isUsableByPlayer(par1EntityPlayer);
    }

    //todo implement
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par1)
    {
        ItemStack var2 = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(par1);
        return var2;
    }

}