package spookipup.uniquemobs.entity.variant.zombie;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class EnderZombieEntity extends Zombie {

	public EnderZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes()
			.add(Attributes.MAX_HEALTH, 24.0);
	}

	@Override
	public float getWalkTargetValue(BlockPos pos, LevelReader level) {
		return 0.0F;
	}

	@Override
	public boolean isSensitiveToWater() {
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
		boolean hurt = super.hurtServer(serverLevel, source, amount);
		if (hurt && this.isAlive()) {
			teleportRandomly(10);
		}
		return hurt;
	}

	private void teleportRandomly(int range) {
		for (int i = 0; i < 10; i++) {
			double tx = this.getX() + (this.random.nextDouble() - 0.5) * 2 * range;
			double ty = this.getY() + (this.random.nextInt(range) - range / 2);
			double tz = this.getZ() + (this.random.nextDouble() - 0.5) * 2 * range;
			if (this.randomTeleport(tx, ty, tz, true)) {
				this.level().playSound(null, this.xo, this.yo, this.zo,
					SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
				return;
			}
		}
	}

	@Override
	public void aiStep() {
		if (!this.level().isClientSide()) {
			if (this.isInWaterOrRain()) {
				this.hurtServer((ServerLevel) this.level(),
					this.damageSources().generic(), 1.0F);
			}

			if (this.level().isBrightOutside() && this.level().canSeeSky(this.blockPosition())
					&& this.random.nextInt(60) == 0) {
				teleportRandomly(16);
			}
		}

		super.aiStep();

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(4) == 0) {
			this.level().addParticle(
				ParticleTypes.PORTAL,
				this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5),
				(this.random.nextDouble() - 0.5) * 0.5,
				this.random.nextDouble() * 0.5,
				(this.random.nextDouble() - 0.5) * 0.5
			);
		}
	}
}
