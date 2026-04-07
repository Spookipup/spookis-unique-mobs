package spookipup.uniquemobs.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

// pounce from medium range in a high arc
public class LeapAttackGoal extends Goal {

	private final Mob mob;
	private final float minDistanceSq;
	private final float maxDistanceSq;
	private final float leapHeight;
	private final float horizontalForce;
	private final int baseCooldown;
	private LivingEntity target;
	private int cooldown;

	public LeapAttackGoal(Mob mob, float minDistance, float maxDistance,
						  float leapHeight, float horizontalForce, int baseCooldown) {
		this.mob = mob;
		this.minDistanceSq = minDistance * minDistance;
		this.maxDistanceSq = maxDistance * maxDistance;
		this.leapHeight = leapHeight;
		this.horizontalForce = horizontalForce;
		this.baseCooldown = baseCooldown;

		this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.cooldown > 0) {
			this.cooldown--;
			return false;
		}

		this.target = this.mob.getTarget();
		if (this.target == null || !this.target.isAlive()) return false;

		double distSq = this.mob.distanceToSqr(this.target);
		return distSq >= this.minDistanceSq && distSq <= this.maxDistanceSq && this.mob.onGround();
	}

	@Override
	public boolean canContinueToUse() {
		return false;
	}

	@Override
	public void start() {
		Vec3 toTarget = new Vec3(
			this.target.getX() - this.mob.getX(),
			0,
			this.target.getZ() - this.mob.getZ()
		);

		double dist = toTarget.length();
		Vec3 direction = toTarget.normalize();

		// scale horizontal force with distance so short leaps don't overshoot
		// and long leaps actually reach
		double distFactor = dist / Math.sqrt(this.maxDistanceSq);
		double hForce = this.horizontalForce * (0.6 + 0.4 * distFactor);

		this.mob.setDeltaMovement(
			direction.x * hForce,
			this.leapHeight,
			direction.z * hForce
		);
		this.cooldown = this.baseCooldown + this.mob.getRandom().nextInt(10);
	}
}
