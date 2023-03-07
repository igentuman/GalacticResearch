package igentuman.galacticresearch.reflection.screen;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.client.capability.PlayerClientSpaceData;
import igentuman.galacticresearch.client.capability.SpaceClientCapabilityHandler;
import micdoodle8.mods.galacticraft.api.client.IScreenManager;
import micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent;
import micdoodle8.mods.galacticraft.api.galaxies.*;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.render.RenderPlanet;
import micdoodle8.mods.galacticraft.core.client.screen.GameScreenCelestial;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.Iterator;

public class GameScreenCelestialReflector {

    private static float centreX;
    private static float scale;
    private static float frameBx;
    private static float centreY;
    private static float frameA;
    private static float frameBy;
    private static DoubleBuffer planes = BufferUtils.createDoubleBuffer(256);
    private static final float cos = MathHelper.cos(0.06981317F);
    private static final float sin = MathHelper.sin(0.06981317F);

    private static boolean isUnlocked(String name)
    {
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        EntityPlayerSP player = minecraft.player;
        EntityPlayerSP playerBaseClient = PlayerUtil.getPlayerBaseClientFromPlayer(player, false);
        PlayerClientSpaceData stats = null;

        if (player != null) {
            stats = playerBaseClient.getCapability(SpaceClientCapabilityHandler.PLAYER_SPACE_CLIENT_DATA, null);
        }
        return stats.getUnlockedMissions().contains(name.toLowerCase()) ||
                Arrays.asList(ModConfig.researchSystem.default_researched_bodies).contains(name.toLowerCase());
    }

    public static void drawCelestialBodiesQ(CelestialBody body, float ticks)
    {
        Star star = null;
        SolarSystem solarSystem = null;
        if (body instanceof Planet) {
            solarSystem = ((Planet)body).getParentSolarSystem();
        } else if (body instanceof Moon) {
            solarSystem = ((Moon)body).getParentPlanet().getParentSolarSystem();
        } else if (body instanceof Satellite) {
            solarSystem = ((Satellite)body).getParentPlanet().getParentSolarSystem();
        }

        if (solarSystem == null) {
            solarSystem = GalacticraftCore.solarSystemSol;
        }

        star = solarSystem.getMainStar();
        if (star != null && star.getBodyIcon() != null) {
            drawCelestialBodyQ(star, 0.0F, 0.0F, ticks, 6.0F);
        }
        String mainSolarSystem;
        try {
            mainSolarSystem = solarSystem.getTranslationKey();
        } catch (NoSuchMethodError ignore) {
            mainSolarSystem = solarSystem.getUnlocalizedName();
        }
        Iterator var6 = GalaxyRegistry.getRegisteredPlanets().values().iterator();
        String pkey;
        while(var6.hasNext()) {
            Planet planet = (Planet)var6.next();
            if(!isUnlocked(planet.getName())) {
                continue;
            }
            try {
                pkey = planet.getParentSolarSystem().getTranslationKey();
            } catch (NoSuchMethodError ignore) {
                pkey = planet.getParentSolarSystem().getUnlocalizedName();
            }

            if (planet.getParentSolarSystem() != null && planet.getBodyIcon() != null && pkey.equalsIgnoreCase(mainSolarSystem)) {
                Vector3f pos = getCelestialBodyPositionQ(planet, ticks);
                drawCircleQ(planet);
                drawCelestialBodyQ(planet, pos.x, pos.y, ticks, planet.getRelativeDistanceFromCenter().unScaledDistance < 1.5F ? 2.0F : 2.8F);
            }
        }
    }

