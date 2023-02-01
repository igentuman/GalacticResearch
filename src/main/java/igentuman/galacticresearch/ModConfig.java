package igentuman.galacticresearch;

import igentuman.galacticresearch.sky.SkyItem;
import net.minecraftforge.common.config.Config;

import java.util.ArrayList;
import java.util.List;

@Config(modid = GalacticResearch.MODID)
public class ModConfig {
    public static ResearchSystemSettings researchSystem= new ResearchSystemSettings();
    public static Research research = new Research();
    public static Machines machines = new Machines();

    public static class ResearchSystemSettings {

        @Config.Name("researchable_bodies")
        @Config.Comment({
                "Define all researchable solar bodies",
                "Format: nameKey, zIndex, rarity,size, dimension ids (separated by ;), parent planet nameKey",
                "(texture for the body location: galacticresearch:textures/gui/planets/nameKey.png)",
                "(translation key for bodies: galacticresearch.planet.nameKey)",
                "parent planet field used to set child planets only observable in dimmension of parent planet (except sun, all planets with parent sun can be observed anywhere)"
        })
        public String[] researchable_bodies = new String[]{
                "mercury, 1, 100, 16, -13, sun",
                "venus, 2, 70, 20, -31, sun",
                "earth, 3, 50, 16, 0, sun",
                "moon, 1, 10, 28, -28, earth",
                "mars, 4, 30, 20, -29, sun",
                "asteroids, 5, 30, 16, -30, sun",
                "jupiter, 6, 30, 32, -15;-1501, sun",
                "saturn, 7, 40, 28, -16, sun",
                "uranus, 8, 70, 16, -17, sun",
                "neptune, 9, 80, 16, -18, sun"
        };

        @Config.Name("default_researched_bodies")
        @Config.Comment({
                "List of body nameKey's which are will be researched by default"
        })
        public String[] default_researched_bodies = new String[]{
            "earth"
        };

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
            List<SkyItem> list = new ArrayList<SkyItem>();
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
    }

    public static class Research {

        @Config.Name("research_mode")
        @Config.Comment({
                "telescope - use telescope only to unlock planets.",
                "satellite - by only sending satellites into Space",
                "combined - by observing planets in telescope and sending satellites after observation"
        })
        public String research_mode = "combined";

        @Config.Name("required_observation_time")
        @Config.Comment({
                "How long (seconds) you need to track and observe body in telescope to collect data "
        })
        public int required_observation_time = 45;

        @Config.Name("enable_telescope_assistant")
        @Config.Comment({
                "Telescope assistant will advise direction of solar bodies in telescope GUI"
        })
        public boolean enable_telescope_assistant = false;

        @Config.Name("unlocked_dimensions")
        @Config.Comment({
                "List of dimension IDs what are unlocked(researched) by default"
        })
        public int[] unlocked_dimensions = new int[]{
                0,
                -27
        };
    }

}
