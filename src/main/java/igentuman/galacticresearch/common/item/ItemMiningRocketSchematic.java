package igentuman.galacticresearch.common.item;

import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.RegistryHandler;
import micdoodle8.mods.galacticraft.api.item.GCRarity;
import micdoodle8.mods.galacticraft.api.recipe.ISchematicItem;
import micdoodle8.mods.galacticraft.api.recipe.SchematicRegistry;
import micdoodle8.mods.galacticraft.core.Constants;
import micdoodle8.mods.galacticraft.core.GCItems;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.entities.EntityHangingSchematic;
import micdoodle8.mods.galacticraft.core.items.ISortableItem;
import micdoodle8.mods.galacticraft.core.items.ItemSchematic;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.util.EnumSortCategoryItem;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemHangingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

import static igentuman.galacticresearch.GalacticResearch.MODID;

public class ItemMiningRocketSchematic extends ItemHangingEntity implements ISchematicItem, ISortableItem, GCRarity
{

    public ItemMiningRocketSchematic(String assetName)
    {
        super(EntityHangingSchematic.class);
        this.setRegistryName(assetName);
        this.setMaxDamage(0);
        this.setHasSubtypes(false);
        this.setMaxStackSize(1);
        this.setTranslationKey(assetName);
    }

    @Override
    public CreativeTabs getCreativeTab()
    {
        return GalacticraftCore.galacticraftItemsTab;
    }

    @Override
    public int getMetadata(int par1)
    {
        return par1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack par1ItemStack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(GCCoreUtil.translate("schematic.mining_rocket.dungeon_level"));
    }

    @Override
    public EnumSortCategoryItem getCategory(int meta)
    {
        return EnumSortCategoryItem.SCHEMATIC;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = playerIn.getHeldItem(hand);
        BlockPos blockpos = pos.offset(facing);

        if (facing != EnumFacing.DOWN && facing != EnumFacing.UP && playerIn.canPlayerEdit(blockpos, facing, stack))
        {
            EntityHangingSchematic entityhanging = this.createEntity(worldIn, blockpos, facing, this.getIndex(stack.getItemDamage()));

            if (entityhanging != null && entityhanging.onValidSurface())
            {
                if (!worldIn.isRemote)
                {
                    entityhanging.playPlaceSound();
                    worldIn.spawnEntity(entityhanging);
                    entityhanging.sendToClient(worldIn, blockpos);
                }

                stack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        } else
        {
            return EnumActionResult.FAIL;
        }
    }

    private EntityHangingSchematic createEntity(World worldIn, BlockPos pos, EnumFacing clickedSide, int index)
    {
        return new EntityHangingSchematic(worldIn, pos, clickedSide, index);
    }

    protected int getIndex(int damage)
    {
        return ModConfig.machines.mining_rocket_schematic_id;
    }

    public static void registerSchematicItems()
    {
        SchematicRegistry.registerSchematicItem(new ItemStack(RegistryHandler.MINING_ROCKET_SCHEMATIC, 1, 0));
    }

    /**
     * Make sure the order of these will match the index values
     */
    @SideOnly(value = Side.CLIENT)
    public static void registerTextures()
    {
        SchematicRegistry.registerTexture(new ResourceLocation(MODID, "textures/items/schematic_mining_rocket.png"));
    }
}
