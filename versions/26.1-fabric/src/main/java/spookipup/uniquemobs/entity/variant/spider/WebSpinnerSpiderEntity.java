package spookipup.uniquemobs.entity.variant.spider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueOutput;
import spookipup.uniquemobs.entity.ai.MeleeWhenCloseGoal;
import spookipup.uniquemobs.entity.ai.ShootGoal;
import spookipup.uniquemobs.entity.projectile.WebProjectileEntity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

// shoots webs, keeps distance, rushes when target is stuck
public class WebSpinnerSpiderEntity extends Spider {

	private static final int WEB_REVERT_TICKS = 120;
	private static final int MAX_WEBS = 12;

	private final List<TemporaryWeb> placedWebs = new ArrayList<>();
	private int trailWebCooldown;

	public WebSpinnerSpiderEntity(EntityType<? extends Spider> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Spider.createAttributes()
			.add(Attributes.MAX_HEALTH, 18.0);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();

		this.goalSelector.removeAllGoals(goal ->
			goal instanceof LeapAtTargetGoal ||
			goal instanceof MeleeAttackGoal
		);

		this.goalSelector.addGoal(3, new WebHuntGoal(this));
		this.goalSelector.addGoal(3, new ShootGoal(this, 20, 40, 12.0F,
			ShootGoal.simple((level, shooter) -> new WebProjectileEntity(level, shooter),
				1.2F, 6.0F, SoundEvents.SPIDER_AMBIENT)
		));
		this.goalSelector.addGoal(3, new MeleeWhenCloseGoal(this, 15));
	}

	public void trackPlacedWeb(BlockPos pos) {
		placedWebs.add(new TemporaryWeb(pos, WEB_REVERT_TICKS));
		// cap the total webs - remove oldest if over limit
		while (placedWebs.size() > MAX_WEBS) {
			TemporaryWeb oldest = placedWebs.removeFirst();
			if (this.level().getBlockState(oldest.pos).is(Blocks.COBWEB)) {
				this.level().setBlockAndUpdate(oldest.pos, Blocks.AIR.defaultBlockState());
			}
		}
	}

	public boolean isTargetInWeb() {
		LivingEntity target = this.getTarget();
		if (target == null) return false;
		BlockState atFeet = this.level().getBlockState(target.blockPosition());
		return atFeet.is(Blocks.COBWEB);
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (!this.level().isClientSide()) {
			placedWebs.removeIf(entry -> {
				if (--entry.ticksLeft <= 0) {
					if (this.level().getBlockState(entry.pos).is(Blocks.COBWEB)) {
						this.level().setBlockAndUpdate(entry.pos, Blocks.AIR.defaultBlockState());
						if (this.level() instanceof ServerLevel sl) {
							sl.sendParticles(ParticleTypes.ITEM_SNOWBALL,
								entry.pos.getX() + 0.5, entry.pos.getY() + 0.5, entry.pos.getZ() + 0.5,
								6, 0.3, 0.3, 0.3, 0.02);
						}
					}
					return true;
				}
				return false;
			});

			if (this.trailWebCooldown > 0) {
				this.trailWebCooldown--;
			} else {
				LivingEntity target = this.getTarget();
				if (target != null && !isTargetInWeb()) {
					double distSq = this.distanceToSqr(target);
					// retreating = target is close and we're moving away
					if (distSq < 5.0 * 5.0 && distSq > 2.0) {
						BlockPos behind = this.blockPosition();
						BlockState state = this.level().getBlockState(behind);
						if (state.isAir()) {
							this.level().setBlockAndUpdate(behind, Blocks.COBWEB.defaultBlockState());
							trackPlacedWeb(behind);
							this.trailWebCooldown = 15;
						}
					}
				}
			}
		}

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(6) == 0) {
			this.level().addParticle(
				ParticleTypes.ITEM_SNOWBALL,
				this.getRandomX(0.5), this.getRandomY() - 0.1, this.getRandomZ(0.5),
				0.0, -0.02, 0.0
			);
		}
	}

	private void revertAllWebs() {
		if (!this.level().isClientSide()) {
			for (TemporaryWeb entry : placedWebs) {
				if (this.level().getBlockState(entry.pos).is(Blocks.COBWEB)) {
					this.level().setBlockAndUpdate(entry.pos, Blocks.AIR.defaultBlockState());
				}
			}
			placedWebs.clear();
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		revertAllWebs();
		super.remove(reason);
	}

	@Override
	public void addAdditionalSaveData(ValueOutput output) {
		revertAllWebs();
		super.addAdditionalSaveData(output);
	}

	// keeps distance and pelts webs, rushes in when target is stuck
	private static class WebHuntGoal extends Goal {

		private final WebSpinnerSpiderEntity spider;
		private LivingEntity target;
		private int strafeDirection;
		private int strafeSwitchTimer;
		private int repathTimer;

		WebHuntGoal(WebSpinnerSpiderEntity spider) {
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

			if (this.spider.isTargetInWeb()) {
				// target is stuck - rush in
				if (this.repathTimer <= 0) {
					this.spider.getNavigation().moveTo(this.target, 1.3);
					this.repathTimer = 5;
				}
			} else {
				if (distanceSq < 4.0 * 4.0) {
					// too close, back off
					if (this.repathTimer <= 0) {
						double dx = this.spider.getX() - this.target.getX();
						double dz = this.spider.getZ() - this.target.getZ();
						double len = Math.sqrt(dx * dx + dz * dz);
						if (len > 0.001) { dx /= len; dz /= len; }
						this.spider.getNavigation().moveTo(
							this.spider.getX() + dx * 6, this.spider.getY(), this.spider.getZ() + dz * 6, 1.0);
						this.repathTimer = 5;
					}
				} else if (distanceSq > 12.0 * 12.0 || !this.spider.getSensing().hasLineOfSight(this.target)) {
					if (this.repathTimer <= 0) {
						this.spider.getNavigation().moveTo(this.target, 1.0);
						this.repathTimer = 15;
					}
				} else {
					this.spider.getNavigation().stop();
					this.spider.getMoveControl().strafe(0, this.strafeDirection * 0.5F);
				}
			}
		}
	}

	private static class TemporaryWeb {
		final BlockPos pos;
		int ticksLeft;

		TemporaryWeb(BlockPos pos, int ticksLeft) {
			this.pos = pos;
			this.ticksLeft = ticksLeft;
		}
	}
}
