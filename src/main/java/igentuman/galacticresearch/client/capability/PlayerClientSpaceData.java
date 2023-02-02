package igentuman.galacticresearch.client.capability;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerClientSpaceData implements ISpaceData {

	public String unlocked_missions = "";
	private WeakReference<EntityPlayerSP> player;

	public List<String> getUnlockedMissions()
	{
		if(unlocked_missions.isEmpty()) {
			return new ArrayList<>();
		}
		return Arrays.asList(unlocked_missions.split(","));
	}

	public void setPlayer(WeakReference<EntityPlayerSP> player)
	{
		this.player = player;
	}

	public void addMission(String name)
	{
		unlocked_missions += ","+name;
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
