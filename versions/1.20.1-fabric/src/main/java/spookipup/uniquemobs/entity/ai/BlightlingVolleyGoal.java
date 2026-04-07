package spookipup.uniquemobs.entity.ai;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.projectile.BlightSporeEntity;
import spookipup.uniquemobs.entity.variant.ghast.BlightlingEntity;

import java.util.EnumSet;

// slides to the side before firing so volleys don't come from a dead stop
public class BlightlingVolleyGoal extends Goal {

	private static final double ATTACK_TRIGGER_RANGE = 20.0;
	private static final double PREFERRED_MIN_DIST = 10.0;
	private static final double PREFERRED_MAX_DIST = 17.0;
	private static final double APPROACH_SPEED = 0.045;
	private static final double RETREAT_SPEED = 0.05;
	private static final double STRAFE_SPEED = 0.035;
	private static final double ALTITUDE_SPEED = 0.022;
	private static final double MAX_CRUISE_SPEED = 0.26;

	private static final int DASH_WINDUP = 9;
	private static final int DASH_DURATION = 11;
	private static final int FIRING_DURATION = 24;
	private static final int COOLDOWN_MIN = 40;
	private static final int COOLDOWN_MAX = 64;
	private static final int SHOTS_MIN = 2;
	private static final int SHOTS_MAX = 3;
	private static final int SHOT_INTERVAL = 7;
	private static final double DASH_SPEED = 0.5;
	private static final double BURST_OFFSET_RADIUS = 1.6;

	private final BlightlingEntity ghast;

	private LivingEntity target;
	private Vec3 dashDirection = Vec3.ZERO;
	private Vec3 firingAnchor = Vec3.ZERO;
	private double strafeAngle;
	private int strafeChangeTimer;
	private int phaseTicks;
	private int cooldownTicks;
	private int shotsRemaining;
	private int nextShotTick;
	private Phase phase = Phase.APPROACH;

	private enum Phase {
		APPROACH,
		DASH_WINDUP,
		DASHING,
		FIRING,
		COOLDOWN
	}

	public BlightlingVolleyGoal(BlightlingEntity ghast) {
		this.ghast = ghast;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		LivingEntity livingEntity = this.ghast.getTarget();
		if (!isValidTarget(livingEntity)) {
			this.ghast.setTarget(null);
			return false;
		}
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		if (!isValidTarget(this.target)) {
			this.ghast.setTarget(null);
			return false;
		}
		return true;
	}

	@Override
	public void start() {
		this.target = this.ghast.getTarget();
		this.phase = Phase.APPROACH;
		this.phaseTicks = 0;
		this.cooldownTicks = randomCooldown();
		this.shotsRemaining = 0;
		this.nextShotTick = 0;
		this.dashDirection = Vec3.ZERO;
		this.firingAnchor = Vec3.ZERO;
		this.strafeAngle = this.ghast.getRandom().nextDouble() * Math.PI * 2.0;
		this.strafeChangeTimer = 24 + this.ghast.getRandom().nextInt(22);
		this.ghast.setCharging(false);
	}

	@Override
	public void stop() {
		this.target = null;
		this.phase = Phase.APPROACH;
		this.phaseTicks = 0;
		this.cooldownTicks = randomCooldown();
		this.shotsRemaining = 0;
		this.nextShotTick = 0;
		this.dashDirection = Vec3.ZERO;
		this.firingAnchor = Vec3.ZERO;
		this.ghast.setCharging(false);
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (!isValidTarget(this.target)) {
			this.ghast.setTarget(null);
			return;
		}

		faceTarget();
		this.ghast.getLookControl().setLookAt(this.target, 40.0F, 40.0F);

		switch (this.phase) {
			case APPROACH -> tickApproach(false);
			case DASH_WINDUP -> tickDashWindup();
			case DASHING -> tickDashing();
			case FIRING -> tickFiring();
			case COOLDOWN -> tickApproach(true);
		}
	}

	private void tickApproach(boolean coolingDown) {
		this.ghast.setCharging(false);
		cruiseAroundTarget();

		if (this.cooldownTicks > 0) {
			this.cooldownTicks--;
		}

		if (coolingDown) {
			if (this.cooldownTicks <= 0) {
				this.phase = Phase.APPROACH;
			}
			return;
		}

		if (this.cooldownTicks > 0) return;
		if (!this.ghast.getSensing().hasLineOfSight(this.target)) return;
		if (this.ghast.distanceTo(this.target) > ATTACK_TRIGGER_RANGE) return;

		beginReposition();
	}

