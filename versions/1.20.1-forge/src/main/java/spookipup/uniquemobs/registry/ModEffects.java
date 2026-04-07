package spookipup.uniquemobs.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegisterEvent;
import spookipup.uniquemobs.UniqueMobs;
import spookipup.uniquemobs.effect.BlightMobEffect;
import spookipup.uniquemobs.effect.BlossomDriftMobEffect;
import spookipup.uniquemobs.effect.BrandMarkMobEffect;
import spookipup.uniquemobs.effect.PetalMarkMobEffect;
import spookipup.uniquemobs.effect.SoulScorchMobEffect;

public class ModEffects {

	public static final MobEffect SOUL_SCORCH = new SoulScorchMobEffect();

	public static final MobEffect BLIGHT = new BlightMobEffect();

	public static final MobEffect BRAND_MARK = new BrandMarkMobEffect();
	public static final MobEffect PETAL_MARK = new PetalMarkMobEffect();
	public static final MobEffect BLOSSOM_DRIFT = new BlossomDriftMobEffect();

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
		bus.addListener(ModEffects::registerEffects);
	}

	private static void registerEffects(RegisterEvent event) {
		event.register(Registries.MOB_EFFECT, helper -> {
			helper.register(new ResourceLocation(UniqueMobs.MOD_ID, "soul_scorch"), SOUL_SCORCH);
			helper.register(new ResourceLocation(UniqueMobs.MOD_ID, "blight"), BLIGHT);
			helper.register(new ResourceLocation(UniqueMobs.MOD_ID, "brand_mark"), BRAND_MARK);
			helper.register(new ResourceLocation(UniqueMobs.MOD_ID, "petal_mark"), PETAL_MARK);
			helper.register(new ResourceLocation(UniqueMobs.MOD_ID, "blossom_drift"), BLOSSOM_DRIFT);
		});
	}
}


