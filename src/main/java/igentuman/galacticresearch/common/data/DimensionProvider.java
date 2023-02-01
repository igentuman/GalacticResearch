package igentuman.galacticresearch.common.data;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import igentuman.galacticresearch.sky.SkyItem;

import java.util.ArrayList;
import java.util.List;

public class DimensionProvider {

    private TileMissionControlStation te;

    public DimensionProvider(TileMissionControlStation te)
    {
        this.te = te;
    }

    //where satellites can be sent
    public int[] getUnlockedDimensions()
    {
        int[] unlocked = ModConfig.research.unlocked_dimensions;
        String[] bodies = te.getMissions();
        List<Integer> researched = new ArrayList<>();
        for (String b: bodies) {
            for(int dim: getDimensionsByBodyName(b)) {
                researched.add(dim);
            }
        }
        int i = 0;
        int[] dims = new int[unlocked.length+researched.size()];
        for(int d: unlocked) {
            dims[i] = d;
            i++;
        }

        return dims;
    }

    public int[] getDimensionsByBodyName(String name)
    {
        for(SkyItem b: GalacticResearch.skyModel.getBodies()) {
            if(b.getName().equals(name)) {
                return b.getDimensions();
            }
        }
        return new int[0];
    }
}
