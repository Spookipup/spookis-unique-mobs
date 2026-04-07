package spookipup.uniquemobs.entity.variant.spider;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import spookipup.uniquemobs.entity.ai.MeleeWhenCloseGoal;
import spookipup.uniquemobs.entity.ai.PanicLeapGoal;
import spookipup.uniquemobs.entity.ai.ShootGoal;
import spookipup.uniquemobs.entity.ai.StrafeAndRetreatGoal;
import spookipup.uniquemobs.entity.projectile.PoisonSpitEntity;

// spits poison, circles at range
public class SpittingSpiderEntity extends Spider {

	public SpittingSpiderEntity(EntityType<? extends Spider> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Spider.createAttributes()
			.add(Attributes.MAX_HEALTH, 14.0)
			.add(Attributes.MOVEMENT_SPEED, 0.28);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();

		// ditch vanilla melee + leap, we handle combat ourselves
		this.goalSelector.getAvailableGoals().removeIf(w ->
			w.getGoal() instanceof LeapAtTargetGoal ||
			w.getGoal() instanceof MeleeAttackGoal
		);

		this.goalSelector.addGoal(3, new StrafeAndRetreatGoal(this, 0.6, 3.5F, 6.0F, 12.0F));
		this.goalSelector.addGoal(3, new ShootGoal(this, 30, 60, 12.0F,
			ShootGoal.simple(PoisonSpitEntity::new, 1.2F, 6.0F, SoundEvents.LLAMA_SPIT)
		));
		this.goalSelector.addGoal(3, new MeleeWhenCloseGoal(this, 20));
		this.goalSelector.addGoal(2, new PanicLeapGoal(this, 3.0F, 0.5F));
	}
}
