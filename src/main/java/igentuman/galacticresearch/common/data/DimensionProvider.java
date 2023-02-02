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
