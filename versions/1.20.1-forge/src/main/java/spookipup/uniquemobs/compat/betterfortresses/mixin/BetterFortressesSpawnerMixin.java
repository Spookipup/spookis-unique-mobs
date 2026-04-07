package spookipup.uniquemobs.compat.betterfortresses.mixin;

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

// hooks Better Fortresses structure processors so blaze-template spawners can swap to our fortress blaze variants
@Pseudo
@Mixin(targets = {
	"com.yungnickyoung.minecraft.betterfortresses.world.processor.BridgeArchProcessor",
	"com.yungnickyoung.minecraft.betterfortresses.world.processor.LiquidBlockProcessor",
	"com.yungnickyoung.minecraft.betterfortresses.world.processor.NetherWartProcessor",
	"com.yungnickyoung.minecraft.betterfortresses.world.processor.PillarProcessor",
	"com.yungnickyoung.minecraft.betterfortresses.world.processor.RedSandstoneStairsProcessor",
	"com.yungnickyoung.minecraft.betterfortresses.world.processor.StairPillarProcessor"
})
public class BetterFortressesSpawnerMixin {

	@Inject(method = {"processBlock", "m_7382_"}, at = @At("RETURN"), cancellable = true, remap = false)
	private void replaceBetterFortressesBlazeSpawner(LevelReader levelReader, BlockPos jigsawPiecePos,
		BlockPos jigsawPieceBottomCenterPos,
		StructureTemplate.StructureBlockInfo blockInfoLocal,
		StructureTemplate.StructureBlockInfo blockInfoGlobal,
		StructurePlaceSettings settings,
		CallbackInfoReturnable<StructureTemplate.StructureBlockInfo> cir) {

		StructureTemplate.StructureBlockInfo result = cir.getReturnValue();
		if (result == null || result.nbt() == null || !result.state().is(Blocks.SPAWNER)) {
			return;
		}

		Holder<Biome> biome = levelReader.getBiome(result.pos());
		RandomSource random = RandomSource.create(result.pos().asLong());
		StructureTemplate.StructureBlockInfo replaced = SpawnerVariants.tryReplaceFortressBlazeSpawner(result, biome, random);
		if (replaced != result) {
			cir.setReturnValue(replaced);
		}
	}
}
