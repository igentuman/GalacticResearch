package igentuman.galacticresearch;

import igentuman.galacticresearch.command.CommandHandler;
import igentuman.galacticresearch.common.data.SpaceMineProvider;
import igentuman.galacticresearch.handler.GREventHandler;
import igentuman.galacticresearch.handler.GRPlayerHandler;
import igentuman.galacticresearch.network.GRChannelHandler;
import igentuman.galacticresearch.network.GuiProxy;
import igentuman.galacticresearch.sky.SkyModel;
import igentuman.galacticresearch.util.GRHooks;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = igentuman.galacticresearch.GalacticResearch.MODID,
        name = igentuman.galacticresearch.GalacticResearch.NAME,
        version = igentuman.galacticresearch.GalacticResearch.VERSION,
        dependencies = "required-after:galacticraftplanets;"
)
@Mod.EventBusSubscriber
public class GalacticResearch
{
    public static final String MODID = "galacticresearch";
    public static final String NAME = "Galactic Research";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MODID)
    public static GalacticResearch instance;
    public static MinecraftServer server;
    public static SkyModel skyModel = SkyModel.get();
    public static SpaceMineProvider spaceMineProvider;
    public static GRChannelHandler packetPipeline;
    public static GRHooks hooks = new GRHooks();

    @SidedProxy(serverSide ="igentuman.galacticresearch.common.CommonProxy", clientSide ="igentuman.galacticresearch.client.ClientProxy")
    public static ISidedProxy proxy;

    public Logger logger;
    public static GRPlayerHandler pHandler;
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new RegistryHandler());
        pHandler = new GRPlayerHandler();
        MinecraftForge.EVENT_BUS.register(new GREventHandler());
        MinecraftForge.EVENT_BUS.register(pHandler);
        proxy.preInit(event);
        logger = event.getModLog();
        MinecraftForge.EVENT_BUS.register(this);
        hooks.hookPreInit();
    }

    @EventHandler
    public void onServerAboutToStart(FMLServerAboutToStartEvent ev) {
        server = ev.getServer();
        spaceMineProvider = SpaceMineProvider.get();
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent ev) {
        server = ev.getServer();
        spaceMineProvider = SpaceMineProvider.get();
        skyModel.initSeed();
    }

    @EventHandler
    public void onServerStopped(FMLServerStoppedEvent ev) {
        server = null;
    }

    @EventHandler
    public void init(FMLInitializationEvent event)  {
        hooks.hookInit();
        proxy.init(event);
        logger.info("Starting Initialization.");
        ConfigManager.sync(MODID, Config.Type.INSTANCE);
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiProxy());
        packetPipeline = GRChannelHandler.init();
    }


    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        hooks.hookPostInit();
        proxy.postInit(event);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals(MODID)) {
            ConfigManager.sync(MODID, Config.Type.INSTANCE);
        }
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent serverStartEvent) {
        CommandHandler.registerCommands(serverStartEvent);
    }
}
