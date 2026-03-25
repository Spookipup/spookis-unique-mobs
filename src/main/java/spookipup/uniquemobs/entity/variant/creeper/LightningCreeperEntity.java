package spookipup.uniquemobs.entity.variant.creeper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

public class LightningCreeperEntity extends Creeper {

	private static final float EXPLOSION_POWER = 2.0F;
	private static final float EXPLOSION_POWER_POWERED = 3.5F;

	public LightningCreeperEntity(EntityType<? extends Creeper> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Creeper.createAttributes();
	}

	@Override
	public void tick() {
		if (this.isAlive() && getSwellDir() > 0 && getSwelling(1.0F) >= 1.0F) {
			if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
				float power = this.isPowered() ? EXPLOSION_POWER_POWERED : EXPLOSION_POWER;
				serverLevel.explode(
					this, this.getX(), this.getY(), this.getZ(),
					power, false, Level.ExplosionInteraction.MOB
				);
				summonLightning(serverLevel);
				this.discard();
			}
			return;
		}

		super.tick();
	}

	private void summonLightning(ServerLevel serverLevel) {
		LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
		if (bolt != null) {
			bolt.moveTo(this.getX(), this.getY(), this.getZ());
			serverLevel.addFreshEntity(bolt);
		}

		// powered version gets a second bolt nearby for extra chaos
		if (this.isPowered()) {
			LightningBolt bolt2 = EntityType.LIGHTNING_BOLT.create(serverLevel);
			if (bolt2 != null) {
				double angle = this.random.nextDouble() * Math.PI * 2;
				bolt2.moveTo(this.getX() + Math.cos(angle) * 3, this.getY(), this.getZ() + Math.sin(angle) * 3);
				serverLevel.addFreshEntity(bolt2);
			}
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(5) == 0) {
			this.level().addParticle(
				ParticleTypes.ELECTRIC_SPARK,
				this.getRandomX(0.4), this.getRandomY(), this.getRandomZ(0.4),
				0.0, 0.02, 0.0
			);
		}
	}
}
