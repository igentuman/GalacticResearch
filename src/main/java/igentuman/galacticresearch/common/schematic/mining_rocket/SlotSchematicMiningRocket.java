package igentuman.galacticresearch.common.schematic.mining_rocket;

import igentuman.galacticresearch.common.CommonProxy;
import micdoodle8.mods.galacticraft.api.recipe.INasaWorkbenchRecipe;
import micdoodle8.mods.galacticraft.core.network.PacketSimple;
import micdoodle8.mods.galacticraft.core.network.PacketSimple.EnumSimplePacket;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class SlotSchematicMiningRocket extends Slot
{

    private final int index;
    private final BlockPos pos;
    private final EntityPlayer player;

    public SlotSchematicMiningRocket(IInventory par2IInventory, int par3, int par4, int par5, BlockPos pos, EntityPlayer player)
    {
        super(par2IInventory, par3, par4, par5);
        this.index = par3;
        this.pos = pos;
        this.player = player;
    }

    @Override
    public void onSlotChanged()
    {
        if (this.player instanceof EntityPlayerMP)
        {
            int dimID = GCCoreUtil.getDimensionID(this.player.world);
            GCCoreUtil.sendToAllAround(new PacketSimple(EnumSimplePacket.C_SPAWN_SPARK_PARTICLES, dimID, new Object[]
            {this.pos}), this.player.world, dimID, this.pos, 20);
        }
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack)
    {
        if (par1ItemStack == null)
            return false;

        List<INasaWorkbenchRecipe> recipes = CommonProxy.getMiningRocketRecipes();
        for (INasaWorkbenchRecipe recipe : recipes)
        {
            if (ItemStack.areItemsEqual(par1ItemStack, recipe.getRecipeInput().get(this.index)))
                return true;
        }
        return false;
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }
}
