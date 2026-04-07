package spookipup.uniquemobs.entity.variant.blaze;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import spookipup.uniquemobs.entity.ai.SoulBlazeAttackGoal;
import spookipup.uniquemobs.registry.ModEffects;

public class SoulBlazeEntity extends Blaze {

	public SoulBlazeEntity(EntityType<? extends Blaze> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Blaze.createAttributes()
			.add(Attributes.MAX_HEALTH, 22.0)
			.add(Attributes.ATTACK_DAMAGE, 4.0)
			.add(Attributes.FOLLOW_RANGE, 36.0)
			.add(Attributes.MOVEMENT_SPEED, 0.26);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new SoulBlazeAttackGoal(this));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public boolean canBeAffected(MobEffectInstance effect) {
		if (effect.is(ModEffects.SOUL_SCORCH)) return false;
		return super.canBeAffected(effect);
	}

	@Override
	public void tick() {
		super.tick();

		if (this.getTarget() instanceof Player player && (player.isCreative() || player.isSpectator())) {
			this.setTarget(null);
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(3) == 0) {
			this.level().addParticle(
				ParticleTypes.SOUL_FIRE_FLAME,
				this.getRandomX(0.45), this.getRandomY(), this.getRandomZ(0.45),
				0.0, 0.01, 0.0
			);
			if (this.random.nextBoolean()) {
				this.level().addParticle(
					ParticleTypes.SOUL,
					this.getRandomX(0.5), this.getRandomY() - 0.1, this.getRandomZ(0.5),
					0.0, 0.01, 0.0
				);
			}
		}
	}
}
