package spookipup.uniquemobs.entity.variant.zombie;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

// freezes water, applies slowness + freeze on hit
public class FrozenZombieEntity extends Zombie {

	private static final int SLOWNESS_DURATION = 80;
	private static final int FREEZE_TICKS_ON_HIT = 140;
	private int frostWalkerCooldown;

	public FrozenZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes()
			.add(Attributes.MAX_HEALTH, 24.0)
			.add(Attributes.MOVEMENT_SPEED, 0.2);
	}

	@Override
	public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
		boolean hit = super.doHurtTarget(serverLevel, target);

		if (hit && target instanceof LivingEntity livingTarget) {
			livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_DURATION, 1));
			int newFreeze = Math.min(livingTarget.getTicksFrozen() + FREEZE_TICKS_ON_HIT,
				livingTarget.getTicksRequiredToFreeze() + 40);
			livingTarget.setTicksFrozen(newFreeze);
		}

		return hit;
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level() instanceof ServerLevel serverLevel) {
			// frost walker - freeze water around feet
			if (this.frostWalkerCooldown > 0) {
				this.frostWalkerCooldown--;
			} else if (this.onGround()
				&& serverLevel.getGameRules().get(GameRules.MOB_GRIEFING)) {
				freezeNearbyWater();
				this.frostWalkerCooldown = 3;
			}
		}

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(4) == 0) {
			this.level().addParticle(
				ParticleTypes.SNOWFLAKE,
				this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5),
				0.0, -0.05, 0.0
			);
		}
	}

	private void freezeNearbyWater() {
		BlockPos center = this.blockPosition();
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (dx * dx + dz * dz > 4) continue;
				BlockPos pos = center.offset(dx, -1, dz);
				BlockState state = this.level().getBlockState(pos);
				FluidState fluid = state.getFluidState();
				if (fluid.is(Fluids.WATER) && state.is(Blocks.WATER)
					&& this.level().getBlockState(pos.above()).isAir()) {
					this.level().setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
				}
			}
		}
	}
}
