package spookipup.uniquemobs.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

// circles target at range, backs off when close, approaches when far.
// MOVE + LOOK flags - pair with no-flag attack goals
public class StrafeAndRetreatGoal extends Goal {

	private final Mob mob;
	private final double strafeSpeed;
	private final float retreatDistanceSq;
	private final float minDistanceSq;
	private final float maxDistanceSq;
	private final boolean avoidFalls;
	private final int maxFallDistance;

	private LivingEntity target;
	private int strafeDirection;
	private int strafeSwitchTimer;
	private int repathTimer;

	public StrafeAndRetreatGoal(Mob mob, double strafeSpeed, float retreatDistance, float minDistance, float maxDistance) {
		this(mob, strafeSpeed, retreatDistance, minDistance, maxDistance, false, 3);
	}

	public StrafeAndRetreatGoal(Mob mob, double strafeSpeed, float retreatDistance, float minDistance, float maxDistance,
								boolean avoidFalls, int maxFallDistance) {
		this.mob = mob;
		this.strafeSpeed = strafeSpeed;
		this.retreatDistanceSq = retreatDistance * retreatDistance;
		this.minDistanceSq = minDistance * minDistance;
		this.maxDistanceSq = maxDistance * maxDistance;
		this.avoidFalls = avoidFalls;
		this.maxFallDistance = maxFallDistance;

		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		LivingEntity target = this.mob.getTarget();
		if (target != null && target.isAlive()) {
			this.target = target;
			return true;
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return this.target != null && this.target.isAlive() && this.mob.getTarget() == this.target;
	}

	@Override
	public void start() {
		this.strafeDirection = this.mob.getRandom().nextBoolean() ? 1 : -1;
		this.strafeSwitchTimer = 20 + this.mob.getRandom().nextInt(40);
		this.repathTimer = 0;
	}

	@Override
	public void stop() {
		this.target = null;
		this.mob.getNavigation().stop();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (this.target == null) return;

		double distanceSq = this.mob.distanceToSqr(this.target);
		boolean canSee = this.mob.getSensing().hasLineOfSight(this.target);

		this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

		this.strafeSwitchTimer--;
		if (this.strafeSwitchTimer <= 0) {
			this.strafeDirection = this.mob.getRandom().nextBoolean() ? 1 : -1;
			this.strafeSwitchTimer = 20 + this.mob.getRandom().nextInt(40);
		}

		this.repathTimer--;

		if (distanceSq < this.retreatDistanceSq) {
			if (this.repathTimer <= 0) {
				navigateAwayFrom(8.0, 1.2);
				this.repathTimer = 5;
			}
		} else if (distanceSq < this.minDistanceSq) {
			if (this.repathTimer <= 0) {
				navigateAwayFrom(6.0, 1.0);
				this.repathTimer = 10;
			}
		} else if (distanceSq > this.maxDistanceSq || !canSee) {
			if (this.repathTimer <= 0) {
				this.mob.getNavigation().moveTo(this.target, 1.0);
				this.repathTimer = 15;
			}
		} else {
			this.mob.getNavigation().stop();
			if (this.avoidFalls && wouldFallStrafing(this.strafeDirection)) {
				if (!wouldFallStrafing(-this.strafeDirection)) {
					this.strafeDirection = -this.strafeDirection;
				} else {
					// both sides are drops, just stand still
					return;
				}
			}
			this.mob.getMoveControl().strafe(0, this.strafeDirection * (float) this.strafeSpeed);
		}
	}

	private boolean wouldFallStrafing(int direction) {
		float yaw = this.mob.getYRot() * Mth.DEG_TO_RAD;
		// strafe is perpendicular to facing: right = +90°
		double strafeX = -Math.sin(yaw + direction * Mth.HALF_PI);
		double strafeZ = Math.cos(yaw + direction * Mth.HALF_PI);

		// check ~1.5 blocks ahead in the strafe direction
		double checkX = this.mob.getX() + strafeX * 1.5;
		double checkZ = this.mob.getZ() + strafeZ * 1.5;
		BlockPos feet = BlockPos.containing(checkX, this.mob.getY(), checkZ);

		Level level = this.mob.level();
		int drop = 0;
		for (int y = 0; y <= this.maxFallDistance; y++) {
			BlockState state = level.getBlockState(feet.below(y));
			if (!state.isAir() && state.getFluidState().isEmpty()) {
				return false;
			}
			drop++;
		}
		return drop > this.maxFallDistance;
	}

	private void navigateAwayFrom(double retreatBlocks, double speedMod) {
		double dx = this.mob.getX() - this.target.getX();
		double dz = this.mob.getZ() - this.target.getZ();

		double length = Math.sqrt(dx * dx + dz * dz);
		if (length < 0.001) {
			// basically on top of each other, just pick a random direction
			double angle = this.mob.getRandom().nextDouble() * Math.PI * 2;
			dx = Math.cos(angle);
			dz = Math.sin(angle);
		} else {
			dx /= length;
			dz /= length;
		}

		double goalX = this.mob.getX() + dx * retreatBlocks;
		double goalZ = this.mob.getZ() + dz * retreatBlocks;

		this.mob.getNavigation().moveTo(goalX, this.mob.getY(), goalZ, speedMod);
	}
}
