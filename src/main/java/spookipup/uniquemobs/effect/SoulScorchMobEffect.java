package spookipup.uniquemobs.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

// soul scorch flares harder when the target keeps moving
public class SoulScorchMobEffect extends MobEffect {

	private static final int BASELINE_TICK_RATE = 25;
	private static final int FLARE_BASE_TICK_RATE = 12;
	private static final double FLARE_HORIZONTAL_SPEED = 0.11;
	private static final double FLARE_VERTICAL_SPEED = 0.08;

	public SoulScorchMobEffect() {
		super(MobEffectCategory.HARMFUL, 0x6ED6FF);
	}

	@Override
	public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
		if (livingEntity.tickCount % BASELINE_TICK_RATE == 0) {
			float damage = 0.75F + amplifier * 0.5F;
			livingEntity.hurt(livingEntity.damageSources().magic(), damage);
		}

		Vec3 velocity = livingEntity.getDeltaMovement();
		boolean flaring = livingEntity.isSprinting()
			|| velocity.horizontalDistanceSqr() > FLARE_HORIZONTAL_SPEED * FLARE_HORIZONTAL_SPEED
			|| Math.abs(velocity.y) > FLARE_VERTICAL_SPEED;

		int flareRate = Math.max(6, FLARE_BASE_TICK_RATE - amplifier * 2);
		if (flaring && livingEntity.tickCount % flareRate == 0) {
			float flareDamage = 1.0F + amplifier * 0.75F;
			livingEntity.hurt(livingEntity.damageSources().magic(), flareDamage);
		}
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}
}


