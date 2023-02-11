package igentuman.galacticresearch.reflection;

import micdoodle8.mods.galacticraft.core.tile.TileEntityDungeonSpawner;
import net.minecraft.entity.EntityLiving;

import java.util.ArrayList;
import java.util.List;

public class TileEntityDungeonSpawnerReflector {
    public static List<Class<? extends EntityLiving>> getDisabledCreatures(TileEntityDungeonSpawner instance)
    {
        List<Class<? extends EntityLiving>> list = new ArrayList<Class<? extends EntityLiving>>();
        return list;
    }
}
