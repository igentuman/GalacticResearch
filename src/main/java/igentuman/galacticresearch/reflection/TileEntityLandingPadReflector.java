package igentuman.galacticresearch.reflection;

import igentuman.galacticresearch.common.tile.TileMissionControlStation;
import micdoodle8.mods.galacticraft.api.tile.ILandingPadAttachable;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.tile.TileEntityLandingPad;
import micdoodle8.mods.galacticraft.planets.mars.tile.TileEntityLaunchController;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;

public class TileEntityLandingPadReflector {

    public static void testConnectedTile(TileEntityLandingPad instance, int x, int z, HashSet<ILandingPadAttachable> connectedTiles) {

        BlockPos testPos = new BlockPos(x, instance.getPos().getY(), z);
        if (!instance.getWorld().isBlockLoaded(testPos, false))
            return;

        TileEntity tile = instance.getWorld().getTileEntity(testPos);

        if (tile instanceof ILandingPadAttachable && ((ILandingPadAttachable) tile).canAttachToLandingPad(instance.getWorld(), instance.getPos()))
        {
            connectedTiles.add((ILandingPadAttachable) tile);
            if (GalacticraftCore.isPlanetsLoaded && tile instanceof TileEntityLaunchController)
            {
                ((TileEntityLaunchController) tile).setAttachedPad(instance);
            }
            if(tile instanceof TileMissionControlStation) {
                ((TileMissionControlStation) tile).setAttachedPad(instance);

            }
        }
    }
}
