package spookipup.uniquemobs.entity.ai;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.BrandMarkHelper;
import spookipup.uniquemobs.entity.variant.blaze.BrandBlazeEntity;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

// brands players and props up nearby hostile mobs
public class BrandBlazeAttackGoal extends Goal {

	private static final double ATTACK_TRIGGER_RANGE = 22.0;
	private static final double PREFERRED_MIN_DIST = 8.0;
	private static final double PREFERRED_MAX_DIST = 13.0;
	private static final int PLAYER_WINDUP_TICKS = 16;
	private static final int PLAYER_CHANNEL_TICKS = 22;
	private static final int ALLY_CHANNEL_TICKS = 18;
	private static final int COOLDOWN_MIN = 48;
	private static final int COOLDOWN_MAX = 72;
	private static final double SUPPORT_RANGE = 16.0;
	private static final float BRAND_BURST_DAMAGE = 6.0F;
	private static final double BRAND_BURST_RADIUS = 2.3;

	private final BrandBlazeEntity blaze;

	private LivingEntity target;
	private Mob supportTarget;
	private int phaseTicks;
	private int cooldownTicks;
	private int strafeChangeTimer;
	private double strafeAngle;
	private Phase phase = Phase.APPROACH;

	private enum Phase {
		APPROACH,
		PLAYER_WINDUP,
		PLAYER_CHANNEL,
		ALLY_CHANNEL,
		COOLDOWN
	}

	public BrandBlazeAttackGoal(BrandBlazeEntity blaze) {
		this.blaze = blaze;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		LivingEntity livingEntity = this.blaze.getTarget();
		if (!isValidPlayerTarget(livingEntity)) {
			this.blaze.setTarget(null);
			return false;
		}
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		LivingEntity currentTarget = this.blaze.getTarget();
		if (!isValidPlayerTarget(currentTarget)) {
			this.blaze.setTarget(null);
			return false;
		}
		this.target = currentTarget;
		return true;
	}

	@Override
	public void start() {
		this.target = this.blaze.getTarget();
		this.supportTarget = null;
		this.phase = Phase.APPROACH;
		this.phaseTicks = 0;
		this.cooldownTicks = 12;
		this.strafeAngle = this.blaze.getRandom().nextDouble() * Math.PI * 2.0;
		this.strafeChangeTimer = 18 + this.blaze.getRandom().nextInt(18);
	}

	@Override
	public void stop() {
		this.target = null;
		this.supportTarget = null;
		this.phase = Phase.APPROACH;
		this.phaseTicks = 0;
		this.cooldownTicks = 0;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (!isValidPlayerTarget(this.target)) {
			this.blaze.setTarget(null);
			return;
		}

		switch (this.phase) {
			case APPROACH -> tickApproach(false);
			case PLAYER_WINDUP -> tickPlayerWindup();
			case PLAYER_CHANNEL -> tickPlayerChannel();
			case ALLY_CHANNEL -> tickAllyChannel();
			case COOLDOWN -> tickApproach(true);
		}
	}

	private void tickApproach(boolean coolingDown) {
		this.blaze.getLookControl().setLookAt(this.target, 40.0F, 40.0F);
		moveAroundTarget(0.32, 0.16, 0.85);

		if (this.cooldownTicks > 0) {
			this.cooldownTicks--;
		}

		if (coolingDown) {
			if (this.cooldownTicks <= 0) {
				this.phase = Phase.APPROACH;
			}
			return;
		}

		if (this.cooldownTicks > 0) return;

		Mob ally = findSupportTarget();
		if (ally != null) {
			this.supportTarget = ally;
			this.phase = Phase.ALLY_CHANNEL;
			this.phaseTicks = ALLY_CHANNEL_TICKS;
			playChannelStart(SoundEvents.BLAZE_AMBIENT, 0.55F);
			return;
		}

		if (!this.blaze.getSensing().hasLineOfSight(this.target)) return;
		if (this.blaze.distanceTo(this.target) > ATTACK_TRIGGER_RANGE) return;

		this.phase = Phase.PLAYER_WINDUP;
		this.phaseTicks = PLAYER_WINDUP_TICKS;
		playChannelStart(SoundEvents.FIRECHARGE_USE, 0.85F);
	}

	private void tickPlayerWindup() {
		this.blaze.getLookControl().setLookAt(this.target, 40.0F, 40.0F);
		moveAroundTarget(0.24, 0.12, 0.65);
		this.phaseTicks--;

		if (!(this.blaze.level() instanceof ServerLevel serverLevel)) return;

		Vec3 mouth = this.blaze.getEyePosition().add(0.0, -0.15, 0.0);
		serverLevel.sendParticles(ParticleTypes.FLAME,
			mouth.x, mouth.y, mouth.z, 8, 0.18, 0.18, 0.18, 0.01);
		serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
			mouth.x, mouth.y, mouth.z, 4, 0.12, 0.12, 0.12, 0.01);

