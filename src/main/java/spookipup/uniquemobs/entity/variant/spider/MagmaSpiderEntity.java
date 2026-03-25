package spookipup.uniquemobs.entity.variant.spider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

// lava spider - converts stone to temp magma blocks, sets things on fire
public class MagmaSpiderEntity extends Spider {

	private static final int FIRE_DURATION_SECONDS = 3;
	private static final int MAGMA_REVERT_TICKS = 100;

	private final List<TemporaryMagma> pendingReverts = new ArrayList<>();
	private int magmaCooldown;

	public MagmaSpiderEntity(EntityType<? extends Spider> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Spider.createAttributes()
			.add(Attributes.MAX_HEALTH, 18.0)
			.add(Attributes.ATTACK_DAMAGE, 3.0);
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
	public boolean doHurtTarget(Entity target) {
		boolean hit = super.doHurtTarget(target);

		if (hit && target instanceof LivingEntity) {
			target.setSecondsOnFire(FIRE_DURATION_SECONDS);
		}

		return hit;
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (!this.level().isClientSide()) {
			// revert expired magma blocks with a fire particle burst
			pendingReverts.removeIf(entry -> {
				if (--entry.ticksLeft <= 0) {
					if (this.level().getBlockState(entry.pos).is(Blocks.MAGMA_BLOCK)) {
						this.level().setBlockAndUpdate(entry.pos, entry.original);
						if (this.level() instanceof ServerLevel sl) {
							sl.sendParticles(ParticleTypes.FLAME,
								entry.pos.getX() + 0.5, entry.pos.getY() + 1.0, entry.pos.getZ() + 0.5,
								12, 0.4, 0.3, 0.4, 0.02);
							sl.sendParticles(ParticleTypes.SMOKE,
								entry.pos.getX() + 0.5, entry.pos.getY() + 1.0, entry.pos.getZ() + 0.5,
								6, 0.3, 0.2, 0.3, 0.02);
						}
					}
					return true;
				}
				return false;
			});

			// burn entities standing on our magma (vanilla magma damage doesn't always fire)
			if (this.level() instanceof ServerLevel serverLevel && this.tickCount % 10 == 0) {
				for (TemporaryMagma entry : pendingReverts) {
					if (!serverLevel.getBlockState(entry.pos).is(Blocks.MAGMA_BLOCK)) continue;
					BlockPos above = entry.pos.above();
					for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class,
							new AABB(above).inflate(0.1, 0.5, 0.1))) {
						if (entity == this || entity.fireImmune()) continue;
						entity.hurt(this.damageSources().hotFloor(), 2.0F);
						entity.setSecondsOnFire(2);
					}
				}
			}

			// place magma in a 3x3 under feet
			if (this.magmaCooldown > 0) {
				this.magmaCooldown--;
			} else if (this.onGround()) {
				BlockPos center = this.blockPosition().below();
				boolean placed = false;
				for (int dx = -1; dx <= 1; dx++) {
					for (int dz = -1; dz <= 1; dz++) {
						BlockPos pos = center.offset(dx, 0, dz);
						BlockState state = this.level().getBlockState(pos);
						if (canConvertToMagma(state)) {
							this.level().setBlockAndUpdate(pos, Blocks.MAGMA_BLOCK.defaultBlockState());
							pendingReverts.add(new TemporaryMagma(pos, state, MAGMA_REVERT_TICKS));
							placed = true;
						}
					}
				}
				if (placed) this.magmaCooldown = 8;
			}
		}

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(3) == 0) {
			this.level().addParticle(
				ParticleTypes.LAVA,
				this.getRandomX(0.6), this.getRandomY() - 0.1, this.getRandomZ(0.6),
				0.0, 0.0, 0.0
			);
		}
	}

	private void revertAllMagma() {
		if (!this.level().isClientSide()) {
			for (TemporaryMagma entry : pendingReverts) {
				if (this.level().getBlockState(entry.pos).is(Blocks.MAGMA_BLOCK)) {
					this.level().setBlockAndUpdate(entry.pos, entry.original);
				}
			}
			pendingReverts.clear();
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		revertAllMagma();
		super.remove(reason);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag output) {
		// revert magma before save so blocks don't persist if we never reload
		revertAllMagma();
		super.addAdditionalSaveData(output);
	}

	private static boolean canConvertToMagma(BlockState state) {
		return state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)
			|| state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)
			|| state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)
			|| state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL);
	}

	private static class TemporaryMagma {
		final BlockPos pos;
		final BlockState original;
		int ticksLeft;

		TemporaryMagma(BlockPos pos, BlockState original, int ticksLeft) {
			this.pos = pos;
			this.original = original;
			this.ticksLeft = ticksLeft;
		}
	}
}
