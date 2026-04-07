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
		method = {
			"place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z",
			"m_142674_(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z"
		},
		at = {
			@At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/level/levelgen/feature/MonsterRoomFeature;randomEntityId(Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/entity/EntityType;"
			),
			@At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/level/levelgen/feature/MonsterRoomFeature;m_225153_(Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/entity/EntityType;",
				remap = false
			)
		}
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
		if (random.nextInt(10) == 0 && isEnabled(ModEntities.ENDER_ZOMBIE.get())) return ModEntities.ENDER_ZOMBIE.get();
		if (biome.is(BiomeTags.SPAWNS_SNOW_FOXES)) return ModEntities.FROZEN_ZOMBIE.get();
		if (biome.is(BiomeTags.HAS_DESERT_PYRAMID) || biome.is(BiomeTags.IS_BADLANDS)) return ModEntities.INFERNAL_ZOMBIE.get();
		if (biome.is(BiomeTags.IS_JUNGLE)) {
			return ConfigWeightedSelection.pickEntityType(random, ModEntities.VENOMOUS_ZOMBIE.get(),
				ModEntities.VENOMOUS_ZOMBIE.get(), ModEntities.PLAGUE_ZOMBIE.get());
		}
		if (isSwamp(biome)) {
			return ConfigWeightedSelection.pickEntityType(random, ModEntities.PLAGUE_ZOMBIE.get(),
				ModEntities.PLAGUE_ZOMBIE.get(), ModEntities.VENOMOUS_ZOMBIE.get());
		}
		if (biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.DRIPSTONE_CAVES)) {
			return ConfigWeightedSelection.pickEntityType(random, ModEntities.VENOMOUS_ZOMBIE.get(),
				ModEntities.VENOMOUS_ZOMBIE.get(), ModEntities.PLAGUE_ZOMBIE.get());
		}
		return ConfigWeightedSelection.pickEntityType(random, ModEntities.SPRINTER_ZOMBIE.get(),
			ModEntities.SPRINTER_ZOMBIE.get(), ModEntities.ARMORED_ZOMBIE.get(), ModEntities.VENOMOUS_ZOMBIE.get(), ModEntities.PLAGUE_ZOMBIE.get());
	}

	@Unique
	private static EntityType<?> pickSkeleton(Holder<Biome> biome, RandomSource random) {
		if (random.nextInt(10) == 0 && isEnabled(ModEntities.ENDER_SKELETON.get())) return ModEntities.ENDER_SKELETON.get();
		if (biome.is(BiomeTags.HAS_DESERT_PYRAMID) || biome.is(BiomeTags.IS_BADLANDS)) return ModEntities.EMBER_SKELETON.get();
		if (biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_FOREST) || isSwamp(biome)) return ModEntities.POISON_SKELETON.get();
		return ConfigWeightedSelection.pickEntityType(random, ModEntities.MULTISHOT_SKELETON.get(),
			ModEntities.MULTISHOT_SKELETON.get(), ModEntities.SNIPER_SKELETON.get(), ModEntities.POISON_SKELETON.get());
	}

	// no ender spider variant exists so no rare ender roll here
	@Unique
	private static EntityType<?> pickSpider(Holder<Biome> biome, RandomSource random) {
		if (biome.is(BiomeTags.SPAWNS_SNOW_FOXES)) return ModEntities.ICE_SPIDER.get();
		if (biome.is(BiomeTags.HAS_DESERT_PYRAMID) || biome.is(BiomeTags.IS_BADLANDS)) return ModEntities.MAGMA_SPIDER.get();
		if (biome.is(BiomeTags.IS_JUNGLE)) {
			return ConfigWeightedSelection.pickEntityType(random, ModEntities.JUMPING_SPIDER.get(),
				ModEntities.JUMPING_SPIDER.get(), ModEntities.SPITTING_SPIDER.get());
		}
		if (biome.is(BiomeTags.IS_FOREST)) {
			return ConfigWeightedSelection.pickEntityType(random, ModEntities.JUMPING_SPIDER.get(),
				ModEntities.JUMPING_SPIDER.get(), ModEntities.WEB_SPINNER_SPIDER.get());
		}
		if (isSwamp(biome)) {
			return ConfigWeightedSelection.pickEntityType(random, ModEntities.SPITTING_SPIDER.get(),
				ModEntities.SPITTING_SPIDER.get(), ModEntities.WEB_SPINNER_SPIDER.get());
		}
		if (biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.DRIPSTONE_CAVES)) {
			return ConfigWeightedSelection.pickEntityType(random, ModEntities.WEB_SPINNER_SPIDER.get(),
				ModEntities.WEB_SPINNER_SPIDER.get(), ModEntities.SPITTING_SPIDER.get());
		}
		return ConfigWeightedSelection.pickEntityType(random, ModEntities.SPITTING_SPIDER.get(),
			ModEntities.SPITTING_SPIDER.get(), ModEntities.WEB_SPINNER_SPIDER.get(), ModEntities.JUMPING_SPIDER.get());
	}
}


