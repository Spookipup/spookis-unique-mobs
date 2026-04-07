package spookipup.uniquemobs.entity.variant.zombie;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// fast + fragile, lunges at you. doesn't burn
public class SprinterZombieEntity extends Zombie {

	private static final double LUNGE_RANGE_SQ = 6.0 * 6.0;
	private static final double LUNGE_STRENGTH = 1.2;
	private int lungeCooldown;

	public SprinterZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes()
			.add(Attributes.MAX_HEALTH, 12.0)
			.add(Attributes.MOVEMENT_SPEED, 0.35)
			.add(Attributes.SCALE, 0.9);
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean hit = super.doHurtTarget(target);
		if (hit && target instanceof LivingEntity) {
			// small knockback boost from the lunge momentum
			Vec3 dir = target.position().subtract(this.position()).normalize();
			target.push(dir.x * 0.3, 0.1, dir.z * 0.3);
			if (target instanceof ServerPlayer) {
				target.hurtMarked = true;
			}
		}
		return hit;
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (!this.level().isClientSide()) {
			if (this.lungeCooldown > 0) {
				this.lungeCooldown--;
			} else {
				LivingEntity target = this.getTarget();
				if (target != null && this.onGround()) {
					double distSq = this.distanceToSqr(target);
					if (distSq <= LUNGE_RANGE_SQ && distSq > 2.0) {
						Vec3 dir = target.position().subtract(this.position()).normalize();
						this.setDeltaMovement(dir.x * LUNGE_STRENGTH, 0.3, dir.z * LUNGE_STRENGTH);
						this.hurtMarked = true;
						this.lungeCooldown = 40;
					}
				}
			}
		}

		if (this.level().isClientSide() && this.isAlive() && this.getDeltaMovement().horizontalDistanceSqr() > 0.01) {
			this.level().addParticle(
				ParticleTypes.CLOUD,
				this.getX(), this.getY() + 0.1, this.getZ(),
				0.0, 0.02, 0.0
			);
		}
	}
}
