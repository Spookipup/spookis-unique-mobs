package spookipup.uniquemobs.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

// launches target upward when the mob lands a melee hit
public class KnockupGoal extends Goal {

	private final Mob mob;
	private final int cooldownTicks;
	private final double launchStrength;

	private int cooldown;
	private LivingEntity target;

	public KnockupGoal(Mob mob, int cooldownTicks, double launchStrength) {
		this.mob = mob;
		this.cooldownTicks = cooldownTicks;
		this.launchStrength = launchStrength;
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

		// detect when our mob just landed a hit - hurtTime resets to 10 on the tick damage is dealt
		if (this.target.hurtTime == 10 && this.target.getLastHurtByMob() == this.mob) {
			this.target.setDeltaMovement(
				this.target.getDeltaMovement().x * 0.5,
				this.launchStrength,
				this.target.getDeltaMovement().z * 0.5
			);
			this.target.hurtMarked = true;
			this.cooldown = this.cooldownTicks;
		}
	}
}
