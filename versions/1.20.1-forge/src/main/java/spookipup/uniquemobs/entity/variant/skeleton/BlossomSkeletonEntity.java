package spookipup.uniquemobs.entity.variant.skeleton;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
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

		goalSelector.getAvailableGoals().removeIf(w ->
			w.getGoal() instanceof RangedBowAttackGoal ||
			w.getGoal() instanceof MeleeAttackGoal
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
			if (level().getDifficulty() == Difficulty.PEACEFUL) {
				stopBlossomFlight();
				setTarget(null);
				return;
			}

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
				if (isValidBlossomTarget(target)
						&& BlossomHelper.shouldStartReachFlight(this, target, blossomFlightCooldown)) {
					startBlossomFlight(target, 90);
				}
			}
		}
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean hit = super.doHurtTarget(target);
		if (hit && target instanceof LivingEntity living) {
			onBlossomAttackLanded(living);
			BlossomHelper.applyUpwardKnockback(living, this, 0.42, 0.6);
		}
		return hit;
	}

	@Override
	public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}

	public void onBlossomAttackLanded(LivingEntity target) {
		if (blossomFlightTicks <= 0 && blossomFlightCooldown > 0) return;
		startBlossomFlight(target, 120);
	}

	private void startBlossomFlight(LivingEntity target, int duration) {
		if (!isValidBlossomTarget(target)) return;

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
		if (isValidBlossomTarget(entity)) return (LivingEntity) entity;
		LivingEntity target = getTarget();
		return isValidBlossomTarget(target) ? target : null;
	}

	private boolean isValidBlossomTarget(Entity entity) {
		if (!(entity instanceof LivingEntity living) || !living.isAlive()) return false;
		return !(living instanceof Player player) || (!player.isCreative() && !player.isSpectator());
	}

	@Override
	public void reassessWeaponGoal() {
	}

	public ItemStack getWeaponItem() {
		return new ItemStack(Items.BOW);
	}
}
