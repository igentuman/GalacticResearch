package igentuman.galacticresearch.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ModPacketHandler {
    private static int packetId = 0;
    public static SimpleNetworkWrapper instance = null;

    public static void registerMessages(String channelName) {
        instance = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);

        instance.registerMessage(
                TileProcessUpdatePacket.Handler.class,
                TileProcessUpdatePacket.class,
                packetId++,
                Side.CLIENT
        );

    }
}