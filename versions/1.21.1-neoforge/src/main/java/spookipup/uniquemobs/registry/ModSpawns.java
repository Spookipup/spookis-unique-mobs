package spookipup.uniquemobs.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import spookipup.uniquemobs.entity.variant.creeper.SculkCreeperEntity;
import spookipup.uniquemobs.entity.variant.spider.JumpingSpiderEntity;
import spookipup.uniquemobs.config.ModConfig;
import spookipup.uniquemobs.UniqueMobs;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;


public class ModSpawns {

	private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
		DeferredRegister.create(NeoForgeRegistries.BIOME_MODIFIER_SERIALIZERS, UniqueMobs.MOD_ID);

	private static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<? extends BiomeModifier>> SPAWNS_BIOME_MODIFIER =
		BIOME_MODIFIER_SERIALIZERS.register("spawns", () -> UniqueMobsSpawnBiomeModifier.CODEC);

	public static void register(IEventBus bus) {
		bus.addListener(ModSpawns::registerPlacements);
		BIOME_MODIFIER_SERIALIZERS.register(bus);
	}

	private static void registerPlacements(RegisterSpawnPlacementsEvent event) {
		monsterPlacement(event, ModEntities.SPRINTER_ZOMBIE.get());
		monsterPlacement(event, ModEntities.VENOMOUS_ZOMBIE.get());
		monsterPlacement(event, ModEntities.INFERNAL_ZOMBIE.get());
		monsterPlacement(event, ModEntities.FROZEN_ZOMBIE.get());
		endMonsterPlacement(event, ModEntities.ENDER_ZOMBIE.get());
		// wither mobs spawn more often below Y 0 (deepslate levels)
		depthBoostedPlacement(event, ModEntities.WITHER_ZOMBIE.get());
		monsterPlacement(event, ModEntities.BUILDER_ZOMBIE.get());
		monsterPlacement(event, ModEntities.PLAGUE_ZOMBIE.get());
		monsterPlacement(event, ModEntities.ARMORED_ZOMBIE.get());
		monsterPlacement(event, ModEntities.BLOSSOM_ZOMBIE.get());

		monsterPlacement(event, ModEntities.SPITTING_SPIDER.get());
		depthBoostedPlacement(event, ModEntities.WITHER_SPIDER.get());
		monsterPlacement(event, ModEntities.MAGMA_SPIDER.get());
		monsterPlacement(event, ModEntities.ICE_SPIDER.get());
		monsterPlacement(event, ModEntities.WEB_SPINNER_SPIDER.get());
		// ON_GROUND rejects leaves via isValidSpawn, so use NO_RESTRICTIONS and validate manually
		event.register(ModEntities.JUMPING_SPIDER.get(),
			SpawnPlacementTypes.NO_RESTRICTIONS,
			Heightmap.Types.MOTION_BLOCKING,
			JumpingSpiderEntity::checkJumpingSpiderSpawnRules,
			RegisterSpawnPlacementsEvent.Operation.REPLACE);

		monsterPlacement(event, ModEntities.SNIPER_SKELETON.get());
		monsterPlacement(event, ModEntities.EMBER_SKELETON.get());
		monsterPlacement(event, ModEntities.POISON_SKELETON.get());
		endMonsterPlacement(event, ModEntities.ENDER_SKELETON.get());
		monsterPlacement(event, ModEntities.MULTISHOT_SKELETON.get());
		monsterPlacement(event, ModEntities.BLOSSOM_SKELETON.get());

		monsterPlacement(event, ModEntities.TOXIC_CREEPER.get());
		depthBoostedPlacement(event, ModEntities.WITHER_CREEPER.get());
		// tolerates sculk glow (light 1-6) that would fail normal monster rules
		event.register(ModEntities.SCULK_CREEPER.get(),
			SpawnPlacementTypes.ON_GROUND,
			Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
			SculkCreeperEntity::checkSculkCreeperSpawnRules,
			RegisterSpawnPlacementsEvent.Operation.REPLACE);
		monsterPlacement(event, ModEntities.MAGMA_CREEPER.get());
		monsterPlacement(event, ModEntities.FROST_CREEPER.get());
		endMonsterPlacement(event, ModEntities.ENDER_CREEPER.get());
		monsterPlacement(event, ModEntities.LIGHTNING_CREEPER.get());
		monsterPlacement(event, ModEntities.BURROWING_CREEPER.get());
		monsterPlacement(event, ModEntities.BLOSSOM_CREEPER.get());

		endMonsterPlacement(event, ModEntities.ASSASSIN_ENDERMAN.get());
		endMonsterPlacement(event, ModEntities.ENRAGED_ENDERMAN.get());
		monsterPlacement(event, ModEntities.BLOSSOM_ENDERMAN.get());

		blazePlacement(event, ModEntities.BLAST_BLAZE.get());
		blazePlacement(event, ModEntities.STORM_BLAZE.get());
		blazePlacement(event, ModEntities.WITHER_BLAZE.get());
		blazePlacement(event, ModEntities.SOUL_BLAZE.get());
		blazePlacement(event, ModEntities.BRAND_BLAZE.get());

		// ghasts
		event.register(ModEntities.GREAT_MOTHER_GHAST.get(),
			SpawnPlacementTypes.NO_RESTRICTIONS,
			Heightmap.Types.MOTION_BLOCKING,
			ModSpawns::checkFullGhastSpawnRules,
			RegisterSpawnPlacementsEvent.Operation.REPLACE);
		event.register(ModEntities.DELTA_GHAST.get(),
			SpawnPlacementTypes.NO_RESTRICTIONS,
			Heightmap.Types.MOTION_BLOCKING,
			ModSpawns::checkFullGhastSpawnRules,
			RegisterSpawnPlacementsEvent.Operation.REPLACE);
		event.register(ModEntities.WITHER_GHAST.get(),
			SpawnPlacementTypes.NO_RESTRICTIONS,
			Heightmap.Types.MOTION_BLOCKING,
			ModSpawns::checkFullGhastSpawnRules,
			RegisterSpawnPlacementsEvent.Operation.REPLACE);
		lingGhastPlacement(event, ModEntities.RAGELING.get());
		lingGhastPlacement(event, ModEntities.SKITTERLING.get());
		lingGhastPlacement(event, ModEntities.OBSIDLING.get());
		lingGhastPlacement(event, ModEntities.BLIGHTLING.get());
	}

