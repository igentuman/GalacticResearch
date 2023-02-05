package igentuman.galacticresearch.command;

import igentuman.galacticresearch.GalacticResearch;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

public class CommandAsteroids extends CommandBase {
	
	@Override
	public @NotNull String getName() {
		return "asteroids";
	}
	
	@Override
	public @NotNull String getUsage(@NotNull ICommandSender sender) {
		return "/asteroids generate|clear";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) {
		EntityPlayerMP p = (EntityPlayerMP) sender;
		if(args.length < 1) {
			p.sendMessage(new TextComponentString(getUsage(sender)));
			return;
		}
		String r = args[0];
		if(r.equals("generate")) {
			String m = GalacticResearch.spaceMineProvider.generateMission(true);
			p.sendMessage(new TextComponentString(I18n.format("message.mining_asteroid.generated", m)));
		}

		if(r.equals("clear")) {
			GalacticResearch.spaceMineProvider.removeMissions();
			p.sendMessage(new TextComponentString(I18n.format("message.mining_asteroid.cleared")));
		}
	}
}
