package spookipup.uniquemobs.entity.variant.ghast;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.ai.SkitterlingAttackGoal;

// dashy little ghast with a short-range followup breath
public class SkitterlingEntity extends Ghast {

	public static final double MOUTH_Y_OFFSET = 0.32;
	private static final float HITBOX_SCALE = 1.22F;

	private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING =
		SynchedEntityData.defineId(SkitterlingEntity.class, EntityDataSerializers.BOOLEAN);

	public SkitterlingEntity(EntityType<? extends Ghast> entityType, Level level) {
		super(entityType, level);
		this.refreshDimensions();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Ghast.createAttributes()
			.add(Attributes.MAX_HEALTH, 14.0)
			.add(Attributes.FOLLOW_RANGE, 40.0)
			.add(Attributes.ATTACK_DAMAGE, 4.0)
			.add(Attributes.MOVEMENT_SPEED, 0.35);
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return super.getDimensions(pose).scale(HITBOX_SCALE);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_IS_CHARGING, false);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.getAvailableGoals().removeIf(
			g -> g.getGoal().getClass().getEnclosingClass() == Ghast.class
				&& g.getGoal().getClass().getSimpleName().contains("Fireball"));
		this.targetSelector.getAvailableGoals().removeIf(
			g -> g.getGoal() instanceof NearestAttackableTargetGoal);
		this.goalSelector.addGoal(1, new SkitterlingAttackGoal(this));
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

	public Vec3 getMouthPosition() {
		return this.position().add(0.0, MOUTH_Y_OFFSET, 0.0);
	}

	@Override
	public float getVoicePitch() {
		return 1.45F + this.random.nextFloat() * 0.25F;
	}

	@Override
	protected float getSoundVolume() {
		return 0.8F;
	}
}


