package spookipup.uniquemobs.entity.ai;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.projectile.WitherAshBoltEntity;
import spookipup.uniquemobs.entity.variant.blaze.WitherBlazeEntity;

import java.util.EnumSet;

// its ash clouds are meant to feed the next volley
public class WitherBlazeAttackGoal extends Goal {

	private static final double ATTACK_TRIGGER_RANGE = 22.0;
	private static final double PREFERRED_MIN_DIST = 8.0;
	private static final double PREFERRED_MAX_DIST = 14.0;
	private static final int WINDUP_TICKS = 18;
	private static final int SHOT_INTERVAL = 8;
	private static final int BASE_COOLDOWN = 48;
	private static final int EMPOWERED_COOLDOWN = 30;
	private static final float SHOT_SPEED = 0.72F;

	private final WitherBlazeEntity blaze;

	private LivingEntity target;
	private int phaseTicks;
	private int cooldownTicks;
	private int shotsRemaining;
	private Phase phase = Phase.APPROACH;

	private enum Phase {
		APPROACH,
		WINDUP,
		VOLLEY,
		COOLDOWN
	}

	public WitherBlazeAttackGoal(WitherBlazeEntity blaze) {
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
		this.shotsRemaining = 0;
		this.cooldownTicks = 10;
	}

	@Override
	public void stop() {
		this.target = null;
		this.phase = Phase.APPROACH;
		this.phaseTicks = 0;
		this.shotsRemaining = 0;
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
			case APPROACH -> tickApproach();
			case WINDUP -> tickWindup();
			case VOLLEY -> tickVolley();
			case COOLDOWN -> tickCooldown();
		}
	}

	private void tickApproach() {
		double distance = this.blaze.distanceTo(this.target);
		Vec3 targetPos = this.target.position();
		double desiredY = this.target.getY() + this.target.getBbHeight() * 0.6 + 1.5;

		if (distance > PREFERRED_MAX_DIST) {
			moveTowards(targetPos.add(0.0, this.target.getBbHeight() * 0.35, 0.0), 0.9);
		} else if (distance < PREFERRED_MIN_DIST) {
			Vec3 away = this.blaze.position().subtract(targetPos);
			if (away.lengthSqr() < 1.0E-4) away = new Vec3(1.0, 0.0, 0.0);
			Vec3 retreatPoint = this.blaze.position().add(away.normalize().scale(3.8));
			moveTowards(new Vec3(retreatPoint.x, desiredY, retreatPoint.z), 0.8);
		} else {
			moveTowards(new Vec3(this.blaze.getX(), desiredY, this.blaze.getZ()), 0.55);
		}

		if (this.cooldownTicks > 0) {
			this.cooldownTicks--;
			return;
		}

		if (distance <= ATTACK_TRIGGER_RANGE && this.blaze.getSensing().hasLineOfSight(this.target)) {
			this.phase = Phase.WINDUP;
			this.phaseTicks = WINDUP_TICKS;
			this.shotsRemaining = this.blaze.isEmpowered() ? 3 : 2;
			if (this.blaze.level() instanceof ServerLevel serverLevel) {
				serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
					SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.0F, 0.85F);
			}
		}
	}

	private void tickWindup() {
		this.phaseTicks--;
		moveTowards(this.blaze.position().add(0.0, 0.05, 0.0), 0.25);

		if (!(this.blaze.level() instanceof ServerLevel serverLevel)) return;

		serverLevel.sendParticles(ParticleTypes.SMOKE,
			this.blaze.getX(), this.blaze.getY() + 0.8, this.blaze.getZ(),
			4, 0.18, 0.18, 0.18, 0.01);
		serverLevel.sendParticles(ParticleTypes.WHITE_ASH,
			this.blaze.getX(), this.blaze.getY() + 0.75, this.blaze.getZ(),
			6, 0.22, 0.15, 0.22, 0.01);

		if (this.phaseTicks % 6 == 0) {
			serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
				SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.9F, 0.6F);
		}

		if (this.phaseTicks <= 0) {
			this.phase = Phase.VOLLEY;
			this.phaseTicks = 0;
		}
	}

	private void tickVolley() {
		if (!(this.blaze.level() instanceof ServerLevel serverLevel)) return;

		if (this.phaseTicks > 0) {
			this.phaseTicks--;
			return;
		}

		fireAshBolt(serverLevel);
		this.shotsRemaining--;
		if (this.shotsRemaining > 0) {
			this.phaseTicks = SHOT_INTERVAL;
		} else {
			this.phase = Phase.COOLDOWN;
			this.cooldownTicks = this.blaze.isEmpowered() ? EMPOWERED_COOLDOWN : BASE_COOLDOWN;
		}
	}

	private void tickCooldown() {
		double desiredY = this.target.getY() + this.target.getBbHeight() * 0.6 + 1.4;
		moveTowards(new Vec3(this.blaze.getX(), desiredY, this.blaze.getZ()), 0.45);
		if (this.cooldownTicks > 0) {
			this.cooldownTicks--;
		}
		if (this.cooldownTicks <= 0) {
			this.phase = Phase.APPROACH;
		}
	}

	private void fireAshBolt(ServerLevel serverLevel) {
		Vec3 origin = this.blaze.getEyePosition().add(0.0, -0.15, 0.0);
		Vec3 targetPos = this.target.position()
			.add(this.target.getDeltaMovement().scale(8.0))
			.add(0.0, this.target.getBbHeight() * 0.55, 0.0);
		Vec3 toTarget = targetPos.subtract(origin);

		WitherAshBoltEntity bolt = new WitherAshBoltEntity(serverLevel, this.blaze);
		bolt.setPos(origin.x, origin.y, origin.z);
		bolt.shoot(toTarget.x, toTarget.y, toTarget.z, SHOT_SPEED, 0.0F);
		serverLevel.addFreshEntity(bolt);

		serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
			SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 0.8F, 1.15F);
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
