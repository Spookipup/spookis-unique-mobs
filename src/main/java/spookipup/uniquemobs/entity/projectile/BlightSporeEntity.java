package spookipup.uniquemobs.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.BlightSporeCloud;
import spookipup.uniquemobs.entity.variant.ghast.BlightlingEntity;
import spookipup.uniquemobs.registry.ModEffects;
import spookipup.uniquemobs.registry.ModEntities;

import java.util.List;

public class BlightSporeEntity extends ThrowableProjectile implements ItemSupplier {

	private static final EntityDataAccessor<Boolean> DATA_IS_HOVERING =
		SynchedEntityData.defineId(BlightSporeEntity.class, EntityDataSerializers.BOOLEAN);

	private static final ItemStack ICON = new ItemStack(Items.SLIME_BALL);
	private static final int MIN_TRAVEL_TICKS = 8;
	private static final int MAX_TRAVEL_TICKS = 28;
	private static final int DEFAULT_HOVER_DURATION = 24;
	private static final double BURST_TRIGGER_RADIUS = 0.9;
	private static final double MAX_TRAVEL_SPEED = 0.44;
	private static final double MAX_SETTLE_SPEED = 0.12;
	private static final double LOCK_HOVER_RADIUS = 0.18;
	private static final int MAX_LIFETIME = 80;
	private static final float DIRECT_HIT_DAMAGE = 2.0F;
	private static final float CLOUD_RADIUS = 3.0F;
	private static final int CLOUD_DURATION = 54;
	private static final double CLOUD_Y_OFFSET = 0.65;

	private int hoverTicks;
	private int hoverDuration = DEFAULT_HOVER_DURATION;
	private Vec3 burstTarget;
	private Vec3 hoverCenter;
	private double lastBurstDistanceSqr = Double.MAX_VALUE;
	private boolean settling;

	public BlightSporeEntity(EntityType<? extends BlightSporeEntity> type, Level level) {
		super(type, level);
	}

