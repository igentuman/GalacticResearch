package igentuman.galacticresearch.common.block;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.common.tile.TileLaunchpadTower;
import igentuman.galacticresearch.common.tile.TileRocketAssembler;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.blocks.BlockAdvancedTile;
import micdoodle8.mods.galacticraft.core.blocks.ISortableBlock;
import micdoodle8.mods.galacticraft.core.items.IShiftDescription;
import micdoodle8.mods.galacticraft.core.tile.IMachineSides;
import micdoodle8.mods.galacticraft.core.tile.IMachineSidesProperties;
import micdoodle8.mods.galacticraft.core.tile.TileEntityFuelLoader;
import micdoodle8.mods.galacticraft.core.util.EnumSortCategoryBlock;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.util.WorldUtil;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRocketAssembler extends BlockAdvancedTile implements IShiftDescription, ISortableBlock {
    public static final PropertyDirection FACING;
    public static IMachineSidesProperties MACHINESIDES_RENDERTYPE;
    public static final PropertyEnum SIDES;

    public BlockRocketAssembler() {
        super(Material.ROCK);
        this.setHardness(1.0F);
        this.setSoundType(SoundType.METAL);
        this.setTranslationKey("rocket_assembler");
        this.setRegistryName("rocket_assembler");
    }

    public CreativeTabs getCreativeTab() {
        return GalacticraftCore.galacticraftBlocksTab;
    }

    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileRocketAssembler();
    }

    public boolean onMachineActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityPlayer, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);

        if(!(te instanceof TileRocketAssembler)) {
            return true;
        }

        if(world.isRemote) {
            return true;
        }

        entityPlayer.openGui(GalacticResearch.instance, 4, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        int angle = MathHelper.floor((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5) & 3;
        worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.byHorizontalIndex(angle).getOpposite()), 3);
        WorldUtil.markAdjacentPadForUpdate(worldIn, pos);
    }

    public void onPlayerDestroy(World worldIn, BlockPos pos, IBlockState state) {
        super.onPlayerDestroy(worldIn, pos, state);
        WorldUtil.markAdjacentPadForUpdate(worldIn, pos);
    }

    public String getShiftDescription(int meta) {
        return GCCoreUtil.translate(this.getTranslationKey() + ".description");
    }

    public boolean showDescription(int meta) {
        return true;
    }

    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byHorizontalIndex(meta);
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    public int getMetaFromState(IBlockState state) {
        return ((EnumFacing)state.getValue(FACING)).getHorizontalIndex();
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING, SIDES});
    }

    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity tile = worldIn.getTileEntity(pos);
        return IMachineSides.addPropertyForTile(state, tile, MACHINESIDES_RENDERTYPE, SIDES);
    }

    public EnumSortCategoryBlock getCategory(int meta) {
        return EnumSortCategoryBlock.MACHINE;
    }

    public boolean onSneakUseWrench(World world, BlockPos pos, EntityPlayer entityPlayer, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IMachineSides) {
            ((IMachineSides)tile).nextSideConfiguration(tile);
            return true;
        } else {
            return false;
        }
    }

    static {
        FACING = PropertyDirection.create("facing", Plane.HORIZONTAL);
        MACHINESIDES_RENDERTYPE = IMachineSidesProperties.TWOFACES_HORIZ;
        SIDES = MACHINESIDES_RENDERTYPE.asProperty;
    }
}
