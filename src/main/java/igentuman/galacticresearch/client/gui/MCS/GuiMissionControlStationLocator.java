package igentuman.galacticresearch.client.gui.MCS;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.common.container.ContainerMissionControlStation;
import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import igentuman.galacticresearch.network.GRPacketSimple;
import li.cil.repack.org.luaj.vm2.ast.Str;
import micdoodle8.mods.galacticraft.core.client.gui.container.GuiContainerGC;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementDropdown;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementInfoRegion;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementTextBox;
import micdoodle8.mods.galacticraft.core.energy.EnergyDisplayHelper;
import micdoodle8.mods.galacticraft.core.util.ColorUtil;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiMissionControlStationLocator extends GuiContainerGC implements GuiElementDropdown.IDropboxCallback, GuiElementTextBox.ITextBoxCallback {
    private static final ResourceLocation guiTexture = new ResourceLocation(GalacticResearch.MODID, "textures/gui/container/mission_control_station_locator.png");
    private final TileMissionControlStation tile;

    private GuiButtonImage btnHelp;
    private GuiButton locateBtn;
    private GuiElementTextBox xCord;
    private GuiElementTextBox yCord;
    private GuiButtonImage btnLocator;
    private GuiButtonImage btnMissions;
    private GuiElementDropdown locatableDropdown;
    private GuiElementDropdown stationsDropdown;
    private GuiElementDropdown locatorDataDropdown;

    private GuiElementInfoRegion electricInfoRegion;
    private GuiElementInfoRegion helpRegion;

    public GuiMissionControlStationLocator(InventoryPlayer par1InventoryPlayer, TileMissionControlStation tile) {
        super(new ContainerMissionControlStation(par1InventoryPlayer, tile));
        this.electricInfoRegion = new GuiElementInfoRegion(guiLeft + 30, guiTop + 167, 64, 9, new ArrayList(), this.width, this.height, this);
        this.helpRegion = new GuiElementInfoRegion(guiLeft + 154, guiTop + 164, 12, 12, new ArrayList(), this.width, this.height, this);
        this.tile = tile;
        this.ySize = 201;
    }

    private int scale(double value, double maxValue)
    {
        return (int)(maxValue/100*value);
    }

    private void renderProgressBar()
    {
        int progress = tile.getLocatorProgress();
        this.drawTexturedModalRect(guiLeft+6, guiTop+104, 0, 202, Math.min(scale(progress,163), 163), 7);
    }

    private void tickButtons()
    {
        if(selectedButton != null && selectedButton.isMouseOver()) {
            btnClick(selectedButton);
            selectedButton = null;
        }
        locateBtn.enabled = tile.locationCounter < 1;
    }

    protected void btnClick(GuiButton btn) {
        switch(btn.id) {
            case 3:
                GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.MCS_LOCATE_BUTTON, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{this.tile.getPos(), 0}));
           break;
            case 6:
               // locatorDataDropdown.optionStrings = new String[]{" ", " "};
                locatorDataDropdown.enabled = false;
                GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.OPEN_GUI_MISSIONS, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{this.tile.getPos(), 0}));
                break;
        }
    }


    public void initButtons()
    {
        int xpos = guiLeft+7;
        String[] dropboxStrings = new String[tile.getObjectsToLocate().length];

        for(int i = 0; i < tile.getObjectsToLocate().length; i++) {
            dropboxStrings[i] =  GCCoreUtil.translate("gui.dropdown."+tile.getObjectsToLocate()[i]);
        }
        String[] stations = new String[tile.getStations().length];

        for(int i = 0; i < tile.getStations().length; i++) {
            stations[i] =  GCCoreUtil.translate(tile.getStationName(tile.getStations()[i]));
        }

        this.buttonList.add(xCord = new GuiElementTextBox(0, this, guiLeft+ 8, guiTop + 80, 30, 15, "", false, 5, true));
        this.buttonList.add(yCord = new GuiElementTextBox(1, this, guiLeft+ 40, guiTop + 80, 30, 15, "", false, 5, true));
        this.buttonList.add(btnHelp = new GuiButtonImage(2, xpos+149, guiTop + 164, 13, 14, 176, 109, 0, guiTexture));
        this.buttonList.add(locateBtn = new GuiButton(3, guiLeft + 79, guiTop + 77, 90, 20,  GCCoreUtil.translate("gui.mission_control_station.locate")));
        this.buttonList.add(locatorDataDropdown =new GuiElementDropdown(4, this, guiLeft+8, guiTop+120, getLocatorDataItems()));
        this.buttonList.add(locatableDropdown = new GuiElementDropdown(5, this, guiLeft+8, 71, dropboxStrings));
        this.buttonList.add(btnMissions = new GuiButtonImage(6, guiLeft+4 , guiTop - 7, 70, 16, 176, 140, 0, guiTexture));
        this.buttonList.add(btnLocator = new GuiButtonImage(7, guiLeft+74 , guiTop - 7, 70, 16, 176, 124, 0, guiTexture));
        this.buttonList.add(stationsDropdown =new GuiElementDropdown(8, this, guiLeft+8, guiTop+25, stations));
    }

    public String[] getLocatorDataItems()
    {
        List<String> items = new ArrayList<>();
        for(String line: tile.locatorData.split(";")) {
            String[] pairs = line.split(",");
            if(pairs[0].isEmpty()) continue;
            items.add("X: "+ Float.valueOf(pairs[0]).intValue()+", Z: "+Float.valueOf(pairs[1]).intValue());
        }
        if(items.size() == 0) items.add(" ");
        return items.stream().toArray(String[]::new);
    }

    private void addHelpRegion()
    {
        List<String> help = new ArrayList();
        help.add(GCCoreUtil.translate("gui.help.mcs.locator.desc.0"));
        help.add(GCCoreUtil.translate("gui.help.mcs.locator.desc.1"));
        help.add(GCCoreUtil.translate("gui.help.mcs.locator.desc.2"));

        this.helpRegion.tooltipStrings = help;
        this.helpRegion.xPosition = guiLeft + 156;
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
        this.infoRegions.add(new GuiElementInfoRegion((this.width - this.xSize) / 2 + 9, (this.height - this.ySize) / 2 + 165, 18, 18, batterySlotDesc, this.width, this.height, this));

    }

    public void initGui() {
        super.initGui();
        addElectricInfoRegion();
        addHelpRegion();
        initButtons();
    }

    @Override
    protected void keyTyped(char keyChar, int keyID) throws IOException
    {
        if (keyID != Keyboard.KEY_ESCAPE)
        {
            if (this.xCord.keyTyped(keyChar, keyID))
            {
                return;
            }
            if (this.yCord.keyTyped(keyChar, keyID))
            {
                return;
            }
        }

        super.keyTyped(keyChar, keyID);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        this.fontRenderer.drawString(GCCoreUtil.translate("gui.mission_control_station.tab.missions"), 10, -3, 4210752);
        this.fontRenderer.drawString(GCCoreUtil.translate("gui.mission_control_station.tab.locator"), 80, -3, 4210752);

        this.fontRenderer.drawString(I18n.format("gui.mission_control_station.station"), 8, 15, 4210752);

        this.fontRenderer.drawString(I18n.format("gui.mission_control_station.locatable"), 8, 43, 4210752);
        this.fontRenderer.drawString(I18n.format("gui.mission_control_station.coordinates"), 8, 70, 4210752);
        this.fontRenderer.drawString(I18n.format("gui.mission_control_station.locator_data"), 8, 150, 4210752);
        updateLocatorData();
        tickButtons();
    }

    public void updateLocatorData()
    {
        if(tile.locationCounter > 0) {
            return;
        }
        locatorDataDropdown.optionStrings = getLocatorDataItems();
        int largestString = -2147483648;
        String[] var8 = locatorDataDropdown.optionStrings;
        int var9 = locatorDataDropdown.optionStrings.length;

        for(int var10 = 0; var10 < var9; ++var10) {
            String element = var8[var10];
            largestString = Math.max(largestString, this.fontRenderer.getStringWidth(element));
        }
        ;locatorDataDropdown.enabled = true;
        locatorDataDropdown.width = largestString + 8 + 15;
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
        renderProgressBar();

    }


    @Override
    public boolean canBeClickedBy(GuiElementDropdown guiElementDropdown, EntityPlayer entityPlayer) {
        return true;
    }

    @Override
    public void onSelectionChanged(GuiElementDropdown guiElementDropdown, int i) {
        if(guiElementDropdown.id == locatableDropdown.id) {
            GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.MCS_SELECT_LOCATABLE, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{this.tile.getPos(), i}));
        }
        if(guiElementDropdown.id == stationsDropdown.id) {
            GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.MCS_SELECT_STATION, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{this.tile.getPos(), i}));
        }
    }

    @Override
    public int getInitialSelection(GuiElementDropdown guiElementDropdown) {
        if(guiElementDropdown.id == locatableDropdown.id) {
            return tile.getLocatableObjectId();
        }
        if(guiElementDropdown.id == stationsDropdown.id) {
            return tile.getCurStationId();
        }
        return 0;
    }

    @Override
    public void onIntruderInteraction() {

    }

    @Override
    public boolean canPlayerEdit(GuiElementTextBox guiElementTextBox, EntityPlayer entityPlayer) {
        return true;
    }

    @Override
    public void onTextChanged(GuiElementTextBox guiElementTextBox, String s) {
        try {
            GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.EDIT_LOCATOR_CORDS, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{this.tile.getPos(), Integer.valueOf(xCord.text), Integer.valueOf(yCord.text)}));
        } catch (NumberFormatException ignored) {

        }
    }

    @Override
    public String getInitialText(GuiElementTextBox guiElementTextBox) {

        if(guiElementTextBox.id == xCord.id) {
            return String.valueOf(tile.getLocatorXCord());
        }
        if(guiElementTextBox.id == yCord.id) {
            return String.valueOf(tile.getLocatorZCord());
        }
        return "";
    }

    @Override
    public int getTextColor(GuiElementTextBox textBox)
    {
        return ColorUtil.to32BitColor(255, 200, 200, 200);
    }

    @Override
    public void onIntruderInteraction(GuiElementTextBox guiElementTextBox) {

    }
}