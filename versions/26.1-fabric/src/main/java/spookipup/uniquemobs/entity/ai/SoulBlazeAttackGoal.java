package spookipup.uniquemobs.entity.ai;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.SoulFireTrailCloud;
import spookipup.uniquemobs.entity.variant.blaze.SoulBlazeEntity;
import spookipup.uniquemobs.registry.ModEffects;

import java.util.EnumSet;
import java.util.List;

// keeps moving during the breath so the trail matters
public class SoulBlazeAttackGoal extends Goal {

	private static final double ATTACK_TRIGGER_RANGE = 17.0;
	private static final double PREFERRED_MIN_DIST = 4.5;
	private static final double PREFERRED_MAX_DIST = 8.5;
	private static final double BREATH_MIN_DIST = 3.8;
	private static final double BREATH_MAX_DIST = 7.0;
	private static final int WINDUP_TICKS = 16;
	private static final int BREATH_DURATION = 54;
	private static final int BREATH_DAMAGE_INTERVAL = 4;
	private static final int TRAIL_INTERVAL = 5;
	private static final int TRAIL_DURATION = 32;
	private static final float TRAIL_RADIUS = 1.25F;
	private static final int COOLDOWN_MIN = 44;
	private static final int COOLDOWN_MAX = 68;
	private static final double BREATH_RANGE = 7.5;
	private static final double BREATH_HALF_ANGLE_COS = Math.cos(Math.toRadians(30.0));
	private static final float BREATH_DAMAGE = 2.0F;

	private final SoulBlazeEntity blaze;

	private LivingEntity target;
	private int phaseTicks;
	private int cooldownTicks;
	private int strafeChangeTimer;
	private double strafeAngle;
	private Phase phase = Phase.APPROACH;

	private enum Phase {
		APPROACH,
		WINDUP,
		BREATHING,
		COOLDOWN
	}

	public SoulBlazeAttackGoal(SoulBlazeEntity blaze) {
		this.blaze = blaze;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		LivingEntity livingEntity = this.blaze.getTarget();
		if (!isValidTarget(livingEntity)) {
			this.blaze.setTarget(null);
			return false;
		}
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		LivingEntity currentTarget = this.blaze.getTarget();
		if (!isValidTarget(currentTarget)) {
			this.blaze.setTarget(null);
			return false;
		}
		this.target = currentTarget;
		return true;
	}

	@Override
	public void start() {
		this.target = this.blaze.getTarget();
		this.phase = Phase.APPROACH;
		this.phaseTicks = 0;
		this.cooldownTicks = 12;
		this.strafeAngle = this.blaze.getRandom().nextDouble() * Math.PI * 2.0;
		this.strafeChangeTimer = 18 + this.blaze.getRandom().nextInt(20);
	}

	@Override
	public void stop() {
		this.target = null;
		this.phase = Phase.APPROACH;
		this.phaseTicks = 0;
		this.cooldownTicks = 0;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (!isValidTarget(this.target)) {
			this.blaze.setTarget(null);
			return;
		}

		this.blaze.getLookControl().setLookAt(this.target, 40.0F, 40.0F);

		switch (this.phase) {
			case APPROACH -> tickApproach(false);
			case WINDUP -> tickWindup();
			case BREATHING -> tickBreathing();
			case COOLDOWN -> tickApproach(true);
		}
	}

