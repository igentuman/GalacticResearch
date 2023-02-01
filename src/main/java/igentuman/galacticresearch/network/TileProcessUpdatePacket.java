package igentuman.galacticresearch.network;

import igentuman.galacticresearch.GalacticResearch;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class TileProcessUpdatePacket implements IMessage {
    public int kineticEnergy;
    public int energyStored;
    public BlockPos pos;
    public int currentY;
    public boolean activeFlag;
    public boolean isRedstonePowered;


    public TileProcessUpdatePacket() {
    }

    public TileProcessUpdatePacket(BlockPos pos, int kineticEnergy, int currentY, int energyStored, boolean activeFlag, boolean isRedstonePowered) {
        this.pos = pos;
        this.kineticEnergy = kineticEnergy;
        this.currentY = currentY;
        this.energyStored = energyStored;
        this.activeFlag = activeFlag;
        this.isRedstonePowered = isRedstonePowered;
    }

    public void fromBytes(ByteBuf buf) {
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.kineticEnergy = buf.readInt();
        this.currentY = buf.readInt();
        this.energyStored = buf.readInt();
        this.activeFlag = buf.readBoolean();
        this.isRedstonePowered = buf.readBoolean();
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.pos.getX());
        buf.writeInt(this.pos.getY());
        buf.writeInt(this.pos.getZ());
        buf.writeInt(this.kineticEnergy);
        buf.writeInt(this.currentY);
        buf.writeInt(this.energyStored);
        buf.writeBoolean(this.activeFlag);
        buf.writeBoolean(this.isRedstonePowered);
    }

    public static class Handler implements IMessageHandler<TileProcessUpdatePacket, IMessage> {

        @Override
        public IMessage onMessage(TileProcessUpdatePacket message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                GalacticResearch.proxy.handleProcessUpdatePacket(message, ctx);
            });
            return null;
        }
    }
}
