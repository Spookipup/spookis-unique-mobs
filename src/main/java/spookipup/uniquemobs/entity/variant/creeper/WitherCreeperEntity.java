package spookipup.uniquemobs.entity.variant.creeper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

// big boom + wither. slow fuse so you hear it coming
public class WitherCreeperEntity extends Creeper {

	private static final float EXPLOSION_POWER = 4.5F;
	private static final float EXPLOSION_POWER_POWERED = 6.5F;
	private static final int WITHER_DURATION = 160;
	private static final int WITHER_AMPLIFIER = 1;
	private static final float WITHER_RANGE = 8.0F;

	public WitherCreeperEntity(EntityType<? extends Creeper> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Creeper.createAttributes()
			.add(Attributes.MOVEMENT_SPEED, 0.2)
			.add(Attributes.SCALE, 1.1);
	}

	@Override
	public boolean canBeAffected(MobEffectInstance effect) {
		if (effect.is(MobEffects.WITHER)) return false;
		return super.canBeAffected(effect);
	}

	@Override
	public void tick() {
		if (this.isAlive() && getSwellDir() > 0 && getSwelling(1.0F) >= 1.0F) {
			if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
				float power = this.isPowered() ? EXPLOSION_POWER_POWERED : EXPLOSION_POWER;
				serverLevel.explode(
					this, this.getX(), this.getY(), this.getZ(),
					power, false, Level.ExplosionInteraction.MOB
				);
				applyWitherToNearby();
				spawnWitherCloud();
				placeWitherRose(serverLevel);
				this.discard();
			}
			return;
		}

		super.tick();
	}

	private void applyWitherToNearby() {
		float range = this.isPowered() ? WITHER_RANGE * 1.5F : WITHER_RANGE;
		for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
				this.getBoundingBox().inflate(range))) {
			if (entity == this) continue;
			entity.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_DURATION, WITHER_AMPLIFIER));
		}
	}

	private void placeWitherRose(ServerLevel serverLevel) {
		BlockPos pos = this.blockPosition();
		if (serverLevel.getBlockState(pos).isAir() && Blocks.WITHER_ROSE.defaultBlockState().canSurvive(serverLevel, pos)) {
			serverLevel.setBlockAndUpdate(pos, Blocks.WITHER_ROSE.defaultBlockState());
		}
	}

	private void spawnWitherCloud() {
		AreaEffectCloud cloud = new AreaEffectCloud(
			this.level(), this.getX(), this.getY(), this.getZ()
		);
		cloud.setRadius(this.isPowered() ? 5.0F : 3.5F);
		cloud.setRadiusOnUse(-0.3F);
		cloud.setRadiusPerTick(-0.015F);
		cloud.setDuration(80);
		cloud.setWaitTime(5);
		cloud.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
		cloud.setOwner(this);
		cloud.setCustomParticle(ParticleTypes.SMOKE);

		this.level().addFreshEntity(cloud);
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(3) == 0) {
			this.level().addParticle(
				ParticleTypes.SMOKE,
				this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5),
				0.0, 0.02, 0.0
			);
		}
	}
}
