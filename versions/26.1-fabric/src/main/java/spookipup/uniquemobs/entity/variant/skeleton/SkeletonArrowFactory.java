package spookipup.uniquemobs.entity.variant.skeleton;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

final class SkeletonArrowFactory {

	private SkeletonArrowFactory() {
	}

	static Arrow standard(Level level, LivingEntity shooter) {
		return new Arrow(level, shooter, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
	}
}
