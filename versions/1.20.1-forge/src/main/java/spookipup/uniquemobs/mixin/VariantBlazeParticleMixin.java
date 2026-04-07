package spookipup.uniquemobs.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import spookipup.uniquemobs.entity.variant.blaze.BrandBlazeEntity;
import spookipup.uniquemobs.entity.variant.blaze.SoulBlazeEntity;
import spookipup.uniquemobs.entity.variant.blaze.StormBlazeEntity;
import spookipup.uniquemobs.entity.variant.blaze.WitherBlazeEntity;

// swaps the vanilla blaze smoke aura for variant-specific particles
@Mixin(Blaze.class)
public abstract class VariantBlazeParticleMixin {

	@WrapOperation(
		method = {
			"aiStep()V",
			"m_8107_()V"
		},
		at = {
			@At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
			),
			@At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/level/Level;m_7106_(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
				remap = false
			)
		}
	)
	private void replaceVariantBlazeAura(Level level, ParticleOptions particle,
									   double x, double y, double z,
									   double dx, double dy, double dz,
									   Operation<Void> original) {
		if ((Object) this instanceof StormBlazeEntity stormBlaze) {
			level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0.0, 0.01, 0.0);
			if (stormBlaze.getRandom().nextBoolean()) {
				level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.0, 0.0);
			}
			return;
		}
		if ((Object) this instanceof SoulBlazeEntity soulBlaze) {
			level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0, 0.01, 0.0);
			if (soulBlaze.getRandom().nextBoolean()) {
				level.addParticle(ParticleTypes.SOUL, x, y, z, 0.0, 0.01, 0.0);
			}
			return;
		}
		if ((Object) this instanceof WitherBlazeEntity witherBlaze) {
			level.addParticle(ParticleTypes.WHITE_ASH, x, y, z, 0.0, 0.01, 0.0);
			if (witherBlaze.getRandom().nextBoolean()) {
				level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.01, 0.0);
			}
			return;
		}
		if ((Object) this instanceof BrandBlazeEntity brandBlaze) {
			level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.01, 0.0);
			if (brandBlaze.getRandom().nextBoolean()) {
				level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0, 0.01, 0.0);
			}
			if (brandBlaze.getRandom().nextInt(4) == 0) {
				level.addParticle(ParticleTypes.LAVA, x, y, z, 0.0, 0.0, 0.0);
			}
			return;
		}

		original.call(level, particle, x, y, z, dx, dy, dz);
	}
}


