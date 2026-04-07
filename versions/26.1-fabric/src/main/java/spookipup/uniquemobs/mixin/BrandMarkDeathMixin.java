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
		method = "hurtServer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"
		)
	)
	private void interruptBrandedDeath(LivingEntity entity, DamageSource damageSource, Operation<Void> original) {
		if (!BrandMarkHelper.consumeDeathSave(entity)) {
			original.call(entity, damageSource);
		}
	}
}