	private void tickApproach(boolean coolingDown) {
		moveAroundTarget(PREFERRED_MIN_DIST, PREFERRED_MAX_DIST, 0.34, 0.16, 0.95);

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
		if (!this.blaze.getSensing().hasLineOfSight(this.target)) return;
		if (this.blaze.distanceTo(this.target) > ATTACK_TRIGGER_RANGE) return;

		this.phase = Phase.WINDUP;
		this.phaseTicks = WINDUP_TICKS;
		if (this.blaze.level() instanceof ServerLevel serverLevel) {
			serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
				SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.4F, 0.85F);
		}
	}

	private void tickWindup() {
		this.phaseTicks--;
		moveAroundTarget(BREATH_MIN_DIST, BREATH_MAX_DIST, 0.24, 0.12, 0.7);

		if (!(this.blaze.level() instanceof ServerLevel serverLevel)) return;

		Vec3 mouth = this.blaze.getEyePosition().add(0.0, -0.2, 0.0);
		serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
			mouth.x, mouth.y, mouth.z, 8, 0.18, 0.15, 0.18, 0.01);
		serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
			mouth.x, mouth.y, mouth.z, 4, 0.1, 0.08, 0.1, 0.01);

		if (this.phaseTicks % 4 == 0) {
			serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
				SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.1F, 0.65F + this.blaze.getRandom().nextFloat() * 0.15F);
		}

		if (this.phaseTicks <= 0) {
			this.phase = Phase.BREATHING;
			this.phaseTicks = BREATH_DURATION;
			serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
				SoundEvents.FIRE_AMBIENT, SoundSource.HOSTILE, 1.6F, 0.55F);
		}
	}

	private void tickBreathing() {
		this.phaseTicks--;
		moveAroundTarget(BREATH_MIN_DIST, BREATH_MAX_DIST, 0.42, 0.22, 1.0);

		if (!(this.blaze.level() instanceof ServerLevel serverLevel)) return;

		Vec3 mouth = this.blaze.getEyePosition().add(0.0, -0.2, 0.0);
		Vec3 forward = this.target.getEyePosition().subtract(mouth).normalize();

		spawnBreathParticles(serverLevel, mouth, forward);
		if (this.phaseTicks % BREATH_DAMAGE_INTERVAL == 0) {
			damageBreathCone(serverLevel, mouth, forward);
		}
		if (this.phaseTicks % TRAIL_INTERVAL == 0) {
			spawnTrail(serverLevel, forward);
		}
		if (this.phaseTicks % 7 == 0) {
			serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
				SoundEvents.FIRE_AMBIENT, SoundSource.HOSTILE, 1.25F, 0.55F + this.blaze.getRandom().nextFloat() * 0.2F);
		}
		if (this.phaseTicks % 10 == 0) {
			serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
				SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.0F, 0.7F + this.blaze.getRandom().nextFloat() * 0.15F);
		}

		if (this.phaseTicks <= 0) {
			this.phase = Phase.COOLDOWN;
			this.cooldownTicks = randomCooldown();
		}
	}

	private void moveAroundTarget(double minDist, double maxDist, double strafeScale, double altitudeScale, double speed) {
		Vec3 toTarget = this.target.position().subtract(this.blaze.position());
		double distance = toTarget.length();
		Vec3 forward = distance > 1.0E-4 ? toTarget.scale(1.0 / distance) : new Vec3(1.0, 0.0, 0.0);
		Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
		if (side.lengthSqr() < 1.0E-4) {
			side = new Vec3(1.0, 0.0, 0.0);
		}

		this.strafeChangeTimer--;
		if (this.strafeChangeTimer <= 0) {
			this.strafeAngle += Math.PI + (this.blaze.getRandom().nextDouble() - 0.5) * 0.95;
			this.strafeChangeTimer = 16 + this.blaze.getRandom().nextInt(18);
		}

		double desiredDistance = distance;
		if (distance > maxDist) {
			desiredDistance = maxDist - 0.5;
		} else if (distance < minDist) {
			desiredDistance = minDist + 1.6;
		}

		Vec3 orbitSide = rotateHorizontal(side.normalize(), forward, this.strafeAngle);
		Vec3 anchor = this.target.position()
			.subtract(forward.scale(desiredDistance))
			.add(orbitSide.scale(Math.max(1.4, desiredDistance * strafeScale)))
			.add(0.0, this.target.getBbHeight() * 0.55 + 1.0 + Math.sin(this.blaze.tickCount * 0.12) * altitudeScale, 0.0);

		moveTowards(anchor, speed);
	}

	private Vec3 rotateHorizontal(Vec3 side, Vec3 forward, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return side.scale(cos).add(forward.scale(sin)).normalize();
	}

	private void spawnBreathParticles(ServerLevel serverLevel, Vec3 mouth, Vec3 forward) {
		Vec3 right = breathRight(forward);
		for (int i = 0; i < 16; i++) {
			double dist = 0.5 + this.blaze.getRandom().nextDouble() * BREATH_RANGE * 0.95;
			double width = 0.22 + dist * 0.11;
			double side = (this.blaze.getRandom().nextDouble() - 0.5) * 2.0 * width;
			double height = this.blaze.getRandom().nextDouble() * (0.15 + dist * 0.04);
			double drop = 0.03 + dist * dist * 0.02;
			Vec3 particlePos = mouth.add(forward.scale(dist)).add(right.scale(side)).add(0.0, height - drop, 0.0);
			Vec3 velocity = forward.scale(0.11 + this.blaze.getRandom().nextDouble() * 0.09)
				.add(right.scale((this.blaze.getRandom().nextDouble() - 0.5) * 0.09))
				.add(0.0, -0.06 - this.blaze.getRandom().nextDouble() * 0.05, 0.0);
			serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
				particlePos.x, particlePos.y, particlePos.z, 0,
				velocity.x, velocity.y, velocity.z, 1.0);
			if (i % 3 == 0) {
				serverLevel.sendParticles(ParticleTypes.SOUL,
					particlePos.x, particlePos.y, particlePos.z, 0,
					velocity.x * 0.25, velocity.y * 0.1, velocity.z * 0.25, 1.0);
			}
			if (i % 4 == 0) {
				serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
					particlePos.x, particlePos.y, particlePos.z, 0,
					velocity.x * 0.2, velocity.y * 0.08, velocity.z * 0.2, 1.0);
			}
		}
	}

	private void damageBreathCone(ServerLevel serverLevel, Vec3 mouth, Vec3 forward) {
		AABB hitBox = this.blaze.getBoundingBox().inflate(BREATH_RANGE);
		List<LivingEntity> hitEntities = serverLevel.getEntitiesOfClass(LivingEntity.class, hitBox,
			livingEntity -> livingEntity != this.blaze && livingEntity.isAlive() && !(livingEntity instanceof SoulBlazeEntity));

		for (LivingEntity hit : hitEntities) {
			Vec3 toHit = hit.getEyePosition().subtract(mouth);
			double distance = toHit.length();
			if (distance < 0.45 || distance > BREATH_RANGE) continue;

			Vec3 dirToHit = toHit.scale(1.0 / distance);
			if (forward.dot(dirToHit) < BREATH_HALF_ANGLE_COS) continue;
			if (!this.blaze.getSensing().hasLineOfSight(hit)) continue;

			hit.hurtServer(serverLevel, this.blaze.damageSources().magic(), BREATH_DAMAGE);
			hit.igniteForSeconds(4);
			hit.addEffect(new MobEffectInstance(ModEffects.SOUL_SCORCH, 120, 0));
			hit.setDeltaMovement(hit.getDeltaMovement().add(dirToHit.scale(0.08)).add(0.0, 0.015, 0.0));
			hit.hurtMarked = true;
		}
	}

	private void spawnTrail(ServerLevel serverLevel, Vec3 forward) {
		Vec3 motion = this.blaze.getDeltaMovement();
		Vec3 trailDir = motion.horizontalDistanceSqr() > 0.01 ? motion.normalize() : forward;
		Vec3 pos = this.blaze.position().subtract(trailDir.scale(0.45)).add(0.0, 0.2, 0.0);
		SoulFireTrailCloud cloud = new SoulFireTrailCloud(serverLevel, pos.x, pos.y, pos.z);
		cloud.setOwner(this.blaze);
		cloud.setCustomParticle(ParticleTypes.SOUL_FIRE_FLAME);
		cloud.setRadius(TRAIL_RADIUS);
		cloud.setDuration(TRAIL_DURATION);
		cloud.setWaitTime(0);
		cloud.setRadiusOnUse(-0.12F);
		cloud.setDurationOnUse(-2);
		cloud.setRadiusPerTick(-TRAIL_RADIUS / TRAIL_DURATION);
		serverLevel.addFreshEntity(cloud);

		serverLevel.sendParticles(ParticleTypes.SOUL,
			pos.x, pos.y, pos.z, 5, 0.2, 0.1, 0.2, 0.01);
	}

	private Vec3 breathRight(Vec3 forward) {
		Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
		if (right.lengthSqr() < 1.0E-4) {
			right = new Vec3(1.0, 0.0, 0.0);
		}
		return right.normalize();
	}

	private void moveTowards(Vec3 targetPos, double speed) {
		this.blaze.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, speed);
	}

	private int randomCooldown() {
		return this.blaze.getRandom().nextIntBetweenInclusive(COOLDOWN_MIN, COOLDOWN_MAX);
	}

	private boolean isValidTarget(LivingEntity livingEntity) {
		if (livingEntity == null || !livingEntity.isAlive()) return false;
		if (livingEntity instanceof Player player) {
			return !player.isCreative() && !player.isSpectator();
		}
		return true;
	}
}
