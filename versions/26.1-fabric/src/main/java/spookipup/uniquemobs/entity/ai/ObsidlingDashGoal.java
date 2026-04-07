package spookipup.uniquemobs.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.variant.ghast.ObsidlingEntity;

import java.util.EnumSet;

// tries to line dashes up with lava or drops before committing
public class ObsidlingDashGoal extends Goal {

	private static final double ATTACK_TRIGGER_RANGE = 14.0;
	private static final double PREFERRED_MIN_DIST = 5.0;
	private static final double PREFERRED_MAX_DIST = 10.0;
	private static final double APPROACH_SPEED = 0.055;
	private static final double RETREAT_SPEED = 0.05;
	private static final double STRAFE_SPEED = 0.03;
	private static final double ALTITUDE_SPEED = 0.022;
	private static final double MAX_CRUISE_SPEED = 0.27;

	private static final int SETUP_DURATION = 18;
	private static final int DASH_WINDUP = 8;
	private static final int DASH_DURATION = 10;
	private static final int RECOVER_DURATION = 18;
	private static final double DASH_SPEED = 0.68;
	private static final float DASH_DAMAGE = 2.5F;
	private static final double BASE_KNOCKBACK = 1.9;
	private static final double HAZARD_KNOCKBACK_BONUS = 0.85;
	private static final double HIT_REACH = 1.6;
	private static final double MIN_COMMIT_HAZARD_SCORE = 8.0;
	private static final double MAX_SETUP_DISTANCE = 1.3;
	private static final double MIN_COMMIT_ALIGNMENT = 0.86;
	private static final double MAX_COMMIT_TARGET_DISTANCE = 4.75;
	private static final double FALLBACK_SETUP_DISTANCE = 2.4;
	private static final double FALLBACK_MAX_SETUP_DISTANCE = 1.65;
	private static final double FALLBACK_MIN_ALIGNMENT = 0.76;
	private static final double FALLBACK_MAX_TARGET_DISTANCE = 4.4;

	private final ObsidlingEntity ghast;

	private LivingEntity target;
	private Vec3 dashDirection = Vec3.ZERO;
	private Vec3 desiredKnockback = Vec3.ZERO;
	private Vec3 setupAnchor = Vec3.ZERO;
	private double hazardScore;
	private double strafeAngle;
	private int strafeChangeTimer;
	private int phaseTicks;
	private int cooldownTicks;
	private boolean hitThisDash;
	private boolean fallbackStrike;
	private Phase phase = Phase.APPROACH;

	private enum Phase {
		APPROACH,
		SETUP,
		DASH_WINDUP,
		DASHING,
		RECOVER
	}

	public ObsidlingDashGoal(ObsidlingEntity ghast) {
		this.ghast = ghast;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		LivingEntity livingEntity = this.ghast.getTarget();
		return livingEntity != null && livingEntity.isAlive();
	}

	@Override
	public boolean canContinueToUse() {
		return this.target != null && this.target.isAlive();
	}

	@Override
	public void start() {
		this.target = this.ghast.getTarget();
		this.phase = Phase.APPROACH;
		this.phaseTicks = 0;
		this.cooldownTicks = randomCooldown();
		this.dashDirection = Vec3.ZERO;
		this.desiredKnockback = Vec3.ZERO;
		this.setupAnchor = Vec3.ZERO;
		this.hazardScore = 0.0;
		this.hitThisDash = false;
		this.fallbackStrike = false;
		this.strafeAngle = this.ghast.getRandom().nextDouble() * Math.PI * 2.0;
		this.strafeChangeTimer = 20 + this.ghast.getRandom().nextInt(20);
		this.ghast.setCharging(false);
	}

