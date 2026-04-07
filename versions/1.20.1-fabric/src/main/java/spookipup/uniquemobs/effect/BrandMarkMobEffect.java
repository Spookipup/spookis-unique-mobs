package spookipup.uniquemobs.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

// marker effect for the brand blaze death save
public class BrandMarkMobEffect extends MobEffect {

	public BrandMarkMobEffect() {
		super(MobEffectCategory.HARMFUL, 0xB5311B);
	}

	@Override
	public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return false;
	}
}


