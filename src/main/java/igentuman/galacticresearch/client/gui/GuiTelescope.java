package igentuman.galacticresearch.client.gui;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.common.container.ContainerTelescope;
import igentuman.galacticresearch.common.tile.TileTelescope;
import igentuman.galacticresearch.network.GRPacketSimple;
import igentuman.galacticresearch.sky.SkyModel;
import igentuman.galacticresearch.sky.body.ISkyBody;
import igentuman.galacticresearch.sky.body.Researchable;
import igentuman.galacticresearch.sky.body.Star;
import igentuman.galacticresearch.util.WorldUtil;
import micdoodle8.mods.galacticraft.core.client.gui.container.GuiContainerGC;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementInfoRegion;
import micdoodle8.mods.galacticraft.core.energy.EnergyDisplayHelper;
import micdoodle8.mods.galacticraft.core.util.ColorUtil;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static igentuman.galacticresearch.common.tile.TileTelescope.viewportSize;


public class GuiTelescope extends GuiContainerGC {
    private static final ResourceLocation guiTexture = new ResourceLocation(GalacticResearch.MODID, "textures/gui/container/telescope_hd.png");
    private static final ResourceLocation overlay = new ResourceLocation(GalacticResearch.MODID, "textures/gui/container/telescope_top_overlay.png");

    private final TileTelescope tile;
    float tmpX = 0;
    float tmpY = 0;
    private GuiButtonImage btnUp;
    private GuiButtonImage btnDown;
    private GuiButtonImage btnLeft;
    private GuiButtonImage btnRight;
    private GuiButton btnMultiplier;
    private GuiButtonImage btnHelp;
    private Map<String,GuiButtonImage> planets = new HashMap<>();
    public float sh = 0;
    public int[] curStar = new int[] {1, 1};
    private GuiElementInfoRegion electricInfoRegion;
    private GuiElementInfoRegion helpRegion;
    private GuiElementInfoRegion researchedRegion;
    private GuiButtonImage btnResearched;
    public static long  lastTickWTime = 0;
    public static float ticks = 0;
    public static float lastXangle = 0;
    public static float lastYangle = 0;

    public GuiTelescope(InventoryPlayer par1InventoryPlayer, TileTelescope tile) {
        super(new ContainerTelescope(par1InventoryPlayer, tile));
        this.electricInfoRegion = new GuiElementInfoRegion(guiLeft + 30, guiTop + 167, 64, 9, new ArrayList(), this.width, this.height, this);
        this.helpRegion = new GuiElementInfoRegion(guiLeft + 154, guiTop + 164, 12, 12, new ArrayList(), this.width, this.height, this);
        this.researchedRegion = new GuiElementInfoRegion(guiLeft + 140, guiTop + 164, 12, 12, new ArrayList(), this.width, this.height, this);
        this.tile = tile;
        this.ySize = 201;
    }

