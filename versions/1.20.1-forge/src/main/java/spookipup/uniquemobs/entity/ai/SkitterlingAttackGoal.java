package spookipup.uniquemobs.entity.ai;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.SoulFireTrailCloud;
import spookipup.uniquemobs.entity.variant.ghast.SkitterlingEntity;
import spookipup.uniquemobs.registry.ModEffects;

import java.util.EnumSet;
import java.util.List;

// dash first, then cash out with the breath
public class SkitterlingAttackGoal extends Goal {

	private static final double ATTACK_TRIGGER_RANGE = 14.0;
	private static final double PREFERRED_MIN_DIST = 6.0;
	private static final double PREFERRED_MAX_DIST = 10.0;
	private static final double APPROACH_SPEED = 0.05;
	private static final double RETREAT_SPEED = 0.06;
	private static final double STRAFE_SPEED = 0.03;
	private static final double ALTITUDE_SPEED = 0.025;
	private static final double MAX_CRUISE_SPEED = 0.28;

	private static final int DASHES_MIN = 2;
	private static final int DASHES_MAX = 4;
	private static final int DASH_WINDUP = 8;
	private static final int DASH_DURATION = 10;
	private static final int INTER_DASH_DELAY = 8;
	private static final double DASH_SPEED = 0.58;
	private static final int TRAIL_INTERVAL = 2;
	private static final int TRAIL_DURATION = 36;
	private static final float TRAIL_RADIUS = 1.35F;

	private static final int BREATH_WINDUP = 28;
	private static final int BREATH_DURATION = 44;
	private static final int BREATH_DAMAGE_INTERVAL = 5;
	private static final double BREATH_RANGE = 8.5;
	private static final double BREATH_HALF_ANGLE_COS = Math.cos(Math.toRadians(27.0));
	private static final float BREATH_DAMAGE = 2.0F;

	private static final int COOLDOWN_MIN = 55;
	private static final int COOLDOWN_MAX = 85;

	private final SkitterlingEntity ghast;

	private LivingEntity target;
	private Vec3 dashDirection = Vec3.ZERO;
	private int dashesRemaining;
	private int phaseTicks;
	private int cooldownTicks;
	private double strafeAngle;
	private int strafeChangeTimer;
	private Phase phase = Phase.APPROACH;

	private enum Phase {
		APPROACH,
		DASH_WINDUP,
		DASHING,
		INTER_DASH,
		BREATH_WINDUP,
		BREATHING,
		COOLDOWN
	}

	public SkitterlingAttackGoal(SkitterlingEntity ghast) {
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
		this.dashesRemaining = 0;
		this.dashDirection = Vec3.ZERO;
		this.cooldownTicks = randomCooldown();
		this.strafeAngle = this.ghast.getRandom().nextDouble() * Math.PI * 2.0;
		this.strafeChangeTimer = 25 + this.ghast.getRandom().nextInt(20);
		this.ghast.setCharging(false);
	}

	@Override
	public void stop() {
		this.target = null;
		this.phase = Phase.APPROACH;
		this.phaseTicks = 0;
		this.dashesRemaining = 0;
		this.cooldownTicks = randomCooldown();
		this.dashDirection = Vec3.ZERO;
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
			case APPROACH -> tickApproach(false);
			case DASH_WINDUP -> tickDashWindup();
			case DASHING -> tickDashing();
			case INTER_DASH -> tickInterDash();
			case BREATH_WINDUP -> tickBreathWindup();
			case BREATHING -> tickBreathing();
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

		this.dashesRemaining = this.ghast.getRandom().nextInt(((DASHES_MAX) - (DASHES_MIN)) + 1) + (DASHES_MIN);
		this.phase = Phase.DASH_WINDUP;
		this.phaseTicks = DASH_WINDUP;
		this.ghast.setCharging(true);
	}

	private void tickDashWindup() {
		this.ghast.setCharging(true);
		this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().scale(0.75));
		this.phaseTicks--;

