package spookipup.uniquemobs.compat.betterfortresses.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookipup.uniquemobs.config.ModConfig;
import spookipup.uniquemobs.spawn.BlazeStructureSpawnHelper;
import spookipup.uniquemobs.spawn.StructureSpawnPoolHelper;

@Mixin(NaturalSpawner.class)
public class BetterFortressesNaturalSpawnMixin {

	@Unique
	private static final ResourceKey<Structure> BETTER_FORTRESS = ResourceKey.create(
		Registries.STRUCTURE,
		new ResourceLocation("betterfortresses", "fortress")
	);

	@Inject(method = "mobsAt", at = @At("RETURN"), cancellable = true)
	private static void addBetterFortressesBlazes(ServerLevel level,
												  StructureManager structureManager,
												  ChunkGenerator generator,
												  MobCategory category,
												  BlockPos pos,
												  Holder<Biome> biome,
												  CallbackInfoReturnable<WeightedRandomList<MobSpawnSettings.SpawnerData>> cir) {
		if (category != MobCategory.MONSTER) {
			return;
		}

		ModConfig cfg = ModConfig.get();
		if (!BlazeStructureSpawnHelper.hasEnabledBlazeVariants(cfg)) {
			return;
		}

		if (!StructureSpawnPoolHelper.isInsideOptionalStructure(structureManager, pos, BETTER_FORTRESS)) {
			return;
		}

		cir.setReturnValue(BlazeStructureSpawnHelper.addFortressBlazeVariants(cir.getReturnValue()));
	}
}
