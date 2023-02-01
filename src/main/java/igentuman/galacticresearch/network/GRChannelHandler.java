package igentuman.galacticresearch.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import java.util.EnumMap;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.network.IPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.FMLOutboundHandler.OutboundTarget;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;

public class GRChannelHandler extends FMLIndexedMessageToMessageCodec<IPacket> {
    private EnumMap<Side, FMLEmbeddedChannel> channels;

    private GRChannelHandler() {
        this.addDiscriminator(0, GRPacketSimple.class);
    }

    public static GRChannelHandler init() {
        GRChannelHandler channelHandler = new GRChannelHandler();
        channelHandler.channels = NetworkRegistry.INSTANCE.newChannel("galacticresearch1", new ChannelHandler[]{channelHandler, new GRPacketHandler()});
        return channelHandler;
    }

    public void encodeInto(ChannelHandlerContext ctx, IPacket msg, ByteBuf target) throws Exception {
        msg.encodeInto(target);
    }

    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, IPacket msg) {
        try {
            msg.decodeInto(source);
        } catch (IndexOutOfBoundsException var5) {
            GalacticraftCore.logger.error("Incomplete Galacticraft entity packet: dimension " + msg.getDimensionID(), new Object[0]);
        }

    }

    public void sendToAll(IPacket message) {
        ((FMLEmbeddedChannel)this.channels.get(Side.SERVER)).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.ALL);
        ((FMLEmbeddedChannel)this.channels.get(Side.SERVER)).writeOutbound(new Object[]{message});
    }

    public void sendTo(IPacket message, EntityPlayerMP player) {
        ((FMLEmbeddedChannel)this.channels.get(Side.SERVER)).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.PLAYER);
        ((FMLEmbeddedChannel)this.channels.get(Side.SERVER)).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        ((FMLEmbeddedChannel)this.channels.get(Side.SERVER)).writeOutbound(new Object[]{message});
    }

    public void sendToAllAround(IPacket message, TargetPoint point) {
        try {
            ((FMLEmbeddedChannel)this.channels.get(Side.SERVER)).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.ALLAROUNDPOINT);
            ((FMLEmbeddedChannel)this.channels.get(Side.SERVER)).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
            ((FMLEmbeddedChannel)this.channels.get(Side.SERVER)).writeOutbound(new Object[]{message});
        } catch (Exception var4) {
            GalacticraftCore.logger.error("Forge error when sending network packet to nearby players - this is not a Galacticraft bug, does another mod make fake players?", new Object[0]);
            var4.printStackTrace();
        }

    }

    public void sendToDimension(IPacket message, int dimensionID) {
        try {
            ((FMLEmbeddedChannel)this.channels.get(Side.SERVER)).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.DIMENSION);
            ((FMLEmbeddedChannel)this.channels.get(Side.SERVER)).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionID);
            ((FMLEmbeddedChannel)this.channels.get(Side.SERVER)).writeOutbound(new Object[]{message});
        } catch (Exception var4) {
            GalacticraftCore.logger.error("Forge error when sending network packet to all players in dimension - this is not a Galacticraft bug, does another mod make fake players?", new Object[0]);
            var4.printStackTrace();
        }

    }

    public void sendToServer(IPacket message) {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            ((FMLEmbeddedChannel)this.channels.get(Side.CLIENT)).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.TOSERVER);
            ((FMLEmbeddedChannel)this.channels.get(Side.CLIENT)).writeOutbound(new Object[]{message});
        }
    }
}
