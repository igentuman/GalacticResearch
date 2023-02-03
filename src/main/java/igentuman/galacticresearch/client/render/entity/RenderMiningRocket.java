package igentuman.galacticresearch.client.render.entity;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.client.model.ItemModelMiningRocket;
import igentuman.galacticresearch.common.entity.EntityMiningRocket;
import micdoodle8.mods.galacticraft.core.util.ClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMiningRocket extends Render<EntityMiningRocket> {
    private ItemModelMiningRocket rocketModel;

    public RenderMiningRocket(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.5F;
    }

    public RenderMiningRocket(RenderManager manager, ModelBase spaceshipModel, String textureDomain, String texture)
    {
        this(manager, new ResourceLocation(textureDomain, "textures/model/" + texture + ".png"));
      //  this.rocketModel = spaceshipModel;
    }

    public RenderMiningRocket(RenderManager manager, ResourceLocation texture)
    {
        super(manager);
      //  this.rocketModel = texture;
        this.shadowSize = 0.9F;
    }

    private void updateModel() {
        if (this.rocketModel == null) {
            ModelResourceLocation modelResourceLocation = new ModelResourceLocation(GalacticResearch.MODID +":mining_rocket", "inventory");
            this.rocketModel = (ItemModelMiningRocket)FMLClientHandler.instance().getClient().getRenderItem().getItemModelMesher().getModelManager().getModel(modelResourceLocation);
        }

    }

    protected ResourceLocation getEntityTexture(EntityMiningRocket entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

    public void doRender(EntityMiningRocket entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        GlStateManager.disableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y + entity.getRenderOffsetY(), (float)z);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
        GlStateManager.rotate(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-pitch, 0.0F, 0.0F, 1.0F);
        this.bindEntityTexture(entity);
        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(7425);
        } else {
            GlStateManager.shadeModel(7424);
        }

        this.updateModel();
        ClientUtil.drawBakedModel(this.rocketModel);
        GlStateManager.popMatrix();
        RenderHelper.enableStandardItemLighting();
    }

    public boolean shouldRender(EntityMiningRocket rocket, ICamera camera, double camX, double camY, double camZ) {
        AxisAlignedBB axisalignedbb = rocket.getEntityBoundingBox();
        return rocket.isInRangeToRender3d(camX, camY, camZ) && camera.isBoundingBoxInFrustum(axisalignedbb);
    }
}
