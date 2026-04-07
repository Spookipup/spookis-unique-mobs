package spookipup.uniquemobs.entity.variant.creeper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

// teleports to you mid-fuse
public class EnderCreeperEntity extends Creeper {

	private boolean hasTeleportedThisSwell;

	public EnderCreeperEntity(EntityType<? extends Creeper> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Creeper.createAttributes();
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
	public boolean hurt(DamageSource source, float amount) {
		boolean hurt = super.hurt(source, amount);
		if (hurt && this.isAlive()) {
			teleportRandomly(8);
		}
		return hurt;
	}

	@Override
	public void tick() {
		if (this.isAlive() && getSwellDir() > 0) {
			// teleport near the target once early in the fuse
			if (!this.hasTeleportedThisSwell && getSwelling(1.0F) > 0.3F) {
				if (!this.level().isClientSide()) {
					LivingEntity target = this.getTarget();
					if (target != null) {
						teleportNearTarget(target);
					}
					this.hasTeleportedThisSwell = true;
				}
			}
		}

		if (getSwellDir() <= 0) {
			this.hasTeleportedThisSwell = false;
		}

		super.tick();
	}

	private void teleportNearTarget(LivingEntity target) {
		for (int i = 0; i < 10; i++) {
			double angle = this.random.nextDouble() * Math.PI * 2;
			double dist = 1.0 + this.random.nextDouble() * 2.0;
			double tx = target.getX() + Math.cos(angle) * dist;
			double tz = target.getZ() + Math.sin(angle) * dist;
			if (this.randomTeleport(tx, target.getY(), tz, true)) {
				this.level().playSound(null, this.xo, this.yo, this.zo,
					SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
				return;
			}
		}
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
