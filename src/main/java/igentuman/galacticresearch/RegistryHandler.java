package igentuman.galacticresearch;

import igentuman.galacticresearch.common.block.*;
import igentuman.galacticresearch.common.entity.EntityMiningRocket;
import igentuman.galacticresearch.common.entity.EntitySatelliteRocket;
import igentuman.galacticresearch.common.item.*;
import igentuman.galacticresearch.common.tile.*;
import micdoodle8.mods.galacticraft.api.recipe.INasaWorkbenchRecipe;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class RegistryHandler {

    @ObjectHolder("galacticresearch:telescope")
    public static Block TELESCOPE = new BlockTelescope();

    @ObjectHolder("galacticresearch:mission_control_station")
    public static Block MISSION_CONTROL_STATION = new BlockMissionControlStation();

    @ObjectHolder("galacticresearch:satellite_rocket")
    public static ItemSatelliteRocket SATELLITE_ROCKET = new ItemSatelliteRocket("satellite_rocket");

    @ObjectHolder("galacticresearch:mining_rocket")
    public static ItemMiningRocket MINING_ROCKET = new ItemMiningRocket("mining_rocket");

    @ObjectHolder("galacticresearch:probe")
    public static Item ITEM_PROBE = new Item().setRegistryName("probe").setTranslationKey("probe");

    public static void registerEntities() {
        ResourceLocation registryName = new ResourceLocation(GalacticResearch.MODID, "satellite_rocket");
        EntityRegistry.registerModEntity(registryName, EntitySatelliteRocket.class, "satellite_rocket", 14, GalacticResearch.instance, 150, 1, false);
        EntityRegistry.registerModEntity(registryName, EntityMiningRocket.class, "mining_rocket", 15, GalacticResearch.instance, 150, 1, false);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(TELESCOPE);
        event.getRegistry().register(MISSION_CONTROL_STATION);


        GameRegistry.registerTileEntity(
                TileMissionControlStation.class,
                MISSION_CONTROL_STATION.getRegistryName()
        );

        GameRegistry.registerTileEntity(
                TileTelescope.class,
                TELESCOPE.getRegistryName()
        );

    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(SATELLITE_ROCKET);
        event.getRegistry().register(MINING_ROCKET);
        event.getRegistry().register(ITEM_PROBE);
        event.getRegistry().register(new ItemBlock(MISSION_CONTROL_STATION).setRegistryName(MISSION_CONTROL_STATION.getRegistryName()));
        event.getRegistry().register(new ItemBlock(TELESCOPE).setRegistryName(TELESCOPE.getRegistryName()));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        registerItemModel(Item.getItemFromBlock(MISSION_CONTROL_STATION), 0, "inventory");
        registerItemModel(Item.getItemFromBlock(TELESCOPE), 0, "inventory");
        registerItemModel(ITEM_PROBE, 0, "inventory");
        registerItemModel(SATELLITE_ROCKET, 0, "inventory");
        registerItemModel(MINING_ROCKET, 0, "inventory");
        //ClientRegistry.bindTileEntitySpecialRenderer(TileDrill.class, new DrillTESR());
    }

    @SideOnly(Side.CLIENT)
    public void registerItemModel(@Nonnull Item item, int meta, String variant) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), variant));
    }
}