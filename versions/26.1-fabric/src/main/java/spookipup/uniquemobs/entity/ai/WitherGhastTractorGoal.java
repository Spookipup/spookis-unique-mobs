package spookipup.uniquemobs.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.variant.ghast.WitherGhastEntity;

import java.util.List;

import java.util.EnumSet;

public class WitherGhastTractorGoal extends Goal {

	private final WitherGhastEntity ghast;

	private static final int CHARGE_TIME = 20;
	private static final int FIRE_DURATION = 120;
	private static final int COOLDOWN = 60;
	private static final double BEAM_RANGE = 32.0;
	private static final double CONE_HALF_ANGLE = Math.toRadians(12.0);
	private static final double PULL_SPEED_BASE = 0.02;
	private static final double PULL_SPEED_MAX = 0.18;
	private static final float WITHER_DAMAGE_MIN = 1.0F;
	private static final float WITHER_DAMAGE_MAX = 6.0F;
	private static final int WITHER_EFFECT_DURATION = 60;
	private static final int DAMAGE_TICK_INTERVAL = 10;
	private static final float LOW_HEALTH_THRESHOLD = 6.0F;
	private static final float BITE_DAMAGE = 12.0F;
	private static final double BITE_RANGE = 4.5;
	private static final int BITE_WINDUP = 10;
	private static final double LAVA_SCAN_RADIUS = 24.0;
	private static final int LAVA_SCAN_DEPTH = 20;
	private static final double TRACK_SPEED = 0.06;
	private static final float SOFT_BLOCK_HARDNESS = 1.0F;
	private static final int BLOCK_DISPLACE_INTERVAL = 4;

	private enum Phase { CHARGING, PULLING, DECIDING, LAVA_REPOSITION, BITING }

	private int chargeTicks;
	private int fireTicks;
	private int cooldown;
	private int biteTicks;
	private Vec3 beamDir;
	private LivingEntity target;
	private boolean attackDone;
	private Phase phase;
	private BlockPos lavaTarget;
	private int releaseDelay;

