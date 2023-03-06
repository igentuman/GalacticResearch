package igentuman.galacticresearch.network;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.client.gui.GuiLaunchpadTower;
import igentuman.galacticresearch.client.gui.MCS.GuiMissionControlStation;
import igentuman.galacticresearch.client.gui.GuiTelescope;
import igentuman.galacticresearch.client.gui.MCS.GuiMissionControlStationLocator;
import igentuman.galacticresearch.common.container.ContainerLaunchpadTower;
import igentuman.galacticresearch.common.container.ContainerMissionControlStation;
import igentuman.galacticresearch.common.container.ContainerTelescope;
import igentuman.galacticresearch.common.entity.EntitySatelliteRocket;
import igentuman.galacticresearch.common.tile.TileLaunchpadTower;
import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import igentuman.galacticresearch.common.tile.TileTelescope;
import micdoodle8.mods.galacticraft.core.inventory.ContainerRocketInventory;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiProxy implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileLaunchpadTower) {
            return new ContainerLaunchpadTower(player.inventory, (TileLaunchpadTower) te);
        }
        if (te instanceof TileTelescope) {
            ((TileTelescope) te).dimension = player.dimension;
            return new ContainerTelescope(player.inventory, (TileTelescope) te);
        }
        if (te instanceof TileMissionControlStation) {
            ((TileMissionControlStation) te).dimension = player.dimension;
            return new ContainerMissionControlStation(player.inventory, (TileMissionControlStation) te);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileLaunchpadTower) {
            return new GuiLaunchpadTower(player.inventory, (TileLaunchpadTower) te);
        }
        if (te instanceof TileTelescope) {
            ((TileTelescope) te).dimension = player.dimension;
            return new GuiTelescope(player.inventory, (TileTelescope) te);
        }
        if (te instanceof TileMissionControlStation) {
            ((TileMissionControlStation) te).dimension = player.dimension;
            if(id == 2) {
                return new GuiMissionControlStation(player.inventory, (TileMissionControlStation) te);
            }
            if(id == 3) {
                return new GuiMissionControlStationLocator(player.inventory, ((TileMissionControlStation) te).fetchPlayerStations(player));
            }
        }
        return null;
    }

    public static void openSatelliteInterface(EntityPlayerMP player, EntitySatelliteRocket rocket) {
        player.getNextWindowId();
        player.closeContainer();
        int windowId = player.currentWindowId;
        GalacticResearch.packetPipeline.sendTo(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.C_OPEN_CUSTOM_GUI, GCCoreUtil.getDimensionID(player.world), new Object[]{windowId, 1, rocket.getEntityId()}), player);
        player.openContainer = new ContainerRocketInventory(player.inventory, rocket, rocket.rocketType, player);
        player.openContainer.windowId = windowId;
        player.openContainer.addListener(player);
    }
}