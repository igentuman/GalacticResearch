package igentuman.galacticresearch.client.capability;


import net.minecraft.nbt.NBTTagCompound;

public interface ISpaceData  {

	NBTTagCompound writeNBT(NBTTagCompound nbt);

	void readNBT(NBTTagCompound nbt);

}
