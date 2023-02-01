package igentuman.galacticresearch.sky.body;

import igentuman.galacticresearch.sky.SkyModel;
import igentuman.galacticresearch.util.WorldUtil;
import net.minecraft.client.Minecraft;

import java.util.Random;

public class Star implements ISkyBody {

    private int x;
    private int y;
    private int size;
    private int color;
    private static Random rand;
    private double s = 0.05;

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

    public int getColor() {
        return color;
    }

    public long time()
    {
        return WorldUtil.getDayTime();
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

    public int getSize()
    {
        return size;
    }
}
