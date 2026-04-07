package spookipup.uniquemobs.compat.betterdungeons.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookipup.uniquemobs.compat.SpawnerVariants;

// hooks into better dungeons' spider dungeon pieces to mix in our variants.
// spider dungeons place spawners directly via setEntityId instead of using processors
@Pseudo
@Mixin(targets = {
	"com.yungnickyoung.minecraft.betterdungeons.world.structure.spider_dungeon.piece.SpiderDungeonNestPiece",
	"com.yungnickyoung.minecraft.betterdungeons.world.structure.spider_dungeon.piece.SpiderDungeonEggRoomPiece"
})
public class BetterDungeonsSpiderMixin {

	// ThreadLocal to pass the level to the redirect
	@Unique
	private static final ThreadLocal<WorldGenLevel> LEVEL = new ThreadLocal<>();

	@Inject(method = "method_14931", at = @At("HEAD"), remap = false)
	private void captureLevel(WorldGenLevel level, StructureManager mgr, ChunkGenerator gen,
		RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot, CallbackInfo ci) {
		LEVEL.set(level);
	}

	@Inject(method = "method_14931", at = @At("RETURN"), remap = false)
	private void clearLevel(WorldGenLevel level, StructureManager mgr, ChunkGenerator gen,
		RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot, CallbackInfo ci) {
		LEVEL.remove();
	}

	@Redirect(
		method = "method_14931",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/class_2636;method_46408(Lnet/minecraft/class_1299;Lnet/minecraft/class_5819;)V"),
		remap = false
	)
	private void replaceSpawnerId(SpawnerBlockEntity spawner, EntityType<?> type, RandomSource random) {
		WorldGenLevel level = LEVEL.get();
		if (level != null) {
			Holder<Biome> biome = level.getBiome(spawner.getBlockPos());
			RandomSource posRandom = RandomSource.create(spawner.getBlockPos().asLong());
			EntityType<?> variant = SpawnerVariants.tryReplaceType(type, biome, posRandom);
			spawner.setEntityId(variant, random);
		} else {
			spawner.setEntityId(type, random);
		}
	}
}
