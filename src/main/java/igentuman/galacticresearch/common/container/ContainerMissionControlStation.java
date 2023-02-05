package igentuman.galacticresearch.common.container;

import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import micdoodle8.mods.galacticraft.api.item.IItemElectric;
import micdoodle8.mods.galacticraft.core.energy.EnergyUtil;
import micdoodle8.mods.galacticraft.core.inventory.SlotSpecific;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerMissionControlStation extends Container {
    private final TileMissionControlStation te;

    public ContainerMissionControlStation(TileMissionControlStation te) {
        this.te = te;
    }

    public ContainerMissionControlStation(InventoryPlayer par1InventoryPlayer, TileMissionControlStation tile) {
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

    //todo implement
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par1)
    {
        ItemStack var2 = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(par1);
        return var2;
    }

}