	@Override
	public void stop() {
		this.target = null;
		this.phase = Phase.APPROACH;
		this.phaseTicks = 0;
		this.dashDirection = Vec3.ZERO;
		this.desiredKnockback = Vec3.ZERO;
		this.setupAnchor = Vec3.ZERO;
		this.hazardScore = 0.0;
		this.hitThisDash = false;
		this.fallbackStrike = false;
		this.cooldownTicks = randomCooldown();
		this.ghast.setCharging(false);
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (this.target == null) return;

		faceTarget();
		this.ghast.getLookControl().setLookAt(this.target, 40.0F, 40.0F);

		switch (this.phase) {
			case APPROACH -> tickApproach();
			case SETUP -> tickSetup();
			case DASH_WINDUP -> tickDashWindup();
			case DASHING -> tickDashing();
			case RECOVER -> tickRecover();
		}
	}

	private void tickApproach() {
		this.ghast.setCharging(false);
		cruiseAroundTarget();

		if (this.cooldownTicks > 0) {
			this.cooldownTicks--;
			return;
		}
		if (!this.ghast.getSensing().hasLineOfSight(this.target)) return;
		if (this.ghast.distanceTo(this.target) > ATTACK_TRIGGER_RANGE) return;

		planDash();
		if (this.hazardScore < MIN_COMMIT_HAZARD_SCORE) {
			configureFallbackDash();
		}
		this.phase = Phase.SETUP;
		this.phaseTicks = SETUP_DURATION;
	}

	private void tickSetup() {
		this.ghast.setCharging(false);

		Vec3 toAnchor = this.setupAnchor.subtract(this.ghast.position());
		double distance = toAnchor.length();
		if (distance > 0.2) {
			Vec3 velocity = this.ghast.getDeltaMovement().scale(0.82).add(toAnchor.normalize().scale(0.12));
			this.ghast.setDeltaMovement(clampSpeed(velocity, 0.42));
		}

		this.phaseTicks--;
		if (canCommitDash(distance)) {
			this.phase = Phase.DASH_WINDUP;
			this.phaseTicks = DASH_WINDUP;
			this.ghast.setCharging(true);
			return;
		}

		if (this.phaseTicks <= 0) {
			if (!this.fallbackStrike) {
				configureFallbackDash();
				this.phaseTicks = Math.max(10, SETUP_DURATION / 2);
			} else {
				this.phase = Phase.APPROACH;
				this.cooldownTicks = 10;
			}
		}
	}

	private void tickDashWindup() {
		this.ghast.setCharging(true);
		this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().scale(0.72));
		this.phaseTicks--;

		if (this.phaseTicks % 4 == 0 && this.ghast.level() instanceof ServerLevel serverLevel) {
			serverLevel.playSound(null, this.ghast.getX(), this.ghast.getY(), this.ghast.getZ(),
				SoundEvents.NETHER_BRICKS_HIT, SoundSource.HOSTILE, 1.4F, 0.75F + this.ghast.getRandom().nextFloat() * 0.1F);
		}

