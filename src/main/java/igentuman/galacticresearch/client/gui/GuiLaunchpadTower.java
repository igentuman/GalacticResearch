package igentuman.galacticresearch.client.gui;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.common.container.ContainerLaunchpadTower;
import igentuman.galacticresearch.common.tile.TileLaunchpadTower;
import igentuman.galacticresearch.network.GRPacketSimple;
import micdoodle8.mods.galacticraft.core.client.gui.container.GuiContainerGC;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementCheckbox;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementInfoRegion;
import micdoodle8.mods.galacticraft.core.energy.EnergyDisplayHelper;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static igentuman.galacticresearch.GalacticResearch.MODID;

public class GuiLaunchpadTower extends GuiContainerGC implements GuiElementCheckbox.ICheckBoxCallback {
    public static final ResourceLocation loaderTexture = new ResourceLocation(MODID, "textures/gui/container/launchpad_tower.png");
    private final TileLaunchpadTower launchpadTower;
    private GuiElementCheckbox autoMount;
    private GuiElementInfoRegion electricInfoRegion;
    private GuiButton btnMount;
    private GuiButton btnUnmount;

    public GuiLaunchpadTower(InventoryPlayer par1InventoryPlayer, TileLaunchpadTower par2TileEntityAirDistributor) {
        super(new ContainerLaunchpadTower(par1InventoryPlayer, par2TileEntityAirDistributor));
        this.electricInfoRegion = new GuiElementInfoRegion((this.width - this.xSize) / 2 + 107, (this.height - this.ySize) / 2 + 101, 56, 9, new ArrayList(), this.width, this.height, this);
        this.launchpadTower = par2TileEntityAirDistributor;
        this.ySize = 201;
    }

    protected void actionPerformed(GuiButton par1GuiButton) {
        switch(par1GuiButton.id) {
            case 0:
                //GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.TOGGLE_AUTOMATIC_MOUNTING, GCCoreUtil.getDimensionID(this.launchpadTower.getWorld()), new Object[]{this.launchpadTower.getPos(), 0}));
                break;
           case 1:
               GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.MOUNT_ROCKET, GCCoreUtil.getDimensionID(this.launchpadTower.getWorld()), new Object[]{this.launchpadTower.getPos(), 0}));
               break;
            case 2:
               GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.UNMOUNT_ROCKET, GCCoreUtil.getDimensionID(this.launchpadTower.getWorld()), new Object[]{this.launchpadTower.getPos(), 0}));
                break;
            default:
        }
    }

    public boolean canPlayerEdit(GuiElementCheckbox checkbox, EntityPlayer player) {
        return true;
    }

    public void onIntruderInteraction() {

    }

    public boolean getInitiallySelected(GuiElementCheckbox checkbox) {
        if (checkbox.equals(this.autoMount)) {
            return this.launchpadTower.autoMount;
        }
        return false;
    }

    public void initGui() {
        super.initGui();
        List<String> electricityDesc = new ArrayList();
        electricityDesc.add(GCCoreUtil.translate("gui.energy_storage.desc.0"));
        electricityDesc.add(EnumColor.YELLOW + GCCoreUtil.translate("gui.energy_storage.desc.1") + (int)Math.floor((double)this.launchpadTower.getEnergyStoredGC()) + " / " + (int)Math.floor((double)this.launchpadTower.getMaxEnergyStoredGC()));
        this.electricInfoRegion.tooltipStrings = electricityDesc;
        this.electricInfoRegion.xPosition = (this.width - this.xSize) / 2 + 112;
        this.electricInfoRegion.yPosition = (this.height - this.ySize) / 2 + 108;
        this.electricInfoRegion.parentWidth = this.width;
        this.electricInfoRegion.parentHeight = this.height;
        this.infoRegions.add(this.electricInfoRegion);
        List<String> batterySlotDesc = new ArrayList();
        batterySlotDesc.add(GCCoreUtil.translate("gui.battery_slot.desc.0"));
        batterySlotDesc.add(GCCoreUtil.translate("gui.battery_slot.desc.1"));
        this.infoRegions.add(new GuiElementInfoRegion((this.width - this.xSize) / 2 + 9, (this.height - this.ySize) / 2 + 26, 18, 18, batterySlotDesc, this.width, this.height, this));
        this.autoMount = new GuiElementCheckbox(0, this, guiLeft+38, guiTop + 63, GCCoreUtil.translate("gui.checkbox.automount"));
        this.buttonList.add(autoMount);
        int btnWidth = 77;
        this.buttonList.add(this.btnMount = new GuiButton(1, guiLeft  + 8, this.height / 2 - 23, btnWidth, 20, GCCoreUtil.translate("gui.button.mount")));
        this.buttonList.add(this.btnUnmount = new GuiButton(2, guiLeft + btnWidth+12, this.height / 2 - 23, btnWidth, 20, GCCoreUtil.translate("gui.button.unmount")));
    }

    public void onSelectionChanged(GuiElementCheckbox checkbox, boolean newSelected) {
        if (checkbox.equals(this.autoMount)) {
            this.launchpadTower.autoMount = !newSelected;
            GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.TOGGLE_AUTOMATIC_MOUNTING, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{this.launchpadTower.getPos(), 0}));
        }
    }

    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        int offsetX = -17;
        int offsetY = 45;
        this.fontRenderer.drawString(this.launchpadTower.getName(), 60, 12, 4210752);
        this.fontRenderer.drawString(GCCoreUtil.translate("container.inventory"), 8, this.ySize - 90, 4210752);
    }


    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(loaderTexture);
        int var5 = (this.width - this.xSize) / 2;
        int var6 = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(var5, var6 + 5, 0, 0, this.xSize, this.ySize);
        List<String> electricityDesc = new ArrayList();
        electricityDesc.add(GCCoreUtil.translate("gui.energy_storage.desc.0"));
        EnergyDisplayHelper.getEnergyDisplayTooltip(this.launchpadTower.getEnergyStoredGC(), this.launchpadTower.getMaxEnergyStoredGC(), electricityDesc);
        this.electricInfoRegion.tooltipStrings = electricityDesc;
        if (this.launchpadTower.getEnergyStoredGC() > 0.0F) {
            this.drawTexturedModalRect(var5 + 99, var6 + 108, 176, 0, 11, 10);
        }

        this.drawTexturedModalRect(var5 + 113, var6 + 109, 187, 0, Math.min(this.launchpadTower.getScaledElecticalLevel(54), 54), 7);
    }
}