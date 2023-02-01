package igentuman.galacticresearch.network;

import igentuman.galacticresearch.common.entity.EntitySatelliteRocket;
import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import igentuman.galacticresearch.common.tile.TileTelescope;
import io.netty.buffer.ByteBuf;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.network.NetworkUtil;
import micdoodle8.mods.galacticraft.core.network.PacketBase;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
import micdoodle8.mods.galacticraft.planets.mars.entities.EntitySlimeling;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GRPacketSimple extends PacketBase implements Packet<INetHandler> {
    private GRPacketSimple.EnumSimplePacket type;
    private List<Object> data;
    private static Map<EntityPlayerMP, GameType> savedSettings = new HashMap();

    public GRPacketSimple() {
    }

    public GRPacketSimple(GRPacketSimple.EnumSimplePacket packetType, int dimID, Object[] data) {
        this(packetType, dimID, Arrays.asList(data));
    }

    public GRPacketSimple(GRPacketSimple.EnumSimplePacket packetType, World world, Object[] data) {
        this(packetType, GCCoreUtil.getDimensionID(world), Arrays.asList(data));
    }

    public GRPacketSimple(GRPacketSimple.EnumSimplePacket packetType, int dimID, List<Object> data) {
        super(dimID);
        if (packetType.getDecodeClasses().length != data.size()) {
            GalacticraftCore.logger.info("Simple Packet Core found data length different than packet type", new Object[0]);
            (new RuntimeException()).printStackTrace();
        }

        this.type = packetType;
        this.data = data;
    }

    public void encodeInto(ByteBuf buffer) {
        super.encodeInto(buffer);
        buffer.writeInt(this.type.ordinal());

        try {
            NetworkUtil.encodeData(buffer, this.data);
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

    public void decodeInto(ByteBuf buffer) {
        super.decodeInto(buffer);
        this.type = GRPacketSimple.EnumSimplePacket.values()[buffer.readInt()];

        try {
            if (this.type.getDecodeClasses().length > 0) {
                this.data = NetworkUtil.decodeData(this.type.getDecodeClasses(), buffer);
            }

            if (buffer.readableBytes() > 0 && buffer.writerIndex() < 1048320) {
                GalacticraftCore.logger.error("Galacticraft packet length problem for packet type " + this.type.toString(), new Object[0]);
            }

        } catch (Exception var3) {
            System.err.println("[Galacticraft] Error handling simple packet type: " + this.type.toString() + " " + buffer.toString());
            var3.printStackTrace();
            throw var3;
        }
    }

    @Override
    public void handleClientSide(EntityPlayer entityPlayer) {

    }

    public void handleServerSide(EntityPlayer player) {
        EntityPlayerMP playerBase = PlayerUtil.getPlayerBaseServerFromPlayer(player, false);
        if (playerBase != null) {
            TileEntity tileAt = player.world.getTileEntity((BlockPos)this.data.get(0));;
            switch(this.type) {
                case C_OPEN_CUSTOM_GUI:
                    Entity entity = null;
                    int entityID;
                    switch((Integer)this.data.get(1)) {
                        case 0:
                            entityID = (Integer)this.data.get(2);
                            entity = player.world.getEntityByID(entityID);
                            if (entity != null && entity instanceof EntitySlimeling) {
                               // FMLClientHandler.instance().getClient().displayGuiScreen(new GuiSlimelingInventory(player, (EntitySlimeling)entity));
                            }

                            player.openContainer.windowId = (Integer)this.data.get(0);
                            break;
                        case 1:
                            entityID = (Integer)this.data.get(2);
                            entity = player.world.getEntityByID(entityID);
                            if (entity != null && entity instanceof EntitySatelliteRocket) {
                               // FMLClientHandler.instance().getClient().displayGuiScreen(new GuiSatelliteRocket(player.inventory, (EntitySatelliteRocket)entity));
                            }

                            player.openContainer.windowId = (Integer)this.data.get(0);
                    }
                case TELESCOPE_UP_BUTTON:
                    if (tileAt instanceof TileTelescope) {
                        TileTelescope machine = (TileTelescope)tileAt;
                        machine.rotateY(-1);
                    }
                    break;
                case TELESCOPE_DOWN_BUTTON:
                    if (tileAt instanceof TileTelescope) {
                        TileTelescope machine = (TileTelescope)tileAt;
                        machine.rotateY(1);
                    }
                    break;
                case TELESCOPE_LEFT_BUTTON:
                    if (tileAt instanceof TileTelescope) {
                        TileTelescope machine = (TileTelescope)tileAt;
                        machine.rotateX(-1);
                    }
                    break;
                case TELESCOPE_RIGHT_BUTTON:
                    if (tileAt instanceof TileTelescope) {
                        TileTelescope machine = (TileTelescope)tileAt;
                        machine.rotateX(1);
                    }
                    break;
                case TELESCOPE_MULTIPLIER_BUTTON:
                    if (tileAt instanceof TileTelescope) {
                        TileTelescope machine = (TileTelescope)tileAt;
                        machine.changeMultiplier();
                    }
                    break;
                case PREV_MISSION_BUTTON:
                    if (tileAt instanceof TileMissionControlStation) {
                        TileMissionControlStation machine = (TileMissionControlStation)tileAt;
                        machine.prevMission();
                    }
                    break;
                case NEXT_MISSION_BUTTON:
                    if (tileAt instanceof TileMissionControlStation) {
                        TileMissionControlStation machine = (TileMissionControlStation)tileAt;
                        machine.nextMission();
                    }
                    break;
                case ACTIVATE_MISSION_BUTTON:
                    if (tileAt instanceof TileMissionControlStation) {
                        TileMissionControlStation machine = (TileMissionControlStation)tileAt;
                        machine.activateMission();
                    }
                    break;
            }

        }
    }

    public void readPacketData(PacketBuffer var1) {
        this.decodeInto(var1);
    }

    public void writePacketData(PacketBuffer var1) {
        this.encodeInto(var1);
    }

    @Override
    public void processPacket(INetHandler iNetHandler) {

    }


    public static enum EnumSimplePacket {

        PREV_MISSION_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        NEXT_MISSION_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        ACTIVATE_MISSION_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        TELESCOPE_UP_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        TELESCOPE_DOWN_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        TELESCOPE_LEFT_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        TELESCOPE_RIGHT_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        TELESCOPE_MULTIPLIER_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        C_OPEN_CUSTOM_GUI(Side.CLIENT, new Class[]{Integer.class, Integer.class, Integer.class});

        private Side targetSide;
        private Class<?>[] decodeAs;

        private EnumSimplePacket(Side targetSide, Class<?>... decodeAs) {
            this.targetSide = targetSide;
            this.decodeAs = decodeAs;
        }

        public Side getTargetSide() {
            return this.targetSide;
        }

        public Class<?>[] getDecodeClasses() {
            return this.decodeAs;
        }
    }
}