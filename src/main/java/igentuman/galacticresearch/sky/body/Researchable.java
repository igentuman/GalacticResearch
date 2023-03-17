package igentuman.galacticresearch.sky.body;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.sky.SkyModel;
import igentuman.galacticresearch.util.WorldUtil;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Random;

public class Researchable implements ISkyBody {

    protected String name;
    protected Researchable parent;
    protected CelestialBody body;
    protected int x;
    protected int y;
    protected float size;
    protected int rarity;
    protected int speed = 0;
    protected ResourceLocation texture;
    protected Random rand;
    protected static HashMap<String, Researchable> cache = new HashMap<>();

    public Researchable(CelestialBody planet) {
        this.size = ModConfig.researchSystem.getSizes().getOrDefault(planet.getName(), 16);
        this.rarity = (int) Math.abs(planet.getRelativeOrbitTime());
        this.name = planet.getName();
        this.x = initialX();
        this.y = initialY();
        this.body = planet;
        if(planet instanceof Moon) {
            this.parent = Researchable.get(((Moon) planet).getParentPlanet().getName());
        }
    }

    public Researchable getParent()
    {
        return parent;
    }

    public static Researchable get(String name)
    {
        if(!cache.keySet().contains(name)) {
            if(GalaxyRegistry.getRegisteredPlanets().get(name) == null) {
                cache.put(name, new Researchable(GalaxyRegistry.getRegisteredMoons().get(name)));
            } else {
                cache.put(name, new Researchable(GalaxyRegistry.getRegisteredPlanets().get(name)));
            }
        }
        return cache.get(name);
    }

    public float guiX(long lastTime, float ticks)
    {
        float sx = (x + (float)time()/speed());
        int cx = (int) (sx / SkyModel.width);
        float xc = sx - cx * SkyModel.width;
        if(isHorizontalReversed()) {
            xc = SkyModel.width - (sx - cx * SkyModel.width);
        }

        sx = (x + (float)lastTime/speed());
        cx = (int) (sx / SkyModel.width);
        float xl = sx - cx * SkyModel.width;
        if(isHorizontalReversed()) {
            xl = SkyModel.width - (sx - cx * SkyModel.width);
        }
        return xl + (xc - xl) * ticks;
    }

    public float guiY(long lastTime, float ticks)
    {
        float sy = (y + (float)time()/speed());
        int cy = (int) (sy / SkyModel.height);
        float yc = sy - cy * SkyModel.height;
        if(isVerticalReversed()) {
            yc = SkyModel.height - (sy - cy * SkyModel.height);
        }

        sy = (y + (float)lastTime/speed());
        cy = (int) (sy / SkyModel.height);
        float yl = sy - cy * SkyModel.height;
        if(isVerticalReversed()) {
            yl = SkyModel.height - (sy - cy * SkyModel.height);
        }
        return yl + (yc - yl) * ticks;
    }

    public boolean isVerticalReversed()
    {
        Random r = new Random((long) (SkyModel.get().seed/2+nameToSeed()+rarity()+WorldUtil.getDay()));
        return r.nextInt(10) < 3;
    }

    public boolean isHorizontalReversed()
    {
        Random r = new Random((long) (SkyModel.get().seed/2+nameToSeed()+rarity()+WorldUtil.getDay()+100));
        return r.nextInt(10) < 4;
    }

    public float getSize() {
        return size;
    }

    private int nameToSeed()
    {
        return Math.abs(name.hashCode());
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

    public boolean isVisible()
    {
        Random r = new Random((long) (SkyModel.get().seed/20+nameToSeed()/1000+rarity()+WorldUtil.getDay()));
        return r.nextInt((int) (10 / (1 / (double)rarity()))) < rarity()*100;
    }

    public int rarity()
    {
        return Math.min(2, Math.abs(rarity));
    }

    public int speed()
    {
        if(speed == 0) {
            Random r = new Random((long) (SkyModel.get().seed/20+nameToSeed()/1000+rarity()));
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
        if(texture == null) {
            if(name.equals("moon")) {
                texture = new ResourceLocation(GalacticResearch.MODID, "textures/gui/planets/"+name+".png");
                return texture;
            }
            if(GalaxyRegistry.getRegisteredPlanets().keySet().contains(name)) {
                texture = GalaxyRegistry.getRegisteredPlanets().get(name).getBodyIcon();
            } else if(GalaxyRegistry.getRegisteredMoons().keySet().contains(name)) {
                texture = GalaxyRegistry.getRegisteredMoons().get(name).getBodyIcon();
            } else {
                texture = new ResourceLocation(GalacticResearch.MODID, "textures/gui/planets/"+name+".png");
            }
        }
        return texture;
    }

    public int yTexOffset()
    {
        return 0;
    }

    public String getName() {
        return name;
    }
}
