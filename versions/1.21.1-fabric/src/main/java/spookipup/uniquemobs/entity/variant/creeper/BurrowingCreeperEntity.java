package spookipup.uniquemobs.entity.variant.creeper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

// digs underground, tunnels behind you, surfaces and blows up
public class BurrowingCreeperEntity extends Creeper {

	private enum BurrowState {
		SURFACE,
		BURROWING_DOWN,
		UNDERGROUND,
		BURROWING_UP,
		EXPOSED
	}

	private static final int EXPOSED_TIMEOUT = 60;
	private static final int CRACK_RADIUS = 2;

	private BurrowState burrowState = BurrowState.SURFACE;
	private int burrowTimer;
	private int undergroundTicks;
	private int exposedTicks;
	private BlockPos burrowTarget;
	private Vec3 undergroundPos;
	private Vec3 sinkStartPos;
	private int meanderTimer;
	private double meanderOffsetX, meanderOffsetZ;
	private final Set<BlockPos> activeCracks = new HashSet<>();

	public BurrowingCreeperEntity(EntityType<? extends Creeper> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Creeper.createAttributes()
			.add(Attributes.MOVEMENT_SPEED, 0.22);
	}

	@Override
	public void tick() {
		if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
			switch (this.burrowState) {
				case SURFACE -> tickSurface(serverLevel);
				case BURROWING_DOWN -> tickBurrowingDown(serverLevel);
				case UNDERGROUND -> tickUnderground(serverLevel);
				case BURROWING_UP -> tickBurrowingUp(serverLevel);
				case EXPOSED -> tickExposed(serverLevel);
			}
		}

