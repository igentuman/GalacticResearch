package igentuman.galacticresearch;

import igentuman.galacticresearch.common.block.*;
import igentuman.galacticresearch.common.entity.EntityMiningRocket;
import igentuman.galacticresearch.common.entity.EntitySatelliteRocket;
import igentuman.galacticresearch.common.item.*;
import igentuman.galacticresearch.common.tile.*;
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

@Mod.EventBusSubscriber
public class RegistryHandler {

    @ObjectHolder("galacticresearch:rocket_assembler")
    public static Block ROCKET_ASSEMBLER = new BlockRocketAssembler();
    @ObjectHolder("galacticresearch:launchpad_tower")
    public static Block LAUNCHPAD_TOWER = new BlockLaunchpadTower("launchpad_tower");

    @ObjectHolder("galacticresearch:telescope")
    public static Block TELESCOPE = new BlockTelescope();

    @ObjectHolder("galacticresearch:mission_control_station")
    public static Block MISSION_CONTROL_STATION = new BlockMissionControlStation();

    @ObjectHolder("galacticresearch:satellite_rocket")
    public static Item SATELLITE_ROCKET = new ItemSatelliteRocket("satellite_rocket");

    @ObjectHolder("galacticresearch:mining_rocket")
    public static Item MINING_ROCKET = new ItemMiningRocket("mining_rocket");

    @ObjectHolder("galacticresearch:probe")
    public static Item ITEM_PROBE = new Item().setRegistryName("probe").setTranslationKey("probe");

    @ObjectHolder("galacticresearch:mining_rocket_schematic")
    public static Item MINING_ROCKET_SCHEMATIC = new ItemMiningRocketSchematic("mining_rocket_schematic");

    public static void registerEntities() {
        EntityRegistry.registerModEntity(
                new ResourceLocation(GalacticResearch.MODID, "satellite_rocket"),
                EntitySatelliteRocket.class,
                "satellite_rocket",
                714, GalacticResearch.instance,
                150,
                1,
                false
        );
        EntityRegistry.registerModEntity(
                new ResourceLocation(GalacticResearch.MODID, "mining_rocket"),
                EntityMiningRocket.class,
                "mining_rocket",
                715,
                GalacticResearch.instance,
                150,
                1,
                false
        );
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(ROCKET_ASSEMBLER);
        event.getRegistry().register(TELESCOPE);
        event.getRegistry().register(LAUNCHPAD_TOWER);
        event.getRegistry().register(MISSION_CONTROL_STATION);

        GameRegistry.registerTileEntity(
                TileRocketAssembler.class,
                ROCKET_ASSEMBLER.getRegistryName()
        );


        GameRegistry.registerTileEntity(
                TileMissionControlStation.class,
                MISSION_CONTROL_STATION.getRegistryName()
        );

        GameRegistry.registerTileEntity(
                TileTelescope.class,
                TELESCOPE.getRegistryName()
        );

        GameRegistry.registerTileEntity(
                TileLaunchpadTower.class,
                LAUNCHPAD_TOWER.getRegistryName()
        );

    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(MINING_ROCKET_SCHEMATIC);
        event.getRegistry().register(new ItemBlock(ROCKET_ASSEMBLER).setRegistryName(ROCKET_ASSEMBLER.getRegistryName()));
        event.getRegistry().register(new ItemBlock(MISSION_CONTROL_STATION).setRegistryName(MISSION_CONTROL_STATION.getRegistryName()));
        event.getRegistry().register(new ItemBlock(TELESCOPE).setRegistryName(TELESCOPE.getRegistryName()));
        event.getRegistry().register(new ItemBlock(LAUNCHPAD_TOWER).setRegistryName(LAUNCHPAD_TOWER.getRegistryName()));
        event.getRegistry().register(SATELLITE_ROCKET);
        event.getRegistry().register(MINING_ROCKET);
        event.getRegistry().register(ITEM_PROBE);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        registerItemModel(Item.getItemFromBlock(ROCKET_ASSEMBLER), 0, "inventory");
        registerItemModel(Item.getItemFromBlock(MISSION_CONTROL_STATION), 0, "inventory");
        registerItemModel(Item.getItemFromBlock(TELESCOPE), 0, "inventory");
        registerItemModel(Item.getItemFromBlock(LAUNCHPAD_TOWER), 0, "inventory");
        registerItemModel(ITEM_PROBE, 0, "inventory");
        registerItemModel(MINING_ROCKET_SCHEMATIC, 0, "inventory");
        registerItemModel(SATELLITE_ROCKET, 0, "inventory");
        registerItemModel(MINING_ROCKET, 0, "inventory");
    }

    @SideOnly(Side.CLIENT)
    public void registerItemModel(@Nonnull Item item, int meta, String variant) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), variant));
    }
}