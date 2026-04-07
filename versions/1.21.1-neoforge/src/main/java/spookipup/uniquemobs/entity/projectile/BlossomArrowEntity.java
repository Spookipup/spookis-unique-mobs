package spookipup.uniquemobs.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import spookipup.uniquemobs.entity.BlossomHelper;
import spookipup.uniquemobs.entity.variant.skeleton.BlossomSkeletonEntity;
import spookipup.uniquemobs.registry.ModEntities;

public class BlossomArrowEntity extends Arrow {

	public BlossomArrowEntity(EntityType<? extends Arrow> entityType, Level level) {
		super(entityType, level);
	}

	public BlossomArrowEntity(Level level, LivingEntity shooter) {
		super(ModEntities.BLOSSOM_ARROW.get(), level);
		setOwner(shooter);
		setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(Items.ARROW);
	}

	@Override
	protected void doPostHurtEffects(LivingEntity livingEntity) {
		super.doPostHurtEffects(livingEntity);
		Entity owner = getOwner();
		Entity source = owner == null ? this : owner;
		BlossomHelper.applyPetalMark(livingEntity, source, 1);
		BlossomHelper.applyUpwardKnockback(livingEntity, source, 0.32, 0.48);
		if (owner instanceof BlossomSkeletonEntity skeleton) {
			skeleton.onBlossomAttackLanded(livingEntity);
		}
	}
}
