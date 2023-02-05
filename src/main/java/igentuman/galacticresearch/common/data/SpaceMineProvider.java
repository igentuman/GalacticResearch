package igentuman.galacticresearch.common.data;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.util.StackUtil;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;

import java.nio.charset.Charset;
import java.util.*;

public class SpaceMineProvider {
    private static SpaceMineProvider instance;
    private HashMap<String, Integer> missions = new HashMap<>();
    public HashMap<ItemStack, Integer> ores = new HashMap<>();
    public int generateCounter;
    private WorldSaveDataGR dataHolder = null;

    public boolean isMissionInProgress(String mission)
    {
        if(missions.containsKey(mission)) {
            int left = missions.get(mission);
            return getOreCnt(mission) != left;
        }
        return false;
    }

    private SpaceMineProvider()
    {}

    public void removeMissions()
    {
        missions.clear();
        saveData();
    }

    //deleting 1 oldest untouched mission and all completed
    public void deleteMissions()
    {
        HashMap<String, Integer> tmp = new HashMap<>();
        String toDelete = "";
        for(String m: missions.keySet()) {
            if(toDelete.isEmpty()) {
                if(!isMissionInProgress(m)) {
                    toDelete = m;
                }
            }
            if(missions.get(m) > 0) {
                tmp.put(m, missions.get(m));
            } else {
                GalacticResearch.skyModel.removeAsteroid(m);
            }
        }
        if(!toDelete.isEmpty() && tmp.size() > ModConfig.machines.mining_missions_limit) {
            tmp.remove(toDelete);
            GalacticResearch.skyModel.removeAsteroid(toDelete);
        }
        missions = tmp;
    }

    public HashMap<String, Integer> getMissions()
    {
        return missions;
    }

    public void setMissions(HashMap<String, Integer> m)
    {
        missions = m;
    }

    private WorldSaveDataGR dataHolder()
    {
        try {
            if (dataHolder == null) {
                dataHolder = WorldSaveDataGR.get(GalacticResearch.server.getEntityWorld());
            }
        } catch (NullPointerException ignored) {}
        return dataHolder;
    }

    public void saveData()
    {
        dataHolder().save(GalacticResearch.server.getEntityWorld());
    }

    public void updateMissions()
    {
        dataHolder();
        generateCounter--;
        if(generateCounter <= 0) {
            deleteMissions();
            generateMission();
            generateCounter = 864000/ModConfig.machines.mining_asteroids_popularity;
            saveData();
        }
    }

    public static SpaceMineProvider get() {
        if(instance == null) {
            instance = new SpaceMineProvider();
            instance.generateCounter = 864000/ModConfig.machines.mining_asteroids_popularity;
            ArrayList<Integer> list = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : ModConfig.machines.mineable_resources.entrySet()) {
                list.add(entry.getValue());
            }
            Collections.sort(list);
            for (int num : list) {
                for (Map.Entry<String, Integer> entry : ModConfig.machines.mineable_resources.entrySet()) {
                    if (entry.getValue().equals(num)) {
                        ItemStack st = StackUtil.stackFromRegistry(entry.getKey());
                        if(st == null || st.equals(ItemStack.EMPTY)) continue;
                        instance.ores.put(st, num);
                    }
                }
            }
        }
        return instance;
    }



    protected ItemStack randomOre(String mission)
    {
        Random r = new Random(mission.hashCode()+missions.get(mission));
        int pos = r.nextInt(ores.size())-1;
        if(pos < ores.size()/2) pos = r.nextInt(ores.size()) - 1;
        int i = 0;
        for(ItemStack st: ores.keySet()) {
            if(i == pos) {
                return st;
            }
            i++;
        }
        return ItemStack.EMPTY;
    }

    public ItemStack mineBlock(String mission)
    {
        if(missions.containsKey(mission)) {
            int left = missions.get(mission);
            if(left > 0) {
                left--;
                missions.replace(mission, left);
                return randomOre(mission);
            }
        }
        return ItemStack.EMPTY;
    }

    private String randomString(int n)
    {
        byte[] array = new byte[256];
        new Random().nextBytes(array);
        String randomString
                = new String(array, Charset.forName("UTF-8"));
        StringBuilder r = new StringBuilder();
        for (int k = 0; k < randomString.length(); k++) {
            char ch = randomString.charAt(k);
            if (((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9'))
                    && (n > 0)) {
                r.append(ch);
                n--;
            }
        }
        return r.toString().toUpperCase();
    }

    public int getOreCnt(String name)
    {
        return Math.max(new Random(name.hashCode()).nextInt(ModConfig.machines.mining_mission_maximal_resources),
                ModConfig.machines.mining_mission_minimal_resources)*64;
    }

    public String generateMission(boolean saveFlag)
    {
        String result = generateMission();
        if(saveFlag) {
            saveData();
        }
        return result;
    }

    public String generateMission()
    {
        String name = "ASTEROID-"+randomString(4);
        int cnt = getOreCnt(name);
        this.missions.put(name, cnt);
        GalacticResearch.skyModel.addAsteroid(name);
        if(ModConfig.machines.announce_asteroids) {
            GalacticResearch.server.getPlayerList().sendMessage(new TextComponentString(GCCoreUtil.translate("message.new_asteroid")));
        }
        return name + " ("+cnt+")";
    }
}
