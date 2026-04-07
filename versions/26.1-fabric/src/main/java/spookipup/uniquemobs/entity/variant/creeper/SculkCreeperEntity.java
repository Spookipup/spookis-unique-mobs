package spookipup.uniquemobs.entity.variant.creeper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;

// tiny explosion, summons a warden. the creeper is the distraction
public class SculkCreeperEntity extends Creeper {

	private static final float EXPLOSION_POWER = 1.5F;
	private static final float EXPLOSION_POWER_POWERED = 2.5F;

	public SculkCreeperEntity(EntityType<? extends Creeper> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Creeper.createAttributes();
	}

	// vanilla checkMonsterSpawnRules requires light level 0 which fails in the
	// deep dark because sculk blocks emit light 1. this tolerates the sculk glow
	// but still blocks spawning near soul lanterns and other bright sources
	public static boolean checkSculkCreeperSpawnRules(EntityType<? extends Monster> type,
			ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
		return level.getDifficulty() != Difficulty.PEACEFUL
			&& level.getMaxLocalRawBrightness(pos) <= 7
			&& Mob.checkMobSpawnRules(type, level, reason, pos, random);
	}

	@Override
	public void tick() {
		if (this.isAlive() && getSwellDir() > 0 && getSwelling(1.0F) >= 1.0F) {
			if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
				float power = this.isPowered() ? EXPLOSION_POWER_POWERED : EXPLOSION_POWER;
				serverLevel.explode(
					this, this.getX(), this.getY(), this.getZ(),
					power, false, Level.ExplosionInteraction.MOB
				);
				emitSculkSignal(serverLevel);
				spawnSculkBurst(serverLevel);
				this.discard();
			}
			return;
		}

		super.tick();
	}

	private void emitSculkSignal(ServerLevel serverLevel) {
		// raise warning levels for nearby players (triggers darkness + sounds)
		for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class,
				this.getBoundingBox().inflate(40.0))) {
			player.getWardenSpawnTracker().ifPresent(tracker ->
				tracker.setWarningLevel(WardenSpawnTracker.MAX_WARNING_LEVEL));
		}

		// the warden spawn logic lives in SculkShriekerBlockEntity, not in the
		// tracker itself, so we spawn the warden directly the same way the shrieker does
		SpawnUtil.trySpawnMob(
			EntityType.WARDEN, EntitySpawnReason.TRIGGERED,
			serverLevel, this.blockPosition(), 20, 5, 6,
			SpawnUtil.Strategy.ON_TOP_OF_COLLIDER, false);

		Warden.applyDarknessAround(serverLevel,
			this.position(), null, 40);

		serverLevel.gameEvent(this, GameEvent.EXPLODE, this.blockPosition());
		serverLevel.gameEvent(this, GameEvent.SHRIEK, this.blockPosition());
	}

	private void spawnSculkBurst(ServerLevel serverLevel) {
		double x = this.getX();
		double y = this.getY() + 1.0;
		double z = this.getZ();

		serverLevel.sendParticles(ParticleTypes.SCULK_SOUL,
			x, y, z, 60, 2.0, 1.5, 2.0, 0.05);
		serverLevel.sendParticles(ParticleTypes.SCULK_CHARGE_POP,
			x, y, z, 40, 1.0, 0.8, 1.0, 0.02);
		serverLevel.sendParticles(ParticleTypes.SONIC_BOOM,
			x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
	}

	@Override
	public boolean dampensVibrations() {
		return true;
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(5) == 0) {
			this.level().addParticle(
				ParticleTypes.SCULK_SOUL,
				this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5),
				0.0, 0.05, 0.0
			);
		}
	}
}
