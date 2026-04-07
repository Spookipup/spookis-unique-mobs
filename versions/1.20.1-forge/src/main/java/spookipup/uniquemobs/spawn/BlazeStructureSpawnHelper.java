package spookipup.uniquemobs.spawn;

import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import spookipup.uniquemobs.config.ModConfig;
import spookipup.uniquemobs.registry.ModEntities;
import spookipup.uniquemobs.spawn.ConfigWeightedSelection.WeightedType;

import java.util.List;

public final class BlazeStructureSpawnHelper {
	private BlazeStructureSpawnHelper() {}

	public static boolean hasEnabledBlazeVariants(ModConfig cfg) {
		return cfg.isMobEnabled("blast_blaze")
			|| cfg.isMobEnabled("storm_blaze")
			|| cfg.isMobEnabled("wither_blaze")
			|| cfg.isMobEnabled("soul_blaze")
			|| cfg.isMobEnabled("brand_blaze");
	}

	public static WeightedRandomList<MobSpawnSettings.SpawnerData> addFortressBlazeVariants(
		WeightedRandomList<MobSpawnSettings.SpawnerData> current
	) {
		List<MobSpawnSettings.SpawnerData> entries = StructureSpawnPoolHelper.copyEntries(current);
		StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(entries, current, "blast_blaze", ModEntities.BLAST_BLAZE.get(), 1, 2, 3);
		StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(entries, current, "storm_blaze", ModEntities.STORM_BLAZE.get(), 1, 2, 3);
		StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(entries, current, "wither_blaze", ModEntities.WITHER_BLAZE.get(), 1, 2, 2);
		StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(entries, current, "soul_blaze", ModEntities.SOUL_BLAZE.get(), 1, 2, 3);
		StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(entries, current, "brand_blaze", ModEntities.BRAND_BLAZE.get(), 1, 1, 1);
		return StructureSpawnPoolHelper.build(entries);
	}

	public static EntityType<?> pickFortressSpawnerVariant(ModConfig cfg, RandomSource random) {
		if (!cfg.isMobEnabled("blast_blaze")
			&& !cfg.isMobEnabled("storm_blaze")
			&& !cfg.isMobEnabled("wither_blaze")
			&& !cfg.isMobEnabled("soul_blaze")) {
			return null;
		}
		return ConfigWeightedSelection.pickWeightedEntityType(random, null,
			new WeightedType(ModEntities.BLAST_BLAZE.get(), 3),
			new WeightedType(ModEntities.STORM_BLAZE.get(), 3),
			new WeightedType(ModEntities.WITHER_BLAZE.get(), 2),
			new WeightedType(ModEntities.SOUL_BLAZE.get(), 3));
	}
}
