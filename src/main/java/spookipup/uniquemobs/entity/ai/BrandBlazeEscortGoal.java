package spookipup.uniquemobs.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.variant.blaze.BrandBlazeEntity;

import java.util.Comparator;
import java.util.EnumSet;

// keeps brand blazes near the pack when they aren't fighting
public class BrandBlazeEscortGoal extends Goal {

	private static final double SEARCH_RANGE = 20.0;
	private static final double ESCORT_RADIUS = 4.0;
	private static final int RETARGET_TICKS_MIN = 20;
	private static final int RETARGET_TICKS_MAX = 40;

	private final BrandBlazeEntity blaze;

	private Mob anchor;
	private int retargetTicks;
	private double orbitAngle;

	public BrandBlazeEscortGoal(BrandBlazeEntity blaze) {
		this.blaze = blaze;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.blaze.getTarget() != null) return false;
		this.anchor = findAnchor();
		return this.anchor != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.blaze.getTarget() == null && isValidAnchor(this.anchor);
	}

	@Override
	public void start() {
		this.retargetTicks = randomRetargetTicks();
		this.orbitAngle = this.blaze.getRandom().nextDouble() * Math.PI * 2.0;
	}

	@Override
	public void stop() {
		this.anchor = null;
		this.retargetTicks = 0;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (!isValidAnchor(this.anchor)) {
			this.anchor = findAnchor();
			if (this.anchor == null) return;
		}

		this.retargetTicks--;
		if (this.retargetTicks <= 0) {
			Mob better = findAnchor();
			if (better != null) {
				this.anchor = better;
			}
			this.retargetTicks = randomRetargetTicks();
		}

		this.blaze.getLookControl().setLookAt(this.anchor, 30.0F, 30.0F);
		this.orbitAngle += 0.07 + this.blaze.getRandom().nextDouble() * 0.015;

		Vec3 center = this.anchor.position().add(0.0, this.anchor.getBbHeight() * 0.75 + 1.2, 0.0);
		Vec3 orbitOffset = new Vec3(Math.cos(this.orbitAngle), 0.0, Math.sin(this.orbitAngle)).scale(ESCORT_RADIUS);
		Vec3 targetPos = center.add(orbitOffset).add(0.0, Math.sin(this.blaze.tickCount * 0.12) * 0.35, 0.0);
		this.blaze.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, 0.6);
	}

	private Mob findAnchor() {
		return this.blaze.level().getEntitiesOfClass(Mob.class, this.blaze.getBoundingBox().inflate(SEARCH_RANGE), this::isValidAnchor)
			.stream()
			.min(Comparator.comparingDouble(mob -> mob.distanceToSqr(this.blaze)))
			.orElse(null);
	}

	private boolean isValidAnchor(Mob mob) {
		return mob != null
			&& mob != this.blaze
			&& mob.isAlive()
			&& mob instanceof Enemy
			&& mob.getTarget() != this.blaze
			&& !(mob instanceof BrandBlazeEntity)
			&& mob.distanceToSqr(this.blaze) <= SEARCH_RANGE * SEARCH_RANGE;
	}

	private int randomRetargetTicks() {
		return this.blaze.getRandom().nextIntBetweenInclusive(RETARGET_TICKS_MIN, RETARGET_TICKS_MAX);
	}
}
