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
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static igentuman.galacticresearch.common.tile.TileTelescope.viewportSize;


public class GuiTelescope extends GuiContainerGC {
    private static final ResourceLocation guiTexture = new ResourceLocation(GalacticResearch.MODID, "textures/gui/container/telescope_hd.png");
    private final TileTelescope tile;

    private GuiButtonImage btnUp;
    private GuiButtonImage btnDown;
    private GuiButtonImage btnLeft;
    private GuiButtonImage btnRight;
    private GuiButton btnMultiplier;
    private GuiButtonImage btnHelp;
    private Map<String,GuiButtonImage> planets = new HashMap<>();

    private GuiElementInfoRegion electricInfoRegion;
    private GuiElementInfoRegion helpRegion;
    private GuiElementInfoRegion researchedRegion;
    private GuiButtonImage btnResearched;

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
        return  body.getX() > tile.xAngle &&
                body.getY() > tile.yAngle &&
                body.getX() < (tile.xAngle + viewportSize) &&
                body.getY() < (tile.yAngle + viewportSize) &&
                body.isVisible();
    }

    public int viewportX(int x)
    {
        return (guiLeft + 6) + x - tile.xAngle;
    }

    public int viewportY(int y)
    {
        return (guiTop + 24) + y - tile.yAngle;
    }

    public float sh = 0;
    public int[] curStar = new int[] {1,1};

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
            int x = viewportX(star.getX());
            int y = viewportY(star.getY());
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
            int x = viewportX(res.getX());
            int y = viewportY(res.getY());

            int viewportBondX = ((guiLeft + 6) + viewportSize) - viewportX(res.getX());
            int viewportBondY = ((guiTop + 24) + viewportSize) - viewportY(res.getY());

            mc.getTextureManager().bindTexture(res.getTexture());
            GlStateManager.disableDepth();
            int yOffset = res.yTexOffset();
            if(res.getBody().getName().equals("moon")) {
                yOffset = WorldUtil.getMoonPhase() * 32;
            }
            this.drawTexturedModalRect(x, y, 0, yOffset, Math.min(viewportBondX, res.getSize()), Math.min(viewportBondY, res.getSize()));
            GlStateManager.enableDepth();
        }
    }

    public void renderFocusArea()
    {
        this.drawTexturedModalRect(guiLeft+6+viewportSize/2-25, guiTop+24+viewportSize/2-25, 176, 123, 21, 21);
        this.drawTexturedModalRect(guiLeft+6+viewportSize/2-25, guiTop+24+viewportSize/2+5, 176, 144, 21, 21);
        this.drawTexturedModalRect(guiLeft+6+viewportSize/2+5, guiTop+24+viewportSize/2-25, 176, 165, 21, 21);
        this.drawTexturedModalRect(guiLeft+6+viewportSize/2+5, guiTop+24+viewportSize/2+5, 176, 186, 21, 21);

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
            lines.add(GCCoreUtil.translate("gui."+name+".name"));
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
            String planet = I18n.format("gui."+tile.curObserveBody+".name");
            statusLine =  I18n.format("gui.telescope.status.researching", planet);
        }
        this.fontRenderer.drawString(I18n.format("gui.telescope.status", statusLine), 8, 141, 4210752);
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

        renderStars();
        renderPlanets();
        mc.getTextureManager().bindTexture(guiTexture);
        renderFocusArea();
        renderProgressBar();
    }
}
