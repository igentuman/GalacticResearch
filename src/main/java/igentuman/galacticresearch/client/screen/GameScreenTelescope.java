package igentuman.galacticresearch.client.screen;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.common.tile.TileTelescope;
import igentuman.galacticresearch.sky.body.Asteroid;
import igentuman.galacticresearch.sky.body.Researchable;
import igentuman.galacticresearch.sky.body.Star;
import micdoodle8.mods.galacticraft.api.client.IGameScreen;
import micdoodle8.mods.galacticraft.api.client.IScreenManager;
import micdoodle8.mods.galacticraft.api.galaxies.*;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.screen.DrawGameScreen;
import micdoodle8.mods.galacticraft.core.tile.TileEntityTelemetry;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.DoubleBuffer;
import java.util.List;

import static igentuman.galacticresearch.GalacticResearch.MODID;

public class GameScreenTelescope implements IGameScreen
{

    private TextureManager renderEngine;

    private float frameA;
    private float frameBx;
    private float frameBy;
    private float centreX;
    private float centreY;
    private float scale;

    private TileTelescope telescope;
    private DoubleBuffer planes;

    public GameScreenTelescope()
    {
        if (GCCoreUtil.getEffectiveSide().isClient())
        {
            renderEngine = FMLClientHandler.instance().getClient().renderEngine;
            planes = BufferUtils.createDoubleBuffer(4 * Double.SIZE);
        }
    }

    public TileTelescope getTelescope(TileEntityTelemetry telemeter)
    {
        for(int i = 0; i<6; i++) {
            TileEntity te =  telemeter.getWorld().getTileEntity(telemeter.getPos().offset(EnumFacing.byIndex(i)));
            if(te instanceof TileTelescope) {
                return (TileTelescope) te;
            }
        }
        return null;
    }

    @Override
    public void setFrameSize(float frameSize)
    {
        this.frameA = frameSize;
    }

    @Override
    public void render(int type, float ticks, float scaleX, float scaleY, IScreenManager scr)
    {
        centreX = scaleX / 2;
        centreY = scaleY / 2;
        frameBx = scaleX - frameA;
        frameBy = scaleY - frameA;
        this.scale = Math.max(scaleX, scaleY) - 0.2F;
        DrawGameScreen screen = (DrawGameScreen) scr;

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

        TileEntityTelemetry telemeter = TileEntityTelemetry.getNearest(screen.driver);
        if (telemeter != null) {
            telescope = getTelescope(telemeter);
            if(telescope != null) {
                if(telescope.getEnergyStoredGC() > 100) {
                    WorldProvider wp = scr.getWorldProvider();
                    CelestialBody body = null;
                    if (wp instanceof IGalacticraftWorldProvider) {
                        body = ((IGalacticraftWorldProvider) wp).getCelestialBody();
                    }
                    if (body == null) {
                        body = GalacticraftCore.planetOverworld;
                    }
                    drawStars();
                    drawCelestialBodies(body);
                }
            }
        }
        GL11.glDisable(GL11.GL_CLIP_PLANE3);
        GL11.glDisable(GL11.GL_CLIP_PLANE2);
        GL11.glDisable(GL11.GL_CLIP_PLANE1);
        GL11.glDisable(GL11.GL_CLIP_PLANE0);
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

    private void drawStars()
    {
        Star[] stars = GalacticResearch.skyModel.getStars();
        this.renderEngine.bindTexture(stars[0].getTexture());

        for(Star star: stars) {
            if(!star.isVisible()) continue;
            float xScaled = (star.getX()-telescope.xAngle)/TileTelescope.viewportSize*frameBx;
            float yScaled = (star.getY()-telescope.yAngle)/TileTelescope.viewportSize*frameBy;
            this.drawStar(star, xScaled, yScaled, star.getSize(), star.getColor());
        }
    }

    private void drawStar(Star star, float xPos, float yPos, float relSize, float color)
    {
        float size = relSize / 200 * scale;
        GL11.glPushMatrix();
        GL11.glTranslatef(xPos+frameA , yPos+frameA, 0F);

        float alpha = .7F;

        GL11.glColor4f(1, 1-color/10, 1-color/20, alpha);
        this.drawTexturedRectCBody(0, 0, size, size);

        GL11.glPopMatrix();
    }

    private void drawCelestialBodies(CelestialBody body)
    {
        List<Researchable> researchables = GalacticResearch.skyModel.getCurrentSystemBodies(body.getDimensionID());
        for(Researchable res: researchables) {
            if(telescope.isBodyVisible(res, TileTelescope.viewportSize, TileTelescope.viewportSize)) {
                float xScaled = (res.getX()-telescope.xAngle)/TileTelescope.viewportSize*frameBx;
                float yScaled = (res.getY()-telescope.yAngle)/TileTelescope.viewportSize*frameBy;
                this.drawCelestialBody(res, xScaled, yScaled, res.getSize());
            }
        }
    }

    private void drawTexturedRectCBody(float x, float y, float width, float height)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(x, y + height, 0F).tex(0D, 1.0).endVertex();
        worldRenderer.pos(x + width, y + height, 0F).tex(1.0, 1.0).endVertex();
        worldRenderer.pos(x + width, y, 0F).tex(1.0, 0D).endVertex();
        worldRenderer.pos(x, y, 0F).tex(0D, 0D).endVertex();
        tessellator.draw();
    }

    private void drawCelestialBody(Researchable planet, float xPos, float yPos, float relSize)
    {
        float size = relSize / 70 * scale;
        GL11.glPushMatrix();
        GL11.glTranslatef(xPos+frameA , yPos+frameA, 0F);

        float alpha = 1.0F;

        GL11.glColor4f(1, 1, 1, alpha);
        if(planet instanceof Asteroid) {
            this.renderEngine.bindTexture(new ResourceLocation(MODID,"textures/gui/planets/asteroid.png"));

        } else {
            this.renderEngine.bindTexture(planet.getTexture());
        }
        this.drawTexturedRectCBody(0, 0, size, size);

        GL11.glPopMatrix();
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
