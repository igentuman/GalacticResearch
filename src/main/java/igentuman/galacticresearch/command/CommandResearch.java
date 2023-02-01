package igentuman.galacticresearch.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.Chunk;


public class CommandResearch extends CommandBase {
	
	@Override
	public String getName() {
		return "research";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return "";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		Chunk chunk = sender.getEntityWorld().getChunk(sender.getPosition());

	}
}
