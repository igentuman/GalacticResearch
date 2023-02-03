package igentuman.galacticresearch.sky.body;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.sky.SkyModel;
import igentuman.galacticresearch.sky.SkyItem;
import igentuman.galacticresearch.util.WorldUtil;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class Researchable implements ISkyBody {

    protected int x;
    protected int y;
    protected SkyItem body;
    protected int speed = 0;

    public boolean isVerticalReversed()
    {
        Random r = new Random(SkyModel.get().seed+nameToSeed()+body.getRarity()+WorldUtil.getDay());
        return r.nextInt(10) < 3;
    }

    public boolean isHorizontalReversed()
    {
        Random r = new Random(SkyModel.get().seed+nameToSeed()+body.getRarity()+WorldUtil.getDay()+100);
        return r.nextInt(10) < 4;
    }

    public int getSize() {
        return body.getSize();
    }

    public SkyItem getBody() {
        return body;
    }

    private int nameToSeed()
    {
       return body.getName().hashCode();
    }

    private int initialX()
    {
        long seed = SkyModel.get().seed + (long) body.getZIndex() + nameToSeed();
        return new Random(seed).nextInt(SkyModel.width);
    }

    private int initialY()
    {
        long seed = SkyModel.get().seed + (long) body.getZIndex()/2 + nameToSeed();
        return new Random(seed).nextInt(SkyModel.height);
    }

    public Researchable(SkyItem body)
    {
        this.body = body;
        this.x = initialX();
        this.y = initialY();
    }

    public boolean isVisible()
    {
        Random r = new Random(SkyModel.get().seed+nameToSeed()+body.getRarity()+ WorldUtil.getDay());
        return r.nextInt((int) (10 / (1 / (double)body.getRarity()))) < body.getRarity()*100;
    }

    public int speed()
    {
        if(speed == 0) {
            Random r = new Random(SkyModel.get().seed+nameToSeed()+body.getRarity());
            speed = r.nextInt(20);
        }
        return Math.max(5, speed);
    }

    public int getX()
    {
        int sx = (int)(x + time()/speed());
        int cx = sx / SkyModel.width;
        if(isHorizontalReversed()) {
            return SkyModel.width - (sx - cx * SkyModel.width);
        }
        return sx - cx * SkyModel.width;
    }

    public int getY()
    {
        int sy = (int)(y + time()/speed());
        int cy = sy / SkyModel.height;
        if(isVerticalReversed()) {
            return SkyModel.height - (sy - cy * SkyModel.height);
        }
        return sy - cy * SkyModel.height;
    }

    public ResourceLocation getTexture() {
        return new ResourceLocation(GalacticResearch.MODID, "textures/gui/planets/"+body.getName()+".png");
    }

    public int yTexOffset()
    {
        return 0;
    }
}
