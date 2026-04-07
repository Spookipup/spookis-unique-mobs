package spookipup.uniquemobs.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import spookipup.uniquemobs.config.ModConfig;
import spookipup.uniquemobs.spawn.BlazeStructureSpawnHelper;

// fortress blaze spawners can swap to the blaze variant instead of relying on natural spawns
@Mixin(NetherFortressPieces.MonsterThrone.class)
public class FortressSpawnerMixin {

	@WrapOperation(
		method = {
			"postProcess(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/core/BlockPos;)V",
			"m_213694_(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/core/BlockPos;)V"
		},
		at = {
			@At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/level/block/entity/SpawnerBlockEntity;setEntityId(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/util/RandomSource;)V"
			),
			@At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/level/block/entity/SpawnerBlockEntity;m_252803_(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/util/RandomSource;)V",
				remap = false
			)
		}
	)
	private void wrapSpawnerEntity(SpawnerBlockEntity spawner, EntityType<?> entityType, RandomSource random, Operation<Void> original) {
		ModConfig cfg = ModConfig.get();
		if (entityType == EntityType.BLAZE
			&& random.nextFloat() < (float) cfg.fortressSpawnerReplacementChance) {
			EntityType<?> replacement = pickBlazeVariant(cfg, random);
			if (replacement != null) {
				entityType = replacement;
			}
		}
		original.call(spawner, entityType, random);
	}

	private static EntityType<?> pickBlazeVariant(ModConfig cfg, RandomSource random) {
		return BlazeStructureSpawnHelper.pickFortressSpawnerVariant(cfg, random);
	}
}


