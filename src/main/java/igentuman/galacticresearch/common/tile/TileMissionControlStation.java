package igentuman.galacticresearch.common.tile;

import appeng.core.worlddata.WorldData;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.common.block.BlockTelescope;
import igentuman.galacticresearch.common.capability.PlayerSpaceData;
import igentuman.galacticresearch.common.capability.SpaceCapabilityHandler;
import igentuman.galacticresearch.common.data.DimensionProvider;
import igentuman.galacticresearch.common.entity.EntityMiningRocket;
import igentuman.galacticresearch.common.entity.EntitySatelliteRocket;
import igentuman.galacticresearch.common.entity.IGRAutoRocket;
import igentuman.galacticresearch.integration.computer.IComputerIntegration;
import igentuman.galacticresearch.network.GRPacketSimple;
import igentuman.galacticresearch.util.Util;
import io.netty.buffer.ByteBuf;
import li.cil.repack.org.luaj.vm2.ast.Str;
import micdoodle8.mods.galacticraft.annotations.ForRemoval;
import micdoodle8.mods.galacticraft.annotations.ReplaceWith;
import micdoodle8.mods.galacticraft.api.entity.IDockable;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntitySpaceshipBase;
import micdoodle8.mods.galacticraft.api.tile.IFuelDock;
import micdoodle8.mods.galacticraft.api.tile.ILandingPadAttachable;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseElectricBlockWithInventory;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import micdoodle8.mods.galacticraft.core.tile.TileEntityLandingPad;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
import micdoodle8.mods.galacticraft.core.util.WorldUtil;
import micdoodle8.mods.galacticraft.core.world.gen.dungeon.MapGenDungeon;
import micdoodle8.mods.miccore.Annotations;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.Village;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.WoodlandMansion;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TileMissionControlStation extends TileBaseElectricBlockWithInventory implements ISidedInventory, ILandingPadAttachable, IComputerIntegration {

    @Override
    public String[] getMethods() {
        return new String[] {
                "getComponentName", "getMissionsInfo","selectMission", "activateMission",
                "missionDuration", "getSpaceStations", "getLocatableObjects", "selectSpaceStation",
                "selectObjectToLocate", "locateObject", "getLocationData", "setCoordinates"
        };
    }

    public String getComponentName()
    {
        return "mission_control_station";
    }

    @Override
    public Object[] invoke(int method, Object[] args) throws NoSuchMethodException {
        switch (method) {
            case 0:
                return new Object[] {getComponentName()};
            case 1:
                return getMissionsInfo();
            case 2:
                return selectMission(args);
            case 3:
                return new Object[] {activateMission()};
            case 4:
                return new Object[]{ModConfig.machines.satellite_mission_duration*20};
            case 5:
                return new Object[]{getStations()};
            case 6:
                return new Object[]{getObjectsToLocate()};
            case 7:
                return new Object[] {selectStation((String) args[0])};
            case 8:
                return new Object[] {selectLocatable((String) args[0])};
            case 9:
                return new Object[] {locate()};
            case 10:
                return new Object[] {getLocatorDataItems()};
            case 11:
                return new Object[] {setLocationCords((Integer) args[0], (Integer) args[1])};
            default:
                throw new NoSuchMethodException();
        }
    }

    public TileMissionControlStation fetchPlayerStations(EntityPlayer player)
    {
        if(world.isRemote) return this;
        GCPlayerStats stats = GCPlayerStats.get(player);
        for(int dim: stats.getSpaceStationDimensionData().keySet()) {
            if(Arrays.stream(getStations()).anyMatch(v -> v.equals(String.valueOf(dim)))) {
                continue;
            }
            if(currentStation.isEmpty()) {
                currentStation = String.valueOf(dim);
            }
            if(stations.isEmpty()) {
                stations = String.valueOf(dim);
            } else {
                stations += ";"+String.valueOf(dim);
            }
        }

        return this;
    }

    public boolean locate()
    {
        if(currentLocatable.isEmpty() || currentStation.isEmpty()) {
            return false;
        }
        locatorData = "";
        locationCounter = ModConfig.locator.location_duration;
        markDirty();
        return true;
    }

    public int getLocatableObjectId()
    {
        if(currentLocatable.isEmpty()) return 0;
        for (int i = 0; i< getObjectsToLocate().length; i++) {
            if(getObjectsToLocate()[i].equals(currentLocatable)) {
                return i;
            }
        }
        return 0;
    }

    public void selectLocatable(int id)
    {
        currentLocatable = getObjectsToLocate()[id];
    }

    public boolean selectLocatable(String name)
    {
        List<String> st = Arrays.asList(getObjectsToLocate());
        if(st.contains(name)) {
            currentLocatable = name;
            return true;
        }
        return false;
    }

    public void selectStation(int id)
    {
        try {
            currentStation = getStations()[id];
        } catch (IndexOutOfBoundsException ignored) {

        }
    }

    public boolean selectStation(String name)
    {
        List<String> st = Arrays.asList(getStations());
        if(st.contains(name)) {
            currentStation = name;
            return true;
        }
        return false;
    }

    public int getCurStationId()
    {
        if(currentStation.isEmpty()) return 0;
        for (int i = 0; i< getStations().length; i++) {
            if(getStations()[i].equals(currentStation)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void decodePacketdata(ByteBuf buffer) {
        super.decodePacketdata(buffer);
        try {
            locationCords = new int[]  {
                            Integer.parseInt(locatorCords.split(",")[0]),
                            Integer.parseInt(locatorCords.split(",")[1])
            };
        } catch (NumberFormatException|IndexOutOfBoundsException ignored) {

        }
    }

    public boolean setLocationCords(Integer x, Integer y)
    {
        locationCords = new int[]{x, y};
        locatorCords = x +","+ y;
        markDirty();
        return true;
    }

    public String getStationName(String dim)
    {
        if(dim.isEmpty()) return "none";
        return WorldUtil.getDimensionTypeById(Integer.parseInt(dim)).getName();
    }

    public String[] getStations()
    {
        return stations.split(";");
    }

    public String[] getObjectsToLocate()
    {
        return ModConfig.locator.getLocatableObjects();
    }


    public Object[] getMissionsInfo()
    {
        return new Object[] {missionsDataMap};
    }

    public Object[] selectMission(Object[] args)
    {
        String name = (String) args[0];
        boolean result = false;
        if(Arrays.asList(getMissions()).contains(name)) {
            currentMission = name;
            result = true;
        }
        return new Object[] {result};
    }

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public int dimension;

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public int locationCounter = -2;

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public String locatorCords;

    public int[] locationCords;

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public String currentLocatable = getObjectsToLocate()[0];

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public String locatorData = "";

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public String currentStation = "";

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public String stations = "";

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public String missions = "";

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public String currentMission = "";

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public String missionsData = "";

    public HashMap<String, Integer> missionsDataMap = new HashMap<>();

    public DimensionProvider dimensionProvider;

    private TileTelescope telescope;

    public Object attachedDock;

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public int rocketState;

    public TileMissionControlStation() {
        super("container.mission_control_staion.name");
        this.storage.setMaxExtract(45.0F);
        this.inventory = NonNullList.withSize(1, ItemStack.EMPTY);
        dimensionProvider = new DimensionProvider(this);
        locationCords = new int[] {getPos().getX(), getPos().getZ()};
        locatorCords = getPos().getX() +","+ getPos().getZ();
    }

    public void serializeLocatorData(List<String> result)
    {
        locatorData = "";
        for(String s: result) {
            if(locatorData.isEmpty()) {
                locatorData = s;
                continue;
            }
            locatorData += ";"+s;
        }
    }

    public void locateIEDeposits()
    {
        List<String> result = new ArrayList<>();
        ExcavatorHandler.MineralWorldInfo info;
        World worldIn = WorldUtil.getWorldForDimensionServer(Integer.parseInt(currentStation));

        for(int r=0; r<ModConfig.locator.radius/32;r++) {
            for(int x = -r; x <=r; x++) {
                int z = r/2;
                info = ExcavatorHandler.getMineralWorldInfo(worldIn, (getLocatorXCord() + x*16) / 16, (getLocatorZCord() + z*16) / 16);
                if (info.mineral != null && info.depletion == 0) {
                    String line = (getLocatorXCord() + x*16) + "," + (getLocatorZCord() + z*16);
                    if(!result.contains(line)) result.add(line);
                }
                info = ExcavatorHandler.getMineralWorldInfo(worldIn, (getLocatorXCord() + x*16) / 16, (getLocatorZCord() -z*16) / 16);
                if (info.mineral != null && info.depletion == 0) {
                    String line = (getLocatorXCord() + x*16) + "," + (getLocatorZCord() - z*16);
                    if(!result.contains(line)) result.add(line);
                }
            }

            for(int z = -r; z <=r; z++) {
                int x = r/2;
                info = ExcavatorHandler.getMineralWorldInfo(worldIn, (getLocatorXCord() + x*16) / 16, (getLocatorZCord() + z*16) / 16);
                if (info.mineral != null && info.depletion == 0) {
                    String line = (getLocatorXCord() + x*16) + "," + (getLocatorZCord() + z*16);
                    if(!result.contains(line)) result.add(line);
                }
                info = ExcavatorHandler.getMineralWorldInfo(worldIn, (getLocatorXCord() - x*16) / 16, (getLocatorZCord() +z*16) / 16);
                if (info.mineral != null && info.depletion == 0) {
                    String line = (getLocatorXCord() - x*16) + "," + (getLocatorZCord() + z*16);
                    if(!result.contains(line)) result.add(line);
                }
            }
            if(result.size() > 4) {
                serializeLocatorData(result);
                return;
            }
        }
        serializeLocatorData(result);
    }

    public void locateVillages()
    {
        BlockPos m = GalacticResearch.server.
                getWorld(Integer.parseInt(currentStation)).
                findNearestStructure("Village", new BlockPos(getLocatorXCord(), 0, getLocatorZCord()), true);

        if(m != null) {
            locatorData = m.getX()+","+m.getZ();
        }

    }

    public void locateMansions()
    {
        BlockPos m = GalacticResearch.server.
                getWorld(Integer.parseInt(currentStation)).
                findNearestStructure("Mansion", new BlockPos(getLocatorXCord(), 0, getLocatorZCord()), true);
        if(m != null) {
            locatorData = m.getX()+","+m.getZ();
        }
    }

    public void locateMonuments()
    {
        BlockPos m = GalacticResearch.server.
                getWorld(Integer.parseInt(currentStation)).
                findNearestStructure("Monument", new BlockPos(getLocatorXCord(), 0, getLocatorZCord()), true);
        if(m != null) {
            locatorData = m.getX()+","+m.getZ();
        }
    }

    public void locateTemples()
    {
        BlockPos m = GalacticResearch.server.
                getWorld(Integer.parseInt(currentStation)).
                findNearestStructure("Temple", new BlockPos(getLocatorXCord(), 0, getLocatorZCord()), true);
        if(m != null) {
            locatorData = m.getX()+","+m.getZ();
        }
    }

    public void locateCustom(String name)
    {
        BlockPos m = GalacticResearch.server.
                getWorld(Integer.parseInt(currentStation)).
                findNearestStructure(name, new BlockPos(getLocatorXCord(), 0, getLocatorZCord()), true);
        if(m != null) {
            locatorData = m.getX()+","+m.getZ();
        }
    }

    public void locateAE2Meteorites()
    {
        World worldIn = WorldUtil.getWorldForDimensionServer(Integer.parseInt(currentStation));

        Collection<NBTTagCompound> meteorites = WorldData.instance().spawnData().
                getNearByMeteorites(worldIn.provider.getDimension(), getLocatorXCord()/16, getLocatorZCord()/16);
        List<String> result = new ArrayList<>();
        for(NBTTagCompound m: meteorites) {
            result.add(m.getInteger("x") +","+ m.getInteger("z"));
        }
        serializeLocatorData(result);
    }

    public void locateBossDungeons()
    {
        World worldIn = WorldUtil.getWorldForDimensionServer(Integer.parseInt(currentStation));

        List<String> result = new ArrayList<>();

        int spacing = ((IGalacticraftWorldProvider)worldIn.provider).getDungeonSpacing();

        int x = MathHelper.floor(getLocatorXCord());
        int z = MathHelper.floor(getLocatorXCord());
        int quadrantX = x % spacing;
        int quadrantZ = z % spacing;
        int searchOffsetX = quadrantX / (spacing / 2);
        int searchOffsetZ = quadrantZ / (spacing / 2);

        for(int cx = searchOffsetX - 1; cx < searchOffsetX + 1; ++cx) {
            for (int cz = searchOffsetZ - 1; cz < searchOffsetZ + 1; ++cz) {
                long dungeonPos = MapGenDungeon.getDungeonPosForCoords(worldIn, (x + cx * spacing) / 16, (z + cz * spacing) / 16, spacing);
                int i = 2 + ((int) (dungeonPos >> 32) << 4);
                int j = 2 + ((int) dungeonPos << 4);
                double oX = (double) i - getLocatorXCord();
                double oZ = (double) j - getLocatorXCord();
                result.add(oX+","+oZ);
                if(result.size() > 5) {
                    serializeLocatorData(result);
                    return;
                }
            }
        }
        serializeLocatorData(result);
    }


    public void doLocate()
    {
        if(locationCounter > 0) {
            locationCounter--;
            return;
        }
        if(locationCounter == -2) {
            return;
        }
        locationCounter = -2;
        if(currentStation.isEmpty()) return;
        switch (currentLocatable) {
            case "ie_deposit":
                locateIEDeposits();
                break;
            case "village":
                locateVillages();
                break;
            case "mansion":
                locateMansions();
                break;
            case "temple":
                locateTemples();
                break;
            case "monument":
                locateMonuments();
                break;
            case "ae2_meteorite":
                locateAE2Meteorites();
                break;
            case "boss_dungeon":
                locateBossDungeons();
                break;
            default:
                locateCustom(currentLocatable);
        }
    }

    public String[] getLocatorDataItems()
    {
        List<String> items = new ArrayList<>();
        for(String line: locatorData.split(";")) {
            String[] pairs = line.split(",");
            if(pairs[0].isEmpty()) continue;
            items.add("X: "+ Float.valueOf(pairs[0]).intValue()+", Z: "+Float.valueOf(pairs[1]).intValue());
        }
        if(items.size() == 0) items.add(" ");
        return items.stream().toArray(String[]::new);
    }


    public void playerAnalyzeData(EntityPlayer player)
    {
        try {
            EntityPlayerMP playerBaseClient = PlayerUtil.getPlayerBaseServerFromPlayerUsername(player.getName(), true);
            PlayerSpaceData cap = playerBaseClient.getCapability(SpaceCapabilityHandler.PLAYER_SPACE_DATA, null);
            if (cap == null) {
                GalacticResearch.instance.logger.log(Level.WARN, "Analyze missions capability error");
                return;
            }
            for (String mission : getMissions()) {
                if (isMissionComplete(mission)) {
                    if (!cap.getUnlockedMissions().contains(mission)) {
                        cap.addMission(mission);
                        player.addExperience(10);
                        String planetLocalized = I18n.format("gui."+mission+".name");
                        player.sendMessage(new TextComponentString(I18n.format("message.analyzed.planet", planetLocalized)));
                    }
                }
            }
            GalacticResearch.packetPipeline.sendTo(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.SYNC_PLAYER_SPACE_DATA, GCCoreUtil.getDimensionID(player.world), new Object[] { cap.unlocked_missions }), (EntityPlayerMP) player);

        } catch (NullPointerException e) {
            GalacticResearch.instance.logger.log(Level.ERROR, "Analyze missions capability error");
        }
    }

    public boolean isMissionComplete(String name)
    {
        return getMissionInfo(name) >= ModConfig.machines.satellite_mission_duration*20;
    }

    public void fetchMissions()
    {
        TileTelescope te = getTelescope();
        if(te == null) return;
        missions = te.getResearchedBodies();
        for(String m: getMissions()) {
            if(missionsDataMap.containsKey(m)) continue;
            missionsDataMap.put(m, -1);
        }
        serializeMissionData();
    }

    @Override
    public boolean canExtractItem(int slotID, @NotNull ItemStack itemstack, EnumFacing side)
    {
        return slotID == 0;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @NotNull ItemStack itemstack)
    {
        return slotID == 0 && ItemElectricBase.isElectricItem(itemstack.getItem());
    }

    public int getLocatorXCord()
    {
        if(locationCords == null || locationCords.length == 0) {
            return getPos().getX();
        }
        return locationCords[0];
    }

    public int getLocatorZCord()
    {
        if(locationCords == null || locationCords.length == 0) {
            return getPos().getZ();
        }
        return locationCords[1];
    }

    @Override
    public ItemStack getBatteryInSlot()
    {
        return this.getStackInSlot(0);
    }


    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            if(btnCooldown > 0) btnCooldown--;
            if(getEnergyStoredGC() < 100) {
                return;
            }
            if(locationCords == null) {
                locationCords = new int[] {getPos().getX(), getPos().getZ()};
            }
            doLocate();
            fetchMissions();
            updateRocketState();
            removeAsteroidMissions(true);
            updateMissionsStateCounter();
        } else {
            unserializeMissionData();
        }
    }

    public void removeAsteroidMissions(boolean check)
    {
        for(String m: getMissions()) {
            if(!m.contains("ASTEROID-")) continue;
            if(check) {
                if (!GalacticResearch.spaceMineProvider.getMissions().containsKey(m)) {
                    missionsDataMap.remove(m);
                    if (currentMission.equals(m)) currentMission = "";
                }
            } else {
                missionsDataMap.remove(m);
                if (currentMission.equals(m)) currentMission = "";
            }
        }
    }


    public void updateMissionsStateCounter()
    {
        int duration = ModConfig.machines.satellite_mission_duration * 20;
        if(missionsDataMap.isEmpty()) return;
        Set<String> tmp = missionsDataMap.keySet();
        for(String s: tmp) {

            int v = missionsDataMap.get(s);
            if(s.contains("ASTEROID-")) {
                if(GalacticResearch.spaceMineProvider.getMissions().size() == 0) {
                     removeAsteroidMissions(false);
                     return;
                }
                try {
                    double left = GalacticResearch.spaceMineProvider.getMissions().get(s);
                    double initial = GalacticResearch.spaceMineProvider.getOreCnt(s);
                    double p = left / initial;
                    if (left <= 0) {
                        v = duration;
                    } else {
                        v = (int) (duration - duration * p) - 1;
                    }
                    missionsDataMap.replace(s, v);
                } catch (NullPointerException ignored) {

                }
            } else if (v > 0 && v < duration) {
                v++;
                missionsDataMap.replace(s, v);
            }
        }
        serializeMissionData();
    }

    public boolean activateMission()
    {
        if(rocketState != 1) return false;
        IGRAutoRocket r = getRocket();
        if(r == null) {
            return false;
        }
        if(r instanceof IGRAutoRocket) {
            ((IGRAutoRocket) r).setMission(currentMission);
        }
        r.setAutolaunchSetting(EntityAutoRocket.EnumAutoLaunch.INSTANT);
        r.setLaunchPhase(EntitySpaceshipBase.EnumLaunchPhase.IGNITED);
        setMissionInfo(currentMission, 0);
        return true;
    }

    public int getMissonPercent(String name)
    {
        int i = getMissionInfo(name);
        return (int)((double)i/(ModConfig.machines.satellite_mission_duration*20)*100);
    }

    public void updateRocketState()
    {
        EntityAutoRocket r = (EntityAutoRocket) getRocket();

        if(r == null || !(r instanceof IGRAutoRocket)) {
            rocketState = -1;
            return;
        }
        if(r.fuelTank.getFluidAmount() >= r.fuelTank.getCapacity()/2) {
            rocketState = 1;
        } else {
            rocketState = 0;
        }
        if(currentMission.contains("ASTEROID-") && !(r instanceof EntityMiningRocket)) {
            rocketState = -2;
        }

        if(!currentMission.contains("ASTEROID-") && !(r instanceof EntitySatelliteRocket)) {
            rocketState = -2;
        }
    }

    public TileTelescope getTelescope() {
        if(telescope == null) {
            TileEntity te = world.getTileEntity(pos.offset(world.getBlockState(pos).getValue(BlockTelescope.FACING).rotateY()));
            if(te != null && te instanceof TileTelescope) {
                telescope = (TileTelescope) te;
                return telescope;
            }
            te = world.getTileEntity(pos.offset(world.getBlockState(pos).getValue(BlockTelescope.FACING).rotateY().getOpposite()));
            if(te != null && te instanceof TileTelescope) {
                telescope = (TileTelescope) te;
                return telescope;
            }
        }
        return telescope;
    }

    private int btnCooldown = 0;

    public void nextMission()
    {
        if(btnCooldown > 0) return;
        btnCooldown = 10;
        String[] tmp = getMissions();
        boolean f = false;
        for(String m: tmp) {
            if(currentMission.isEmpty() || f) {
                currentMission = m;
                return;
            }
            if(currentMission.equals(m)) {
                f = true;
            }
        }
    }

    public void prevMission()
    {
        if(btnCooldown > 0) return;
        btnCooldown = 10;
        String[] tmp = getMissions();
        int i = 0;
        for(String m: tmp) {
            if(currentMission.isEmpty()) {
                currentMission = m;
            }
            if(currentMission.equals(m)) {
                try {
                    currentMission = tmp[i-1];
                    return;
                } catch (Exception e){
                    currentMission = "";
                    return;
                }
            }
            i++;
        }
    }

    public String[] getMissions()
    {
        return Arrays.stream(missions.split(",")).filter(val -> !val.isEmpty()).toArray(String[]::new);
    }

    public boolean shouldUseEnergy() {
        return !this.getDisabled(0);
    }

    public int[] getSlotsForFace(@NotNull EnumFacing side) {
        return side != this.getElectricInputDirection() ? new int[]{0} : new int[0];
    }

    public EnumFacing getElectricInputDirection() {
        return world.getBlockState(pos).getValue(BlockTelescope.FACING).getOpposite();
    }

    public void serializeMissionData()
    {
        missionsData = Util.serializeMap(missionsDataMap);
        markDirty();
    }

    public void setMissionInfo(String name, int v)
    {
        if(missionsDataMap.containsKey(name)) {
            missionsDataMap.replace(name, v);
        } else {
            missionsDataMap.put(name, v);
        }
        if(!world.isRemote) serializeMissionData();
    }

    public int getMissionInfo(String name)
    {
        if(missionsDataMap.containsKey(name)) {
            return missionsDataMap.get(name);
        }
        return -1;
    }

    public String getMissionStatusKey(String name)
    {
        int state = getMissionInfo(name);
        if(state >= ModConfig.machines.satellite_mission_duration*20) {
            return "gui.mission.complete";
        }
        if(state == -1) {
            return "gui.mission.pending";
        }
        return "gui.mission.progress";
    }

    public int getLocatorProgress()
    {
        if(locationCounter == -2) return 0;
        return (int) (((float)(ModConfig.locator.location_duration-locationCounter)/ModConfig.locator.location_duration)*100);
    }

    public void unserializeMissionData()
    {
        missionsDataMap = Util.unserializeMap(missionsData);
    }

    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.currentLocatable = nbt.getString("currentLocatable");
        this.currentStation = nbt.getString("currentStation");
        this.locatorData = nbt.getString("locatorData");
        this.dimension = nbt.getInteger("dimension");
        this.locationCounter = nbt.getInteger("locationCounter");
        this.locationCords = nbt.getIntArray("locationCords");
        this.currentMission = nbt.getString("currentMission");
        this.missions = nbt.getString("missions");
        this.stations = nbt.getString("stations");
        this.missionsData = nbt.getString("missionsData");
        unserializeMissionData();
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        serializeMissionData();
        nbt.setString("currentLocatable", this.currentLocatable);
        nbt.setString("currentStation", this.currentStation);
        nbt.setString("locatorData", this.locatorData);
        nbt.setString("stations", this.stations);
        nbt.setInteger("dimension", this.dimension);
        nbt.setInteger("locationCounter", this.locationCounter);
        nbt.setIntArray("locationCords", this.locationCords);
        nbt.setString("currentMission", this.currentMission);
        nbt.setString("missions", this.missions);
        nbt.setString("missionsData", this.missionsData);
        return nbt;
    }

    /** @deprecated */
    @Deprecated
    @ForRemoval(
            deadline = "4.1.0"
    )
    @ReplaceWith("byIndex()")
    public EnumFacing getFront() {
        return this.byIndex();
    }

    @Override
    public boolean canAttachToLandingPad(IBlockAccess iBlockAccess, BlockPos blockPos) {
        TileEntity tile = world.getTileEntity(blockPos);
        return tile instanceof TileEntityLandingPad;
    }

    public void setAttachedPad(IFuelDock pad) {
        this.attachedDock = pad;
    }

    public IGRAutoRocket getRocket()
    {
        if (attachedDock instanceof TileEntityLandingPad)
        {
            TileEntityLandingPad pad = ((TileEntityLandingPad) attachedDock);
            IDockable rocket = pad.getDockedEntity();
            if (rocket instanceof IGRAutoRocket)
            {
                return (IGRAutoRocket) rocket;
            }
        }
        return null;
    }
}
