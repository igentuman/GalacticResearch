package igentuman.galacticresearch.common.capability;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class SpaceDataProvider implements ICapabilitySerializable<NBTTagCompound> {

	private final PlayerSpaceData spaceData;
	private EntityPlayerMP owner;

	public SpaceDataProvider(EntityPlayerMP owner) {
		this.owner = owner;
		spaceData = SpaceCapabilityHandler.PLAYER_SPACE_DATA.getDefaultInstance();
		spaceData.setPlayer(new WeakReference<>(this.owner));

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
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		spaceData.writeNBT(nbt);
		return nbt;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		spaceData.readNBT(nbt);
	}
}