		if (this.phaseTicks % 4 == 0 && this.ghast.level() instanceof ServerLevel serverLevel) {
			Vec3 pos = this.ghast.position();
			serverLevel.playSound(null, pos.x, pos.y, pos.z,
				SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 1.6F, 1.3F);
		}

		if (this.phaseTicks <= 0) {
			beginDash();
		}
	}

	private void beginDash() {
		Vec3 targetPos = this.target.getEyePosition().add(this.target.getDeltaMovement().scale(3.0));
		Vec3 toTarget = targetPos.subtract(this.ghast.position()).normalize();
		Vec3 sideways = new Vec3(-toTarget.z, 0.0, toTarget.x);
		if (sideways.lengthSqr() < 1.0E-4) {
			sideways = new Vec3(1.0, 0.0, 0.0);
		}
		double sideSign = this.ghast.getRandom().nextBoolean() ? 1.0 : -1.0;
		double verticalBias = -0.03 + this.ghast.getRandom().nextDouble() * 0.12;
		this.dashDirection = toTarget.add(sideways.normalize().scale(0.55 * sideSign)).add(0.0, verticalBias, 0.0).normalize();

		this.phase = Phase.DASHING;
		this.phaseTicks = DASH_DURATION;

		if (this.ghast.level() instanceof ServerLevel serverLevel) {
			Vec3 pos = this.ghast.position();
			serverLevel.playSound(null, pos.x, pos.y, pos.z,
				SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, 1.35F);
		}
	}

	private void tickDashing() {
		Vec3 toTarget = this.target.getEyePosition().subtract(this.ghast.position()).normalize();
		this.dashDirection = this.dashDirection.scale(0.82).add(toTarget.scale(0.18)).normalize();

		Vec3 velocity = this.ghast.getDeltaMovement().scale(0.45).add(this.dashDirection.scale(DASH_SPEED));
		this.ghast.setDeltaMovement(velocity);
		this.phaseTicks--;

		if (this.ghast.level() instanceof ServerLevel serverLevel) {
			if (this.phaseTicks % TRAIL_INTERVAL == 0) {
				spawnTrail(serverLevel);
			}
			serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
				this.ghast.getX(), this.ghast.getY() + 0.4, this.ghast.getZ(),
				2, 0.12, 0.12, 0.12, 0.01);
		}

		if (this.phaseTicks <= 0) {
			this.dashesRemaining--;
			if (this.dashesRemaining > 0) {
				this.phase = Phase.INTER_DASH;
				this.phaseTicks = INTER_DASH_DELAY;
			} else {
				this.phase = Phase.BREATH_WINDUP;
				this.phaseTicks = BREATH_WINDUP;
			}
		}
	}

	private void tickInterDash() {
		this.ghast.setCharging(true);
		cruiseAroundTarget();
		this.phaseTicks--;
		if (this.phaseTicks <= 0) {
			this.phase = Phase.DASH_WINDUP;
			this.phaseTicks = DASH_WINDUP;
		}
	}

	private void tickBreathWindup() {
		this.ghast.setCharging(true);
		this.phaseTicks--;
		this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().scale(0.72));

		if (!(this.ghast.level() instanceof ServerLevel serverLevel)) return;

		Vec3 mouth = this.ghast.getMouthPosition();
		Vec3 forward = this.target.getEyePosition().subtract(mouth).normalize();
		Vec3 right = breathRight(forward);

		for (int i = 0; i < 6; i++) {
			double side = (this.ghast.getRandom().nextDouble() - 0.5) * 0.6;
			double rise = this.ghast.getRandom().nextDouble() * 0.25;
			Vec3 particlePos = mouth.add(right.scale(side)).add(0.0, rise, 0.0);
			Vec3 velocity = forward.scale(0.02).add(0.0, 0.02 + this.ghast.getRandom().nextDouble() * 0.02, 0.0);
			serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
				particlePos.x, particlePos.y, particlePos.z, 0,
				velocity.x, velocity.y, velocity.z, 1.0);
			serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
				particlePos.x, particlePos.y, particlePos.z, 0,
				velocity.x * 0.35, velocity.y * 0.2, velocity.z * 0.35, 1.0);
		}

		if (this.phaseTicks % 4 == 0) {
			serverLevel.playSound(null, this.ghast.getX(), this.ghast.getY(), this.ghast.getZ(),
				SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 1.8F, 0.75F + this.ghast.getRandom().nextFloat() * 0.15F);
		}

		if (this.phaseTicks <= 0) {
			this.phase = Phase.BREATHING;
			this.phaseTicks = BREATH_DURATION;
			serverLevel.playSound(null, this.ghast.getX(), this.ghast.getY(), this.ghast.getZ(),
				SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.6F, 0.6F);
		}
	}

	private void tickBreathing() {
		this.ghast.setCharging(true);
		this.phaseTicks--;
		this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().scale(0.6));

		if (!(this.ghast.level() instanceof ServerLevel serverLevel)) return;

		Vec3 mouth = this.ghast.getMouthPosition();
		Vec3 forward = this.target.getEyePosition().subtract(mouth).normalize();

		spawnBreathParticles(serverLevel, mouth, forward);
		if (this.phaseTicks % BREATH_DAMAGE_INTERVAL == 0) {
			damageBreathCone(serverLevel, mouth, forward);
		}
		if (this.phaseTicks % 6 == 0) {
			serverLevel.playSound(null, this.ghast.getX(), this.ghast.getY(), this.ghast.getZ(),
				SoundEvents.FIRE_AMBIENT, SoundSource.HOSTILE, 1.8F, 0.65F + this.ghast.getRandom().nextFloat() * 0.2F);
		}
		if (this.phaseTicks % 10 == 0) {
			serverLevel.playSound(null, this.ghast.getX(), this.ghast.getY(), this.ghast.getZ(),
				SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 1.5F, 0.55F + this.ghast.getRandom().nextFloat() * 0.15F);
		}

		if (this.phaseTicks <= 0) {
			this.ghast.setCharging(false);
			this.phase = Phase.COOLDOWN;
			this.cooldownTicks = randomCooldown();
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
			this.strafeAngle += Math.PI + (this.ghast.getRandom().nextDouble() - 0.5) * 0.8;
			this.strafeChangeTimer = 22 + this.ghast.getRandom().nextInt(26);
		}

		Vec3 strafe = perpendicular(forward, this.strafeAngle);
		velocity = velocity.add(strafe.scale(STRAFE_SPEED));

		double desiredY = this.target.getY() + this.target.getBbHeight() * 0.45 + 0.55;
		double yDiff = desiredY - this.ghast.getY();
		velocity = velocity.add(0.0, Math.signum(yDiff) * Math.min(Math.abs(yDiff) * 0.01, ALTITUDE_SPEED), 0.0);

		if (velocity.lengthSqr() > MAX_CRUISE_SPEED * MAX_CRUISE_SPEED) {
			velocity = velocity.normalize().scale(MAX_CRUISE_SPEED);
		}
		this.ghast.setDeltaMovement(velocity);
	}

	private Vec3 perpendicular(Vec3 forward, double angle) {
		if (forward.lengthSqr() < 1.0E-4) {
			return new Vec3(1.0, 0.0, 0.0);
		}
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return new Vec3(-forward.z * cos + forward.x * sin, 0.0, forward.x * cos + forward.z * sin).normalize();
	}

	private void spawnTrail(ServerLevel serverLevel) {
		Vec3 pos = this.ghast.position().subtract(this.dashDirection.scale(0.45)).add(0.0, 0.15, 0.0);
		SoulFireTrailCloud cloud = new SoulFireTrailCloud(serverLevel, pos.x, pos.y, pos.z);
		cloud.setOwner(this.ghast);
		cloud.setParticle(ParticleTypes.SOUL_FIRE_FLAME);
		cloud.setRadius(TRAIL_RADIUS);
		cloud.setDuration(TRAIL_DURATION);
		cloud.setWaitTime(0);
		cloud.setRadiusOnUse(-0.12F);
		cloud.setDurationOnUse(-2);
		cloud.setRadiusPerTick(-TRAIL_RADIUS / TRAIL_DURATION);
		serverLevel.addFreshEntity(cloud);

		serverLevel.sendParticles(ParticleTypes.SOUL,
			pos.x, pos.y, pos.z, 3, 0.25, 0.1, 0.25, 0.01);
	}

	private void spawnBreathParticles(ServerLevel serverLevel, Vec3 mouth, Vec3 forward) {
		Vec3 right = breathRight(forward);
		for (int i = 0; i < 18; i++) {
			double dist = 0.6 + this.ghast.getRandom().nextDouble() * BREATH_RANGE * 0.95;
			double width = 0.18 + dist * 0.11;
			double side = (this.ghast.getRandom().nextDouble() - 0.5) * 2.0 * width;
			double height = this.ghast.getRandom().nextDouble() * (0.12 + dist * 0.05);
			double drop = 0.05 + dist * dist * 0.028;
			Vec3 particlePos = mouth.add(forward.scale(dist)).add(right.scale(side)).add(0.0, height - drop, 0.0);
			Vec3 velocity = forward.scale(0.12 + this.ghast.getRandom().nextDouble() * 0.08)
				.add(right.scale((this.ghast.getRandom().nextDouble() - 0.5) * 0.12))
				.add(0.0, -0.08 - this.ghast.getRandom().nextDouble() * 0.06, 0.0);
			serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
				particlePos.x, particlePos.y, particlePos.z, 0,
				velocity.x, velocity.y, velocity.z, 1.0);
			if (i % 3 == 0) {
				serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
					particlePos.x, particlePos.y, particlePos.z, 0,
					velocity.x * 0.25, velocity.y * 0.15, velocity.z * 0.25, 1.0);
			}
		}
		serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
			mouth.x, mouth.y, mouth.z, 2, 0.12, 0.08, 0.12, 0.01);
	}

	private void damageBreathCone(ServerLevel serverLevel, Vec3 mouth, Vec3 forward) {
		AABB breathBox = this.ghast.getBoundingBox().inflate(BREATH_RANGE);
		List<LivingEntity> hitEntities = serverLevel.getEntitiesOfClass(LivingEntity.class, breathBox,
			livingEntity -> livingEntity != this.ghast && livingEntity.isAlive());

		for (LivingEntity hit : hitEntities) {
			Vec3 toHit = hit.getEyePosition().subtract(mouth);
			double distance = toHit.length();
			if (distance < 0.5 || distance > BREATH_RANGE) continue;

			Vec3 dirToHit = toHit.scale(1.0 / distance);
			if (forward.dot(dirToHit) < BREATH_HALF_ANGLE_COS) continue;
			if (!this.ghast.getSensing().hasLineOfSight(hit)) continue;

			hit.hurt(this.ghast.damageSources().magic(), BREATH_DAMAGE);
			hit.addEffect(new MobEffectInstance(ModEffects.SOUL_SCORCH, 120, 1));
			hit.setDeltaMovement(hit.getDeltaMovement().add(dirToHit.scale(0.08)).add(0.0, 0.02, 0.0));
			hit.hurtMarked = true;
		}
	}

	private int randomCooldown() {
		return this.ghast.getRandom().nextInt(((COOLDOWN_MAX) - (COOLDOWN_MIN)) + 1) + (COOLDOWN_MIN);
	}

	private void faceTarget() {
		Vec3 toTarget = this.target.getEyePosition().subtract(this.ghast.getMouthPosition());
		this.ghast.faceDirection(toTarget);
	}

	private Vec3 breathRight(Vec3 forward) {
		Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
		if (right.lengthSqr() < 1.0E-4) {
			right = new Vec3(1.0, 0.0, 0.0);
		}
		return right.normalize();
	}
}


