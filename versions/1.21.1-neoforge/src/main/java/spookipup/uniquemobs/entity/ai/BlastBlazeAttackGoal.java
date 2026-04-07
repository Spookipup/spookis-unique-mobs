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
import spookipup.uniquemobs.entity.variant.blaze.BlastBlazeEntity;

import java.util.EnumSet;
import java.util.List;

// advances into blast range, detonates a fiery knockback burst, then lingers without chasing for a beat
public class BlastBlazeAttackGoal extends Goal {

	private static final double ATTACK_TRIGGER_RANGE = 4.9;
	private static final double PREFERRED_MIN_DIST = 3.8;
	private static final double PREFERRED_MAX_DIST = 6.4;
	private static final double EXPLOSION_RADIUS = 4.3;
	private static final float EXPLOSION_DAMAGE = 5.0F;
	private static final double KNOCKBACK_STRENGTH = 4.75;
	private static final int WINDUP_TICKS = 24;
	private static final int RECOVER_TICKS = 55;
	private static final int MIN_REENGAGE_TICKS = 10;

	private final BlastBlazeEntity blaze;

	private LivingEntity target;
	private Vec3 recoverAnchor = Vec3.ZERO;
	private int phaseTicks;
	private Phase phase = Phase.APPROACH;

	private enum Phase {
		APPROACH,
		WINDUP,
		RECOVER
	}

	public BlastBlazeAttackGoal(BlastBlazeEntity blaze) {
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
		if (!isValidTarget(this.target)) {
			this.blaze.setTarget(null);
			return false;
		}
		return true;
	}

	@Override
	public void start() {
		this.target = this.blaze.getTarget();
		this.phase = Phase.APPROACH;
		this.phaseTicks = 0;
		this.recoverAnchor = this.blaze.position();
	}

