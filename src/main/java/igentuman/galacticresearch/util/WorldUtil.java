package igentuman.galacticresearch.util;

import igentuman.galacticresearch.GalacticResearch;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class WorldUtil {

    protected static long ticksPerDay = 1728000;

    public static int getDay()
    {
        return (int) (totalTIme()/ticksPerDay);
    }

    //In ticks
    public static int getDayTime()
    {
        return (int) (totalTIme() - getDay()*ticksPerDay);
    }

    public static long totalTIme()
    {
        try {
            if (FMLCommonHandler.instance().getSide().isClient()) {
                return Minecraft.getMinecraft().world.getTotalWorldTime();
            }
            return GalacticResearch.server.getEntityWorld().getTotalWorldTime();
        } catch (NullPointerException ignore) {
            return 0;
        }
    }

    public static int getMoonPhase()
    {
        return Minecraft.getMinecraft().world.getMoonPhase();
    }
}
