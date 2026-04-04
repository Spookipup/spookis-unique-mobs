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
import spookipup.uniquemobs.registry.ModEntities;
import spookipup.uniquemobs.spawn.ConfigWeightedSelection;

// fortress blaze spawners can swap to the blaze variant instead of relying on natural spawns
@Mixin(NetherFortressPieces.MonsterThrone.class)
public class FortressSpawnerMixin {

	@WrapOperation(
		method = "postProcess",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/SpawnerBlockEntity;setEntityId(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/util/RandomSource;)V")
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
		if (!cfg.isMobEnabled("blast_blaze")
			&& !cfg.isMobEnabled("storm_blaze")
			&& !cfg.isMobEnabled("wither_blaze")
			&& !cfg.isMobEnabled("soul_blaze")) {
			return null;
		}
		return ConfigWeightedSelection.pickWeightedEntityType(random, null,
			new ConfigWeightedSelection.WeightedType(ModEntities.BLAST_BLAZE, 3),
			new ConfigWeightedSelection.WeightedType(ModEntities.STORM_BLAZE, 3),
			new ConfigWeightedSelection.WeightedType(ModEntities.WITHER_BLAZE, 2),
			new ConfigWeightedSelection.WeightedType(ModEntities.SOUL_BLAZE, 3));
	}
}
