package spookipup.uniquemobs.entity.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

import java.util.List;

// ranged attack goal, no flags so it layers on top of movement.
// supports weighted random projectile selection + indirect fire arcs
public class ShootGoal extends Goal {

	@FunctionalInterface
	public interface ProjectileCreator {
		Projectile create(Level level, LivingEntity shooter);
	}

	public record ProjectileFactory(
		ProjectileCreator creator,
		float speed,
		float inaccuracy,
		int weight,
		SoundEvent sound,
		float volume,
		float pitchBase,
		boolean directFire
	) {
	}

	private final Mob mob;
	private final List<ProjectileFactory> factories;
	private final int totalWeight;
	private final int attackIntervalMin;
	private final int attackIntervalMax;
	private final float attackRadiusSq;
	private final int seeTimeRequired;
	private final boolean useItemWhileActive;
	private final boolean indirectFire;
	private final int indirectShotBudget;

	private Runnable afterShot;

	private int attackCooldown;
	private int seeTime;
	private int indirectShotsLeft;
	private boolean targetWasHurt;
	private LivingEntity target;
	private double prevTargetX, prevTargetY, prevTargetZ;
	private double targetVelX, targetVelY, targetVelZ;

	public ShootGoal(Mob mob, int attackIntervalMin, int attackIntervalMax, float attackRadius,
					 ProjectileFactory... factories) {
		this(mob, attackIntervalMin, attackIntervalMax, attackRadius, 5, false, false, 4, factories);
	}

	public ShootGoal(Mob mob, int attackIntervalMin, int attackIntervalMax, float attackRadius,
					 int seeTimeRequired, ProjectileFactory... factories) {
		this(mob, attackIntervalMin, attackIntervalMax, attackRadius, seeTimeRequired, false, false, 4, factories);
	}

	public ShootGoal(Mob mob, int attackIntervalMin, int attackIntervalMax, float attackRadius,
					 int seeTimeRequired, boolean useItemWhileActive, boolean indirectFire,
					 ProjectileFactory... factories) {
		this(mob, attackIntervalMin, attackIntervalMax, attackRadius, seeTimeRequired, useItemWhileActive, indirectFire, 4, factories);
	}

	public ShootGoal(Mob mob, int attackIntervalMin, int attackIntervalMax, float attackRadius,
					 int seeTimeRequired, boolean useItemWhileActive, boolean indirectFire,
					 int indirectShotBudget, ProjectileFactory... factories) {
		this.mob = mob;
		this.attackIntervalMin = attackIntervalMin;
		this.attackIntervalMax = attackIntervalMax;
		this.attackRadiusSq = attackRadius * attackRadius;
		this.seeTimeRequired = seeTimeRequired;
		this.useItemWhileActive = useItemWhileActive;
		this.indirectFire = indirectFire;
		this.indirectShotBudget = indirectShotBudget;
		this.factories = List.of(factories);
		this.totalWeight = this.factories.stream().mapToInt(ProjectileFactory::weight).sum();
	}

	public static ProjectileFactory simple(ProjectileCreator creator, float speed, float inaccuracy) {
		return new ProjectileFactory(creator, speed, inaccuracy, 1, null, 1.0F, 1.0F, false);
	}

	public static ProjectileFactory simple(ProjectileCreator creator, float speed, float inaccuracy,
										   SoundEvent sound) {
		return new ProjectileFactory(creator, speed, inaccuracy, 1, sound, 1.0F, 1.0F, false);
	}

	public static ProjectileFactory direct(ProjectileCreator creator, float speed, float inaccuracy,
										   SoundEvent sound) {
		return new ProjectileFactory(creator, speed, inaccuracy, 1, sound, 1.0F, 1.0F, true);
	}

	public static ProjectileFactory weighted(int weight, ProjectileCreator creator, float speed, float inaccuracy) {
		return new ProjectileFactory(creator, speed, inaccuracy, weight, null, 1.0F, 1.0F, false);
	}

	public static ProjectileFactory weighted(int weight, ProjectileCreator creator, float speed, float inaccuracy,
											 SoundEvent sound) {
		return new ProjectileFactory(creator, speed, inaccuracy, weight, sound, 1.0F, 1.0F, false);
	}

	public ShootGoal afterShot(Runnable callback) {
		this.afterShot = callback;
		return this;
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
		this.seeTime = 0;
		if (this.target != null) {
			this.prevTargetX = this.target.getX();
			this.prevTargetY = this.target.getY();
			this.prevTargetZ = this.target.getZ();
		}
		this.targetVelX = 0;
		this.targetVelY = 0;
		this.targetVelZ = 0;
		if (this.useItemWhileActive) {
			this.mob.startUsingItem(InteractionHand.MAIN_HAND);
		}
		this.mob.setAggressive(true);
	}

