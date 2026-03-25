package spookipup.uniquemobs.entity.projectile;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class FreezeSnowballEntity extends Snowball {

	private static final int FREEZE_TICKS = 80;
	private static final int SLOWNESS_DURATION = 60;

	public FreezeSnowballEntity(EntityType<? extends Snowball> entityType, Level level) {
		super(entityType, level);
	}

	public FreezeSnowballEntity(Level level, LivingEntity shooter) {
		super(level, shooter);
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);

		if (result.getEntity() instanceof LivingEntity living) {
			int newFreeze = Math.min(living.getTicksFrozen() + FREEZE_TICKS,
				living.getTicksRequiredToFreeze() + 40);
			living.setTicksFrozen(newFreeze);
			living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, 0));
		}
	}
}
