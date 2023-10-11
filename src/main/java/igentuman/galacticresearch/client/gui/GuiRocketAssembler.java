package igentuman.galacticresearch.client.gui;

import java.util.ArrayList;
import java.util.List;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.common.container.ContainerRocketAssembler;
import igentuman.galacticresearch.common.tile.TileRocketAssembler;
import igentuman.galacticresearch.network.GRPacketSimple;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import micdoodle8.mods.galacticraft.core.client.gui.container.GuiContainerGC;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementInfoRegion;
import micdoodle8.mods.galacticraft.core.energy.EnergyDisplayHelper;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static igentuman.galacticresearch.GalacticResearch.MODID;

@SideOnly(Side.CLIENT)
public class GuiRocketAssembler extends GuiContainerGC
{
    private GuiButtonImage prevBtn;
    private GuiButtonImage nextBtn;
    private static final ResourceLocation GUI = new ResourceLocation(MODID, "textures/gui/container/rocket_assembler.png");
    private TileRocketAssembler tile;
    private GuiElementInfoRegion electricInfoRegion = new GuiElementInfoRegion(0, 0, 56, 9, null, 0, 0, this);
    private GuiElementInfoRegion processInfoRegion = new GuiElementInfoRegion(0, 0, 52, 9, null, 0, 0, this);

    public GuiRocketAssembler(InventoryPlayer par1InventoryPlayer, TileRocketAssembler tileEntity)
    {
        super(new ContainerRocketAssembler(par1InventoryPlayer, tileEntity));
        this.tile = tileEntity;
        this.ySize = 201;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        List<String> electricityDesc = new ArrayList();
        electricityDesc.add(GCCoreUtil.translate("gui.energy_storage.desc.0"));
        electricityDesc.add(EnumColor.YELLOW + GCCoreUtil.translate("gui.energy_storage.desc.1") + (int)Math.floor((double)this.tile.getEnergyStoredGC()) + " / " + (int)Math.floor((double)this.tile.getMaxEnergyStoredGC()));
        this.electricInfoRegion.tooltipStrings = electricityDesc;
        this.electricInfoRegion.xPosition = (this.width - this.xSize) / 2 + 112;
        this.electricInfoRegion.yPosition = (this.height - this.ySize) / 2 + 108;
        this.electricInfoRegion.parentWidth = this.width;
        this.electricInfoRegion.parentHeight = this.height;
        List<String> desc = new ArrayList<String>();
        desc.add(GCCoreUtil.translate("gui.battery_slot.desc.0"));
        desc.add(GCCoreUtil.translate("gui.battery_slot.desc.1"));
        this.electricInfoRegion = new GuiElementInfoRegion((this.width - this.xSize) / 2 + 107, (this.height - this.ySize) / 2 + 101, 56, 9, new ArrayList(), this.width, this.height, this);

        this.processInfoRegion.tooltipStrings = new ArrayList<>();
        this.processInfoRegion.xPosition = (this.width - this.xSize) / 2 + 110;
        this.processInfoRegion.yPosition = (this.height - this.ySize) / 2 + 90;
        this.processInfoRegion.parentWidth = this.width;
        this.processInfoRegion.parentHeight = this.height;
        this.infoRegions.add(this.processInfoRegion);
        
        this.infoRegions.add(new GuiElementInfoRegion((this.width + this.xSize) / 2, (this.height - this.ySize) / 2 + 16, 18, 21 * 4, desc, this.width, this.height, this));
        initButtons();
    }

    public void initButtons()
    {
        int xpos = guiLeft+13;
        int ypos = guiTop+97;
        this.buttonList.add(prevBtn = new GuiButtonImage(0, xpos, ypos, 11, 16, 176, 44, 16, GUI));
        this.buttonList.add(nextBtn = new GuiButtonImage(1, xpos+33, ypos, 11, 16, 176, 12, 16, GUI));
    }
    int btnDelay = 10;
    private void tickButtons()
    {
        if(btnDelay > 0) {
            btnDelay--;
            return;
        }
        btnDelay = 10;
        if(selectedButton != null && selectedButton.isMouseOver()) {
            actionPerformed(selectedButton);
        }
    }


    protected void actionPerformed(GuiButton par1GuiButton) {
        switch(par1GuiButton.id) {
            case 0:
                GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.ASSEMBLER_RECIPE, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{this.tile.getPos(), -1}));
                break;
            case 1:
                GalacticResearch.packetPipeline.sendToServer(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.ASSEMBLER_RECIPE, GCCoreUtil.getDimensionID(this.mc.world), new Object[]{this.tile.getPos(), 1}));
                break;
            default:
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        this.fontRenderer.drawString(this.tile.getName(), 19, 7, 4210752);
        tickButtons();
        renderCurrentRecipe(par1, par2);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        this.mc.renderEngine.bindTexture(GUI);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int containerWidth = (this.width - this.xSize) / 2;
        int containerHeight = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(containerWidth, containerHeight, 0, 0, this.xSize, this.ySize);
        int scale;

        List<String> electricityDesc = new ArrayList<String>();
        electricityDesc.add(GCCoreUtil.translate("gui.energy_storage.desc.0"));
        EnergyDisplayHelper.getEnergyDisplayTooltip(this.tile.getEnergyStoredGC(), this.tile.getMaxEnergyStoredGC(), electricityDesc);
        this.electricInfoRegion.tooltipStrings = electricityDesc;

        if (this.tile.processTicks > 0)
        {
            scale = (int) ((double) tile.processTicks / (double) tile.processTicks * 100);
        }
        else
        {
            scale = 0;
        }

        List<String> processDesc = new ArrayList<String>();
        processDesc.clear();
        processDesc.add(GCCoreUtil.translate("gui.electric_compressor.desc.0") + ": " + scale + "%");
        this.processInfoRegion.tooltipStrings = processDesc;

        this.drawTexturedModalRect(containerWidth + 100, containerHeight + 91, 204, 96, 52, 9);
       
        this.drawTexturedModalRect(containerWidth + 125, containerHeight + 147, 204, 96, 52, 9);
        this.drawTexturedModalRect(containerWidth + 110, containerHeight + 147, 217, 239, 11, 10);
        
        if (this.tile.processTicks > 0)
        {
            scale = (int) ((100D-(double) this.tile.processTicks) / 100D * 13);
            GL11.glColor4f(0.0F, 1.0F, 0.0F, 1.0F);
            this.drawTexturedModalRect(containerWidth + 140, containerHeight + 23, 176, 82, scale, 66);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

       this.electricInfoRegion.tooltipStrings = electricityDesc;
        if (this.tile.getEnergyStoredGC() > 0.0F) {
            this.drawTexturedModalRect(containerWidth + 99, containerHeight + 104, 176, 0, 11, 10);
        }
        this.drawTexturedModalRect(containerWidth + 113, containerHeight + 104, 187, 0, Math.min(this.tile.getScaledElecticalLevel(54), 54), 7);

    }
    private void renderCurrentRecipe(int x, int y) {
        ItemStack recipe = tile.getResultItem();
        if(recipe != null && !recipe.isEmpty()) {
            this.mc.renderEngine.bindTexture(GUI);
            this.itemRender.renderItemIntoGUI(recipe, 27, 97);
            if(x > 27+guiLeft && x < 27+16+guiLeft && y > 97+guiTop && y < 97+16+guiTop) {
                this.renderToolTip(recipe, x-guiLeft, y-guiTop);
            }
        }
    }
}