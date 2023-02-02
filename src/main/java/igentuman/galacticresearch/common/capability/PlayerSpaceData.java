package igentuman.galacticresearch.common.capability;

import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerSpaceData implements ISpaceData {

	public String unlocked_missions = "";

	public List<String> getUnlockedMissions()
	{
		if(unlocked_missions.isEmpty()) {
			return new ArrayList<>();
		}
		return Arrays.asList(unlocked_missions.split(","));
	}
	
	@Override
	public NBTTagCompound writeNBT(NBTTagCompound nbt) {
		nbt.setString("unlocked_missions", unlocked_missions);
		return nbt;
	}

	public void readNBT(NBTTagCompound nbt) {
		unlocked_missions = nbt.getString("unlocked_missions");
	}
}
