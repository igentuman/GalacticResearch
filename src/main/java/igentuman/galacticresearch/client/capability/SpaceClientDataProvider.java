package igentuman.galacticresearch.client.capability;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpaceClientDataProvider implements ICapabilityProvider {

	private final PlayerClientSpaceData spaceData;
	private EntityPlayerSP owner;

	public SpaceClientDataProvider(EntityPlayerSP owner) {
		this.owner = owner;
		spaceData = SpaceClientCapabilityHandler.PLAYER_SPACE_CLIENT_DATA.getDefaultInstance();

	}
	
	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == SpaceClientCapabilityHandler.PLAYER_SPACE_CLIENT_DATA;
	}
	
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == SpaceClientCapabilityHandler.PLAYER_SPACE_CLIENT_DATA) {
			return SpaceClientCapabilityHandler.PLAYER_SPACE_CLIENT_DATA.cast(spaceData);
		}
		return null;
	}
}
