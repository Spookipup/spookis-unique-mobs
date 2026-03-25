package spookipup.uniquemobs.entity.variant.zombie;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.ai.ShootGoal;
import spookipup.uniquemobs.entity.ai.StrafeAndRetreatGoal;

// dual-headed skull lobber, tracks two targets independently
public class WitherZombieEntity extends Zombie {

	private static final int MELEE_WITHER_DURATION = 80;
	private static final int SECONDARY_SCAN_INTERVAL = 20;

	private static final EntityDataAccessor<Integer> DATA_LEFT_TARGET =
		SynchedEntityData.defineId(WitherZombieEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_RIGHT_TARGET =
		SynchedEntityData.defineId(WitherZombieEntity.class, EntityDataSerializers.INT);

	private final float[] xRotHeads = new float[2];
	private final float[] yRotHeads = new float[2];
	private final float[] xRotOHeads = new float[2];
	private final float[] yRotOHeads = new float[2];

	private int secondaryScanCooldown;
	private int hopCooldown;

	public WitherZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes()
			.add(Attributes.MAX_HEALTH, 28.0)
			.add(Attributes.MOVEMENT_SPEED, 0.2)
			.add(Attributes.FOLLOW_RANGE, 32.0)
			.add(Attributes.SCALE, 1.1);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_LEFT_TARGET, 0);
		builder.define(DATA_RIGHT_TARGET, 0);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();

		this.goalSelector.removeAllGoals(goal -> goal instanceof MeleeAttackGoal);

		// keep distance so skulls have room to fly - close in only for melee
		this.goalSelector.addGoal(2, new StrafeAndRetreatGoal(this, 0.4, 5.0F, 8.0F, 18.0F));

		this.goalSelector.addGoal(2, new ShootGoal(this, 40, 70, 20.0F, 5,
			ShootGoal.direct((level, shooter) -> {
				LivingEntity target = ((Zombie) shooter).getTarget();
				if (target == null) return null;
				Vec3 eyePos = new Vec3(shooter.getX(), shooter.getEyeY(), shooter.getZ());
				Vec3 dir = target.getEyePosition().subtract(eyePos).normalize();
				WitherSkull skull = new WitherSkull(level, shooter, dir);
				skull.setPos(eyePos);
				return skull;
			}, 1.0F, 3.0F, SoundEvents.WITHER_SHOOT)
		));

		// melee fallback
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
	}

	@Override
	public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
		boolean hit = super.doHurtTarget(serverLevel, target);

		if (hit && target instanceof LivingEntity livingTarget) {
			livingTarget.addEffect(new MobEffectInstance(
				MobEffects.WITHER, MELEE_WITHER_DURATION, 0
			));
		}

		return hit;
	}

	@Override
	public boolean canBeAffected(MobEffectInstance effect) {
		if (effect.is(MobEffects.WITHER)) return false;
		return super.canBeAffected(effect);
	}

	@Override
	public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
		if (damageSource.getDirectEntity() instanceof WitherSkull skull
			&& skull.getOwner() == this) {
			return false;
		}
		return super.hurtServer(serverLevel, damageSource, amount);
	}

	@Override
	public void setTarget(LivingEntity target) {
		LivingEntity old = this.getTarget();
		super.setTarget(target);
		if (target != null && target != old) {
			this.playSound(SoundEvents.WITHER_SKELETON_AMBIENT, 1.5F, 0.6F);
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (!this.level().isClientSide()) {
			if (this.hopCooldown > 0) {
				this.hopCooldown--;
			} else if (this.onGround() && this.getTarget() != null && this.random.nextInt(40) == 0) {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.42, 0.0));
				this.hurtMarked = true;
				this.hopCooldown = 20 + this.random.nextInt(40);
			}

			updateHeadTargets();
		}

		// needs to run on both sides since client uses synced target IDs to compute rotations
		updateHeadRotations();

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(5) == 0) {
			this.level().addParticle(
				ParticleTypes.SMOKE,
				this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5),
				0.0, 0.02, 0.0
			);
		}
	}

	private void updateHeadTargets() {
		LivingEntity mainTarget = this.getTarget();
		int mainId = mainTarget != null ? mainTarget.getId() : 0;

		this.entityData.set(DATA_LEFT_TARGET, mainId);

		if (this.secondaryScanCooldown > 0) {
			this.secondaryScanCooldown--;
			int rightId = this.entityData.get(DATA_RIGHT_TARGET);
			if (rightId > 0) {
				Entity secondary = this.level().getEntity(rightId);
				if (secondary == null || !secondary.isAlive()
					|| this.distanceToSqr(secondary) > 24.0 * 24.0) {
					this.entityData.set(DATA_RIGHT_TARGET, mainId);
				}
			}
		} else {
			this.secondaryScanCooldown = SECONDARY_SCAN_INTERVAL;
			LivingEntity secondary = findSecondaryTarget(mainTarget);
			this.entityData.set(DATA_RIGHT_TARGET, secondary != null ? secondary.getId() : mainId);
		}
	}

	private LivingEntity findSecondaryTarget(LivingEntity exclude) {
		AABB searchBox = this.getBoundingBox().inflate(20.0, 8.0, 20.0);
		List<Player> players = this.level().getEntitiesOfClass(Player.class, searchBox,
			p -> p != exclude && p.isAlive() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(p)
				&& this.hasLineOfSight(p));

		Player closest = null;
		double closestDist = Double.MAX_VALUE;
		for (Player player : players) {
			double dist = this.distanceToSqr(player);
			if (dist < closestDist) {
				closestDist = dist;
				closest = player;
			}
		}
		return closest;
	}

	private void updateHeadRotations() {
		for (int i = 0; i < 2; i++) {
			this.xRotOHeads[i] = this.xRotHeads[i];
			this.yRotOHeads[i] = this.yRotHeads[i];

			int targetId = this.entityData.get(i == 0 ? DATA_LEFT_TARGET : DATA_RIGHT_TARGET);
			Entity target = targetId > 0 ? this.level().getEntity(targetId) : null;

			if (target instanceof LivingEntity living && living.isAlive()) {
				double dx = living.getX() - this.getX();
				double dy = living.getEyeY() - this.getEyeY();
				double dz = living.getZ() - this.getZ();
				double horizontalDist = Math.sqrt(dx * dx + dz * dz);

				float targetYRot = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
				float targetXRot = (float) -(Mth.atan2(dy, horizontalDist) * Mth.RAD_TO_DEG);

				this.xRotHeads[i] = rotlerp(this.xRotHeads[i], targetXRot, 40.0F);
				this.yRotHeads[i] = rotlerp(this.yRotHeads[i], targetYRot, 10.0F);
			} else {
				this.yRotHeads[i] = rotlerp(this.yRotHeads[i], this.yBodyRot, 10.0F);
				this.xRotHeads[i] = rotlerp(this.xRotHeads[i], 0.0F, 40.0F);
			}
		}
	}

	private float rotlerp(float current, float target, float maxStep) {
		float diff = Mth.wrapDegrees(target - current);
		diff = Mth.clamp(diff, -maxStep, maxStep);
		return current + diff;
	}

	public float[] getHeadXRots() {
		return this.xRotHeads;
	}

	public float[] getHeadYRots() {
		return this.yRotHeads;
	}
}
