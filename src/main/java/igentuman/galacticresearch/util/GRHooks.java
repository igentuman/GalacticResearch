package igentuman.galacticresearch.util;

import dan200.computercraft.api.ComputerCraftAPI;
import igentuman.galacticresearch.integration.computer.CCPeripheral;
import igentuman.galacticresearch.integration.computer.OCDriver;
import li.cil.oc.api.Driver;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional.Method;

public final class GRHooks {

    public boolean CCLoaded = false;
    public boolean CraftTweakerLoaded = false;
    public boolean OCLoaded = false;
    public boolean ExtraPlanetsLoaded = false;
    public boolean IELoaded = false;
    public boolean AE2Loaded = false;
    public boolean GalaxySpaceLoaded = false;

    public void hookPreInit() {
        CCLoaded = Loader.isModLoaded("computercraft");
        CraftTweakerLoaded = Loader.isModLoaded("crafttweaker");
        OCLoaded = Loader.isModLoaded("opencomputers");
        ExtraPlanetsLoaded = Loader.isModLoaded("extraplanets");
        GalaxySpaceLoaded = Loader.isModLoaded("galaxyspace");
        IELoaded = Loader.isModLoaded("immersiveengineering");
        AE2Loaded = Loader.isModLoaded("appliedenergistics2");

    }

    public void hookInit() {
        if (OCLoaded) {
            loadOCDrivers();
        }
    }

    public void hookPostInit() {

        if (CCLoaded) {
            loadCCPeripheralProviders();
        }

        if (CraftTweakerLoaded) {
            //CrafttweakerIntegration.registerCommands();
           // CrafttweakerIntegration.applyRecipeChanges();
        }
    }

    @Method(modid = "computercraft")
    private void loadCCPeripheralProviders() {
        try {
            ComputerCraftAPI.registerPeripheralProvider(new CCPeripheral.CCPeripheralProvider());
        } catch (Exception ignored) {
        }
    }

    @Method(modid = "opencomputers")
    private void loadOCDrivers() {
        try {
            Driver.add(new OCDriver());
        } catch (Exception ignored) {
        }
    }
}