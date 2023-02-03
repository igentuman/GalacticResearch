package igentuman.galacticresearch.sky;

import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.sky.body.ISkyBody;
import igentuman.galacticresearch.sky.body.Researchable;
import igentuman.galacticresearch.sky.body.Star;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkyModel {
    public static int width = 1200;
    public static int height = 1200;
    private static int starsDensity = 5000;
    public long seed;
    private Star[] stars;
    private SkyItem currentBody;
    private int curDim;
    List<SkyItem> bodies;
    List<Researchable> researchables;

    private static SkyModel instance;

    private SkyModel()
    {
    }

    public void setSeed(long seed)
    {
        this.seed = seed;
    }

    public static SkyModel get()
    {
        if(instance == null) {
            instance = new SkyModel();
        }
        return instance;
    }

    public void initBodies()
    {
        bodies = ModConfig.researchSystem.getListOfResearchable();
        //TODO add an event
        for (SkyItem body: bodies) {
            body.init();
        }
    }

    public List<Researchable> getResearchables()
    {
        if(researchables == null) {
            researchables = new ArrayList<>();
            for(SkyItem b: getBodies()) {
                researchables.add(new Researchable(b));
            }
        }
        return researchables;
    }

    public SkyItem getBodyByDIM(int dim)
    {
        for(SkyItem b: getBodies()) {
            if(Arrays.stream(b.getDimensions()).anyMatch(v -> v == dim)) {
                return b;
            }
        }
        return null;
    }

    public List<Researchable> getObjectsToResearch(int dim)
    {
        List<Researchable> res = new ArrayList<>();
        for(Researchable r: getResearchables()) {
            SkyItem b = getBodyByDIM(dim);
            if(b == null) return res;
            if(b.getParent().equals(r.getBody().parent) ||
                    b.getName().equals(r.getBody().parent)
            ) {
                if(b.getName().equals(r.getBody().getName())) continue;
                res.add(r);
            }
        }
        return res;
    }

    public Star[] getStars()
    {
        if(stars == null) {
            stars = new Star[starsDensity];
            for (int i = 0; i < starsDensity; i++) {
                stars[i] = Star.generate();
            }
        }
        return stars;
    }

    public List<SkyItem> getBodies()
    {
        if(bodies == null) {
            initBodies();
        }
        return bodies;
    }
}
