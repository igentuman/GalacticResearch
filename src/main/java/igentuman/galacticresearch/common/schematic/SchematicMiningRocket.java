package igentuman.galacticresearch.common.schematic;

import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.RegistryHandler;
import igentuman.galacticresearch.common.schematic.mining_rocket.ContainerSchematicMiningRocket;
import igentuman.galacticresearch.common.schematic.mining_rocket.GuiSchematicMiningRocket;
import micdoodle8.mods.galacticraft.api.recipe.SchematicPage;
import micdoodle8.mods.galacticraft.planets.GuiIdsPlanets;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SchematicMiningRocket extends SchematicPage
{

    @Override
    public int getPageID()
    {
        return ModConfig.machines.mining_rocket_schematic_id;
    }

    @Override
    public int getGuiID()
    {
        return GuiIdsPlanets.NASA_WORKBENCH_CARGO_ROCKET + ModConfig.machines.mining_rocket_schematic_id;
    }

    @Override
    public ItemStack getRequiredItem()
    {
        return new ItemStack(RegistryHandler.MINING_ROCKET_SCHEMATIC);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getResultScreen(EntityPlayer player, BlockPos pos)
    {
        return new GuiSchematicMiningRocket(player.inventory, pos);
    }

    @Override
    public Container getResultContainer(EntityPlayer player, BlockPos pos)
    {
        return new ContainerSchematicMiningRocket(player.inventory, pos);
    }
}
