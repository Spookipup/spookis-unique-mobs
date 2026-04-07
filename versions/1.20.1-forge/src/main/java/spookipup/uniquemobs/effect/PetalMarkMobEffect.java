package spookipup.uniquemobs.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import spookipup.uniquemobs.entity.BlossomHelper;

public class PetalMarkMobEffect extends MobEffect {

	public PetalMarkMobEffect() {
		super(MobEffectCategory.HARMFUL, 0xF7B7D3);
	}

	@Override
	public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
		if (amplifier >= BlossomHelper.BLOOM_THRESHOLD - 1) {
			BlossomHelper.triggerDrift(livingEntity, null, BlossomHelper.DRIFT_DURATION);
		}
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return amplifier >= BlossomHelper.BLOOM_THRESHOLD - 1;
	}
}
