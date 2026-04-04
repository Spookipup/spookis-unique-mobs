package spookipup.uniquemobs.entity.variant.ghast;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import spookipup.uniquemobs.entity.ai.SpawnGhastlingGoal;

import java.util.List;
import java.util.UUID;

// brood mother ghast that spawns ragelings instead of fireballs
public class GreatMotherGhastEntity extends Ghast {

	private static final int MAX_RAGELINGS = 20;

	public GreatMotherGhastEntity(EntityType<? extends Ghast> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Ghast.createAttributes()
			.add(Attributes.MAX_HEALTH, 40.0)
			.add(Attributes.FOLLOW_RANGE, 64.0);
	}

	@Override
	protected void registerGoals() {
		// don't call super because the ghast inner goals are private
		this.goalSelector.addGoal(5, new Ghast.RandomFloatAroundGoal(this));
		this.goalSelector.addGoal(7, new Ghast.GhastLookGoal(this));
		this.goalSelector.addGoal(2, new SpawnGhastlingGoal(this, 80, 140, MAX_RAGELINGS));

		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	public int countLivingRagelings() {
		UUID myId = this.getUUID();
		List<RagelingEntity> nearby = this.level().getEntitiesOfClass(
			RagelingEntity.class,
			this.getBoundingBox().inflate(64),
			g -> myId.equals(g.getOwnerUUID())
		);
		return nearby.size();
	}
}
