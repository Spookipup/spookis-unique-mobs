package spookipup.uniquemobs.entity.variant.skeleton;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import spookipup.uniquemobs.entity.ai.ShootGoal;
import spookipup.uniquemobs.entity.ai.StrafeAndRetreatGoal;

// shoot -> teleport -> repeat
public class EnderSkeletonEntity extends Skeleton {

	private boolean shouldTeleportAfterShot;
	private boolean teleportNext;

	public EnderSkeletonEntity(EntityType<? extends Skeleton> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Skeleton.createAttributes()
			.add(Attributes.MAX_HEALTH, 22.0)
			.add(Attributes.FOLLOW_RANGE, 32.0);
	}

	@Override
	public float getWalkTargetValue(BlockPos pos, LevelReader level) {
		return 0.0F;
	}

	@Override
	public boolean isSensitiveToWater() {
		return true;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();

		this.goalSelector.getAvailableGoals().removeIf(w ->
			w.getGoal() instanceof RangedBowAttackGoal ||
			w.getGoal() instanceof MeleeAttackGoal
		);

		this.goalSelector.addGoal(3, new StrafeAndRetreatGoal(this, 0.5, 5.0F, 10.0F, 24.0F));

		this.goalSelector.addGoal(3, new ShootGoal(this, 50, 70, 24.0F, 3, true, false,
			ShootGoal.simple((level, shooter) -> {
				Arrow arrow = new Arrow(level, shooter);
				arrow.setCritArrow(true);
				return arrow;
			}, 1.8F, 3.0F)
		).afterShot(() -> {
			this.teleportNext = !this.teleportNext;
			if (this.teleportNext) {
				this.shouldTeleportAfterShot = true;
			}
		}));

		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, false));
	}

	@Override
	public void reassessWeaponGoal() {
	}

	@Override
	public void aiStep() {
		if (!this.level().isClientSide()) {
			if (this.isInWaterOrRain()) {
				this.hurt(
					this.damageSources().generic(), 1.0F);
			}

			if (this.level().isDay() && this.level().canSeeSky(this.blockPosition())
					&& this.random.nextInt(60) == 0) {
				teleportRandomly(16);
			}
		}

		super.aiStep();

		if (!this.level().isClientSide() && this.shouldTeleportAfterShot) {
			this.shouldTeleportAfterShot = false;
			teleportToNewPosition();
		}

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(4) == 0) {
			this.level().addParticle(
				ParticleTypes.PORTAL,
				this.getRandomX(0.4), this.getRandomY(), this.getRandomZ(0.4),
				(this.random.nextDouble() - 0.5) * 0.5,
				this.random.nextDouble() * 0.5,
				(this.random.nextDouble() - 0.5) * 0.5
			);
		}
	}

	private void teleportRandomly(int range) {
		for (int i = 0; i < 10; i++) {
			double tx = this.getX() + (this.random.nextDouble() - 0.5) * 2 * range;
			double ty = this.getY() + (this.random.nextInt(range) - range / 2);
			double tz = this.getZ() + (this.random.nextDouble() - 0.5) * 2 * range;
			if (this.randomTeleport(tx, ty, tz, true)) {
				this.level().playSound(null, this.xo, this.yo, this.zo,
					SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
				return;
			}
		}
	}

	private void teleportToNewPosition() {
		LivingEntity target = this.getTarget();
		if (target == null) return;

		for (int i = 0; i < 16; i++) {
			double angle = this.random.nextDouble() * Math.PI * 2;
			double dist = 12.0 + this.random.nextDouble() * 10.0;
			double tx = target.getX() + Math.cos(angle) * dist;
			double tz = target.getZ() + Math.sin(angle) * dist;
			double ty = target.getY() + (this.random.nextInt(5) - 2);
			if (this.randomTeleport(tx, ty, tz, true)) {
				this.level().playSound(null, this.xo, this.yo, this.zo,
					SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
				return;
			}
		}
	}
}
