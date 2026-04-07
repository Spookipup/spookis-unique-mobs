package spookipup.uniquemobs.entity.variant.spider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import spookipup.uniquemobs.entity.ai.MeleeWhenCloseGoal;
import spookipup.uniquemobs.entity.ai.ShootGoal;
import spookipup.uniquemobs.entity.projectile.FreezeSnowballEntity;

import java.util.EnumSet;

// pelts freezing snowballs then rushes when you're frozen
public class IceSpiderEntity extends Spider {

	private static final int SLOWNESS_DURATION = 40;
	private static final int FREEZE_THRESHOLD = 100;
	private int frostWalkerCooldown;

	public IceSpiderEntity(EntityType<? extends Spider> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Spider.createAttributes()
			.add(Attributes.MAX_HEALTH, 20.0)
			.add(Attributes.SCALE, 1.2);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();

		this.goalSelector.getAvailableGoals().removeIf(w ->
			w.getGoal() instanceof LeapAtTargetGoal ||
			w.getGoal() instanceof MeleeAttackGoal
		);

		this.goalSelector.addGoal(3, new FreezeHuntGoal(this));
		this.goalSelector.addGoal(3, new ShootGoal(this, 15, 30, 14.0F,
			ShootGoal.simple((level, shooter) ->
				new FreezeSnowballEntity(level, shooter),
				1.5F, 5.0F, SoundEvents.SNOW_GOLEM_SHOOT)
		));
		this.goalSelector.addGoal(3, new MeleeWhenCloseGoal(this, 15));
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean hit = super.doHurtTarget(target);

		if (hit && target instanceof LivingEntity livingTarget) {
			livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, 0));
		}

		return hit;
	}

	public boolean isTargetFrozenEnough() {
		LivingEntity target = this.getTarget();
		return target != null && target.getTicksFrozen() >= FREEZE_THRESHOLD;
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level() instanceof ServerLevel serverLevel) {
			if (this.frostWalkerCooldown > 0) {
				this.frostWalkerCooldown--;
			} else if (this.onGround()
				&& serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
				freezeNearbyWater();
				this.frostWalkerCooldown = 3;
			}
		}

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(4) == 0) {
			this.level().addParticle(
				ParticleTypes.SNOWFLAKE,
				this.getRandomX(0.6), this.getRandomY() - 0.1, this.getRandomZ(0.6),
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
				FluidState fluid = state.getFluidState();
				if (fluid.is(Fluids.WATER) && state.is(Blocks.WATER)
					&& this.level().getBlockState(pos.above()).isAir()) {
					this.level().setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
				}
			}
		}
	}

	// keeps distance while pelting snowballs, then charges once target is frozen
	private static class FreezeHuntGoal extends Goal {

		private final IceSpiderEntity spider;
		private LivingEntity target;
		private int strafeDirection;
		private int strafeSwitchTimer;
		private int repathTimer;

		FreezeHuntGoal(IceSpiderEntity spider) {
			this.spider = spider;
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			LivingEntity t = this.spider.getTarget();
			if (t != null && t.isAlive()) {
				this.target = t;
				return true;
			}
			return false;
		}

		@Override
		public boolean canContinueToUse() {
			return this.target != null && this.target.isAlive() && this.spider.getTarget() == this.target;
		}

		@Override
		public void start() {
			this.strafeDirection = this.spider.getRandom().nextBoolean() ? 1 : -1;
			this.strafeSwitchTimer = 20 + this.spider.getRandom().nextInt(40);
			this.repathTimer = 0;
		}

		@Override
		public void stop() {
			this.target = null;
			this.spider.getNavigation().stop();
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			if (this.target == null) return;

			double distanceSq = this.spider.distanceToSqr(this.target);
			this.spider.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

			this.strafeSwitchTimer--;
			if (this.strafeSwitchTimer <= 0) {
				this.strafeDirection = this.spider.getRandom().nextBoolean() ? 1 : -1;
				this.strafeSwitchTimer = 20 + this.spider.getRandom().nextInt(40);
			}

			this.repathTimer--;

			if (this.spider.isTargetFrozenEnough()) {
				// target is frozen - rush in for the kill
				if (this.repathTimer <= 0) {
					this.spider.getNavigation().moveTo(this.target, 1.3);
					this.repathTimer = 5;
				}
			} else {
				// keep distance and pelt with snowballs
				if (distanceSq < 4.0 * 4.0) {
					// too close, back off
					if (this.repathTimer <= 0) {
						double dx = this.spider.getX() - this.target.getX();
						double dz = this.spider.getZ() - this.target.getZ();
						double len = Math.sqrt(dx * dx + dz * dz);
						if (len > 0.001) {
							dx /= len;
							dz /= len;
						}
						this.spider.getNavigation().moveTo(
							this.spider.getX() + dx * 6, this.spider.getY(), this.spider.getZ() + dz * 6, 1.0);
						this.repathTimer = 5;
					}
				} else if (distanceSq > 12.0 * 12.0 || !this.spider.getSensing().hasLineOfSight(this.target)) {
					// too far or can't see, approach
					if (this.repathTimer <= 0) {
						this.spider.getNavigation().moveTo(this.target, 1.0);
						this.repathTimer = 15;
					}
				} else {
					// in range - strafe and shoot
					this.spider.getNavigation().stop();
					this.spider.getMoveControl().strafe(0, this.strafeDirection * 0.5F);
				}
			}
		}
	}
}
