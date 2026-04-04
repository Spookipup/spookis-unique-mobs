package spookipup.uniquemobs.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import spookipup.uniquemobs.config.ModConfig;

import java.util.ArrayList;
import java.util.List;

public final class StructureSpawnPoolHelper {

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
		entries.add(new MobSpawnSettings.SpawnerData(type, weight, minCount, maxCount));
	}

	public static WeightedRandomList<MobSpawnSettings.SpawnerData> build(List<MobSpawnSettings.SpawnerData> entries) {
		return WeightedRandomList.create(entries);
	}

	public static boolean isInsideStructure(StructureManager structureManager, BlockPos pos, ResourceKey<Structure> key) {
		Structure structure = structureManager.registryAccess()
			.registryOrThrow(Registries.STRUCTURE)
			.getHolderOrThrow(key)
			.value();
		return structure != null && structureManager.getStructureAt(pos, structure).isValid();
	}

	public static boolean isInsideOptionalStructure(StructureManager structureManager, BlockPos pos, ResourceKey<Structure> key) {
		Structure structure = structureManager.registryAccess()
			.registryOrThrow(Registries.STRUCTURE)
			.get(key.location());
		return structure != null && structureManager.getStructureAt(pos, structure).isValid();
	}
}


