package spookipup.uniquemobs.mixin.accessor;

import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrialSpawner.class)
public interface TrialSpawnerAccessor {

	@Accessor("normalConfig")
	TrialSpawnerConfig uniqueMobs$getNormalConfig();

	@Mutable
	@Accessor("normalConfig")
	void uniqueMobs$setNormalConfig(TrialSpawnerConfig config);

	@Accessor("ominousConfig")
	TrialSpawnerConfig uniqueMobs$getOminousConfig();

	@Mutable
	@Accessor("ominousConfig")
	void uniqueMobs$setOminousConfig(TrialSpawnerConfig config);
}