	private void beginReposition() {
		Vec3 toTarget = this.target.position().subtract(this.ghast.position());
		Vec3 forward = toTarget.lengthSqr() > 1.0E-4 ? toTarget.normalize() : new Vec3(0.0, 0.0, 1.0);
		Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
		if (side.lengthSqr() < 1.0E-4) {
			side = new Vec3(1.0, 0.0, 0.0);
		}

		double sideSign = this.ghast.getRandom().nextBoolean() ? 1.0 : -1.0;
		double lateralShift = 4.5 + this.ghast.getRandom().nextDouble() * 2.0;
		double backShift = 1.8 + this.ghast.getRandom().nextDouble() * 2.2;
		this.firingAnchor = this.target.position()
			.add(side.normalize().scale(lateralShift * sideSign))
			.subtract(forward.scale(backShift))
			.add(0.0, this.target.getBbHeight() * 0.5 + 0.6, 0.0);
		this.dashDirection = this.firingAnchor.subtract(this.ghast.position()).normalize();

		this.phase = Phase.DASH_WINDUP;
		this.phaseTicks = DASH_WINDUP;
		this.ghast.setCharging(true);
	}

	private void tickDashWindup() {
		this.ghast.setCharging(true);
		this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().scale(0.78));
		this.phaseTicks--;

		if (this.phaseTicks % 3 == 0 && this.ghast.level() instanceof ServerLevel serverLevel) {
			serverLevel.playSound(null, this.ghast.getX(), this.ghast.getY(), this.ghast.getZ(),
				SoundEvents.SLIME_SQUISH_SMALL, SoundSource.HOSTILE, 1.0F, 0.75F + this.ghast.getRandom().nextFloat() * 0.2F);
		}

