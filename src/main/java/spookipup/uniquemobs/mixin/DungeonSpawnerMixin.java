package spookipup.uniquemobs.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookipup.uniquemobs.config.ModConfig;
import spookipup.uniquemobs.registry.ModEntities;
import spookipup.uniquemobs.spawn.ConfigWeightedSelection;

// biome-aware dungeon spawner replacement. ThreadLocal because worldgen is multithreaded
@Mixin(MonsterRoomFeature.class)
public class DungeonSpawnerMixin {

	@Unique
	private static final ThreadLocal<FeaturePlaceContext<?>> CONTEXT = new ThreadLocal<>();

	// grab the place context so we can read the biome later in the wrap
	@Inject(method = "place", at = @At("HEAD"))
	private void captureContext(FeaturePlaceContext<NoneFeatureConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
		CONTEXT.set(context);
	}

	@Inject(method = "place", at = @At("RETURN"))
	private void clearContext(FeaturePlaceContext<NoneFeatureConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
		CONTEXT.remove();
	}

	// randomEntityId normally picks zombie/skeleton/spider for the spawner.
	// we let it pick first, then sometimes swap it for a variant
	@WrapOperation(
		method = "place",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/MonsterRoomFeature;randomEntityId(Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/entity/EntityType;")
	)
	private EntityType<?> wrapSpawnerMob(MonsterRoomFeature instance, RandomSource random, Operation<EntityType<?>> original) {
		EntityType<?> vanilla = original.call(instance, random);

		FeaturePlaceContext<?> ctx = CONTEXT.get();
		if (ctx == null) return vanilla;

		Holder<Biome> biome = ctx.level().getBiome(ctx.origin());
		boolean themed = isBiomeThemed(biome);

		ModConfig cfg = ModConfig.get();
		float chance = themed
			? (float) cfg.dungeonThemedReplacementChance
			: (float) cfg.dungeonReplacementChance;
		if (random.nextFloat() >= chance) return vanilla;

		return pickVariant(vanilla, biome, random);
	}

	@Unique
	private static boolean isBiomeThemed(Holder<Biome> biome) {
		return biome.is(BiomeTags.SPAWNS_SNOW_FOXES)
			|| biome.is(BiomeTags.HAS_DESERT_PYRAMID)
			|| biome.is(BiomeTags.IS_BADLANDS)
			|| biome.is(BiomeTags.IS_JUNGLE)
			|| biome.is(BiomeTags.IS_FOREST)
			|| biome.is(Biomes.SWAMP)
			|| biome.is(Biomes.MANGROVE_SWAMP)
			|| biome.is(Biomes.LUSH_CAVES)
			|| biome.is(Biomes.DRIPSTONE_CAVES);
	}

	@Unique
	private static boolean isSwamp(Holder<Biome> biome) {
		return biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP);
	}

	@Unique
	private static EntityType<?> pickVariant(EntityType<?> vanilla, Holder<Biome> biome, RandomSource random) {
		EntityType<?> variant = vanilla;
		if (vanilla == EntityType.ZOMBIE) variant = pickZombie(biome, random);
		else if (vanilla == EntityType.SKELETON) variant = pickSkeleton(biome, random);
		else if (vanilla == EntityType.SPIDER) variant = pickSpider(biome, random);
		// fall back to vanilla if the picked variant is disabled
		if (variant != vanilla && !isEnabled(variant)) return vanilla;
		return variant;
	}

	@Unique
	private static boolean isEnabled(EntityType<?> type) {
		String id = EntityType.getKey(type).getPath();
		return ModConfig.get().isMobEnabled(id);
	}

