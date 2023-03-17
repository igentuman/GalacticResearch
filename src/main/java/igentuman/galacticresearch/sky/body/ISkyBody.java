package igentuman.galacticresearch.sky.body;

import igentuman.galacticresearch.util.WorldUtil;
import net.minecraft.client.Minecraft;

public interface ISkyBody {

    int getX();

    int getY();

    boolean isVisible();

    float getSize();

    default long time()
    {
        return WorldUtil.totalTIme();
    }
}
