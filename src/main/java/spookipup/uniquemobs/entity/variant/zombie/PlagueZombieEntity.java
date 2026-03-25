package spookipup.uniquemobs.entity.variant.zombie;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

// wither aura on animals, buffs other zombies when it dies
public class PlagueZombieEntity extends Zombie {

	private static final double AURA_RANGE = 6.0;
	private static final int AURA_INTERVAL = 40;
	private static final double DEATH_BUFF_RANGE = 10.0;

	private int auraCooldown;

	public PlagueZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes()
			.add(Attributes.MAX_HEALTH, 24.0)
			.add(Attributes.MOVEMENT_SPEED, 0.22);
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean hit = super.doHurtTarget(target);
		if (hit && target instanceof LivingEntity living) {
			living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 1));
			living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
		}
		return hit;
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (!this.level().isClientSide()) {
			if (this.auraCooldown > 0) {
				this.auraCooldown--;
			} else {
				this.auraCooldown = AURA_INTERVAL;
				applyWitherAura();
			}
		}

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(3) == 0) {
			this.level().addParticle(
				ParticleTypes.SPORE_BLOSSOM_AIR,
				this.getRandomX(0.8), this.getRandomY(), this.getRandomZ(0.8),
				0.0, -0.03, 0.0
			);
		}
	}

	private void applyWitherAura() {
		AABB auraBox = this.getBoundingBox().inflate(AURA_RANGE);
		List<Animal> animals = this.level().getEntitiesOfClass(Animal.class, auraBox);
		for (Animal animal : animals) {
			animal.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
		}

		if (this.level() instanceof ServerLevel sl) {
			sl.sendParticles(
				ParticleTypes.SPORE_BLOSSOM_AIR,
				this.getX(), this.getY() + 1.0, this.getZ(),
				12, AURA_RANGE * 0.4, 1.0, AURA_RANGE * 0.4, 0.01
			);
		}
	}

	@Override
	public void die(DamageSource damageSource) {
		super.die(damageSource);

		if (!this.level().isClientSide()) {
			AABB buffBox = this.getBoundingBox().inflate(DEATH_BUFF_RANGE);
			List<Zombie> zombies = this.level().getEntitiesOfClass(Zombie.class, buffBox,
				z -> z != this && z.isAlive());
			for (Zombie zombie : zombies) {
				zombie.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
				zombie.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
			}

			if (this.level() instanceof ServerLevel sl) {
				sl.sendParticles(
					ParticleTypes.SPORE_BLOSSOM_AIR,
					this.getX(), this.getY() + 1.0, this.getZ(),
					30, 2.0, 1.5, 2.0, 0.05
				);
			}
		}
	}
}
