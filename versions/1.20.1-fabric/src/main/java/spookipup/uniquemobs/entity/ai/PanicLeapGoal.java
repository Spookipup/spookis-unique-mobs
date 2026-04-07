package spookipup.uniquemobs.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PanicLeapGoal extends Goal {

	private final Mob mob;
	private final float triggerDistanceSq;
	private final float leapVelocity;
	private LivingEntity target;
	private int cooldown;

	public PanicLeapGoal(Mob mob, float triggerDistance, float leapVelocity) {
		this.mob = mob;
		this.triggerDistanceSq = triggerDistance * triggerDistance;
		this.leapVelocity = leapVelocity;

		this.setFlags(EnumSet.of(Goal.Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (this.cooldown > 0) {
			this.cooldown--;
			return false;
		}

		this.target = this.mob.getTarget();
		if (this.target == null || !this.target.isAlive()) return false;

		return this.mob.distanceToSqr(this.target) <= this.triggerDistanceSq && this.mob.onGround();
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
		).normalize();

		this.mob.setDeltaMovement(toTarget.x * 0.6, this.leapVelocity, toTarget.z * 0.6);
		this.cooldown = 60 + this.mob.getRandom().nextInt(40);
	}
}
