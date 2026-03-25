package spookipup.uniquemobs.entity.variant.creeper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

// ice explosion, bigger radius in cold biomes
public class FrostCreeperEntity extends Creeper {

	private static final int SLOWNESS_DURATION = 120;
	private static final int FREEZE_TICKS = 200;
	private int frostWalkerCooldown;

	public FrostCreeperEntity(EntityType<? extends Creeper> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Creeper.createAttributes();
	}

	@Override
	public void tick() {
		if (this.isAlive() && getSwellDir() > 0 && getSwelling(1.0F) >= 1.0F) {
			if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
				iceExplosion(serverLevel);
				this.discard();
			}
			return;
		}

		super.tick();
	}

	private void iceExplosion(ServerLevel serverLevel) {
		// cold biomes = bigger blast
		float biomeTemp = serverLevel.getBiome(this.blockPosition()).value().getBaseTemperature();
		int baseRadius = this.isPowered() ? 6 : 4;
		// cold biomes (< 0.2) get up to +4 radius, warm biomes get no bonus
		int tempBonus = biomeTemp < 0.2F ? 4 : biomeTemp < 0.5F ? 2 : 0;
		int radius = baseRadius + tempBonus;

		this.playSound(SoundEvents.GLASS_BREAK, 2.0F, 0.5F);
		this.playSound(SoundEvents.POWDER_SNOW_BREAK, 2.0F, 0.8F);

		BlockPos center = this.blockPosition();

		// convert blocks in a rough sphere
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -radius / 2; dy <= radius / 2; dy++) {
				for (int dz = -radius; dz <= radius; dz++) {
					double distSq = dx * dx + dy * dy * 4 + dz * dz;
					if (distSq > radius * radius) continue;

					// random falloff at the edges for a chaotic look
					if (distSq > (radius - 2) * (radius - 2) && this.random.nextFloat() < 0.5F) continue;

					BlockPos pos = center.offset(dx, dy, dz);
					BlockState state = serverLevel.getBlockState(pos);

					if (state.isAir() || state.getFluidState().is(Fluids.WATER)) {
						serverLevel.setBlockAndUpdate(pos, pickIceBlock(distSq, radius));
					} else {
						float hardness = state.getDestroySpeed(serverLevel, pos);
						if (hardness >= 0 && hardness <= 2.0F) {
							serverLevel.setBlockAndUpdate(pos, pickIceBlock(distSq, radius));
						}
					}
				}
			}
		}

		// particles
		serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
			this.getX(), this.getY() + 1, this.getZ(),
			80, radius * 0.5, radius * 0.3, radius * 0.5, 0.1);
		serverLevel.sendParticles(ParticleTypes.POOF,
			this.getX(), this.getY() + 1, this.getZ(),
			30, radius * 0.4, radius * 0.2, radius * 0.4, 0.05);

		// debuff nearby entities
		float effectRange = radius + 2.0F;
		for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class,
				this.getBoundingBox().inflate(effectRange))) {
			if (entity == this) continue;
			entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_DURATION, 2));
			int newFreeze = Math.min(entity.getTicksFrozen() + FREEZE_TICKS,
				entity.getTicksRequiredToFreeze() + 60);
			entity.setTicksFrozen(newFreeze);
		}
	}

	private BlockState pickIceBlock(double distSq, int radius) {
		// center = packed ice and powdered snow, edges = regular ice and snow
		double ratio = distSq / (radius * radius);
		float roll = this.random.nextFloat();

		if (ratio < 0.3) {
			// inner core - heavier blocks
			if (roll < 0.35F) return Blocks.PACKED_ICE.defaultBlockState();
			if (roll < 0.6F) return Blocks.POWDER_SNOW.defaultBlockState();
			if (roll < 0.85F) return Blocks.ICE.defaultBlockState();
			return Blocks.SNOW_BLOCK.defaultBlockState();
		} else if (ratio < 0.7) {
			// mid range
			if (roll < 0.3F) return Blocks.ICE.defaultBlockState();
			if (roll < 0.55F) return Blocks.SNOW_BLOCK.defaultBlockState();
			if (roll < 0.75F) return Blocks.POWDER_SNOW.defaultBlockState();
			return Blocks.PACKED_ICE.defaultBlockState();
		} else {
			// outer edge - lighter blocks
			if (roll < 0.4F) return Blocks.SNOW_BLOCK.defaultBlockState();
			if (roll < 0.7F) return Blocks.ICE.defaultBlockState();
			if (roll < 0.85F) return Blocks.POWDER_SNOW.defaultBlockState();
			return Blocks.PACKED_ICE.defaultBlockState();
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (!this.level().isClientSide()) {
			if (this.frostWalkerCooldown > 0) {
				this.frostWalkerCooldown--;
			} else if (this.onGround()) {
				freezeNearbyWater();
				this.frostWalkerCooldown = 3;
			}
		}

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(4) == 0) {
			this.level().addParticle(
				ParticleTypes.SNOWFLAKE,
				this.getRandomX(0.4), this.getRandomY(), this.getRandomZ(0.4),
				0.0, -0.04, 0.0
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
				if (state.getFluidState().is(Fluids.WATER) && state.is(Blocks.WATER)
					&& this.level().getBlockState(pos.above()).isAir()) {
					this.level().setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
				}
			}
		}
	}
}
