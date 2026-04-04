package spookipup.uniquemobs.entity.variant.ghast;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.ai.DeltaGhastLaserGoal;
import spookipup.uniquemobs.entity.ai.DeltaGhastMaintainSightGoal;

// keeps the beam aim synced to the client
public class DeltaGhastEntity extends Ghast {

	public static final double MOUTH_Y_OFFSET = -1.2;

	private static final EntityDataAccessor<Integer> DATA_CHARGE_TICKS =
		SynchedEntityData.defineId(DeltaGhastEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_IS_FIRING =
		SynchedEntityData.defineId(DeltaGhastEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING =
		SynchedEntityData.defineId(DeltaGhastEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Float> DATA_BEAM_YAW =
		SynchedEntityData.defineId(DeltaGhastEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_BEAM_PITCH =
		SynchedEntityData.defineId(DeltaGhastEntity.class, EntityDataSerializers.FLOAT);

	public DeltaGhastEntity(EntityType<? extends Ghast> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Ghast.createAttributes()
			.add(Attributes.MAX_HEALTH, 20.0)
			.add(Attributes.FOLLOW_RANGE, 64.0)
			.add(Attributes.SCALE, 0.8);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_CHARGE_TICKS, 0);
		builder.define(DATA_IS_FIRING, false);
		builder.define(DATA_IS_CHARGING, false);
		builder.define(DATA_BEAM_YAW, 0.0F);
		builder.define(DATA_BEAM_PITCH, 0.0F);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new DeltaGhastLaserGoal(this));
		this.goalSelector.addGoal(3, new DeltaGhastMaintainSightGoal(this));
		this.goalSelector.addGoal(5, new Ghast.RandomFloatAroundGoal(this));
		this.goalSelector.addGoal(7, new Ghast.GhastLookGoal(this));

		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	public int getChargeTicks() {
		return this.entityData.get(DATA_CHARGE_TICKS);
	}

	public void setChargeTicks(int ticks) {
		this.entityData.set(DATA_CHARGE_TICKS, ticks);
	}

	public boolean isFiring() {
		return this.entityData.get(DATA_IS_FIRING);
	}

	public void setFiring(boolean firing) {
		this.entityData.set(DATA_IS_FIRING, firing);
	}

	@Override
	public void setCharging(boolean charging) {
		this.entityData.set(DATA_IS_CHARGING, charging);
	}

	@Override
	public boolean isCharging() {
		return this.entityData.get(DATA_IS_CHARGING);
	}

	public void setBeamDirection(Vec3 dir) {
		float yaw = (float) (-Math.toDegrees(Math.atan2(dir.x, dir.z)));
		float pitch = (float) (-Math.toDegrees(Math.asin(dir.y)));
		this.entityData.set(DATA_BEAM_YAW, yaw);
		this.entityData.set(DATA_BEAM_PITCH, pitch);
		this.setYRot(yaw);
		this.yRotO = yaw;
		this.setXRot(pitch);
		this.xRotO = pitch;
		this.setYHeadRot(yaw);
		this.yHeadRotO = yaw;
	}

	public Vec3 getBeamDirection(float partialTick) {
		float yaw = this.entityData.get(DATA_BEAM_YAW);
		float pitch = this.entityData.get(DATA_BEAM_PITCH);
		float yRad = (float) Math.toRadians(-yaw);
		float pRad = (float) Math.toRadians(-pitch);
		float cosPitch = Mth.cos(pRad);
		return new Vec3(Mth.sin(yRad) * cosPitch, Mth.sin(pRad), Mth.cos(yRad) * cosPitch);
	}
}
