package igentuman.galacticresearch.client.screen;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.common.entity.EntityMiningRocket;
import igentuman.galacticresearch.common.entity.EntitySatelliteRocket;
import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import micdoodle8.mods.galacticraft.api.client.IGameScreen;
import micdoodle8.mods.galacticraft.api.client.IScreenManager;
import micdoodle8.mods.galacticraft.api.entity.ITelemetry;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.core.client.render.entities.RenderPlayerGC;
import micdoodle8.mods.galacticraft.core.client.screen.DrawGameScreen;
import micdoodle8.mods.galacticraft.core.tile.TileEntityTelemetry;
import micdoodle8.mods.galacticraft.core.util.ColorUtil;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Method;
import java.nio.DoubleBuffer;

public class GameScreenMission implements IGameScreen
{

    private float frameA;
    private float frameBx;
    private float frameBy;
    private int yPos;
    private DoubleBuffer planes;
    private Method renderModelMethod;
    private Method renderLayersMethod;
    private String status;

    public GameScreenMission()
    {
        if (GCCoreUtil.getEffectiveSide().isClient())
        {
            planes = BufferUtils.createDoubleBuffer(4 * Double.SIZE);
            try
            {
                Class clazz = RenderLivingBase.class;
                int count = 0;
                for (Method m : clazz.getDeclaredMethods())
                {
                    String s = m.getName();
                    if (s.equals(GCCoreUtil.isDeobfuscated() ? "renderModel" : "func_77036_a"))
                    {
                        m.setAccessible(true);
                        this.renderModelMethod = m;
                        if (count == 1)
                            break;
                        count = 1;
                    } else if (s.equals(GCCoreUtil.isDeobfuscated() ? "renderLayers" : "func_177093_a"))
                    {
                        m.setAccessible(true);
                        this.renderLayersMethod = m;
                        if (count == 1)
                            break;
                        count = 1;
                    }
                }
            } catch (Exception e)
            {
            }
        }
    }

    @Override
    public void setFrameSize(float frameSize)
    {
        this.frameA = frameSize;
    }

