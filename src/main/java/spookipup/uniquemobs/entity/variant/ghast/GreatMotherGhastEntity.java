package spookipup.uniquemobs.entity.variant.ghast;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.Level;
import spookipup.uniquemobs.entity.ai.SpawnGhastlingGoal;

import java.util.List;
import java.util.UUID;

// doom-inspired brood mother - spawns ghastlings instead of fireballs
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
		// let ghast add its float + look + fireball + targeting goals
		super.registerGoals();
		// strip the fireball goal and replace with our spawn attack
		this.goalSelector.getAvailableGoals().removeIf(
			g -> g.getGoal().getClass().getEnclosingClass() == Ghast.class
				&& g.getGoal().getClass().getSimpleName().contains("Fireball"));
		this.goalSelector.addGoal(2, new SpawnGhastlingGoal(this, 80, 140, MAX_RAGELINGS));
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
