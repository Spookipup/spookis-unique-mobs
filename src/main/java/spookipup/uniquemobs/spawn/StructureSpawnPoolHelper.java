package spookipup.uniquemobs.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import spookipup.uniquemobs.config.ModConfig;

public final class StructureSpawnPoolHelper {

	private StructureSpawnPoolHelper() {}

	public static WeightedList.Builder<MobSpawnSettings.SpawnerData> copyToBuilder(WeightedList<MobSpawnSettings.SpawnerData> source) {
		WeightedList.Builder<MobSpawnSettings.SpawnerData> builder = WeightedList.builder();
		for (Weighted<MobSpawnSettings.SpawnerData> weighted : source.unwrap()) {
			builder.add(weighted.value(), weighted.weight());
		}
		return builder;
	}

	public static boolean containsType(WeightedList<MobSpawnSettings.SpawnerData> source, EntityType<?> type) {
		for (Weighted<MobSpawnSettings.SpawnerData> weighted : source.unwrap()) {
			if (weighted.value().type() == type) {
				return true;
			}
		}
		return false;
	}

	public static void addAdjustedIfEnabledAndMissing(WeightedList.Builder<MobSpawnSettings.SpawnerData> builder,
													  WeightedList<MobSpawnSettings.SpawnerData> source,
													  String mobId, EntityType<?> type,
													  int minCount, int maxCount, int baseWeight) {
		ModConfig cfg = ModConfig.get();
		if (!cfg.isMobEnabled(mobId) || containsType(source, type)) {
			return;
		}

		int weight = Math.max(1, cfg.adjustWeight(mobId, baseWeight));
		builder.add(new MobSpawnSettings.SpawnerData(type, minCount, maxCount), weight);
	}

	public static boolean isInsideStructure(StructureManager structureManager, BlockPos pos, ResourceKey<Structure> key) {
		Structure structure = structureManager.registryAccess()
			.lookupOrThrow(Registries.STRUCTURE)
			.getValue(key);
		return structure != null && structureManager.getStructureAt(pos, structure).isValid();
	}
}
