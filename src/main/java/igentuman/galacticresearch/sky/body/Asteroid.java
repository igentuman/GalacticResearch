package igentuman.galacticresearch.sky.body;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.sky.SkyModel;
import igentuman.galacticresearch.util.WorldUtil;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class Asteroid extends Researchable {

    public boolean isVerticalReversed()
    {
        Random r = new Random((long) (SkyModel.get().seed+nameToSeed()+rarity+WorldUtil.getDay()));
        return r.nextInt(10) < 3;
    }

    public boolean isHorizontalReversed()
    {
        Random r = new Random((long) (SkyModel.get().seed+nameToSeed()+rarity+WorldUtil.getDay()+100));
        return r.nextInt(10) < 4;
    }

    public float getSize() {
        return 16;
    }

    private int nameToSeed()
    {
       return body.getName().hashCode();
    }

    private int initialX()
    {
        long seed = SkyModel.get().seed + nameToSeed();
        return new Random(seed).nextInt(SkyModel.width);
    }

    private int initialY()
    {
        long seed = (SkyModel.get().seed + nameToSeed())/2;
        return new Random(seed).nextInt(SkyModel.height);
    }

    public Asteroid(String name)
    {
        super(new Planet(name).setRelativeSize(1).setRelativeOrbitTime(20));
        this.x = initialX();
        this.y = initialY();
    }

    public boolean isVisible()
    {
        Random r = new Random((long) (SkyModel.get().seed+nameToSeed()+rarity+ WorldUtil.getDay()));
        return r.nextInt((int) (10 / (1 / (double)rarity))) < rarity*100;
    }

    public int speed()
    {
        if(speed == 0) {
            Random r = new Random((long) (SkyModel.get().seed+nameToSeed()+rarity));
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
