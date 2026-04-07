package spookipup.uniquemobs.entity.ai;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.StormArcEntity;
import spookipup.uniquemobs.entity.variant.blaze.StormBlazeEntity;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// the preview ring is the warning here, so keep the windup readable
public class StormBlazeAttackGoal extends Goal {

	private static final double ATTACK_TRIGGER_RANGE = 24.0;
	private static final double PREFERRED_MIN_DIST = 10.0;
	private static final double PREFERRED_MAX_DIST = 18.0;
	private static final double ARC_RADIUS = 0.75;
	private static final double IMPACT_RADIUS = 2.5;
	private static final int WINDUP_TICKS = 20;
	private static final int BURST_REFIRE_TICKS = 16;
	private static final int BURST_SHOTS = 3;
	private static final int STRIKE_LEAD_TICKS = 3;
	private static final int COOLDOWN_MIN = 38;
	private static final int COOLDOWN_MAX = 60;
	private static final float ARC_DAMAGE = 4.5F;
	private static final float IMPACT_DAMAGE = 7.0F;

	private final StormBlazeEntity blaze;

	private LivingEntity target;
	private Vec3 strikeTarget = Vec3.ZERO;
	private int shotsRemaining;
	private int phaseTicks;
	private int cooldownTicks;
	private Phase phase = Phase.APPROACH;

	private enum Phase {
		APPROACH,
		WINDUP,
		COOLDOWN
	}

	public StormBlazeAttackGoal(StormBlazeEntity blaze) {
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
		this.strikeTarget = Vec3.ZERO;
		this.shotsRemaining = 0;
		this.phase = Phase.APPROACH;
		this.phaseTicks = 0;
		this.cooldownTicks = randomCooldown();
	}

	@Override
	public void stop() {
		this.target = null;
		this.strikeTarget = Vec3.ZERO;
		this.shotsRemaining = 0;
		this.phase = Phase.APPROACH;
		this.phaseTicks = 0;
		this.cooldownTicks = randomCooldown();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		LivingEntity currentTarget = this.blaze.getTarget();
		if (isValidTarget(currentTarget)) {
			this.target = currentTarget;
		}

		if (!isValidTarget(this.target)) {
			this.blaze.setTarget(null);
			return;
		}

		this.blaze.getLookControl().setLookAt(this.target, 40.0F, 40.0F);

		switch (this.phase) {
			case APPROACH -> tickApproach();
			case WINDUP -> tickWindup();
			case COOLDOWN -> tickCooldown();
		}
	}

