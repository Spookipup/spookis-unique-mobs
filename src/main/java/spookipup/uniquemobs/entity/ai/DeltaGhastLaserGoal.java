package spookipup.uniquemobs.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.variant.ghast.DeltaGhastEntity;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// charge first so players get a real tell before the beam starts
public class DeltaGhastLaserGoal extends Goal {

	private final DeltaGhastEntity ghast;

	private static final int CHARGE_TIME = 80;
	private static final int FIRE_DURATION = 70;
	private static final int COOLDOWN = 240;
	private static final double BEAM_RANGE = 48.0;
	private static final double TRACK_SPEED = 0.035;
	private static final float BEAM_DAMAGE = 8.0F;
	private static final float MAX_BREAKABLE_HARDNESS = 25.0F;
	private int chargeTicks;
	private int fireTicks;
	private int cooldown;
	private Vec3 beamDir;
	private LivingEntity target;
	private boolean attackDone;

	public DeltaGhastLaserGoal(DeltaGhastEntity ghast) {
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
		if (this.ghast.distanceTo(this.target) > BEAM_RANGE * 1.2) return false;
		if (this.fireTicks <= 0 && this.chargeTicks > 0 && !this.ghast.getSensing().hasLineOfSight(this.target)) {
			return false;
		}
		return true;
	}

	@Override
	public void start() {
		this.target = this.ghast.getTarget();
		this.chargeTicks = 0;
		this.fireTicks = 0;
		this.beamDir = null;
		this.attackDone = false;
		this.ghast.setCharging(true);
	}

	@Override
	public void stop() {
		this.ghast.setCharging(false);
		this.ghast.setFiring(false);
		this.ghast.setChargeTicks(0);
		this.chargeTicks = 0;
		this.fireTicks = 0;
		this.beamDir = null;
		this.target = null;
		this.attackDone = false;
		if (this.cooldown <= 0) this.cooldown = COOLDOWN;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (this.target == null) return;

		if (this.fireTicks > 0 && this.beamDir != null) {
			this.ghast.setBeamDirection(this.beamDir);
		} else {
			Vec3 eyePos = this.ghast.getEyePosition();
			Vec3 targetPos = this.target.position().add(0, this.target.getBbHeight() * 0.5, 0);
			Vec3 dir = targetPos.subtract(eyePos).normalize();
			this.ghast.setBeamDirection(dir);
		}

		if (this.fireTicks > 0) {
			tickFiring();
		} else {
			tickCharging();
		}
	}

	private void tickCharging() {
		this.chargeTicks++;
		this.ghast.setChargeTicks(this.chargeTicks);

		if (this.ghast.level() instanceof ServerLevel serverLevel) {
			Vec3 gPos = this.ghast.position();

			if (this.chargeTicks % 8 == 0) {
				float progress = (float) this.chargeTicks / CHARGE_TIME;
				float pitch = 0.5F + progress * 1.5F;
				serverLevel.playSound(null, gPos.x, gPos.y, gPos.z,
					SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 8.0F + progress * 6.0F, pitch);
			}

			if (this.chargeTicks % 20 == 0) {
				float progress = (float) this.chargeTicks / CHARGE_TIME;
				serverLevel.playSound(null, gPos.x, gPos.y, gPos.z,
					SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 6.0F * progress, 0.5F + progress * 0.8F);
			}

		}

		if (this.chargeTicks >= CHARGE_TIME) {
			beginFiring();
		}
	}

	private void beginFiring() {
		this.fireTicks = FIRE_DURATION;
		this.ghast.setFiring(true);
		this.ghast.setCharging(false);

		Vec3 eyePos = this.ghast.getEyePosition();
		Vec3 mouthPos = eyePos.add(0, DeltaGhastEntity.MOUTH_Y_OFFSET, 0);
		this.beamDir = this.target.position().add(0, this.target.getBbHeight() * 0.5, 0)
			.subtract(mouthPos).normalize();

		if (this.ghast.level() instanceof ServerLevel serverLevel) {
			Vec3 gPos = this.ghast.position();
			serverLevel.playSound(null, gPos.x, gPos.y, gPos.z,
				SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 12.0F, 0.5F);
			serverLevel.playSound(null, gPos.x, gPos.y, gPos.z,
				SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 10.0F, 0.3F);
		}
	}

