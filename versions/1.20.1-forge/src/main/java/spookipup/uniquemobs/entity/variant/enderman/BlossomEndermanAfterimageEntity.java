package spookipup.uniquemobs.entity.variant.enderman;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import spookipup.uniquemobs.entity.BlossomHelper;

import java.util.UUID;

public class BlossomEndermanAfterimageEntity extends EnderMan {

	private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
		SynchedEntityData.defineId(BlossomEndermanAfterimageEntity.class, EntityDataSerializers.INT);

	private static final int LIFETIME = 36;

	private UUID ownerId;

	public BlossomEndermanAfterimageEntity(EntityType<? extends EnderMan> entityType, Level level) {
		super(entityType, level);
		setNoGravity(true);
		setSilent(true);
		xpReward = 0;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return EnderMan.createAttributes()
			.add(Attributes.MAX_HEALTH, 1.0);
	}

	@Override
	protected void registerGoals() {
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(DATA_TARGET_ID, -1);
	}

	public void setSource(BlossomEndermanEntity owner, LivingEntity target) {
		ownerId = owner.getUUID();
		if (target != null) {
			entityData.set(DATA_TARGET_ID, target.getId());
			setTarget(target);
			faceTarget(target);
			setBeingStaredAt();
		}
	}

	public boolean isOwnedBy(Entity entity) {
		return ownerId != null && ownerId.equals(entity.getUUID());
	}

	@Override
	public void aiStep() {
		setDeltaMovement(0.0, 0.0, 0.0);
		BlossomHelper.tickNaturalSlowFall(this);

		if (level().isClientSide()) {
			if (random.nextInt(2) == 0) {
				level().addParticle(ParticleTypes.CHERRY_LEAVES,
					getRandomX(0.45), getRandomY(), getRandomZ(0.45),
					(random.nextDouble() - 0.5) * 0.025, -0.015, (random.nextDouble() - 0.5) * 0.025);
			}
			LivingEntity target = getTrackedTarget(level());
			if (target != null) {
				setBeingStaredAt();
				faceTarget(target);
			}
		}

		if (tickCount > LIFETIME) {
			discard();
			return;
		}

		if (!(level() instanceof ServerLevel serverLevel)) return;
		LivingEntity target = getTrackedTarget(level());
		if (target != null) {
			setTarget(target);
			setBeingStaredAt();
			faceTarget(target);
		}

		for (LivingEntity living : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.45, 0.1, 0.45))) {
			if (living == this || living instanceof BlossomEndermanEntity || living instanceof BlossomEndermanAfterimageEntity) continue;
			trigger(living, serverLevel);
			break;
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		Entity attacker = source.getEntity();
		if (attacker instanceof LivingEntity living) {
			if (level() instanceof ServerLevel serverLevel) {
				trigger(living, serverLevel);
			}
		} else {
			discard();
		}
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	private void trigger(LivingEntity living, ServerLevel serverLevel) {
		BlossomHelper.triggerDrift(living, this, BlossomHelper.DRIFT_DURATION + 30);
		BlossomHelper.spawnPetalBurst(serverLevel, getX(), getY(0.6), getZ(), 1.1, 34);
		playSound(SoundEvents.CHERRY_LEAVES_BREAK, 1.0F, 1.65F);
		discard();
	}

	private LivingEntity getTrackedTarget(Level level) {
		LivingEntity currentTarget = getTarget();
		if (currentTarget != null && currentTarget.isAlive()) return currentTarget;
		int targetId = entityData.get(DATA_TARGET_ID);
		if (targetId < 0) return null;

		Entity entity = level.getEntity(targetId);
		return entity instanceof LivingEntity living && living.isAlive() ? living : null;
	}

	private void faceTarget(LivingEntity target) {
		double dx = target.getX() - getX();
		double dz = target.getZ() - getZ();
		double dy = target.getEyeY() - getEyeY();
		double horizontal = Math.sqrt(dx * dx + dz * dz);
		float yRot = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
		float xRot = (float) (-(Mth.atan2(dy, horizontal) * (180.0 / Math.PI)));

		setYRot(yRot);
		setXRot(xRot);
		yHeadRot = yRot;
		yBodyRot = yRot;
	}
}
