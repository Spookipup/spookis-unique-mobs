package spookipup.uniquemobs.mixin.accessor;

import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrialSpawner.class)
public interface TrialSpawnerAccessor {

	@Accessor("config")
	TrialSpawner.FullConfig uniqueMobs$getConfig();

	@Accessor("config")
	void uniqueMobs$setConfig(TrialSpawner.FullConfig config);
}
