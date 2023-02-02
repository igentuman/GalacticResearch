package igentuman.galacticresearch.client;

import com.google.common.collect.ImmutableList;
import igentuman.galacticresearch.RegistryHandler;
import igentuman.galacticresearch.client.model.ItemModelSatelliteRocket;
import igentuman.galacticresearch.client.render.entity.RenderSatelliteRocket;
import igentuman.galacticresearch.client.sound.SoundHandler;
import igentuman.galacticresearch.common.CommonProxy;
import igentuman.galacticresearch.common.entity.EntitySatelliteRocket;
import igentuman.galacticresearch.common.tile.TileTelescope;
import igentuman.galacticresearch.network.TileProcessUpdatePacket;
import micdoodle8.mods.galacticraft.core.util.ClientUtil;
import micdoodle8.mods.galacticraft.core.wrappers.ModelTransformWrapper;
import micdoodle8.mods.galacticraft.planets.GalacticraftPlanets;
import micdoodle8.mods.galacticraft.planets.mars.items.MarsItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
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
        MinecraftForge.EVENT_BUS.register(SoundHandler.class);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        RenderingRegistry.registerEntityRenderingHandler(EntitySatelliteRocket.class, (RenderManager manager) -> new RenderSatelliteRocket(manager));
        MinecraftForge.EVENT_BUS.register(this);
        OBJLoader.INSTANCE.addDomain(MODID);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onModelBakeEvent(ModelBakeEvent event) {
        replaceModelDefault(event, "satellite_rocket", "satellite_rocket.obj", ImmutableList.of("Rocket"), ItemModelSatelliteRocket.class, TRSRTransformation.identity());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void loadTextures(TextureStitchEvent.Pre event) {
        registerTexture(event, "satellite_rocket");
    }

    @Override
    public void handleProcessUpdatePacket(TileProcessUpdatePacket message, MessageContext ctx) {
        TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);

        if(te instanceof TileTelescope) {
         //   ((TileTelescope) te).onTileUpdatePacket(message);
        }
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
     //   ModelResourceLocation modelResourceLocation = new ModelResourceLocation(MODID + ":satellite_rocket", "inventory");
    //    ModelLoader.setCustomModelResourceLocation(RegistryHandler.SATELLITE_ROCKET, 0, modelResourceLocation);
    //    ClientUtilities.registerModel(Constants.TEXTURE_PREFIX, PlanetProgression_Items.SATELLITE_ROCKET, 1, "satellite_rocket");

    }
}