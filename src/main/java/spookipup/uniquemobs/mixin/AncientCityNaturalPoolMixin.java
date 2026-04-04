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
public class AncientCityNaturalPoolMixin {

	@Inject(method = "mobsAt", at = @At("RETURN"), cancellable = true)
	private static void uniqueMobs$addAncientCitySculkCreeper(ServerLevel level,
															  StructureManager structureManager,
															  ChunkGenerator generator,
															  MobCategory category,
															  BlockPos pos,
															  Holder<Biome> biome,
															  CallbackInfoReturnable<WeightedRandomList<MobSpawnSettings.SpawnerData>> cir) {
		if (category != MobCategory.MONSTER) {
			return;
		}
		if (!ModConfig.get().isMobEnabled("sculk_creeper")) {
			return;
		}
		if (!StructureSpawnPoolHelper.isInsideStructure(structureManager, pos, BuiltinStructures.ANCIENT_CITY)) {
			return;
		}

		WeightedRandomList<MobSpawnSettings.SpawnerData> current = cir.getReturnValue();
		List<MobSpawnSettings.SpawnerData> entries = StructureSpawnPoolHelper.copyEntries(current);
		int before = entries.size();
		StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(entries, current, "sculk_creeper", ModEntities.SCULK_CREEPER, 1, 1, 10);
		if (entries.size() != before) {
			cir.setReturnValue(StructureSpawnPoolHelper.build(entries));
		}
	}
}
