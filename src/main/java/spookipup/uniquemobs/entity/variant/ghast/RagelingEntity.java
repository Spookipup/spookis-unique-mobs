package spookipup.uniquemobs.entity.variant.ghast;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.ai.RagelingBurstMoveGoal;

import java.util.List;
import java.util.UUID;

// tiny red ghast spawned by the great mother - darts at players, burns on contact, explodes when dying
public class RagelingEntity extends Ghast {

	private static final float HITBOX_SCALE = 0.85F;

	private static final EntityDataAccessor<Boolean> DATA_IS_FUSING =
		SynchedEntityData.defineId(RagelingEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_IS_ATTACKING =
		SynchedEntityData.defineId(RagelingEntity.class, EntityDataSerializers.BOOLEAN);

	private static final float FUSE_HEALTH_THRESHOLD = 0.3F;
	private static final int FUSE_TIME = 40;
	private static final int MAX_LIFETIME = 300; // 15 seconds
	private static final float EXPLOSION_RADIUS = 3.0F;
	private static final float EXPLOSION_DAMAGE = 6.0F;
	private static final int FIRE_SECONDS = 5;
	private static final int CONTACT_DAMAGE_COOLDOWN = 10;
	private static final float CONTACT_DAMAGE = 3.0F;

	private UUID ownerUUID;
	private int fuseTimer = -1;
	private int lifetime;
	private int contactCooldown;
	private boolean hostilityActivated;

	public RagelingEntity(EntityType<? extends Ghast> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Ghast.createAttributes()
			.add(Attributes.MAX_HEALTH, 8.0)
			.add(Attributes.MOVEMENT_SPEED, 0.5)
			.add(Attributes.FOLLOW_RANGE, 48.0);
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return super.getDimensions(pose).scale(HITBOX_SCALE);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_IS_FUSING, false);
		this.entityData.define(DATA_IS_ATTACKING, false);
	}

	@Override
	protected void registerGoals() {
		// let ghast register its goals, then strip all and add ours
		super.registerGoals();
		this.goalSelector.getAvailableGoals().clear();
		this.targetSelector.getAvailableGoals().clear();

		this.goalSelector.addGoal(1, new RagelingBurstMoveGoal(this, 0.7F, 15, 30, 8, 12));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	public void setOwnerUUID(UUID uuid) {
		this.ownerUUID = uuid;
	}

	public UUID getOwnerUUID() {
		return this.ownerUUID;
	}

	public boolean isFusing() {
		return this.entityData.get(DATA_IS_FUSING);
	}

	// use our own synced data instead of Ghast's DATA_IS_CHARGING which doesn't sync for subclasses
	@Override
	public void setCharging(boolean charging) {
		this.entityData.set(DATA_IS_ATTACKING, charging);
	}

	@Override
	public boolean isCharging() {
		return this.entityData.get(DATA_IS_ATTACKING);
	}

	@Override
	public void tick() {
		super.tick();

		// replicate Ghast.GhastLookGoal (private in 1.20.1) - face target or movement direction
		if (this.getTarget() != null) {
			double dx = this.getTarget().getX() - this.getX();
			double dz = this.getTarget().getZ() - this.getZ();
			this.setYRot(-((float) Mth.atan2(dx, dz)) * (180F / (float) Math.PI));
		} else {
			Vec3 vel = this.getDeltaMovement();
			if (vel.horizontalDistanceSqr() > 1.0E-6) {
				this.setYRot(-((float) Mth.atan2(vel.x, vel.z)) * (180F / (float) Math.PI));
			}
		}
		this.yBodyRot = this.getYRot();

		if (this.contactCooldown > 0) this.contactCooldown--;

		if (!this.level().isClientSide()) {
			serverFuseTick();
		} else {
			clientFuseEffects();
		}
	}

	private void serverFuseTick() {
		if (this.getTarget() != null) {
			this.hostilityActivated = true;
		}
		if (this.ownerUUID != null && this.hostilityActivated) {
			this.lifetime++;
		}

		boolean lowHealth = this.getHealth() / this.getMaxHealth() < FUSE_HEALTH_THRESHOLD;
		boolean tooOld = this.ownerUUID != null && this.hostilityActivated && this.lifetime >= MAX_LIFETIME;
		boolean shouldFuse = this.isAlive() && (lowHealth || tooOld);

		if (shouldFuse && this.fuseTimer < 0) {
			this.fuseTimer = FUSE_TIME;
			this.entityData.set(DATA_IS_FUSING, true);
			this.setCharging(true);
			this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 1.5F);
		}

		if (this.fuseTimer >= 0) {
			this.fuseTimer--;
			if (this.fuseTimer <= 0) {
				explode();
			}
		}
	}

	private void clientFuseEffects() {
		if (!this.isFusing()) return;

		for (int i = 0; i < 3; i++) {
			this.level().addParticle(ParticleTypes.FLAME,
				this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5),
				0.0, 0.05, 0.0);
		}
		if (this.random.nextInt(3) == 0) {
			this.level().addParticle(ParticleTypes.SMOKE,
				this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5),
				0.0, 0.02, 0.0);
		}
	}

	private void explode() {
		if (!(this.level() instanceof ServerLevel serverLevel)) return;

		List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
			LivingEntity.class,
			this.getBoundingBox().inflate(EXPLOSION_RADIUS),
			e -> e != this && e.isAlive()
		);

		DamageSource fireDamage = this.damageSources().onFire();
		for (LivingEntity target : targets) {
			double dist = this.distanceTo(target);
			if (dist > EXPLOSION_RADIUS) continue;

			float damage = EXPLOSION_DAMAGE * (1.0F - (float) (dist / EXPLOSION_RADIUS));
			target.hurt(fireDamage, damage);
			target.setSecondsOnFire(FIRE_SECONDS);
		}

		this.playSound(SoundEvents.GENERIC_EXPLODE, 1.5F, 1.2F);

		serverLevel.sendParticles(ParticleTypes.FLAME,
			this.getX(), this.getY() + 0.5, this.getZ(),
			40, 0.8, 0.8, 0.8, 0.05);
		serverLevel.sendParticles(ParticleTypes.SMOKE,
			this.getX(), this.getY() + 0.5, this.getZ(),
			20, 0.6, 0.6, 0.6, 0.02);
		serverLevel.sendParticles(ParticleTypes.LAVA,
			this.getX(), this.getY() + 0.5, this.getZ(),
			10, 0.5, 0.5, 0.5, 0.0);

		this.discard();
	}

	@Override
	public void playerTouch(Player player) {
		super.playerTouch(player);

		if (this.level().isClientSide() || this.contactCooldown > 0) return;

		player.hurt(this.damageSources().onFire(), CONTACT_DAMAGE);
		player.setSecondsOnFire(3);
		this.contactCooldown = CONTACT_DAMAGE_COOLDOWN;
	}

	@Override
	public float getVoicePitch() {
		return 1.8F + this.random.nextFloat() * 0.4F;
	}

	@Override
	protected float getSoundVolume() {
		return 0.6F;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (this.ownerUUID != null) {
			tag.putUUID("OwnerUUID", this.ownerUUID);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.hasUUID("OwnerUUID")) {
			this.ownerUUID = tag.getUUID("OwnerUUID");
		}
	}
}
