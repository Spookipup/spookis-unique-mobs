package spookipup.uniquemobs.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookipup.uniquemobs.config.ModConfig;
import spookipup.uniquemobs.registry.ModEntities;
import spookipup.uniquemobs.spawn.StructureSpawnPoolHelper;

import java.util.List;

@Mixin(NaturalSpawner.class)
public class FortressNaturalPoolMixin {

	@Inject(method = "mobsAt", at = @At("RETURN"), cancellable = true)
	private static void uniqueMobs$addFortressVariants(ServerLevel level,
													   StructureManager structureManager,
													   ChunkGenerator generator,
													   MobCategory category,
													   BlockPos pos,
													   Holder<Biome> biome,
													   CallbackInfoReturnable<WeightedRandomList<MobSpawnSettings.SpawnerData>> cir) {
		if (category != MobCategory.MONSTER) {
			return;
		}

		boolean inFortress = NaturalSpawner.isInNetherFortressBounds(pos, level, category, structureManager);
		boolean inBastion = StructureSpawnPoolHelper.isInsideStructure(structureManager, pos, BuiltinStructures.BASTION_REMNANT);
		if (!inFortress && !inBastion) {
			return;
		}

		ModConfig cfg = ModConfig.get();
		if (!cfg.isMobEnabled("blast_blaze")
			&& !cfg.isMobEnabled("storm_blaze")
			&& !cfg.isMobEnabled("wither_blaze")
			&& !cfg.isMobEnabled("soul_blaze")
			&& !cfg.isMobEnabled("brand_blaze")) {
			return;
		}

		WeightedRandomList<MobSpawnSettings.SpawnerData> current = cir.getReturnValue();
		List<MobSpawnSettings.SpawnerData> entries = StructureSpawnPoolHelper.copyEntries(current);
		int before = entries.size();

		if (inFortress) {
			StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(entries, current, "blast_blaze", ModEntities.BLAST_BLAZE.get(), 1, 2, 3);
			StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(entries, current, "storm_blaze", ModEntities.STORM_BLAZE.get(), 1, 2, 3);
			StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(entries, current, "wither_blaze", ModEntities.WITHER_BLAZE.get(), 1, 2, 2);
			StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(entries, current, "soul_blaze", ModEntities.SOUL_BLAZE.get(), 1, 2, 3);
			StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(entries, current, "brand_blaze", ModEntities.BRAND_BLAZE.get(), 1, 1, 1);
		}

		if (inBastion) {
			StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(entries, current, "brand_blaze", ModEntities.BRAND_BLAZE.get(), 1, 1, 3);
		}

		if (entries.size() != before) {
			cir.setReturnValue(StructureSpawnPoolHelper.build(entries));
		}
	}
}
