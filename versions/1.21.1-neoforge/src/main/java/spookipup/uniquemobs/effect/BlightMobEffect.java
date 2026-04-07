package spookipup.uniquemobs.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

// blight keeps ticking between direct hits
public class BlightMobEffect extends MobEffect {

	private static final int BASE_TICK_RATE = 34;

	public BlightMobEffect() {
		super(MobEffectCategory.HARMFUL, 0x8DBA45);
	}

	@Override
	public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
		int stacks = amplifier + 1;
		int tickRate = Math.max(16, BASE_TICK_RATE - amplifier * 4);
		if (livingEntity.tickCount % tickRate == 0) {
			float damage = 0.5F + amplifier * 0.4F;
			livingEntity.hurt(livingEntity.damageSources().magic(), damage);
		}
		return stacks > 0;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}
}


