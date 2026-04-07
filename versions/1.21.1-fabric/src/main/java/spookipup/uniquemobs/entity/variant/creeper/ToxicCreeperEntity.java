package spookipup.uniquemobs.entity.variant.creeper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

public class ToxicCreeperEntity extends Creeper {

	private static final int CLOUD_POISON_DURATION = 160;
	private static final int CLOUD_NAUSEA_DURATION = 160;
	private static final float CLOUD_RADIUS = 4.0F;
	private static final float CLOUD_RADIUS_POWERED = 6.0F;
	private static final int CLOUD_LINGER_DURATION = 100;
	private static final double CLOUD_Y_OFFSET = 0.6;

	public ToxicCreeperEntity(EntityType<? extends Creeper> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Creeper.createAttributes();
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(4) == 0) {
			this.level().addParticle(
				ParticleTypes.ITEM_SLIME,
				this.getRandomX(0.5), this.getRandomY() - 0.2, this.getRandomZ(0.5),
				0.0, -0.05, 0.0
			);
		}
	}

	@Override
	public void tick() {
		// explodeCreeper() is private so we can't override it - catch the swell
		// right before it maxes out and do our thing instead
		if (this.isAlive() && getSwellDir() > 0 && getSwelling(1.0F) >= 1.0F) {
			if (!this.level().isClientSide()) {
				spawnGasCloud();
				this.discard();
			}
			return;
		}

		super.tick();
	}

	private void spawnGasCloud() {
		float radius = this.isPowered() ? CLOUD_RADIUS_POWERED : CLOUD_RADIUS;

		AreaEffectCloud cloud = new AreaEffectCloud(
			this.level(), this.getX(), this.getY() + CLOUD_Y_OFFSET, this.getZ()
		);
		cloud.setRadius(radius);
		cloud.setRadiusOnUse(-0.3F);
		cloud.setRadiusPerTick(-0.01F);
		cloud.setDuration(CLOUD_LINGER_DURATION);
		cloud.setWaitTime(5);
		cloud.addEffect(new MobEffectInstance(MobEffects.POISON, CLOUD_POISON_DURATION, 1));
		cloud.addEffect(new MobEffectInstance(MobEffects.CONFUSION, CLOUD_NAUSEA_DURATION, 0));
		cloud.setOwner(this);
		cloud.setParticle(ParticleTypes.ITEM_SLIME);

		this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.5F, 0.8F);
		this.level().addFreshEntity(cloud);
	}
}
