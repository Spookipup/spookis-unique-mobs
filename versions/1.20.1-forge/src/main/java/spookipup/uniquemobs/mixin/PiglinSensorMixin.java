package spookipup.uniquemobs.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.PiglinSpecificSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookipup.uniquemobs.entity.variant.skeleton.EmberSkeletonEntity;
import spookipup.uniquemobs.entity.variant.zombie.InfernalZombieEntity;

// piglins treat fire mobs as nemeses like wither skeletons
@Mixin(PiglinSpecificSensor.class)
public class PiglinSensorMixin {

	// forge 1.20.1 mis-remaps this override in dev, so hit both names
	@Inject(method = {"doTick", "m_5578_"}, at = @At("RETURN"), remap = false)
	private void addFireMobsAsNemesis(ServerLevel level, LivingEntity entity, CallbackInfo ci) {
		Brain<?> brain = entity.getBrain();

		// don't override if vanilla already picked a nemesis (wither skeleton, etc.)
		if (brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS).isPresent()) return;

		NearestVisibleLivingEntities visible = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
			.orElse(NearestVisibleLivingEntities.empty());

		visible.findClosest(e -> e instanceof InfernalZombieEntity || e instanceof EmberSkeletonEntity)
			.ifPresent(nemesis -> brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, (Mob) nemesis));
	}
}
