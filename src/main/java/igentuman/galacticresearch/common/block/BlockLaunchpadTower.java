package igentuman.galacticresearch.common.block;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.common.tile.TileLaunchpadTower;
import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import micdoodle8.mods.galacticraft.api.block.IPartialSealableBlock;
import micdoodle8.mods.galacticraft.core.GCBlocks;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.blocks.BlockMulti;
import micdoodle8.mods.galacticraft.core.blocks.BlockTileGC;
import micdoodle8.mods.galacticraft.core.blocks.ISortableBlock;
import micdoodle8.mods.galacticraft.core.items.IShiftDescription;
import micdoodle8.mods.galacticraft.core.util.EnumSortCategoryBlock;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class BlockLaunchpadTower extends BlockTileGC implements IShiftDescription, IPartialSealableBlock, ISortableBlock
{

    public BlockLaunchpadTower(String assetName)
    {
        super(Material.IRON);
        this.setHardness(2.0F);
        this.setSoundType(SoundType.METAL);
        this.setTranslationKey(assetName);
        this.setRegistryName(assetName);
    }

    @Override
    public CreativeTabs getCreativeTab()
    {
        return GalacticraftCore.galacticraftBlocksTab;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side)
    {
        for (int y = 1; y <= 2; y++)
        {
            for (int x = -1; x <= 1; x++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    BlockPos pos1 = pos.add((y == 2 ? x : 0), y, (y == 2 ? z : 0));
                    Block block = world.getBlockState(pos1).getBlock();

                    if (block.getMaterial(world.getBlockState(pos)) != Material.AIR && !block.isReplaceable(world, pos1))
                    {
                        return false;
                    }
                }
            }
        }

        EnumFacing facing = EnumFacing.byIndex(side.getIndex() ^ 1);
        return world.getBlockState(pos.add(facing.getXOffset(), facing.getYOffset(), facing.getZOffset())).getBlock() != GCBlocks.fakeBlock;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        int metadata = state.getBlock().getMetaFromState(state);

        int angle = MathHelper.floor(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        int change = 0;

        switch (angle)
        {
            case 0:
                change = 1;
                break;
            case 1:
                change = 2;
                break;
            case 2:
                change = 0;
                break;
            case 3:
                change = 3;
                break;
        }

        worldIn.setBlockState(pos, state.getBlock().getStateFromMeta(change), 3);

        BlockMulti.onPlacement(worldIn, pos, placer, this);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        final TileEntity tile = worldIn.getTileEntity(pos);

        if (tile instanceof TileLaunchpadTower)
        {
            ((TileLaunchpadTower) tile).onDestroy(tile);
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onUseWrench(World world, BlockPos pos, EntityPlayer entityPlayer, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return true;
    }

    @Override
    public boolean onSneakMachineActivated(World world, BlockPos pos, EntityPlayer entityPlayer, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        // entityPlayer.openGui(GalacticraftCore.instance, -1, world, x, y, z);
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState metadata)
    {
        return new TileLaunchpadTower();
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public String getShiftDescription(int meta)
    {
        return GCCoreUtil.translate("description.launchpad_tower");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, @Nullable World world, List<String> currentTooltip, ITooltipFlag flag) {
        super.addInformation(itemStack, world, currentTooltip, flag);
        String[] parts = I18n.format("description.launchpad_tower", 123).split("\\\\n");
        for(String line: parts) {
            currentTooltip.add(TextFormatting.AQUA + line);
        }
    }

    @Override
    public boolean showDescription(int meta)
    {
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isSealed(World world, BlockPos pos, EnumFacing direction)
    {
        return true;
    }

    @Override
    public EnumSortCategoryBlock getCategory(int meta)
    {
        return EnumSortCategoryBlock.GENERAL;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = worldIn.getTileEntity(pos);

        if(!(te instanceof TileLaunchpadTower)) {
            return true;
        }

        if(worldIn.isRemote) {
            return true;
        }

        playerIn.openGui(GalacticResearch.instance, 3, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
}
