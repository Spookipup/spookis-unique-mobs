package spookipup.uniquemobs.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

// finds high ground with LOS, perches there, flees when approached
public class VantagePointGoal extends Goal {

	private enum State { SEEKING, PERCHED, FLEEING }

	private final Mob mob;
	private final double seekSpeed;
	private final double fleeSpeed;
	private final int minHeightAdvantage;
	private final int searchRadius;
	private final int searchHeight;
	private final float fleeDistanceSq;
	private final float dangerDistanceSq;
	private final int heightPanicThreshold;

	private LivingEntity target;
	private State state;
	private BlockPos vantagePos;
	private int repathTimer;
	private int searchFailures;
	private int perchedTicks;

	public VantagePointGoal(Mob mob, double seekSpeed, double fleeSpeed, int minHeightAdvantage,
							int searchRadius, int searchHeight, float fleeDistance,
							float dangerDistance, int heightPanicThreshold) {
		this.mob = mob;
		this.seekSpeed = seekSpeed;
		this.fleeSpeed = fleeSpeed;
		this.minHeightAdvantage = minHeightAdvantage;
		this.searchRadius = searchRadius;
		this.searchHeight = searchHeight;
		this.fleeDistanceSq = fleeDistance * fleeDistance;
		this.dangerDistanceSq = dangerDistance * dangerDistance;
		this.heightPanicThreshold = heightPanicThreshold;

		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		LivingEntity target = this.mob.getTarget();
		if (target == null || !target.isAlive()) return false;
		this.target = target;
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return this.target != null && this.target.isAlive() && this.mob.getTarget() == this.target;
	}

	@Override
	public void start() {
		this.state = State.SEEKING;
		this.vantagePos = null;
		this.repathTimer = 0;
		this.searchFailures = 0;
		this.perchedTicks = 0;
	}

	@Override
	public void stop() {
		this.target = null;
		this.vantagePos = null;
		this.mob.getNavigation().stop();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (this.target == null) return;

		this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

		switch (this.state) {
			case SEEKING -> tickSeeking();
			case PERCHED -> tickPerched();
			case FLEEING -> tickFleeing();
		}
	}

	private void tickSeeking() {
		double distSq = this.mob.distanceToSqr(this.target);

		// too close to search, get out first
		if (distSq < this.dangerDistanceSq) {
			startFleeing();
			return;
		}

		this.repathTimer--;
		if (this.repathTimer > 0) {
			if (this.vantagePos != null && this.mob.blockPosition().closerThan(this.vantagePos, 2.0)) {
				double heightAdv = this.mob.getY() - this.target.getY();
				if (heightAdv >= this.minHeightAdvantage) {
					startPerched();
					return;
				}
			}
			return;
		}

		this.vantagePos = findVantagePoint();
		if (this.vantagePos != null) {
			this.mob.getNavigation().moveTo(
				this.vantagePos.getX() + 0.5, this.vantagePos.getY(),
				this.vantagePos.getZ() + 0.5, this.seekSpeed);
			this.repathTimer = 30;
			this.searchFailures = 0;
		} else {
			this.searchFailures++;
			this.repathTimer = Math.min(20 + this.searchFailures * 10, 60);

			// nothing nearby, run and try again from further out
			if (this.searchFailures >= 3) {
				startFleeing();
			}
		}
	}

	private void tickPerched() {
		this.perchedTicks++;
		double distSq = this.mob.distanceToSqr(this.target);
		double heightAdv = this.mob.getY() - this.target.getY();

		boolean heightThreat = heightAdv < this.heightPanicThreshold;
		boolean tooClose = distSq < this.fleeDistanceSq;
		boolean canSee = this.mob.getSensing().hasLineOfSight(this.target);

		if (tooClose || heightThreat) {
			startFleeing();
		} else if (!canSee && this.perchedTicks > 40) {
			// lost sight, find a new perch
			this.state = State.SEEKING;
			this.vantagePos = null;
			this.repathTimer = 0;
		}
	}

	private void tickFleeing() {
		this.repathTimer--;

		double distSq = this.mob.distanceToSqr(this.target);

		// far enough, start looking for a new perch
		if (distSq > this.fleeDistanceSq * 2.0) {
			this.state = State.SEEKING;
			this.vantagePos = null;
			this.repathTimer = 0;
			this.searchFailures = 0;
			return;
		}

		if (this.repathTimer > 0) return;

		double dx = this.mob.getX() - this.target.getX();
		double dz = this.mob.getZ() - this.target.getZ();
		double length = Math.sqrt(dx * dx + dz * dz);

		if (length < 0.001) {
			double angle = this.mob.getRandom().nextDouble() * Math.PI * 2;
			dx = Math.cos(angle);
			dz = Math.sin(angle);
		} else {
			dx /= length;
			dz /= length;
		}

		this.mob.getNavigation().moveTo(
			this.mob.getX() + dx * 10.0, this.mob.getY(), this.mob.getZ() + dz * 10.0, this.fleeSpeed);
		this.repathTimer = 10;
	}

	private void startPerched() {
		this.state = State.PERCHED;
		this.mob.getNavigation().stop();
		this.perchedTicks = 0;
	}

	private void startFleeing() {
		this.state = State.FLEEING;
		this.vantagePos = null;
		this.repathTimer = 0;
	}

	private BlockPos findVantagePoint() {
		Level level = this.mob.level();
		BlockPos mobPos = this.mob.blockPosition();
		double targetY = this.target.getY();
		Vec3 targetEyes = this.target.getEyePosition();

		List<ScoredPos> candidates = new ArrayList<>();

		for (int dx = -this.searchRadius; dx <= this.searchRadius; dx += 2) {
			for (int dz = -this.searchRadius; dz <= this.searchRadius; dz += 2) {
				int x = mobPos.getX() + dx;
				int z = mobPos.getZ() + dz;

				for (int dy = this.searchHeight; dy >= -2; dy--) {
					int y = mobPos.getY() + dy;
					BlockPos check = new BlockPos(x, y, z);
					BlockState below = level.getBlockState(check.below());
					BlockState at = level.getBlockState(check);
					BlockState above = level.getBlockState(check.above());

					boolean standable = (!below.isAir() && below.getFluidState().isEmpty())
						&& (at.isAir() || at.is(BlockTags.LEAVES))
						&& (above.isAir() || above.is(BlockTags.LEAVES));

					if (!standable) continue;

					double heightAdv = y - targetY;
					if (heightAdv < this.minHeightAdvantage) continue;

					Vec3 from = new Vec3(x + 0.5, y + 1.5, z + 0.5);
					if (!hasLineOfSight(level, from, targetEyes)) continue;

					double distSq = mobPos.distSqr(check);
					double score = heightAdv * 4.0 - Math.sqrt(distSq) * 0.5;
					if (below.is(BlockTags.LEAVES)) score += 3.0;

					candidates.add(new ScoredPos(check, score));
					break;
				}
			}
		}

		if (candidates.isEmpty()) return null;

		// pick from the top few with some randomness so it doesn't always beeline the same spot
		candidates.sort((a, b) -> Double.compare(b.score, a.score));
		int pick = Math.min(candidates.size(), 3);
		return candidates.get(this.mob.getRandom().nextInt(pick)).pos;
	}

	private boolean hasLineOfSight(Level level, Vec3 from, Vec3 to) {
		HitResult hit = level.clip(new ClipContext(
			from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.mob));
		return hit.getType() == HitResult.Type.MISS
			|| hit.getLocation().distanceToSqr(to) < 1.0;
	}

	private record ScoredPos(BlockPos pos, double score) {}
}
