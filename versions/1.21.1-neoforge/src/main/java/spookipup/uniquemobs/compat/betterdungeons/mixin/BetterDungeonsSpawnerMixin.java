package spookipup.uniquemobs.compat.betterdungeons.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookipup.uniquemobs.compat.SpawnerVariants;

// hooks into better dungeons' spawner processors to mix in our variants
@Pseudo
@Mixin(targets = {
	"com.yungnickyoung.minecraft.betterdungeons.world.processor.skeleton_dungeon.SkeletonMobSpawnerProcessor",
	"com.yungnickyoung.minecraft.betterdungeons.world.processor.zombie_dungeon.ZombieMobSpawnerProcessor",
	"com.yungnickyoung.minecraft.betterdungeons.world.processor.zombie_dungeon.ZombieTombstoneSpawnerProcessor",
	"com.yungnickyoung.minecraft.betterdungeons.world.processor.MobSpawnerProcessor"
})
public class BetterDungeonsSpawnerMixin {

	// remap=false because @Pseudo targets can't be resolved at compile time for remapping
	@Inject(method = "processBlock", at = @At("RETURN"), cancellable = true, remap = false)
	private void replaceWithVariant(LevelReader levelReader, BlockPos jigsawPiecePos,
		BlockPos jigsawPieceBottomCenterPos,
		StructureTemplate.StructureBlockInfo blockInfoLocal,
		StructureTemplate.StructureBlockInfo blockInfoGlobal,
		StructurePlaceSettings settings,
		CallbackInfoReturnable<StructureTemplate.StructureBlockInfo> cir) {

		StructureTemplate.StructureBlockInfo result = cir.getReturnValue();
		if (result.nbt() == null || !result.state().is(Blocks.SPAWNER)) return;

		Holder<Biome> biome = levelReader.getBiome(result.pos());
		// position-seeded random for deterministic worldgen
		RandomSource random = RandomSource.create(result.pos().asLong());
		StructureTemplate.StructureBlockInfo replaced = SpawnerVariants.tryReplace(result, biome, random);
		if (replaced != result) cir.setReturnValue(replaced);
	}
}
