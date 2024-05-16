package igentuman.galacticresearch.ai.task.minions;

import com.mjr.extraplanets.entities.bosses.*;
import com.mjr.extraplanets.entities.mobs.EntityEvolvedMagmaCube;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSkeleton;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedZombie;
import net.minecraft.entity.EntityLiving;

import static igentuman.galacticresearch.GalacticResearch.hooks;

public class ExtraPlanetsMinions {
    public static EntityLiving spawn(EntityLiving entity) {
        if(entity instanceof EntityEvolvedMagmaCubeBoss) {
            return new EntityEvolvedMagmaCube(entity.world);
        }
        if(entity instanceof EntityEvolvedGiantZombieBoss) {
            return new EntityEvolvedZombie(entity.world);
        }
        if(entity instanceof EntityEvolvedFireBatBoss) {
            return new EntityEvolvedSkeleton(entity.world);
        }
        if(entity instanceof EntityEvolvedGhastBoss) {
            return new EntityEvolvedSkeleton(entity.world);
        }
        if(entity instanceof EntityEvolvedSpacemanBoss) {
            return new EntityEvolvedZombie(entity.world);
        }
        if(entity instanceof EntityEvolvedIceSlimeBoss) {
            return new EntityEvolvedSkeleton(entity.world);
        }
        return null;
    }
}
