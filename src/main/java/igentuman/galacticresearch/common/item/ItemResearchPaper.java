package igentuman.galacticresearch.common.item;

import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemResearchPaper extends Item {
    public ItemResearchPaper() {
        setRegistryName("research_paper");
        setTranslationKey("research_paper");
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if(world.isRemote) {
            return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
        }
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileMissionControlStation) {
            TileMissionControlStation station = (TileMissionControlStation) tile;
            ItemStack stack = player.getHeldItem(hand);
            if(!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
            }
            if(stack.getTagCompound().hasKey("mission")) {
                if(!station.completedMissions().contains(stack.getTagCompound().getString("mission"))) {
                    station.addResearchedMission(stack.getTagCompound().getString("mission"));
                    player.sendMessage(new TextComponentTranslation("message.saved_mission_to_mcs"));
                    return EnumActionResult.SUCCESS;
                }
                return EnumActionResult.FAIL;
            }
            if(!station.completedMissions().contains(station.currentMission)) {
                player.sendMessage(new TextComponentTranslation("message.research_paper_mission_not_completed"));
                return EnumActionResult.FAIL;
            }
            stack.getTagCompound().setString("mission", station.currentMission);
            player.sendMessage(new TextComponentTranslation("message.research_paper_mission", station.currentMission));
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }



    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if(stack.hasTagCompound() && stack.getTagCompound().hasKey("mission")) {
            tooltip.add(EnumColor.YELLOW + "\u00a7o" + new TextComponentTranslation("tooltip.research_paper_mission", stack.getTagCompound().getString("mission")).getFormattedText());
        } else {
            tooltip.add(new TextComponentTranslation("tooltip.research_paper_no_mission").getFormattedText());
        }
        tooltip.add(new TextComponentTranslation("tooltip.how_to_use").getFormattedText());
    }
}