	public WitherGhastTractorGoal(WitherGhastEntity ghast) {
		this.ghast = ghast;
		this.setFlags(EnumSet.of(Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.cooldown > 0) {
			this.cooldown--;
			return false;
		}
		LivingEntity t = this.ghast.getTarget();
		if (t == null || !t.isAlive()) return false;
		return this.ghast.distanceTo(t) <= BEAM_RANGE && this.ghast.getSensing().hasLineOfSight(t);
	}

	@Override
	public boolean canContinueToUse() {
		if (this.attackDone) return false;
		if (this.target == null || !this.target.isAlive()) return false;
		if (this.ghast.distanceTo(this.target) > BEAM_RANGE * 1.5) return false;
		if (this.phase == Phase.CHARGING && !this.ghast.getSensing().hasLineOfSight(this.target)) {
			return false;
		}
		return true;
	}

	@Override
	public void start() {
		this.target = this.ghast.getTarget();
		this.chargeTicks = 0;
		this.fireTicks = 0;
		this.biteTicks = 0;
		this.beamDir = null;
		this.attackDone = false;
		this.lavaTarget = null;
		this.releaseDelay = 0;
		this.phase = Phase.CHARGING;
		this.ghast.setCharging(true);
	}

	@Override
	public void stop() {
		this.ghast.setCharging(false);
		this.ghast.setFiring(false);
		this.ghast.setBiting(false);
		this.ghast.setChargeTicks(0);
		this.chargeTicks = 0;
		this.fireTicks = 0;
		this.biteTicks = 0;
		this.beamDir = null;
		this.target = null;
		this.attackDone = false;
		this.lavaTarget = null;
		this.phase = Phase.CHARGING;
		if (this.cooldown <= 0) this.cooldown = COOLDOWN;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (this.target == null) return;

		if (this.beamDir != null) {
			this.ghast.setBeamDirection(this.beamDir);
		} else {
			Vec3 eyePos = this.ghast.getEyePosition();
			Vec3 targetPos = this.target.position().add(0, this.target.getBbHeight() * 0.5, 0);
			Vec3 dir = targetPos.subtract(eyePos).normalize();
			this.ghast.setBeamDirection(dir);
		}

		switch (this.phase) {
			case CHARGING -> tickCharging();
			case PULLING -> tickPulling();
			case DECIDING -> tickDeciding();
			case LAVA_REPOSITION -> tickLavaReposition();
			case BITING -> tickBiting();
		}
	}

	private void tickCharging() {
		this.chargeTicks++;
		this.ghast.setChargeTicks(this.chargeTicks);

		if (this.ghast.level() instanceof ServerLevel serverLevel) {
			Vec3 gPos = this.ghast.position();

			if (this.chargeTicks % 5 == 0) {
				float progress = (float) this.chargeTicks / CHARGE_TIME;
				serverLevel.playSound(null, gPos.x, gPos.y, gPos.z,
					SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 4.0F + progress * 4.0F, 0.8F + progress * 0.6F);
			}
		}

		if (this.chargeTicks >= CHARGE_TIME) {
			beginFiring();
		}
	}

	private void beginFiring() {
		this.fireTicks = FIRE_DURATION;
		this.phase = Phase.PULLING;
		this.ghast.setFiring(true);
		this.ghast.setCharging(false);

		Vec3 eyePos = this.ghast.getEyePosition();
		Vec3 mouthPos = eyePos.add(0, WitherGhastEntity.MOUTH_Y_OFFSET, 0);
		this.beamDir = this.target.position().add(0, this.target.getBbHeight() * 0.5, 0)
			.subtract(mouthPos).normalize();

		if (this.ghast.level() instanceof ServerLevel serverLevel) {
			Vec3 gPos = this.ghast.position();
			serverLevel.playSound(null, gPos.x, gPos.y, gPos.z,
				SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 10.0F, 0.5F);
		}
	}

	private void tickPulling() {
		this.fireTicks--;

		Vec3 eyePos = this.ghast.getEyePosition();
		Vec3 mouthPos = eyePos.add(0, WitherGhastEntity.MOUTH_Y_OFFSET, 0);
		Vec3 targetPos = this.target.position().add(0, this.target.getBbHeight() * 0.5, 0);
		Vec3 desiredDir = targetPos.subtract(mouthPos).normalize();

		this.beamDir = this.beamDir.scale(1.0 - TRACK_SPEED).add(desiredDir.scale(TRACK_SPEED)).normalize();

		double dist = this.ghast.distanceTo(this.target);

		Vec3 toTarget = targetPos.subtract(mouthPos).normalize();
		double angle = Math.acos(Math.min(1.0, this.beamDir.dot(toTarget)));
		boolean inCone = angle <= CONE_HALF_ANGLE && dist <= BEAM_RANGE;

		if (inCone) {
			double proximity = 1.0 - Math.min(dist / BEAM_RANGE, 1.0);
			double pullStrength = PULL_SPEED_BASE + (PULL_SPEED_MAX - PULL_SPEED_BASE) * proximity;
			Vec3 pullDir = mouthPos.subtract(this.target.position()).normalize();
			Vec3 pull = pullDir.scale(pullStrength).add(0, 0.02, 0);
			this.target.setDeltaMovement(this.target.getDeltaMovement().add(pull));
			this.target.hurtMarked = true;

			if (this.fireTicks % DAMAGE_TICK_INTERVAL == 0 && this.ghast.level() instanceof ServerLevel serverLevel) {
				float damage = WITHER_DAMAGE_MIN + (WITHER_DAMAGE_MAX - WITHER_DAMAGE_MIN) * (float) proximity;
				this.target.hurtServer(serverLevel, this.ghast.damageSources().wither(), damage);
				this.target.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_EFFECT_DURATION, 1));
			}
		}

		pullProjectiles(mouthPos);

