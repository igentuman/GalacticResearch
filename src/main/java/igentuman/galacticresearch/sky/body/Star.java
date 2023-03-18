package igentuman.galacticresearch.sky.body;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.sky.SkyModel;
import igentuman.galacticresearch.util.WorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class Star implements ISkyBody {

    private int x;
    private int y;
    private int size;
    private int color;
    private static Random rand;
    private double s = 0.05;
    private int speed = 8;
    public static Star generate()
    {
        if(rand == null) {
            rand = new Random(SkyModel.get().seed);
        }
        return new Star(rand.nextInt(SkyModel.width), rand.nextInt(SkyModel.height), rand.nextInt(3)+1,  rand.nextInt(3)+1);
    }

    public Star(int x, int y, int size, int color)
    {
        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
    }

    public float guiX(long lastTime, float ticks)
    {
        float sx = (x + (float)time()/speed);
        int cx = (int) (sx / SkyModel.width);
        float xc = sx - cx * SkyModel.width;

        sx = (x + (float)lastTime/speed);
        cx = (int) (sx / SkyModel.width);
        float xl = sx - cx * SkyModel.width;
        return xl + (xc - xl) * ticks;
    }

    public float guiY(long lastTime, float ticks)
    {
        float sy = (y + (float)time()/speed);
        int cy = (int) (sy / SkyModel.height);
        float yc = sy - cy * SkyModel.height;


        sy = (y + (float)lastTime/speed);
        cy = (int) (sy / SkyModel.height);
        float yl = sy - cy * SkyModel.height;

        return yl + (yc - yl) * ticks;
    }

    public int getColor() {
        return color;
    }

    public long time()
    {
        return WorldUtil.getClientDayTime();
    }


    public int getX()
    {
        int w = (int) ((x + time()*s)/SkyModel.width);
        return (int) (x + time()*s - w*SkyModel.width);
    }

    public int getY()
    {
        double m = 0.5;
        if(Minecraft.getMinecraft().world.isDaytime()) {
            m = 1;
        }
        int w = (int) ((y + time()*s*m)/SkyModel.height);
        return (int) (y + time()*s*m - w*SkyModel.height);
    }

    public boolean isVisible()
    {
        return true;
    }

    public float getSize()
    {
        return size;
    }

    public ResourceLocation getTexture() {
        return new ResourceLocation(GalacticResearch.MODID, "textures/gui/planets/star.png");
    }
}
