package igentuman.galacticresearch.ai.task;

import com.google.common.base.Predicates;
import igentuman.galacticresearch.ai.task.minions.ExtraPlanetsMinions;
import igentuman.galacticresearch.ai.task.minions.GalaxtSpaceMinions;
import micdoodle8.mods.galacticraft.core.entities.*;
import micdoodle8.mods.galacticraft.planets.mars.entities.EntityCreeperBoss;
import micdoodle8.mods.galacticraft.planets.venus.entities.EntitySpiderQueen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.BlockPos;

import static igentuman.galacticresearch.GalacticResearch.hooks;

public class EntityAISpawnMinions extends EntityAIBase {
    protected EntityLiving entity;
    protected Entity closestEntity;
    protected float maxDistance;
    private int counter = 10;
    protected Class<? extends Entity> watchedClass;


    public EntityAISpawnMinions(EntityLiving entityIn, Class<? extends Entity> watchTargetClass, float maxDistance) {
        this.entity = entityIn;
        this.watchedClass = watchTargetClass;
        this.maxDistance = maxDistance;
        this.setMutexBits(2);
    }

    public EntityAISpawnMinions(EntityLiving entityIn, Class<? extends Entity> watchTargetClass, float maxDistance, float chanceIn) {
        this.entity = entityIn;
        this.watchedClass = watchTargetClass;
        this.maxDistance = maxDistance;
        this.setMutexBits(2);
    }

    @Override
    public boolean shouldExecute() {
        counter--;
        if(counter <= 0) {
            counter = Math.max(entity.world.rand.nextInt(100), 60);
            if (this.entity.getAttackTarget() != null) {
                this.closestEntity = this.entity.getAttackTarget();
            }

            if (this.watchedClass == EntityPlayer.class) {
                this.closestEntity = this.entity.world.getClosestPlayer(this.entity.posX, this.entity.posY, this.entity.posZ, (double) this.maxDistance, Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.notRiding(this.entity)));
            } else {
                this.closestEntity = this.entity.world.findNearestEntityWithinAABB(this.watchedClass, this.entity.getEntityBoundingBox().grow((double) this.maxDistance, 3.0D, (double) this.maxDistance), this.entity);
            }
            return  this.closestEntity != null;
        }
        return false;
    }

    private void spawnMobs() {
        for(int i = 0; i < 1 + entity.world.rand.nextInt(2); i++) {
            EntityLiving mob = null;
            if(entity instanceof EntitySkeletonBoss) {
                mob = new EntityEvolvedSkeleton(entity.world);
            } else if(entity instanceof EntityCreeperBoss) {
                mob = new EntityEvolvedCreeper(entity.world);
            } else if(entity instanceof EntitySpiderQueen) {
                mob = new EntityEvolvedSpider(entity.world);
            }
            if(entity == null) return;
            if(mob == null && hooks.ExtraPlanetsLoaded) {
                mob = ExtraPlanetsMinions.spawn(entity);
            }
            if(mob == null && hooks.GalaxySpaceLoaded) {
                mob = GalaxtSpaceMinions.spawn(entity);
            }
            if(mob == null) {
                mob = new EntityEvolvedZombie(entity.world);
            }
            float range = 3F;
            mob.setPosition(entity.posX + 0.5 + Math.random() * range - range / 2, entity.posY + 1, entity.posZ + 0.5 + Math.random() * range - range / 2);
            mob.onInitialSpawn(entity.world.getDifficultyForLocation(new BlockPos(mob)), null);
            mob.setAttackTarget((EntityLivingBase) closestEntity);
            mob.forceSpawn = true;
            EntityAIBase task = null;
            for(EntityAITasks.EntityAITaskEntry t: mob.targetTasks.taskEntries) {
                if(t.action instanceof EntityAIHurtByTarget) {
                    task = t.action;
                    break;
                }
            }
            if(task != null)
                mob.targetTasks.removeTask(task);//do not attack each other

            if(mob instanceof EntityEvolvedSkeleton) {
                mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));

            }
            closestEntity.world.spawnEntity(mob);
        }
    }

    public void updateTask() {
        spawnMobs();
    }
}