    public TileMissionControlStation getMissionControlCenter(TileEntityTelemetry telemeter)
    {
        for(int i = 0; i<6; i++) {
           TileEntity te =  telemeter.getWorld().getTileEntity(telemeter.getPos().offset(EnumFacing.byIndex(i)));
           if(te instanceof TileMissionControlStation) {
               return (TileMissionControlStation) te;
           }
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(int type, float ticks, float sizeX, float sizeY, IScreenManager scr)
    {
        DrawGameScreen screen = (DrawGameScreen) scr;

        frameBx = sizeX - frameA;
        frameBy = sizeY - frameA;
        drawBlackBackground(0.0F);
        planeEquation(frameA, frameA, 0, frameA, frameBy, 0, frameA, frameBy, 1);
        GL11.glClipPlane(GL11.GL_CLIP_PLANE0, planes);
        GL11.glEnable(GL11.GL_CLIP_PLANE0);
        planeEquation(frameBx, frameBy, 0, frameBx, frameA, 0, frameBx, frameA, 1);
        GL11.glClipPlane(GL11.GL_CLIP_PLANE1, planes);
        GL11.glEnable(GL11.GL_CLIP_PLANE1);
        planeEquation(frameA, frameBy, 0, frameBx, frameBy, 0, frameBx, frameBy, 1);
        GL11.glClipPlane(GL11.GL_CLIP_PLANE2, planes);
        GL11.glEnable(GL11.GL_CLIP_PLANE2);
        planeEquation(frameBx, frameA, 0, frameA, frameA, 0, frameA, frameA, 1);
        GL11.glClipPlane(GL11.GL_CLIP_PLANE3, planes);
        GL11.glEnable(GL11.GL_CLIP_PLANE3);
        yPos = 0;

        TileEntityTelemetry telemeter = TileEntityTelemetry.getNearest(screen.driver);


        String strName = "";
        String[] str =
        {GCCoreUtil.translate("gui.display.nolink"), "", "", "", ""};
        Render renderEntity = null;
        Entity entity = null;
        float Xmargin = 0;

        if (telemeter != null) {
            TileMissionControlStation mcs = getMissionControlCenter(telemeter);
            if (mcs != null) {
                if (mcs.getRocket() == null) {
                    if (mcs.currentMission.contains("ASTEROID-")) {
                        entity = new EntityMiningRocket(telemeter.getWorld());
                    } else if (!mcs.currentMission.isEmpty()) {
                        entity = new EntitySatelliteRocket(telemeter.getWorld());
                    }
                } else {
                    entity = (EntityAutoRocket) mcs.getRocket();
                }
                if(entity != null) {
                    renderEntity = (Render) FMLClientHandler.instance().getClient().getRenderManager().getEntityRenderObject(entity);
                }
                strName = mcs.currentMission;
                if(!strName.isEmpty()) {
                    status = mcs.getMissionStatusKey(mcs.currentMission);
                    if(!strName.contains("ASTEROID-")) {
                        String tmp = I18n.format("planet."+strName);
                        if(tmp.equals(strName)) {
                            tmp = I18n.format("moon."+strName);
                        }
                        strName = tmp;
                    }
                    str[0] = GCCoreUtil.translate(status);
                    int state = mcs.getMissionInfo(mcs.currentMission);
                    int percent = 0;
                    if(state > 0) {
                        percent = mcs.getMissonPercent(mcs.currentMission);
                        if(strName.contains("ASTEROID-")) {
                            str[2] = I18n.format("screen.mined_blocks", GalacticResearch.spaceMineProvider.getOreCnt(strName)/100*percent);
                        }
                    }
                    if(percent > 0 && percent < 100) {
                        str[1] = percent+GCCoreUtil.translate("screen.progress_done");
                    }
                }
            } else {
                str[2] = "No MCS found";
            }
        }
        int textWidthPixels = 155;
        int textHeightPixels = 60;
        if (str[3].isEmpty())
        {
            textHeightPixels -= 10;
        }
        if (str[4].isEmpty())
        {
            textHeightPixels -= 10;
        }

        // First pass - approximate border size
        float borders = frameA * 2 + 0.05F * Math.min(sizeX, sizeY);
        float scaleXTest = (sizeX - borders) / textWidthPixels;
        float scaleYTest = (sizeY - borders) / textHeightPixels;
        float scale = sizeX;
        if (scaleYTest < scaleXTest)
        {
            scale = sizeY;
        }
        // Second pass - the border size may be more accurate now
        borders = frameA * 2 + 0.05F * scale;
        scaleXTest = (sizeX - borders) / textWidthPixels;
        scaleYTest = (sizeY - borders) / textHeightPixels;
        scale = sizeX;
        float scaleText = scaleXTest;
        if (scaleYTest < scaleXTest)
        {
            scale = sizeY;
            scaleText = scaleYTest;
        }

        // Centre the text in the display
        float border = frameA + 0.025F * scale;
        if (entity != null && renderEntity != null)
        {
            Xmargin = (sizeX - borders) / 2;
        }
        float Xoffset = (sizeX - borders - textWidthPixels * scaleText) / 2 + Xmargin;
        float Yoffset = (sizeY - borders - textHeightPixels * scaleText) / 2 + scaleText;
        int whiteColour = ColorUtil.to32BitColor(255, 240, 216, 255);
        String title = "";
        if(strName.contains("ASTEROID-")) {
            title = I18n.format("screen.title.mining");
        } else {
            title = I18n.format("screen.title.research");
        }
        int tlength = Minecraft.getMinecraft().fontRenderer.getStringWidth(title);
        GL11.glTranslatef(border+ (tlength*scaleText)/4, border , 0.0F);
        GL11.glScalef(scaleText, scaleText, 1.0F);
        drawTitle(title, whiteColour);
        GL11.glScalef(1/scaleText, 1/scaleText, 1.0F);
        GL11.glTranslatef(-border- (tlength*scaleText)/4, -border , 0.0F);
        GL11.glTranslatef(border + Xoffset, border + Yoffset, 0.0F);
        GL11.glScalef(scaleText, scaleText, 1.0F);

        // Actually draw the text
        drawText(strName, whiteColour);
        drawText(str[0], whiteColour);
        drawText(str[1], whiteColour);
        drawText(str[2], whiteColour);
        drawText(str[3], whiteColour);
        drawText(str[4], whiteColour);

        // If there is an entity to render, draw it on the left of the text
        if (renderEntity != null && entity != null)
        {
            GL11.glTranslatef(-Xmargin / 2 / scaleText, textHeightPixels / 2 + (-Yoffset + (sizeY - borders) / 2) / scaleText, -0.0005F);
            float scalefactor = 38F / (float) Math.pow(Math.max(entity.height, entity.width), 0.65);
            GL11.glScalef(scalefactor, scalefactor, 0.0015F);
            GL11.glRotatef(180F, 0, 0, 1);
            GL11.glRotatef(180F, 0, 1, 0);
            RenderPlayerGC.flagThermalOverride = true;
            renderEntity.doRender(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
            RenderPlayerGC.flagThermalOverride = false;
        }

        GL11.glDisable(GL11.GL_CLIP_PLANE3);
        GL11.glDisable(GL11.GL_CLIP_PLANE2);
        GL11.glDisable(GL11.GL_CLIP_PLANE1);
        GL11.glDisable(GL11.GL_CLIP_PLANE0);
    }

    private void drawTitle(String str, int colour)
    {
        Minecraft.getMinecraft().fontRenderer.drawString(str, 0, 0, colour, false);
        yPos += 10;
    }

    private void drawText(String str, int colour)
    {
        Minecraft.getMinecraft().fontRenderer.drawString(str, 0, yPos, colour, false);
        yPos += 10;
    }

    private void drawBlackBackground(float greyLevel)
    {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        final Tessellator tess = Tessellator.getInstance();
        BufferBuilder worldRenderer = tess.getBuffer();
        GL11.glColor4f(greyLevel, greyLevel, greyLevel, 1.0F);
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(frameA, frameBy, 0.005F).endVertex();
        worldRenderer.pos(frameBx, frameBy, 0.005F).endVertex();
        worldRenderer.pos(frameBx, frameA, 0.005F).endVertex();
        worldRenderer.pos(frameA, frameA, 0.005F).endVertex();
        tess.draw();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void planeEquation(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3)
    {
        double[] result = new double[4];
        result[0] = y1 * (z2 - z3) + y2 * (z3 - z1) + y3 * (z1 - z2);
        result[1] = z1 * (x2 - x3) + z2 * (x3 - x1) + z3 * (x1 - x2);
        result[2] = x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2);
        result[3] = -(x1 * (y2 * z3 - y3 * z2) + x2 * (y3 * z1 - y1 * z3) + x3 * (y1 * z2 - y2 * z1));
        planes.put(result, 0, 4);
        planes.position(0);
    }
}
