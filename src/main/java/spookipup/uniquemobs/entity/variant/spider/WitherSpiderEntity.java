package spookipup.uniquemobs.entity.variant.spider;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

public class WitherSpiderEntity extends Spider {

	private static final int WITHER_DURATION = 100;
	private static final int WITHER_AMPLIFIER = 0;

	public WitherSpiderEntity(EntityType<? extends Spider> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Spider.createAttributes()
			.add(Attributes.MAX_HEALTH, 20.0)
			.add(Attributes.ATTACK_DAMAGE, 3.0);
	}

	@Override
	public boolean canBeAffected(MobEffectInstance effect) {
		if (effect.getEffect() == MobEffects.WITHER) return false;
		return super.canBeAffected(effect);
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean hit = super.doHurtTarget(target);

		if (hit && target instanceof LivingEntity livingTarget) {
			livingTarget.addEffect(new MobEffectInstance(
				MobEffects.WITHER, WITHER_DURATION, WITHER_AMPLIFIER
			));
		}

		return hit;
	}

	@Override
	public void die(DamageSource damageSource) {
		if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
			int count = 1 + this.random.nextInt(2);
			for (int i = 0; i < count; i++) {
				Spider baby = EntityType.CAVE_SPIDER.create(serverLevel);
				if (baby == null) continue;
				double offsetX = (this.random.nextDouble() - 0.5) * 1.5;
				double offsetZ = (this.random.nextDouble() - 0.5) * 1.5;
				baby.setPos(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
				baby.setYRot(this.random.nextFloat() * 360.0F);
				serverLevel.addFreshEntity(baby);
			}
		}

		super.die(damageSource);
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(4) == 0) {
			this.level().addParticle(
				ParticleTypes.SMOKE,
				this.getRandomX(0.6), this.getRandomY() - 0.1, this.getRandomZ(0.6),
				0.0, 0.01, 0.0
			);
		}
	}
}
