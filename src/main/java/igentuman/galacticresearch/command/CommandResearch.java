package igentuman.galacticresearch.command;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.common.capability.PlayerSpaceData;
import igentuman.galacticresearch.common.capability.SpaceCapabilityHandler;
import igentuman.galacticresearch.network.GRPacketSimple;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Level;


public class CommandResearch extends CommandBase {
	
	@Override
	public String getName() {
		return "research";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return "/research <planet name> | reset - to reset researches";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		EntityPlayerMP p = (EntityPlayerMP) sender;
		if(args.length < 1) {

			return;
		}
		String r = args[0];
		PlayerSpaceData cap = p.getCapability(SpaceCapabilityHandler.PLAYER_SPACE_DATA, null);

		if (cap == null) {
			GalacticResearch.instance.logger.log(Level.WARN, "Capability error");
			return;
		}
		if(r.equals("reset")) {
			cap.unlocked_missions = "";
			p.sendMessage(new TextComponentString(I18n.format("message.research.reset")));
		} else {
			if(!cap.getUnlockedMissions().contains(r)) {
				cap.addMission(r);
				p.sendMessage(new TextComponentString(I18n.format("message.research.added")));
			}
		}
		GalacticResearch.packetPipeline.sendTo(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.SYNC_PLAYER_SPACE_DATA, GCCoreUtil.getDimensionID(p.world), new Object[] { cap.unlocked_missions }), (EntityPlayerMP) p);

	}
}
