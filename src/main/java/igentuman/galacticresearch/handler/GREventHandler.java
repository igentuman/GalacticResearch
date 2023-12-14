package igentuman.galacticresearch.handler;

import com.mjr.mjrlegendslib.item.BasicItem;
import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.ai.task.EntityAIPlayerNoFly;
import igentuman.galacticresearch.ai.task.EntityAIPyrokinetic;
import igentuman.galacticresearch.ai.task.EntityAISpawnMinions;
import igentuman.galacticresearch.ai.task.EntityAISpawnWeb;
import igentuman.galacticresearch.client.capability.SpaceClientCapabilityHandler;
import igentuman.galacticresearch.client.capability.SpaceClientDataProvider;
import igentuman.galacticresearch.client.gui.GRGuiCelestialSelection;
import igentuman.galacticresearch.client.gui.GuiTelescope;
import igentuman.galacticresearch.common.capability.PlayerSpaceData;
import igentuman.galacticresearch.common.capability.SpaceCapabilityHandler;
import igentuman.galacticresearch.common.capability.SpaceDataProvider;
import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import igentuman.galacticresearch.network.GRPacketSimple;
import igentuman.galacticresearch.util.Util;
import micdoodle8.mods.galacticraft.api.tile.IFuelDock;
import micdoodle8.mods.galacticraft.api.tile.ILandingPadAttachable;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import micdoodle8.mods.galacticraft.core.entities.EntityBossBase;
import micdoodle8.mods.galacticraft.core.event.EventLandingPadRemoval;
import micdoodle8.mods.galacticraft.core.items.ItemBasic;
import micdoodle8.mods.galacticraft.core.tile.TileEntityDish;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.planets.mars.entities.EntityCreeperBoss;
import micdoodle8.mods.galacticraft.planets.venus.entities.EntitySpiderQueen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
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
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        Entity entity = event.getEntity();
        if(ModConfig.tweaks.hard_boss_fight) {
            if (entity instanceof EntityBossBase) {
                ((EntityBossBase) entity).tasks.addTask(1, new EntityAIPlayerNoFly((EntityLiving) entity, EntityPlayer.class, 8.0F));
                ((EntityBossBase) entity).tasks.addTask(1, new EntityAISpawnMinions((EntityLiving) entity, EntityPlayer.class, 12.0F));
            }
            if (entity instanceof EntityCreeperBoss) {
                ((EntityBossBase) entity).tasks.addTask(1, new EntityAIPyrokinetic((EntityLiving) entity, EntityPlayer.class, 12.0F));
            }
            if (entity instanceof EntitySpiderQueen) {
                ((EntityBossBase) entity).tasks.addTask(1, new EntityAISpawnWeb((EntityLiving) entity, EntityPlayer.class, 12.0F));
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        try {
            if (event.phase != TickEvent.Phase.END) return;
            GuiTelescope.lastTickWTime = Minecraft.getMinecraft().world.getTotalWorldTime();
            GuiTelescope.lastXangle = 0;
            GuiTelescope.lastYangle = 0;
        } catch (NullPointerException ignored ) { }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void renderOverlay(GuiScreenEvent.DrawScreenEvent event) {
        GuiTelescope.ticks = event.getRenderPartialTicks();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onGuiOpenEvent(GuiOpenEvent event) {

        if(     event.getGui() == null ||
                event.getGui().getClass().getName().equals("com.mjr.extraplanets.client.gui.screen.CustomCelestialSelection") ||
                event.getGui().getClass().getName().equals("asmodeuscore.core.astronomy.gui.screen.NewGuiCelestialSelection") ||
                event.getGui().getClass().getName().equals("igentuman.galacticresearch.client.gui.GRGuiCelestialSelectionExtraPlanets") ||
                event.getGui().getClass().getName().equals("igentuman.galacticresearch.client.gui.GRGuiCelestialSelectionGalaxySpace")
            ) {
            return;
        }
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
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void attachSpaceDataCapabilityClient(AttachCapabilitiesEvent<Entity> event) {
         if(event.getObject() instanceof EntityPlayerSP) {
            event.addCapability(SpaceClientCapabilityHandler.PLAYER_SPACE_DATA_CLIENT_NAME, new SpaceClientDataProvider((EntityPlayerSP) event.getObject()));
        }
    }

    @SubscribeEvent
    public void playerRbmBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if(stack.getItem() instanceof ItemBasic) {
            if(stack.getItemDamage() == 19) {
               TileEntity te = event.getEntityPlayer().world.getTileEntity(event.getPos());
               if(te instanceof TileEntityDish) {
                   if (stack.getTagCompound() == null) {
                       stack.setTagCompound(new NBTTagCompound());
                   }
                   stack.getTagCompound().setIntArray("teledishPos", new int[]{event.getPos().getX(), event.getPos().getY(), event.getPos().getZ()});
                   if(!event.getEntityPlayer().world.isRemote) {
                       event.getEntityPlayer().sendMessage(new TextComponentString(GCCoreUtil.translate("message.teledish_assigned")));
                   }
               }
            }
        }
    }


    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        GalacticResearch.packetPipeline.sendTo(
                new GRPacketSimple(
                        GRPacketSimple.EnumSimplePacket.SKY_SEED,
                        GCCoreUtil.getDimensionID(event.player.world),
                        new Object[] { GalacticResearch.skyModel.seed }),
                (EntityPlayerMP) event.player
        );
        GalacticResearch.instance.logger.error("Sky seed: " + GalacticResearch.skyModel.seed);
        GalacticResearch.spaceMineProvider.syncToPlayer((EntityPlayerMP) event.player);
        GalacticResearch.instance.logger.error("Asteoids: " + Util.serializeMap(GalacticResearch.spaceMineProvider.getMissions()));
        PlayerSpaceData stats = event.player.getCapability(SpaceCapabilityHandler.PLAYER_SPACE_DATA, null);
        for(String m: ModConfig.researchSystem.default_researched_objects) {
            assert stats != null;
            stats.addMission(m);
        }
        assert stats != null;
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
                for (String m : ModConfig.researchSystem.default_researched_objects) {
                    assert stats != null;
                    stats.addMission(m);
                }
                assert stats != null;
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
    public long tickSent = 0;
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        GalacticResearch.spaceMineProvider.updateMissions();
        if(GalacticResearch.server != null) {
            if(tickSent == GalacticResearch.server.getEntityWorld().getTotalWorldTime()) return;
            tickSent = GalacticResearch.server.getEntityWorld().getTotalWorldTime();
            GalacticResearch.packetPipeline.sendToAll(
                    new GRPacketSimple(
                            GRPacketSimple.EnumSimplePacket.WORLD_TIME,
                            0,
                            new Object[]{tickSent})
            );
        }
    }
}
