package spookipup.uniquemobs.entity.variant.skeleton;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import spookipup.uniquemobs.entity.ai.ShootGoal;
import spookipup.uniquemobs.entity.ai.StrafeAndRetreatGoal;

// hangs way back, hits hard, remembers you longer than normal
public class SniperSkeletonEntity extends Skeleton {

	public SniperSkeletonEntity(EntityType<? extends Skeleton> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Skeleton.createAttributes()
			.add(Attributes.MAX_HEALTH, 18.0)
			.add(Attributes.MOVEMENT_SPEED, 0.22)
			.add(Attributes.FOLLOW_RANGE, 64.0);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();

		this.goalSelector.getAvailableGoals().removeIf(w ->
			w.getGoal() instanceof RangedBowAttackGoal ||
			w.getGoal() instanceof MeleeAttackGoal
		);

		// vanilla skeleton forgets players after 3 seconds behind cover - a sniper remembers much longer
		this.targetSelector.getAvailableGoals().removeIf(w -> w.getGoal() instanceof NearestAttackableTargetGoal);
		NearestAttackableTargetGoal<Player> sniperTarget = new NearestAttackableTargetGoal<>(this, Player.class, true);
		sniperTarget.setUnseenMemoryTicks(400);
		this.targetSelector.addGoal(2, sniperTarget);

		// stays 20-40 blocks out, panics at 12, won't walk off cliffs
		this.goalSelector.addGoal(3, new StrafeAndRetreatGoal(this, 0.5, 12.0F, 20.0F, 40.0F, true, 3));

		// slower fire rate (3-5 sec), 48 block range, very tight spread, faster arrows, arcs over cover
		this.goalSelector.addGoal(3, new ShootGoal(this, 60, 100, 48.0F, 5, true, true,
			ShootGoal.simple((level, shooter) -> {
				Arrow arrow = new Arrow(level, shooter, ItemStack.EMPTY, new ItemStack(Items.BOW));
				arrow.setBaseDamage(4.0);
				arrow.setCritArrow(true);
				return arrow;
			}, 2.5F, 0.5F)
		));
	}

	// don't let reassessWeaponGoal re-add the vanilla bow goal
	@Override
	public void reassessWeaponGoal() {
	}
}
