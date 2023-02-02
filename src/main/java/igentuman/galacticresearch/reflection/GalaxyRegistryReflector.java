package igentuman.galacticresearch.reflection;

import com.google.common.collect.*;
import micdoodle8.mods.galacticraft.api.galaxies.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.*;

public class GalaxyRegistryReflector {

    static int maxSolarSystemID = 0;
    static int maxPlanetID = 0;
    static int maxMoonID = 0;
    static int maxSatelliteID = 0;
    static HashMap<String, SolarSystem> solarSystems = Maps.newHashMap();
    static BiMap<String, Integer> solarSystemIDs = HashBiMap.create();
    static TreeMap<String, Planet> planets = Maps.newTreeMap();
    static BiMap<String, Integer> planetIDs = HashBiMap.create();
    static TreeMap<String, Moon> moons = Maps.newTreeMap();
    static BiMap<String, Integer> moonIDs = HashBiMap.create();
    static HashMap<String, Satellite> satellites = Maps.newHashMap();
    static BiMap<String, Integer> satelliteIDs = HashBiMap.create();
    static HashMap<Planet, List<Moon>> moonList = Maps.newHashMap();
    static HashMap<CelestialBody, List<Satellite>> satelliteList = Maps.newHashMap();
    static HashMap<SolarSystem, List<Planet>> solarSystemList = Maps.newHashMap();

    public static CelestialBody getCelestialBodyFromDimensionID(int dimensionID) {
        Iterator var1 = planets.values().iterator();

        Planet planet;
        do {
            if (!var1.hasNext()) {
                var1 = moons.values().iterator();

                Moon moon;
                do {
                    if (!var1.hasNext()) {
                        var1 = satellites.values().iterator();

                        Satellite satellite;
                        do {
                            if (!var1.hasNext()) {
                                return null;
                            }

                            satellite = (Satellite)var1.next();
                        } while(satellite.getDimensionID() != dimensionID);

                        return satellite;
                    }

                    moon = (Moon)var1.next();
                } while(moon.getDimensionID() != dimensionID);

                return moon;
            }

            planet = (Planet)var1.next();
        } while(planet.getDimensionID() != dimensionID);

        return planet;
    }

    public static void refreshGalaxies() {
        moonList.clear();
        satelliteList.clear();
        solarSystemList.clear();
        Iterator var0 = getRegisteredMoons().values().iterator();

        Planet celestialBody;
        Object planetList;
        while(var0.hasNext()) {
            Moon moon = (Moon)var0.next();
            celestialBody = moon.getParentPlanet();
            planetList = moonList.get(celestialBody);
            if (planetList == null) {
                planetList = new ArrayList();
            }

            ((List)planetList).add(moon);
            moonList.put(celestialBody, (List<Moon>) planetList);
        }

        var0 = getRegisteredSatellites().values().iterator();

        while(var0.hasNext()) {
            Satellite satellite = (Satellite)var0.next();
            celestialBody = satellite.getParentPlanet();
            planetList = satelliteList.get(celestialBody);
            if (planetList == null) {
                planetList = new ArrayList();
            }

            ((List)planetList).add(satellite);
            satelliteList.put(celestialBody, (List<Satellite>) planetList);
        }

        var0 = getRegisteredPlanets().values().iterator();

        while(var0.hasNext()) {
            Planet planet = (Planet)var0.next();
            SolarSystem solarSystem = planet.getParentSolarSystem();
            planetList = solarSystemList.get(solarSystem);
            if (planetList == null) {
                planetList = new ArrayList();
            }

            ((List)planetList).add(planet);
            solarSystemList.put(solarSystem, (List<Planet>) planetList);
        }

    }

    public static List<Planet> getPlanetsForSolarSystem(SolarSystem solarSystem) {
        List<Planet> solarSystemListLocal = solarSystemList.get(solarSystem);
        return solarSystemListLocal == null ? new ArrayList() : ImmutableList.copyOf(solarSystemListLocal);
    }

    public static List<Moon> getMoonsForPlanet(Planet planet) {
        List<Moon> moonListLocal = moonList.get(planet);
        return moonListLocal == null ? new ArrayList() : ImmutableList.copyOf(moonListLocal);
    }

    public static List<Satellite> getSatellitesForCelestialBody( CelestialBody celestialBody) {
        List<Satellite> satelliteList1 = satelliteList.get(celestialBody);
        return satelliteList1 == null ? new ArrayList() : ImmutableList.copyOf(satelliteList1);
    }

    public static CelestialBody getCelestialBodyFromUnlocalizedName(String unlocalizedName) {
        Iterator var1 = planets.values().iterator();

        Planet planet;
        do {
            if (!var1.hasNext()) {
                var1 = moons.values().iterator();

                Moon moon;
                do {
                    if (!var1.hasNext()) {
                        return null;
                    }

                    moon = (Moon)var1.next();
                } while(!moon.getTranslationKey().equals(unlocalizedName));

                return moon;
            }

            planet = (Planet)var1.next();
        } while(!planet.getTranslationKey().equals(unlocalizedName));

        return planet;
    }

