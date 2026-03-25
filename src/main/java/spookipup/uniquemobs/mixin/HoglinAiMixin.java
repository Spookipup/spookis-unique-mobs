package spookipup.uniquemobs.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookipup.uniquemobs.entity.variant.skeleton.EmberSkeletonEntity;
import spookipup.uniquemobs.entity.variant.zombie.InfernalZombieEntity;

import java.util.Optional;

// nether rivalry - hoglins attack our fire mobs
@Mixin(HoglinAi.class)
public class HoglinAiMixin {

	@Inject(method = "findNearestValidAttackTarget", at = @At("RETURN"), cancellable = true)
	private static void addFireMobTargets(ServerLevel level, Hoglin hoglin, CallbackInfoReturnable<Optional<? extends LivingEntity>> cir) {
		// don't override if vanilla already found a target (player)
		if (cir.getReturnValue().isPresent()) return;

		NearestVisibleLivingEntities visible = hoglin.getBrain()
			.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
			.orElse(NearestVisibleLivingEntities.empty());

		visible.findClosest(e -> e instanceof InfernalZombieEntity || e instanceof EmberSkeletonEntity)
			.ifPresent(target -> cir.setReturnValue(Optional.of(target)));
	}
}
