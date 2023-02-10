package igentuman.galacticresearch.handler;

import igentuman.galacticresearch.ModConfig;
import micdoodle8.mods.galacticraft.api.recipe.ISchematicPage;
import micdoodle8.mods.galacticraft.api.recipe.SchematicRegistry;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.entities.player.*;
import micdoodle8.mods.galacticraft.core.network.PacketSimple;
import micdoodle8.mods.galacticraft.core.network.PacketSimple.EnumSimplePacket;
import micdoodle8.mods.galacticraft.core.util.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class GRPlayerHandler
{


    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event)
    {
        if (event.player instanceof EntityPlayerMP)
        {
            this.onPlayerLogin((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerCloned(PlayerEvent.Clone event)
    {
        GCPlayerStats oldStats = GCPlayerStats.get(event.getOriginal());
        GCPlayerStats newStats = GCPlayerStats.get(event.getEntityPlayer());
        newStats.copyFrom(oldStats, !event.isWasDeath() || event.getOriginal().world.getGameRules().getBoolean("keepInventory"));
    }

    @SubscribeEvent
    public void onAttachCapability(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof EntityPlayerMP)
        {
            //event.addCapability(GCCapabilities.GC_PLAYER_PROP, new CapabilityProviderStats((EntityPlayerMP) event.getObject()));
        } else if (event.getObject() instanceof EntityPlayer && ((EntityPlayer) event.getObject()).world.isRemote)
        {
            this.onAttachCapabilityClient(event);
        }
    }

    @SideOnly(Side.CLIENT)
    private void onAttachCapabilityClient(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof EntityPlayerSP)
        {
            //event.addCapability(GCCapabilities.GC_PLAYER_CLIENT_PROP, new CapabilityProviderStatsClient((EntityPlayerSP) event.getObject()));
        }
    }

    private void onPlayerLogin(EntityPlayerMP player)
    {
       GCPlayerStats stats = GCPlayerStats.get(player);

        GalacticraftCore.packetPipeline.sendTo(new PacketSimple(EnumSimplePacket.C_GET_CELESTIAL_BODY_LIST, GCCoreUtil.getDimensionID(player.world), new Object[]
        {}), player);
        int repeatCount = stats.getBuildFlags() >> 9;
        if (repeatCount < 3)
        {
            stats.setBuildFlags(stats.getBuildFlags() & 1536);
        }
        GalacticraftCore.packetPipeline.sendTo(new PacketSimple(EnumSimplePacket.C_UPDATE_STATS, GCCoreUtil.getDimensionID(player.world), stats.getMiscNetworkedStats()), player);
        ColorUtil.sendUpdatedColorsToPlayer(stats);
    }

    protected void updateSchematics(EntityPlayerMP player, GCPlayerStats stats)
    {
        SchematicRegistry.addUnlockedPage(player, SchematicRegistry.getMatchingRecipeForID(ModConfig.machines.satellite_rocket_schematic_id));

        Collections.sort(stats.getUnlockedSchematics());

        if (player.connection != null && (stats.getUnlockedSchematics().size() != stats.getLastUnlockedSchematics().size() || (player.ticksExisted - 1) % 100 == 0))
        {
            Integer[] iArray = new Integer[stats.getUnlockedSchematics().size()];

            for (int i = 0; i < iArray.length; i++)
            {
                ISchematicPage page = stats.getUnlockedSchematics().get(i);
                iArray[i] = page == null ? -2 : page.getPageID();
            }

            List<Object> objList = new ArrayList<Object>();
            objList.add(iArray);

            GalacticraftCore.packetPipeline.sendTo(new PacketSimple(EnumSimplePacket.C_UPDATE_SCHEMATIC_LIST, GCCoreUtil.getDimensionID(player.world), objList), player);
        }
    }

    public void onPlayerUpdate(EntityPlayerMP player)
    {
        GCPlayerStats stats = GCPlayerStats.get(player);
        this.updateSchematics(player, stats);
    }

}
