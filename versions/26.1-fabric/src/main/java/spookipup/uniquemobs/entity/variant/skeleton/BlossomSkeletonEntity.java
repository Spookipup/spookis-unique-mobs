package spookipup.uniquemobs.entity.variant.skeleton;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import spookipup.uniquemobs.entity.BlossomHelper;
import spookipup.uniquemobs.entity.ai.ShootGoal;
import spookipup.uniquemobs.entity.ai.StrafeAndRetreatGoal;
import spookipup.uniquemobs.entity.projectile.BlossomArrowEntity;

public class BlossomSkeletonEntity extends Skeleton {

	private int blossomFlightTicks;
	private int blossomFlightTargetId = -1;
	private int blossomFlightCooldown;

	public BlossomSkeletonEntity(EntityType<? extends Skeleton> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Skeleton.createAttributes()
			.add(Attributes.MAX_HEALTH, 22.0)
			.add(Attributes.FOLLOW_RANGE, 48.0);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();

		goalSelector.removeAllGoals(goal ->
			goal instanceof RangedBowAttackGoal ||
			goal instanceof MeleeAttackGoal
		);

		goalSelector.addGoal(3, new StrafeAndRetreatGoal(this, 0.75, 5.0F, 9.0F, 24.0F));
		goalSelector.addGoal(3, new ShootGoal(this, 35, 60, 24.0F, 3, true, false,
			ShootGoal.simple((level, shooter) -> new BlossomArrowEntity(level, shooter), 1.55F, 3.0F)
		));
		goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.1, false));
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
					BlossomHelper.tickSmoothFlight(serverLevel, this, target, 7.5, 0.14, 0.16, 1.15, 0.28);
				} else {
					stopBlossomFlight();
				}

				if (blossomFlightTicks <= 0) {
					stopBlossomFlight();
				}
			} else {
				LivingEntity target = getTarget();
				if (target != null && BlossomHelper.shouldStartReachFlight(this, target, blossomFlightCooldown)) {
					startBlossomFlight(target, 90);
				}
			}
		}
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit && target instanceof LivingEntity living) {
			onBlossomAttackLanded(living);
			BlossomHelper.applyUpwardKnockback(living, this, 0.42, 0.6);
		}
		return hit;
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}

	public void onBlossomAttackLanded(LivingEntity target) {
		if (blossomFlightTicks <= 0 && blossomFlightCooldown > 0) return;
		startBlossomFlight(target, 120);
	}

	private void startBlossomFlight(LivingEntity target, int duration) {
		blossomFlightTargetId = target.getId();
		blossomFlightTicks = duration;
		blossomFlightCooldown = Math.max(blossomFlightCooldown, duration + 55);
		setNoGravity(true);
		if (level() instanceof ServerLevel serverLevel) {
			BlossomHelper.spawnPetals(serverLevel, this, 16, 0.035);
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

	@Override
	public void reassessWeaponGoal() {
	}

	@Override
	public ItemStack getWeaponItem() {
		return new ItemStack(Items.BOW);
	}
}
