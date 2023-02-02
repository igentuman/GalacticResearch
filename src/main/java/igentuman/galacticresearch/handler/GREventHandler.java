package igentuman.galacticresearch.handler;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.client.capability.SpaceClientCapabilityHandler;
import igentuman.galacticresearch.client.capability.SpaceClientDataProvider;
import igentuman.galacticresearch.client.gui.GRGuiCelestialSelection;
import igentuman.galacticresearch.common.capability.PlayerSpaceData;
import igentuman.galacticresearch.common.capability.SpaceCapabilityHandler;
import igentuman.galacticresearch.common.capability.SpaceDataProvider;
import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import igentuman.galacticresearch.network.GRPacketSimple;
import micdoodle8.mods.galacticraft.api.tile.IFuelDock;
import micdoodle8.mods.galacticraft.api.tile.ILandingPadAttachable;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import micdoodle8.mods.galacticraft.core.event.EventLandingPadRemoval;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GREventHandler
{
    @SubscribeEvent
    public void entityLivingEvent(LivingEvent.LivingUpdateEvent event)
    {
        final EntityLivingBase entityLiving = event.getEntityLiving();
        if (entityLiving instanceof EntityPlayerMP)
        {
            GalacticResearch.pHandler.onPlayerUpdate((EntityPlayerMP) entityLiving);
            return;
        }
    }

    @SubscribeEvent
    public void onLandingPadRemoved(EventLandingPadRemoval event)
    {
        TileEntity tile = event.world.getTileEntity(event.pos);

        if (tile instanceof IFuelDock)
        {
            IFuelDock dock = (IFuelDock) tile;
            for (ILandingPadAttachable connectedTile : dock.getConnectedTiles())
            {
                if (connectedTile instanceof TileMissionControlStation)
                {
                    event.allow = false;
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onGuiOpenEvent(GuiOpenEvent event) {
        if (((event.getGui() instanceof GuiCelestialSelection))) {
            if (GameSettings.isKeyDown(micdoodle8.mods.galacticraft.core.tick.KeyHandlerClient.galaxyMap)) {
                event.setGui(new GRGuiCelestialSelection(true, ((GuiCelestialSelection) event.getGui()).possibleBodies, ((GuiCelestialSelection) event.getGui()).canCreateStations));
            } else {
                event.setGui(new GRGuiCelestialSelection(false, ((GuiCelestialSelection) event.getGui()).possibleBodies, ((GuiCelestialSelection) event.getGui()).canCreateStations));
            }
        }
    }

    @SubscribeEvent
    public void attachSpaceDataCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayerMP) {
            event.addCapability(SpaceCapabilityHandler.PLAYER_SPACE_DATA_NAME, new SpaceDataProvider((EntityPlayerMP) event.getObject()));
        } else if(event.getObject() instanceof EntityPlayerSP) {
            event.addCapability(SpaceClientCapabilityHandler.PLAYER_SPACE_DATA_CLIENT_NAME, new SpaceClientDataProvider((EntityPlayerSP) event.getObject()));
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerSpaceData stats = event.player.getCapability(SpaceCapabilityHandler.PLAYER_SPACE_DATA, null);
        for(String m: ModConfig.researchSystem.default_researched_bodies) {
            stats.addMission(m);
        }
        GalacticResearch.packetPipeline.sendTo(
                new GRPacketSimple(
                        GRPacketSimple.EnumSimplePacket.SYNC_PLAYER_SPACE_DATA,
                        GCCoreUtil.getDimensionID(event.player.world),
                        new Object[] { stats.unlocked_missions }),
                (EntityPlayerMP) event.player
        );

    }

    @SubscribeEvent
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayerMP) {
            final EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
            int tick = player.ticksExisted - 1;
            if (tick % 30 == 0) {
                PlayerSpaceData stats = player.getCapability(SpaceCapabilityHandler.PLAYER_SPACE_DATA, null);
                for (String m : ModConfig.researchSystem.default_researched_bodies) {
                    stats.addMission(m);
                }
                GalacticResearch.packetPipeline.sendTo(
                        new GRPacketSimple(
                                GRPacketSimple.EnumSimplePacket.SYNC_PLAYER_SPACE_DATA,
                                GCCoreUtil.getDimensionID(player.world),
                                new Object[]{stats.unlocked_missions}),
                        player
                );
            }
        }
    }

    /*@SubscribeEvent
    public void schematicUnlocked(Unlock event)
    {
        GCPlayerStats stats = GCPlayerStats.get(event.player);

        if (!stats.getUnlockedSchematics().contains(event.page))
        {
            stats.getUnlockedSchematics().add(event.page);
            Collections.sort(stats.getUnlockedSchematics());

            if (event.player != null && event.player.connection != null)
            {
                Integer[] iArray = new Integer[stats.getUnlockedSchematics().size()];

                for (int i = 0; i < iArray.length; i++)
                {
                    ISchematicPage page = stats.getUnlockedSchematics().get(i);
                    iArray[i] = page == null ? -2 : page.getPageID();
                }

                List<Object> objList = new ArrayList<Object>();
                objList.add(iArray);

                GalacticraftCore.packetPipeline.sendTo(new PacketSimple(EnumSimplePacket.C_UPDATE_SCHEMATIC_LIST, GCCoreUtil.getDimensionID(event.player.world), objList), event.player);
            }
        }
    }*/


}
