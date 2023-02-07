package igentuman.galacticresearch;

import igentuman.galacticresearch.sky.SkyItem;
import net.minecraftforge.common.config.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Config(modid = GalacticResearch.MODID)
public class ModConfig {
    public static ResearchSystemSettings researchSystem= new ResearchSystemSettings();
    public static Machines machines = new Machines();

    public static class ResearchSystemSettings {

        @Config.Name("researchable_bodies")
        @Config.Comment({
                "Define all researchable solar bodies",
                "All other bodies, not defined in the list will be researched by default",
                "Format: nameKey, zIndex, rarity,size, dimension ids (separated by ;), parent planet nameKey",
                "(texture for the body location: galacticresearch:textures/gui/planets/nameKey.png)",
                "(translation key for bodies: galacticresearch.planet.nameKey)",
                "parent planet field used to set child planets only observable in dimmension of parent planet (except sun, all planets with parent sun can be observed anywhere)"
        })
        public String[] researchable_bodies = new String[]{
                "mercury, 1, 100, 16, -13, sun",
                "venus, 2, 70, 20, -31, sun",
                "overworld, 3, 50, 16, 0, sun",
                "moon, 1, 10, 32, -28, overworld",
                "mars, 4, 30, 20, -29, sun",
                "asteroids, 5, 30, 16, -30, sun",
                "jupiter, 6, 30, 32, -15, sun",
                "saturn, 7, 40, 28, -16, sun",
                "uranus, 8, 70, 16, -17, sun",
                "neptune, 9, 80, 16, -18, sun",
                "ceres, 9, 80, 16, -20, sun",
                "neptune, 9, 80, 16, -18, sun",
                "neptune, 9, 80, 16, -18, sun",
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

        protected SkyItem parseBodyLine(String line)
        {
            String[] parts = line.split(",");
            String name = parts[0].trim();
            int zIndex = Integer.parseInt(parts[1].trim());
            int rarity = Integer.parseInt(parts[2].trim());
            int size = Integer.parseInt(parts[3].trim());
            String[] dimensionIds = parts[4].trim().split(";");
            int[] dims = new int[dimensionIds.length];
            int i = 0;
            for (String dim: dimensionIds) {
                dims[i] = Integer.parseInt(dim.trim());
            }
            String parent = "";
            if(parts.length > 5) {
                parent = parts[5].trim();
            }
            return new SkyItem(name, zIndex, rarity, size, dims, parent);
        }

        public List<SkyItem> getListOfResearchable()
        {
            List<SkyItem> list = new ArrayList<>();
            for (String bodyLine: researchable_bodies) {
                try {
                    list.add(parseBodyLine(bodyLine));
                } catch (NullPointerException ignore) {

                }
            }
            return list;
        }
    }

    public static class Machines {
        @Config.Name("satellite_rocket_schematic_id")
        public int satellite_rocket_schematic_id = 9261;

        @Config.Name("satellite_mission_duration")
        @Config.Comment({
                "How long it takes to unlock planets (seconds)"
        })
        public int satellite_mission_duration = 240;

        @Config.Name("mining_rocket_schematic_id")
        public int mining_rocket_schematic_id = 9262;

        @Config.Name("mining_speed")
        @Config.Comment({
                "Ticks to mine one block on asteroid"
        })
        public int mining_speed = 10;

        @Config.Name("mining_mission_minimal_resources")
        @Config.Comment({
                "In stacks"
        })
        public int mining_mission_minimal_resources = 20;

        @Config.Name("mining_mission_maximal_resources")
        @Config.Comment({
                "In stacks (limit 54 as rocket has 54 slots)"
        })
        public int mining_mission_maximal_resources = 45;

        @Config.Name("announce_asteroids")
        @Config.Comment({
                "Will post messages in global chat about new asteroids"
        })
        public boolean announce_asteroids = true;

        @Config.Name("mining_asteroids_popularity")
        @Config.Comment({
                "Bigger value means more often appearance of asteroids on sky"
        })
        public int mining_asteroids_popularity = 50;

        @Config.Name("mining_missions_limit")
        @Config.Comment({
                "Limit for pending missions at the same time",
                "Means if new asteroid will appear and there already 5 asteroids waiting, it will delete the oldest one",
                "Currently mined asteroids won't be deleted"
        })
        public int mining_missions_limit = 5;

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
            }
        };
    }


}
