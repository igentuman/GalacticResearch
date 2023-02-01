package igentuman.galacticresearch.common.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;


public class PlayerSpaceData implements ISpaceData {

	protected BlockPos satelliteStationPos;
	
	@Override
	public NBTTagCompound writeNBT(NBTTagCompound nbt) {
		nbt.setIntArray("satellite_station_pos", new int[]{
				satelliteStationPos.getX(),
				satelliteStationPos.getY(),
				satelliteStationPos.getZ()
		});
		return nbt;
	}

	public void readNBT(NBTTagCompound nbt) {
		int[] pos = nbt.getIntArray("satellite_station_pos");
		if(pos.length == 3) {
			satelliteStationPos = new BlockPos(pos[0], pos[1], pos[2]);
		}
	}
}