	@Unique
	private static EntityType<?> pickZombie(Holder<Biome> biome, RandomSource random) {
		// rare ender variant can show up in any biome
		if (random.nextInt(10) == 0 && isEnabled(ModEntities.ENDER_ZOMBIE)) return ModEntities.ENDER_ZOMBIE;
		if (biome.is(BiomeTags.SPAWNS_SNOW_FOXES)) return ModEntities.FROZEN_ZOMBIE;
		if (biome.is(BiomeTags.HAS_DESERT_PYRAMID) || biome.is(BiomeTags.IS_BADLANDS)) return ModEntities.INFERNAL_ZOMBIE;
		if (biome.is(BiomeTags.IS_JUNGLE)) {
			return ConfigWeightedSelection.pickEntityType(random, ModEntities.VENOMOUS_ZOMBIE,
				ModEntities.VENOMOUS_ZOMBIE, ModEntities.PLAGUE_ZOMBIE);
		}
		if (isSwamp(biome)) {
			return ConfigWeightedSelection.pickEntityType(random, ModEntities.PLAGUE_ZOMBIE,
				ModEntities.PLAGUE_ZOMBIE, ModEntities.VENOMOUS_ZOMBIE);
		}
		if (biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.DRIPSTONE_CAVES)) {
			return ConfigWeightedSelection.pickEntityType(random, ModEntities.VENOMOUS_ZOMBIE,
				ModEntities.VENOMOUS_ZOMBIE, ModEntities.PLAGUE_ZOMBIE);
		}
		return ConfigWeightedSelection.pickEntityType(random, ModEntities.SPRINTER_ZOMBIE,
			ModEntities.SPRINTER_ZOMBIE, ModEntities.ARMORED_ZOMBIE, ModEntities.VENOMOUS_ZOMBIE, ModEntities.PLAGUE_ZOMBIE);
	}

	@Unique
	private static EntityType<?> pickSkeleton(Holder<Biome> biome, RandomSource random) {
		if (random.nextInt(10) == 0 && isEnabled(ModEntities.ENDER_SKELETON)) return ModEntities.ENDER_SKELETON;
		if (biome.is(BiomeTags.HAS_DESERT_PYRAMID) || biome.is(BiomeTags.IS_BADLANDS)) return ModEntities.EMBER_SKELETON;
		if (biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_FOREST) || isSwamp(biome)) return ModEntities.POISON_SKELETON;
		return ConfigWeightedSelection.pickEntityType(random, ModEntities.MULTISHOT_SKELETON,
			ModEntities.MULTISHOT_SKELETON, ModEntities.SNIPER_SKELETON, ModEntities.POISON_SKELETON);
	}

	// no ender spider variant exists so no rare ender roll here
	@Unique
	private static EntityType<?> pickSpider(Holder<Biome> biome, RandomSource random) {
		if (biome.is(BiomeTags.SPAWNS_SNOW_FOXES)) return ModEntities.ICE_SPIDER;
		if (biome.is(BiomeTags.HAS_DESERT_PYRAMID) || biome.is(BiomeTags.IS_BADLANDS)) return ModEntities.MAGMA_SPIDER;
		if (biome.is(BiomeTags.IS_JUNGLE)) {
			return ConfigWeightedSelection.pickEntityType(random, ModEntities.JUMPING_SPIDER,
				ModEntities.JUMPING_SPIDER, ModEntities.SPITTING_SPIDER);
		}
		if (biome.is(BiomeTags.IS_FOREST)) {
			return ConfigWeightedSelection.pickEntityType(random, ModEntities.JUMPING_SPIDER,
				ModEntities.JUMPING_SPIDER, ModEntities.WEB_SPINNER_SPIDER);
		}
		if (isSwamp(biome)) {
			return ConfigWeightedSelection.pickEntityType(random, ModEntities.SPITTING_SPIDER,
				ModEntities.SPITTING_SPIDER, ModEntities.WEB_SPINNER_SPIDER);
		}
		if (biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.DRIPSTONE_CAVES)) {
			return ConfigWeightedSelection.pickEntityType(random, ModEntities.WEB_SPINNER_SPIDER,
				ModEntities.WEB_SPINNER_SPIDER, ModEntities.SPITTING_SPIDER);
		}
		return ConfigWeightedSelection.pickEntityType(random, ModEntities.SPITTING_SPIDER,
			ModEntities.SPITTING_SPIDER, ModEntities.WEB_SPINNER_SPIDER, ModEntities.JUMPING_SPIDER);
	}
}
