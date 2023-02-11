package igentuman.galacticresearch.reflection;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.client.handlers.MainHandlerClient;
import com.mjr.mjrlegendslib.util.MessageUtilities;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.client.gui.GRGuiCelestialSelection;
import igentuman.galacticresearch.client.gui.GRGuiCelestialSelectionExtraPlanets;
import igentuman.galacticresearch.client.gui.GRGuiCelestialSelectionGalaxySpace;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.GuiOpenEvent;

public class MainHandlerClientReflector {

    public static void onGuiOpenEvent(MainHandlerClient instance, GuiOpenEvent event) {
        if(ModConfig.researchSystem.extraplanets_intergration) {
            if (Config.USE_CUSTOM_CELESTIAL_SELECTION && event.getGui().getClass().getName().equals("com.mjr.extraplanets.client.gui.screen.CustomCelestialSelection")) {
                if (GameSettings.isKeyDown(micdoodle8.mods.galacticraft.core.tick.KeyHandlerClient.galaxyMap)) {
                    event.setGui(new GRGuiCelestialSelectionExtraPlanets(true, ((GuiCelestialSelection)event.getGui()).possibleBodies, ((GuiCelestialSelection)event.getGui()).canCreateStations));
                } else {
                    event.setGui(new GRGuiCelestialSelectionExtraPlanets(false, ((GuiCelestialSelection)event.getGui()).possibleBodies, ((GuiCelestialSelection)event.getGui()).canCreateStations));
                }
                return;
            }
        }

        if(ModConfig.researchSystem.galaxy_space_integration) {
            if(event.getGui() != null && event.getGui().getClass().getName().equals("asmodeuscore.core.astronomy.gui.screen.NewGuiCelestialSelection")) {
                if (GameSettings.isKeyDown(micdoodle8.mods.galacticraft.core.tick.KeyHandlerClient.galaxyMap)) {
                    event.setGui(new GRGuiCelestialSelectionGalaxySpace(true, ((GuiCelestialSelection) event.getGui()).possibleBodies, ((GuiCelestialSelection) event.getGui()).canCreateStations));
                } else {
                    event.setGui(new GRGuiCelestialSelectionGalaxySpace(false, ((GuiCelestialSelection) event.getGui()).possibleBodies, ((GuiCelestialSelection) event.getGui()).canCreateStations));
                }
                return;
            }
        }

        if (((event.getGui() instanceof GuiCelestialSelection))) {
            if (GameSettings.isKeyDown(micdoodle8.mods.galacticraft.core.tick.KeyHandlerClient.galaxyMap)) {
                event.setGui(new GRGuiCelestialSelection(true, ((GuiCelestialSelection) event.getGui()).possibleBodies, ((GuiCelestialSelection) event.getGui()).canCreateStations));
            } else {
                event.setGui(new GRGuiCelestialSelection(false, ((GuiCelestialSelection) event.getGui()).possibleBodies, ((GuiCelestialSelection) event.getGui()).canCreateStations));
            }
        }
    }

}
