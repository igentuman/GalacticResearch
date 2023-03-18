package igentuman.galacticresearch.util;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.network.GRPacketSimple;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class WorldUtil {

    protected static long ticksPerDay = 1728000;
    private static long clientTimestamp = 0;
    private static long worldTime = 0;

    public static void setWorldTime(long time)
    {
        worldTime = time;
        clientTimestamp = Minecraft.getMinecraft().world.getTotalWorldTime();
    }

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
            if (FMLCommonHandler.instance().getSide().isClient() && GalacticResearch.server == null) {
                return worldTime+(Minecraft.getMinecraft().world.getTotalWorldTime()-clientTimestamp);
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

    public static long getClientDayTime() {
        long time = Minecraft.getMinecraft().world.getTotalWorldTime();
        return (int) (time - (time/ticksPerDay)*ticksPerDay);
    }
}
