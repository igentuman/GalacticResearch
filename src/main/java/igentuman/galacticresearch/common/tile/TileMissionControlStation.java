package igentuman.galacticresearch.common.tile;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.common.block.BlockTelescope;
import igentuman.galacticresearch.common.capability.ISpaceData;
import igentuman.galacticresearch.common.capability.PlayerSpaceData;
import igentuman.galacticresearch.common.capability.SpaceCapabilityHandler;
import igentuman.galacticresearch.common.data.DimensionProvider;
import igentuman.galacticresearch.common.entity.EntitySatelliteRocket;
import igentuman.galacticresearch.network.GRPacketSimple;
import micdoodle8.mods.galacticraft.annotations.ForRemoval;
import micdoodle8.mods.galacticraft.annotations.ReplaceWith;
import micdoodle8.mods.galacticraft.api.entity.IDockable;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntitySpaceshipBase;
import micdoodle8.mods.galacticraft.api.tile.IFuelDock;
import micdoodle8.mods.galacticraft.api.tile.ILandingPadAttachable;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseElectricBlockWithInventory;
import micdoodle8.mods.galacticraft.core.tile.TileEntityLandingPad;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;

import java.util.Arrays;
import java.util.HashMap;

public class TileMissionControlStation extends TileBaseElectricBlockWithInventory implements ISidedInventory, ILandingPadAttachable {

    @Annotations.NetworkedField(
            targetSide = Side.CLIENT
    )
    public int dimension;

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
    }

    public static String removeLastChar(String s) {
        return (s == null || s.length() == 0)
                ? ""
                : (s.substring(0, s.length() - 1));
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
            String tmp = "";
            for (String mission : getMissions()) {
                if (isMissionComplete(mission)) {
                    tmp += mission + ",";
                    if (!cap.getUnlockedMissions().contains(mission)) {
                        cap.addMission(mission);
                        player.addExperience(10);
                        String planetLocalized = I18n.format("gui."+mission+".name");
                        player.sendMessage(new TextComponentString(I18n.format("message.analyzed.planet", planetLocalized)));
                    }
                }
            }
            cap.unlocked_missions = removeLastChar(tmp);
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
    public boolean canExtractItem(int slotID, ItemStack itemstack, EnumFacing side)
    {
        return slotID == 0;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
    {
        return slotID == 0 && ItemElectricBase.isElectricItem(itemstack.getItem());
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
            fetchMissions();
            updateRocketState();
            updateMissionsStateCounter();
        } else {
            unserializeMissionData();
        }
    }

    public void updateMissionsStateCounter()
    {
        for(String s: missionsDataMap.keySet()) {
            int v = missionsDataMap.get(s);
            if(v > 0 && v < ModConfig.machines.satellite_mission_duration*20) {
                v++;
                missionsDataMap.replace(s, v);
            }
        }
        if(!world.isRemote) serializeMissionData();
    }

    public void activateMission()
    {
        EntityAutoRocket r = getRocket();
        if(r == null) {
            return;
        }
        if(r instanceof EntitySatelliteRocket) {
            ((EntitySatelliteRocket) r).setMission(currentMission);
        }
        r.autoLaunchSetting = EntityAutoRocket.EnumAutoLaunch.INSTANT;

        r.setLaunchPhase(EntitySpaceshipBase.EnumLaunchPhase.IGNITED);
        setMissionInfo(currentMission, 0);
    }

    public int getMissonPercent(String name)
    {
        int i = getMissionInfo(name);
        return (int)((double)i/(ModConfig.machines.satellite_mission_duration*20)*100);
    }

    public void updateRocketState()
    {
        EntityAutoRocket r = getRocket();
        if(r == null) {
            rocketState = -1;
            return;
        }
        if(r.fuelTank.getFluidAmount() >= r.fuelTank.getCapacity()/2) {
            rocketState = 1;
        } else {
            rocketState = 0;
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

    public int[] getSlotsForFace(EnumFacing side) {
        return side != this.getElectricInputDirection() ? new int[]{0} : new int[0];
    }

    public EnumFacing getElectricInputDirection() {
        return world.getBlockState(pos).getValue(BlockTelescope.FACING).getOpposite();
    }

    public void serializeMissionData()
    {
        String tmp = "";
        for(String m: missionsDataMap.keySet()) {
            tmp += m+":"+missionsDataMap.get(m)+";";
        }
        missionsData = tmp;
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

    public void unserializeMissionData()
    {
        missionsDataMap.clear();
        String[] missions = missionsData.split(";");
        for(String m: missions) {
            String[] p = m.split(":");
            if(p.length < 2) continue;
            missionsDataMap.put(p[0], Integer.valueOf(p[1]));
        }
    }

    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.dimension = nbt.getInteger("dimension");
        this.currentMission = nbt.getString("currentMission");
        this.missions = nbt.getString("missions");
        this.missionsData = nbt.getString("missionsData");
        unserializeMissionData();
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        serializeMissionData();
        nbt.setInteger("dimension", this.dimension);
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

    public EntityAutoRocket getRocket()
    {
        if (attachedDock instanceof TileEntityLandingPad)
        {
            TileEntityLandingPad pad = ((TileEntityLandingPad) attachedDock);
            IDockable rocket = pad.getDockedEntity();
            if (rocket instanceof EntityAutoRocket)
            {
                return (EntityAutoRocket) rocket;
            }
        }
        return null;
    }
}
