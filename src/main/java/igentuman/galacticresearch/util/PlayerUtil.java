package igentuman.galacticresearch.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Predicate;

public class PlayerUtil {

    private static MinecraftServer server()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance();
    }

    public static EntityPlayerMP getPlayerById(String uuid)
    {
        return (EntityPlayerMP) server().getEntityFromUuid(UUID.fromString(uuid));
    }

    public static boolean isOnline(String uuid)
    {
        return Arrays.asList(server().getOnlinePlayerNames()).contains(uuid);
    }
}
