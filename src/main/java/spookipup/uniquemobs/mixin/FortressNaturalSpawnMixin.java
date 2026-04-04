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
import spookipup.uniquemobs.spawn.BlazeStructureSpawnHelper;
import spookipup.uniquemobs.spawn.StructureSpawnPoolHelper;

import java.util.List;

@Mixin(NaturalSpawner.class)
public class FortressNaturalSpawnMixin {

	@Inject(method = "mobsAt", at = @At("RETURN"), cancellable = true)
	private static void addCustomFortressBlazes(ServerLevel level, StructureManager structureManager,
												ChunkGenerator generator, MobCategory category, BlockPos pos,
												Holder<Biome> biome, CallbackInfoReturnable<WeightedRandomList<MobSpawnSettings.SpawnerData>> cir) {
		if (category != MobCategory.MONSTER) {
			return;
		}

		boolean inFortress = NaturalSpawner.isInNetherFortressBounds(pos, level, category, structureManager);
		boolean inBastion = StructureSpawnPoolHelper.isInsideStructure(structureManager, pos, BuiltinStructures.BASTION_REMNANT);
		if (!inFortress && !inBastion) {
			return;
		}

		ModConfig cfg = ModConfig.get();
		if (!BlazeStructureSpawnHelper.hasEnabledBlazeVariants(cfg)) {
			return;
		}

		WeightedRandomList<MobSpawnSettings.SpawnerData> current = cir.getReturnValue();
		if (inFortress) {
			current = BlazeStructureSpawnHelper.addFortressBlazeVariants(current);
		}

		List<MobSpawnSettings.SpawnerData> entries = StructureSpawnPoolHelper.copyEntries(current);

		if (inBastion) {
			StructureSpawnPoolHelper.addAdjustedIfEnabledAndMissing(entries, current, "brand_blaze", ModEntities.BRAND_BLAZE, 1, 1, 3);
		}

		cir.setReturnValue(StructureSpawnPoolHelper.build(entries));
	}
}


