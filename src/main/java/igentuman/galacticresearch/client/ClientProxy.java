package igentuman.galacticresearch.client;

import com.google.common.collect.ImmutableList;
import igentuman.galacticresearch.client.capability.SpaceClientCapabilityHandler;
import igentuman.galacticresearch.client.model.ItemModelMiningRocket;
import igentuman.galacticresearch.client.model.ItemModelSatelliteRocket;
import igentuman.galacticresearch.client.render.entity.RenderMiningRocket;
import igentuman.galacticresearch.client.render.entity.RenderSatelliteRocket;
import igentuman.galacticresearch.client.render.tile.TileLaunchpadTowerRender;
import igentuman.galacticresearch.client.screen.GameScreenMission;
import igentuman.galacticresearch.client.screen.GameScreenTelescope;
import igentuman.galacticresearch.common.CommonProxy;
import igentuman.galacticresearch.common.entity.EntityMiningRocket;
import igentuman.galacticresearch.common.entity.EntitySatelliteRocket;
import igentuman.galacticresearch.common.tile.TileLaunchpadTower;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.core.wrappers.ModelTransformWrapper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static igentuman.galacticresearch.GalacticResearch.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Side.CLIENT)

public class ClientProxy extends CommonProxy {

    private void replaceModelDefault(ModelBakeEvent event, String resLoc, String objLoc, List<String> visibleGroups, Class<? extends ModelTransformWrapper> clazz, IModelState parentState, String... variants)
    {
        micdoodle8.mods.galacticraft.core.util.ClientUtil.replaceModel(MODID, event, resLoc, objLoc, visibleGroups, clazz, parentState, variants);
    }

    private void registerTexture(TextureStitchEvent.Pre event, String texture)
    {
        event.getMap().registerSprite(new ResourceLocation(MODID + ":blocks/" + texture));
    }

    @Override
    public void init(FMLInitializationEvent e)
    {
        super.init(e);
        ClientRegistry.bindTileEntitySpecialRenderer(TileLaunchpadTower.class, new TileLaunchpadTowerRender());
        GalacticraftRegistry.registerScreen(new GameScreenMission());
        GalacticraftRegistry.registerScreen(new GameScreenTelescope());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        SpaceClientCapabilityHandler.register();
        super.postInit(event);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        RenderingRegistry.registerEntityRenderingHandler(EntitySatelliteRocket.class, (RenderManager manager) -> new RenderSatelliteRocket(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityMiningRocket.class, (RenderManager manager) -> new RenderMiningRocket(manager));
        MinecraftForge.EVENT_BUS.register(this);
        OBJLoader.INSTANCE.addDomain(MODID);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onModelBakeEvent(ModelBakeEvent event) {
        replaceModelDefault(event, "satellite_rocket", "satellite_rocket.obj", ImmutableList.of("Rocket"), ItemModelSatelliteRocket.class, TRSRTransformation.identity());
        replaceModelDefault(event, "mining_rocket", "mining_rocket.obj", ImmutableList.of("Rocket"), ItemModelMiningRocket.class, TRSRTransformation.identity());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void loadTextures(TextureStitchEvent.Pre event) {
        registerTexture(event, "mining_rocket");
        registerTexture(event, "satellite_rocket");
       // registerTexture(event, "tower");
    }

}