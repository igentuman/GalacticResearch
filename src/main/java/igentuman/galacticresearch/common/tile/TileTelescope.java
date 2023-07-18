package igentuman.galacticresearch.common.tile;

import igentuman.galacticresearch.GalacticResearch;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.common.block.BlockTelescope;
import igentuman.galacticresearch.integration.computer.IComputerIntegration;
import igentuman.galacticresearch.sky.SkyModel;
import igentuman.galacticresearch.sky.body.Researchable;
import igentuman.galacticresearch.util.WorldUtil;
import micdoodle8.mods.galacticraft.annotations.ForRemoval;
import micdoodle8.mods.galacticraft.annotations.ReplaceWith;
import micdoodle8.mods.galacticraft.api.transmission.NetworkType;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseElectricBlockWithInventory;
import micdoodle8.mods.miccore.Annotations;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

public class TileTelescope extends TileBaseElectricBlockWithInventory implements ISidedInventory, IComputerIntegration {

    public static int viewportSize = 112;
    
    @Override
    public String[] getMethods() {
        return new String[]{"getComponentName", "getObservationStatus", "getResearchedBodies", "rotateTelescope"};
    }

    @Override
    public Object[] invoke(int method, Object[] args) throws NoSuchMethodException {
        switch (method) {
            case 0:
                return new Object[] {getComponentName()};
            case 1:
                return getObservationStatus();
            case 2:
                return new Object[] {getResearchedBodies().split(",")};
            case 3:
                return rotateTelescope(args);
            default:
                throw new NoSuchMethodException();
        }
    }

    public String getComponentName()
    {
        return "telescope";
    }

    public Object[] getObservationStatus()
    {
        HashMap<String, Object> data = new HashMap<>();
        data.put("object", curObserveBody);
        data.put("relative_x", curBodyRelativeX);
        data.put("relative_y", curBodyRelativeY);
        data.put("progress", getObservationProgress());
        return new Object[] {data};
    }

    public Object[] rotateTelescope(Object[] args)
    {
        deltaX = (new Double(args[0].toString())).intValue();
        deltaY = (new Double(args[1].toString())).intValue();
        return new Object[] {true};
    }

    @Annotations.NetworkedField(targetSide = Side.CLIENT)
    public float xAngle = viewportSize;

    @Annotations.NetworkedField(targetSide = Side.CLIENT)
    public float yAngle = viewportSize;

    @Annotations.NetworkedField(targetSide = Side.CLIENT)
    public int dimension;

    @Annotations.NetworkedField(targetSide = Side.CLIENT)
    public int movementAmplifier = 1;

    @Annotations.NetworkedField(targetSide = Side.CLIENT)
    public int observationTime = 0;

    @Annotations.NetworkedField(targetSide = Side.CLIENT)
    public String curObserveBody = "";

    @Annotations.NetworkedField(targetSide = Side.CLIENT)
    public float curBodyRelativeY = 0;

    @Annotations.NetworkedField(targetSide = Side.CLIENT)
    public float curBodyRelativeX = 0;

    @Annotations.NetworkedField(targetSide = Side.CLIENT)
    public String researchedBodies = "";

    public int operationCooldown = 10;
    private float deltaX;
    private float deltaY;

    public TileTelescope() {
        super("container.telescope.name");
        this.storage.setMaxExtract(45.0F);
        this.inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    }


    public void rotateX(int x)
    {
        deltaX = x*10*movementAmplifier;
    }

