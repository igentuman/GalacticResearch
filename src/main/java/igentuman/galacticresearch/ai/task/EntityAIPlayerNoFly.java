package igentuman.galacticresearch.ai.task;

import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;

public class EntityAIPlayerNoFly extends EntityAIBase {
    protected EntityLiving entity;
    protected Entity closestEntity;
    protected float maxDistance;
    private int lookTime;
    private final float chance;
    protected Class<? extends Entity> watchedClass;


    public EntityAIPlayerNoFly(EntityLiving entityIn, Class<? extends Entity> watchTargetClass, float maxDistance) {
        this.entity = entityIn;
        this.watchedClass = watchTargetClass;
        this.maxDistance = maxDistance;
        this.chance = 0.02F;
        this.setMutexBits(2);
    }

    public EntityAIPlayerNoFly(EntityLiving entityIn, Class<? extends Entity> watchTargetClass, float maxDistance, float chanceIn) {
        this.entity = entityIn;
        this.watchedClass = watchTargetClass;
        this.maxDistance = maxDistance;
        this.chance = chanceIn;
        this.setMutexBits(2);
    }

    @Override
    public boolean shouldExecute() {
        if (this.entity.getAttackTarget() != null) {
            this.closestEntity = this.entity.getAttackTarget();
        }

        if (this.watchedClass == EntityPlayer.class) {
            this.closestEntity = this.entity.world.getClosestPlayer(this.entity.posX, this.entity.posY, this.entity.posZ, (double)this.maxDistance, Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.notRiding(this.entity)));
        } else {
            this.closestEntity = this.entity.world.findNearestEntityWithinAABB(this.watchedClass, this.entity.getEntityBoundingBox().grow((double)this.maxDistance, 3.0D, (double)this.maxDistance), this.entity);
        }
        return  this.closestEntity != null;
    }

    public void updateTask() {
        EntityPlayer p = (EntityPlayer) this.closestEntity;
        p.capabilities.isFlying &= p.capabilities.isCreativeMode;
        p.capabilities.allowFlying &= p.capabilities.isCreativeMode;
    }
}
