package igentuman.galacticresearch.ai.task;

import com.google.common.base.Predicates;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedCreeper;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSkeleton;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSpider;
import micdoodle8.mods.galacticraft.core.entities.EntitySkeletonBoss;
import micdoodle8.mods.galacticraft.planets.mars.entities.EntityCreeperBoss;
import micdoodle8.mods.galacticraft.planets.venus.entities.EntitySpiderQueen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class EntityAISpawnWeb extends EntityAIBase {
    protected EntityLiving entity;
    protected Entity closestEntity;
    protected float maxDistance;
    private int counter = 10;
    protected Class<? extends Entity> watchedClass;


    public EntityAISpawnWeb(EntityLiving entityIn, Class<? extends Entity> watchTargetClass, float maxDistance) {
        this.entity = entityIn;
        this.watchedClass = watchTargetClass;
        this.maxDistance = maxDistance;
        this.setMutexBits(2);
    }

    public EntityAISpawnWeb(EntityLiving entityIn, Class<? extends Entity> watchTargetClass, float maxDistance, float chanceIn) {
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

    private void placeWeb() {
        Random r = closestEntity.world.rand;
        for(int i = 0; i < r.nextInt(6); i++) {
            switch (i) {
                case 1:
                    closestEntity.world.setBlockState(closestEntity.getPosition().offset(EnumFacing.NORTH,1), Blocks.WEB.getDefaultState());
                case 2:
                    closestEntity.world.setBlockState(closestEntity.getPosition().offset(EnumFacing.SOUTH,1), Blocks.WEB.getDefaultState());
                case 3:
                    closestEntity.world.setBlockState(closestEntity.getPosition().offset(EnumFacing.WEST,1), Blocks.WEB.getDefaultState());
                case 4:
                    closestEntity.world.setBlockState(closestEntity.getPosition().offset(EnumFacing.EAST,1), Blocks.WEB.getDefaultState());
                default:
                    closestEntity.world.setBlockState(closestEntity.getPosition(), Blocks.WEB.getDefaultState());
            }
            if(i > 2) {
                ((EntityPlayer) closestEntity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,50, 1));
            }
            if(i > 3) {
                ((EntityPlayer) closestEntity).addPotionEffect(new PotionEffect(MobEffects.POISON,50, 1));
            }
        }
    }

    public void updateTask() {
        placeWeb();
    }
}
