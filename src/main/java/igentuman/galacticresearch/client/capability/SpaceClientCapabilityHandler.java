package igentuman.galacticresearch.client.capability;

import igentuman.galacticresearch.GalacticResearch;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SpaceClientCapabilityHandler {

	@CapabilityInject(PlayerClientSpaceData.class)
	public static Capability<PlayerClientSpaceData> PLAYER_SPACE_CLIENT_DATA = null;
	public static final ResourceLocation PLAYER_SPACE_DATA_CLIENT_NAME = new ResourceLocation(GalacticResearch.MODID, "player_client_space_data");

	@SubscribeEvent
	public void attachSpaceDataCapability(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityPlayerSP) {
			event.addCapability(PLAYER_SPACE_DATA_CLIENT_NAME, new SpaceClientDataProvider((EntityPlayerSP) event.getObject()));
		}
	}

	public static void register()
	{
		CapabilityManager.INSTANCE.register(PlayerClientSpaceData.class, new Capability.IStorage<PlayerClientSpaceData>()
		{
			@Override
			public NBTBase writeNBT(Capability<PlayerClientSpaceData> capability, PlayerClientSpaceData instance, EnumFacing side)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				instance.writeNBT(nbt);
				return nbt;
			}

			@Override
			public void readNBT(Capability<PlayerClientSpaceData> capability, PlayerClientSpaceData instance, EnumFacing side, NBTBase nbt)
			{
				instance.readNBT((NBTTagCompound) nbt);
			}
		}, PlayerClientSpaceData::new);
	}
}
