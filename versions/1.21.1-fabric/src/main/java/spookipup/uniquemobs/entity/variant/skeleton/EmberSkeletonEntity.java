package spookipup.uniquemobs.entity.variant.skeleton;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import spookipup.uniquemobs.entity.ai.ShootGoal;
import spookipup.uniquemobs.entity.ai.StrafeAndRetreatGoal;

public class EmberSkeletonEntity extends Skeleton {

	public EmberSkeletonEntity(EntityType<? extends Skeleton> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Skeleton.createAttributes()
			.add(Attributes.MAX_HEALTH, 22.0);
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
	protected void registerGoals() {
		super.registerGoals();

		this.goalSelector.getAvailableGoals().removeIf(w ->
			w.getGoal() instanceof RangedBowAttackGoal ||
			w.getGoal() instanceof MeleeAttackGoal
		);

		// nether turf war - picks fights with piglins and hoglins
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Hoglin.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractPiglin.class, true));

		this.goalSelector.addGoal(3, new StrafeAndRetreatGoal(this, 0.5, 5.0F, 8.0F, 16.0F));

		this.goalSelector.addGoal(3, new ShootGoal(this, 25, 45, 18.0F, 3, true, false,
			ShootGoal.simple((level, shooter) -> {
				Arrow arrow = new Arrow(level, shooter, ItemStack.EMPTY, new ItemStack(Items.BOW));
				arrow.setCritArrow(true);
				arrow.igniteForSeconds(100);
				return arrow;
			}, 1.8F, 4.0F)
		));

		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, false));
	}

	@Override
	public void reassessWeaponGoal() {
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(3) == 0) {
			this.level().addParticle(
				ParticleTypes.FLAME,
				this.getRandomX(0.4), this.getRandomY(), this.getRandomZ(0.4),
				0.0, 0.015, 0.0
			);
		}
	}
}
