package igentuman.galacticresearch.common.data;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.util.Util;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static igentuman.galacticresearch.GalacticResearch.MODID;

public class WorldSaveDataGR extends WorldSavedData {
    private static final String DATA_ASTEROIDS = MODID + "_data";
    private static WorldSaveDataGR instance;

    public WorldSaveDataGR(String name) {
        super(name);
    }
    public WorldSaveDataGR() {
        super(DATA_ASTEROIDS);
    }

    public void readFromNBT(NBTTagCompound nbt) {
        HashMap<String, Integer> m = Util.unserializeMap(nbt.getString("missions"));
        GalacticResearch.spaceMineProvider.generateCounter  = nbt.getInteger("generateCounter");
        GalacticResearch.spaceMineProvider.setMissions(m);
    }

    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("missions", Util.serializeMap(GalacticResearch.spaceMineProvider.getMissions()));
        nbt.setInteger("generateCounter", GalacticResearch.spaceMineProvider.generateCounter);
        return nbt;
    }

    public static WorldSaveDataGR get(World world) {
        MapStorage storage = world.getMapStorage();
        if(instance == null) {
            try {
                assert storage != null;
                instance = (WorldSaveDataGR) storage.getOrLoadData(WorldSaveDataGR.class, DATA_ASTEROIDS);
            } catch (NullPointerException ignore) {
            } finally {
                if (instance == null) {
                    instance = new WorldSaveDataGR();
                    assert storage != null;
                    storage.setData(DATA_ASTEROIDS, instance);
                }
            }
        }
        return instance;
    }

    public void save(World world)
    {
        MapStorage storage = world.getMapStorage();
        assert storage != null;
        this.setDirty(true);
        storage.saveAllData();
    }

}
