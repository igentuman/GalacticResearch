package igentuman.galacticresearch.common.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpaceDataProvider implements ICapabilitySerializable<NBTBase> {

	private final ISpaceData spaceData;

	public SpaceDataProvider() {
		spaceData = new PlayerSpaceData();
	}
	
	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == SpaceCapabilityHandler.PLAYER_SPACE_DATA;
	}
	
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == SpaceCapabilityHandler.PLAYER_SPACE_DATA) {
			return SpaceCapabilityHandler.PLAYER_SPACE_DATA.cast(spaceData);
		}
		return null;
	}
	
	@Override
	public NBTBase serializeNBT() {
		try {
			return SpaceCapabilityHandler.PLAYER_SPACE_DATA.writeNBT(spaceData, null);
		} catch (NullPointerException exception) {
			NBTTagCompound nbt = new NBTTagCompound();
			spaceData.writeNBT(nbt);
			return nbt;
		}

	}
	
	@Override
	public void deserializeNBT(NBTBase nbt) {
		SpaceCapabilityHandler.PLAYER_SPACE_DATA.readNBT(spaceData, null, nbt);
	}
}
