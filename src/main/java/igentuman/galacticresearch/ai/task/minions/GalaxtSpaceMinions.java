package igentuman.galacticresearch.ai.task.minions;

import galaxyspace.core.prefab.entities.EntityEvolvedColdBlaze;
import galaxyspace.systems.SolarSystem.moons.io.entities.EntityBossGhast;
import galaxyspace.systems.SolarSystem.planets.ceres.entities.EntityBossBlaze;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSkeleton;
import net.minecraft.entity.EntityLiving;

public class GalaxtSpaceMinions {
    public static EntityLiving spawn(EntityLiving entity) {
        if(entity instanceof EntityBossBlaze) {
            return new EntityEvolvedColdBlaze(entity.world);
        }
        if(entity instanceof EntityBossGhast) {
            return new EntityEvolvedSkeleton(entity.world);
        }
        return null;
    }
}
