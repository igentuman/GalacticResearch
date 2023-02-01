package igentuman.galacticresearch.sky;

import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.sky.body.ISkyBody;
import igentuman.galacticresearch.sky.body.Researchable;
import igentuman.galacticresearch.sky.body.Star;

import java.util.ArrayList;
import java.util.List;

public class SkyModel {
    public static int width = 1500;
    public static int height = 1500;
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

    public SkyItem getCurrentBody(int dim)
    {
        if(dim == curDim && currentBody != null) {
            return currentBody;
        }
        curDim = dim;
        for(SkyItem b: getBodies()) {
            for(int d: b.getDimensions()) {
                if(d == dim) {
                    currentBody = b;
                }
            }
        }
        return currentBody;
    }

    public List<Researchable> getObjectsToResearch(int dim)
    {
        List<Researchable> res = new ArrayList<>();
        for(Researchable r: getResearchables()) {
            if(getCurrentBody(dim).getParent().equals(r.getBody().parent) ||
                    getCurrentBody(dim).getName().equals(r.getBody().parent)
            ) {
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
