package igentuman.galacticresearch.sky;

import igentuman.galacticresearch.GalacticResearch;

public class SkyItem {

    //translation key
    protected String name;

    protected int[] dimensions;

    protected int zIndex;

    protected int size;

    protected int rarity;

    protected int initialAngle;

    protected String parent;

    protected double[] initialPosition;

    protected SkyItem parentInstance;

    public SkyItem(
            String name,
            int zIndex,
            int rarity,
            int size,
            int[] dimensions,
            String parent
            ) {
        this.name = name;
        this.dimensions = dimensions;
        this.zIndex = zIndex;
        this.rarity = rarity;
        this.parent = parent;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }


    public void init()
    {
        if(parent != null && !parent.isEmpty()) {
            for (SkyItem body: GalacticResearch.skyModel.bodies) {
                if(body.getName().equals(parent)) {
                    this.parentInstance = body;
                }
            }
        }
    }

    public int getRarity() {
        return rarity;
    }

    public String getName() {
        return name;
    }

    public int[] getDimensions() {
        return dimensions;
    }

    public int getZIndex() {
        return zIndex;
    }

    public String getParent() {
        return parent;
    }

    public SkyItem getParentInstance()
    {
        return parentInstance;
    }


}
