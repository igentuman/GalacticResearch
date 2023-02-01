package igentuman.galacticresearch.sky.body;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.sky.SkyModel;
import igentuman.galacticresearch.sky.SkyItem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class Moon extends Researchable {

    public Moon(SkyItem body)
    {
        super(body);
    }

    int slowdown = 10;

    public boolean isVisible()
    {
        return true;
    }

    public int getX()
    {
        float speed = (float)getBody().getZIndex()+0.001f;
        int sx = (int)(x + (time()*speed)/slowdown);
        int cx = sx / SkyModel.width;
        return sx - cx * SkyModel.width;
    }


    public int getY()
    {
        double sinY = Math.sin((double)getDaytime()/1440000)/5;
        int sy = (int) (y + (time() * body.getZIndex() * Math.abs(sinY))+sinY);
        int c = sy / SkyModel.height;
        return sy - c * SkyModel.height;
    }

    public ResourceLocation getTexture() {
        return new ResourceLocation(GalacticResearch.MODID, "textures/gui/planets/moon_phases.png");
    }

    public int yTexOffset()
    {
        return  Minecraft.getMinecraft().world.getMoonPhase() * getSize();
    }
}