		if (this.phaseTicks % 4 == 0) {
			serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
				SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.1F, 0.6F + this.blaze.getRandom().nextFloat() * 0.15F);
		}

		if (this.phaseTicks <= 0) {
			this.phase = Phase.PLAYER_CHANNEL;
			this.phaseTicks = PLAYER_CHANNEL_TICKS;
			playChannelStart(SoundEvents.BLAZE_AMBIENT, 0.5F);
		}
	}

	private void tickPlayerChannel() {
		if (!this.blaze.getSensing().hasLineOfSight(this.target) || this.blaze.distanceTo(this.target) > ATTACK_TRIGGER_RANGE + 3.0) {
			this.phase = Phase.COOLDOWN;
			this.cooldownTicks = randomCooldown() / 2;
			return;
		}

		this.blaze.getLookControl().setLookAt(this.target, 40.0F, 40.0F);
		moveAroundTarget(0.4, 0.2, 0.95);
		this.phaseTicks--;

		if (!(this.blaze.level() instanceof ServerLevel serverLevel)) return;

		Vec3 start = this.blaze.getEyePosition().add(0.0, -0.15, 0.0);
		Vec3 end = this.target.getEyePosition();
		spawnBrandLine(serverLevel, start, end);
		serverLevel.sendParticles(ParticleTypes.FLAME,
			end.x, this.target.getY() + 0.1, end.z, 6, 0.2, 0.03, 0.2, 0.01);
		serverLevel.sendParticles(ParticleTypes.SMOKE,
			end.x, this.target.getY() + 0.05, end.z, 4, 0.18, 0.02, 0.18, 0.01);

		if (this.phaseTicks % 6 == 0) {
			serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
				SoundEvents.FIRE_AMBIENT, SoundSource.HOSTILE, 1.0F, 0.55F + this.blaze.getRandom().nextFloat() * 0.15F);
		}

		if (this.phaseTicks <= 0) {
			detonateBrand(serverLevel);
			this.phase = Phase.COOLDOWN;
			this.cooldownTicks = randomCooldown();
		}
	}

	private void tickAllyChannel() {
		if (!isValidSupportTarget(this.supportTarget)) {
			this.phase = Phase.COOLDOWN;
			this.cooldownTicks = randomCooldown() / 2;
			return;
		}

		this.blaze.getLookControl().setLookAt(this.supportTarget, 40.0F, 40.0F);
		Vec3 anchor = this.supportTarget.position().add(0.0, this.supportTarget.getBbHeight() * 0.9 + 1.2, 0.0);
		moveTowards(anchor, 0.7);
		this.phaseTicks--;

		if (!(this.blaze.level() instanceof ServerLevel serverLevel)) return;

		Vec3 start = this.blaze.getEyePosition().add(0.0, -0.15, 0.0);
		Vec3 end = this.supportTarget.getEyePosition();
		spawnBrandLine(serverLevel, start, end);
		serverLevel.sendParticles(ParticleTypes.FLAME,
			end.x, end.y, end.z, 5, 0.18, 0.2, 0.18, 0.01);
		serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
			end.x, end.y, end.z, 3, 0.12, 0.12, 0.12, 0.01);

		if (this.phaseTicks % 5 == 0) {
			serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
				SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.9F, 0.6F + this.blaze.getRandom().nextFloat() * 0.15F);
		}

		if (this.phaseTicks <= 0) {
			BrandMarkHelper.applyBrand(this.supportTarget);
			this.supportTarget.heal(4.0F);
			this.supportTarget = null;
			this.phase = Phase.COOLDOWN;
			this.cooldownTicks = randomCooldown();
		}
	}

	private void detonateBrand(ServerLevel serverLevel) {
		Vec3 strikePos = this.target.position();
		AABB hitBox = new AABB(
			strikePos.x - BRAND_BURST_RADIUS, strikePos.y - 0.5, strikePos.z - BRAND_BURST_RADIUS,
			strikePos.x + BRAND_BURST_RADIUS, strikePos.y + 2.0, strikePos.z + BRAND_BURST_RADIUS
		);
		List<LivingEntity> hitEntities = serverLevel.getEntitiesOfClass(LivingEntity.class, hitBox,
			entity -> entity.isAlive() && entity != this.blaze);

		for (LivingEntity hit : hitEntities) {
			double distance = hit.position().distanceTo(strikePos);
			if (distance > BRAND_BURST_RADIUS) continue;

			double scale = 1.0 - distance / BRAND_BURST_RADIUS;
			Vec3 push = hit.position().subtract(strikePos);
			if (push.lengthSqr() < 1.0E-4) {
				push = hit.position().subtract(this.blaze.position());
			}
			if (push.lengthSqr() < 1.0E-4) {
				push = new Vec3(0.0, 0.0, 1.0);
			}
			Vec3 pushDir = push.normalize();

			hit.hurt(this.blaze.damageSources().onFire(), Math.max(3.0F, BRAND_BURST_DAMAGE * (float) scale));
			hit.setSecondsOnFire(5);
			hit.setDeltaMovement(hit.getDeltaMovement().add(pushDir.scale(0.18 + 0.16 * scale)).add(0.0, 0.28 + 0.08 * scale, 0.0));
			hit.hurtMarked = true;
		}

		serverLevel.playSound(null, strikePos.x, strikePos.y, strikePos.z,
			SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.2F, 1.15F);
		serverLevel.sendParticles(ParticleTypes.FLAME,
			strikePos.x, this.target.getY() + 0.25, strikePos.z, 28, 0.45, 0.15, 0.45, 0.02);
		serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
			strikePos.x, this.target.getY() + 0.2, strikePos.z, 16, 0.35, 0.12, 0.35, 0.02);
		serverLevel.sendParticles(ParticleTypes.LAVA,
			strikePos.x, this.target.getY() + 0.1, strikePos.z, 10, 0.22, 0.06, 0.22, 0.01);
	}

	private void spawnBrandLine(ServerLevel serverLevel, Vec3 start, Vec3 end) {
		Vec3 path = end.subtract(start);
		double length = path.length();
		if (length < 1.0E-4) return;

		Vec3 dir = path.scale(1.0 / length);
		int steps = Math.max(6, (int) (length * 4.0));
		for (int i = 0; i <= steps; i++) {
			double t = (double) i / steps;
			Vec3 point = start.add(dir.scale(length * t));
			serverLevel.sendParticles(ParticleTypes.FLAME,
				point.x, point.y, point.z, 0, 0.0, 0.0, 0.0, 1.0);
			if (i % 2 == 0) {
				serverLevel.sendParticles(ParticleTypes.SMOKE,
					point.x, point.y, point.z, 0, 0.0, 0.0, 0.0, 1.0);
			}
		}
	}

	private Mob findSupportTarget() {
		AABB box = this.blaze.getBoundingBox().inflate(SUPPORT_RANGE);
		return this.blaze.level().getEntitiesOfClass(Mob.class, box, this::isValidSupportTarget).stream()
			.min(Comparator.comparingDouble(mob -> healthRatio(mob) * 10.0 + mob.distanceToSqr(this.blaze)))
			.orElse(null);
	}

	private boolean isValidSupportTarget(Mob mob) {
		return mob != null
			&& mob != this.blaze
			&& mob instanceof Enemy
			&& BrandMarkHelper.canBrand(mob)
			&& healthRatio(mob) <= 0.4
			&& (mob.hurtTime > 0 || mob.getTarget() != null)
			&& this.blaze.hasLineOfSight(mob)
			&& this.blaze.distanceTo(mob) <= SUPPORT_RANGE;
	}

	private double healthRatio(LivingEntity livingEntity) {
		return livingEntity.getHealth() / Math.max(1.0F, livingEntity.getMaxHealth());
	}

	private void moveAroundTarget(double strafeScale, double altitudeScale, double speed) {
		Vec3 toTarget = this.target.position().subtract(this.blaze.position());
		double distance = toTarget.length();
		Vec3 forward = distance > 1.0E-4 ? toTarget.scale(1.0 / distance) : new Vec3(1.0, 0.0, 0.0);
		Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
		if (side.lengthSqr() < 1.0E-4) {
			side = new Vec3(1.0, 0.0, 0.0);
		}

		this.strafeChangeTimer--;
		if (this.strafeChangeTimer <= 0) {
			this.strafeAngle += Math.PI + (this.blaze.getRandom().nextDouble() - 0.5) * 0.9;
			this.strafeChangeTimer = 18 + this.blaze.getRandom().nextInt(18);
		}

		double desiredDistance = distance;
		if (distance > PREFERRED_MAX_DIST) {
			desiredDistance = PREFERRED_MAX_DIST - 0.5;
		} else if (distance < PREFERRED_MIN_DIST) {
			desiredDistance = PREFERRED_MIN_DIST + 1.4;
		}

		Vec3 orbitSide = rotateHorizontal(side.normalize(), forward, this.strafeAngle);
		Vec3 anchor = this.target.position()
			.subtract(forward.scale(desiredDistance))
			.add(orbitSide.scale(Math.max(1.6, desiredDistance * strafeScale)))
			.add(0.0, this.target.getBbHeight() * 0.85 + 1.2 + Math.sin(this.blaze.tickCount * 0.13) * altitudeScale, 0.0);

		moveTowards(anchor, speed);
	}

	private Vec3 rotateHorizontal(Vec3 side, Vec3 forward, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return side.scale(cos).add(forward.scale(sin)).normalize();
	}

	private void moveTowards(Vec3 targetPos, double speed) {
		this.blaze.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, speed);
	}

	private void playChannelStart(SoundEvent sound, float pitch) {
		if (this.blaze.level() instanceof ServerLevel serverLevel) {
			serverLevel.playSound(null, this.blaze.getX(), this.blaze.getY(), this.blaze.getZ(),
				sound, SoundSource.HOSTILE, 1.0F, pitch);
		}
	}

	private int randomCooldown() {
		return this.blaze.getRandom().nextInt(((COOLDOWN_MAX) - (COOLDOWN_MIN)) + 1) + (COOLDOWN_MIN);
	}

	private boolean isValidPlayerTarget(LivingEntity livingEntity) {
		if (!(livingEntity instanceof Player player) || !livingEntity.isAlive()) return false;
		return !player.isCreative() && !player.isSpectator();
	}
}


