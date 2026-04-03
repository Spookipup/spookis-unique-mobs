package spookipup.uniquemobs.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.variant.ghast.RagelingEntity;

import java.util.EnumSet;

// squid-like burst movement - kicks of thrust toward the target then coasts on momentum
public class RagelingBurstMoveGoal extends Goal {

	private final RagelingEntity rageling;
	private final float thrustStrength;
	private final int idleMin;
	private final int idleMax;
	private final int burstMin;
	private final int burstMax;

	private boolean bursting;
	private int stateTimer;

	// drag applied each tick - 0.95 means it keeps 95% of its speed per tick
	private static final double COAST_DRAG = 0.95;
	// lighter drag during active thrust so impulses stack properly
	private static final double BURST_DRAG = 0.97;

	public RagelingBurstMoveGoal(RagelingEntity rageling, float thrustStrength,
								 int idleMin, int idleMax, int burstMin, int burstMax) {
		this.rageling = rageling;
		this.thrustStrength = thrustStrength;
		this.idleMin = idleMin;
		this.idleMax = idleMax;
		this.burstMin = burstMin;
		this.burstMax = burstMax;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		return this.rageling.getTarget() != null && this.rageling.getTarget().isAlive();
	}

	@Override
	public void start() {
		this.bursting = false;
		this.stateTimer = randomIdle();
	}

	@Override
	public void stop() {
		// don't kill momentum on stop - let them drift
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		LivingEntity target = this.rageling.getTarget();
		if (target == null) return;

		this.rageling.getLookControl().setLookAt(target, 30.0F, 30.0F);

		this.stateTimer--;
		if (this.stateTimer <= 0) {
			this.bursting = !this.bursting;
			this.stateTimer = this.bursting ? randomBurst() : randomIdle();
			// don't override charging while fusing - fuse locks it on
			if (!this.rageling.isFusing()) {
				this.rageling.setCharging(this.bursting);
			}
		}

		Vec3 vel = this.rageling.getDeltaMovement();

		if (this.bursting) {
			double dx = target.getX() - this.rageling.getX();
			double dy = (target.getY() + target.getBbHeight() * 0.5) - this.rageling.getY();
			double dz = target.getZ() - this.rageling.getZ();
			double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

			if (dist > 0.5) {
				double wobbleX = (this.rageling.getRandom().nextDouble() - 0.5) * 0.05;
				double wobbleZ = (this.rageling.getRandom().nextDouble() - 0.5) * 0.05;

				// additive thrust on top of current velocity
				double thrust = this.thrustStrength * 0.15;
				this.rageling.setDeltaMovement(
					vel.x * BURST_DRAG + dx / dist * thrust + wobbleX,
					vel.y * BURST_DRAG + dy / dist * thrust,
					vel.z * BURST_DRAG + dz / dist * thrust + wobbleZ
				);
			}
		} else {
			// coasting - gentle drag lets them overshoot and drift past the target
			this.rageling.setDeltaMovement(vel.x * COAST_DRAG, vel.y * COAST_DRAG, vel.z * COAST_DRAG);
		}
	}

	private int randomIdle() {
		return this.rageling.getRandom().nextIntBetweenInclusive(this.idleMin, this.idleMax);
	}

	private int randomBurst() {
		return this.rageling.getRandom().nextIntBetweenInclusive(this.burstMin, this.burstMax);
	}
}
