package spookipup.uniquemobs.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Enemy;
import spookipup.uniquemobs.entity.variant.blaze.BrandBlazeEntity;
import spookipup.uniquemobs.registry.ModEffects;

public final class BrandMarkHelper {

	private static final int BRAND_DURATION = 180;

	private BrandMarkHelper() {}

	public static boolean canBrand(Mob mob) {
		return mob.isAlive()
			&& mob instanceof Enemy
			&& !(mob instanceof BrandBlazeEntity)
			&& !mob.hasEffect(ModEffects.BRAND_MARK);
	}

	public static void applyBrand(Mob mob) {
		mob.addEffect(new MobEffectInstance(ModEffects.BRAND_MARK, BRAND_DURATION, 0));

		if (mob.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.FLAME,
				mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
				20, 0.35, 0.45, 0.35, 0.01);
			serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
				mob.getX(), mob.getY() + mob.getBbHeight() * 0.55, mob.getZ(),
				10, 0.25, 0.35, 0.25, 0.01);
			serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
				SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.9F, 0.7F);
		}
	}

	public static boolean consumeDeathSave(LivingEntity livingEntity) {
		if (!(livingEntity instanceof Mob mob) || mob instanceof BrandBlazeEntity) {
			return false;
		}
		if (!mob.hasEffect(ModEffects.BRAND_MARK)) {
			return false;
		}

		mob.removeEffect(ModEffects.BRAND_MARK);
		mob.setHealth(Math.max(4.0F, mob.getMaxHealth() * 0.35F));
		mob.deathTime = 0;
		mob.hurtTime = 0;
		mob.setPose(Pose.STANDING);
		mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 80, 0));
		mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 1));

		if (mob.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.FLAME,
				mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
				30, 0.45, 0.55, 0.45, 0.02);
			serverLevel.sendParticles(ParticleTypes.LAVA,
				mob.getX(), mob.getY() + 0.2, mob.getZ(),
				10, 0.22, 0.12, 0.22, 0.01);
			serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
				mob.getX(), mob.getY() + mob.getBbHeight() * 0.55, mob.getZ(),
				18, 0.35, 0.45, 0.35, 0.01);
			serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
				SoundEvents.TOTEM_USE, SoundSource.HOSTILE, 0.8F, 0.7F);
			serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
				SoundEvents.BLAZE_AMBIENT, SoundSource.HOSTILE, 0.8F, 0.6F);
		}

		return true;
	}
}