	@Override
	public void stop() {
		this.target = null;
		this.seeTime = 0;
		if (this.useItemWhileActive) {
			this.mob.stopUsingItem();
		}
		this.mob.setAggressive(false);
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (this.target == null) return;

		// track actual position change per tick - more reliable than getDeltaMovement for players
		this.targetVelX = this.target.getX() - this.prevTargetX;
		this.targetVelY = this.target.getY() - this.prevTargetY;
		this.targetVelZ = this.target.getZ() - this.prevTargetZ;
		this.prevTargetX = this.target.getX();
		this.prevTargetY = this.target.getY();
		this.prevTargetZ = this.target.getZ();

		this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

		// snap body to face target so the skeleton actually aims its bow
		double dx = this.target.getX() - this.mob.getX();
		double dz = this.target.getZ() - this.mob.getZ();
		float targetYRot = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
		this.mob.setYRot(Mth.rotLerp(0.5F, this.mob.getYRot(), targetYRot));
		this.mob.yBodyRot = this.mob.getYRot();

		double distanceSq = this.mob.distanceToSqr(this.target);
		boolean canSee = this.mob.getSensing().hasLineOfSight(this.target);

		if (canSee) {
			this.seeTime++;
			this.indirectShotsLeft = this.indirectShotBudget;
		} else {
			this.seeTime = 0;

			// blind shot landed - refill the budget
			if (this.indirectFire && this.target.hurtTime == 10 && this.target.getLastHurtByMob() == this.mob) {
				if (!this.targetWasHurt) {
					this.indirectShotsLeft = this.indirectShotBudget;
					this.targetWasHurt = true;
				}
			} else {
				this.targetWasHurt = false;
			}
		}

		if (this.attackCooldown > 0) {
			this.attackCooldown--;
		}

		if (distanceSq > this.attackRadiusSq || this.attackCooldown > 0) return;

		if (canSee && this.seeTime >= this.seeTimeRequired) {
			shoot(false);
			this.attackCooldown = this.mob.getRandom().nextIntBetweenInclusive(this.attackIntervalMin, this.attackIntervalMax);
		} else if (this.indirectFire && !canSee && this.indirectShotsLeft > 0) {
			shoot(true);
			this.indirectShotsLeft--;
			this.attackCooldown = this.mob.getRandom().nextIntBetweenInclusive(this.attackIntervalMin, this.attackIntervalMax);
		}
	}

	private void shoot(boolean highArc) {
		if (!(this.mob.level() instanceof ServerLevel serverLevel)) return;

		ProjectileFactory factory = pickFactory();
		Projectile projectile = factory.creator().create(this.mob.level(), this.mob);

		double dx = this.target.getX() - this.mob.getX();
		double dy = this.target.getY(0.3333333333) - projectile.getY();
		double dz = this.target.getZ() - this.mob.getZ();
		float inaccuracy = factory.inaccuracy();

		if (factory.directFire()) {
			// straight-line projectiles (wither skulls etc) - aim directly, no gravity comp
		} else {
			double horizontalDist = Math.sqrt(dx * dx + dz * dz);
			double travelTicks = Math.max(1.0, horizontalDist / factory.speed());

			// lead the target using observed position deltas (not getDeltaMovement)
			dx += this.targetVelX * travelTicks;
			dy += this.targetVelY * travelTicks;
			dz += this.targetVelZ * travelTicks;

			if (highArc) {
				// first pass: find the arc angle and its actual flight time
				double baseHDist = Math.sqrt(dx * dx + dz * dz);
				double targetHeight = dy;
				double v = factory.speed();
				double bestTan = 1.0;
				double bestError = Double.MAX_VALUE;
				int bestFlightTime = 1;

				for (int deg = 30; deg <= 88; deg++) {
					double rad = Math.toRadians(deg);
					double svx = v * Math.cos(rad);
					double svy = v * Math.sin(rad);
					double sx = 0, sy = 0;

					for (int t = 0; t < 200; t++) {
						sx += svx;
						sy += svy;
						svx *= 0.99;
						svy = svy * 0.99 - 0.05;

						if (svy < 0 && sy <= targetHeight) {
							double error = Math.abs(sx - baseHDist);
							if (error < bestError) {
								bestError = error;
								bestTan = Math.tan(rad);
								bestFlightTime = t + 1;
							}
							break;
						}
					}
				}

				// re-lead using the actual flight time of the chosen arc, not flat trajectory time
				dx = (this.target.getX() - this.mob.getX()) + this.targetVelX * bestFlightTime;
				dz = (this.target.getZ() - this.mob.getZ()) + this.targetVelZ * bestFlightTime;
				double ledHDist = Math.sqrt(dx * dx + dz * dz);
				dy = bestTan * ledHDist;
				inaccuracy += 0.1F;
			} else {
				// flat trajectory - just compensate for gravity drop
				dy += 0.025 * travelTicks * travelTicks;
			}
		}

		if (factory.sound() != null) {
			float pitch = factory.pitchBase() + (this.mob.getRandom().nextFloat() - 0.5F) * 0.2F;
			this.mob.playSound(factory.sound(), factory.volume(), pitch);
		}

		projectile.shoot(dx, dy, dz, factory.speed(), inaccuracy);
		serverLevel.addFreshEntity(projectile);

		if (this.afterShot != null) {
			this.afterShot.run();
		}
	}

	private ProjectileFactory pickFactory() {
		if (this.factories.size() == 1) {
			return this.factories.get(0);
		}

		int roll = this.mob.getRandom().nextInt(this.totalWeight);
		for (ProjectileFactory factory : this.factories) {
			roll -= factory.weight();
			if (roll < 0) return factory;
		}
		return this.factories.get(this.factories.size() - 1);
	}
}
