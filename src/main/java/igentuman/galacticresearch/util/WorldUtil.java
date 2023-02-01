package igentuman.galacticresearch.util;

import igentuman.galacticresearch.GalacticResearch;

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
        return GalacticResearch.server.getEntityWorld().getTotalWorldTime();
    }
}
