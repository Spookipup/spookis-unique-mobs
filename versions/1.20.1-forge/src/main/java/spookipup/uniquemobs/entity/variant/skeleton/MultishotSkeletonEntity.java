package spookipup.uniquemobs.entity.variant.skeleton;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import spookipup.uniquemobs.entity.ai.ShootGoal;
import spookipup.uniquemobs.entity.ai.StrafeAndRetreatGoal;

// 3 arrows per shot
public class MultishotSkeletonEntity extends Skeleton {

	private static final float SPREAD_ANGLE = 12.0F;

	public MultishotSkeletonEntity(EntityType<? extends Skeleton> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Skeleton.createAttributes()
			.add(Attributes.MAX_HEALTH, 22.0);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();

		this.goalSelector.getAvailableGoals().removeIf(w ->
			w.getGoal() instanceof RangedBowAttackGoal ||
			w.getGoal() instanceof MeleeAttackGoal
		);

		this.goalSelector.addGoal(3, new StrafeAndRetreatGoal(this, 0.5, 3.0F, 6.0F, 14.0F));
		this.goalSelector.addGoal(3, new ShootGoal(this, 40, 60, 16.0F, 3, true, false,
			ShootGoal.simple((level, shooter) -> {
				Arrow arrow = new Arrow(level, shooter);
				arrow.setBaseDamage(1.2);
				return arrow;
			}, 1.6F, 6.0F)
		).afterShot(this::fireExtraArrows));

		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, false));
	}

	@Override
	public void reassessWeaponGoal() {
	}

	private void fireExtraArrows() {
		if (!(this.level() instanceof ServerLevel serverLevel)) return;
		LivingEntity target = this.getTarget();
		if (target == null) return;

		double dx = target.getX() - this.getX();
		double dy = target.getY(0.3333) - this.getEyeY();
		double dz = target.getZ() - this.getZ();
		double dist = Math.sqrt(dx * dx + dz * dz);
		dy += dist * 0.2;

		for (int side = -1; side <= 1; side += 2) {
			float angleOffset = side * SPREAD_ANGLE * Mth.DEG_TO_RAD;
			double cos = Math.cos(angleOffset);
			double sin = Math.sin(angleOffset);
			double sdx = dx * cos - dz * sin;
			double sdz = dx * sin + dz * cos;

			Arrow arrow = new Arrow(serverLevel, this);
			arrow.setBaseDamage(1.2);
			arrow.shoot(sdx, dy, sdz, 1.6F, 6.0F);
			serverLevel.addFreshEntity(arrow);
		}
	}
}
