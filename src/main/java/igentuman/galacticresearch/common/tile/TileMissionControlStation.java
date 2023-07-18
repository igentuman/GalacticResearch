package igentuman.galacticresearch.common.tile;

import appeng.core.worlddata.WorldData;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import com.mjr.extraplanets.tileEntities.blocks.TileEntityTier2LandingPad;
import galaxyspace.systems.SolarSystem.planets.overworld.tile.TileEntityAdvLandingPad;
import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.common.block.BlockTelescope;
import igentuman.galacticresearch.common.capability.PlayerSpaceData;
import igentuman.galacticresearch.common.capability.SpaceCapabilityHandler;
import igentuman.galacticresearch.common.entity.EntityMiningRocket;
import igentuman.galacticresearch.common.entity.EntitySatelliteRocket;
import igentuman.galacticresearch.common.entity.IGRAutoRocket;
import igentuman.galacticresearch.integration.computer.IComputerIntegration;
import igentuman.galacticresearch.network.GRPacketSimple;
import igentuman.galacticresearch.util.GRSounds;
import igentuman.galacticresearch.util.Util;
import io.netty.buffer.ByteBuf;
import micdoodle8.mods.galacticraft.annotations.ForRemoval;
import micdoodle8.mods.galacticraft.annotations.ReplaceWith;
import micdoodle8.mods.galacticraft.api.entity.IDockable;
import micdoodle8.mods.galacticraft.api.galaxies.*;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntitySpaceshipBase;
import micdoodle8.mods.galacticraft.api.tile.IFuelDock;
import micdoodle8.mods.galacticraft.api.tile.ILandingPadAttachable;
import micdoodle8.mods.galacticraft.api.transmission.NetworkType;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseElectricBlockWithInventory;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import micdoodle8.mods.galacticraft.core.tile.TileEntityDish;
import micdoodle8.mods.galacticraft.core.tile.TileEntityLandingPad;
import micdoodle8.mods.galacticraft.core.tile.TileEntityMulti;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
import micdoodle8.mods.galacticraft.core.util.WorldUtil;
import micdoodle8.mods.galacticraft.core.world.gen.dungeon.MapGenDungeon;
import micdoodle8.mods.miccore.Annotations;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TileMissionControlStation extends TileBaseElectricBlockWithInventory implements ISidedInventory, ILandingPadAttachable, IComputerIntegration {

    private BlockPos dishPos;
    private int fetchCounter = 0;

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
    public String padCords = "";

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
    public String teleDishPos = "";

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
        if(!(worldIn.provider instanceof IGalacticraftWorldProvider)) return;
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
                    setMissionToPlayer(mission, player, cap);
                    CelestialBody p = GalaxyRegistry.getRegisteredPlanets().get(mission);
                    if(p instanceof Planet) {
                        if(ModConfig.researchSystem.research_moons_with_parent_planet) {
                            for(Moon m: GalaxyRegistry.getMoonsForPlanet((Planet) p)) {
                                setMissionToPlayer(m.getName(), player, cap);
                            }
                        }
                    }
                }
            }
            GalacticResearch.packetPipeline.sendTo(new GRPacketSimple(GRPacketSimple.EnumSimplePacket.SYNC_PLAYER_SPACE_DATA, GCCoreUtil.getDimensionID(player.world), new Object[] { cap.unlocked_missions }), (EntityPlayerMP) player);

        } catch (NullPointerException e) {
            GalacticResearch.instance.logger.log(Level.ERROR, "Analyze missions capability error");
        }
    }

    private void setMissionToPlayer(String mission, EntityPlayer player, PlayerSpaceData cap) {
        if (!cap.getUnlockedMissions().contains(mission)) {
            cap.addMission(mission);
            player.addExperience(10);
            player.sendMessage(new TextComponentTranslation("message.analyzed.planet", mission));
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
        for(String body: te.getResearchedBodiesArray()) {
            if(!missions.contains(body)) {
                if(missions.length() < 1) {
                    missions += body;

                } else {
                    missions += "," + body;
                }
            }
        }
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

    public boolean connectToPad(TileEntityMulti te)
    {
        TileEntity main = world.getTileEntity(te.mainBlockPosition);
        if(main instanceof TileEntityLandingPad) {
            ((TileEntityLandingPad) main).getConnectedTiles();
            if(attachedDock == null) setAttachedPad((IFuelDock) main);
            return true;
        }

        if(GalacticResearch.hooks.GalaxySpaceLoaded) {
            if(main instanceof TileEntityAdvLandingPad) {
                ((TileEntityAdvLandingPad) main).getConnectedTiles();
                if(attachedDock == null) setAttachedPad((IFuelDock) main);
                return true;
            }
        }

        if(GalacticResearch.hooks.ExtraPlanetsLoaded) {
            if(main instanceof TileEntityTier2LandingPad) {
                ((TileEntityTier2LandingPad) main).getConnectedTiles();
                if(attachedDock == null) setAttachedPad((IFuelDock) main);
                return true;
            }
        }

        return false;
    }

    public void updatePadConnection()
    {
        TileEntity te = world.getTileEntity(getPos().north(1));
        boolean tmpConnection;
        if(te instanceof TileEntityMulti) {
            tmpConnection = connectToPad((TileEntityMulti) te);
            if(tmpConnection) return;
        }
        te = world.getTileEntity(getPos().south(1));
        if(te instanceof TileEntityMulti) {
            tmpConnection = connectToPad((TileEntityMulti) te);
            if(tmpConnection) return;
        }
        te = world.getTileEntity(getPos().west(1));
        if(te instanceof TileEntityMulti) {
            tmpConnection = connectToPad((TileEntityMulti) te);
            if(tmpConnection) return;
        }
        te = world.getTileEntity(getPos().east(1));
        if(te instanceof TileEntityMulti) {
            tmpConnection = connectToPad((TileEntityMulti) te);
            if(tmpConnection) return;
        }
        attachedDock = null;
    }
    public int refreshCountdown = 40;

    public boolean hasTeleDish()
    {
        if(dishPos == null) return false;
        TileEntity te = world.getTileEntity(dishPos);
        return te instanceof TileEntityDish;
    }

    public void fetchSolarSystems()
    {
        if(!hasTeleDish()) return;
        fetchCounter++;
        if(fetchCounter > 200) {
            fetchCounter = 0;
            int lim = Math.min(4, GalacticResearch.skyModel.getCurrentSystemBodies(GCCoreUtil.getDimensionID(world)).size() / 2);
            if (missionsDataMap.size() < lim) return;
            Random r = new Random(ticks);
            for (SolarSystem sys : GalaxyRegistry.getRegisteredSolarSystems().values()) {
                if (!missionsDataMap.containsKey(sys.getName())) {
                    if (r.nextInt(100) < 5) {
                        missions += "," + sys.getName();
                        missionsDataMap.put(sys.getName(), -1);
                        serializeMissionData();
                    }
                }
            }
        }
    }

    public void update() {
        super.update();
        if(refreshCountdown > 0) {
            refreshCountdown--;
        } else {
            refreshCountdown = 40;
            updatePadConnection();
        }
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
            fetchSolarSystems();
            updateRocketState();
            removeAsteroidMissions(true);
            updateMissionsStateCounter();
        } else {
            unserializeMissionData();
            getLocatorProgress();
            handleMissionsOnClient();
        }
    }

    public void handleMissionsOnClient()
    {
        if(playSoundDelay <= 0) {
            List<String> tmpCompleted = completedMissions();
            if (!completedMissions.equals(tmpCompleted) && tmpCompleted.size() >= completedMissions.size()) {
                completedMissions = tmpCompleted;
                playMissionComplete(1f);
                return;
            } else if(tmpCompleted.size() < completedMissions.size()) {
                completedMissions = tmpCompleted;
            }

            List<String> tmpFailed = completedMissions();
            if (!failedMissions.equals(tmpFailed) && tmpFailed.size() >= failedMissions.size()) {
                failedMissions = tmpFailed;
                playMissionFailed(1f);
            } else if(tmpFailed.size() < failedMissions.size()) {
                failedMissions = tmpFailed;
            }
        }
    }

    public List<String> completedMissions = new ArrayList<>();

    public List<String> completedMissions()
    {
        List<String> tmp = new ArrayList<>();
        for(String mission: missionsDataMap.keySet()) {
            if(isMissionComplete(mission)) {
                tmp.add(mission);
            }
        }
        return tmp;
    }

    public List<String> failedMissions = new ArrayList<>();

    public List<String> failedMissions()
    {
        List<String> tmp = new ArrayList<>();
        for(String mission: missionsDataMap.keySet()) {
            if(getMissionInfo(mission) == -3) {
                tmp.add(mission);
            }
        }
        return tmp;
    }

    public void removeAsteroidMissions(boolean check)
    {
        for(String m: getMissions()) {
            if(!m.toUpperCase().contains("ASTEROID-")) continue;
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
            try {
                int v = missionsDataMap.get(s);
                if (s.toUpperCase().contains("ASTEROID-")) {
                    if (v == -3) continue;
                    if (GalacticResearch.spaceMineProvider.getMissions().size() == 0) {
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
            } catch (NullPointerException ignore) {

            }
        }
        serializeMissionData();
    }

    public boolean activateMission()
    {
        if(
                currentMission.isEmpty()
                || rocketState != 1
                || !missionsDataMap.containsKey(currentMission)) {
            return false;
        }
        IGRAutoRocket r = getRocket();
        if(r == null) {
            return false;
        }
        if(r instanceof IGRAutoRocket) {
            r.setMission(currentMission);
        }
        r.setAutolaunchSetting(EntityAutoRocket.EnumAutoLaunch.INSTANT);
        r.setLaunchPhase(EntitySpaceshipBase.EnumLaunchPhase.IGNITED);
        if(missionsDataMap.get(currentMission) < 0) {
            setMissionInfo(currentMission, 0);
        }
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
        if(currentMission.toUpperCase().contains("ASTEROID-") && !(r instanceof EntityMiningRocket)) {
            rocketState = -2;
        }

        if(!currentMission.toUpperCase().contains("ASTEROID-") && !(r instanceof EntitySatelliteRocket)) {
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
        return  new int[]{0};
    }

    public EnumFacing getElectricInputDirection() {
        return world.getBlockState(pos).getValue(BlockTelescope.FACING).getOpposite();
    }

    public boolean canConnect(EnumFacing direction, NetworkType type) {
        if (direction != null && type == NetworkType.POWER) {
            return getElectricalInputDirections().contains(direction);
        } else {
            return false;
        }
    }

    @Override
    public EnumSet<EnumFacing> getElectricalInputDirections() {
        EnumFacing facing = getWorld().getBlockState(getPos()).getValue(BlockTelescope.FACING);
        return EnumSet.of(facing.rotateY(), facing.rotateYCCW());
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
        if(state == -3) {
            return "gui.mission.fail";
        }
        return "gui.mission.progress";
    }
    int playSoundDelay = 0;
    @SideOnly(Side.CLIENT)
    public void playMissionComplete(float volume)
    {
        world.playSound((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), GRSounds.mcs_mission_complete, SoundCategory.BLOCKS, volume, 1, false);
        playSoundDelay = 40;
    }

    @SideOnly(Side.CLIENT)
    public void playMissionFailed(float volume)
    {
        world.playSound((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), GRSounds.mcs_mission_fail, SoundCategory.BLOCKS, volume, 1, false);
        playSoundDelay = 40;
    }

    public int getLocatorProgress()
    {
        if(playSoundDelay > 0) {
            playSoundDelay--;
        }
        if(locationCounter == -2) return 0;
        int progress =  (int) (((float)(ModConfig.locator.location_duration-locationCounter)/ModConfig.locator.location_duration)*100);
        if(world.isRemote && progress >= 99 && playSoundDelay <= 0) {
            playMissionComplete(0.5f);
        }
        return progress;
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
        this.padCords = nbt.getString("padCords");
        this.currentMission = nbt.getString("currentMission");
        this.missions = nbt.getString("missions");
        this.stations = nbt.getString("stations");
        this.missionsData = nbt.getString("missionsData");
        this.teleDishPos = nbt.getString("teleDishPos");
        String[] parts = teleDishPos.split(",");
        if(parts.length == 3) {
            dishPos = new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }
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
        nbt.setString("padCords", this.padCords);
        nbt.setString("currentMission", this.currentMission);
        nbt.setString("missions", this.missions);
        nbt.setString("missionsData", this.missionsData);
        nbt.setString("teleDishPos", this.teleDishPos);
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
        boolean flag = tile instanceof TileEntityLandingPad || isGSPadTile(tile) || isEPPadTile(tile);
        if(flag) {
            setAttachedPad((IFuelDock) tile);
        }
        return flag;
    }

    public BlockPos getPadCords()
    {
        if(padCords.isEmpty()) {
            return new BlockPos(0,0,0);
        }
        String[] cords = padCords.split(",");
        return new BlockPos(Integer.parseInt(cords[0]), Integer.parseInt(cords[1]), Integer.parseInt(cords[2]));
    }

    public void setAttachedPad(IFuelDock pad) {
        this.attachedDock = pad;
        BlockPos bp = ((TileEntity) attachedDock).getPos();
        padCords = bp.getX()+","+bp.getY()+","+bp.getZ();
    }

    private static boolean isGSPadTile(TileEntity tile) {
        boolean result = false;
        if(GalacticResearch.hooks.GalaxySpaceLoaded) {
            result = tile instanceof TileEntityAdvLandingPad;
        }
        return result;
    }

    private static boolean isEPPadTile(TileEntity tile) {
        boolean result = false;
        if(GalacticResearch.hooks.ExtraPlanetsLoaded) {
            result = tile instanceof TileEntityTier2LandingPad;
        }
        return result;
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
        } else if(isGSPadTile((TileEntity) attachedDock)) {
            TileEntityAdvLandingPad pad = ((TileEntityAdvLandingPad) attachedDock);
            IDockable rocket = pad.getDockedEntity();
            if (rocket instanceof IGRAutoRocket)
            {
                return (IGRAutoRocket) rocket;
            }
        } else if(isEPPadTile((TileEntity) attachedDock)) {
            TileEntityTier2LandingPad pad = ((TileEntityTier2LandingPad) attachedDock);
            IDockable rocket = pad.getDockedEntity();
            if (rocket instanceof IGRAutoRocket)
            {
                return (IGRAutoRocket) rocket;
            }
        } else if(!padCords.isEmpty()) {
            TileEntity dock = world.getTileEntity(getPadCords());
            if(dock instanceof TileEntityLandingPad) {
                setAttachedPad((IFuelDock) dock);
                return getRocket();
            }
        }
        return null;
    }

    public void bindTeleDish(int[] teledishPos) {
        this.teleDishPos = teledishPos[0]+","+teledishPos[1]+","+teledishPos[2];
        dishPos = new BlockPos(teledishPos[0], teledishPos[1], teledishPos[2]);
    }
}
