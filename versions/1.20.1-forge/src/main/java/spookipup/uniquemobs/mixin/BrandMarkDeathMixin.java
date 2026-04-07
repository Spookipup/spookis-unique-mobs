package spookipup.uniquemobs.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import spookipup.uniquemobs.entity.BrandMarkHelper;

// branded mobs get one death save instead of completing the lethal hit
@Mixin(LivingEntity.class)
public abstract class BrandMarkDeathMixin {

	@WrapOperation(
		method = {
			"hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
			"m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
		},
		at = {
			@At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"
			),
			@At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/entity/LivingEntity;m_6667_(Lnet/minecraft/world/damagesource/DamageSource;)V",
				remap = false
			)
		}
	)
	private void interruptBrandedDeath(LivingEntity entity, DamageSource damageSource, Operation<Void> original) {
		if (!BrandMarkHelper.consumeDeathSave(entity)) {
			original.call(entity, damageSource);
		}
	}
}


