package igentuman.galacticresearch.sky.body;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.client.gui.GuiTelescope;
import igentuman.galacticresearch.sky.SkyModel;
import igentuman.galacticresearch.sky.SkyItem;
import igentuman.galacticresearch.util.WorldUtil;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class Researchable implements ISkyBody {

    protected int x;
    protected int y;
    protected SkyItem body;
    protected int speed = 0;
    protected ResourceLocation texture;

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
        if(texture == null) {
            if(body.getName().equals("moon")) {
                texture = new ResourceLocation(GalacticResearch.MODID, "textures/gui/planets/"+body.getName()+".png");
                return texture;
            }
            if(GalaxyRegistry.getRegisteredPlanets().keySet().contains(body.getName())) {
                texture = GalaxyRegistry.getRegisteredPlanets().get(body.getName()).getBodyIcon();
            } else if(GalaxyRegistry.getRegisteredMoons().keySet().contains(body.getName())) {
                texture = GalaxyRegistry.getRegisteredMoons().get(body.getName()).getBodyIcon();
            } else {
                texture = new ResourceLocation(GalacticResearch.MODID, "textures/gui/planets/"+body.getName()+".png");
            }
        }
        return texture;
    }

    public int yTexOffset()
    {
        return 0;
    }
}
