package spookipup.uniquemobs.entity.variant.skeleton;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import spookipup.uniquemobs.entity.ai.ShootGoal;
import spookipup.uniquemobs.entity.ai.VantagePointGoal;

// climbs trees, perches up high, rains poison arrows.
// flees when you get close and finds a new spot
public class PoisonSkeletonEntity extends Skeleton {

	private static final int POISON_DURATION = 100;

	public PoisonSkeletonEntity(EntityType<? extends Skeleton> entityType, Level level) {
		super(entityType, level);
		// treats leaves as walkable so it paths through tree canopies
		this.setPathfindingMalus(BlockPathTypes.LEAVES, 0.0F);
	}

	// uses spider-style wall climbing navigation so it actually plans paths up trees
	@Override
	protected PathNavigation createNavigation(Level level) {
		return new WallClimberNavigation(this, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Skeleton.createAttributes()
			.add(Attributes.MAX_HEALTH, 22.0)
			.add(Attributes.MOVEMENT_SPEED, 0.27);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();

		this.goalSelector.getAvailableGoals().removeIf(w ->
			w.getGoal() instanceof RangedBowAttackGoal ||
			w.getGoal() instanceof MeleeAttackGoal
		);

		// find elevated LOS position -> perch and shoot -> flee if approached -> repeat
		this.goalSelector.addGoal(2, new VantagePointGoal(this,
			1.0,   // seek speed
			1.3,   // flee speed (faster to escape)
			4,     // min height advantage over target
			12,    // search radius (horizontal)
			20,    // search height (vertical)
			8.0F,  // flee distance - abandon perch if target is closer than this
			4.0F,  // danger distance - panic flee
			2      // height panic - flee if target is within 2 blocks of our Y
		));

		// fires poison arrows while perched or fleeing - no flags so it runs alongside movement
		this.goalSelector.addGoal(3, new ShootGoal(this, 25, 45, 24.0F, 3, true, false,
			ShootGoal.simple((level, shooter) ->
				((PoisonSkeletonEntity) shooter).createPoisonArrow(), 1.6F, 4.0F)
		));

		// last resort if cornered with no escape
		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, false));
	}

	// goes through getArrow() - arrows built with ItemStack.EMPTY lose their effects on sync
	public Arrow createPoisonArrow() {
		return (Arrow) this.getArrow(new ItemStack(Items.ARROW), 1.0F);
	}

	@Override
	protected AbstractArrow getArrow(ItemStack arrowStack, float velocity) {
		AbstractArrow arrow = super.getArrow(arrowStack, velocity);
		if (arrow instanceof Arrow tipped) {
			tipped.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION, 0));
		}
		return arrow;
	}

	// climbs like a spider - horizontal collision against leaves or climbable blocks
	// triggers climbing. also climbs when standing inside climbable/leaf blocks
	@Override
	public boolean onClimbable() {
		if (super.onClimbable()) return true;

		// spider-style: climb when bumping into a wall and adjacent to foliage
		if (this.horizontalCollision) {
			BlockPos feet = this.blockPosition();
			BlockPos head = feet.above();
			if (isClimbableBlock(feet) || isClimbableBlock(head)) {
				return true;
			}
			// check the blocks we're colliding with (one block out in each direction)
			for (BlockPos neighbor : new BlockPos[]{feet.north(), feet.south(), feet.east(), feet.west(),
				head.north(), head.south(), head.east(), head.west()}) {
				if (isClimbableBlock(neighbor)) return true;
			}
		}

		// also climb if we're standing inside a climbable block (vines, etc.)
		BlockState atFeet = this.level().getBlockState(this.blockPosition());
		return atFeet.is(BlockTags.LEAVES) || atFeet.is(BlockTags.CLIMBABLE);
	}

	private boolean isClimbableBlock(BlockPos pos) {
		BlockState state = this.level().getBlockState(pos);
		return state.is(BlockTags.LEAVES) || state.is(BlockTags.CLIMBABLE);
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(5) == 0) {
			this.level().addParticle(
				ParticleTypes.ITEM_SLIME,
				this.getRandomX(0.4), this.getRandomY(), this.getRandomZ(0.4),
				0.0, 0.01, 0.0
			);
		}
	}
}
