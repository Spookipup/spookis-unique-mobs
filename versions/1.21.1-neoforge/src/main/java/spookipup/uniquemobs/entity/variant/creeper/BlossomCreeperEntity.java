package spookipup.uniquemobs.entity.variant.creeper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import spookipup.uniquemobs.entity.BlossomHelper;

public class BlossomCreeperEntity extends Creeper {

	public BlossomCreeperEntity(EntityType<? extends Creeper> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Creeper.createAttributes();
	}

	@Override
	public void tick() {
		BlossomHelper.tickNaturalSlowFall(this);

		if (isAlive() && getSwellDir() > 0 && getSwelling(1.0F) >= 1.0F) {
			if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
				blossomBurst(serverLevel);
				discard();
			}
			return;
		}

		super.tick();
	}

	private void blossomBurst(ServerLevel serverLevel) {
		float radius = isPowered() ? 6.0F : 4.25F;
		playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.3F, 1.35F);
		playSound(SoundEvents.CHERRY_LEAVES_BREAK, 1.4F, 0.75F);
		BlossomHelper.spawnPetalBurst(serverLevel, getX(), getY() + 0.8, getZ(), radius * 0.35, 80);

		for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius))) {
			if (entity == this) continue;
			double distance = Math.max(0.25, entity.distanceTo(this));
			if (distance > radius) continue;

			double falloff = 1.0 - distance / radius;
			BlossomHelper.applyPetalMark(entity, this, BlossomHelper.BLOOM_THRESHOLD);
			BlossomHelper.applyUpwardKnockback(entity, this,
				(isPowered() ? 3.2 : 2.35) + falloff * (isPowered() ? 2.2 : 1.65),
				(isPowered() ? 2.35 : 1.65) + falloff * (isPowered() ? 1.6 : 1.15)
			);
		}

		serverLevel.explode(this, getX(), getY(), getZ(),
			isPowered() ? 1.0F : 0.65F, false, Level.ExplosionInteraction.NONE);
	}

	@Override
	public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}
}
