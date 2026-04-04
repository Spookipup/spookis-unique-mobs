package spookipup.uniquemobs.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookipup.uniquemobs.config.ModConfig;
import spookipup.uniquemobs.registry.ModEntities;
import spookipup.uniquemobs.spawn.StructureSpawnPoolHelper;

import java.util.List;

@Mixin(NaturalSpawner.class)
public class AncientCityNaturalSpawnMixin {

	@Inject(method = "mobsAt", at = @At("RETURN"), cancellable = true)
	private static void addAncientCitySculkCreeper(ServerLevel level, StructureManager structureManager,
												   ChunkGenerator generator, MobCategory category, BlockPos pos,
												   Holder<Biome> biome, CallbackInfoReturnable<WeightedRandomList<MobSpawnSettings.SpawnerData>> cir) {
		if (category != MobCategory.MONSTER) return;
		if (!ModConfig.get().isMobEnabled("sculk_creeper")) return;
		if (!isInsideAncientCity(structureManager, pos)) return;

		WeightedRandomList<MobSpawnSettings.SpawnerData> current = cir.getReturnValue();
		List<MobSpawnSettings.SpawnerData> entries = StructureSpawnPoolHelper.copyEntries(current);
		StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(entries, current, "sculk_creeper", ModEntities.SCULK_CREEPER, 1, 1, 10);

		cir.setReturnValue(StructureSpawnPoolHelper.build(entries));
	}

	private static boolean isInsideAncientCity(StructureManager structureManager, BlockPos pos) {
		Structure structure = structureManager.registryAccess()
			.registryOrThrow(Registries.STRUCTURE)
			.getHolderOrThrow(BuiltinStructures.ANCIENT_CITY)
			.value();
		return structure != null && structureManager.getStructureAt(pos, structure).isValid();
	}
}


