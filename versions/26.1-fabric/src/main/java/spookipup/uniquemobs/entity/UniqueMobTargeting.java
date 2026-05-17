package spookipup.uniquemobs.entity;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public final class UniqueMobTargeting {

	private UniqueMobTargeting() {
	}

	public static boolean canAttackTarget(Mob mob, LivingEntity target) {
		return mob.level().getDifficulty() != Difficulty.PEACEFUL
			&& isValidTarget(target);
	}

	public static boolean isValidTarget(LivingEntity target) {
		if (target == null || !target.isAlive()) return false;
		return !(target instanceof Player player) || (!player.isCreative() && !player.isSpectator());
	}

	public static void clearTargetIfInvalid(Mob mob) {
		LivingEntity target = mob.getTarget();
		if (target != null && !canAttackTarget(mob, target)) {
			mob.setTarget(null);
		}
	}
}
