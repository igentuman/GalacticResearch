package igentuman.galacticresearch.command;

import igentuman.galacticresearch.GalacticResearch;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.server.command.CommandTreeBase;
import org.jetbrains.annotations.NotNull;

public class CommandAsteroids extends CommandTreeBase {

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
			if(args.length > 1) {
				for(int i = 0; i < Integer.parseInt(args[1]); i++) {
					String m = GalacticResearch.spaceMineProvider.generateMission(true);
					notifyCommandListener(sender, this, "message.mining_asteroid.generated", m);
				}
			} else {
				String m = GalacticResearch.spaceMineProvider.generateMission(true);
				notifyCommandListener(sender, this, "message.mining_asteroid.generated", m);
			}
		}

		if(r.equals("clear")) {
			GalacticResearch.spaceMineProvider.removeMissions();
			notifyCommandListener(sender, this, "message.mining_asteroid.cleared");
		}
	}
}
