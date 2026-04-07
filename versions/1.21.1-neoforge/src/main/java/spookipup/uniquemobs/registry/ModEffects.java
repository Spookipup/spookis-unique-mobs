package spookipup.uniquemobs.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import spookipup.uniquemobs.UniqueMobs;
import spookipup.uniquemobs.effect.BlightMobEffect;
import spookipup.uniquemobs.effect.BlossomDriftMobEffect;
import spookipup.uniquemobs.effect.BrandMarkMobEffect;
import spookipup.uniquemobs.effect.PetalMarkMobEffect;
import spookipup.uniquemobs.effect.SoulScorchMobEffect;

public class ModEffects {

	private static final DeferredRegister<MobEffect> EFFECTS =
		DeferredRegister.create(Registries.MOB_EFFECT, UniqueMobs.MOD_ID);

	public static final Holder<MobEffect> SOUL_SCORCH = EFFECTS.register("soul_scorch", SoulScorchMobEffect::new);
	public static final Holder<MobEffect> BLIGHT = EFFECTS.register("blight", BlightMobEffect::new);
	public static final Holder<MobEffect> BRAND_MARK = EFFECTS.register("brand_mark", BrandMarkMobEffect::new);
	public static final Holder<MobEffect> PETAL_MARK = EFFECTS.register("petal_mark", PetalMarkMobEffect::new);
	public static final Holder<MobEffect> BLOSSOM_DRIFT = EFFECTS.register("blossom_drift", BlossomDriftMobEffect::new);

	public static void addBlight(LivingEntity target, int stacks) {
		int currentStacks = 0;
		int duration = 100;

		MobEffectInstance current = target.getEffect(BLIGHT);
		if (current != null) {
			currentStacks = current.getAmplifier() + 1;
			duration = Math.max(duration, current.getDuration());
		}

		int totalStacks = Math.min(4, currentStacks + stacks);
		int totalDuration = Math.min(220, duration + 30 + stacks * 20);
		target.addEffect(new MobEffectInstance(BLIGHT, totalDuration, totalStacks - 1));

		target.addEffect(new MobEffectInstance(MobEffects.POISON, 70 + totalStacks * 20, totalStacks >= 3 ? 1 : 0));
		if (totalStacks >= 2) {
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60 + totalStacks * 15, 0));
		}
		if (totalStacks >= 3) {
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80 + totalStacks * 20, 0));
		}
		if (totalStacks >= 4) {
			target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 50, 0));
		}
	}

	public static void register(IEventBus bus) {
		EFFECTS.register(bus);
	}
}