    public static void drawCelestialBodyQ(CelestialBody planet, float xPos, float yPos, float ticks, float relSize) {
        if (!(xPos + centreX > frameBx) && !(xPos + centreX < frameA)) {
            if (!(yPos + centreY > frameBy) && !(yPos + centreY < frameA)) {
                GL11.glPushMatrix();
                GL11.glTranslatef(xPos + centreX, yPos + centreY, 0.0F);
                float alpha = 1.0F;
                CelestialBodyRenderEvent.Pre preEvent = new CelestialBodyRenderEvent.Pre(planet, planet.getBodyIcon(), 12);
                MinecraftForge.EVENT_BUS.post(preEvent);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
                if (preEvent.celestialBodyTexture != null) {
                    Minecraft.getMinecraft().renderEngine.bindTexture(preEvent.celestialBodyTexture);
                }

                if (!preEvent.isCanceled()) {
                    float size = relSize / 70.0F * scale;
                    drawTexturedRectCBodyQ(-size / 2.0F, -size / 2.0F, size, size);
                }

                CelestialBodyRenderEvent.Post postEvent = new CelestialBodyRenderEvent.Post(planet);
                MinecraftForge.EVENT_BUS.post(postEvent);
                GL11.glPopMatrix();
            }
        }
    }

    private static void drawTexturedRectCBodyQ(float x, float y, float width, float height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos((double)x, (double)(y + height), 0.0D).tex(0.0D, 1.0D).endVertex();
        worldRenderer.pos((double)(x + width), (double)(y + height), 0.0D).tex(1.0D, 1.0D).endVertex();
        worldRenderer.pos((double)(x + width), (double)y, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldRenderer.pos((double)x, (double)y, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
    }

    private static void drawBlackBackgroundQ(float greyLevel) {
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder worldRenderer = tess.getBuffer();
        GL11.glColor4f(greyLevel, greyLevel, greyLevel, 1.0F);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos((double)frameA, (double)frameBy, 0.004999999888241291D).endVertex();
        worldRenderer.pos((double)frameBx, (double)frameBy, 0.004999999888241291D).endVertex();
        worldRenderer.pos((double)frameBx, (double)frameA, 0.004999999888241291D).endVertex();
        worldRenderer.pos((double)frameA, (double)frameA, 0.004999999888241291D).endVertex();
        tess.draw();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(3553);
    }
    
    public static void render(GameScreenCelestial inst, int type, float ticks, float scaleX, float scaleY, IScreenManager scr) {
        centreX = scaleX / 2.0F;
        centreY = scaleY / 2.0F;
        frameBx = scaleX - frameA;
        frameBy = scaleY - frameA;
        scale = Math.max(scaleX, scaleY) - 0.2F;
        drawBlackBackgroundQ(0.0F);
        planeEquationQ(frameA, frameA, 0.0F, frameA, frameBy, 0.0F, frameA, frameBy, 1.0F);
        GL11.glClipPlane(12288, planes);
        GL11.glEnable(12288);
        planeEquationQ(frameBx, frameBy, 0.0F, frameBx, frameA, 0.0F, frameBx, frameA, 1.0F);
        GL11.glClipPlane(12289, planes);
        GL11.glEnable(12289);
        planeEquationQ(frameA, frameBy, 0.0F, frameBx, frameBy, 0.0F, frameBx, frameBy, 1.0F);
        GL11.glClipPlane(12290, planes);
        GL11.glEnable(12290);
        planeEquationQ(frameBx, frameA, 0.0F, frameA, frameA, 0.0F, frameA, frameA, 1.0F);
        GL11.glClipPlane(12291, planes);
        GL11.glEnable(12291);
        switch(type) {
            case 2:
                WorldProvider wp = scr.getWorldProvider();
                CelestialBody body = null;
                if (wp instanceof IGalacticraftWorldProvider) {
                    body = ((IGalacticraftWorldProvider)wp).getCelestialBody();
                }

                if (body == null) {
                    body = GalacticraftCore.planetOverworld;
                }

                drawCelestialBodiesQ((CelestialBody)body, ticks);
                break;
            case 3:
                drawCelestialBodiesZQ(GalacticraftCore.planetOverworld, ticks);
                break;
            case 4:
                drawPlanetsTestQ(ticks);
        }

        GL11.glDisable(12291);
        GL11.glDisable(12290);
        GL11.glDisable(12289);
        GL11.glDisable(12288);
    }

    private static void drawCelestialBodiesZQ(CelestialBody planet, float ticks) {
        drawCelestialBodyQ(planet, 0.0F, 0.0F, ticks, 11.0F);
        Iterator var3 = GalaxyRegistry.getRegisteredMoons().values().iterator();

        Vector3f pos;
        while(var3.hasNext()) {
            Moon moon = (Moon)var3.next();
            if (moon.getParentPlanet() == planet && moon.getBodyIcon() != null) {
                pos = getCelestialBodyPositionQ(moon, ticks);
                drawCircleQ(moon);
                drawCelestialBodyQ(moon, pos.x, pos.y, ticks, 4.0F);
            }
        }

        var3 = GalaxyRegistry.getRegisteredSatellites().values().iterator();

        while(var3.hasNext()) {
            Satellite satellite = (Satellite)var3.next();
            if (satellite.getParentPlanet() == planet) {
                pos = getCelestialBodyPositionQ(satellite, ticks);
                drawCircleQ(satellite);
                drawCelestialBodyQ(satellite, pos.x, pos.y, ticks, 3.0F);
            }
        }

    }

    private static Vector3f getCelestialBodyPositionQ(CelestialBody cBody, float ticks) {
        float timeScale = cBody instanceof Planet ? 200.0F : 2.0F;
        float distanceFromCenter = getScaleQ(cBody) * scale;
        return new Vector3f((float)Math.sin((double)(ticks / (timeScale * cBody.getRelativeOrbitTime()) + cBody.getPhaseShift())) * distanceFromCenter, (float)Math.cos((double)(ticks / (timeScale * cBody.getRelativeOrbitTime()) + cBody.getPhaseShift())) * distanceFromCenter, 0.0F);
    }

    private static void planeEquationQ(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        double[] result = new double[]{(double)(y1 * (z2 - z3) + y2 * (z3 - z1) + y3 * (z1 - z2)), (double)(z1 * (x2 - x3) + z2 * (x3 - x1) + z3 * (x1 - x2)), (double)(x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)), (double)(-(x1 * (y2 * z3 - y3 * z2) + x2 * (y3 * z1 - y1 * z3) + x3 * (y1 * z2 - y2 * z1)))};
        planes.put(result, 0, 4);
        planes.position(0);
    }

    private static void drawPlanetsTestQ(float ticks) {
        GL11.glPushMatrix();
        GL11.glTranslatef(centreX, centreY, 0.0F);
        int id = (int)(ticks / 600.0F) % 5;
        RenderPlanet.renderID(id, scale, ticks);
        GL11.glPopMatrix();
    }

    private static void drawTexturedRectUVQ(float x, float y, float width, float height, float ticks) {
        for(int ysect = 0; ysect < 6; ++ysect) {
            float angle = 7.5F + 15.0F * (float)ysect;
            drawTexturedRectUVSixthQ(x, y, width, height, ticks / (900.0F - 80.0F * MathHelper.cos(angle)) % 1.0F, (float)ysect / 6.0F);
        }

    }

    private static void drawTexturedRectUVSixthQ(float x, float y, float width, float height, float prog, float y0) {
        y0 /= 2.0F;
        prog = 1.0F - prog;
        float y1 = y0 + 0.083333336F;
        float y2 = 1.0F - y1;
        float y3 = 1.0F - y0;
        float yaa = y + height * y0;
        float yab = y + height * y1;
        float yba = y + height * y2;
        float ybb = y + height * y3;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();
        if (prog <= 0.75F) {
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos((double)x, (double)yab, 0.0D).tex((double)prog, (double)y1).endVertex();
            worldRenderer.pos((double)(x + width), (double)yab, 0.0D).tex((double)(prog + 0.25F), (double)y1).endVertex();
            worldRenderer.pos((double)(x + width), (double)yaa, 0.0D).tex((double)(prog + 0.25F), (double)y0).endVertex();
            worldRenderer.pos((double)x, (double)yaa, 0.0D).tex((double)prog, (double)y0).endVertex();
            tessellator.draw();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos((double)x, (double)ybb, 0.0D).tex((double)prog, (double)y3).endVertex();
            worldRenderer.pos((double)(x + width), (double)ybb, 0.0D).tex((double)(prog + 0.25F), (double)y3).endVertex();
            worldRenderer.pos((double)(x + width), (double)yba, 0.0D).tex((double)(prog + 0.25F), (double)y2).endVertex();
            worldRenderer.pos((double)x, (double)yba, 0.0D).tex((double)prog, (double)y2).endVertex();
            tessellator.draw();
        } else {
            double xp = (double)(x + width * (1.0F - prog) / 0.25F);
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos((double)x, (double)yab, 0.0D).tex((double)prog, (double)y1).endVertex();
            worldRenderer.pos(xp, (double)yab, 0.0D).tex(1.0D, (double)y1).endVertex();
            worldRenderer.pos(xp, (double)yaa, 0.0D).tex(1.0D, (double)y0).endVertex();
            worldRenderer.pos((double)x, (double)yaa, 0.0D).tex((double)prog, (double)y0).endVertex();
            tessellator.draw();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos((double)x, (double)ybb, 0.0D).tex((double)prog, (double)y3).endVertex();
            worldRenderer.pos(xp, (double)ybb, 0.0D).tex(1.0D, (double)y3).endVertex();
            worldRenderer.pos(xp, (double)yba, 0.0D).tex(1.0D, (double)y2).endVertex();
            worldRenderer.pos((double)x, (double)yba, 0.0D).tex((double)prog, (double)y2).endVertex();
            tessellator.draw();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos(xp, (double)yab, 0.0D).tex(0.0D, (double)y1).endVertex();
            worldRenderer.pos((double)(x + width), (double)yab, 0.0D).tex((double)(prog - 0.75F), (double)y1).endVertex();
            worldRenderer.pos((double)(x + width), (double)yaa, 0.0D).tex((double)(prog - 0.75F), (double)y0).endVertex();
            worldRenderer.pos(xp, (double)yaa, 0.0D).tex(0.0D, (double)y0).endVertex();
            tessellator.draw();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos(xp, (double)ybb, 0.0D).tex(0.0D, (double)y3).endVertex();
            worldRenderer.pos((double)(x + width), (double)ybb, 0.0D).tex((double)(prog - 0.75F), (double)y3).endVertex();
            worldRenderer.pos((double)(x + width), (double)yba, 0.0D).tex((double)(prog - 0.75F), (double)y2).endVertex();
            worldRenderer.pos(xp, (double)yba, 0.0D).tex(0.0D, (double)y2).endVertex();
            tessellator.draw();
        }

    }
    
    private static float getScaleQ(CelestialBody celestialBody) {
        float distance = celestialBody.getRelativeDistanceFromCenter().unScaledDistance;
        if (distance >= 1.375F) {
            if (distance >= 1.5F) {
                distance *= 1.15F;
            } else {
                distance += 0.075F;
            }
        }

        return 0.007142857F * distance * (celestialBody instanceof Planet ? 25.0F : 3.5F);
    }

    public static void drawCircleQ(CelestialBody cBody) {
        GL11.glPushMatrix();
        GL11.glTranslatef(centreX, centreY, 0.002F);
        GL11.glDisable(3553);
        float sd = 0.002514F * scale;
        float x = getScaleQ(cBody);
        float y = 0.0F;
        float grey = 0.1F + 0.65F * Math.max(0.0F, 0.5F - x);
        x = x * scale / sd;
        GL11.glColor4f(grey, grey, grey, 1.0F);
        GL11.glLineWidth(0.002F);
        GL11.glScalef(sd, sd, sd);
        micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent.CelestialRingRenderEvent.Pre preEvent = new micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent.CelestialRingRenderEvent.Pre(cBody, new Vector3f(0.0F, 0.0F, 0.0F));
        MinecraftForge.EVENT_BUS.post(preEvent);
        if (!preEvent.isCanceled()) {
            GL11.glBegin(2);

            for(int i = 0; i < 90; ++i) {
                GL11.glVertex2f(x, y);
                float temp = x;
                x = cos * x - sin * y;
                y = sin * temp + cos * y;
            }

            GL11.glEnd();
        }

        micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent.CelestialRingRenderEvent.Post postEvent = new micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent.CelestialRingRenderEvent.Post(cBody);
        MinecraftForge.EVENT_BUS.post(postEvent);
        GL11.glEnable(3553);
        GL11.glPopMatrix();
    }

}