		if (this.phaseTicks <= 0) {
			beginDash();
		}
	}

	private void beginDash() {
		Vec3 toTarget = this.target.getEyePosition().subtract(this.ghast.position()).normalize();
		this.dashDirection = this.desiredKnockback.lengthSqr() > 1.0E-4 ? this.desiredKnockback : toTarget;
		this.hitThisDash = false;
		this.phase = Phase.DASHING;
		this.phaseTicks = DASH_DURATION;

		if (this.ghast.level() instanceof ServerLevel serverLevel) {
			serverLevel.playSound(null, this.ghast.getX(), this.ghast.getY(), this.ghast.getZ(),
				SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.8F, 1.55F);
		}
	}

	private void tickDashing() {
		this.phaseTicks--;
		Vec3 toTarget = this.target.getEyePosition().subtract(this.ghast.position()).normalize();
		this.dashDirection = this.dashDirection.scale(0.88).add(toTarget.scale(0.12)).normalize();

		Vec3 velocity = this.ghast.getDeltaMovement().scale(0.36).add(this.dashDirection.scale(DASH_SPEED));
		this.ghast.setDeltaMovement(velocity);

		if (!this.hitThisDash && this.ghast.distanceTo(this.target) <= HIT_REACH) {
			hitTarget();
		}

		if (this.phaseTicks <= 0) {
			this.phase = Phase.RECOVER;
			this.phaseTicks = RECOVER_DURATION;
			this.ghast.setCharging(false);
			this.cooldownTicks = randomCooldown();
		}
	}

	private void tickRecover() {
		this.ghast.setCharging(false);
		cruiseAroundTarget();
		this.phaseTicks--;
		if (this.phaseTicks <= 0) {
			this.phase = Phase.APPROACH;
		}
	}

	private void planDash() {
		this.fallbackStrike = false;
		HazardPlan hazardPlan = findBestHazardDirection();
		Vec3 fallback = this.target.position().subtract(this.ghast.position()).normalize();
		this.desiredKnockback = hazardPlan.direction.lengthSqr() > 1.0E-4 ? hazardPlan.direction : fallback;
		this.hazardScore = hazardPlan.score;

		double sideScale = this.hazardScore >= MIN_COMMIT_HAZARD_SCORE ? 0.8 : 0.0;
		Vec3 side = sideScale > 0.0
			? perpendicular(this.desiredKnockback, this.ghast.getRandom().nextBoolean() ? Math.PI * 0.5 : -Math.PI * 0.5)
				.scale((this.ghast.getRandom().nextDouble() - 0.5) * sideScale)
			: Vec3.ZERO;
		this.setupAnchor = this.target.position()
			.subtract(this.desiredKnockback.scale(3.2 + Math.min(this.hazardScore * 0.08, 1.2)))
			.add(side)
			.add(0.0, this.target.getBbHeight() * 0.4 + 0.45, 0.0);
	}

	private void configureFallbackDash() {
		this.fallbackStrike = true;
		this.hazardScore = 0.0;
		Vec3 direct = this.target.position().subtract(this.ghast.position());
		if (direct.lengthSqr() < 1.0E-4) {
			direct = new Vec3(1.0, 0.0, 0.0);
		}
		this.desiredKnockback = direct.normalize();
		this.setupAnchor = this.target.position()
			.subtract(this.desiredKnockback.scale(FALLBACK_SETUP_DISTANCE))
			.add(0.0, this.target.getBbHeight() * 0.35 + 0.35, 0.0);
	}

	private HazardPlan findBestHazardDirection() {
		Level level = this.ghast.level();
		Vec3 targetPos = this.target.position();
		Vec3 bestDirection = Vec3.ZERO;
		double bestScore = 0.0;

		for (int i = 0; i < 12; i++) {
			double angle = (Math.PI * 2.0 * i) / 12.0;
			Vec3 direction = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
			double score = scoreHazardDirection(level, targetPos, direction);
			if (score > bestScore) {
				bestScore = score;
				bestDirection = direction;
			}
		}

		return new HazardPlan(bestDirection, bestScore);
	}

	private double scoreHazardDirection(Level level, Vec3 origin, Vec3 direction) {
		double best = 0.0;
		for (int distance = 2; distance <= 8; distance++) {
			Vec3 sample = origin.add(direction.scale(distance));
			BlockPos pos = BlockPos.containing(sample.x, this.target.getY(), sample.z);

			if (isLavaNearby(level, pos)) {
				best = Math.max(best, 24.0 - distance * 2.0);
			}

			int dropDepth = dropDepth(level, pos);
			if (dropDepth >= 4) {
				best = Math.max(best, 10.0 + dropDepth * 1.8 - distance);
			}
		}
		return best;
	}

	private boolean isLavaNearby(Level level, BlockPos pos) {
		for (int dy = -1; dy <= 1; dy++) {
			BlockPos check = pos.offset(0, dy, 0);
			if (level.getBlockState(check).getFluidState().is(FluidTags.LAVA)) {
				return true;
			}
		}
		return false;
	}

	private int dropDepth(Level level, BlockPos pos) {
		for (int depth = 0; depth <= 8; depth++) {
			BlockPos check = pos.below(depth);
			if (!level.getBlockState(check).isAir() || !level.getFluidState(check).isEmpty()) {
				return depth;
			}
		}
		return 9;
	}

	private void hitTarget() {
		if (!(this.ghast.level() instanceof ServerLevel serverLevel)) return;

		this.hitThisDash = true;
		this.target.hurtServer(serverLevel, this.ghast.damageSources().mobAttack(this.ghast), DASH_DAMAGE);

		double strength = BASE_KNOCKBACK + (this.hazardScore > 0.0 ? HAZARD_KNOCKBACK_BONUS : 0.0);
		this.target.knockback(strength, -this.desiredKnockback.x, -this.desiredKnockback.z);
		this.target.setDeltaMovement(this.target.getDeltaMovement().add(0.0, 0.18, 0.0));
		this.target.hurtMarked = true;

		serverLevel.playSound(null, this.target.getX(), this.target.getY(), this.target.getZ(),
			SoundEvents.ANVIL_HIT, SoundSource.HOSTILE, 0.75F, 1.35F);
	}

	private void cruiseAroundTarget() {
		Vec3 velocity = this.ghast.getDeltaMovement().scale(0.88);
		Vec3 toTarget = this.target.position().subtract(this.ghast.position());
		double distance = toTarget.length();
		Vec3 forward = distance > 1.0E-4 ? toTarget.scale(1.0 / distance) : Vec3.ZERO;

		if (distance > PREFERRED_MAX_DIST) {
			velocity = velocity.add(forward.scale(APPROACH_SPEED));
		} else if (distance < PREFERRED_MIN_DIST) {
			velocity = velocity.add(forward.scale(-RETREAT_SPEED));
		}

		this.strafeChangeTimer--;
		if (this.strafeChangeTimer <= 0) {
			this.strafeAngle += Math.PI + (this.ghast.getRandom().nextDouble() - 0.5) * 0.8;
			this.strafeChangeTimer = 18 + this.ghast.getRandom().nextInt(24);
		}

		Vec3 strafe = perpendicular(forward, this.strafeAngle);
		velocity = velocity.add(strafe.scale(STRAFE_SPEED));

		double desiredY = this.target.getY() + this.target.getBbHeight() * 0.35 + 0.45;
		double yDiff = desiredY - this.ghast.getY();
		velocity = velocity.add(0.0, Math.signum(yDiff) * Math.min(Math.abs(yDiff) * 0.01, ALTITUDE_SPEED), 0.0);

		this.ghast.setDeltaMovement(clampSpeed(velocity, MAX_CRUISE_SPEED));
	}

	private Vec3 perpendicular(Vec3 forward, double angle) {
		if (forward.lengthSqr() < 1.0E-4) {
			return new Vec3(1.0, 0.0, 0.0);
		}
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return new Vec3(-forward.z * cos + forward.x * sin, 0.0, forward.x * cos + forward.z * sin).normalize();
	}

	private Vec3 clampSpeed(Vec3 velocity, double max) {
		if (velocity.lengthSqr() > max * max) {
			return velocity.normalize().scale(max);
		}
		return velocity;
	}

	private void faceTarget() {
		Vec3 toTarget = this.target.getEyePosition().subtract(this.ghast.getEyePosition());
		this.ghast.faceDirection(toTarget);
	}

	private boolean canCommitDash(double setupDistance) {
		Vec3 toTarget = this.target.position().subtract(this.ghast.position());
		double targetDistance = toTarget.length();
		if (targetDistance < 1.0E-4) return false;

		Vec3 approachDir = toTarget.scale(1.0 / targetDistance);
		double alignment = approachDir.dot(this.desiredKnockback);
		if (this.fallbackStrike) {
			return setupDistance <= FALLBACK_MAX_SETUP_DISTANCE
				&& targetDistance <= FALLBACK_MAX_TARGET_DISTANCE
				&& alignment >= FALLBACK_MIN_ALIGNMENT;
		}

		if (this.hazardScore < MIN_COMMIT_HAZARD_SCORE) return false;
		return setupDistance <= MAX_SETUP_DISTANCE
			&& targetDistance <= MAX_COMMIT_TARGET_DISTANCE
			&& alignment >= MIN_COMMIT_ALIGNMENT;
	}

	private int randomCooldown() {
		return this.ghast.getRandom().nextIntBetweenInclusive(35, 60);
	}

	private record HazardPlan(Vec3 direction, double score) {}
}
