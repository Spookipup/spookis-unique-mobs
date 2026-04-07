package spookipup.uniquemobs.entity.ai;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

// swings at whatever's in reach, doesn't chase
public class MeleeWhenCloseGoal extends Goal {

	private final Mob mob;
	private final int cooldownTicks;

	private int cooldown;
	private LivingEntity target;

	public MeleeWhenCloseGoal(Mob mob, int cooldownTicks) {
		this.mob = mob;
		this.cooldownTicks = cooldownTicks;
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

		if (this.cooldown > 0) {
			this.cooldown--;
			return;
		}

		double distanceSq = this.mob.distanceToSqr(this.target);
		double meleeReach = this.mob.getBbWidth() * 2.0 + this.target.getBbWidth();

		if (distanceSq <= meleeReach * meleeReach) {
			this.mob.swing(InteractionHand.MAIN_HAND);
			this.mob.doHurtTarget(getServerLevel(this.mob), this.target);
			this.cooldown = this.cooldownTicks;
		}
	}
}