    private void tickButtons()
    {
        if(selectedButton != null && selectedButton.isMouseOver()) {
            if(selectedButton.id == 4) return;
            actionPerformed(selectedButton);
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(keyCode == Keyboard.KEY_UP) {
            actionPerformed(btnUp);
            return;
        }
        if(keyCode == Keyboard.KEY_DOWN) {
            actionPerformed(btnDown);
            return;
        }
        if(keyCode == Keyboard.KEY_LEFT) {
            actionPerformed(btnLeft);
            return;
        }
        if(keyCode == Keyboard.KEY_RIGHT) {
            actionPerformed(btnRight);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    protected void actionPerformed(GuiButton par1GuiButton) {
        switch(par1GuiButton.id) {
            case 0:
                GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.TELESCOPE_UP_BUTTON, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{this.tile.getPos(), 0}));
                break;
            case 1:
                GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.TELESCOPE_DOWN_BUTTON, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{this.tile.getPos(), 0}));
                break;
            case 2:
                GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.TELESCOPE_LEFT_BUTTON, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{this.tile.getPos(), 0}));
                break;
            case 3:
                GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.TELESCOPE_RIGHT_BUTTON, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{this.tile.getPos(), 0}));
                break;
            case 4:
                GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.TELESCOPE_MULTIPLIER_BUTTON, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{this.tile.getPos(), 0}));
            default:
        }
    }

    public boolean isVisible(ISkyBody body)
    {
        return  body.getX()+body.getSize()/4 > xAngle() &&
                body.getY()+body.getSize()/4 > yAngle() &&
                body.getX() < (xAngle() + viewportSize) &&
                body.getY() < (yAngle() + viewportSize) &&
                body.isVisible();
    }



    public float xAngle()
    {
        if(lastXangle == 0) {
            lastXangle = tmpX;
        }
        tmpX = tile.xAngle;
        return lastXangle + (tile.xAngle - lastXangle) * ticks;
    }

    public float yAngle()
    {
        if(lastYangle == 0) {
            lastYangle = tmpY;
        }
        tmpY = tile.yAngle;
        return lastYangle + (tile.yAngle - lastYangle) * ticks;
    }

    public float viewportX(float x)
    {
        return (guiLeft + 6) + x - xAngle();
    }

    public float viewportY(float y)
    {
        return (guiTop + 24) + y - yAngle();
    }

    public void renderStars()
    {
        Star[] stars = SkyModel.get().getStars();
        if(stars == null) return;
        GlStateManager.scale(0.5, 0.5, 0.5);

        for(Star star: stars) {
            if(star.equals(stars[0]) && (curStar[0] != star.getX() || curStar[1] != star.getY())) {
                curStar[0] = star.getX();
                curStar[1] = star.getY();
                sh = 0.0F;
            }
            if(!isVisible(star)) continue;
            float x = viewportX(star.getX());
            float y = viewportY(star.getY());
            int offset = star.getColor()*3;
            this.drawTexturedModalRect(x*2+sh, y*2+sh, 0, 201 + offset, star.getSize(), star.getSize());
        }
        sh+=0.033;

        GlStateManager.scale(2, 2, 2);
    }


    public void renderPlanets()
    {
        List<Researchable> bodies = SkyModel.get().getObjectsToResearch(tile.dimension);
        if(bodies == null) return;

        for(ISkyBody researchable: bodies) {
            if(researchable == null) continue;
            Researchable res = (Researchable) researchable;
            if(!isVisible(res)) {
                continue;
            }
            mc.getTextureManager().bindTexture(res.getTexture());
            GlStateManager.disableDepth();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);

            renderBody(res);

            GlStateManager.enableDepth();
        }

    }

    public void renderScaledBody(Researchable res)
    {
        float x = viewportX(res.guiX(lastTickWTime, ticks));
        float y = viewportY(res.guiY(lastTickWTime, ticks));
        float scale = (float)res.getSize()/256;
        int yOffset = res.yTexOffset();
        if(res.getBody().getName().equals("moon")) {
            yOffset = WorldUtil.getMoonPhase() * 32;
        }
        float centerX = x+(float)res.getSize()/2;
        float centerY = y+(float)res.getSize()/2;

        GlStateManager.translate(centerX, centerY, 0f);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(-centerX, -centerY, 0f);

        float viewportBondX = ((guiLeft + 6) + viewportSize) - viewportX(res.getX())+6;
        float viewportBondY = ((guiTop + 24) + viewportSize) - viewportY(res.getY())+6;

        this.drawTexturedModalRect(x, y, 0, yOffset, (int) Math.min(viewportBondX/scale, (float)res.getSize()/scale), (int) Math.min(viewportBondY/scale, (float)res.getSize()/scale));
    }

    public void renderAsteroid(Researchable res)
    {

        float x = viewportX(res.guiX(lastTickWTime, ticks));
        float y = viewportY(res.guiY(lastTickWTime, ticks));
        float centerX = x+(float)res.getSize()/2;
        float centerY = y+(float)res.getSize()/2;
        GlStateManager.translate(centerX, centerY, 0f);
        GlStateManager.rotate(Minecraft.getMinecraft().world.getTotalWorldTime(), 1, 0, 45);
        GlStateManager.translate(-centerX, -centerY, 0f);

        float viewportBondX = ((guiLeft + 6) + viewportSize) - viewportX(res.getX())+6;
        float viewportBondY = ((guiTop + 24) + viewportSize) - viewportY(res.getY())+6;

        drawTexturedModalRect(x, y, 0, 0, (int) Math.min(viewportBondX, (float)res.getSize()), (int) Math.min(viewportBondY, (float)res.getSize()));

    }


    public void renderBody(Researchable res)
    {
        GlStateManager.pushMatrix();
        if(res.getBody().getName().contains("ASTEROID-")) {
            renderAsteroid(res);
        } else {
            renderScaledBody(res);
        }
        GlStateManager.popMatrix();
    }


    public void renderFocusArea()
    {
        int padding = 25;
        this.drawTexturedModalRect(guiLeft+6+padding, guiTop+24+padding, 176, 123, 21, 21);
        this.drawTexturedModalRect(guiLeft+6+padding, guiTop+3+viewportSize-padding, 176, 144, 21, 21);
        this.drawTexturedModalRect(guiLeft+6+viewportSize-padding-21, guiTop+24+padding, 176, 165, 21, 21);
        this.drawTexturedModalRect(guiLeft+6+viewportSize-padding-21, guiTop+24+viewportSize-padding-21, 176, 186, 21, 21);
    }

    public void initButtons()
    {
        int xpos = guiLeft+142;
        int ypos = guiTop+40;
        int size = 12;
        int padding = 6;
        this.buttonList.add(btnUp = new GuiButtonImage(0, xpos, ypos-size-padding, size, size, 176, 83, 12, guiTexture));
        this.buttonList.add(btnDown = new GuiButtonImage(1, xpos, ypos+size+padding, size, size, 176, 59, 12, guiTexture));
        this.buttonList.add(btnLeft = new GuiButtonImage(2, xpos-size-padding, ypos, size, size, 176, 11, 12, guiTexture));
        this.buttonList.add(btnRight = new GuiButtonImage(3, xpos+size+padding, ypos, size, size, 176, 35, 12, guiTexture));
        this.buttonList.add(btnMultiplier = new GuiButton(4, xpos-4, ypos-4, 20, 20, String.valueOf(tile.movementAmplifier)));
        this.buttonList.add(btnHelp = new GuiButtonImage(5, xpos+size, guiTop + 164, 13, 14, 176, 109, 0, guiTexture));
        this.buttonList.add(btnResearched = new GuiButtonImage(6, xpos-2, guiTop + 164, 12, 14, 0, 233, 0, guiTexture));

    }

    public void updateResearchedData()
    {
        List<String> lines = new ArrayList();
        lines.add(GCCoreUtil.translate("gui.telescope.researched"));
        String[] bodies = tile.getResearchedBodiesArray();

        for(String name: bodies) {
            if(name.contains("ASTEROID-")) {
                lines.add(name);
            } else {
                String planet = I18n.format("planet."+name);
                if(planet.equals("planet."+name)) {
                    planet = I18n.format("moon."+name);
                }
                lines.add(planet);
            }
        }
        if(bodies.length == 0) {
            lines.add(GCCoreUtil.translate("gui.telescope.researched.none"));
        }

        this.researchedRegion.tooltipStrings = lines;
    }
    private void addResearchedRegion()
    {
        updateResearchedData();
        this.researchedRegion.xPosition = guiLeft + 140;
        this.researchedRegion.yPosition = guiTop + 164;
        this.researchedRegion.parentWidth = this.width;
        this.researchedRegion.parentHeight = this.height;

        this.infoRegions.add(this.researchedRegion);
    }

    private void addHelpRegion()
    {
        List<String> help = new ArrayList();
        help.add(GCCoreUtil.translate("gui.help.desc.0"));
        help.add(GCCoreUtil.translate("gui.help.desc.1"));
        help.add(GCCoreUtil.translate("gui.help.desc.2"));

        this.helpRegion.tooltipStrings = help;
        this.helpRegion.xPosition = guiLeft + 154;
        this.helpRegion.yPosition = guiTop + 164;
        this.helpRegion.parentWidth = this.width;
        this.helpRegion.parentHeight = this.height;

        this.infoRegions.add(this.helpRegion);
    }

    private void addElectricInfoRegion()
    {
        List<String> electricityDesc = new ArrayList();
        electricityDesc.add(GCCoreUtil.translate("gui.energy_storage.desc.0"));
        electricityDesc.add(EnumColor.YELLOW + GCCoreUtil.translate("gui.energy_storage.desc.1") + (int)Math.floor((double)this.tile.getEnergyStoredGC()) + " / " + (int)Math.floor((double)this.tile.getMaxEnergyStoredGC()));
        this.electricInfoRegion.tooltipStrings = electricityDesc;
        this.electricInfoRegion.xPosition = (this.width - this.xSize) / 2 + 32;
        this.electricInfoRegion.yPosition = (this.height - this.ySize) / 2 + 165;
        this.electricInfoRegion.parentWidth = this.width;
        this.electricInfoRegion.parentHeight = this.height;
        this.infoRegions.add(this.electricInfoRegion);

        List<String> batterySlotDesc = new ArrayList();
        batterySlotDesc.add(GCCoreUtil.translate("gui.battery_slot.desc.0"));
        batterySlotDesc.add(GCCoreUtil.translate("gui.battery_slot.desc.1"));
        this.infoRegions.add(new GuiElementInfoRegion((this.width - this.xSize) / 2 + 9, (this.height - this.ySize) / 2 + 26, 18, 18, batterySlotDesc, this.width, this.height, this));

    }

    public void initGui() {
        super.initGui();
        addElectricInfoRegion();
        addHelpRegion();
        addResearchedRegion();
        initButtons();
    }



    private int scale(double value, double maxValue)
    {
        return (int)(maxValue/100*value);
    }

    private void renderProgressBar()
    {
        int progress = tile.getObservationProgress();
        this.drawTexturedModalRect(guiLeft+8, guiTop + 150, 0, 249, Math.min(scale(progress,159), 159), 7);

    }

    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        this.fontRenderer.drawString(GCCoreUtil.translate("gui.telescope"), 60, 10, 4210752);
        this.btnMultiplier.displayString = String.valueOf(tile.movementAmplifier);
        String statusLine = I18n.format("gui.telescope.status.idle");
        if(!tile.curObserveBody.isEmpty()) {
            String planet = "";
            if(tile.curObserveBody.contains("ASTEROID-")) {
                planet = "ASTEROID";
            } else {
                planet = I18n.format("planet."+tile.curObserveBody);
                if(planet.equals("planet."+tile.curObserveBody)) {
                    planet = I18n.format("moon."+tile.curObserveBody);
                }
            }
            statusLine =  I18n.format("gui.telescope.status.researching", planet);
        }
        if(tile.getWorld().canSeeSky(tile.getPos())) {
            this.fontRenderer.drawString(I18n.format("gui.telescope.status", statusLine), 8, 141, 4210752);
        } else {
            this.fontRenderer.drawString(I18n.format("gui.telescope.sky_blocked"), 8, 141, ColorUtil.to32BitColor(255,255, 0, 0));
        }
        updateResearchedData();
        tickButtons();
    }

    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(guiTexture);
        int var5 = (this.width - this.xSize) / 2;
        int var6 = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(var5, var6 + 5, 0, 0, this.xSize, this.ySize);
        List<String> electricityDesc = new ArrayList();
        electricityDesc.add(GCCoreUtil.translate("gui.energy_storage.desc.0"));
        EnergyDisplayHelper.getEnergyDisplayTooltip(tile.getEnergyStoredGC(), tile.getMaxEnergyStoredGC(), electricityDesc);
        this.electricInfoRegion.tooltipStrings = electricityDesc;
        if (tile.getEnergyStoredGC() > 0.0F) {
            this.drawTexturedModalRect(guiLeft+29, guiTop + 165, 176, 0, 11, 10);
        }

        this.drawTexturedModalRect(guiLeft+42, guiTop + 167, 187, 0, Math.min(tile.getScaledElecticalLevel(54), 54), 7);
        if(Minecraft.getMinecraft().world.isRaining() && Minecraft.getMinecraft().world.getBiome(tile.getPos()).canRain()) return;

        if(tile.getWorld().canSeeSky(tile.getPos())) {
            renderStars();
            renderPlanets();
        }
        mc.getTextureManager().bindTexture(overlay);
        this.drawTexturedModalRect(var5, var6 + 5, 0, 0, this.xSize, this.ySize);

        mc.getTextureManager().bindTexture(guiTexture);
        renderFocusArea();
        renderProgressBar();
    }
}
