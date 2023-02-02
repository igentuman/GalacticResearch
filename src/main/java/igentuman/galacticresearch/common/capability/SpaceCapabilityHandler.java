package igentuman.galacticresearch.common.capability;

import igentuman.galacticresearch.GalacticResearch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SpaceCapabilityHandler {

	@CapabilityInject(PlayerSpaceData.class)
	public static Capability<PlayerSpaceData> PLAYER_SPACE_DATA = null;
	public static final ResourceLocation PLAYER_SPACE_DATA_NAME = new ResourceLocation(GalacticResearch.MODID, "player_space_data");



	public static void register()
	{
		CapabilityManager.INSTANCE.register(PlayerSpaceData.class, new Capability.IStorage<PlayerSpaceData>()
		{
			@Override
			public NBTBase writeNBT(Capability<PlayerSpaceData> capability, PlayerSpaceData instance, EnumFacing side)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				instance.writeNBT(nbt);
				return nbt;
			}

			@Override
			public void readNBT(Capability<PlayerSpaceData> capability, PlayerSpaceData instance, EnumFacing side, NBTBase nbt)
			{
				instance.readNBT((NBTTagCompound) nbt);
			}
		}, PlayerSpaceData::new);
	}
}
