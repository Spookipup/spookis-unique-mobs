package spookipup.uniquemobs.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedList;
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

@Mixin(NaturalSpawner.class)
public class FortressNaturalSpawnMixin {

	@Inject(method = "mobsAt", at = @At("RETURN"), cancellable = true)
	private static void addCustomFortressBlazes(ServerLevel level, StructureManager structureManager,
												ChunkGenerator generator, MobCategory category, BlockPos pos,
												Holder<Biome> biome, CallbackInfoReturnable<WeightedList<MobSpawnSettings.SpawnerData>> cir) {
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

		WeightedList<MobSpawnSettings.SpawnerData> current = cir.getReturnValue();
		WeightedList.Builder<MobSpawnSettings.SpawnerData> builder = StructureSpawnPoolHelper.copyToBuilder(current);

		if (inFortress) {
			StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(builder, current, "blast_blaze", ModEntities.BLAST_BLAZE, 1, 2, 3);
			StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(builder, current, "storm_blaze", ModEntities.STORM_BLAZE, 1, 2, 3);
			StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(builder, current, "wither_blaze", ModEntities.WITHER_BLAZE, 1, 2, 2);
			StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(builder, current, "soul_blaze", ModEntities.SOUL_BLAZE, 1, 2, 3);
			StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(builder, current, "brand_blaze", ModEntities.BRAND_BLAZE, 1, 1, 1);
		}

		if (inBastion) {
			StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(builder, current, "brand_blaze", ModEntities.BRAND_BLAZE, 1, 1, 3);
		}

		cir.setReturnValue(builder.build());
	}
}
