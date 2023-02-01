package igentuman.galacticresearch.network;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.network.GalacticraftPacketHandler;
import micdoodle8.mods.galacticraft.core.network.IPacket;
import micdoodle8.mods.galacticraft.core.tick.TickHandlerClient;
import micdoodle8.mods.galacticraft.core.tick.TickHandlerServer;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Sharable
public class GRPacketHandler extends GalacticraftPacketHandler {
    private final Map<Side, Map<Integer, Queue<GRPacketHandler.PacketPlayerPair>>> packetMap;
    private static volatile int livePacketCount = 0;

    public GRPacketHandler() {
        Map<Side, Map<Integer, Queue<GRPacketHandler.PacketPlayerPair>>> map = Maps.newHashMap();
        Side[] var2 = Side.values();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Side side = var2[var4];
            Map<Integer, Queue<GRPacketHandler.PacketPlayerPair>> sideMap = new ConcurrentHashMap();
            map.put(side, sideMap);
        }

        this.packetMap = ImmutableMap.copyOf(map);
        if (GCCoreUtil.getEffectiveSide() == Side.CLIENT) {
            TickHandlerClient.addPacketHandler(this);
        }

        TickHandlerServer.addPacketHandler(this);
    }

    public void unload(World world) {
        Side side = world.isRemote ? Side.CLIENT : Side.SERVER;
        int dimId = GCCoreUtil.getDimensionID(world);
        Queue<GRPacketHandler.PacketPlayerPair> queue = this.getQueue(side, dimId);
        queue.clear();
    }

    public void tick(World world) {
        Side side = world.isRemote ? Side.CLIENT : Side.SERVER;
        int dimID = GCCoreUtil.getDimensionID(world);
        Queue queue = this.getQueue(side, dimID);

        GRPacketHandler.PacketPlayerPair pair;
        while((pair = (GRPacketHandler.PacketPlayerPair)queue.poll()) != null) {
            switch(side) {
            case CLIENT:
                pair.getPacket().handleClientSide(pair.getPlayer());
                break;
            case SERVER:
                pair.getPacket().handleServerSide(pair.getPlayer());
            }
        }

    }

    protected void channelRead0(ChannelHandlerContext ctx, IPacket msg) throws Exception {
        INetHandler netHandler = (INetHandler)ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        Side side = (Side)ctx.channel().attr(NetworkRegistry.CHANNEL_SOURCE).get();
        EntityPlayer player = GalacticraftCore.proxy.getPlayerFromNetHandler(netHandler);
        if (player != null) {
            if (side != null) {
                this.getQueue(side, msg.getDimensionID()).add(new GRPacketHandler.PacketPlayerPair(msg, player));
                ++livePacketCount;
            }

        }
    }

    private Queue<GRPacketHandler.PacketPlayerPair> getQueue(Side side, int dimID) {
        Map<Integer, Queue<GRPacketHandler.PacketPlayerPair>> map = (Map)this.packetMap.get(side);
        if (!map.containsKey(dimID)) {
            map.put(dimID, Queues.newConcurrentLinkedQueue());
        }

        return (Queue)map.get(dimID);
    }

    private final class PacketPlayerPair {
        private IPacket packet;
        private EntityPlayer player;

        public PacketPlayerPair(IPacket packet, EntityPlayer player) {
            this.packet = packet;
            this.player = player;
        }

        public IPacket getPacket() {
            return this.packet;
        }

        public void setPacket(IPacket packet) {
            this.packet = packet;
        }

        public EntityPlayer getPlayer() {
            return this.player;
        }

        public void setPlayer(EntityPlayer player) {
            this.player = player;
        }
    }
}