	private static <T extends Monster> void monsterPlacement(RegisterSpawnPlacementsEvent event, EntityType<T> type) {
		event.register(type,
			SpawnPlacementTypes.ON_GROUND,
			Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
			Monster::checkMonsterSpawnRules,
			RegisterSpawnPlacementsEvent.Operation.REPLACE);
	}

	// uses normal overworld rules + skips light in The End
	private static <T extends Monster> void endMonsterPlacement(RegisterSpawnPlacementsEvent event, EntityType<T> type) {
		event.register(type,
			SpawnPlacementTypes.ON_GROUND,
			Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
			(entityType, level, spawnReason, pos, random) ->
				level.getDifficulty() != Difficulty.PEACEFUL
					&& (spawnReason == MobSpawnType.SPAWNER
						|| ((ServerLevel) level).dimension() == Level.END
						|| Monster.isDarkEnoughToSpawn(level, pos, random))
					&& Mob.checkMobSpawnRules(entityType, level, spawnReason, pos, random),
			RegisterSpawnPlacementsEvent.Operation.REPLACE);
	}

	// always spawns below Y 0, only 1 in 8 attempts above - makes them much more common at depth
	private static <T extends Monster> void depthBoostedPlacement(RegisterSpawnPlacementsEvent event, EntityType<T> type) {
		event.register(type,
			SpawnPlacementTypes.ON_GROUND,
			Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
			(entityType, level, spawnReason, pos, random) ->
				Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random)
					&& (pos.getY() < 0 || random.nextInt(8) == 0),
			RegisterSpawnPlacementsEvent.Operation.REPLACE);
	}

	private static <T extends Blaze> void blazePlacement(RegisterSpawnPlacementsEvent event, EntityType<T> type) {
		event.register(type,
			SpawnPlacementTypes.ON_GROUND,
			Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
			Monster::checkMonsterSpawnRules,
			RegisterSpawnPlacementsEvent.Operation.REPLACE);
	}

	@SuppressWarnings("unchecked")
	private static boolean checkFullGhastSpawnRules(EntityType<? extends Ghast> entityType,
													ServerLevelAccessor level,
													MobSpawnType spawnReason,
													BlockPos pos,
													RandomSource random) {
		return Ghast.checkGhastSpawnRules((EntityType<Ghast>) entityType, level, spawnReason, pos, random);
	}

	private static <T extends Ghast> void lingGhastPlacement(RegisterSpawnPlacementsEvent event, EntityType<T> type) {
		event.register(type,
			SpawnPlacementTypes.NO_RESTRICTIONS,
			Heightmap.Types.MOTION_BLOCKING,
			ModSpawns::checkLingGhastSpawnRules,
			RegisterSpawnPlacementsEvent.Operation.REPLACE);
	}

	private static boolean checkLingGhastSpawnRules(EntityType<? extends Ghast> entityType,
													ServerLevelAccessor level,
													MobSpawnType spawnReason,
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

	private static boolean isNearStructure(ServerLevelAccessor level, BlockPos pos,
										   ResourceKey<Structure> structureKey,
										   int radius) {
		Holder<Structure> holder = level.registryAccess()
			.registryOrThrow(Registries.STRUCTURE)
			.getHolderOrThrow(structureKey);
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

	private static void registerBiomeSpawns(Holder<Biome> biome, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		ModConfig cfg = ModConfig.get();

		// zombies
		spawn(biome, builder, "sprinter_zombie", cfg, ModEntities.SPRINTER_ZOMBIE.get(), 80, 1, 2, ModSpawns::overworld);
		spawn(biome, builder, "armored_zombie", cfg, ModEntities.ARMORED_ZOMBIE.get(), 70, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "venomous_zombie", cfg, ModEntities.VENOMOUS_ZOMBIE.get(), 60, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "venomous_zombie", cfg, ModEntities.VENOMOUS_ZOMBIE.get(), 40, 1, 1, ModSpawns::jungle);
		spawn(biome, builder, "venomous_zombie", cfg, ModEntities.VENOMOUS_ZOMBIE.get(), 45, 1, 1, ModSpawns::swamp);
		spawn(biome, builder, "venomous_zombie", cfg, ModEntities.VENOMOUS_ZOMBIE.get(), 30, 1, 1, ModSpawns::caves);
		spawn(biome, builder, "plague_zombie", cfg, ModEntities.PLAGUE_ZOMBIE.get(), 50, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "plague_zombie", cfg, ModEntities.PLAGUE_ZOMBIE.get(), 35, 1, 1, ModSpawns::jungle);
		spawn(biome, builder, "plague_zombie", cfg, ModEntities.PLAGUE_ZOMBIE.get(), 50, 1, 2, ModSpawns::swamp);
		spawn(biome, builder, "plague_zombie", cfg, ModEntities.PLAGUE_ZOMBIE.get(), 25, 1, 1, ModSpawns::caves);
		spawn(biome, builder, "builder_zombie", cfg, ModEntities.BUILDER_ZOMBIE.get(), 30, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "frozen_zombie", cfg, ModEntities.FROZEN_ZOMBIE.get(), 80, 1, 2, ModSpawns::cold);
		spawn(biome, builder, "infernal_zombie", cfg, ModEntities.INFERNAL_ZOMBIE.get(), 80, 1, 2, ModSpawns::hotOverworld);
		spawn(biome, builder, "infernal_zombie", cfg, ModEntities.INFERNAL_ZOMBIE.get(), 6, 1, 1, ModSpawns::nether);

		spawn(biome, builder, "wither_zombie", cfg, ModEntities.WITHER_ZOMBIE.get(), 12, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "wither_zombie", cfg, ModEntities.WITHER_ZOMBIE.get(), 8, 1, 1, ModSpawns::nether);

		spawn(biome, builder, "ender_zombie", cfg, ModEntities.ENDER_ZOMBIE.get(), 15, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "ender_zombie", cfg, ModEntities.ENDER_ZOMBIE.get(), 50, 1, 2, ModSpawns::end);
		spawn(biome, builder, "blossom_zombie", cfg, ModEntities.BLOSSOM_ZOMBIE.get(), 90, 1, 2, ModSpawns::cherry);
		spawn(biome, builder, "blossom_zombie", cfg, ModEntities.BLOSSOM_ZOMBIE.get(), 20, 1, 1, ModSpawns::forest);

		// spiders
		spawn(biome, builder, "spitting_spider", cfg, ModEntities.SPITTING_SPIDER.get(), 60, 1, 2, ModSpawns::overworld);
		spawn(biome, builder, "spitting_spider", cfg, ModEntities.SPITTING_SPIDER.get(), 40, 1, 1, ModSpawns::jungle);
		spawn(biome, builder, "spitting_spider", cfg, ModEntities.SPITTING_SPIDER.get(), 45, 1, 1, ModSpawns::swamp);
		spawn(biome, builder, "spitting_spider", cfg, ModEntities.SPITTING_SPIDER.get(), 25, 1, 1, ModSpawns::caves);
		spawn(biome, builder, "web_spinner_spider", cfg, ModEntities.WEB_SPINNER_SPIDER.get(), 60, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "web_spinner_spider", cfg, ModEntities.WEB_SPINNER_SPIDER.get(), 40, 1, 1, ModSpawns::swamp);
		spawn(biome, builder, "web_spinner_spider", cfg, ModEntities.WEB_SPINNER_SPIDER.get(), 40, 1, 1, ModSpawns::caves);
		spawn(biome, builder, "ice_spider", cfg, ModEntities.ICE_SPIDER.get(), 70, 1, 2, ModSpawns::cold);
		spawn(biome, builder, "magma_spider", cfg, ModEntities.MAGMA_SPIDER.get(), 60, 1, 1, ModSpawns::hotOverworld);
		spawn(biome, builder, "magma_spider", cfg, ModEntities.MAGMA_SPIDER.get(), 6, 1, 1, ModSpawns::nether);

		spawn(biome, builder, "jumping_spider", cfg, ModEntities.JUMPING_SPIDER.get(), 150, 2, 4, ModSpawns::jungle);
		spawn(biome, builder, "jumping_spider", cfg, ModEntities.JUMPING_SPIDER.get(), 70, 1, 2, ModSpawns::forest);

		spawn(biome, builder, "wither_spider", cfg, ModEntities.WITHER_SPIDER.get(), 20, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "wither_spider", cfg, ModEntities.WITHER_SPIDER.get(), 8, 1, 1, ModSpawns::nether);

		// skeletons
		spawn(biome, builder, "multishot_skeleton", cfg, ModEntities.MULTISHOT_SKELETON.get(), 70, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "sniper_skeleton", cfg, ModEntities.SNIPER_SKELETON.get(), 30, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "poison_skeleton", cfg, ModEntities.POISON_SKELETON.get(), 40, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "poison_skeleton", cfg, ModEntities.POISON_SKELETON.get(), 50, 1, 1, ModSpawns::jungle);
		spawn(biome, builder, "poison_skeleton", cfg, ModEntities.POISON_SKELETON.get(), 40, 1, 1, ModSpawns::forest);
		spawn(biome, builder, "poison_skeleton", cfg, ModEntities.POISON_SKELETON.get(), 55, 1, 1, ModSpawns::swamp);
		spawn(biome, builder, "ember_skeleton", cfg, ModEntities.EMBER_SKELETON.get(), 60, 1, 1, ModSpawns::hotOverworld);
		spawn(biome, builder, "ember_skeleton", cfg, ModEntities.EMBER_SKELETON.get(), 8, 1, 1, ModSpawns::nether);

		spawn(biome, builder, "ender_skeleton", cfg, ModEntities.ENDER_SKELETON.get(), 15, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "ender_skeleton", cfg, ModEntities.ENDER_SKELETON.get(), 40, 1, 2, ModSpawns::end);
		spawn(biome, builder, "blossom_skeleton", cfg, ModEntities.BLOSSOM_SKELETON.get(), 70, 1, 1, ModSpawns::cherry);
		spawn(biome, builder, "blossom_skeleton", cfg, ModEntities.BLOSSOM_SKELETON.get(), 18, 1, 1, ModSpawns::forest);

		// creepers
		spawn(biome, builder, "lightning_creeper", cfg, ModEntities.LIGHTNING_CREEPER.get(), 60, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "burrowing_creeper", cfg, ModEntities.BURROWING_CREEPER.get(), 55, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "toxic_creeper", cfg, ModEntities.TOXIC_CREEPER.get(), 50, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "toxic_creeper", cfg, ModEntities.TOXIC_CREEPER.get(), 35, 1, 1, ModSpawns::jungle);
		spawn(biome, builder, "toxic_creeper", cfg, ModEntities.TOXIC_CREEPER.get(), 50, 1, 1, ModSpawns::swamp);
		spawn(biome, builder, "toxic_creeper", cfg, ModEntities.TOXIC_CREEPER.get(), 30, 1, 1, ModSpawns::caves);
		spawn(biome, builder, "frost_creeper", cfg, ModEntities.FROST_CREEPER.get(), 30, 1, 1, ModSpawns::cold);
		spawn(biome, builder, "magma_creeper", cfg, ModEntities.MAGMA_CREEPER.get(), 40, 1, 1, ModSpawns::hotOverworld);
		spawn(biome, builder, "magma_creeper", cfg, ModEntities.MAGMA_CREEPER.get(), 5, 1, 1, ModSpawns::nether);

		spawn(biome, builder, "wither_creeper", cfg, ModEntities.WITHER_CREEPER.get(), 18, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "wither_creeper", cfg, ModEntities.WITHER_CREEPER.get(), 7, 1, 1, ModSpawns::nether);

		spawn(biome, builder, "ender_creeper", cfg, ModEntities.ENDER_CREEPER.get(), 15, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "ender_creeper", cfg, ModEntities.ENDER_CREEPER.get(), 50, 1, 2, ModSpawns::end);

		// structure override handles ancient city; this covers the rest of the deep dark
		spawn(biome, builder, "sculk_creeper", cfg, ModEntities.SCULK_CREEPER.get(), 10, 1, 1, ModSpawns::deepDark);
		spawn(biome, builder, "blossom_creeper", cfg, ModEntities.BLOSSOM_CREEPER.get(), 55, 1, 1, ModSpawns::cherry);
		spawn(biome, builder, "blossom_creeper", cfg, ModEntities.BLOSSOM_CREEPER.get(), 12, 1, 1, ModSpawns::forest);

		// endermen
		spawn(biome, builder, "assassin_enderman", cfg, ModEntities.ASSASSIN_ENDERMAN.get(), 10, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "enraged_enderman", cfg, ModEntities.ENRAGED_ENDERMAN.get(), 10, 1, 1, ModSpawns::overworld);
		spawn(biome, builder, "assassin_enderman", cfg, ModEntities.ASSASSIN_ENDERMAN.get(), 40, 1, 2, ModSpawns::end);
		spawn(biome, builder, "enraged_enderman", cfg, ModEntities.ENRAGED_ENDERMAN.get(), 40, 1, 2, ModSpawns::end);
		spawn(biome, builder, "blossom_enderman", cfg, ModEntities.BLOSSOM_ENDERMAN.get(), 14, 1, 1, ModSpawns::cherry);

		// blazes
		// Variant blaze spawns are injected into fortress/bastion structure pools by mixin,
		// so they should not also be added to the general Nether biome spawn lists here.

		// ghasts
		spawn(biome, builder, "great_mother_ghast", cfg, ModEntities.GREAT_MOTHER_GHAST.get(), 4, 1, 1, ModSpawns::nether);
		spawn(biome, builder, "great_mother_ghast", cfg, ModEntities.GREAT_MOTHER_GHAST.get(), 6, 1, 1, ModSpawns::netherWastes);
		spawn(biome, builder, "great_mother_ghast", cfg, ModEntities.GREAT_MOTHER_GHAST.get(), 5, 1, 1, ModSpawns::crimsonForest);
		spawn(biome, builder, "delta_ghast", cfg, ModEntities.DELTA_GHAST.get(), 4, 1, 1, ModSpawns::nether);
		spawn(biome, builder, "delta_ghast", cfg, ModEntities.DELTA_GHAST.get(), 12, 1, 1, ModSpawns::basaltDeltas);
		spawn(biome, builder, "wither_ghast", cfg, ModEntities.WITHER_GHAST.get(), 4, 1, 1, ModSpawns::nether);
		spawn(biome, builder, "wither_ghast", cfg, ModEntities.WITHER_GHAST.get(), 12, 1, 1, ModSpawns::soulValley);
		spawn(biome, builder, "rageling", cfg, ModEntities.RAGELING.get(), 1, 1, 2, ModSpawns::nether);
		spawn(biome, builder, "rageling", cfg, ModEntities.RAGELING.get(), 3, 1, 3, ModSpawns::netherWastes);
		spawn(biome, builder, "rageling", cfg, ModEntities.RAGELING.get(), 2, 1, 2, ModSpawns::crimsonForest);
		spawn(biome, builder, "skitterling", cfg, ModEntities.SKITTERLING.get(), 2, 1, 1, ModSpawns::nether);
		spawn(biome, builder, "skitterling", cfg, ModEntities.SKITTERLING.get(), 6, 1, 2, ModSpawns::soulValley);
		spawn(biome, builder, "obsidling", cfg, ModEntities.OBSIDLING.get(), 2, 1, 1, ModSpawns::nether);
		spawn(biome, builder, "obsidling", cfg, ModEntities.OBSIDLING.get(), 5, 1, 2, ModSpawns::basaltDeltas);
		spawn(biome, builder, "blightling", cfg, ModEntities.BLIGHTLING.get(), 2, 1, 1, ModSpawns::nether);
		spawn(biome, builder, "blightling", cfg, ModEntities.BLIGHTLING.get(), 5, 1, 2, ModSpawns::fungalForests);
	}

	@FunctionalInterface
	private interface BiomeMatcher {
		boolean matches(Holder<Biome> biome);
	}

	private static void spawn(Holder<Biome> biome, ModifiableBiomeInfo.BiomeInfo.Builder builder,
							  String id, ModConfig cfg, EntityType<?> type,
							  int baseWeight, int min, int max, BiomeMatcher matcher) {
		if (!matcher.matches(biome)) return;
		if (!cfg.isMobEnabled(id)) return;
		int weight = cfg.adjustWeight(id, baseWeight);
		if (weight <= 0) return;
		builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,
			new MobSpawnSettings.SpawnerData(type, weight, min, max));
	}

	private static boolean overworld(Holder<Biome> biome) {
		return biome.is(BiomeTags.IS_OVERWORLD);
	}

	private static boolean nether(Holder<Biome> biome) {
		return biome.is(BiomeTags.IS_NETHER);
	}

	private static boolean netherWastes(Holder<Biome> biome) {
		return biome.is(Biomes.NETHER_WASTES);
	}

	private static boolean soulValley(Holder<Biome> biome) {
		return biome.is(Biomes.SOUL_SAND_VALLEY);
	}

	private static boolean basaltDeltas(Holder<Biome> biome) {
		return biome.is(Biomes.BASALT_DELTAS);
	}

	private static boolean crimsonForest(Holder<Biome> biome) {
		return biome.is(Biomes.CRIMSON_FOREST);
	}

	private static boolean fungalForests(Holder<Biome> biome) {
		return biome.is(Biomes.CRIMSON_FOREST) || biome.is(Biomes.WARPED_FOREST);
	}

	private static boolean end(Holder<Biome> biome) {
		return biome.is(BiomeTags.IS_END);
	}

	private static boolean cold(Holder<Biome> biome) {
		return biome.is(BiomeTags.IS_OVERWORLD) && biome.is(BiomeTags.SPAWNS_SNOW_FOXES);
	}

	private static boolean jungle(Holder<Biome> biome) {
		return biome.is(BiomeTags.IS_JUNGLE);
	}

	private static boolean forest(Holder<Biome> biome) {
		return biome.is(BiomeTags.IS_FOREST);
	}

	private static boolean cherry(Holder<Biome> biome) {
		return biome.is(Biomes.CHERRY_GROVE);
	}

	private static boolean swamp(Holder<Biome> biome) {
		return biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP);
	}

	private static boolean caves(Holder<Biome> biome) {
		return biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.DRIPSTONE_CAVES);
	}

	private static boolean deepDark(Holder<Biome> biome) {
		return biome.is(Biomes.DEEP_DARK);
	}

	private static boolean hotOverworld(Holder<Biome> biome) {
		return biome.is(BiomeTags.IS_OVERWORLD)
			&& (biome.is(BiomeTags.HAS_DESERT_PYRAMID) || biome.is(BiomeTags.IS_BADLANDS));
	}

	private record UniqueMobsSpawnBiomeModifier() implements BiomeModifier {
		private static final MapCodec<UniqueMobsSpawnBiomeModifier> CODEC =
			MapCodec.unit(UniqueMobsSpawnBiomeModifier::new);

		@Override
		public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
			if (phase == Phase.ADD) {
				registerBiomeSpawns(biome, builder);
			}
		}

		@Override
		public MapCodec<? extends BiomeModifier> codec() {
			return SPAWNS_BIOME_MODIFIER.get();
		}
	}
}


