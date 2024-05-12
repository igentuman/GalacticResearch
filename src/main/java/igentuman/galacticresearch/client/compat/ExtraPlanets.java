package igentuman.galacticresearch.client.compat;

import com.mjr.extraplanets.api.event.CustomCelestialGUIEvent;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static igentuman.galacticresearch.GalacticResearch.MODID;
import static igentuman.galacticresearch.util.PlayerUtil.isUnlocked;

@Mod.EventBusSubscriber(modid = MODID, value = Side.CLIENT)
public class ExtraPlanets {

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void filterGalaxies(CustomCelestialGUIEvent.PreLoadingGalaxies preEvent) {
        for (SolarSystem solarSystem : GalaxyRegistry.getRegisteredSolarSystems().values()) {
            String parentGalaxyName = solarSystem.getUnlocalizedParentGalaxyName();
            if(!isUnlocked(solarSystem.getName())) {
                preEvent.solarSystemUnlocalizedNamesToIgnore.add(solarSystem.getUnlocalizedName());
                preEvent.solarSystemUnlocalizedNamesToIgnore.add(solarSystem.getName());
                if(!isUnlocked(parentGalaxyName)) {
                    preEvent.solarSystemUnlocalizedNamesToIgnore.add(parentGalaxyName);
                }
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void filterBodies(CustomCelestialGUIEvent.PreLoadingCelestialBodies preEvent) {
        for (SolarSystem solarSystem : GalaxyRegistry.getRegisteredSolarSystems().values()) {
            String parentGalaxyName = solarSystem.getUnlocalizedParentGalaxyName();
            if(!isUnlocked(solarSystem.getName())) {
                preEvent.bodyNamesToIgnore.add(solarSystem.getUnlocalizedName());
                preEvent.bodyNamesToIgnore.add(solarSystem.getName());
                if(!isUnlocked(parentGalaxyName)) {
                    preEvent.bodyNamesToIgnore.add(parentGalaxyName);
                }
            }
        }
        for (Planet planet : GalaxyRegistry.getRegisteredPlanets().values()) {
            if(!isUnlocked(planet.getName())) {
                preEvent.bodyUnlocalizedNamesToIgnore.add(planet.getUnlocalizedName());
                preEvent.bodyNamesToIgnore.add(planet.getName());
            }
        }

        for (Moon moon : GalaxyRegistry.getRegisteredMoons().values()) {
            if(!isUnlocked(moon.getName())) {
                preEvent.bodyUnlocalizedNamesToIgnore.add(moon.getUnlocalizedName());
                preEvent.bodyNamesToIgnore.add(moon.getName());
            }
        }
    }
}
