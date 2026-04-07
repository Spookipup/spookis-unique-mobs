package spookipup.uniquemobs.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import spookipup.uniquemobs.config.ModConfig;
import spookipup.uniquemobs.mixin.accessor.TrialSpawnerAccessor;
import spookipup.uniquemobs.registry.ModEntities;
import spookipup.uniquemobs.spawn.ConfigWeightedSelection;

import java.util.List;

@Mixin(StructureTemplate.class)
public class TrialSpawnerPlacementMixin {

	@WrapOperation(
		method = "placeInWorld",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V")
	)
	private void wrapTrialSpawnerLoad(BlockEntity blockEntity, CompoundTag tag, HolderLookup.Provider provider, Operation<Void> original,
		ServerLevelAccessor level, BlockPos pos, BlockPos pivot, StructurePlaceSettings settings, RandomSource random, int flags) {
		original.call(blockEntity, tag, provider);

		if (!(blockEntity instanceof TrialSpawnerBlockEntity spawner)) return;

		TrialSpawner trialSpawner = spawner.getTrialSpawner();
		TrialSpawnerAccessor accessor = (TrialSpawnerAccessor) (Object) trialSpawner;
		boolean changed = false;

		TrialSpawnerConfig normalConfig = accessor.uniqueMobs$getNormalConfig();
		TrialSpawnerConfig replacedNormalConfig = maybeReplaceConfig(normalConfig, level.getLevel(), spawner.getBlockPos(), random);
		if (replacedNormalConfig != normalConfig) {
			accessor.uniqueMobs$setNormalConfig(replacedNormalConfig);
			changed = true;
		}

		TrialSpawnerConfig ominousConfig = accessor.uniqueMobs$getOminousConfig();
		TrialSpawnerConfig replacedOminousConfig = maybeReplaceConfig(ominousConfig, level.getLevel(), spawner.getBlockPos(), random);
		if (replacedOminousConfig != ominousConfig) {
			accessor.uniqueMobs$setOminousConfig(replacedOminousConfig);
			changed = true;
		}

		if (changed) {
			trialSpawner.getData().reset();
			trialSpawner.markUpdated();
			spawner.setChanged();
		}
	}

	@Unique
	private static TrialSpawnerConfig maybeReplaceConfig(TrialSpawnerConfig config, Level level, BlockPos pos, RandomSource random) {
		EntityType<?> vanilla = getConfiguredVanilla(config);
		if (vanilla == null) return config;

		EntityType<?> replacement = maybeReplace(vanilla, level, pos, random);
		if (replacement == vanilla) return config;

		return withEntity(config, replacement);
	}

	@Unique
	private static TrialSpawnerConfig withEntity(TrialSpawnerConfig config, EntityType<?> replacement) {
		SimpleWeightedRandomList.Builder<SpawnData> builder = SimpleWeightedRandomList.builder();
		String replacementId = EntityType.getKey(replacement).toString();

		for (WeightedEntry.Wrapper<SpawnData> entry : config.spawnPotentialsDefinition().unwrap()) {
			SpawnData spawnData = entry.data();
			CompoundTag entityTag = spawnData.entityToSpawn().copy();
			entityTag.putString("id", replacementId);
			builder.add(new SpawnData(entityTag, spawnData.customSpawnRules(), spawnData.equipment()), entry.weight().asInt());
		}

		return new TrialSpawnerConfig(
			config.spawnRange(),
			config.totalMobs(),
			config.simultaneousMobs(),
			config.totalMobsAddedPerPlayer(),
			config.simultaneousMobsAddedPerPlayer(),
			config.ticksBetweenSpawn(),
			builder.build(),
			config.lootTablesToEject(),
			config.itemsToDropWhenOminous()
		);
	}

	@Unique
	private static EntityType<?> maybeReplace(EntityType<?> original, Level level, BlockPos pos, RandomSource random) {
		EntityType<?> supported = normalizeSupportedTrialMob(original);
		if (supported == null) return original;

		Holder<Biome> biome = level.getBiome(pos);
		ModConfig cfg = ModConfig.get();
		float chance = isBiomeThemed(biome)
			? (float) cfg.dungeonThemedReplacementChance
			: (float) cfg.dungeonReplacementChance;
		if (random.nextFloat() >= chance) return original;

		EntityType<?> variant = pickVariant(supported, biome, random);
		if (variant != supported && !isEnabled(variant)) return original;
		return variant == supported ? original : variant;
	}

	@Unique
	private static EntityType<?> normalizeSupportedTrialMob(EntityType<?> type) {
		if (type == EntityType.ZOMBIE) return EntityType.ZOMBIE;
		if (type == EntityType.SKELETON) return EntityType.SKELETON;
		if (type == EntityType.SPIDER || type == EntityType.CAVE_SPIDER) return EntityType.SPIDER;
		return null;
	}

	@Unique
	private static EntityType<?> getConfiguredVanilla(TrialSpawnerConfig config) {
		List<WeightedEntry.Wrapper<SpawnData>> potentials = config.spawnPotentialsDefinition().unwrap();
		if (potentials.size() != 1) return null;

		return parseSupportedVanillaId(potentials.get(0).data().entityToSpawn().getString("id"));
	}

	@Unique
	private static EntityType<?> parseSupportedVanillaId(String id) {
		return switch (id) {
			case "minecraft:zombie" -> EntityType.ZOMBIE;
			case "minecraft:skeleton" -> EntityType.SKELETON;
			case "minecraft:spider", "minecraft:cave_spider" -> EntityType.SPIDER;
			default -> null;
		};
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
	private static boolean isEnabled(EntityType<?> type) {
		String id = EntityType.getKey(type).getPath();
		return ModConfig.get().isMobEnabled(id);
	}

	@Unique
	private static EntityType<?> pickVariant(EntityType<?> vanilla, Holder<Biome> biome, RandomSource random) {
		if (vanilla == EntityType.ZOMBIE) return pickZombie(biome, random);
		if (vanilla == EntityType.SKELETON) return pickSkeleton(biome, random);
		if (vanilla == EntityType.SPIDER) return pickSpider(biome, random);
		return vanilla;
	}

	@Unique
	private static EntityType<?> pickZombie(Holder<Biome> biome, RandomSource random) {
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