		if (this.phaseTicks <= 0) {
			this.phase = Phase.DASHING;
			this.phaseTicks = DASH_DURATION;
			if (this.ghast.level() instanceof ServerLevel serverLevel) {
				serverLevel.playSound(null, this.ghast.getX(), this.ghast.getY(), this.ghast.getZ(),
					SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 1.1F, 0.95F);
			}
		}
	}

	private void tickDashing() {
		this.ghast.setCharging(true);
		this.phaseTicks--;

		Vec3 toAnchor = this.firingAnchor.subtract(this.ghast.position());
		if (toAnchor.lengthSqr() > 1.0E-4) {
			this.dashDirection = this.dashDirection.scale(0.76).add(toAnchor.normalize().scale(0.24)).normalize();
		}

		Vec3 velocity = this.ghast.getDeltaMovement().scale(0.4).add(this.dashDirection.scale(DASH_SPEED));
		this.ghast.setDeltaMovement(clampSpeed(velocity, 0.58));

		if (this.ghast.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
				this.ghast.getX(), this.ghast.getY() + 0.35, this.ghast.getZ(),
				2, 0.1, 0.1, 0.1, 0.01);
		}

		if (this.phaseTicks <= 0 || this.ghast.position().distanceToSqr(this.firingAnchor) <= 2.0) {
			this.phase = Phase.FIRING;
			this.phaseTicks = FIRING_DURATION;
			this.shotsRemaining = this.ghast.getRandom().nextInt(((SHOTS_MAX) - (SHOTS_MIN)) + 1) + (SHOTS_MIN);
			this.nextShotTick = 2;
			this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().scale(0.45));
		}
	}

	private void tickFiring() {
		this.ghast.setCharging(true);
		this.phaseTicks--;
		holdFiringLane();

		if (!(this.ghast.level() instanceof ServerLevel serverLevel)) return;

		spawnChargeParticles(serverLevel);
		this.nextShotTick--;
		if (this.shotsRemaining > 0 && this.nextShotTick <= 0) {
			fireSpore(serverLevel);
			this.shotsRemaining--;
			this.nextShotTick = SHOT_INTERVAL;
		}

		if (this.phaseTicks <= 0 || this.shotsRemaining <= 0) {
			this.phase = Phase.COOLDOWN;
			this.cooldownTicks = randomCooldown();
			this.ghast.setCharging(false);
		}
	}

	private void holdFiringLane() {
		Vec3 velocity = this.ghast.getDeltaMovement().scale(0.82);
		Vec3 toAnchor = this.firingAnchor.subtract(this.ghast.position());
		if (toAnchor.lengthSqr() > 0.25) {
			velocity = velocity.add(toAnchor.normalize().scale(0.06));
		}

		double desiredY = this.target.getY() + this.target.getBbHeight() * 0.55 + 0.65;
		double yDiff = desiredY - this.ghast.getY();
		velocity = velocity.add(0.0, Mth.clamp(yDiff * 0.015, -ALTITUDE_SPEED, ALTITUDE_SPEED), 0.0);
		this.ghast.setDeltaMovement(clampSpeed(velocity, 0.22));
	}

	private void fireSpore(ServerLevel serverLevel) {
		Vec3 mouth = this.ghast.getMouthPosition();
		Vec3 predictedTarget = this.target.getEyePosition().add(this.target.getDeltaMovement().scale(8.0));
		Vec3 forward = predictedTarget.subtract(mouth).normalize();
		Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
		if (right.lengthSqr() < 1.0E-4) {
			right = new Vec3(1.0, 0.0, 0.0);
		}
		right = right.normalize();

		double sideOffset = (this.ghast.getRandom().nextDouble() - 0.5) * 2.0 * BURST_OFFSET_RADIUS;
		double heightOffset = 0.15 + this.ghast.getRandom().nextDouble() * 1.05;
		Vec3 burstPoint = predictedTarget.add(right.scale(sideOffset)).add(0.0, heightOffset, 0.0);
		Vec3 shot = burstPoint.subtract(mouth);

		BlightSporeEntity spore = new BlightSporeEntity(serverLevel, this.ghast);
		spore.setPos(mouth.x, mouth.y, mouth.z);
		spore.setBurstTarget(burstPoint, 20 + this.ghast.getRandom().nextInt(10));
		spore.shoot(shot.x, shot.y + shot.horizontalDistance() * 0.015, shot.z, 0.38F, 4.0F);
		serverLevel.addFreshEntity(spore);

		serverLevel.playSound(null, this.ghast.getX(), this.ghast.getY(), this.ghast.getZ(),
			SoundEvents.LLAMA_SPIT, SoundSource.HOSTILE, 1.15F, 0.7F + this.ghast.getRandom().nextFloat() * 0.2F);
		serverLevel.sendParticles(ParticleTypes.ITEM_SLIME,
			mouth.x, mouth.y, mouth.z, 8, 0.12, 0.12, 0.12, 0.02);
		serverLevel.sendParticles(ParticleTypes.MYCELIUM,
			mouth.x, mouth.y, mouth.z, 6, 0.08, 0.08, 0.08, 0.01);
	}

	private void spawnChargeParticles(ServerLevel serverLevel) {
		Vec3 mouth = this.ghast.getMouthPosition();
		Vec3 forward = this.target.getEyePosition().subtract(mouth).normalize();
		for (int i = 0; i < 5; i++) {
			double side = (this.ghast.getRandom().nextDouble() - 0.5) * 0.45;
			double rise = this.ghast.getRandom().nextDouble() * 0.28;
			serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
				mouth.x + side, mouth.y + rise, mouth.z + side, 0,
				forward.x * 0.03, 0.02, forward.z * 0.03, 1.0);
		}
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
			this.strafeAngle += Math.PI + (this.ghast.getRandom().nextDouble() - 0.5) * 0.9;
			this.strafeChangeTimer = 20 + this.ghast.getRandom().nextInt(24);
		}

		Vec3 strafe = perpendicular(forward, this.strafeAngle);
		velocity = velocity.add(strafe.scale(STRAFE_SPEED));

		double desiredY = this.target.getY() + this.target.getBbHeight() * 0.7 + 0.9;
		double yDiff = desiredY - this.ghast.getY();
		velocity = velocity.add(0.0, Mth.clamp(yDiff * 0.012, -ALTITUDE_SPEED, ALTITUDE_SPEED), 0.0);

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
		Vec3 toTarget = this.target.getEyePosition().subtract(this.ghast.getMouthPosition());
		this.ghast.faceDirection(toTarget);
	}

	private int randomCooldown() {
		return this.ghast.getRandom().nextInt(((COOLDOWN_MAX) - (COOLDOWN_MIN)) + 1) + (COOLDOWN_MIN);
	}

	private boolean isValidTarget(LivingEntity livingEntity) {
		if (livingEntity == null || !livingEntity.isAlive()) return false;
		if (livingEntity instanceof Player player) {
			return !player.isCreative() && !player.isSpectator();
		}
		return true;
	}
}


