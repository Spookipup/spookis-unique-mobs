package spookipup.uniquemobs.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.variant.ghast.WitherGhastEntity;

import java.util.EnumSet;

// keeps the wither ghast in a good pull position
public class WitherGhastMovementGoal extends Goal {

	private final WitherGhastEntity ghast;

	private static final double PREFERRED_MIN_DIST = 12.0;
	private static final double PREFERRED_MAX_DIST = 25.0;
	private static final double STRAFE_SPEED = 0.02;
	private static final double REPOSITION_SPEED = 0.08;
	private static final double RANGE_ADJUST_SPEED = 0.04;
	private static final int LOS_LOST_PATIENCE = 15;
	private static final double ALTITUDE_PREFERENCE = 10.0;

	private LivingEntity target;
	private int losLostTicks;
	private double strafeAngle;
	private int strafeChangeTimer;

	public WitherGhastMovementGoal(WitherGhastEntity ghast) {
		this.ghast = ghast;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		LivingEntity t = this.ghast.getTarget();
		return t != null && t.isAlive();
	}

	@Override
	public boolean canContinueToUse() {
		return this.target != null && this.target.isAlive();
	}

	@Override
	public void start() {
		this.target = this.ghast.getTarget();
		this.losLostTicks = 0;
		this.strafeAngle = this.ghast.getRandom().nextDouble() * Math.PI * 2;
		this.strafeChangeTimer = 40 + this.ghast.getRandom().nextInt(40);
	}

	@Override
	public void stop() {
		this.target = null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (this.target == null) return;

		if (this.ghast.isFiring()) return;

		boolean hasLOS = checkLOS();
		double dist = this.ghast.distanceTo(this.target);

		if (hasLOS) {
			this.losLostTicks = 0;
			tickWithSight(dist);
		} else {
			this.losLostTicks++;
			tickWithoutSight(dist);
		}
	}

	private void tickWithSight(double dist) {
		Vec3 vel = this.ghast.getDeltaMovement();
		Vec3 toTarget = this.target.position().subtract(this.ghast.position()).normalize();

		if (dist < PREFERRED_MIN_DIST) {
			vel = vel.add(toTarget.scale(-RANGE_ADJUST_SPEED));
		} else if (dist > PREFERRED_MAX_DIST) {
			vel = vel.add(toTarget.scale(RANGE_ADJUST_SPEED));
		}

		this.strafeChangeTimer--;
		if (this.strafeChangeTimer <= 0) {
			this.strafeAngle += Math.PI + (this.ghast.getRandom().nextDouble() - 0.5) * 0.8;
			this.strafeChangeTimer = 40 + this.ghast.getRandom().nextInt(60);
		}

		Vec3 strafeDir = perpendicular(toTarget, this.strafeAngle);
		vel = vel.add(strafeDir.scale(STRAFE_SPEED));

		double targetY = this.target.getY() + ALTITUDE_PREFERENCE;
		double yDiff = targetY - this.ghast.getY();
		vel = vel.add(0, Math.signum(yDiff) * Math.min(Math.abs(yDiff) * 0.005, 0.02), 0);

		this.ghast.setDeltaMovement(vel);
	}

	private void tickWithoutSight(double dist) {
		if (this.losLostTicks < LOS_LOST_PATIENCE) return;

		Vec3 toTarget = this.target.getEyePosition().subtract(this.ghast.position()).normalize();
		Vec3 vel = this.ghast.getDeltaMovement();
		vel = vel.add(toTarget.x * REPOSITION_SPEED, 0, toTarget.z * REPOSITION_SPEED);
		vel = vel.add(0, REPOSITION_SPEED * 0.7, 0);
		this.ghast.setDeltaMovement(vel);
	}

	private boolean checkLOS() {
		Vec3 from = this.ghast.getEyePosition();
		Vec3 to = this.target.getEyePosition();
		HitResult hit = this.ghast.level().clip(new ClipContext(
			from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.ghast));
		return hit.getType() == HitResult.Type.MISS
			|| hit.getLocation().distanceToSqr(to) < 1.0;
	}

	private Vec3 perpendicular(Vec3 forward, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return new Vec3(-forward.z * cos + forward.x * sin, 0, forward.x * cos + forward.z * sin).normalize();
	}
}