    public static boolean registerSolarSystem(SolarSystem solarSystem) {
        if (solarSystemIDs.containsKey(solarSystem.getName())) {
            return false;
        } else {
            solarSystems.put(solarSystem.getName(), solarSystem);
            solarSystemIDs.put(solarSystem.getName(), ++maxSolarSystemID);
            MinecraftForge.EVENT_BUS.post(new GalaxyRegistry.SolarSystemRegisterEvent(solarSystem.getName(), maxSolarSystemID));
            return true;
        }
    }

    public static boolean registerPlanet(Planet planet) {
        if (planetIDs.containsKey(planet.getName())) {
            return false;
        } else {
            planets.put(planet.getName(), planet);
            planetIDs.put(planet.getName(), ++maxPlanetID);
            MinecraftForge.EVENT_BUS.post(new GalaxyRegistry.PlanetRegisterEvent(planet.getName(), maxPlanetID));
            return true;
        }
    }

    public static boolean registerMoon(Moon moon) {
        if (moonIDs.containsKey(moon.getName())) {
            return false;
        } else {
            moons.put(moon.getName(), moon);
            moonIDs.put(moon.getName(), ++maxMoonID);
            MinecraftForge.EVENT_BUS.post(new GalaxyRegistry.MoonRegisterEvent(moon.getName(), maxMoonID));
            return true;
        }
    }

    public static boolean registerSatellite(Satellite satellite) {
        if (satelliteIDs.containsKey(satellite.getName())) {
            return false;
        } else if (satellite.getParentPlanet() == null) {
            throw new RuntimeException("Registering satellite without a parent!!!");
        } else {
            satellites.put(satellite.getName(), satellite);
            satelliteIDs.put(satellite.getName(), ++maxSatelliteID);
            MinecraftForge.EVENT_BUS.post(new GalaxyRegistry.SatelliteRegisterEvent(satellite.getName(), maxSatelliteID));
            return true;
        }
    }

    public static Map<String, SolarSystem> getRegisteredSolarSystems() {
        return ImmutableMap.copyOf(solarSystems);
    }

    public static Map<String, Integer> getRegisteredSolarSystemIDs() {
        return ImmutableMap.copyOf(solarSystemIDs);
    }

    public static boolean isClient()
    {
        try {
           Object t = Class.forName("net.minecraft.client.Minecraft");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public static Map<String, Planet> getRegisteredPlanets() {
        TreeMap<String, Planet> planetMap = (TreeMap<String, Planet>) planets.clone();

        if(isClient()) {
            planetMap.clear();
            for(String p: planets.keySet()) {
                if(planets.get(p).getName().equals("venus")) {
                    planetMap.put(p, planets.get(p));
                }
            }
            return ImmutableMap.copyOf(planetMap);
        }
        return ImmutableMap.copyOf(planets);
    }

    public static Map<String, Integer> getRegisteredPlanetIDs() {
        return ImmutableMap.copyOf(planetIDs);
    }

    public static Map<String, Moon> getRegisteredMoons() {
        TreeMap<String, Moon> moonMap = (TreeMap<String, Moon>) moons.clone();

        if(isClient()) {
            moonMap.clear();
            for(String p: moons.keySet()) {
                if(moons.get(p).getName().equals("venus")) {
                    moonMap.put(p, moons.get(p));
                }
            }
            return ImmutableMap.copyOf(moonMap);
        }
        return ImmutableMap.copyOf(moons);
    }

    public static Map<String, Integer> getRegisteredMoonIDs() {
        return ImmutableMap.copyOf(moonIDs);
    }

    public static Map<String, Satellite> getRegisteredSatellites() {
        return ImmutableMap.copyOf(satellites);
    }

    public static Map<String, Integer> getRegisteredSatelliteIDs() {
        return ImmutableMap.copyOf(satelliteIDs);
    }

    public static int getSolarSystemID(String solarSystemName) {
        return solarSystemIDs.get(solarSystemName);
    }

    public static int getPlanetID(String planetName) {
        return planetIDs.get(planetName);
    }

    public static int getMoonID(String moonName) {
        return moonIDs.get(moonName);
    }

    public static int getSatelliteID(String satelliteName) {
        return satelliteIDs.get(satelliteName);
    }

    public static class SatelliteRegisterEvent extends Event {
        public final String satelliteName;
        public final int satelliteID;

        public SatelliteRegisterEvent(String satelliteName, int satelliteID) {
            this.satelliteName = satelliteName;
            this.satelliteID = satelliteID;
        }
    }

    public static class MoonRegisterEvent extends Event {
        public final String moonName;
        public final int moonID;

        public MoonRegisterEvent(String moonName, int moonID) {
            this.moonName = moonName;
            this.moonID = moonID;
        }
    }

    public static class PlanetRegisterEvent extends Event {
        public final String planetName;
        public final int planetID;

        public PlanetRegisterEvent(String planetName, int planetID) {
            this.planetName = planetName;
            this.planetID = planetID;
        }
    }

    public static class SolarSystemRegisterEvent extends Event {
        public final String solarSystemName;
        public final int solarSystemID;

        public SolarSystemRegisterEvent(String solarSystemName, int solarSystemID) {
            this.solarSystemName = solarSystemName;
            this.solarSystemID = solarSystemID;
        }
    }
}
