package spookipup.uniquemobs.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.registry.ModEntities;

// visual-only arc for the storm blaze strike
public class StormArcEntity extends Entity {

	private static final EntityDataAccessor<Float> DATA_END_OFFSET_X =
		SynchedEntityData.defineId(StormArcEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_END_OFFSET_Y =
		SynchedEntityData.defineId(StormArcEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_END_OFFSET_Z =
		SynchedEntityData.defineId(StormArcEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_SEED =
		SynchedEntityData.defineId(StormArcEntity.class, EntityDataSerializers.INT);

	private static final int LIFETIME = 3;

	public StormArcEntity(EntityType<? extends StormArcEntity> entityType, Level level) {
		super(entityType, level);
		this.noPhysics = true;
		this.setNoGravity(true);
	}

	public StormArcEntity(Level level, Vec3 origin, Vec3 end) {
		this(ModEntities.STORM_ARC, level);
		this.setPos(origin.x, origin.y, origin.z);
		this.setEndOffset(end.subtract(origin));
		this.setSeed(this.random.nextInt());
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_END_OFFSET_X, 0.0F);
		this.entityData.define(DATA_END_OFFSET_Y, 0.0F);
		this.entityData.define(DATA_END_OFFSET_Z, 0.0F);
		this.entityData.define(DATA_SEED, 0);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		this.setEndOffset(new Vec3(
			tag.getFloat("EndOffsetX"),
			tag.getFloat("EndOffsetY"),
			tag.getFloat("EndOffsetZ")
		));
		this.setSeed(tag.getInt("Seed"));
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		Vec3 endOffset = this.getEndOffset();
		tag.putFloat("EndOffsetX", (float) endOffset.x);
		tag.putFloat("EndOffsetY", (float) endOffset.y);
		tag.putFloat("EndOffsetZ", (float) endOffset.z);
		tag.putInt("Seed", this.getSeedValue());
	}

	@Override
	public void tick() {
		super.tick();
		this.noPhysics = true;
		this.setDeltaMovement(Vec3.ZERO);
		if (this.tickCount >= LIFETIME) {
			this.discard();
		}
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		return distance < 4096.0;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float amount) {
		return false;
	}

	public Vec3 getEndOffset() {
		return new Vec3(
			this.entityData.get(DATA_END_OFFSET_X),
			this.entityData.get(DATA_END_OFFSET_Y),
			this.entityData.get(DATA_END_OFFSET_Z)
		);
	}

	public void setEndOffset(Vec3 endOffset) {
		this.entityData.set(DATA_END_OFFSET_X, (float) endOffset.x);
		this.entityData.set(DATA_END_OFFSET_Y, (float) endOffset.y);
		this.entityData.set(DATA_END_OFFSET_Z, (float) endOffset.z);
	}

	public int getSeedValue() {
		return this.entityData.get(DATA_SEED);
	}

	public void setSeed(int seed) {
		this.entityData.set(DATA_SEED, seed);
	}
}


