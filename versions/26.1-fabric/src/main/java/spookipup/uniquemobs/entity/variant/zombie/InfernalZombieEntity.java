package spookipup.uniquemobs.entity.variant.zombie;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;

// fire guy, fights piglins
public class InfernalZombieEntity extends Zombie {

	private static final int FIRE_DURATION_SECONDS = 4;
	private int fireTrailCooldown;

	public InfernalZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		// nether rivalry - hoglins and piglins don't take kindly to fire zombies
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Hoglin.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractPiglin.class, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes()
			.add(Attributes.MOVEMENT_SPEED, 0.25);
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public boolean isSensitiveToWater() {
		return true;
	}

	@Override
	public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
		boolean hit = super.doHurtTarget(serverLevel, target);

		if (hit && target instanceof LivingEntity) {
			target.igniteForSeconds(FIRE_DURATION_SECONDS);
		}

		return hit;
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level() instanceof ServerLevel serverLevel) {
			if (this.fireTrailCooldown > 0) {
				this.fireTrailCooldown--;
			} else if (this.onGround() && this.getTarget() != null
				&& serverLevel.getGameRules().get(GameRules.MOB_GRIEFING)) {
				BlockPos below = this.blockPosition();
				BlockPos firePos = below.above();
				if (this.level().getBlockState(firePos).isAir()
					&& BaseFireBlock.canBePlacedAt(this.level(), firePos, this.getDirection())) {
					this.level().setBlockAndUpdate(firePos, BaseFireBlock.getState(this.level(), firePos));
					this.fireTrailCooldown = 8;
				}
			}
		}

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(3) == 0) {
			this.level().addParticle(
				ParticleTypes.FLAME,
				this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5),
				0.0, 0.02, 0.0
			);
		}
	}
}
