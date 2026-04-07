package spookipup.uniquemobs.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class BlossomDriftMobEffect extends MobEffect {

	public BlossomDriftMobEffect() {
		super(MobEffectCategory.HARMFUL, 0xFAD6E5);
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
		Vec3 motion = livingEntity.getDeltaMovement();
		double phase = livingEntity.tickCount * 0.35 + livingEntity.getId() * 0.77;
		double driftScale = livingEntity.onGround() ? 0.006 : 0.018;
		double driftX = Math.sin(phase) * driftScale;
		double driftZ = Math.cos(phase * 0.8) * driftScale;
		double y = motion.y;

		if (!livingEntity.onGround()) {
			if (y < -0.08) {
				y = -0.08;
			} else if (y < 0.04) {
				y += 0.012;
			}
		}

		double drag = livingEntity.onGround() ? 0.88 : 0.95;
		livingEntity.setDeltaMovement(motion.x * drag + driftX, y, motion.z * drag + driftZ);
		livingEntity.fallDistance = 0.0F;
		livingEntity.hurtMarked = true;
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}
}
