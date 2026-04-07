package spookipup.uniquemobs.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

// smashes through soft blocks when stuck. respects mobGriefing
public class BlockBreakGoal extends Goal {

	private final Mob mob;
	private final float maxHardness;
	private final int breakIntervalTicks;

	private int breakCooldown;
	private int stuckTicks;
	private double lastPathX, lastPathZ;

	public BlockBreakGoal(Mob mob, float maxHardness, int breakIntervalTicks) {
		this.mob = mob;
		this.maxHardness = maxHardness;
		this.breakIntervalTicks = breakIntervalTicks;
	}

	@Override
	public boolean canUse() {
		if (this.mob.level() instanceof ServerLevel serverLevel
			&& !serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return false;
		return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
	}

	@Override
	public boolean canContinueToUse() {
		return canUse();
	}

	@Override
	public void start() {
		this.stuckTicks = 0;
		this.lastPathX = this.mob.getX();
		this.lastPathZ = this.mob.getZ();
	}

	@Override
	public void stop() {
		this.stuckTicks = 0;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (this.breakCooldown > 0) {
			this.breakCooldown--;
			return;
		}

		// check if we're stuck (barely moved in the last 20 ticks)
		double dx = this.mob.getX() - this.lastPathX;
		double dz = this.mob.getZ() - this.lastPathZ;
		if (dx * dx + dz * dz < 0.5) {
			this.stuckTicks++;
		} else {
			this.stuckTicks = 0;
			this.lastPathX = this.mob.getX();
			this.lastPathZ = this.mob.getZ();
		}

		if (this.stuckTicks < 20) return;

		LivingEntity target = this.mob.getTarget();
		if (target == null) return;

		// try to break blocks between us and the target
		double toX = target.getX() - this.mob.getX();
		double toZ = target.getZ() - this.mob.getZ();
		double len = Math.sqrt(toX * toX + toZ * toZ);
		if (len < 0.1) return;
		toX /= len;
		toZ /= len;

		Level level = this.mob.level();
		BlockPos mobPos = this.mob.blockPosition();
		boolean brokeAny = false;

		// check a small area in front of the mob at feet and head height
		for (int yOff = 0; yOff <= 1; yOff++) {
			BlockPos checkPos = BlockPos.containing(
				mobPos.getX() + toX * 1.2,
				mobPos.getY() + yOff,
				mobPos.getZ() + toZ * 1.2
			);
			if (tryBreakBlock(level, checkPos)) brokeAny = true;
		}

		// also check above if the mob is tall
		if (this.mob.getBbHeight() > 2.0) {
			BlockPos headPos = BlockPos.containing(
				mobPos.getX() + toX * 1.2,
				mobPos.getY() + 2,
				mobPos.getZ() + toZ * 1.2
			);
			tryBreakBlock(level, headPos);
		}

		if (brokeAny) {
			this.breakCooldown = this.breakIntervalTicks;
			this.stuckTicks = 0;
		}
	}

	private boolean tryBreakBlock(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.isAir() || !state.getFluidState().isEmpty()) return false;

		float hardness = state.getDestroySpeed(level, pos);
		if (hardness < 0 || hardness > this.maxHardness) return false;

		level.destroyBlock(pos, true, this.mob, 1);
		return true;
	}
}
