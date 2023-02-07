package igentuman.galacticresearch.sky.body;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.sky.SkyItem;
import igentuman.galacticresearch.sky.SkyModel;
import igentuman.galacticresearch.util.WorldUtil;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class Asteroid extends Researchable {

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

    public Asteroid(String name)
    {
        super(new SkyItem(name, 5, 30, 16, new int[] {323200099}, "sun"));
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

    @Override
    public ResourceLocation getTexture() {
        return new ResourceLocation(GalacticResearch.MODID, "textures/gui/planets/minable_asteroids.png");
    }

    public int yTexOffset()
    {
        Random r = new Random(body.getName().hashCode());
        int i = r.nextInt(6);
        return i*16+i;
    }
}