	private void tickFiring() {
		this.fireTicks--;

		Vec3 eyePos = this.ghast.getEyePosition();
		Vec3 mouthPos = eyePos.add(0, DeltaGhastEntity.MOUTH_Y_OFFSET, 0);
		Vec3 targetPos = this.target.position().add(0, this.target.getBbHeight() * 0.5, 0);
		Vec3 desiredDir = targetPos.subtract(mouthPos).normalize();

		float progress = 1.0F - (float) this.fireTicks / FIRE_DURATION;
		double speed = TRACK_SPEED + progress * TRACK_SPEED;
		this.beamDir = this.beamDir.scale(1.0 - speed).add(desiredDir.scale(speed)).normalize();

		if (this.ghast.level() instanceof ServerLevel serverLevel) {
			boolean canGrief = serverLevel.getGameRules().get(GameRules.MOB_GRIEFING);
			traceBeam(serverLevel, mouthPos, this.beamDir, canGrief);

			Vec3 gPos = this.ghast.position();
			if (this.fireTicks % 3 == 0) {
				serverLevel.playSound(null, gPos.x, gPos.y, gPos.z,
					SoundEvents.FIRE_AMBIENT, SoundSource.HOSTILE, 14.0F, 0.3F + this.ghast.getRandom().nextFloat() * 0.15F);
			}
			if (this.fireTicks % 10 == 0) {
				serverLevel.playSound(null, gPos.x, gPos.y, gPos.z,
					SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 10.0F, 0.2F);
			}
			if (this.fireTicks % 20 == 0) {
				serverLevel.playSound(null, gPos.x, gPos.y, gPos.z,
					SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 8.0F, 0.4F);
			}
		}

		if (this.fireTicks <= 0) {
			this.ghast.setFiring(false);
			this.cooldown = COOLDOWN;
			this.attackDone = true;
		}
	}

	private void traceBeam(ServerLevel level, Vec3 origin, Vec3 dir, boolean canGrief) {
		double step = 0.5;
		Vec3 hitPoint = null;
		Set<Integer> hitEntityIds = new HashSet<>();

		for (double d = 0; d < BEAM_RANGE; d += step) {
			Vec3 pos = origin.add(dir.scale(d));
			BlockPos blockPos = BlockPos.containing(pos);

			BlockState state = level.getBlockState(blockPos);
			if (!state.isAir() && state.getFluidState().isEmpty()) {
				float hardness = state.getDestroySpeed(level, blockPos);
				if (hardness < 0 || hardness >= MAX_BREAKABLE_HARDNESS || !canGrief) {
					hitPoint = pos;
					break;
				}
				level.destroyBlock(blockPos, false, this.ghast);
			}

			AABB beamBox = new AABB(pos.x - 0.8, pos.y - 0.8, pos.z - 0.8,
				pos.x + 0.8, pos.y + 0.8, pos.z + 0.8);

			List<LivingEntity> hitEntities = level.getEntitiesOfClass(LivingEntity.class, beamBox,
				e -> e != this.ghast && e.isAlive());
			for (LivingEntity hit : hitEntities) {
				if (!hitEntityIds.add(hit.getId())) continue;
				hit.hurtServer(level, this.ghast.damageSources().indirectMagic(this.ghast, this.ghast), BEAM_DAMAGE);
				hit.igniteForSeconds(3);
			}

			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, beamBox);
			for (ItemEntity item : items) {
				item.discard();
			}

			if (d > 1.0 && this.ghast.getRandom().nextFloat() < 0.3) {
				level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
					pos.x, pos.y, pos.z, 1, 0.15, 0.15, 0.15, 0.01);
			}
		}

		if (hitPoint != null) {
			level.sendParticles(ParticleTypes.LARGE_SMOKE,
				hitPoint.x, hitPoint.y, hitPoint.z, 3, 0.3, 0.3, 0.3, 0.02);
			level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
				hitPoint.x, hitPoint.y, hitPoint.z, 2, 0.2, 0.2, 0.2, 0.03);
		}
	}
}
