package spookipup.uniquemobs.entity.variant.spider;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import spookipup.uniquemobs.entity.ai.LeapAttackGoal;
import spookipup.uniquemobs.entity.ai.PanicLeapGoal;

// tiny pouncing spider
public class JumpingSpiderEntity extends Spider {

	public JumpingSpiderEntity(EntityType<? extends Spider> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Spider.createAttributes()
			.add(Attributes.MAX_HEALTH, 10.0)
			.add(Attributes.MOVEMENT_SPEED, 0.35)
			.add(Attributes.SCALE, 0.6)
			.add(Attributes.SAFE_FALL_DISTANCE, 100.0);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();

		this.goalSelector.removeAllGoals(goal ->
			goal instanceof LeapAtTargetGoal ||
			goal instanceof MeleeAttackGoal
		);

		// escape leap when target is right on top of us
		this.goalSelector.addGoal(2, new PanicLeapGoal(this, 2.5F, 0.6F));
		// big pounce from 3-10 blocks out
		this.goalSelector.addGoal(3, new LeapAttackGoal(this, 3.0F, 10.0F, 0.85F, 1.3F, 10));
		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
	}

	public static boolean checkJumpingSpiderSpawnRules(EntityType<? extends Monster> type,
		ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
		if (level.getDifficulty() == Difficulty.PEACEFUL) return false;

		BlockState below = level.getBlockState(pos.below());

		// tree canopies are almost always lit by sky, so skip the dark check for leaves.
		// using NO_RESTRICTIONS placement so we validate the position ourselves
		if (below.is(BlockTags.LEAVES)) {
			return !level.getBlockState(pos).isCollisionShapeFullBlock(level, pos);
		}

		if (!below.isValidSpawn(level, pos.below(), type)) return false;
		return Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
	}
}
