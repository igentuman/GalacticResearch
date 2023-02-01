package igentuman.galacticresearch.util;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import igentuman.galacticresearch.GalacticResearch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import org.apache.logging.log4j.LogManager;

import java.util.List;

public class ClientUtil {
    public static <T extends TileEntity> void registerTERenderer(Class<T> tileEntityClass, TileEntitySpecialRenderer<? super T> specialRenderer) {
        ClientRegistry.bindTileEntitySpecialRenderer(tileEntityClass, specialRenderer);
    }

    public static <T extends Entity> void registerEntityRenderer(Class<T> entityClass, IRenderFactory<? super T> renderFactory) {
        RenderingRegistry.registerEntityRenderingHandler(entityClass, renderFactory);
    }

    public static void replaceModelDefault(String modID, ModelBakeEvent event, String loc, List<String> visibleGroups, Class<? extends IBakedModel> clazz, String... variants) {
        replaceModelDefault(modID, event, loc, loc + ".obj", visibleGroups, clazz, TRSRTransformation.identity(), variants);
    }

    public static void replaceModelDefault(String modID, ModelBakeEvent event, String resLoc, String objLoc, List<String> visibleGroups, Class<? extends IBakedModel> clazz, String... variants) {
        replaceModelDefault(modID, event, resLoc, objLoc, visibleGroups, clazz, TRSRTransformation.identity(), variants);
    }

    public static void replaceModelDefault(String modID, ModelBakeEvent event, String resLoc, String objLoc, List<String> visibleGroups, Class<? extends IBakedModel> clazz, IModelState parentState, String... variants) {
        if (variants.length == 0) {
            variants = new String[] { "inventory" };
        }

        OBJModel model;

        try {
            model = (OBJModel) ModelLoaderRegistry.getModel(new ResourceLocation(modID, objLoc));
            model = (OBJModel) model.process(ImmutableMap.of("flip-v", "true"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Function<ResourceLocation, TextureAtlasSprite> spriteFunction = location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
        for (String variant : variants) {
            ModelResourceLocation modelResourceLocation = new ModelResourceLocation(modID + ":" + resLoc, variant);
            IBakedModel object = event.getModelRegistry().getObject(modelResourceLocation);
            if (object != null) {
                if (!variant.equals("inventory"))
                    parentState = TRSRTransformation.identity();

                IBakedModel newModel = model.bake(new OBJModel.OBJState(visibleGroups, false, parentState), DefaultVertexFormats.ITEM, spriteFunction);
                if (clazz != null) {
                    try {
                        newModel = clazz.getConstructor(IBakedModel.class).newInstance(newModel);
                    } catch (Exception e) {
                        LogManager.getLogger().fatal(GalacticResearch.MODID, "ItemModel constructor problem for " + modelResourceLocation);
                        e.printStackTrace();
                    }
                }
                event.getModelRegistry().putObject(modelResourceLocation, newModel);
            }
        }
    }


}
