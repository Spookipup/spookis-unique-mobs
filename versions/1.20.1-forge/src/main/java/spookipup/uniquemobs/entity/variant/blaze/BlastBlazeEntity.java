package spookipup.uniquemobs.entity.variant.blaze;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import spookipup.uniquemobs.entity.ai.BlastBlazeAttackGoal;

// hangs back after each burst instead of chain-chasing
public class BlastBlazeEntity extends Blaze {

	public BlastBlazeEntity(EntityType<? extends Blaze> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Blaze.createAttributes()
			.add(Attributes.MAX_HEALTH, 26.0)
			.add(Attributes.ATTACK_DAMAGE, 5.0)
			.add(Attributes.FOLLOW_RANGE, 40.0)
			.add(Attributes.MOVEMENT_SPEED, 0.48)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.2);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new BlastBlazeAttackGoal(this));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public void tick() {
		super.tick();

		if (this.getTarget() instanceof Player player && (player.isCreative() || player.isSpectator())) {
			this.setTarget(null);
		}
	}
}


