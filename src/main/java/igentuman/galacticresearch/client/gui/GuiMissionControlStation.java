package igentuman.galacticresearch.client.gui;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.common.container.ContainerMissionControlStation;
import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import igentuman.galacticresearch.network.GRPacketSimple;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.gui.container.GuiContainerGC;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementCheckbox;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementDropdown;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementInfoRegion;
import micdoodle8.mods.galacticraft.core.energy.EnergyDisplayHelper;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
import micdoodle8.mods.galacticraft.planets.mars.network.PacketSimpleMars;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GuiMissionControlStation extends GuiContainerGC implements GuiElementDropdown.IDropboxCallback {
    private static final ResourceLocation guiTexture = new ResourceLocation(GalacticResearch.MODID, "textures/gui/container/mission_control_station.png");
    private final TileMissionControlStation tile;

    private GuiButtonImage btnUp;
    private GuiButtonImage btnDown;
    private GuiButtonImage btnLeft;
    private GuiButtonImage btnRight;
    private GuiButton btnMultiplier;
    private GuiButtonImage btnHelp;
    private GuiButtonImage btnTelescope;
    private Map<String,GuiButtonImage> planets = new HashMap<>();
    private GuiElementDropdown targetPlanet;

    private GuiElementInfoRegion electricInfoRegion;
    private GuiElementInfoRegion helpRegion;
    private GuiElementInfoRegion researchedRegion;
    private GuiButtonImage btnResearched;
    private int cannotEditTimer;

    public GuiMissionControlStation(InventoryPlayer par1InventoryPlayer, TileMissionControlStation tile) {
        super(new ContainerMissionControlStation(par1InventoryPlayer, tile));
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

    public void onIntruderInteraction() {
        this.cannotEditTimer = 50;
    }

    public boolean canBeClickedBy(GuiElementDropdown dropdown, EntityPlayer player) {
        return true;
    }

    public void onSelectionChanged(GuiElementDropdown dropdown, int selection) {
        if (dropdown.equals(this.targetPlanet)) {
            this.tile.targetPlanet = String.valueOf(selection);
            GalacticraftCore.packetPipeline.sendToServer(new PacketSimpleMars(PacketSimpleMars.EnumSimplePacketMars.S_UPDATE_ADVANCED_GUI, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{1, tile.getPos(), this.tile.targetPlanet}));
        }

    }

    public int getInitialSelection(GuiElementDropdown dropdown) {
        return dropdown.equals(this.targetPlanet) ? Integer.parseInt(tile.targetPlanet) : 0;
    }

    public void onSelectionChanged(GuiElementCheckbox checkbox, boolean newSelected) {
/*        if (checkbox.equals(this.enablePadRemovalButton)) {
            this.launchController.launchPadRemovalDisabled = !newSelected;
            GalacticraftCore.packetPipeline.sendToServer(new PacketSimpleMars(PacketSimpleMars.EnumSimplePacketMars.S_UPDATE_ADVANCED_GUI, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{3, this.launchController.getPos(), this.launchController.launchPadRemovalDisabled ? 1 : 0}));
        } else if (checkbox.equals(this.launchWhenCheckbox)) {
            this.launchController.launchSchedulingEnabled = newSelected;
            GalacticraftCore.packetPipeline.sendToServer(new PacketSimpleMars(PacketSimpleMars.EnumSimplePacketMars.S_UPDATE_ADVANCED_GUI, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{4, this.launchController.getPos(), this.launchController.launchSchedulingEnabled ? 1 : 0}));
        }*/

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
        this.buttonList.add(btnHelp = new GuiButtonImage(5, xpos+size, guiTop + 164, 13, 14, 176, 109, 0, guiTexture));
        this.buttonList.add(btnResearched = new GuiButtonImage(6, xpos-2, guiTop + 164, 12, 14, 0, 233, 0, guiTexture));
        this.buttonList.add(btnResearched = new GuiButtonImage(6, xpos-2, guiTop + 164, 12, 14, 0, 233, 0, guiTexture));

    }

    public void updateResearchedData()
    {
        List<String> lines = new ArrayList();
        lines.add(GCCoreUtil.translate("gui.telescope.researched"));
        String[] bodies = tile.getResearchedBodies();
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

        this.targetPlanet = new GuiElementDropdown(3, this, guiLeft + 52, guiTop + 52, new String[]{});
        addElectricInfoRegion();
        addHelpRegion();
        addResearchedRegion();
        initButtons();
    }

    private int scale(double value, double maxValue)
    {
        return (int)(maxValue/100*value);
    }



    protected void drawGuiContainerForegroundLayer(int par1, int par2) {

        this.fontRenderer.drawString(GCCoreUtil.translate("gui.telescope"), 60, 10, 4210752);

        this.fontRenderer.drawString(I18n.format("gui.telescope.status"), 8, 141, 4210752);
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

        mc.getTextureManager().bindTexture(guiTexture);

    }
}
