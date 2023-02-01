package igentuman.galacticresearch.common.capability;


import net.minecraft.nbt.NBTTagCompound;

public interface ISpaceData  {

	NBTTagCompound writeNBT(NBTTagCompound nbt);

	void readNBT(NBTTagCompound nbt);

}
