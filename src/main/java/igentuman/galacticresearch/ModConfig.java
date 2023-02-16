package igentuman.galacticresearch;

import igentuman.galacticresearch.sky.SkyItem;
import igentuman.galacticresearch.util.GRHooks;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import net.minecraftforge.common.config.Config;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Config(modid = GalacticResearch.MODID)
public class ModConfig {
    public static ResearchSystemSettings researchSystem= new ResearchSystemSettings();
    public static Machines machines = new Machines();
    public static Tweaks tweaks = new Tweaks();
    public static Locator locator = new Locator();

    public static class ResearchSystemSettings {

        @Config.Name("researchable_bodies")
        @Config.Comment({
                "Define all researchable solar bodies",
                "All other bodies, not defined in the list will be researched by default",
                "Format: nameKey, zIndex, rarity,size, dimension ids (separated by ;), parent planet nameKey",
                "(texture for the body location: galacticresearch:textures/gui/planets/nameKey.png)",
                "(translation key for bodies: planet.nameKey)",
                "parent planet field used to set child planets only observable in dimmension of parent planet (except sun, all planets with parent sun can be observed anywhere)"
        })
        public String[] researchable_bodies = new String[]{
     //     name        zindex  rarity  size    dim        parent
            "mercury,   1,      100,    16,     -13,        sun",
            "venus,     2,      70,     20,     -31;5,      sun",
            "overworld, 3,      50,     16,      0;3,       sun",
            "moon,      1,      10,     32,     -28,        overworld",
            "mars,      4,      30,     20,     -29;4,      sun",
            "phobos,    1,      15,     16,     -1012,      mars",
            "deimos,    2,      15,     16,     -1013,      mars",
            "asteroids, 5,      30,     16,     -30,        sun",
            "ceres,     6,      35,     16,     -1007,      sun",
            "jupiter,   7,      30,     32,     -15;-1501,  sun",
            "io,        1,      30,     16,     -1014,      jupiter",
            "europa,    2,      30,     16,     -1015,      jupiter",
            "ganymede,  3,      30,     16,     -1016,      jupiter",
            "callisto,  4,      30,     16,     -1022,      jupiter",
            "saturn,    8,      40,     28,     -16,        sun",
            "enceladus, 2,      40,     18,     -1017,      saturn",
            "titan,     6,      40,     15,     -1018,      saturn",
            "uranus,    9,      70,     16,     -17,        sun",
            "miranda,   1,      65,     16,     -1024,      uranus",
            "oberon,    5,      40,     16,     -1019,      uranus",
            "neptune,   10,     60,     16,     -18,        sun",
            "proteus,   1,      55,     16,     -1020,      neptune",
            "triton,    2,      55,     16,     -1018,      neptune",
            "pluto,     11,     120,    16,     -1008,      sun",
            "kuiperbelt, 12,    50,     16,     -1009,      sun",
            "haumea,    13,     160,    16,     -1023,      sun"
        };



        @Config.Name("default_researched_bodies")
        @Config.Comment({
                "List of body nameKey's which are will be researched by default"
        })
        public String[] default_researched_bodies = new String[]{
                "overworld"
        };

        @Config.Name("required_observation_time")
        @Config.Comment({
                "How long (seconds) you need to track and observe body in telescope to collect data "
        })
        public int required_observation_time = 45;

        @Config.Name("extraplanets_intergration")
        @Config.Comment({
                "Disable in case of conflicts"
        })
        public boolean extraplanets_intergration = true;

        @Config.Name("galaxy_space_integration")
        @Config.Comment({
                "Disable in case of conflicts"
        })
        public boolean galaxy_space_integration = true;

        protected SkyItem parseBodyLine(String line)
        {
            if(line.isEmpty()) return null;
            String[] parts = line.split(",");
            String name = parts[0].trim();
            int zIndex = Integer.parseInt(parts[1].trim());
            int rarity = Integer.parseInt(parts[2].trim());
            int size = Integer.parseInt(parts[3].trim());
            String[] dimensionIds = parts[4].trim().split(";");
            int[] dims = new int[dimensionIds.length];
            int i = 0;
            for (String dim : dimensionIds) {
                dims[i] = Integer.parseInt(dim.trim());
            }
            String parent = "";
            if (parts.length > 5) {
                parent = parts[5].trim();
            }
            return new SkyItem(name, zIndex, rarity, size, dims, parent);
        }

        public boolean isRegistered(String name)
        {
            return GalaxyRegistry.getRegisteredPlanets().keySet().contains(name) ||
                    GalaxyRegistry.getRegisteredMoons().keySet().contains(name);

        }

