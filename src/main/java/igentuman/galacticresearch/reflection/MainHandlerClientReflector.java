package igentuman.galacticresearch.reflection;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.client.handlers.MainHandlerClient;
import com.mjr.mjrlegendslib.util.MessageUtilities;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.client.gui.GRGuiCelestialSelectionExtraPlanets;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.GuiOpenEvent;

public class MainHandlerClientReflector {

    public static void onGuiOpenEvent(MainHandlerClient instance, GuiOpenEvent event) {
        if(!ModConfig.researchSystem.extraplanets_intergration) return;
        if (Config.USE_CUSTOM_CELESTIAL_SELECTION && event.getGui() instanceof GuiCelestialSelection) {
            if (event.getGui().getClass().getName().equalsIgnoreCase("asmodeuscore.core.astronomy.gui.screen.NewGuiCelestialSelection")) {
                MessageUtilities.throwCrashError("Please disable the following option: enableNewGalaxyMap in configs/AsmodeusCore/core.conf");
            }

            if (GameSettings.isKeyDown(micdoodle8.mods.galacticraft.core.tick.KeyHandlerClient.galaxyMap)) {
                event.setGui(new GRGuiCelestialSelectionExtraPlanets(true, ((GuiCelestialSelection)event.getGui()).possibleBodies, ((GuiCelestialSelection)event.getGui()).canCreateStations));
            } else {
                event.setGui(new GRGuiCelestialSelectionExtraPlanets(false, ((GuiCelestialSelection)event.getGui()).possibleBodies, ((GuiCelestialSelection)event.getGui()).canCreateStations));
            }
        }
    }

}
