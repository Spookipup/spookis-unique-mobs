package spookipup.uniquemobs.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.registry.ModEffects;

public final class BlossomHelper {

	public static final int BLOOM_THRESHOLD = 3;
	public static final int MARK_DURATION = 140;
	public static final int DRIFT_DURATION = 80;

	private BlossomHelper() {
	}

	public static void applyPetalMark(LivingEntity target, Entity source, int amount) {
		if (target.level().isClientSide() || amount <= 0) return;
		if (target.hasEffect(ModEffects.BLOSSOM_DRIFT)) {
			target.addEffect(new MobEffectInstance(ModEffects.BLOSSOM_DRIFT, DRIFT_DURATION, 0), source);
			spawnPetals((ServerLevel) target.level(), target, 8, 0.03);
			return;
		}

		MobEffectInstance current = target.getEffect(ModEffects.PETAL_MARK);
		int stacks = current == null ? 0 : current.getAmplifier() + 1;
		stacks = Math.min(BLOOM_THRESHOLD, stacks + amount);

		if (stacks >= BLOOM_THRESHOLD) {
			triggerDrift(target, source, DRIFT_DURATION);
		} else {
			target.addEffect(new MobEffectInstance(ModEffects.PETAL_MARK, MARK_DURATION, stacks - 1), source);
			spawnPetals((ServerLevel) target.level(), target, 6 + stacks * 3, 0.02);
		}
	}

	public static void triggerDrift(LivingEntity target, Entity source, int duration) {
		if (target.level().isClientSide()) return;

		target.removeEffect(ModEffects.PETAL_MARK);
		target.addEffect(new MobEffectInstance(ModEffects.BLOSSOM_DRIFT, duration, 0), source);
		Vec3 motion = target.getDeltaMovement();
		target.setDeltaMovement(motion.x * 0.9, Math.max(motion.y, 0.42), motion.z * 0.9);
		target.hurtMarked = true;
		target.level().playSound(null, target.blockPosition(), SoundEvents.CHERRY_LEAVES_BREAK, target.getSoundSource(), 0.9F, 1.45F);
		spawnPetals((ServerLevel) target.level(), target, 28, 0.08);
	}

	public static void applyUpwardKnockback(LivingEntity target, Entity source, double horizontalStrength, double upwardStrength) {
		Vec3 away = target.position().subtract(source.position());
		double horizontalLength = Math.sqrt(away.x * away.x + away.z * away.z);
		Vec3 sourceLook = source.getLookAngle();
		double x = horizontalLength < 0.001 ? -sourceLook.x : away.x / horizontalLength;
		double z = horizontalLength < 0.001 ? -sourceLook.z : away.z / horizontalLength;
		Vec3 motion = target.getDeltaMovement();

		target.setDeltaMovement(
			motion.x + x * horizontalStrength,
			Math.max(motion.y + upwardStrength, upwardStrength),
			motion.z + z * horizontalStrength
		);
		target.fallDistance = 0.0F;
		target.hurtMarked = true;
	}

	public static boolean shouldFloatHop(LivingEntity mob, LivingEntity target, int cooldown) {
		return cooldown <= 0
			&& mob.onGround()
			&& target.isAlive()
			&& target.getY() > mob.getY() + 0.9
			&& mob.distanceToSqr(target) < 256.0;
	}

	public static void floatHopToward(ServerLevel level, LivingEntity mob, LivingEntity target, double horizontalStrength, double upwardStrength) {
		Vec3 toward = target.position().subtract(mob.position());
		double horizontalLength = Math.sqrt(toward.x * toward.x + toward.z * toward.z);
		if (horizontalLength < 0.001) return;

		Vec3 motion = mob.getDeltaMovement();
		mob.setDeltaMovement(
			motion.x * 0.35 + toward.x / horizontalLength * horizontalStrength,
			Math.max(motion.y, upwardStrength),
			motion.z * 0.35 + toward.z / horizontalLength * horizontalStrength
		);
		mob.fallDistance = 0.0F;
		mob.hurtMarked = true;
		spawnPetals(level, mob, 12, 0.035);
	}

	public static void floatStrafeAround(ServerLevel level, LivingEntity mob, LivingEntity target, double preferredDistance, double speed, double lift) {
		Vec3 toTarget = target.position().subtract(mob.position());
		double horizontalLength = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
		if (horizontalLength < 0.001) return;

		double towardX = toTarget.x / horizontalLength;
		double towardZ = toTarget.z / horizontalLength;
		double strafeX = -towardZ;
		double strafeZ = towardX;
		double distanceOffset = horizontalLength - preferredDistance;
		double rangeCorrection = Math.max(-0.75, Math.min(0.75, distanceOffset * 0.18));
		double verticalCorrection = Math.max(-0.08, Math.min(0.24, (target.getY(0.65) - mob.getY(0.55)) * 0.08 + lift));
		Vec3 motion = mob.getDeltaMovement();

		mob.setDeltaMovement(
			motion.x * 0.62 + towardX * rangeCorrection + strafeX * speed,
			Math.max(motion.y * 0.7 + verticalCorrection, -0.025),
			motion.z * 0.62 + towardZ * rangeCorrection + strafeZ * speed
		);
		mob.fallDistance = 0.0F;
		mob.hurtMarked = true;
		if (mob.tickCount % 6 == 0) {
			spawnPetals(level, mob, 8, 0.025);
		}
	}

