package spookipup.uniquemobs.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookipup.uniquemobs.config.ModConfig;
import spookipup.uniquemobs.registry.ModEntities;

// mineshaft spawner variant swap, same deal as the dungeon one
@Mixin(MineshaftPieces.MineShaftCorridor.class)
public class MineshaftSpawnerMixin {

	@Unique
	private static final ThreadLocal<WorldGenLevel> LEVEL = new ThreadLocal<>();

	// postProcess is where the corridor actually places blocks into the world
	@Inject(method = "postProcess", at = @At("HEAD"))
	private void captureLevel(WorldGenLevel level, StructureManager structureManager,
		ChunkGenerator generator, RandomSource random, BoundingBox box,
		ChunkPos chunkPos, BlockPos blockPos, CallbackInfo ci) {
		LEVEL.set(level);
	}

	@Inject(method = "postProcess", at = @At("RETURN"))
	private void clearLevel(WorldGenLevel level, StructureManager structureManager,
		ChunkGenerator generator, RandomSource random, BoundingBox box,
		ChunkPos chunkPos, BlockPos blockPos, CallbackInfo ci) {
		LEVEL.remove();
	}

	// intercept the setEntityId(CAVE_SPIDER) call on the spawner block entity
	@WrapOperation(
		method = "postProcess",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/SpawnerBlockEntity;setEntityId(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/util/RandomSource;)V")
	)
	private void wrapSpawnerEntity(SpawnerBlockEntity spawner, EntityType<?> entityType, RandomSource random, Operation<Void> original) {
		WorldGenLevel level = LEVEL.get();
		ModConfig cfg = ModConfig.get();
		if (level != null && random.nextFloat() < (float) cfg.mineshaftReplacementChance) {
			Holder<Biome> biome = level.getBiome(spawner.getBlockPos());
			EntityType<?> variant = pickSpider(biome, random);
			String id = EntityType.getKey(variant).getPath();
			if (cfg.isMobEnabled(id)) entityType = variant;
		}
		original.call(spawner, entityType, random);
	}

	@Unique
	private static EntityType<?> pickSpider(Holder<Biome> biome, RandomSource random) {
		if (biome.is(BiomeTags.SPAWNS_SNOW_FOXES)) return ModEntities.ICE_SPIDER;
		if (biome.is(BiomeTags.HAS_DESERT_PYRAMID) || biome.is(BiomeTags.IS_BADLANDS)) return ModEntities.MAGMA_SPIDER;
		if (biome.is(BiomeTags.IS_JUNGLE)) return ModEntities.SPITTING_SPIDER;
		if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) {
			return random.nextBoolean() ? ModEntities.SPITTING_SPIDER : ModEntities.WEB_SPINNER_SPIDER;
		}
		if (biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.DRIPSTONE_CAVES)) return ModEntities.WEB_SPINNER_SPIDER;
		// web spinners and spitting spiders feel right for mineshafts
		return random.nextBoolean() ? ModEntities.WEB_SPINNER_SPIDER : ModEntities.SPITTING_SPIDER;
	}
}