	public BlightSporeEntity(Level level, LivingEntity shooter) {
		super(ModEntities.BLIGHT_SPORE, level);
		this.setOwner(shooter);
		this.setPos(shooter.getX(), shooter.getEyeY() - 0.15, shooter.getZ());
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_IS_HOVERING, false);
	}

	@Override
	public ItemStack getItem() {
		return ICON;
	}

	@Override
	public void tick() {
		if (this.isHovering()) {
			this.setDeltaMovement(Vec3.ZERO);
		} else if (this.settling) {
			tickSettlingTowardHover();
		} else if (this.burstTarget != null) {
			guideTowardBurstTarget();
		} else {
			this.setDeltaMovement(this.getDeltaMovement().scale(0.94));
		}

		super.tick();
		if (this.isRemoved()) return;

		if (!this.level().isClientSide()) {
			checkContactBurst();
			if (this.isRemoved()) return;
		}

		if (this.isHovering()) {
			this.hoverTicks++;
			spawnHoverParticles();
		} else {
			spawnTrailParticles();
			if (this.settling) {
				if (shouldLockHover()) {
					enterHovering(this.hoverCenter != null ? this.hoverCenter : this.position());
				}
			} else if (shouldBeginSettling()) {
				beginSettling(this.burstTarget != null ? this.burstTarget : this.position());
			}
		}

		if (!this.level().isClientSide() && this.tickCount >= MAX_LIFETIME) {
			burst(null);
		}
		if (!this.level().isClientSide() && this.isHovering() && this.hoverTicks >= this.hoverDuration) {
			burst(null);
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		Entity target = result.getEntity();

		if (target instanceof LivingEntity livingTarget && this.level() instanceof ServerLevel serverLevel) {
			applyDirectHit(serverLevel, livingTarget);
		}
	}

	@Override
	protected void onHit(HitResult result) {
		if (result.getType() == HitResult.Type.ENTITY) {
			super.onHit(result);
			if (!this.level().isClientSide()) {
				burst(result);
			}
			return;
		}

		if (result.getType() == HitResult.Type.BLOCK) {
			beginSettling(result.getLocation());
			return;
		}

		super.onHit(result);
		if (!this.level().isClientSide()) {
			burst(result);
		}
	}

	private void enterHovering(Vec3 pos) {
		this.setPos(pos.x, pos.y, pos.z);
		this.setDeltaMovement(Vec3.ZERO);
		this.entityData.set(DATA_IS_HOVERING, true);
		this.hoverTicks = 0;
		this.settling = false;
		this.hoverCenter = pos;
		this.lastBurstDistanceSqr = 0.0;
	}

	public void setBurstTarget(Vec3 burstTarget, int hoverDuration) {
		this.burstTarget = burstTarget;
		this.hoverDuration = Math.max(8, hoverDuration);
		this.lastBurstDistanceSqr = this.position().distanceToSqr(burstTarget);
	}

	private void beginSettling(Vec3 hoverCenter) {
		this.hoverCenter = hoverCenter;
		this.settling = true;
	}

	private void burst(HitResult result) {
		if (!(this.level() instanceof ServerLevel serverLevel)) return;

		double cloudX = result != null ? result.getLocation().x : this.getX();
		double cloudY = (result != null ? result.getLocation().y : this.getY()) + CLOUD_Y_OFFSET;
		double cloudZ = result != null ? result.getLocation().z : this.getZ();

		BlightSporeCloud cloud = new BlightSporeCloud(serverLevel, cloudX, cloudY, cloudZ);
		cloud.setOwner(this.getOwner() instanceof LivingEntity living ? living : null);
		cloud.setParticle(ParticleTypes.SPORE_BLOSSOM_AIR);
		cloud.setRadius(CLOUD_RADIUS);
		cloud.setDuration(CLOUD_DURATION);
		cloud.setWaitTime(0);
		cloud.setRadiusPerTick(-0.02F);
		serverLevel.addFreshEntity(cloud);

		serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
			cloudX, cloudY + 0.1, cloudZ, 16, 0.7, 0.4, 0.7, 0.01);
		serverLevel.sendParticles(ParticleTypes.MYCELIUM,
			cloudX, cloudY, cloudZ, 28, 0.95, 0.55, 0.95, 0.01);
		serverLevel.sendParticles(ParticleTypes.ITEM_SLIME,
			cloudX, cloudY, cloudZ, 12, 0.28, 0.28, 0.28, 0.01);

		this.discard();
	}

	private void spawnTrailParticles() {
		this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR,
			this.getX(), this.getY(), this.getZ(),
			0.0, 0.005, 0.0);
		this.level().addParticle(ParticleTypes.MYCELIUM,
			this.getX(), this.getY(), this.getZ(),
			0.0, 0.0, 0.0);
	}

	private void spawnHoverParticles() {
		for (int i = 0; i < 2; i++) {
			double sideX = (this.random.nextDouble() - 0.5) * 0.45;
			double sideY = (this.random.nextDouble() - 0.5) * 0.28;
			double sideZ = (this.random.nextDouble() - 0.5) * 0.45;
			this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR,
				this.getX() + sideX, this.getY() + sideY, this.getZ() + sideZ,
				0.0, 0.005, 0.0);
		}
		this.level().addParticle(ParticleTypes.ITEM_SLIME,
			this.getX(), this.getY(), this.getZ(),
			0.0, 0.0, 0.0);
	}

	@Override
	protected float getGravity() {
		return 0.0F;
	}

	private boolean isHovering() {
		return this.entityData.get(DATA_IS_HOVERING);
	}

	private void guideTowardBurstTarget() {
		Vec3 toBurst = this.burstTarget.subtract(this.position());
		double distance = toBurst.length();
		if (distance < 1.0E-4) {
			this.setDeltaMovement(this.getDeltaMovement().scale(0.85));
			return;
		}

		double desiredSpeed = Math.min(MAX_TRAVEL_SPEED, 0.18 + distance * 0.06);
		Vec3 desiredVelocity = toBurst.scale(desiredSpeed / distance);
		Vec3 velocity = this.getDeltaMovement().scale(0.8).add(desiredVelocity.scale(0.2));
		if (velocity.lengthSqr() > MAX_TRAVEL_SPEED * MAX_TRAVEL_SPEED) {
			velocity = velocity.normalize().scale(MAX_TRAVEL_SPEED);
		}
		this.setDeltaMovement(velocity);
	}

	private void tickSettlingTowardHover() {
		if (this.hoverCenter == null) {
			this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
			return;
		}

		Vec3 toHover = this.hoverCenter.subtract(this.position());
		double distance = toHover.length();
		if (distance < 1.0E-4) {
			this.setDeltaMovement(this.getDeltaMovement().scale(0.7));
			return;
		}

		double desiredSpeed = Math.min(MAX_SETTLE_SPEED, 0.02 + distance * 0.18);
		Vec3 desiredVelocity = toHover.scale(desiredSpeed / distance);
		Vec3 velocity = this.getDeltaMovement().scale(0.72).add(desiredVelocity.scale(0.28));
		this.setDeltaMovement(velocity);
	}

	private boolean shouldBeginSettling() {
		if (this.tickCount < MIN_TRAVEL_TICKS) return false;

		if (this.burstTarget == null) {
			return this.getDeltaMovement().lengthSqr() <= 0.02;
		}

		double distanceSqr = this.position().distanceToSqr(this.burstTarget);
		boolean reachedTarget = distanceSqr <= BURST_TRIGGER_RADIUS * BURST_TRIGGER_RADIUS;
		boolean passedTarget = this.tickCount >= MIN_TRAVEL_TICKS
			&& distanceSqr > this.lastBurstDistanceSqr + 0.08;
		boolean forcedArrival = this.tickCount >= MAX_TRAVEL_TICKS;

		this.lastBurstDistanceSqr = distanceSqr;
		return reachedTarget || passedTarget || forcedArrival;
	}

	private boolean shouldLockHover() {
		if (this.hoverCenter == null) {
			return this.getDeltaMovement().lengthSqr() <= 0.0025;
		}

		double distanceSqr = this.position().distanceToSqr(this.hoverCenter);
		return distanceSqr <= LOCK_HOVER_RADIUS * LOCK_HOVER_RADIUS
			&& this.getDeltaMovement().lengthSqr() <= MAX_SETTLE_SPEED * MAX_SETTLE_SPEED;
	}

	private void checkContactBurst() {
		if (!(this.level() instanceof ServerLevel serverLevel)) return;

		List<LivingEntity> contacts = serverLevel.getEntitiesOfClass(
			LivingEntity.class,
			this.getBoundingBox().inflate(0.3),
			this::canTriggerBurstOnContact
		);
		if (contacts.isEmpty()) return;

		LivingEntity hit = contacts.get(0);
		applyDirectHit(serverLevel, hit);
		burst(new EntityHitResult(hit));
	}

	private void applyDirectHit(ServerLevel serverLevel, LivingEntity livingTarget) {
		if (isBlightlingImmune(livingTarget)) return;

		LivingEntity owner = this.getOwner() instanceof LivingEntity le ? le : null;
		livingTarget.hurt(this.damageSources().mobProjectile(this, owner), DIRECT_HIT_DAMAGE);
		ModEffects.addBlight(livingTarget, 2);
	}

	private boolean canTriggerBurstOnContact(LivingEntity target) {
		if (!target.isAlive()) return false;
		if (target == this.getOwner() && this.tickCount < 5) return false;
		return target.getBoundingBox().inflate(0.05).intersects(this.getBoundingBox());
	}

	private boolean isBlightlingImmune(LivingEntity target) {
		return target instanceof BlightlingEntity;
	}
}


