package spookipup.uniquemobs.registry;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Blaze;
import spookipup.uniquemobs.config.ModConfig;
import spookipup.uniquemobs.entity.variant.creeper.SculkCreeperEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import spookipup.uniquemobs.entity.variant.spider.JumpingSpiderEntity;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;


public class ModSpawns {

	public static void init() {
		registerPlacements();
		registerBiomeSpawns();
	}

	private static void registerPlacements() {
		monsterPlacement(ModEntities.SPRINTER_ZOMBIE);
		monsterPlacement(ModEntities.VENOMOUS_ZOMBIE);
		monsterPlacement(ModEntities.INFERNAL_ZOMBIE);
		monsterPlacement(ModEntities.FROZEN_ZOMBIE);
		endMonsterPlacement(ModEntities.ENDER_ZOMBIE);
		// wither mobs spawn more often below Y 0 (deepslate levels)
		depthBoostedPlacement(ModEntities.WITHER_ZOMBIE);
		monsterPlacement(ModEntities.BUILDER_ZOMBIE);
		monsterPlacement(ModEntities.PLAGUE_ZOMBIE);
		monsterPlacement(ModEntities.ARMORED_ZOMBIE);

		monsterPlacement(ModEntities.SPITTING_SPIDER);
		depthBoostedPlacement(ModEntities.WITHER_SPIDER);
		monsterPlacement(ModEntities.MAGMA_SPIDER);
		monsterPlacement(ModEntities.ICE_SPIDER);
		monsterPlacement(ModEntities.WEB_SPINNER_SPIDER);
		// ON_GROUND rejects leaves via isValidSpawn, so use NO_RESTRICTIONS and validate manually
		SpawnPlacements.register(ModEntities.JUMPING_SPIDER,
			SpawnPlacementTypes.NO_RESTRICTIONS,
			Heightmap.Types.MOTION_BLOCKING,
			JumpingSpiderEntity::checkJumpingSpiderSpawnRules);

		monsterPlacement(ModEntities.SNIPER_SKELETON);
		monsterPlacement(ModEntities.EMBER_SKELETON);
		monsterPlacement(ModEntities.POISON_SKELETON);
		endMonsterPlacement(ModEntities.ENDER_SKELETON);
		monsterPlacement(ModEntities.MULTISHOT_SKELETON);

		monsterPlacement(ModEntities.TOXIC_CREEPER);
		depthBoostedPlacement(ModEntities.WITHER_CREEPER);
		// tolerates sculk glow (light 1-6) that would fail normal monster rules
		SpawnPlacements.register(ModEntities.SCULK_CREEPER,
			SpawnPlacementTypes.ON_GROUND,
			Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
			SculkCreeperEntity::checkSculkCreeperSpawnRules);
		monsterPlacement(ModEntities.MAGMA_CREEPER);
		monsterPlacement(ModEntities.FROST_CREEPER);
		endMonsterPlacement(ModEntities.ENDER_CREEPER);
		monsterPlacement(ModEntities.LIGHTNING_CREEPER);
		monsterPlacement(ModEntities.BURROWING_CREEPER);

		endMonsterPlacement(ModEntities.ASSASSIN_ENDERMAN);
		endMonsterPlacement(ModEntities.ENRAGED_ENDERMAN);

		blazePlacement(ModEntities.BLAST_BLAZE);
		blazePlacement(ModEntities.STORM_BLAZE);
		blazePlacement(ModEntities.WITHER_BLAZE);
		blazePlacement(ModEntities.SOUL_BLAZE);
		structureBlazePlacement(ModEntities.BRAND_BLAZE);

		// ghasts - flying nether mobs, delegate to vanilla ghast spawn rules
		SpawnPlacements.register(ModEntities.GREAT_MOTHER_GHAST,
			SpawnPlacementTypes.NO_RESTRICTIONS,
			Heightmap.Types.MOTION_BLOCKING,
			ModSpawns::checkFullGhastSpawnRules);
		SpawnPlacements.register(ModEntities.DELTA_GHAST,
			SpawnPlacementTypes.NO_RESTRICTIONS,
			Heightmap.Types.MOTION_BLOCKING,
			ModSpawns::checkFullGhastSpawnRules);
		SpawnPlacements.register(ModEntities.WITHER_GHAST,
			SpawnPlacementTypes.NO_RESTRICTIONS,
			Heightmap.Types.MOTION_BLOCKING,
			ModSpawns::checkFullGhastSpawnRules);
		lingGhastPlacement(ModEntities.RAGELING);
		lingGhastPlacement(ModEntities.SKITTERLING);
		lingGhastPlacement(ModEntities.OBSIDLING);
		lingGhastPlacement(ModEntities.BLIGHTLING);
	}

