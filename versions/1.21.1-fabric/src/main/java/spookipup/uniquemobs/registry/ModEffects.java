package spookipup.uniquemobs.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import spookipup.uniquemobs.UniqueMobs;
import spookipup.uniquemobs.effect.BlightMobEffect;
import spookipup.uniquemobs.effect.BlossomDriftMobEffect;
import spookipup.uniquemobs.effect.BrandMarkMobEffect;
import spookipup.uniquemobs.effect.PetalMarkMobEffect;
import spookipup.uniquemobs.effect.SoulScorchMobEffect;

public class ModEffects {

	public static final Holder<MobEffect> SOUL_SCORCH = Registry.registerForHolder(
		BuiltInRegistries.MOB_EFFECT,
		ResourceLocation.fromNamespaceAndPath(UniqueMobs.MOD_ID, "soul_scorch"),
		new SoulScorchMobEffect()
	);

	public static final Holder<MobEffect> BLIGHT = Registry.registerForHolder(
		BuiltInRegistries.MOB_EFFECT,
		ResourceLocation.fromNamespaceAndPath(UniqueMobs.MOD_ID, "blight"),
		new BlightMobEffect()
	);

	public static final Holder<MobEffect> BRAND_MARK = Registry.registerForHolder(
		BuiltInRegistries.MOB_EFFECT,
		ResourceLocation.fromNamespaceAndPath(UniqueMobs.MOD_ID, "brand_mark"),
		new BrandMarkMobEffect()
	);

	public static final Holder<MobEffect> PETAL_MARK = Registry.registerForHolder(
		BuiltInRegistries.MOB_EFFECT,
		ResourceLocation.fromNamespaceAndPath(UniqueMobs.MOD_ID, "petal_mark"),
		new PetalMarkMobEffect()
	);

	public static final Holder<MobEffect> BLOSSOM_DRIFT = Registry.registerForHolder(
		BuiltInRegistries.MOB_EFFECT,
		ResourceLocation.fromNamespaceAndPath(UniqueMobs.MOD_ID, "blossom_drift"),
		new BlossomDriftMobEffect()
	);

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

	public static void init() {}
}