	private void tickApproach() {
		double distance = this.blaze.distanceTo(this.target);
		Vec3 targetPos = this.target.position();
		double desiredY = this.target.getY() + this.target.getBbHeight() * 0.85 + 1.8;

		if (distance > PREFERRED_MAX_DIST) {
			moveTowards(targetPos.add(0.0, this.target.getBbHeight() * 0.5, 0.0), 0.95);
		} else if (distance < PREFERRED_MIN_DIST) {
			Vec3 away = this.blaze.position().subtract(targetPos);
			if (away.lengthSqr() < 1.0E-4) {
				away = new Vec3(1.0, 0.0, 0.0);
			}
			Vec3 retreatPoint = this.blaze.position().add(away.normalize().scale(4.5));
			moveTowards(new Vec3(retreatPoint.x, desiredY, retreatPoint.z), 0.9);
		} else {
			moveTowards(new Vec3(this.blaze.getX(), desiredY, this.blaze.getZ()), 0.6);
		}

		if (this.cooldownTicks > 0) {
			this.cooldownTicks--;
			return;
		}

		if (distance <= ATTACK_TRIGGER_RANGE && this.blaze.getSensing().hasLineOfSight(this.target)) {
			this.phase = Phase.WINDUP;
			this.phaseTicks = WINDUP_TICKS;
			this.shotsRemaining = BURST_SHOTS;
			this.strikeTarget = pickStrikeTarget(this.phaseTicks);
			if (this.blaze.level() instanceof ServerLevel serverLevel) {
				serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
					SoundEvents.TRIDENT_THUNDER, SoundSource.HOSTILE, 0.9F, 1.45F);
			}
		}
	}

	private void tickWindup() {
		this.phaseTicks--;
		moveTowards(this.blaze.position().add(0.0, 0.08, 0.0), 0.3);

		if (!(this.blaze.level() instanceof ServerLevel serverLevel)) return;

		serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
			this.blaze.getX(), this.blaze.getY() + 0.9, this.blaze.getZ(),
			10, 0.28, 0.32, 0.28, 0.02);
		serverLevel.sendParticles(ParticleTypes.END_ROD,
			this.blaze.getX(), this.blaze.getY() + 0.8, this.blaze.getZ(),
			2, 0.08, 0.08, 0.08, 0.0);
		spawnChargePreview(serverLevel);

		if (this.phaseTicks % 5 == 0) {
			serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
				SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.9F, 1.35F);
		}

		if (this.phaseTicks <= 0) {
			summonLightning(serverLevel);
			this.shotsRemaining--;
			if (this.shotsRemaining > 0 && isValidTarget(this.target) && this.blaze.distanceTo(this.target) <= ATTACK_TRIGGER_RANGE + 4.0) {
				this.phase = Phase.WINDUP;
				this.phaseTicks = BURST_REFIRE_TICKS;
				this.strikeTarget = pickStrikeTarget(this.phaseTicks);
				serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
					SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.8F, 1.7F);
			} else {
				this.phase = Phase.COOLDOWN;
				this.cooldownTicks = randomCooldown();
				this.shotsRemaining = 0;
			}
		}
	}

	private void tickCooldown() {
		double distance = this.blaze.distanceTo(this.target);
		double desiredY = this.target.getY() + this.target.getBbHeight() * 0.8 + 1.5;
		moveTowards(new Vec3(this.blaze.getX(), desiredY, this.blaze.getZ()), 0.45);

		if (this.cooldownTicks > 0) {
			this.cooldownTicks--;
		}

		if (this.cooldownTicks <= 0 && distance <= ATTACK_TRIGGER_RANGE) {
			this.phase = Phase.APPROACH;
		}
	}

	private void summonLightning(ServerLevel serverLevel) {
		Vec3 strikePos = this.strikeTarget.equals(Vec3.ZERO) ? pickStrikeTarget(0) : this.strikeTarget;
		Vec3 origin = this.blaze.getEyePosition().add(0.0, -0.2, 0.0);
		Vec3 destination = strikePos.add(0.0, this.target.getBbHeight() * 0.6, 0.0);
		serverLevel.addFreshEntity(new StormArcEntity(serverLevel, origin, destination));
		Set<UUID> hitTargets = new HashSet<>();
		damageArcPath(serverLevel, origin, strikePos, hitTargets);
		damageImpact(serverLevel, strikePos, hitTargets);

		serverLevel.playSound(null, strikePos.x, strikePos.y, strikePos.z,
			SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 1.0F, 1.0F);
		serverLevel.playSound(null, strikePos.x, strikePos.y, strikePos.z,
			SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 3.0F, 1.1F);
		serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
			strikePos.x, strikePos.y + 1.0, strikePos.z,
			40, 0.65, 1.0, 0.65, 0.08);
		serverLevel.sendParticles(ParticleTypes.END_ROD,
			strikePos.x, strikePos.y + 0.2, strikePos.z,
			8, 0.25, 0.25, 0.25, 0.0);
		this.strikeTarget = Vec3.ZERO;
	}

	private Vec3 pickStrikeTarget(int ticksUntilImpact) {
		int leadTicks = Math.max(0, ticksUntilImpact) + STRIKE_LEAD_TICKS;
		Vec3 targetPos = this.target.position().add(this.target.getDeltaMovement().scale(leadTicks));
		return new Vec3(targetPos.x, this.target.getY(), targetPos.z);
	}

	private void spawnChargePreview(ServerLevel serverLevel) {
		if (this.strikeTarget.equals(Vec3.ZERO)) {
			return;
		}

		serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
			this.strikeTarget.x, this.strikeTarget.y + 0.15, this.strikeTarget.z,
			9, 0.35, 0.08, 0.35, 0.02);
		serverLevel.sendParticles(ParticleTypes.END_ROD,
			this.strikeTarget.x, this.strikeTarget.y + 0.08, this.strikeTarget.z,
			2, 0.14, 0.03, 0.14, 0.0);
		serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
			this.strikeTarget.x, this.strikeTarget.y + 0.02, this.strikeTarget.z,
			6, 0.55, 0.01, 0.55, 0.0);

		serverLevel.sendParticles(ParticleTypes.END_ROD,
			this.blaze.getX(), this.blaze.getY() + 0.85, this.blaze.getZ(),
			1, 0.05, 0.05, 0.05, 0.0);
	}

	private void damageArcPath(ServerLevel serverLevel, Vec3 origin, Vec3 strikePos, Set<UUID> hitTargets) {
		Vec3 path = strikePos.subtract(origin);
		double lengthSqr = path.lengthSqr();
		if (lengthSqr < 1.0E-4) return;

		AABB hitBox = new AABB(origin, strikePos).inflate(ARC_RADIUS + 0.6);
		List<LivingEntity> hitEntities = serverLevel.getEntitiesOfClass(LivingEntity.class, hitBox,
			entity -> entity != this.blaze && entity.isAlive());

		for (LivingEntity hit : hitEntities) {
			Vec3 sample = hit.getEyePosition();
			double distanceSqr = distanceToSegmentSqr(sample, origin, strikePos, lengthSqr);
			if (distanceSqr > ARC_RADIUS * ARC_RADIUS) continue;

			double progress = progressAlongSegment(sample, origin, path, lengthSqr);
			Vec3 pushDir = path.normalize();
			hit.hurt(this.blaze.damageSources().magic(), ARC_DAMAGE);
			hit.setDeltaMovement(hit.getDeltaMovement()
				.add(pushDir.scale(0.14 + 0.08 * progress))
				.add(0.0, 0.08, 0.0));
			hit.hurtMarked = true;
			hitTargets.add(hit.getUUID());
		}
	}

	private void damageImpact(ServerLevel serverLevel, Vec3 strikePos, Set<UUID> hitTargets) {
		AABB hitBox = new AABB(
			strikePos.x - IMPACT_RADIUS, strikePos.y - 1.0, strikePos.z - IMPACT_RADIUS,
			strikePos.x + IMPACT_RADIUS, strikePos.y + 2.2, strikePos.z + IMPACT_RADIUS
		);
		List<LivingEntity> hitEntities = serverLevel.getEntitiesOfClass(LivingEntity.class, hitBox,
			entity -> entity != this.blaze && entity.isAlive());

		for (LivingEntity hit : hitEntities) {
			if (hitTargets.contains(hit.getUUID())) continue;
			double distance = hit.position().distanceTo(strikePos);
			if (distance > IMPACT_RADIUS) continue;

			double scale = 1.0 - (distance / IMPACT_RADIUS);
			Vec3 push = hit.position().subtract(strikePos);
			if (push.lengthSqr() < 1.0E-4) {
				push = hit.position().subtract(this.blaze.position());
			}
			if (push.lengthSqr() < 1.0E-4) {
				push = new Vec3(0.0, 0.0, 1.0);
			}
			Vec3 pushDir = push.normalize();

			hit.hurt(this.blaze.damageSources().magic(), Math.max(3.0F, IMPACT_DAMAGE * (float) scale));
			hit.setDeltaMovement(hit.getDeltaMovement()
				.add(pushDir.scale(0.28 + 0.18 * scale))
				.add(0.0, 0.18 + 0.1 * scale, 0.0));
			hit.hurtMarked = true;
			hitTargets.add(hit.getUUID());
		}
	}

	private double distanceToSegmentSqr(Vec3 point, Vec3 start, Vec3 end, double lengthSqr) {
		double progress = progressAlongSegment(point, start, end.subtract(start), lengthSqr);
		Vec3 closest = start.lerp(end, progress);
		return point.distanceToSqr(closest);
	}

	private double progressAlongSegment(Vec3 point, Vec3 start, Vec3 path, double lengthSqr) {
		double progress = point.subtract(start).dot(path) / lengthSqr;
		return Math.max(0.0, Math.min(1.0, progress));
	}

	private void moveTowards(Vec3 targetPos, double speed) {
		this.blaze.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, speed);
	}

	private int randomCooldown() {
		return this.blaze.getRandom().nextInt(((COOLDOWN_MAX) - (COOLDOWN_MIN)) + 1) + (COOLDOWN_MIN);
	}

	private boolean isValidTarget(LivingEntity livingEntity) {
		if (livingEntity == null || !livingEntity.isAlive()) return false;
		if (livingEntity instanceof Player player) {
			return !player.isCreative() && !player.isSpectator();
		}
		return true;
	}
}


