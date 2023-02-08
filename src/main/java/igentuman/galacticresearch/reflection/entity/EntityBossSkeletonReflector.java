package igentuman.galacticresearch.reflection.entity;

import igentuman.galacticresearch.ModConfig;
import micdoodle8.mods.galacticraft.core.Constants;
import micdoodle8.mods.galacticraft.core.entities.EntitySkeletonBoss;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;

public class EntityBossSkeletonReflector {

    public static void updatePassenger(EntitySkeletonBoss instance, Entity passenger)
    {
        if(!ModConfig.tweaks.hard_boss_fight) {
            return;
        }
        if (instance.isPassenger(passenger))
        {
            if(passenger instanceof EntityPlayer && !passenger.world.isRemote) {
                ((EntityPlayer) passenger).addPotionEffect(new PotionEffect(MobEffects.WITHER,50, 1));
                ((EntityPlayer) passenger).addPotionEffect(new PotionEffect(MobEffects.BLINDNESS,150, 1));
                ((EntityPlayer) passenger).attackEntityFrom(DamageSource.FALL,0.1f);
                instance.heal(0.5f);
            }
            double offsetX = Math.sin(-instance.rotationYawHead / Constants.RADIANS_TO_DEGREES_D);
            double offsetZ = Math.cos(instance.rotationYawHead / Constants.RADIANS_TO_DEGREES_D);
            double offsetY = 2 * Math.cos((instance.throwTimer + instance.postThrowDelay) * 0.05F);

            passenger.setPosition(instance.posX + offsetX, instance.posY + instance.getMountedYOffset() + passenger.getYOffset() + offsetY, instance.posZ + offsetZ);
        }
    }

    public static void attackEntityWithRangedAttack(EntitySkeletonBoss instance, EntityLivingBase target, float f) {
        if(!ModConfig.tweaks.hard_boss_fight) {
            return;
        }

        if (instance.getPassengers().isEmpty()) {
            EntityTippedArrow arrow = new EntityTippedArrow(instance.world, instance);
            for(int i = 0; i < 2; i++) {
                double d0 = target.posX - instance.posX;
                double d1 = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - arrow.posY;
                double d2 = target.posZ - instance.posZ;
                double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
                arrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float) (14 - instance.world.getDifficulty().getId() * 4));
                instance.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (instance.getRNG().nextFloat() * 0.4F + 0.8F));
                instance.world.spawnEntity(arrow);
            }
        }
    }

}
