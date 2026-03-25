package spookipup.uniquemobs.entity.variant.zombie;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;

// poison on hit, death cloud
public class VenomousZombieEntity extends Zombie {

	private static final int MELEE_POISON_DURATION = 60;
	private static final int MELEE_POISON_AMPLIFIER = 1;
	private static final int CLOUD_POISON_DURATION = 40;
	private static final float CLOUD_RADIUS = 2.5F;
	private static final int CLOUD_LINGER_DURATION = 40;

	public VenomousZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes()
			.add(Attributes.MOVEMENT_SPEED, 0.26);
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(4) == 0) {
			this.level().addParticle(
				ParticleTypes.ITEM_SLIME,
				this.getRandomX(0.5), this.getRandomY() - 0.2, this.getRandomZ(0.5),
				0.0, -0.05, 0.0
			);
		}
	}

	@Override
	public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
		boolean hit = super.doHurtTarget(serverLevel, target);

		if (hit && target instanceof LivingEntity livingTarget) {
			livingTarget.addEffect(new MobEffectInstance(
				MobEffects.POISON, MELEE_POISON_DURATION, MELEE_POISON_AMPLIFIER
			));
		}

		return hit;
	}

	@Override
	public void die(DamageSource damageSource) {
		super.die(damageSource);

		if (!this.level().isClientSide()) {
			AreaEffectCloud cloud = new AreaEffectCloud(
				this.level(), this.getX(), this.getY(), this.getZ()
			);
			cloud.setRadius(CLOUD_RADIUS);
			cloud.setRadiusOnUse(-0.5F);
			cloud.setRadiusPerTick(-0.02F);
			cloud.setDuration(CLOUD_LINGER_DURATION);
			cloud.setWaitTime(0);
			cloud.addEffect(new MobEffectInstance(MobEffects.POISON, CLOUD_POISON_DURATION, 0));
			cloud.setOwner(this);
			cloud.setCustomParticle(ParticleTypes.ITEM_SLIME);

			this.level().addFreshEntity(cloud);
		}
	}
}
