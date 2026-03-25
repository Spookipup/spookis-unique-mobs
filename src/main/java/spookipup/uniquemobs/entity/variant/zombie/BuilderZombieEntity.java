package spookipup.uniquemobs.entity.variant.zombie;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// mines through walls, builds bridges to reach you. collects blocks from what it breaks
public class BuilderZombieEntity extends Zombie {

	private final List<BlockState> inventory = new ArrayList<>();
	private static final int MAX_INVENTORY = 16;
	private final Set<BlockPos> placedBlocks = new HashSet<>();

	public BuilderZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes()
			.add(Attributes.MAX_HEALTH, 30.0)
			.add(Attributes.MOVEMENT_SPEED, 0.2)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
			.add(Attributes.SCALE, 1.2)
			.add(Attributes.ATTACK_DAMAGE, 5.0);
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
		super.populateDefaultEquipmentSlots(random, difficulty);
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new MineBlockGoal(this));
		this.goalSelector.addGoal(1, new BridgeBuildGoal(this));
	}

	public void collectBlock(BlockState minedState) {
		if (this.inventory.size() >= MAX_INVENTORY) return;
		BlockState drop = getDropForm(minedState);
		if (drop != null) {
			this.inventory.add(drop);
		}
	}

	public boolean hasBlocks() {
		return !this.inventory.isEmpty();
	}

	public int getBlockCount() {
		return this.inventory.size();
	}

	public BlockState takeBlock() {
		if (this.inventory.isEmpty()) return null;
		return this.inventory.remove(this.inventory.size() - 1);
	}

	public void trackPlacedBlock(BlockPos pos) {
		this.placedBlocks.add(pos.immutable());
	}

	public boolean isOwnBlock(BlockPos pos) {
		return this.placedBlocks.contains(pos);
	}

	// maps mined blocks to what they'd actually drop, like vanilla
	private static BlockState getDropForm(BlockState state) {
		Block block = state.getBlock();
		if (block == Blocks.STONE) return Blocks.COBBLESTONE.defaultBlockState();
		if (block == Blocks.DEEPSLATE) return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
		if (block == Blocks.GRASS_BLOCK || block == Blocks.ROOTED_DIRT
			|| block == Blocks.MYCELIUM || block == Blocks.PODZOL
			|| block == Blocks.FARMLAND || block == Blocks.DIRT_PATH)
			return Blocks.DIRT.defaultBlockState();
		if (block == Blocks.DIRT || block == Blocks.COARSE_DIRT
			|| block == Blocks.COBBLESTONE || block == Blocks.COBBLED_DEEPSLATE
			|| block == Blocks.NETHERRACK || block == Blocks.TUFF
			|| block == Blocks.ANDESITE || block == Blocks.DIORITE
			|| block == Blocks.GRANITE || block == Blocks.MUD
			|| block == Blocks.SAND || block == Blocks.RED_SAND)
			return state;
		if (block == Blocks.GRAVEL) return Blocks.GRAVEL.defaultBlockState();
		if (block == Blocks.CLAY) return Blocks.CLAY.defaultBlockState();
		return null;
	}

	private static boolean canCollect(BlockState state) {
		return getDropForm(state) != null;
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(8) == 0) {
			this.level().addParticle(
				ParticleTypes.DUST_PLUME,
				this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5),
				0.0, 0.02, 0.0
			);
		}
	}

	private static boolean isMineable(BlockState state, Level level, BlockPos pos) {
		if (state.isAir() || !state.getFluidState().isEmpty()) return false;
		float hardness = state.getDestroySpeed(level, pos);
		return hardness >= 0 && hardness <= 5.0F;
	}

	private static boolean isGap(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		BlockState below = level.getBlockState(pos.below());
		return (state.isAir() || !state.getFluidState().isEmpty())
			&& (below.isAir() || !below.getFluidState().isEmpty());
	}

	private static class MineBlockGoal extends Goal {

		private final BuilderZombieEntity zombie;
		private BlockPos miningPos;
		private int miningTicks;
		private int miningTicksRequired;
		private int stuckTicks;
		private double lastX, lastZ;

		MineBlockGoal(BuilderZombieEntity zombie) {
			this.zombie = zombie;
		}

		@Override
		public boolean canUse() {
			if (this.zombie.level() instanceof ServerLevel sl
				&& !sl.getGameRules().get(GameRules.MOB_GRIEFING)) return false;
			return this.zombie.getTarget() != null && this.zombie.getTarget().isAlive();
		}

		@Override
		public boolean canContinueToUse() {
			return canUse();
		}

		@Override
		public void start() {
			this.stuckTicks = 0;
			this.lastX = this.zombie.getX();
			this.lastZ = this.zombie.getZ();
			this.miningPos = null;
		}

		@Override
		public void stop() {
			clearMining();
			this.stuckTicks = 0;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			double dx = this.zombie.getX() - this.lastX;
			double dz = this.zombie.getZ() - this.lastZ;
			if (dx * dx + dz * dz < 0.04) {
				this.stuckTicks++;
			} else {
				this.stuckTicks = 0;
				this.lastX = this.zombie.getX();
				this.lastZ = this.zombie.getZ();
			}

			if (this.miningPos != null) {
				tickMining();
				return;
			}

			if (this.stuckTicks < 15) return;

			LivingEntity target = this.zombie.getTarget();
			if (target == null) return;

			double toX = target.getX() - this.zombie.getX();
			double toZ = target.getZ() - this.zombie.getZ();
			double len = Math.sqrt(toX * toX + toZ * toZ);
			if (len < 0.1) return;
			toX /= len;
			toZ /= len;

			BlockPos mobPos = this.zombie.blockPosition();
			for (int yOff = 0; yOff <= 1; yOff++) {
				BlockPos checkPos = BlockPos.containing(
					mobPos.getX() + toX * 1.2,
					mobPos.getY() + yOff,
					mobPos.getZ() + toZ * 1.2
				);
				if (tryStartMining(checkPos)) return;
			}
		}

		private boolean tryStartMining(BlockPos pos) {
			Level level = this.zombie.level();
			BlockState state = level.getBlockState(pos);
			if (!isMineable(state, level, pos)) return false;

			float hardness = state.getDestroySpeed(level, pos);
			this.miningTicksRequired = Math.max(4, (int) (hardness * 4));
			this.miningTicks = 0;
			this.miningPos = pos;
			return true;
		}

		private void tickMining() {
			Level level = this.zombie.level();
			BlockState state = level.getBlockState(this.miningPos);
			if (state.isAir() || !isMineable(state, level, this.miningPos)) {
				clearMining();
				return;
			}

			this.zombie.getLookControl().setLookAt(
				this.miningPos.getX() + 0.5, this.miningPos.getY() + 0.5, this.miningPos.getZ() + 0.5);
			this.zombie.swing(InteractionHand.MAIN_HAND);

			this.miningTicks++;
			int progress = (int) ((this.miningTicks / (float) this.miningTicksRequired) * 10);
			progress = Math.min(9, progress);
			level.destroyBlockProgress(this.zombie.getId(), this.miningPos, progress);

			// block hit sound every few ticks like a player mining
			if (this.miningTicks % 4 == 0) {
				SoundType sound = state.getSoundType();
				level.playSound(null, this.miningPos, sound.getHitSound(), SoundSource.HOSTILE,
					(sound.getVolume() + 1.0F) / 8.0F, sound.getPitch() * 0.5F);

				if (level instanceof ServerLevel sl) {
					sl.sendParticles(
						new BlockParticleOption(ParticleTypes.BLOCK, state),
						this.miningPos.getX() + 0.5, this.miningPos.getY() + 0.5, this.miningPos.getZ() + 0.5,
						4, 0.3, 0.3, 0.3, 0.01
					);
				}
			}

			if (this.miningTicks >= this.miningTicksRequired) {
				if (canCollect(state)) {
					this.zombie.collectBlock(state);
				}
				this.zombie.placedBlocks.remove(this.miningPos);
				level.destroyBlock(this.miningPos, false, this.zombie);
				clearMining();
				this.stuckTicks = 0;
			}
		}

		private void clearMining() {
			if (this.miningPos != null) {
				this.zombie.level().destroyBlockProgress(this.zombie.getId(), this.miningPos, -1);
				this.miningPos = null;
			}
			this.miningTicks = 0;
		}
	}

	private static class BridgeBuildGoal extends Goal {

		private enum BuildState { IDLE, GATHERING, BUILDING }

		private final BuilderZombieEntity zombie;
		private int buildCooldown;
		private int stuckTicks;
		private double lastX, lastZ;

		private BuildState buildState = BuildState.IDLE;
		private int blocksNeeded;
		private boolean needsTower;

		BridgeBuildGoal(BuilderZombieEntity zombie) {
			this.zombie = zombie;
		}

		@Override
		public boolean canUse() {
			if (this.zombie.level() instanceof ServerLevel sl
				&& !sl.getGameRules().get(GameRules.MOB_GRIEFING)) return false;
			return this.zombie.getTarget() != null && this.zombie.getTarget().isAlive();
		}

		@Override
		public boolean canContinueToUse() {
			return canUse();
		}

		@Override
		public void start() {
			this.stuckTicks = 0;
			this.lastX = this.zombie.getX();
			this.lastZ = this.zombie.getZ();
			this.buildState = BuildState.IDLE;
		}

		@Override
		public void stop() {
			this.stuckTicks = 0;
			this.buildState = BuildState.IDLE;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			if (this.buildCooldown > 0) {
				this.buildCooldown--;
				return;
			}

			double dx = this.zombie.getX() - this.lastX;
			double dz = this.zombie.getZ() - this.lastZ;
			if (dx * dx + dz * dz < 0.04) {
				this.stuckTicks++;
			} else {
				this.stuckTicks = 0;
				this.lastX = this.zombie.getX();
				this.lastZ = this.zombie.getZ();
				if (this.buildState == BuildState.IDLE) return;
			}

			switch (this.buildState) {
				case IDLE -> tickIdle();
				case GATHERING -> tickGathering();
				case BUILDING -> tickBuilding();
			}
		}

		private void tickIdle() {
			if (this.stuckTicks < 10) return;

			LivingEntity target = this.zombie.getTarget();
			if (target == null) return;

			Level level = this.zombie.level();
			BlockPos mobPos = this.zombie.blockPosition();

			double heightDiff = target.getY() - this.zombie.getY();
			if (heightDiff > 1.5 && this.zombie.onGround()) {
				int towerHeight = Math.min(8, (int) Math.ceil(heightDiff));
				this.blocksNeeded = towerHeight;
				this.needsTower = true;
				transitionToBuildOrGather();
				return;
			}

			double toX = target.getX() - this.zombie.getX();
			double toZ = target.getZ() - this.zombie.getZ();
			double len = Math.sqrt(toX * toX + toZ * toZ);
			if (len < 0.1) return;
			toX /= len;
			toZ /= len;

			int gapLength = scanGapLength(level, mobPos, toX, toZ);
			if (gapLength > 0) {
				this.blocksNeeded = gapLength;
				this.needsTower = false;
				transitionToBuildOrGather();
			}
		}

		private void transitionToBuildOrGather() {
			if (this.zombie.getBlockCount() >= this.blocksNeeded) {
				this.buildState = BuildState.BUILDING;
			} else {
				this.buildState = BuildState.GATHERING;
			}
		}

		private void tickGathering() {
			if (this.zombie.getBlockCount() >= this.blocksNeeded) {
				this.buildState = BuildState.BUILDING;
				return;
			}

			Level level = this.zombie.level();
			BlockPos center = this.zombie.blockPosition();

			for (int r = 1; r <= 3; r++) {
				for (int dx = -r; dx <= r; dx++) {
					for (int dy = -1; dy <= 1; dy++) {
						for (int dz = -r; dz <= r; dz++) {
							if (Math.abs(dx) < r && Math.abs(dz) < r) continue;
							BlockPos pos = center.offset(dx, dy, dz);
							if (this.zombie.isOwnBlock(pos)) continue;
							BlockState state = level.getBlockState(pos);
							if (canCollect(state) && isMineable(state, level, pos)) {
								if (level.getBlockState(pos.above()).isAir()) {
									this.zombie.collectBlock(state);
									level.destroyBlock(pos, false, this.zombie);
									this.zombie.playSound(SoundEvents.GRAVEL_BREAK, 0.7F, 1.0F);
									this.buildCooldown = 6;
									return;
								}
							}
						}
					}
				}
			}

			if (this.zombie.hasBlocks()) {
				this.buildState = BuildState.BUILDING;
			} else {
				this.buildState = BuildState.IDLE;
				this.stuckTicks = 0;
			}
		}

		private void tickBuilding() {
			if (!this.zombie.hasBlocks()) {
				this.buildState = BuildState.IDLE;
				this.stuckTicks = 0;
				return;
			}

			Level level = this.zombie.level();
			BlockPos mobPos = this.zombie.blockPosition();

			if (this.needsTower) {
				if (!tryTower(level, mobPos)) {
					this.buildState = BuildState.IDLE;
				}
			} else {
				LivingEntity target = this.zombie.getTarget();
				if (target == null) {
					this.buildState = BuildState.IDLE;
					return;
				}

				double toX = target.getX() - this.zombie.getX();
				double toZ = target.getZ() - this.zombie.getZ();
				double len = Math.sqrt(toX * toX + toZ * toZ);
				if (len < 0.1) { this.buildState = BuildState.IDLE; return; }
				toX /= len;
				toZ /= len;

				BlockPos ahead = BlockPos.containing(
					mobPos.getX() + toX * 1.5,
					mobPos.getY(),
					mobPos.getZ() + toZ * 1.5
				);

				boolean placed = false;
				if (isGap(level, ahead)) {
					placed = tryPlaceBlock(level, ahead.below());
				}
				if (!placed && isGap(level, mobPos)) {
					placed = tryPlaceBlock(level, mobPos.below());
				}
				if (!placed) {
					this.buildState = BuildState.IDLE;
					this.stuckTicks = 0;
				}
			}
		}

		private int scanGapLength(Level level, BlockPos mobPos, double dirX, double dirZ) {
			int count = 0;
			for (int i = 1; i <= 8; i++) {
				BlockPos check = BlockPos.containing(
					mobPos.getX() + dirX * i,
					mobPos.getY(),
					mobPos.getZ() + dirZ * i
				);
				if (isGap(level, check)) {
					count++;
				} else {
					break;
				}
			}
			return count;
		}

		private boolean tryTower(Level level, BlockPos mobPos) {
			BlockState feetState = level.getBlockState(mobPos);
			if (!feetState.isAir() && !feetState.canBeReplaced()) return false;
			if (!level.getBlockState(mobPos.above()).isAir()
				|| !level.getBlockState(mobPos.above(2)).isAir()) return false;

			BlockState toPlace = this.zombie.takeBlock();
			if (toPlace == null) return false;

			level.setBlockAndUpdate(mobPos, toPlace);
			this.zombie.trackPlacedBlock(mobPos);
			playPlaceSound(level, mobPos, toPlace);
			this.zombie.jumpFromGround();
			this.buildCooldown = 8;
			this.blocksNeeded--;
			if (this.blocksNeeded <= 0) {
				this.buildState = BuildState.IDLE;
				this.stuckTicks = 0;
			}
			return true;
		}

		private boolean tryPlaceBlock(Level level, BlockPos pos) {
			BlockState state = level.getBlockState(pos);
			if (!state.isAir() && state.getFluidState().isEmpty()) return false;

			BlockState toPlace = this.zombie.takeBlock();
			if (toPlace == null) return false;

			level.setBlockAndUpdate(pos, toPlace);
			this.zombie.trackPlacedBlock(pos);
			playPlaceSound(level, pos, toPlace);
			this.buildCooldown = 6;
			this.stuckTicks = 0;
			this.blocksNeeded--;
			if (this.blocksNeeded <= 0) {
				this.buildState = BuildState.IDLE;
				this.stuckTicks = 0;
			}
			return true;
		}

		private void playPlaceSound(Level level, BlockPos pos, BlockState state) {
			SoundType sound = state.getSoundType();
			level.playSound(null, pos, sound.getPlaceSound(), SoundSource.HOSTILE,
				(sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
		}
	}
}