	@Override
	public void stop() {
		this.target = null;
		this.phase = Phase.APPROACH;
		this.phaseTicks = 0;
		this.recoverAnchor = this.blaze.position();
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
			case APPROACH -> tickApproach();
			case WINDUP -> tickWindup();
			case RECOVER -> tickRecover();
		}
	}

	private void tickApproach() {
		double distance = this.blaze.distanceTo(this.target);
		Vec3 targetPos = this.target.position();
		double desiredY = this.target.getY() + this.target.getBbHeight() * 0.8 + 1.2;

		if (distance > PREFERRED_MAX_DIST) {
			moveTowards(targetPos.add(0.0, this.target.getBbHeight() * 0.5, 0.0), 1.0);
		} else if (distance < PREFERRED_MIN_DIST) {
			Vec3 away = this.blaze.position().subtract(targetPos);
			if (away.lengthSqr() < 1.0E-4) {
				away = new Vec3(1.0, 0.0, 0.0);
			}
			Vec3 retreatPoint = this.blaze.position().add(away.normalize().scale(3.5));
			moveTowards(new Vec3(retreatPoint.x, desiredY, retreatPoint.z), 0.95);
		} else {
			moveTowards(new Vec3(this.blaze.getX(), desiredY, this.blaze.getZ()), 0.7);
		}

		if (distance <= ATTACK_TRIGGER_RANGE && this.blaze.getSensing().hasLineOfSight(this.target)) {
			this.phase = Phase.WINDUP;
			this.phaseTicks = WINDUP_TICKS;
			if (this.blaze.level() instanceof ServerLevel serverLevel) {
				serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
					SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.5F, 0.55F);
			}
		}
	}

	private void tickWindup() {
		this.phaseTicks--;
		moveTowards(this.blaze.position().add(0.0, 0.06, 0.0), 0.35);

		if (!(this.blaze.level() instanceof ServerLevel serverLevel)) return;

		serverLevel.sendParticles(ParticleTypes.FLAME,
			this.blaze.getX(), this.blaze.getY() + 0.8, this.blaze.getZ(),
			6, 0.25, 0.35, 0.25, 0.02);
		serverLevel.sendParticles(ParticleTypes.SMOKE,
			this.blaze.getX(), this.blaze.getY() + 0.7, this.blaze.getZ(),
			4, 0.18, 0.25, 0.18, 0.02);

		if (this.phaseTicks % 6 == 0) {
			serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
				SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.25F, 0.7F + this.blaze.getRandom().nextFloat() * 0.15F);
		}

		if (this.phaseTicks <= 0) {
			detonate(serverLevel);
			this.phase = Phase.RECOVER;
			this.phaseTicks = RECOVER_TICKS;
			this.recoverAnchor = this.blaze.position();
		}
	}

	private void tickRecover() {
		this.phaseTicks--;

		double distance = this.blaze.distanceTo(this.target);
		Vec3 toTarget = this.target.position().subtract(this.blaze.position());
		Vec3 holdPoint = this.recoverAnchor;

		if (distance < 5.5 && toTarget.lengthSqr() > 1.0E-4) {
			Vec3 retreat = this.blaze.position().subtract(this.target.position()).normalize().scale(2.6);
			holdPoint = this.recoverAnchor.add(retreat.x, 0.4, retreat.z);
		}

		moveTowards(new Vec3(holdPoint.x, Math.max(holdPoint.y, this.target.getY() + 1.4), holdPoint.z), 0.55);

		if (this.blaze.level() instanceof ServerLevel serverLevel && this.phaseTicks % 12 == 0) {
			serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
				this.blaze.getX(), this.blaze.getY() + 0.6, this.blaze.getZ(),
				2, 0.12, 0.15, 0.12, 0.01);
		}

		if (this.phaseTicks <= 0 && distance <= ATTACK_TRIGGER_RANGE + 3.0) {
			this.phase = Phase.APPROACH;
			this.phaseTicks = 0;
		} else if (this.phaseTicks < MIN_REENGAGE_TICKS && distance > ATTACK_TRIGGER_RANGE + 3.0) {
			this.phaseTicks = MIN_REENGAGE_TICKS;
		}
	}

	private void detonate(ServerLevel serverLevel) {
		AABB hitBox = this.blaze.getBoundingBox().inflate(EXPLOSION_RADIUS);
		List<LivingEntity> hitEntities = serverLevel.getEntitiesOfClass(LivingEntity.class, hitBox,
			entity -> entity != this.blaze && entity.isAlive());

		for (LivingEntity hit : hitEntities) {
			double distance = this.blaze.distanceTo(hit);
			if (distance > EXPLOSION_RADIUS) continue;

			Vec3 push = hit.position().subtract(this.blaze.position());
			if (push.lengthSqr() < 1.0E-4) {
				push = new Vec3(0.0, 0.0, 1.0);
			}
			Vec3 pushDir = push.normalize();
			float scaledDamage = (float) (EXPLOSION_DAMAGE * (1.0 - distance / EXPLOSION_RADIUS));
			hit.hurt(this.blaze.damageSources().onFire(), Math.max(2.0F, scaledDamage));
			hit.igniteForSeconds(4);
			hit.knockback(KNOCKBACK_STRENGTH * (1.0 - distance / (EXPLOSION_RADIUS + 0.5)), -pushDir.x, -pushDir.z);
			hit.setDeltaMovement(hit.getDeltaMovement()
				.add(pushDir.scale(0.9))
				.add(0.0, 0.5, 0.0));
			hit.hurtMarked = true;
		}

		serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
			SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.9F, 0.9F);
		serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
			SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.6F, 0.35F);
		serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
			this.blaze.getX(), this.blaze.getY() + 0.3, this.blaze.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
		serverLevel.sendParticles(ParticleTypes.FLAME,
			this.blaze.getX(), this.blaze.getY() + 0.5, this.blaze.getZ(),
			40, 1.0, 0.5, 1.0, 0.02);
		serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
			this.blaze.getX(), this.blaze.getY() + 0.4, this.blaze.getZ(),
			24, 0.7, 0.35, 0.7, 0.02);
		serverLevel.sendParticles(ParticleTypes.LAVA,
			this.blaze.getX(), this.blaze.getY() + 0.3, this.blaze.getZ(),
			12, 0.35, 0.2, 0.35, 0.01);
	}

	private void moveTowards(Vec3 targetPos, double speed) {
		this.blaze.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, speed);
	}

	private boolean isValidTarget(LivingEntity livingEntity) {
		if (livingEntity == null || !livingEntity.isAlive()) return false;
		if (livingEntity instanceof Player player) {
			return !player.isCreative() && !player.isSpectator();
		}
		return true;
	}
}


