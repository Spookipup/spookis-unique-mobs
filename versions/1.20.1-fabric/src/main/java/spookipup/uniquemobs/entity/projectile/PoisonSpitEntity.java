package spookipup.uniquemobs.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import spookipup.uniquemobs.registry.ModEntities;

public class PoisonSpitEntity extends ThrowableProjectile implements ItemSupplier {

	private static final int POISON_DURATION = 100;
	private static final int POISON_AMPLIFIER = 0;
	private static final ItemStack ICON = new ItemStack(Items.SLIME_BALL);

	public PoisonSpitEntity(EntityType<? extends PoisonSpitEntity> type, Level level) {
		super(type, level);
	}

	public PoisonSpitEntity(Level level, LivingEntity shooter) {
		super(ModEntities.POISON_SPIT, level);
		this.setOwner(shooter);
		this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
	}

	@Override
	protected void defineSynchedData() {
	}

	@Override
	public ItemStack getItem() {
		return ICON;
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		Entity target = result.getEntity();

		if (target instanceof LivingEntity livingTarget && this.level() instanceof ServerLevel serverLevel) {
			LivingEntity owner = this.getOwner() instanceof LivingEntity le ? le : null;
			livingTarget.hurt(this.damageSources().mobProjectile(this, owner), 2.0F);
			livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION, POISON_AMPLIFIER));
		}
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (!this.level().isClientSide()) {
			this.level().broadcastEntityEvent(this, (byte) 3);
			this.discard();
		}
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 3) {
			for (int i = 0; i < 8; i++) {
				this.level().addParticle(
					ParticleTypes.ITEM_SLIME,
					this.getX(), this.getY(), this.getZ(),
					(this.random.nextDouble() - 0.5) * 0.08,
					(this.random.nextDouble() - 0.5) * 0.08,
					(this.random.nextDouble() - 0.5) * 0.08
				);
			}
		}
	}

	@Override
	protected float getGravity() {
		return 0.02F;
	}
}
