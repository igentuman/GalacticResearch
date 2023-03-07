package igentuman.galacticresearch.reflection;

import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import micdoodle8.mods.galacticraft.core.items.ItemBasic;
import micdoodle8.mods.galacticraft.core.tile.TileEntityDish;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemBasicReflector {

    public static ActionResult<ItemStack> onItemRightClick(ItemBasic inst, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);
        if (itemStackIn.getItemDamage() == 19)
        {
            if (playerIn instanceof EntityPlayerMP)
            {
                GCPlayerStats stats = GCPlayerStats.get(playerIn);
                ItemStack gear = stats.getExtendedInventory().getStackInSlot(5);

                if (gear.isEmpty() && itemStackIn.getTagCompound() == null)
                {
                    stats.getExtendedInventory().setInventorySlotContents(5, itemStackIn.copy());
                    itemStackIn = ItemStack.EMPTY;
                }
            }
            RayTraceResult r = playerIn.rayTrace(4,0);
            if(r.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {

                TileEntity te = worldIn.getTileEntity(r.getBlockPos());

                if (!worldIn.isRemote && te instanceof TileEntityDish) {
                    ItemStack itemStack = playerIn.getHeldItem(hand);

                    if (itemStack.getTagCompound() == null) {
                        itemStack.setTagCompound(new NBTTagCompound());
                    }

                    itemStack.getTagCompound().setIntArray("teledishPos", new int[]{r.getBlockPos().getX(), r.getBlockPos().getY(), r.getBlockPos().getZ()});
                    playerIn.sendMessage(new TextComponentString(GCCoreUtil.translate("message.teledish_assigned")));

                }
            }
        }

        return new ActionResult<>(EnumActionResult.FAIL, itemStackIn);
    }

    public static EnumActionResult onItemUse(ItemBasic inst, EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = worldIn.getTileEntity(pos);

        if(!worldIn.isRemote && te instanceof TileEntityDish) {
            ItemStack itemStack = player.getHeldItem(hand);
            if(itemStack.getItemDamage() == 19) {
                if (itemStack.getTagCompound() == null) {
                    itemStack.setTagCompound(new NBTTagCompound());
                }

                itemStack.getTagCompound().setIntArray("teledishPos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
                player.sendMessage(new TextComponentString(GCCoreUtil.translate("message.teledish_assigned")));
            }
        }

        return EnumActionResult.PASS;
    }

}
