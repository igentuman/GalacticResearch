package igentuman.galacticresearch.util;

import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.client.capability.PlayerClientSpaceData;
import igentuman.galacticresearch.client.capability.SpaceClientCapabilityHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.client.FMLClientHandler;
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

    public static boolean isUnlocked(String name)
    {
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        EntityPlayerSP player = minecraft.player;
        EntityPlayerSP playerBaseClient = micdoodle8.mods.galacticraft.core.util.PlayerUtil.getPlayerBaseClientFromPlayer(player, false);
        PlayerClientSpaceData stats = null;

        if (player != null) {
            stats = playerBaseClient.getCapability(SpaceClientCapabilityHandler.PLAYER_SPACE_CLIENT_DATA, null);
        }
        return stats.getUnlockedMissions().contains(name.toLowerCase()) ||
                Arrays.asList(ModConfig.researchSystem.default_researched_objects).contains(name.toLowerCase());
    }
}