        public List<SkyItem> getListOfResearchable()
        {
            List<SkyItem> list = new ArrayList<>();
            for (String bodyLine: researchable_bodies) {
                try {
                    SkyItem item = parseBodyLine(bodyLine.trim());
                    if(item != null) {
                        if(isRegistered(item.getName())) { //add only registered bodies
                            list.add(item);
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException|NullPointerException er) {
                    GalacticResearch.instance.logger.log(Level.ERROR, er.getLocalizedMessage());
                }
            }
            return list;
        }
    }

    public static class Tweaks {

        @Config.Name("hard_boss_fight")
        @Config.Comment({
                "More challenge - more fun"
        })
        public boolean hard_boss_fight = true;
    }

    public static class Locator {
        @Config.Name("location_duration")
        @Config.Comment({
                "How long it takes to locate objects (ticks)"
        })
        public int location_duration = 240;

        @Config.Name("radius")
        @Config.Comment({
                "Locator will search objects in defined radius around offset coordinates"
        })
        public int radius = 1000;

        @Config.Name("locatable_objects")
        @Config.Comment({
                "list of objects possible to locate",
                "You can define custom structure name here, And maybe it will work :)"
        })
        public String[] locatable_objects = new String[] {
                "village",
                "boss_dungeon",
                "mansion",
                "monument",
                "temple",
                "ae2_meteorite",
                "ie_deposit"
        };

        public String[] getLocatableObjects()
        {
            List<String> tmp = new ArrayList<>();
            for (String l: locatable_objects) {
                if(l.equals("ae2_meteorite") && !GalacticResearch.hooks.AE2Loaded) {
                    continue;
                }
                if(l.equals("ie_deposit") && !GalacticResearch.hooks.IELoaded) {
                    continue;
                }
                tmp.add(l);
            }
            return tmp.stream().toArray(String[]::new);
        }
    }

    public static class Machines {
        @Config.Name("satellite_rocket_schematic_id")
        public int satellite_rocket_schematic_id = 9261;

        @Config.Name("satellite_mission_duration")
        @Config.Comment({
                "How long it takes to unlock planets (seconds)"
        })
        public int satellite_mission_duration = 300;

        @Config.Name("mining_rocket_schematic_id")
        public int mining_rocket_schematic_id = 9262;

        @Config.Name("mining_speed")
        @Config.Comment({
                "Ticks to mine one block on asteroid"
        })
        public int mining_speed = 15;

        @Config.Name("mining_mission_minimal_resources")
        @Config.Comment({
                "In stacks"
        })
        public int mining_mission_minimal_resources = 10;

        @Config.Name("mining_mission_maximal_resources")
        @Config.Comment({
                "In stacks (max 50)"
        })
        public int mining_mission_maximal_resources = 35;

        @Config.Name("announce_asteroids")
        @Config.Comment({
                "Will post messages in global chat about new asteroids"
        })
        public boolean announce_asteroids = true;

        @Config.Name("mining_asteroids_popularity")
        @Config.Comment({
                "Bigger value means more often appearance of asteroids on sky"
        })
        public int mining_asteroids_popularity = 20;

        @Config.Name("mining_missions_limit")
        @Config.Comment({
                "Limit for pending missions at the same time",
                "Means if new asteroid will appear and there already 5 asteroids waiting, it will delete the oldest one",
                "Currently mined asteroids won't be deleted"
        })
        public int mining_missions_limit = 5;

        @Config.Name("mining_mission_success_rate")
        @Config.Comment({
                "Value range 0-100 (%)"
        })
        public int mining_mission_success_rate = 90;

        @Config.Name("mineable_resources")
        @Config.Comment({
                "Define minable resource and priority",
                "Format: I:\"modname:blockname\"=123"
        })
        public HashMap<String, Integer> mineable_resources = new HashMap<String, Integer>() {
            {
                put("minecraft:diamond_ore", 30);
                put("minecraft:gold_ore", 10);
                put("galacticraftcore:fallen_meteor", 30);
                put("minecraft:emerald_ore", 5);
                put("galacticraftplanets:asteroids_block:4", 30);
                put("galacticraftplanets:asteroids_block:3", 10);
                put("galacticraftplanets:asteroids_block:5", 10);
                put("galacticraftplanets:mars:2", 20);
                put("galacticraftcore:basic_block_moon:2", 20);
                put("galacticraftcore:basic_block_moon:1", 10);
                put("galacticraftcore:basic_block_moon", 10);
                put("galacticraftcore:basic_block_moon:6", 20);
                put("galacticraftplanets:venus:10", 10);
                put("galacticraftplanets:asteroids_block:2", 1);
                put("galacticraftplanets:dense_ice", 1);

            }
        };
    }


}