    public void rotateY(int y)
    {
        deltaY = y*10*movementAmplifier;
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
    public List<Integer> multipliers = Arrays.asList(1,2,5,10);
    public void changeMultiplier()
    {
        int i = multipliers.indexOf(movementAmplifier);
        i++;
        if(i>multipliers.size()-1) {
            i=0;
        }
        movementAmplifier = multipliers.get(i);
    }

    public void doMovement()
    {
        if(operationCooldown > 0) {
            operationCooldown--;
            return;
        }

        if (deltaX != 0) {
            if (deltaX > 0) {
                xAngle += movementAmplifier;
                deltaX-= movementAmplifier;
            } else {
                xAngle -= movementAmplifier;
                deltaX+= movementAmplifier;
            }
            xAngle = (int) Math.min(SkyModel.width - viewportSize*1.5, Math.max(viewportSize/2, xAngle));
        }
        if (deltaY != 0) {
            if (deltaY > 0) {
                yAngle += movementAmplifier;
                deltaY-= movementAmplifier;
            } else {
                yAngle -= movementAmplifier;
                deltaY+= movementAmplifier;
            }
            yAngle = (int) Math.min(SkyModel.height - viewportSize*1.5, Math.max(viewportSize/2, yAngle));
        }

        if(deltaX == 0 & deltaY == 0) {
            operationCooldown = 10;
        }
    }

    public boolean isBodyResearched(String name)
    {
        return Arrays.stream(researchedBodies.split(",")).anyMatch(val -> val.equals(name));
    }
    private int looseCounter = 100;

    public boolean looseCounterEnd()
    {
        observationTime--;
        looseCounter--;
        return looseCounter < 1;
    }

    public boolean isBodyVisible(Researchable res, float viewPortX, float viewPortY)
    {
        return  res.getX()+res.getSize()>xAngle && res.getY()+res.getSize()>yAngle &&
                res.getY()<yAngle+viewPortY && res.getX()<xAngle+viewPortX;
    }

    public void observe()
    {
        List<Researchable> bodies = SkyModel.get().getCurrentSystemBodies(dimension);
        if(bodies == null) return;
        for (Researchable b: bodies) {
            if(Arrays.stream(ModConfig.researchSystem.default_researched_objects).anyMatch(s -> s.equals(b.getName()))) {
                continue;
            }
            float x = b.getX();
            float y = b.getY();
            int padding = 25;
            if(
                    x+b.getSize()/2 > xAngle + padding && x < (xAngle + viewportSize-padding-b.getSize()) &&
                    y+b.getSize()/2 > yAngle + padding && y < (yAngle + viewportSize-padding-b.getSize())
               ) {
                if(!isBodyResearched(b.getName())) {
                    if(!curObserveBody.equals(b.getName())) {
                        observationTime = 0;
                        curObserveBody = b.getName();
                        looseCounter = 100;
                    }
                    curBodyRelativeY = y-(yAngle+viewportSize/2-b.getSize()/2);
                    curBodyRelativeX = x-(xAngle+viewportSize/2-b.getSize()/2);

                    observationTime++;
                    if(observationTime/20 >= ModConfig.researchSystem.required_observation_time) {
                        if(researchedBodies.length() < 1) {
                            researchedBodies += curObserveBody;
                        } else {
                            researchedBodies += "," + curObserveBody;
                        }
                        observationTime = 0;
                        curObserveBody = "";
                        curBodyRelativeY = 0;
                        curBodyRelativeX = 0;
                        markDirty();
                    }
                    return;
                }
            }
        }
        if(looseCounterEnd()) {
            observationTime = 0;
            curObserveBody = "";
            curBodyRelativeY = 0;
            curBodyRelativeX = 0;
        }
    }

    public int getObservationProgress()
    {
        if(curObserveBody.isEmpty()) {
            observationTime = 0;
            return 0;
        }
        return (int)(100/(float)ModConfig.researchSystem.required_observation_time*((float)observationTime/20));
    }

    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            if(getEnergyStoredGC() < 100) {
                return;
            }
            doMovement();
            if(!getWorld().isRaining() ||
                !getWorld().getBiome(getPos()).canRain()) {
                if(world.canSeeSky(getPos())) {
                    observe();
                }
            }
            IBlockState st = world.getBlockState(getPos());
            BlockTelescope be = (BlockTelescope) st.getBlock();
            be.updateCmp(world, pos, st);
            clearAsteroids();
        }
    }

    public void deleteResearch(String name)
    {
        StringBuilder tmp = new StringBuilder();
        for (String r: getResearchedBodiesArray()) {
            if(!r.equals(name)) {
                tmp.append(r).append(",");
            }
        }
        researchedBodies = tmp.toString();
        markDirty();
    }

    public void clearAsteroids()
    {
        for (String m: getResearchedBodiesArray()) {
            if(!m.toUpperCase().contains("ASTEROID-")) continue;
            if(!GalacticResearch.spaceMineProvider.getMissions().containsKey(m)) {
                deleteResearch(m);
            }
        }
    }

    public String[] getResearchedBodiesArray()
    {
        return Arrays.stream(researchedBodies.split(",")).filter(val -> !val.isEmpty()).toArray(String[]::new);
    }

    public String getResearchedBodies()
    {
        return researchedBodies;
    }

    public boolean shouldUseEnergy() {
        return !this.getDisabled(0);
    }

    public int[] getSlotsForFace(EnumFacing side) {
        return  new int[]{0};
    }

    public boolean canConnect(EnumFacing direction, NetworkType type) {
        if (direction != null && type == NetworkType.POWER) {
            return getElectricalInputDirections().contains(direction);
        } else {
            return false;
        }
    }

    public EnumFacing getElectricInputDirection() {
        return world.getBlockState(pos).getValue(BlockTelescope.FACING).getOpposite();
    }

    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.dimension = nbt.getInteger("dimension");
        this.xAngle = nbt.getFloat("xAngle");
        this.yAngle = nbt.getFloat("yAngle");
        this.movementAmplifier = nbt.getInteger("movementAmplifier");
        this.operationCooldown = nbt.getInteger("operationCooldown");
        this.observationTime = nbt.getInteger("observationTime");
        this.researchedBodies = nbt.getString("researchedBodies");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("dimension", this.dimension);
        nbt.setFloat("xAngle", this.xAngle);
        nbt.setFloat("yAngle", this.yAngle);
        nbt.setInteger("movementAmplifier", this.movementAmplifier);
        nbt.setInteger("operationCooldown", this.operationCooldown);
        nbt.setInteger("observationTime", this.observationTime);
        nbt.setString("researchedBodies", this.researchedBodies);
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
    public EnumSet<EnumFacing> getElectricalInputDirections() {
        EnumFacing facing = getWorld().getBlockState(getPos()).getValue(BlockTelescope.FACING);
        return EnumSet.of(facing.rotateY(), facing.rotateYCCW());
    }
}
