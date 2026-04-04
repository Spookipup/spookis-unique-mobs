package spookipup.uniquemobs.entity.variant.ghast;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.ai.BlightlingVolleyGoal;
import spookipup.uniquemobs.registry.ModEffects;

// shifts sideways before firing so the volley has some setup
public class BlightlingEntity extends Ghast {

	private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING =
		SynchedEntityData.defineId(BlightlingEntity.class, EntityDataSerializers.BOOLEAN);

	public BlightlingEntity(EntityType<? extends Ghast> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Ghast.createAttributes()
			.add(Attributes.MAX_HEALTH, 12.0)
			.add(Attributes.FOLLOW_RANGE, 48.0)
			.add(Attributes.ATTACK_DAMAGE, 2.0)
			.add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.SCALE, 0.28);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_IS_CHARGING, false);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new BlightlingVolleyGoal(this));
		this.goalSelector.addGoal(5, new Ghast.RandomFloatAroundGoal(this));
		this.goalSelector.addGoal(7, new Ghast.GhastLookGoal(this));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public void setCharging(boolean charging) {
		this.entityData.set(DATA_IS_CHARGING, charging);
	}

	@Override
	public boolean isCharging() {
		return this.entityData.get(DATA_IS_CHARGING);
	}

	public void faceDirection(Vec3 direction) {
		if (direction.lengthSqr() < 1.0E-4) return;

		Vec3 dir = direction.normalize();
		float yaw = (float) (-Math.toDegrees(Math.atan2(dir.x, dir.z)));
		float horizontal = (float) Math.sqrt(dir.x * dir.x + dir.z * dir.z);
		float pitch = (float) (-Math.toDegrees(Math.atan2(dir.y, horizontal)));

		this.setYRot(yaw);
		this.setYHeadRot(yaw);
		this.setYBodyRot(yaw);
		this.setXRot(pitch);
	}

	@Override
	public boolean canBeAffected(MobEffectInstance effectInstance) {
		return effectInstance.getEffect() != ModEffects.BLIGHT
			&& effectInstance.getEffect() != MobEffects.POISON
			&& super.canBeAffected(effectInstance);
	}

	@Override
	public void tick() {
		super.tick();

		if (this.getTarget() instanceof Player player && (player.isCreative() || player.isSpectator())) {
			this.setTarget(null);
		}
	}

	@Override
	public float getVoicePitch() {
		return 1.35F + this.random.nextFloat() * 0.2F;
	}

	@Override
	protected float getSoundVolume() {
		return 0.75F;
	}
}
