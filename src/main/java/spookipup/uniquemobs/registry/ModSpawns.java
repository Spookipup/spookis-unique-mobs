package spookipup.uniquemobs.registry;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import spookipup.uniquemobs.entity.variant.creeper.SculkCreeperEntity;
import spookipup.uniquemobs.entity.variant.spider.JumpingSpiderEntity;
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
				level.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
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

	private static void registerBiomeSpawns() {
		// zombies
		overworld(ModEntities.SPRINTER_ZOMBIE,  80, 1, 2);
		overworld(ModEntities.ARMORED_ZOMBIE,   70, 1, 1);
		overworld(ModEntities.VENOMOUS_ZOMBIE,  60, 1, 1);
		jungle(ModEntities.VENOMOUS_ZOMBIE,     40, 1, 1);
		swamp(ModEntities.VENOMOUS_ZOMBIE,      45, 1, 1);
		caves(ModEntities.VENOMOUS_ZOMBIE,      30, 1, 1);
		overworld(ModEntities.PLAGUE_ZOMBIE,    50, 1, 1);
		jungle(ModEntities.PLAGUE_ZOMBIE,       35, 1, 1);
		swamp(ModEntities.PLAGUE_ZOMBIE,        50, 1, 2);
		caves(ModEntities.PLAGUE_ZOMBIE,        25, 1, 1);
		overworld(ModEntities.BUILDER_ZOMBIE,   30, 1, 1);
		cold(ModEntities.FROZEN_ZOMBIE,         80, 1, 2);
		hot(ModEntities.INFERNAL_ZOMBIE,        80, 1, 2);

		overworld(ModEntities.WITHER_ZOMBIE,    25, 1, 1);
		nether(ModEntities.WITHER_ZOMBIE,       40, 1, 1);

		overworld(ModEntities.ENDER_ZOMBIE,     15, 1, 1);
		end(ModEntities.ENDER_ZOMBIE,           50, 1, 2);

		// spiders
		overworld(ModEntities.SPITTING_SPIDER,     60, 1, 2);
		jungle(ModEntities.SPITTING_SPIDER,        40, 1, 1);
		swamp(ModEntities.SPITTING_SPIDER,         45, 1, 1);
		caves(ModEntities.SPITTING_SPIDER,         25, 1, 1);
		overworld(ModEntities.WEB_SPINNER_SPIDER,  60, 1, 1);
		swamp(ModEntities.WEB_SPINNER_SPIDER,      40, 1, 1);
		caves(ModEntities.WEB_SPINNER_SPIDER,      40, 1, 1);
		cold(ModEntities.ICE_SPIDER,               70, 1, 2);
		hot(ModEntities.MAGMA_SPIDER,              60, 1, 1);

		jungle(ModEntities.JUMPING_SPIDER,        150, 2, 4);
		forest(ModEntities.JUMPING_SPIDER,         70, 1, 2);

		overworld(ModEntities.WITHER_SPIDER,       40, 1, 1);
		nether(ModEntities.WITHER_SPIDER,          50, 1, 1);

		// skeletons
		overworld(ModEntities.MULTISHOT_SKELETON,  70, 1, 1);
		overworld(ModEntities.SNIPER_SKELETON,     30, 1, 1);
		overworld(ModEntities.POISON_SKELETON,     40, 1, 1);
		jungle(ModEntities.POISON_SKELETON,        50, 1, 1);
		forest(ModEntities.POISON_SKELETON,        40, 1, 1);
		swamp(ModEntities.POISON_SKELETON,         55, 1, 1);
		hot(ModEntities.EMBER_SKELETON,            60, 1, 1);

		overworld(ModEntities.ENDER_SKELETON,      15, 1, 1);
		end(ModEntities.ENDER_SKELETON,            40, 1, 2);

		// creepers
		overworld(ModEntities.LIGHTNING_CREEPER,   60, 1, 1);
		overworld(ModEntities.BURROWING_CREEPER,   55, 1, 1);
		overworld(ModEntities.TOXIC_CREEPER,       50, 1, 1);
		jungle(ModEntities.TOXIC_CREEPER,          35, 1, 1);
		swamp(ModEntities.TOXIC_CREEPER,           50, 1, 1);
		caves(ModEntities.TOXIC_CREEPER,           30, 1, 1);
		cold(ModEntities.FROST_CREEPER,            30, 1, 1);
		hot(ModEntities.MAGMA_CREEPER,             40, 1, 1);

		overworld(ModEntities.WITHER_CREEPER,      35, 1, 1);
		nether(ModEntities.WITHER_CREEPER,         40, 1, 1);

		overworld(ModEntities.ENDER_CREEPER,       15, 1, 1);
		end(ModEntities.ENDER_CREEPER,             50, 1, 2);

		// structure override handles ancient city; this covers the rest of the deep dark
		deepDark(ModEntities.SCULK_CREEPER,        10, 1, 1);

		// endermen
		overworld(ModEntities.ASSASSIN_ENDERMAN,   10, 1, 1);
		overworld(ModEntities.ENRAGED_ENDERMAN,    10, 1, 1);
		end(ModEntities.ASSASSIN_ENDERMAN,         40, 1, 2);
		end(ModEntities.ENRAGED_ENDERMAN,          40, 1, 2);
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

	private static void hot(EntityType<?> type, int weight, int min, int max) {
		BiomeModifications.addSpawn(
			BiomeSelectors.tag(BiomeTags.IS_OVERWORLD).and(BiomeSelectors.tag(BiomeTags.HAS_DESERT_PYRAMID)
				.or(BiomeSelectors.tag(BiomeTags.IS_BADLANDS))),
			MobCategory.MONSTER, type, weight, min, max);
		BiomeModifications.addSpawn(
			BiomeSelectors.foundInTheNether(),
			MobCategory.MONSTER, type, weight, min, max);
	}
}
