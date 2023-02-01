package igentuman.galacticresearch.sky.body;

import igentuman.galacticresearch.util.WorldUtil;
import net.minecraft.client.Minecraft;

public interface ISkyBody {

    int getX();

    int getY();

    boolean isVisible();

    int getSize();

    default long time()
    {
        if(Minecraft.getMinecraft().world == null) {
            return  0;
        }
        return WorldUtil.totalTIme();
    }

    default int getDaytime()
    {
        int days = (int) (time()/1728000);
        return (int) (time() - days*1728000);
    }

}
