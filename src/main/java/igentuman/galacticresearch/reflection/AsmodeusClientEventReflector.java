package igentuman.galacticresearch.reflection;

import asmodeuscore.core.astronomy.gui.screen.AC_GuiCelestialSelection;
import asmodeuscore.core.configs.AsmodeusConfig;
import asmodeuscore.core.event.AsmodeusClientEvent;
import igentuman.galacticresearch.client.gui.GRGuiCelestialSelectionGalaxySpace;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import micdoodle8.mods.galacticraft.core.tick.KeyHandlerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.GuiOpenEvent;

import java.util.List;

public class AsmodeusClientEventReflector {

    public static void onGuiOpenEvent(AsmodeusClientEvent inst, GuiOpenEvent event) {
        if (event.getGui() instanceof GuiCelestialSelection && AsmodeusConfig.enableNewGalaxyMap) {
            GameSettings var10000 = Minecraft.getMinecraft().gameSettings;
            if (GameSettings.isKeyDown(KeyHandlerClient.galaxyMap)) {
                event.setGui(new GRGuiCelestialSelectionGalaxySpace(true, (List)null, false));
            } else {
                event.setGui(new GRGuiCelestialSelectionGalaxySpace(false, (List)null, true));
            }

            if (GameSettings.isKeyDown(var10000.keyBindSneak)) {
                if (GameSettings.isKeyDown(KeyHandlerClient.galaxyMap)) {
                    event.setGui(new AC_GuiCelestialSelection(true, (List)null, false));
                }
            }
        }
    }

}
