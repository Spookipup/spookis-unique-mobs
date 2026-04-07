package spookipup.uniquemobs.entity.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import spookipup.uniquemobs.entity.variant.ghast.RagelingEntity;
import spookipup.uniquemobs.entity.variant.ghast.GreatMotherGhastEntity;
import spookipup.uniquemobs.registry.ModEntities;

// mother ghast's attack - spawns ragelings and launches them at the target
public class SpawnGhastlingGoal extends Goal {

	private final GreatMotherGhastEntity mother;
	private final int cooldownMin;
	private final int cooldownMax;
	private final int maxRagelings;

	private int cooldown;
	private int chargingFaceTimer;

	public SpawnGhastlingGoal(GreatMotherGhastEntity mother, int cooldownMin, int cooldownMax, int maxRagelings) {
		this.mother = mother;
		this.cooldownMin = cooldownMin;
		this.cooldownMax = cooldownMax;
		this.maxRagelings = maxRagelings;
	}

	@Override
	public boolean canUse() {
		LivingEntity target = this.mother.getTarget();
		return target != null && target.isAlive();
	}

	@Override
	public void start() {
		this.cooldown = 0;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		LivingEntity target = this.mother.getTarget();
		if (target == null) return;

		this.mother.getLookControl().setLookAt(target, 30.0F, 30.0F);

		if (this.chargingFaceTimer > 0) {
			this.chargingFaceTimer--;
			if (this.chargingFaceTimer == 0) this.mother.setCharging(false);
		}

		if (this.cooldown > 0) {
			this.cooldown--;
			return;
		}

		if (this.mother.countLivingRagelings() >= this.maxRagelings) return;
		if (!this.mother.getSensing().hasLineOfSight(target)) return;

		spawnRageling(target);
		this.cooldown = this.cooldownMin + this.mother.getRandom().nextInt(this.cooldownMax - this.cooldownMin + 1);
	}

	private void spawnRageling(LivingEntity target) {
		if (!(this.mother.level() instanceof ServerLevel serverLevel)) return;

		RagelingEntity rageling = ModEntities.RAGELING.get().create(serverLevel);
		if (rageling == null) return;

		rageling.setOwnerUUID(this.mother.getUUID());
		rageling.moveTo(this.mother.getX(), this.mother.getY(), this.mother.getZ(),
			this.mother.getYRot(), this.mother.getXRot());

		// launch toward the target - scale speed with distance so they actually arrive
		double dx = target.getX() - this.mother.getX();
		double dy = (target.getY() + target.getBbHeight() * 0.5) - this.mother.getY();
		double dz = target.getZ() - this.mother.getZ();
		double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (dist > 0) {
			double baseSpeed = 1.5;
			double flightTicks = dist / baseSpeed;
			dx += (target.getX() - target.xOld) * flightTicks;
			dy += (target.getY() - target.yOld) * flightTicks;
			dz += (target.getZ() - target.zOld) * flightTicks;

			double ledDist = Math.sqrt(dx * dx + dy * dy + dz * dz);

			double speed = Math.min(4.0, baseSpeed + dist * 0.02);
			rageling.setDeltaMovement(
				dx / ledDist * speed,
				dy / ledDist * speed,
				dz / ledDist * speed
			);
		}

		serverLevel.addFreshEntity(rageling);

		this.mother.setCharging(true);
		this.chargingFaceTimer = 10;
		this.mother.playSound(SoundEvents.GHAST_WARN, 1.0F, 1.0F);
	}
}
