package igentuman.galacticresearch.client.render.tile;

import igentuman.galacticresearch.common.tile.TileLaunchpadTower;
import micdoodle8.mods.galacticraft.core.util.ClientUtil;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

import static igentuman.galacticresearch.GalacticResearch.MODID;

public class TileLaunchpadTowerRender extends TileEntitySpecialRenderer<TileLaunchpadTower>
{

    private static final ResourceLocation textureMain = new ResourceLocation(MODID, "textures/model/tower_main.png");
    private static final ResourceLocation textureSupport = new ResourceLocation(MODID, "textures/model/tower_support.png");
    private static final ResourceLocation textureCrane = new ResourceLocation(MODID, "textures/model/tower_crane.png");
    private static IBakedModel modelSupport;
    private static IBakedModel modelFork;
    private static IBakedModel modelCrane;
    private TextureManager renderEngine;
    private int ticks = 0;

    public TileLaunchpadTowerRender() {
        this.renderEngine = FMLClientHandler.instance().getClient().renderEngine;
    }

    private void updateModels() {
        if (modelSupport == null) {
            try {
                modelFork = ClientUtil.modelFromOBJ(new ResourceLocation(MODID, "tower_support.obj"));
                modelSupport = ClientUtil.modelFromOBJ(new ResourceLocation(MODID, "tower_main.obj"));
                modelCrane = ClientUtil.modelFromOBJ(new ResourceLocation(MODID, "tower_crane.obj"));
            } catch (Exception var2) {
                var2.printStackTrace();
            }
        }
    }

    private void rotateByFacing(EnumFacing side)
    {
        switch (side) {
            case WEST:
                GL11.glRotatef(270, 0F, 1.0F, 0.0F);
            case SOUTH:
                GL11.glRotatef(180, 0F, 1.0F, 0.0F);
            case EAST:
                GL11.glRotatef(90, 0F, 1.0F, 0.0F);
        }
    }


    TileLaunchpadTower tile;

    public void render(TileLaunchpadTower tile, double par2, double par4, double par6, float partialTickTime, int par9, float alpha) {
        this.updateModels();
        this.tile = tile;

        //EnumFacing side = tile.getFront();
        EntityPlayer player = FMLClientHandler.instance().getClient().player;
        GL11.glPushMatrix();
        GL11.glEnable(32826);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)par2, (float)par4, (float)par6);
        GL11.glTranslatef(0.5F, 0.0F, 0.5F);
       // rotateByFacing(side);
        GL11.glScalef(1.2F, 1.4F, 1.2F);
        this.renderEngine.bindTexture(textureMain);
        ClientUtil.drawBakedModel(modelSupport);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef((float)par2, (float)par4, (float)par6);
        GL11.glTranslatef(0.5F, 1.0F, 0.5F);
      //  rotateByFacing(side);
        GL11.glRotatef(90, -1F, 0.0F, 0.0F);

        GL11.glScalef(1.1F, 1.2F, 1.1F);
        GL11.glTranslatef(-0.0F, -1.5F, 0.0F);
        this.renderEngine.bindTexture(textureSupport);
        ClientUtil.drawBakedModel(modelFork);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef((float)par2, (float)par4, (float)par6);
       // rotateByFacing(side);

        GL11.glTranslatef(0.5F, 2.6F, tile.cPos);
        GL11.glScalef(1.9F, 1.4F, 1.4F);
        this.renderEngine.bindTexture(textureCrane);
        ClientUtil.drawBakedModel(modelCrane);
        GL11.glPopMatrix();

        GL11.glDisable(32826);
        GL11.glPopMatrix();
    }
}
