
package igentuman.galacticresearch.common.block;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import igentuman.galacticresearch.common.tile.TileTelescope;
import micdoodle8.mods.galacticraft.core.GCItems;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockMissionControlStation extends BlockHorizontal {

    public BlockMissionControlStation() {
        super(Material.IRON);
        this.setHardness(3.5f);
        this.setResistance(17.5f);
        this.setTranslationKey("mission_control_station");
        this.setRegistryName(GalacticResearch.MODID, "mission_control_station");
        this.setCreativeTab(GalacticraftCore.galacticraftBlocksTab);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.3D, 1.0D);
    }

    @NotNull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @NotNull
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state) {
        return new TileMissionControlStation();
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        this.onBlockHarvested(world, pos, state, player);
        if(player.isCreative()) {
            world.setBlockToAir(pos);
            return true;
        }
        return world.setBlockState(pos, Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = worldIn.getTileEntity(pos);

        if(!(te instanceof TileMissionControlStation)) {
            return true;
        }

        if(worldIn.isRemote) {
            return true;
        }

        ItemStack held = playerIn.inventory.getCurrentItem();
        if (!held.isEmpty() && held.getItem() == GCItems.basicItem && held.getItemDamage() == 19) {
            NBTTagCompound fmData = held.getTagCompound();
            if (fmData != null && fmData.hasKey("teledishPos")) {
                ((TileMissionControlStation)te).bindTeleDish(fmData.getIntArray("teledishPos"));
                playerIn.sendMessage(new TextComponentTranslation("message.teledish_set"));
                return true;
            }
        }

        playerIn.openGui(GalacticResearch.instance, 2, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, @Nullable World world, List<String> currentTooltip, ITooltipFlag flag) {
        super.addInformation(itemStack, world, currentTooltip, flag);
        String[] parts = I18n.format("description.mission_control_station", 123).split("\\\\n");
        for(String line: parts) {
            currentTooltip.add(TextFormatting.AQUA + line);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile != null)
        {
            if(tile instanceof TileMissionControlStation)
            {
                ItemStack item = new ItemStack(Item.getItemFromBlock(this));
                NBTTagCompound compound = new NBTTagCompound();
                if(!((TileMissionControlStation) tile).missionsData.isEmpty())
                {
                    compound.setString("missionsData", ((TileMissionControlStation) tile).missionsData);
                }
                if(!((TileMissionControlStation) tile).stations.isEmpty())
                {
                    compound.setString("stations", ((TileMissionControlStation) tile).stations);
                }
                item.setTagCompound(compound);
                this.spawnAsEntity(worldIn, pos, item);
                worldIn.removeTileEntity(pos);
            }
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ItemStack.EMPTY.getItem();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if(!worldIn.isRemote) {
            if(stack != null && tile != null)
            {
                if(stack.hasTagCompound() && tile instanceof TileMissionControlStation)
                {
                    if(stack.getTagCompound().hasKey("missionsData"))
                    {
                        String missionsData = stack.getTagCompound().getString("missionsData");
                        ((TileMissionControlStation) tile).missionsData = missionsData;
                        ((TileMissionControlStation) tile).unserializeMissionData();
                    }

                    if(stack.getTagCompound().hasKey("stations"))
                    {
                        String stations = stack.getTagCompound().getString("stations");
                        ((TileMissionControlStation) tile).stations = stations;
                    }
                }
            }
        }
    }
}
