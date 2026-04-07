package spookipup.uniquemobs.entity.variant.zombie;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import spookipup.uniquemobs.entity.BlossomHelper;
import spookipup.uniquemobs.registry.ModEffects;

public class BlossomZombieEntity extends Zombie {

	private int blossomFlightTicks;
	private int blossomFlightTargetId = -1;
	private int blossomFlightCooldown;

	public BlossomZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes()
			.add(Attributes.MAX_HEALTH, 24.0)
			.add(Attributes.MOVEMENT_SPEED, 0.22)
			.add(Attributes.FOLLOW_RANGE, 40.0);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		BlossomHelper.tickNaturalSlowFall(this);

		if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
			if (blossomFlightCooldown > 0) blossomFlightCooldown--;

			if (blossomFlightTicks > 0) {
				blossomFlightTicks--;
				LivingEntity target = getBlossomFlightTarget(serverLevel);
				if (target != null) {
					BlossomHelper.tickSmoothFlight(serverLevel, this, target, 1.35, 0.22, 0.02, 0.2, 0.38);
				} else {
					stopBlossomFlight();
				}

				if (blossomFlightTicks <= 0) {
					stopBlossomFlight();
				}
			} else {
				LivingEntity target = getTarget();
				if (target != null && BlossomHelper.shouldStartReachFlight(this, target, blossomFlightCooldown)) {
					startBlossomFlight(target, 80);
				}
			}
		}
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean hit = super.doHurtTarget(target);
		if (hit && target instanceof LivingEntity living) {
			if (living.hasEffect(ModEffects.BLOSSOM_DRIFT)) {
				living.removeEffect(ModEffects.BLOSSOM_DRIFT);
				living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 3), this);
				if (level() instanceof ServerLevel serverLevel) {
					BlossomHelper.spawnPetalBurst(serverLevel, living.getX(), living.getY(0.2), living.getZ(), 1.0, 22);
				}
				playSound(SoundEvents.CHERRY_LEAVES_BREAK, 0.9F, 0.8F);
			} else {
				BlossomHelper.applyPetalMark(living, this, 1);
			}
			BlossomHelper.applyUpwardKnockback(living, this, 0.55, 0.72);
			startBlossomFlight(living, 100);
		}
		return hit;
	}

	@Override
	public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}

	private void startBlossomFlight(LivingEntity target, int duration) {
		blossomFlightTargetId = target.getId();
		blossomFlightTicks = duration;
		blossomFlightCooldown = Math.max(blossomFlightCooldown, duration + 45);
		setNoGravity(true);
		if (level() instanceof ServerLevel serverLevel) {
			BlossomHelper.spawnPetals(serverLevel, this, 18, 0.04);
		}
	}

	private void stopBlossomFlight() {
		blossomFlightTicks = 0;
		blossomFlightTargetId = -1;
		setNoGravity(false);
	}

	private LivingEntity getBlossomFlightTarget(ServerLevel serverLevel) {
		Entity entity = serverLevel.getEntity(blossomFlightTargetId);
		if (entity instanceof LivingEntity living && living.isAlive()) return living;
		LivingEntity target = getTarget();
		return target != null && target.isAlive() ? target : null;
	}
}