		if (this.fireTicks % BLOCK_DISPLACE_INTERVAL == 0 && this.ghast.level() instanceof ServerLevel serverLevel) {
			displaceBlocks(serverLevel, mouthPos);
		}

		if (this.ghast.level() instanceof ServerLevel serverLevel) {
			Vec3 gPos = this.ghast.position();
			if (this.fireTicks % 20 == 0) {
				serverLevel.playSound(null, gPos.x, gPos.y, gPos.z,
					SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 6.0F, 0.6F);
			}
			if (this.fireTicks % 5 == 0) {
				serverLevel.playSound(null, gPos.x, gPos.y, gPos.z,
					SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 3.0F, 0.8F + this.ghast.getRandom().nextFloat() * 0.4F);
			}
		}

		if (dist <= BITE_RANGE) {
			this.phase = Phase.DECIDING;
		} else if (this.fireTicks <= 0) {
			this.ghast.setFiring(false);
			this.cooldown = COOLDOWN;
			this.attackDone = true;
		}
	}

	private void tickDeciding() {
		if (this.target.getHealth() <= LOW_HEALTH_THRESHOLD) {
			BlockPos lava = scanForLava();
			if (lava != null) {
				this.lavaTarget = lava;
				this.phase = Phase.LAVA_REPOSITION;
				return;
			}
		}
		this.phase = Phase.BITING;
		this.biteTicks = BITE_WINDUP;
		this.ghast.setBiting(true);
	}

	private void tickLavaReposition() {
		applyPull();

		Vec3 above = new Vec3(this.lavaTarget.getX() + 0.5, this.lavaTarget.getY() + 8.0, this.lavaTarget.getZ() + 0.5);
		Vec3 toTarget = above.subtract(this.ghast.position()).normalize();
		this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().add(toTarget.scale(0.12)));

		double horizDist = Math.sqrt(
			Math.pow(this.ghast.getX() - above.x, 2) + Math.pow(this.ghast.getZ() - above.z, 2));

		if (horizDist < 3.0) {
			this.ghast.setFiring(false);

			this.releaseDelay++;
			if (this.releaseDelay >= 10) {
				this.cooldown = COOLDOWN;
				this.attackDone = true;
			}
		}
	}

	private void tickBiting() {
		applyPull();

		this.biteTicks--;
		if (this.biteTicks <= 0 && this.ghast.level() instanceof ServerLevel serverLevel) {
			this.target.hurtServer(serverLevel, this.ghast.damageSources().mobAttack(this.ghast), BITE_DAMAGE);
			this.target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 3));

			Vec3 knockDir = this.target.position().subtract(this.ghast.position()).normalize();
			this.target.knockback(2.0, -knockDir.x, -knockDir.z);
			this.target.hurtMarked = true;

			serverLevel.playSound(null, this.ghast.getX(), this.ghast.getY(), this.ghast.getZ(),
				SoundEvents.WITHER_HURT, SoundSource.HOSTILE, 8.0F, 0.6F);

			this.ghast.setFiring(false);
			this.ghast.setBiting(false);
			this.cooldown = COOLDOWN;
			this.attackDone = true;
		}
	}

	private void pullProjectiles(Vec3 mouthPos) {
		AABB coneBox = new AABB(mouthPos.x - BEAM_RANGE, mouthPos.y - BEAM_RANGE, mouthPos.z - BEAM_RANGE,
			mouthPos.x + BEAM_RANGE, mouthPos.y + BEAM_RANGE, mouthPos.z + BEAM_RANGE);
		List<Entity> entities = this.ghast.level().getEntities(this.ghast, coneBox,
			e -> e instanceof Projectile || e instanceof FallingBlockEntity);

		for (Entity entity : entities) {
			Vec3 toEntity = entity.position().subtract(mouthPos);
			double dist = toEntity.length();
			if (dist > BEAM_RANGE || dist < 0.5) continue;

			Vec3 dirToEntity = toEntity.normalize();
			double angle = Math.acos(Math.min(1.0, this.beamDir.dot(dirToEntity)));
			if (angle > CONE_HALF_ANGLE) continue;

			double proximity = 1.0 - dist / BEAM_RANGE;
			double pull = 0.05 + 0.25 * proximity;
			Vec3 pullDir = mouthPos.subtract(entity.position()).normalize();
			entity.setDeltaMovement(entity.getDeltaMovement().add(pullDir.scale(pull)));
			entity.hurtMarked = true;
		}
	}

	private void displaceBlocks(ServerLevel level, Vec3 mouthPos) {
		if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) return;

		for (int i = 0; i < 3; i++) {
			double t = 0.3 + this.ghast.getRandom().nextDouble() * 0.7;
			double dist = BEAM_RANGE * t;
			Vec3 center = mouthPos.add(this.beamDir.scale(dist));

			double coneRadius = dist * Math.tan(CONE_HALF_ANGLE);
			double ox = (this.ghast.getRandom().nextDouble() - 0.5) * 2 * coneRadius;
			double oy = (this.ghast.getRandom().nextDouble() - 0.5) * 2 * coneRadius;
			double oz = (this.ghast.getRandom().nextDouble() - 0.5) * 2 * coneRadius;

			BlockPos pos = BlockPos.containing(center.x + ox, center.y + oy, center.z + oz);
			BlockState state = level.getBlockState(pos);
			if (state.isAir() || !state.getFluidState().isEmpty()) continue;

			float hardness = state.getDestroySpeed(level, pos);
			if (hardness < 0 || hardness > SOFT_BLOCK_HARDNESS) continue;

			FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
			if (falling != null) {
				Vec3 pullDir = mouthPos.subtract(falling.position()).normalize();
				double proximity = 1.0 - dist / BEAM_RANGE;
				falling.setDeltaMovement(pullDir.scale(0.3 + 0.4 * proximity).add(0, 0.1, 0));
				falling.hurtMarked = true;
			}
		}
	}

	private void applyPull() {
		Vec3 mouthPos = this.ghast.getEyePosition().add(0, WitherGhastEntity.MOUTH_Y_OFFSET, 0);
		Vec3 pullDir = mouthPos.subtract(this.target.position()).normalize();
		double dist = this.ghast.distanceTo(this.target);
		double proximity = 1.0 - Math.min(dist / BEAM_RANGE, 1.0);
		double pullStrength = PULL_SPEED_BASE + (PULL_SPEED_MAX - PULL_SPEED_BASE) * proximity;
		this.target.setDeltaMovement(this.target.getDeltaMovement().add(pullDir.scale(pullStrength).add(0, 0.02, 0)));
		this.target.hurtMarked = true;
	}

	private BlockPos scanForLava() {
		BlockPos ghastPos = this.ghast.blockPosition();
		Level level = this.ghast.level();
		int radius = (int) LAVA_SCAN_RADIUS;
		BlockPos best = null;
		double bestDistSq = Double.MAX_VALUE;

		for (int dx = -radius; dx <= radius; dx += 3) {
			for (int dz = -radius; dz <= radius; dz += 3) {
				for (int dy = 0; dy > -LAVA_SCAN_DEPTH; dy--) {
					BlockPos check = ghastPos.offset(dx, dy, dz);
					BlockState state = level.getBlockState(check);
					if (state.getFluidState().is(FluidTags.LAVA)) {
						if (isLavaPool(level, check)) {
							double distSq = ghastPos.distSqr(check);
							if (distSq < bestDistSq) {
								best = check;
								bestDistSq = distSq;
							}
						}
						break;
					}
					if (!state.isAir() && state.getFluidState().isEmpty()) {
						break;
					}
				}
			}
		}
		return best;
	}

	private boolean isLavaPool(Level level, BlockPos center) {
		int count = 0;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (level.getBlockState(center.offset(dx, 0, dz)).getFluidState().is(FluidTags.LAVA)) {
					count++;
				}
			}
		}
		return count >= 5;
	}
}
