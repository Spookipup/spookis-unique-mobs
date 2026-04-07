package spookipup.uniquemobs.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import spookipup.uniquemobs.entity.WitherAshCloud;
import spookipup.uniquemobs.entity.variant.blaze.WitherBlazeEntity;
import spookipup.uniquemobs.registry.ModEntities;

// the bolt is just the delivery for the ash cloud
public class WitherAshBoltEntity extends ThrowableProjectile implements ItemSupplier {

	private static final ItemStack ICON = new ItemStack(Items.COAL);
	private static final int MAX_LIFETIME = 80;
	private static final float DIRECT_HIT_DAMAGE = 4.0F;
	private static final float CLOUD_RADIUS = 3.2F;
	private static final int CLOUD_DURATION = 100;
	private static final double CLOUD_Y_OFFSET = 0.7;

	public WitherAshBoltEntity(EntityType<? extends WitherAshBoltEntity> type, Level level) {
		super(type, level);
	}

	public WitherAshBoltEntity(Level level, LivingEntity shooter) {
		super(ModEntities.WITHER_ASH_BOLT, level);
		this.setOwner(shooter);
		this.setPos(shooter.getX(), shooter.getEyeY() - 0.12, shooter.getZ());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public ItemStack getItem() {
		return ICON;
	}

	@Override
	public void tick() {
		super.tick();

		if (this.level().isClientSide()) {
			this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
			this.level().addParticle(ParticleTypes.WHITE_ASH, this.getX(), this.getY(), this.getZ(), 0.0, 0.01, 0.0);
		}

		if (!this.level().isClientSide() && this.tickCount >= MAX_LIFETIME) {
			burst(null);
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		Entity target = result.getEntity();
		if (target instanceof LivingEntity livingTarget
			&& !(livingTarget instanceof WitherBlazeEntity)
			&& this.level() instanceof ServerLevel serverLevel) {
			LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
			livingTarget.hurt(this.damageSources().mobProjectile(this, owner), DIRECT_HIT_DAMAGE);
			livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
		}
		if (!this.level().isClientSide()) {
			burst(result);
		}
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (!this.level().isClientSide()) {
			burst(result);
		}
	}

	@Override
	protected double getDefaultGravity() {
		return 0.0F;
	}

	private void burst(HitResult result) {
		if (!(this.level() instanceof ServerLevel serverLevel)) return;

		double cloudX = result != null ? result.getLocation().x : this.getX();
		double cloudY = (result != null ? result.getLocation().y : this.getY()) + CLOUD_Y_OFFSET;
		double cloudZ = result != null ? result.getLocation().z : this.getZ();

		WitherAshCloud cloud = new WitherAshCloud(serverLevel, cloudX, cloudY, cloudZ);
		cloud.setOwner(this.getOwner() instanceof LivingEntity living ? living : null);
		cloud.setRadius(CLOUD_RADIUS);
		cloud.setDuration(CLOUD_DURATION);
		cloud.setWaitTime(0);
		cloud.setRadiusPerTick(-0.016F);
		cloud.setParticle(ParticleTypes.ASH);
		serverLevel.addFreshEntity(cloud);

		serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
			cloudX, cloudY, cloudZ, 16, 0.65, 0.35, 0.65, 0.01);
		serverLevel.sendParticles(ParticleTypes.SMOKE,
			cloudX, cloudY + 0.2, cloudZ, 24, 0.85, 0.45, 0.85, 0.01);
		serverLevel.sendParticles(ParticleTypes.WHITE_ASH,
			cloudX, cloudY, cloudZ, 32, 0.9, 0.55, 0.9, 0.01);
		serverLevel.sendParticles(ParticleTypes.ASH,
			cloudX, cloudY + 0.3, cloudZ, 24, 0.9, 0.65, 0.9, 0.01);

		this.discard();
	}
}


