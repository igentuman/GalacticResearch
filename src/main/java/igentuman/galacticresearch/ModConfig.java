package igentuman.galacticresearch;

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
    public static HashMap<String, Integer> sizes = new HashMap<>();

    public static class ResearchSystemSettings {

        @Config.Name("body_size")
        @Config.Comment({
                "All bodies have size of 16 by default",
                "You can personailze body size here, just type body name key and the size",
                "This only affects telescope"
                       })
        public String[] body_size = new String[]{
     //     name        size
            "venus,      20",
            "moon,       32",
            "mars,       20",
            "jupiter,    32",
            "saturn,     28",
            "enceladus,  18"
        };

        @Config.Name("research_moons_with_parent_planet")
        @Config.Comment({
                "If true, moons will be automatically researched when you research the parent planet"
        })
        public boolean research_moons_with_parent_planet = false;

        @Config.Name("default_researched_objects")
        @Config.Comment({
                "List of objects what are researched by default. You can check object names by running command /research objects",
                "You need to keep at least one planet for solar system, and one solar system, so when you research solar system there will be at least one planet to fly"
        })
        public String[] default_researched_objects = new String[]{
                "galaxy.milky_way",
                "overworld",
                "sol",
                "proxima_b",
                "tauceti_f",
                "centauri_b",
                "barnarda_c",
                "kepler22b",
                "tatooine",
                "crait",
                "aqua",
                "kepler62b",
                "kepler62c",
                "kepler62d",
                "kepler62e",
                "kepler62f",
                "kepler69b",
                "kepler69c",
                "kepler47b",
                "kepler47c",
                "jetraruta",
                "ketherth",
                "moswion",
                "qustroithea",
                "xovis",
                "brajutov",
                "oarilia",
                "speshani16",
                "struronides",
                "trars9"
        };

        @Config.Name("required_observation_time")
        @Config.Comment({
                "How long (seconds) you need to track and observe body in telescope to collect data "
        })
        public int required_observation_time = 40;

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

        protected void parseBodyLine(String line)
        {
            if(line.isEmpty()) return;
            String[] parts = line.split(",");
            String name = parts[0].trim();
            int size = Integer.parseInt(parts[1].trim());
            sizes.put(name,size);
        }

        public HashMap<String, Integer> getSizes()
        {
            if(sizes.size() == 0) {
                for (String line : body_size) {
                    try {
                        parseBodyLine(line);
                    } catch (ArrayIndexOutOfBoundsException | NullPointerException er) {
                        GalacticResearch.instance.logger.log(Level.ERROR, er.getLocalizedMessage());
                    }
                }
            }
            return sizes;
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

        @Config.Name("asteroid_spawn_delay")
        @Config.Comment({
                "Delay before asteroids will start appearing in sky (game days)"
        })
        public int asteroid_spawn_delay = 15;

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
        public int mining_mission_maximal_resources = 30;

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
                put("galacticraftcore:meteoric_iron_raw", 20);
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