	private static <T extends Monster> void monsterPlacement(EntityType<T> type) {
		SpawnPlacements.register(type,
			SpawnPlacementTypes.ON_GROUND,
			Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
			Monster::checkMonsterSpawnRules);
	}

	// uses normal overworld rules + skips light in The End
	private static <T extends Monster> void endMonsterPlacement(EntityType<T> type) {
		SpawnPlacements.register(type,
			SpawnPlacementTypes.ON_GROUND,
			Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
			(entityType, level, spawnReason, pos, random) ->
				level.getDifficulty() != Difficulty.PEACEFUL
					&& (EntitySpawnReason.ignoresLightRequirements(spawnReason)
						|| level.dimensionType().hasEnderDragonFight()
						|| Monster.isDarkEnoughToSpawn(level, pos, random))
					&& Mob.checkMobSpawnRules(entityType, level, spawnReason, pos, random));
	}

	// always spawns below Y 0, only 1 in 3 attempts above - makes them more common at depth
	private static <T extends Monster> void depthBoostedPlacement(EntityType<T> type) {
		SpawnPlacements.register(type,
			SpawnPlacementTypes.ON_GROUND,
			Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
			(entityType, level, spawnReason, pos, random) ->
				Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random)
					&& (pos.getY() < 0 || random.nextInt(3) == 0));
	}

	private static <T extends Blaze> void structureBlazePlacement(EntityType<T> type) {
		SpawnPlacements.register(type,
			SpawnPlacementTypes.NO_RESTRICTIONS,
			Heightmap.Types.MOTION_BLOCKING,
			ModSpawns::checkStructureBlazeSpawnRules);
	}

	private static <T extends Blaze> void blazePlacement(EntityType<T> type) {
		SpawnPlacements.register(type,
			SpawnPlacementTypes.ON_GROUND,
			Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
			Monster::checkMonsterSpawnRules);
	}

	@SuppressWarnings("unchecked")
	private static boolean checkFullGhastSpawnRules(EntityType<? extends Ghast> entityType,
													ServerLevelAccessor level,
													EntitySpawnReason spawnReason,
													BlockPos pos,
													RandomSource random) {
		return Ghast.checkGhastSpawnRules((EntityType<Ghast>) entityType, level, spawnReason, pos, random);
	}

	// smaller ghasts can spawn in tighter air pockets as long as they have a nearby floor or lava shelf below
	private static <T extends Ghast> void lingGhastPlacement(EntityType<T> type) {
		SpawnPlacements.register(type,
			SpawnPlacementTypes.NO_RESTRICTIONS,
			Heightmap.Types.MOTION_BLOCKING,
			ModSpawns::checkLingGhastSpawnRules);
	}

	private static boolean checkLingGhastSpawnRules(EntityType<? extends Ghast> entityType,
													ServerLevelAccessor level,
													EntitySpawnReason spawnReason,
													BlockPos pos,
													RandomSource random) {
		if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
		if (!Mob.checkMobSpawnRules(entityType, level, spawnReason, pos, random)) return false;
		if (!hasLingClearance(level, pos)) return false;
		return hasNearbyFloor(level, pos, 4);
	}

	private static boolean hasLingClearance(ServerLevelAccessor level, BlockPos pos) {
		for (int y = 0; y <= 1; y++) {
			BlockPos check = pos.above(y);
			if (level.getBlockState(check).isCollisionShapeFullBlock(level, check)) return false;
			if (!level.getFluidState(check).isEmpty()) return false;
		}
		return true;
	}

	private static boolean hasNearbyFloor(ServerLevelAccessor level, BlockPos pos, int maxBelow) {
		for (int depth = 1; depth <= maxBelow; depth++) {
			BlockPos check = pos.below(depth);
			if (level.getBlockState(check).isCollisionShapeFullBlock(level, check) || !level.getFluidState(check).isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private static boolean checkStructureBlazeSpawnRules(EntityType<? extends Blaze> entityType,
														 ServerLevelAccessor level,
														 EntitySpawnReason spawnReason,
														 BlockPos pos,
														 RandomSource random) {
		if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
		if (!Mob.checkMobSpawnRules(entityType, level, spawnReason, pos, random)) return false;
		if (!hasLingClearance(level, pos)) return false;
		if (!hasNearbyFloor(level, pos, 4)) return false;
		return isNearStructure(level, pos, BuiltinStructures.FORTRESS, 20)
			|| isNearStructure(level, pos, BuiltinStructures.BASTION_REMNANT, 20);
	}

	private static boolean isNearStructure(ServerLevelAccessor level, BlockPos pos,
										   ResourceKey<Structure> structureKey,
										   int radius) {
		Holder<Structure> holder = level.registryAccess()
			.lookupOrThrow(Registries.STRUCTURE)
			.getOrThrow(structureKey);
		Structure structure = holder.value();

		for (int x = -radius; x <= radius; x += 8) {
			for (int z = -radius; z <= radius; z += 8) {
				BlockPos sample = pos.offset(x, 0, z);
				StructureStart start = level.getLevel().structureManager().getStructureWithPieceAt(sample, structure);
				if (start != StructureStart.INVALID_START) {
					return true;
				}
			}
		}
		return false;
	}

	private static void registerBiomeSpawns() {
		ModConfig cfg = ModConfig.get();

		// zombies
		spawn("sprinter_zombie",  cfg, ModEntities.SPRINTER_ZOMBIE,  80, 1, 2, ModSpawns::overworld);
		spawn("armored_zombie",   cfg, ModEntities.ARMORED_ZOMBIE,   70, 1, 1, ModSpawns::overworld);
		spawn("venomous_zombie",  cfg, ModEntities.VENOMOUS_ZOMBIE,  60, 1, 1, ModSpawns::overworld);
		spawn("venomous_zombie",  cfg, ModEntities.VENOMOUS_ZOMBIE,  40, 1, 1, ModSpawns::jungle);
		spawn("venomous_zombie",  cfg, ModEntities.VENOMOUS_ZOMBIE,  45, 1, 1, ModSpawns::swamp);
		spawn("venomous_zombie",  cfg, ModEntities.VENOMOUS_ZOMBIE,  30, 1, 1, ModSpawns::caves);
		spawn("plague_zombie",    cfg, ModEntities.PLAGUE_ZOMBIE,    50, 1, 1, ModSpawns::overworld);
		spawn("plague_zombie",    cfg, ModEntities.PLAGUE_ZOMBIE,    35, 1, 1, ModSpawns::jungle);
		spawn("plague_zombie",    cfg, ModEntities.PLAGUE_ZOMBIE,    50, 1, 2, ModSpawns::swamp);
		spawn("plague_zombie",    cfg, ModEntities.PLAGUE_ZOMBIE,    25, 1, 1, ModSpawns::caves);
		spawn("builder_zombie",   cfg, ModEntities.BUILDER_ZOMBIE,   30, 1, 1, ModSpawns::overworld);
		spawn("frozen_zombie",    cfg, ModEntities.FROZEN_ZOMBIE,    80, 1, 2, ModSpawns::cold);
		spawn("infernal_zombie",  cfg, ModEntities.INFERNAL_ZOMBIE,  80, 1, 2, ModSpawns::hotOverworld);
		spawn("infernal_zombie",  cfg, ModEntities.INFERNAL_ZOMBIE,  6,  1, 1, ModSpawns::nether);

		spawn("wither_zombie",    cfg, ModEntities.WITHER_ZOMBIE,    25, 1, 1, ModSpawns::overworld);
		spawn("wither_zombie",    cfg, ModEntities.WITHER_ZOMBIE,    14, 1, 1, ModSpawns::nether);

		spawn("ender_zombie",     cfg, ModEntities.ENDER_ZOMBIE,     15, 1, 1, ModSpawns::overworld);
		spawn("ender_zombie",     cfg, ModEntities.ENDER_ZOMBIE,     50, 1, 2, ModSpawns::end);

		// spiders
		spawn("spitting_spider",     cfg, ModEntities.SPITTING_SPIDER,     60, 1, 2, ModSpawns::overworld);
		spawn("spitting_spider",     cfg, ModEntities.SPITTING_SPIDER,     40, 1, 1, ModSpawns::jungle);
		spawn("spitting_spider",     cfg, ModEntities.SPITTING_SPIDER,     45, 1, 1, ModSpawns::swamp);
		spawn("spitting_spider",     cfg, ModEntities.SPITTING_SPIDER,     25, 1, 1, ModSpawns::caves);
		spawn("web_spinner_spider",  cfg, ModEntities.WEB_SPINNER_SPIDER,  60, 1, 1, ModSpawns::overworld);
		spawn("web_spinner_spider",  cfg, ModEntities.WEB_SPINNER_SPIDER,  40, 1, 1, ModSpawns::swamp);
		spawn("web_spinner_spider",  cfg, ModEntities.WEB_SPINNER_SPIDER,  40, 1, 1, ModSpawns::caves);
		spawn("ice_spider",          cfg, ModEntities.ICE_SPIDER,          70, 1, 2, ModSpawns::cold);
		spawn("magma_spider",        cfg, ModEntities.MAGMA_SPIDER,        60, 1, 1, ModSpawns::hotOverworld);
		spawn("magma_spider",        cfg, ModEntities.MAGMA_SPIDER,        6,  1, 1, ModSpawns::nether);

		spawn("jumping_spider",      cfg, ModEntities.JUMPING_SPIDER,     150, 2, 4, ModSpawns::jungle);
		spawn("jumping_spider",      cfg, ModEntities.JUMPING_SPIDER,      70, 1, 2, ModSpawns::forest);

		spawn("wither_spider",       cfg, ModEntities.WITHER_SPIDER,       40, 1, 1, ModSpawns::overworld);
		spawn("wither_spider",       cfg, ModEntities.WITHER_SPIDER,       14, 1, 1, ModSpawns::nether);

		// skeletons
		spawn("multishot_skeleton",  cfg, ModEntities.MULTISHOT_SKELETON,  70, 1, 1, ModSpawns::overworld);
		spawn("sniper_skeleton",     cfg, ModEntities.SNIPER_SKELETON,     30, 1, 1, ModSpawns::overworld);
		spawn("poison_skeleton",     cfg, ModEntities.POISON_SKELETON,     40, 1, 1, ModSpawns::overworld);
		spawn("poison_skeleton",     cfg, ModEntities.POISON_SKELETON,     50, 1, 1, ModSpawns::jungle);
		spawn("poison_skeleton",     cfg, ModEntities.POISON_SKELETON,     40, 1, 1, ModSpawns::forest);
		spawn("poison_skeleton",     cfg, ModEntities.POISON_SKELETON,     55, 1, 1, ModSpawns::swamp);
		spawn("ember_skeleton",      cfg, ModEntities.EMBER_SKELETON,      60, 1, 1, ModSpawns::hotOverworld);
		spawn("ember_skeleton",      cfg, ModEntities.EMBER_SKELETON,      8,  1, 1, ModSpawns::nether);

		spawn("ender_skeleton",      cfg, ModEntities.ENDER_SKELETON,      15, 1, 1, ModSpawns::overworld);
		spawn("ender_skeleton",      cfg, ModEntities.ENDER_SKELETON,      40, 1, 2, ModSpawns::end);

		// creepers
		spawn("lightning_creeper",   cfg, ModEntities.LIGHTNING_CREEPER,   60, 1, 1, ModSpawns::overworld);
		spawn("burrowing_creeper",   cfg, ModEntities.BURROWING_CREEPER,   55, 1, 1, ModSpawns::overworld);
		spawn("toxic_creeper",       cfg, ModEntities.TOXIC_CREEPER,       50, 1, 1, ModSpawns::overworld);
		spawn("toxic_creeper",       cfg, ModEntities.TOXIC_CREEPER,       35, 1, 1, ModSpawns::jungle);
		spawn("toxic_creeper",       cfg, ModEntities.TOXIC_CREEPER,       50, 1, 1, ModSpawns::swamp);
		spawn("toxic_creeper",       cfg, ModEntities.TOXIC_CREEPER,       30, 1, 1, ModSpawns::caves);
		spawn("frost_creeper",       cfg, ModEntities.FROST_CREEPER,       30, 1, 1, ModSpawns::cold);
		spawn("magma_creeper",       cfg, ModEntities.MAGMA_CREEPER,       40, 1, 1, ModSpawns::hotOverworld);
		spawn("magma_creeper",       cfg, ModEntities.MAGMA_CREEPER,       5,  1, 1, ModSpawns::nether);

		spawn("wither_creeper",      cfg, ModEntities.WITHER_CREEPER,      35, 1, 1, ModSpawns::overworld);
		spawn("wither_creeper",      cfg, ModEntities.WITHER_CREEPER,      12, 1, 1, ModSpawns::nether);

		spawn("ender_creeper",       cfg, ModEntities.ENDER_CREEPER,       15, 1, 1, ModSpawns::overworld);
		spawn("ender_creeper",       cfg, ModEntities.ENDER_CREEPER,       50, 1, 2, ModSpawns::end);

		// structure override handles ancient city; this covers the rest of the deep dark
		spawn("sculk_creeper",       cfg, ModEntities.SCULK_CREEPER,       10, 1, 1, ModSpawns::deepDark);

		// endermen
		spawn("assassin_enderman",   cfg, ModEntities.ASSASSIN_ENDERMAN,   10, 1, 1, ModSpawns::overworld);
		spawn("enraged_enderman",    cfg, ModEntities.ENRAGED_ENDERMAN,    10, 1, 1, ModSpawns::overworld);
		spawn("assassin_enderman",   cfg, ModEntities.ASSASSIN_ENDERMAN,   40, 1, 2, ModSpawns::end);
		spawn("enraged_enderman",    cfg, ModEntities.ENRAGED_ENDERMAN,    40, 1, 2, ModSpawns::end);

		// ghasts
		spawn("great_mother_ghast",  cfg, ModEntities.GREAT_MOTHER_GHAST, 4, 1, 1, ModSpawns::nether);
		spawn("great_mother_ghast",  cfg, ModEntities.GREAT_MOTHER_GHAST, 6, 1, 1, ModSpawns::netherWastes);
		spawn("great_mother_ghast",  cfg, ModEntities.GREAT_MOTHER_GHAST, 5, 1, 1, ModSpawns::crimsonForest);

		spawn("delta_ghast",         cfg, ModEntities.DELTA_GHAST,         4, 1, 1, ModSpawns::nether);
		spawn("delta_ghast",         cfg, ModEntities.DELTA_GHAST,         12, 1, 1, ModSpawns::basaltDeltas);

		spawn("wither_ghast",        cfg, ModEntities.WITHER_GHAST,        4,  1, 1, ModSpawns::nether);
		spawn("wither_ghast",        cfg, ModEntities.WITHER_GHAST,        12, 1, 1, ModSpawns::soulValley);
		spawn("rageling",            cfg, ModEntities.RAGELING,            1,  1, 2, ModSpawns::nether);
		spawn("rageling",            cfg, ModEntities.RAGELING,            3,  1, 3, ModSpawns::netherWastes);
		spawn("rageling",            cfg, ModEntities.RAGELING,            2,  1, 2, ModSpawns::crimsonForest);

		spawn("skitterling",         cfg, ModEntities.SKITTERLING,         2,  1, 1, ModSpawns::nether);
		spawn("skitterling",         cfg, ModEntities.SKITTERLING,         6,  1, 2, ModSpawns::soulValley);

		spawn("obsidling",           cfg, ModEntities.OBSIDLING,           2,  1, 1, ModSpawns::nether);
		spawn("obsidling",           cfg, ModEntities.OBSIDLING,           5,  1, 2, ModSpawns::basaltDeltas);

		spawn("blightling",          cfg, ModEntities.BLIGHTLING,          2,  1, 1, ModSpawns::nether);
		spawn("blightling",          cfg, ModEntities.BLIGHTLING,          5,  1, 2, ModSpawns::fungalForests);
	}

	@FunctionalInterface
	private interface BiomeSpawnRegistrar {
		void register(EntityType<?> type, int weight, int min, int max);
	}

	private static void spawn(String id, ModConfig cfg, EntityType<?> type,
							  int baseWeight, int min, int max, BiomeSpawnRegistrar registrar) {
		if (!cfg.isMobEnabled(id)) return;
		int weight = cfg.adjustWeight(id, baseWeight);
		if (weight <= 0) return;
		registrar.register(type, weight, min, max);
	}

	private static void overworld(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.foundInOverworld(),
			MobCategory.MONSTER, type, weight, min, max);
	}

	private static void nether(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.foundInTheNether(),
			MobCategory.MONSTER, type, weight, min, max);
	}

	private static void netherWastes(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.includeByKey(Biomes.NETHER_WASTES),
			MobCategory.MONSTER, type, weight, min, max);
	}

	private static void soulValley(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY),
			MobCategory.MONSTER, type, weight, min, max);
	}

	private static void basaltDeltas(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.includeByKey(Biomes.BASALT_DELTAS),
			MobCategory.MONSTER, type, weight, min, max);
	}

	private static void crimsonForest(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.includeByKey(Biomes.CRIMSON_FOREST),
			MobCategory.MONSTER, type, weight, min, max);
	}

	private static void fungalForests(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.includeByKey(Biomes.CRIMSON_FOREST, Biomes.WARPED_FOREST),
			MobCategory.MONSTER, type, weight, min, max);
	}

	private static void end(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.foundInTheEnd(),
			MobCategory.MONSTER, type, weight, min, max);
	}

	private static void cold(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.tag(BiomeTags.IS_OVERWORLD).and(BiomeSelectors.tag(BiomeTags.SPAWNS_SNOW_FOXES)),
			MobCategory.MONSTER, type, weight, min, max);
	}

	private static void jungle(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.tag(BiomeTags.IS_JUNGLE),
			MobCategory.MONSTER, type, weight, min, max);
	}

	private static void forest(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.tag(BiomeTags.IS_FOREST),
			MobCategory.MONSTER, type, weight, min, max);
	}

	private static void swamp(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.includeByKey(Biomes.SWAMP, Biomes.MANGROVE_SWAMP),
			MobCategory.MONSTER, type, weight, min, max);
	}

	private static void caves(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.includeByKey(Biomes.LUSH_CAVES, Biomes.DRIPSTONE_CAVES),
			MobCategory.MONSTER, type, weight, min, max);
	}

	private static void deepDark(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.includeByKey(Biomes.DEEP_DARK),
			MobCategory.MONSTER, type, weight, min, max);
	}

	private static void hotOverworld(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.tag(BiomeTags.IS_OVERWORLD).and(BiomeSelectors.tag(BiomeTags.HAS_DESERT_PYRAMID)
				.or(BiomeSelectors.tag(BiomeTags.IS_BADLANDS))),
			MobCategory.MONSTER, type, weight, min, max);
	}
}
