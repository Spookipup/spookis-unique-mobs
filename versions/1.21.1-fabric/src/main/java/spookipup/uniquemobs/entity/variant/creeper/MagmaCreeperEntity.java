package spookipup.uniquemobs.entity.variant.creeper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

// converts blocks to magma on detonation
public class MagmaCreeperEntity extends Creeper {

	private static final int FIRE_DURATION_SECONDS = 6;

	public MagmaCreeperEntity(EntityType<? extends Creeper> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Creeper.createAttributes();
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public void tick() {
		if (this.isAlive() && getSwellDir() > 0 && getSwelling(1.0F) >= 1.0F) {
			if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
				magmaExplosion(serverLevel);
				this.discard();
			}
			return;
		}

		super.tick();
	}

	private void magmaExplosion(ServerLevel serverLevel) {
		int baseRadius = this.isPowered() ? 6 : 4;

		float power = this.isPowered() ? 3.5F : 2.5F;
		serverLevel.explode(
			this, this.getX(), this.getY(), this.getZ(),
			power, true, Level.ExplosionInteraction.MOB
		);

		BlockPos center = this.blockPosition();

		for (int dx = -baseRadius; dx <= baseRadius; dx++) {
			for (int dy = -baseRadius / 2; dy <= baseRadius / 2; dy++) {
				for (int dz = -baseRadius; dz <= baseRadius; dz++) {
					double distSq = dx * dx + dy * dy * 4 + dz * dz;
					if (distSq > baseRadius * baseRadius) continue;

					if (distSq > (baseRadius - 2) * (baseRadius - 2) && this.random.nextFloat() < 0.4F) continue;

					BlockPos pos = center.offset(dx, dy, dz);
					BlockState state = serverLevel.getBlockState(pos);

					if (isConvertibleToMagma(state)) {
						serverLevel.setBlockAndUpdate(pos, Blocks.MAGMA_BLOCK.defaultBlockState());
					} else if (isConvertibleToSoulSand(state)) {
						serverLevel.setBlockAndUpdate(pos, Blocks.SOUL_SAND.defaultBlockState());
					} else if (state.isAir()) {
						BlockPos below = pos.below();
						if (!serverLevel.getBlockState(below).isAir() && this.random.nextFloat() < 0.6F) {
							serverLevel.setBlockAndUpdate(pos, BaseFireBlock.getState(serverLevel, pos));
						}
					}
				}
			}
		}

		// particles
		serverLevel.sendParticles(ParticleTypes.LAVA,
			this.getX(), this.getY() + 1, this.getZ(),
			60, baseRadius * 0.5, baseRadius * 0.3, baseRadius * 0.5, 0.1);
		serverLevel.sendParticles(ParticleTypes.FLAME,
			this.getX(), this.getY() + 1, this.getZ(),
			80, baseRadius * 0.4, baseRadius * 0.3, baseRadius * 0.4, 0.15);
		serverLevel.sendParticles(ParticleTypes.SMOKE,
			this.getX(), this.getY() + 1, this.getZ(),
			40, baseRadius * 0.3, baseRadius * 0.2, baseRadius * 0.3, 0.05);

		// ignite nearby entities
		float effectRange = baseRadius + 2.0F;
		for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class,
				this.getBoundingBox().inflate(effectRange))) {
			if (entity == this) continue;
			entity.igniteForSeconds(FIRE_DURATION_SECONDS);
		}
	}

	private static boolean isConvertibleToMagma(BlockState state) {
		return state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)
			|| state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)
			|| state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)
			|| state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SOIL);
	}

	private static boolean isConvertibleToSoulSand(BlockState state) {
		return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND);
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(3) == 0) {
			this.level().addParticle(
				ParticleTypes.LAVA,
				this.getRandomX(0.4), this.getRandomY(), this.getRandomZ(0.4),
				0.0, 0.0, 0.0
			);
		}
	}
}