		if (this.burrowState == BurrowState.SURFACE || this.burrowState == BurrowState.EXPOSED) {
			super.tick();
		} else if (this.burrowState == BurrowState.BURROWING_DOWN || this.burrowState == BurrowState.BURROWING_UP) {
			this.tickCount++;
			this.baseTick();
		} else {
			this.tickCount++;
		}
	}

	private void tickSurface(ServerLevel serverLevel) {
		LivingEntity target = this.getTarget();
		if (target != null && this.getSensing().hasLineOfSight(target)) {
			BlockPos below = this.blockPosition().below();
			if (isSoftBlock(serverLevel.getBlockState(below))) {
				startBurrowing();
			}
		}
	}

	private void startBurrowing() {
		this.burrowState = BurrowState.BURROWING_DOWN;
		this.burrowTimer = 25;
		this.sinkStartPos = this.position();
		this.setNoGravity(true);
		this.playSound(SoundEvents.GRAVEL_BREAK, 1.0F, 0.8F);
	}

	private void tickBurrowingDown(ServerLevel serverLevel) {
		this.burrowTimer--;

		double sinkProgress = 1.0 - (this.burrowTimer / 25.0);
		this.setPos(this.sinkStartPos.x, this.sinkStartPos.y - sinkProgress * 1.8, this.sinkStartPos.z);

		updateCrackOverlays(serverLevel, this.position(), sinkProgress);

		if (this.burrowTimer % 3 == 0) {
			spawnDigParticles(serverLevel, BlockPos.containing(this.sinkStartPos));
		}

		if (sinkProgress > 0.6 && !this.isInvisible()) {
			this.setInvisible(true);
		}

		if (this.burrowTimer <= 0) {
			clearAllCracks();
			this.burrowState = BurrowState.UNDERGROUND;
			this.undergroundTicks = 0;
			this.undergroundPos = new Vec3(this.sinkStartPos.x, this.sinkStartPos.y - 3.0, this.sinkStartPos.z);
			this.setPos(this.undergroundPos.x, this.undergroundPos.y, this.undergroundPos.z);
			this.setInvisible(true);
			this.meanderTimer = 0;
		}
	}

	private void tickUnderground(ServerLevel serverLevel) {
		this.undergroundTicks++;

		LivingEntity target = this.getTarget();
		if (target == null || !target.isAlive()) {
			clearAllCracks();
			this.burrowTarget = findSurfaceAbove(serverLevel, BlockPos.containing(this.undergroundPos));
			beginSurfacing(serverLevel);
			return;
		}

		// figure out where "behind" the player is
		Vec3 lookDir = target.getLookAngle();
		Vec3 behindPlayer = target.position().subtract(lookDir.x * 4, 0, lookDir.z * 4);

		this.meanderTimer--;
		if (this.meanderTimer <= 0) {
			this.meanderOffsetX = (this.random.nextDouble() - 0.5) * 6.0;
			this.meanderOffsetZ = (this.random.nextDouble() - 0.5) * 6.0;
			this.meanderTimer = 20 + this.random.nextInt(20);
		}

		// target Y follows the terrain - try to stay 2-3 blocks below surface
		BlockPos surfaceHere = findSurfaceAbove(serverLevel, BlockPos.containing(
			this.undergroundPos.x + this.meanderOffsetX * 0.1,
			this.undergroundPos.y,
			this.undergroundPos.z + this.meanderOffsetZ * 0.1
		));
		double targetY = surfaceHere != null ? surfaceHere.getY() - 3.0 : this.undergroundPos.y;

		Vec3 wanderTarget = new Vec3(
			behindPlayer.x + this.meanderOffsetX,
			targetY,
			behindPlayer.z + this.meanderOffsetZ
		);

		Vec3 toTarget = wanderTarget.subtract(this.undergroundPos);
		double totalDist = toTarget.length();
		if (totalDist > 0.01) {
			Vec3 dir = toTarget.normalize();
			double speed = 0.15;
			// allow vertical movement but clamp so it doesn't move too steeply
			double dy = Math.max(-0.1, Math.min(0.1, dir.y * speed));
			this.undergroundPos = this.undergroundPos.add(dir.x * speed, dy, dir.z * speed);
		}
		this.setPos(this.undergroundPos.x, this.undergroundPos.y, this.undergroundPos.z);

		updateCrackOverlays(serverLevel, this.undergroundPos, 1.0);

		// particles on the surface above
		BlockPos surfacePos = findSurfaceAbove(serverLevel, BlockPos.containing(this.undergroundPos));
		if (surfacePos != null && this.undergroundTicks % 4 == 0) {
			spawnDigParticles(serverLevel, surfacePos);
			if (this.random.nextInt(8) == 0) {
				serverLevel.playSound(null, surfacePos,
					SoundEvents.GRAVEL_STEP, this.getSoundSource(), 0.6F, 0.6F);
			}
		}

		// close enough to behind the player - surface
		double hDistSq = horizontalDistSq(this.undergroundPos, behindPlayer);
		if (hDistSq < 3.0 * 3.0 && this.undergroundTicks > 30) {
			this.burrowTarget = surfacePos;
			if (this.burrowTarget != null) {
				clearAllCracks();
				beginSurfacing(serverLevel);
			}
		}

	}

	private void beginSurfacing(ServerLevel serverLevel) {
		this.burrowState = BurrowState.BURROWING_UP;
		this.burrowTimer = 20;
		if (this.burrowTarget == null) {
			this.burrowTarget = this.blockPosition().above(3);
		}
		this.playSound(SoundEvents.GRAVEL_BREAK, 0.8F, 1.0F);
	}

	private void tickBurrowingUp(ServerLevel serverLevel) {
		this.burrowTimer--;

		double riseProgress = 1.0 - (this.burrowTimer / 20.0);
		double targetY = this.burrowTarget.getY();
		this.setPos(
			this.burrowTarget.getX() + 0.5,
			targetY - 1.8 + riseProgress * 1.8,
			this.burrowTarget.getZ() + 0.5
		);

		// cracking around the emergence point
		updateCrackOverlays(serverLevel, this.position(), 1.0 - riseProgress);

		if (riseProgress > 0.3 && this.isInvisible()) {
			this.setInvisible(false);
		}

		if (this.burrowTimer % 3 == 0) {
			spawnDigParticles(serverLevel, this.burrowTarget);
		}

		if (this.burrowTimer <= 0) {
			clearAllCracks();
			this.burrowState = BurrowState.EXPOSED;
			this.exposedTicks = 0;
			this.setPos(this.burrowTarget.getX() + 0.5, targetY, this.burrowTarget.getZ() + 0.5);
			this.setNoGravity(false);
			this.setInvisible(false);
			this.playSound(SoundEvents.GRAVEL_BREAK, 1.2F, 0.6F);
		}
	}

	private void tickExposed(ServerLevel serverLevel) {
		this.exposedTicks++;

		if (this.getSwellDir() > 0) {
			return;
		}

		if (this.exposedTicks > EXPOSED_TIMEOUT) {
			LivingEntity target = this.getTarget();
			if (target != null && target.isAlive()) {
				BlockPos below = this.blockPosition().below();
				if (isSoftBlock(serverLevel.getBlockState(below))) {
					startBurrowing();
					return;
				}
			}
			this.burrowState = BurrowState.SURFACE;
		}
	}

	// block cracking overlay system

	private void updateCrackOverlays(ServerLevel serverLevel, Vec3 center, double intensity) {
		Set<BlockPos> newCracks = new HashSet<>();
		BlockPos blockCenter = BlockPos.containing(center);

		for (int dx = -CRACK_RADIUS; dx <= CRACK_RADIUS; dx++) {
			for (int dy = -CRACK_RADIUS; dy <= CRACK_RADIUS; dy++) {
				for (int dz = -CRACK_RADIUS; dz <= CRACK_RADIUS; dz++) {
					BlockPos pos = blockCenter.offset(dx, dy, dz);
					BlockState state = serverLevel.getBlockState(pos);
					if (state.isAir() || state.getFluidState().isSource()) continue;

					double distSq = pos.getCenter().distanceToSqr(center);
					double maxDistSq = (CRACK_RADIUS + 0.5) * (CRACK_RADIUS + 0.5);
					if (distSq > maxDistSq) continue;

					// closer blocks = more damage, scaled by intensity
					double ratio = 1.0 - (distSq / maxDistSq);
					int progress = (int) (ratio * intensity * 9);
					progress = Math.max(0, Math.min(9, progress));

					// use entity ID + offset so each block gets its own overlay
					int crackId = this.getId() + pos.hashCode();
					serverLevel.destroyBlockProgress(crackId, pos, progress);
					newCracks.add(pos);
				}
			}
		}

		// clear cracks that are no longer in range
		for (BlockPos old : activeCracks) {
			if (!newCracks.contains(old)) {
				int crackId = this.getId() + old.hashCode();
				serverLevel.destroyBlockProgress(crackId, old, -1);
			}
		}

		activeCracks.clear();
		activeCracks.addAll(newCracks);
	}

	private void clearAllCracks() {
		if (!this.level().isClientSide()) {
			for (BlockPos pos : activeCracks) {
				int crackId = this.getId() + pos.hashCode();
				this.level().destroyBlockProgress(crackId, pos, -1);
			}
			activeCracks.clear();
		}
	}

	// particles

	private void spawnDigParticles(ServerLevel serverLevel, BlockPos surfacePos) {
		BlockPos belowPos = surfacePos.below();
		BlockState groundBlock = serverLevel.getBlockState(belowPos);
		if (groundBlock.isAir()) {
			groundBlock = serverLevel.getBlockState(surfacePos);
			belowPos = surfacePos;
		}
		if (!groundBlock.isAir()) {
			double topY = belowPos.getY() + groundBlock.getShape(serverLevel, belowPos).max(Direction.Axis.Y);
			serverLevel.sendParticles(
				new BlockParticleOption(ParticleTypes.BLOCK, groundBlock),
				surfacePos.getX() + 0.5, topY + 0.05, surfacePos.getZ() + 0.5,
				8, 0.4, 0.05, 0.4, 0.02
			);
		}
	}

	// helpers

	private BlockPos findSurfaceAbove(ServerLevel level, BlockPos underground) {
		BlockPos.MutableBlockPos pos = underground.mutable();
		for (int y = 0; y < 10; y++) {
			pos.setY(underground.getY() + y);
			BlockState state = level.getBlockState(pos);
			BlockState below = level.getBlockState(pos.below());
			if (state.isAir() && !below.isAir()) {
				return pos.immutable();
			}
		}
		return null;
	}

	private static boolean isSoftBlock(BlockState state) {
		return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)
			|| state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)
			|| state.is(Blocks.GRAVEL) || state.is(Blocks.CLAY)
			|| state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT)
			|| state.is(Blocks.MUD) || state.is(Blocks.SOUL_SAND)
			|| state.is(Blocks.SOUL_SOIL) || state.is(Blocks.MYCELIUM)
			|| state.is(Blocks.PODZOL) || state.is(Blocks.FARMLAND)
			|| state.is(Blocks.DIRT_PATH) || state.is(Blocks.STONE)
			|| state.is(Blocks.DEEPSLATE) || state.is(Blocks.NETHERRACK)
			|| state.is(Blocks.TUFF) || state.is(Blocks.CALCITE)
			|| state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)
			|| state.is(Blocks.ANDESITE) || state.is(Blocks.SANDSTONE);
	}

	private static double horizontalDistSq(Vec3 a, Vec3 b) {
		double dx = a.x - b.x;
		double dz = a.z - b.z;
		return dx * dx + dz * dz;
	}

	@Override
	public void aiStep() {
		if (this.burrowState == BurrowState.SURFACE || this.burrowState == BurrowState.EXPOSED) {
			super.aiStep();
		}

		if (this.level().isClientSide() && this.isAlive() && this.burrowState == BurrowState.SURFACE
			&& this.random.nextInt(6) == 0) {
			this.level().addParticle(
				ParticleTypes.MYCELIUM,
				this.getRandomX(0.4), this.getRandomY(), this.getRandomZ(0.4),
				0.0, -0.04, 0.0
			);
		}
	}

	@Override
	public boolean isInvulnerable() {
		if (this.burrowState == BurrowState.UNDERGROUND) {
			return true;
		}
		return super.isInvulnerable();
	}

	@Override
	public void remove(RemovalReason reason) {
		clearAllCracks();
		super.remove(reason);
	}
}
