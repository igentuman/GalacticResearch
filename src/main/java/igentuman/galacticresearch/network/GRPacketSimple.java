package igentuman.galacticresearch.network;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.client.capability.PlayerClientSpaceData;
import igentuman.galacticresearch.client.capability.SpaceClientCapabilityHandler;
import igentuman.galacticresearch.common.data.SpaceMineProvider;
import igentuman.galacticresearch.common.entity.EntitySatelliteRocket;
import igentuman.galacticresearch.common.tile.TileLaunchpadTower;
import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import igentuman.galacticresearch.common.tile.TileRocketAssembler;
import igentuman.galacticresearch.common.tile.TileTelescope;
import igentuman.galacticresearch.sky.SkyModel;
import igentuman.galacticresearch.util.Util;
import igentuman.galacticresearch.util.WorldUtil;
import io.netty.buffer.ByteBuf;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.network.NetworkUtil;
import micdoodle8.mods.galacticraft.core.network.PacketBase;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
import micdoodle8.mods.galacticraft.planets.mars.entities.EntitySlimeling;
import net.minecraft.client.entity.EntityPlayerSP;
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
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.*;

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

    @SideOnly(Side.CLIENT)
    @Override
    public void handleClientSide(EntityPlayer entityPlayer) {

        switch (this.type) {
            case SYNC_PLAYER_SPACE_DATA:
                PlayerClientSpaceData stats = null;
                if (entityPlayer instanceof EntityPlayerSP) {
                    stats = entityPlayer.getCapability(SpaceClientCapabilityHandler.PLAYER_SPACE_CLIENT_DATA, null);
                }
                stats.unlocked_missions = (String) data.get(0);
                break;
            case WORLD_TIME:
                WorldUtil.setWorldTime((Long) data.get(0));
                break;
            case SKY_SEED:
                SkyModel.get().setSeed((Long) data.get(0));
                break;
            case SYNC_ASTEROIDS:
                HashMap<String, Integer> missions = Util.unserializeMap((String) data.get(0));
                SpaceMineProvider.get().setMissions(missions);
                break;
        }
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
                case ASSEMBLER_RECIPE:
                    if (tileAt instanceof TileRocketAssembler) {
                        TileRocketAssembler machine = (TileRocketAssembler)tileAt;
                        machine.switchRecipe((Integer)this.data.get(1));
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
                case TOGGLE_AUTOMATIC_MOUNTING:
                    if (tileAt instanceof TileLaunchpadTower) {
                        TileLaunchpadTower machine = (TileLaunchpadTower)tileAt;
                        machine.toggleAutomount();
                    }
                    break;
                case MOUNT_ROCKET:
                    if (tileAt instanceof TileLaunchpadTower) {
                        TileLaunchpadTower machine = (TileLaunchpadTower)tileAt;
                        machine.mount();
                    }
                    break;
                case UNMOUNT_ROCKET:
                    if (tileAt instanceof TileLaunchpadTower) {
                        TileLaunchpadTower machine = (TileLaunchpadTower)tileAt;
                        machine.unmount();
                    }
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
                case ANALYZE_DATA_BUTTON:
                    if (tileAt instanceof TileMissionControlStation) {
                        TileMissionControlStation machine = (TileMissionControlStation)tileAt;
                        machine.playerAnalyzeData(playerBase);
                    }
                    break;
                case MCS_LOCATE_BUTTON:
                    if (tileAt instanceof TileMissionControlStation) {
                        TileMissionControlStation machine = (TileMissionControlStation)tileAt;
                        machine.locate();
                    }
                    break;
                case MCS_SELECT_LOCATABLE:
                    if (tileAt instanceof TileMissionControlStation) {
                        TileMissionControlStation machine = (TileMissionControlStation)tileAt;
                        machine.selectLocatable((Integer) this.data.get(1));
                    }
                    break;
                case EDIT_LOCATOR_CORDS:
                    if (tileAt instanceof TileMissionControlStation) {
                        TileMissionControlStation machine = (TileMissionControlStation)tileAt;
                        machine.setLocationCords((Integer) this.data.get(1), (Integer) this.data.get(2));
                    }
                    break;
                case MCS_SELECT_STATION:
                    if (tileAt instanceof TileMissionControlStation) {
                        TileMissionControlStation machine = (TileMissionControlStation)tileAt;
                        machine.selectStation((Integer) this.data.get(1));
                    }
                    break;
                case OPEN_GUI_LOCATOR:
                    if (tileAt instanceof TileMissionControlStation) {
                        ((TileMissionControlStation) tileAt).fetchPlayerStations(player);
                        player.openGui(GalacticResearch.instance, 3, player.world, tileAt.getPos().getX(), tileAt.getPos().getY(), tileAt.getPos().getZ());
                    }
                    break;
                case OPEN_GUI_MISSIONS:
                    if (tileAt instanceof TileMissionControlStation) {
                        player.openGui(GalacticResearch.instance, 2, player.world, tileAt.getPos().getX(), tileAt.getPos().getY(), tileAt.getPos().getZ());
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
        WORLD_TIME(Side.CLIENT, Long.class),
        SKY_SEED(Side.CLIENT, Long.class),
        SYNC_ASTEROIDS(Side.CLIENT, String.class),
        SYNC_PLAYER_SPACE_DATA(Side.CLIENT, String.class),
        PREV_MISSION_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        UNMOUNT_ROCKET(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        MOUNT_ROCKET(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        TOGGLE_AUTOMATIC_MOUNTING(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        NEXT_MISSION_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        EDIT_LOCATOR_CORDS(Side.SERVER, new Class[]{BlockPos.class, Integer.class, Integer.class}),
        ACTIVATE_MISSION_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        ANALYZE_DATA_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        TELESCOPE_UP_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        MCS_LOCATE_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        TELESCOPE_DOWN_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        ASSEMBLER_RECIPE(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        TELESCOPE_LEFT_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        TELESCOPE_RIGHT_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        TELESCOPE_MULTIPLIER_BUTTON(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        MCS_SELECT_LOCATABLE(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        MCS_SELECT_STATION(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        OPEN_GUI_LOCATOR(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
        OPEN_GUI_MISSIONS(Side.SERVER, new Class[]{BlockPos.class, Integer.class}),
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