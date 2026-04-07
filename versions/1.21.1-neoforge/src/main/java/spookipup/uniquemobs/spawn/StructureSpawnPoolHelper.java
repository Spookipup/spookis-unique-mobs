package spookipup.uniquemobs.spawn;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.apache.commons.lang3.mutable.MutableBoolean;
import spookipup.uniquemobs.config.ModConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class StructureSpawnPoolHelper {

	private static final Map<SpawnerDataKey, MobSpawnSettings.SpawnerData> CACHED_SPAWNER_DATA = new ConcurrentHashMap<>();

	private StructureSpawnPoolHelper() {}

	public static List<MobSpawnSettings.SpawnerData> copyEntries(WeightedRandomList<MobSpawnSettings.SpawnerData> source) {
		return new ArrayList<>(source.unwrap());
	}

	public static boolean containsType(WeightedRandomList<MobSpawnSettings.SpawnerData> source, EntityType<?> type) {
		for (MobSpawnSettings.SpawnerData entry : source.unwrap()) {
			if (entry.type == type) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsType(List<MobSpawnSettings.SpawnerData> entries, EntityType<?> type) {
		for (MobSpawnSettings.SpawnerData entry : entries) {
			if (entry.type == type) {
				return true;
			}
		}
		return false;
	}

	public static void addAdjustedIfEnabledAndMissing(List<MobSpawnSettings.SpawnerData> entries,
													  WeightedRandomList<MobSpawnSettings.SpawnerData> source,
													  String mobId,
													  Supplier<? extends EntityType<?>> type,
													  int minCount,
													  int maxCount,
													  int baseWeight) {
		addAdjustedIfEnabledAndMissing(entries, source, mobId, type.get(), minCount, maxCount, baseWeight);
	}

	public static void addAdjustedIfEnabledAndMissing(List<MobSpawnSettings.SpawnerData> entries,
													  WeightedRandomList<MobSpawnSettings.SpawnerData> source,
													  String mobId,
													  EntityType<?> type,
													  int minCount,
													  int maxCount,
													  int baseWeight) {
		ModConfig cfg = ModConfig.get();
		if (!cfg.isMobEnabled(mobId) || containsType(source, type)) {
			return;
		}

		int weight = Math.max(1, cfg.adjustWeight(mobId, baseWeight));
		entries.add(getCachedSpawnerData(type, weight, minCount, maxCount));
	}

	public static void addAdjustedIfEnabledAndMissing(List<MobSpawnSettings.SpawnerData> entries,
													  String mobId,
													  Supplier<? extends EntityType<?>> type,
													  int minCount,
													  int maxCount,
													  int baseWeight) {
		addAdjustedIfEnabledAndMissing(entries, mobId, type.get(), minCount, maxCount, baseWeight);
	}

	public static void addAdjustedIfEnabledAndMissing(List<MobSpawnSettings.SpawnerData> entries,
													  String mobId,
													  EntityType<?> type,
													  int minCount,
													  int maxCount,
													  int baseWeight) {
		ModConfig cfg = ModConfig.get();
		if (!cfg.isMobEnabled(mobId) || containsType(entries, type)) {
			return;
		}

		int weight = Math.max(1, cfg.adjustWeight(mobId, baseWeight));
		entries.add(getCachedSpawnerData(type, weight, minCount, maxCount));
	}

	public static WeightedRandomList<MobSpawnSettings.SpawnerData> build(List<MobSpawnSettings.SpawnerData> entries) {
		return WeightedRandomList.create(entries);
	}

	// NaturalSpawner re-queries the pool and checks List.contains(selectedEntry).
	// Reusing the same injected SpawnerData instances keeps those follow-up checks valid.
	private static MobSpawnSettings.SpawnerData getCachedSpawnerData(EntityType<?> type, int weight, int minCount, int maxCount) {
		return CACHED_SPAWNER_DATA.computeIfAbsent(
			new SpawnerDataKey(type, weight, minCount, maxCount),
			key -> new MobSpawnSettings.SpawnerData(key.type(), key.weight(), key.minCount(), key.maxCount())
		);
	}

	private record SpawnerDataKey(EntityType<?> type, int weight, int minCount, int maxCount) {}

	public static boolean isInsideStructure(StructureManager structureManager, BlockPos pos, ResourceKey<Structure> key) {
		return isInsideStructureBoundingBox(structureManager, pos, key);
	}

	public static boolean isInsideStructureBoundingBox(StructureManager structureManager, BlockPos pos, ResourceKey<Structure> key) {
		Structure structure = structureManager.registryAccess()
			.registryOrThrow(Registries.STRUCTURE)
			.getHolderOrThrow(key)
			.value();
		return isInsideResolvedStructureBoundingBox(structureManager, pos, structure);
	}

	public static boolean isInsideOptionalStructure(StructureManager structureManager, BlockPos pos, ResourceKey<Structure> key) {
		return isInsideOptionalStructureBoundingBox(structureManager, pos, key);
	}

	public static boolean isInsideOptionalStructureBoundingBox(StructureManager structureManager, BlockPos pos, ResourceKey<Structure> key) {
		Structure structure = structureManager.registryAccess()
			.registryOrThrow(Registries.STRUCTURE)
			.get(key.location());
		return isInsideResolvedStructureBoundingBox(structureManager, pos, structure);
	}

	public static boolean isInsideStructurePiece(StructureManager structureManager, BlockPos pos, ResourceKey<Structure> key) {
		Structure structure = structureManager.registryAccess()
			.registryOrThrow(Registries.STRUCTURE)
			.getHolderOrThrow(key)
			.value();
		return isInsideResolvedStructurePiece(structureManager, pos, structure);
	}

	public static boolean isInsideOptionalStructurePiece(StructureManager structureManager, BlockPos pos, ResourceKey<Structure> key) {
		Structure structure = structureManager.registryAccess()
			.registryOrThrow(Registries.STRUCTURE)
			.get(key.location());
		return isInsideResolvedStructurePiece(structureManager, pos, structure);
	}

	private static boolean isInsideResolvedStructureBoundingBox(StructureManager structureManager, BlockPos pos, Structure structure) {
		return matchesResolvedStructure(structureManager, pos, structure, start -> start.getBoundingBox().isInside(pos));
	}

	private static boolean isInsideResolvedStructurePiece(StructureManager structureManager, BlockPos pos, Structure structure) {
		return matchesResolvedStructure(structureManager, pos, structure, start -> structureManager.structureHasPieceAt(pos, start));
	}

	private static boolean matchesResolvedStructure(StructureManager structureManager,
													BlockPos pos,
													Structure structure,
													Predicate<StructureStart> predicate) {
		if (structure == null) {
			return false;
		}

		Map<Structure, LongSet> structuresAtPos = structureManager.getAllStructuresAt(pos);
		LongSet references = structuresAtPos.get(structure);
		if (references == null || references.isEmpty()) {
			return false;
		}

		MutableBoolean matches = new MutableBoolean(false);
		structureManager.fillStartsForStructure(structure, references, start -> {
			if (!matches.isTrue() && predicate.test(start)) {
				matches.setTrue();
			}
		});
		return matches.isTrue();
	}
}