	public static void tickSmoothFlight(ServerLevel level, LivingEntity mob, LivingEntity target, double preferredDistance,
										double approachStrength, double orbitStrength, double verticalOffset, double maxSpeed) {
		Vec3 toTarget = target.position().subtract(mob.position());
		double horizontalLength = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
		if (horizontalLength < 0.001) return;

		double towardX = toTarget.x / horizontalLength;
		double towardZ = toTarget.z / horizontalLength;
		double strafeX = -towardZ;
		double strafeZ = towardX;
		double rangeCorrection = Math.max(-maxSpeed, Math.min(maxSpeed, (horizontalLength - preferredDistance) * approachStrength));
		double yTarget = target.getY(0.65) + verticalOffset;
		double yCorrection = Math.max(-maxSpeed * 0.45, Math.min(maxSpeed * 0.45, (yTarget - mob.getY(0.55)) * 0.08));
		double desiredX = towardX * rangeCorrection + strafeX * orbitStrength;
		double desiredZ = towardZ * rangeCorrection + strafeZ * orbitStrength;
		double desiredHorizontal = Math.sqrt(desiredX * desiredX + desiredZ * desiredZ);
		if (desiredHorizontal > maxSpeed) {
			desiredX = desiredX / desiredHorizontal * maxSpeed;
			desiredZ = desiredZ / desiredHorizontal * maxSpeed;
		}

		Vec3 motion = mob.getDeltaMovement();
		mob.setNoGravity(true);
		faceTarget(mob, target);
		if (mob instanceof Mob pathingMob) {
			pathingMob.getLookControl().setLookAt(target, 30.0F, 30.0F);
		}
		mob.setDeltaMovement(
			motion.x * 0.82 + desiredX * 0.18,
			motion.y * 0.84 + yCorrection * 0.16,
			motion.z * 0.82 + desiredZ * 0.18
		);
		mob.fallDistance = 0.0F;
		mob.hurtMarked = true;
		if (mob.tickCount % 10 == 0) {
			spawnPetals(level, mob, 5, 0.015);
		}
		if (mob.tickCount % 4 == 0) {
			spawnFloatTrail(level, mob);
		}
	}

	public static boolean shouldStartReachFlight(LivingEntity mob, LivingEntity target, int cooldown) {
		boolean invalidPath = false;
		if (mob instanceof Mob pathingMob && mob.tickCount % 20 == 0) {
			Path path = pathingMob.getNavigation().createPath(target, 0);
			invalidPath = path == null || !path.canReach();
		}

		return cooldown <= 0
			&& target.isAlive()
			&& mob.distanceToSqr(target) < 1600.0
			&& (target.getY() > mob.getY() + 1.75 || invalidPath);
	}

	public static void tickMobFloat(LivingEntity mob) {
		if (mob.onGround()) return;
		Vec3 motion = mob.getDeltaMovement();
		double y = motion.y;
		if (y < 0.18) {
			y = Math.max(y + 0.045, -0.035);
		}

		mob.setDeltaMovement(motion.x * 0.99, y, motion.z * 0.99);
		mob.fallDistance = 0.0F;
		mob.hurtMarked = true;
	}

	public static void tickNaturalSlowFall(LivingEntity mob) {
		Vec3 motion = mob.getDeltaMovement();
		if (!mob.onGround() && motion.y < -0.09) {
			mob.setDeltaMovement(motion.x, -0.09, motion.z);
			mob.hurtMarked = true;
		}
		mob.fallDistance = 0.0F;
	}

	public static void faceTarget(LivingEntity mob, LivingEntity target) {
		double dx = target.getX() - mob.getX();
		double dz = target.getZ() - mob.getZ();
		float yRot = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
		mob.setYRot(yRot);
		mob.setYHeadRot(yRot);
		mob.setYBodyRot(yRot);
	}

	public static void spawnPetals(ServerLevel level, Entity entity, int count, double speed) {
		level.sendParticles(ParticleTypes.CHERRY_LEAVES,
			entity.getX(), entity.getY(0.6), entity.getZ(),
			count, entity.getBbWidth() * 0.55, entity.getBbHeight() * 0.35, entity.getBbWidth() * 0.55, speed);
	}

	public static void spawnPetalBurst(ServerLevel level, double x, double y, double z, double radius, int count) {
		level.sendParticles(ParticleTypes.CHERRY_LEAVES, x, y, z, count, radius, radius * 0.35, radius, 0.08);
		level.sendParticles(ParticleTypes.POOF, x, y, z, Math.max(4, count / 4), radius * 0.35, radius * 0.15, radius * 0.35, 0.03);
	}

	public static void spawnFloatTrail(ServerLevel level, Entity entity) {
		level.sendParticles(ParticleTypes.CHERRY_LEAVES,
			entity.getX(), entity.getY(0.08), entity.getZ(),
			3, entity.getBbWidth() * 0.35, 0.05, entity.getBbWidth() * 0.35, 0.01);
		level.sendParticles(ParticleTypes.POOF,
			entity.getX(), entity.getY(0.05), entity.getZ(),
			1, entity.getBbWidth() * 0.22, 0.02, entity.getBbWidth() * 0.22, 0.01);
	}
}
