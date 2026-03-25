package spookipup.uniquemobs.entity.variant.enderman;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;

// warps behind you, leaves damage clouds everywhere it teleports from
public class AssassinEndermanEntity extends EnderMan {

	private static final int MAX_TELEPORT_ATTEMPTS = 8;
	private static final float CLOUD_RADIUS = 3.0F;
	private static final int CLOUD_DURATION = 60;
	private static final int CLOUD_NAUSEA_DURATION = 60;

	private int warpTimer;
	private double prevTickX, prevTickY, prevTickZ;

	public AssassinEndermanEntity(EntityType<? extends EnderMan> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return EnderMan.createAttributes()
			.add(Attributes.MAX_HEALTH, 30.0);
	}

	@Override
	public void aiStep() {
		prevTickX = this.getX();
		prevTickY = this.getY();
		prevTickZ = this.getZ();

		if (!this.level().isClientSide() && this.getTarget() != null && this.getTarget().isAlive()) {
			this.warpTimer--;
			if (this.warpTimer <= 0) {
				warpBehindTarget(this.getTarget());
				this.warpTimer = 30 + this.random.nextInt(20);
			}
		}

		super.aiStep();

		if (!this.level().isClientSide()) {
			double dx = this.getX() - prevTickX;
			double dy = this.getY() - prevTickY;
			double dz = this.getZ() - prevTickZ;
			if (dx * dx + dy * dy + dz * dz > 9.0) {
				spawnWarpCloud(prevTickX, prevTickY, prevTickZ);
			}
		}
	}

	private void warpBehindTarget(LivingEntity target) {
		// figure out which way the player is looking
		float yaw = target.getYRot() * ((float) Math.PI / 180.0F);
		double lookX = -Math.sin(yaw);
		double lookZ = Math.cos(yaw);

		// try positions behind the player first, then fall back to random nearby spots
		for (int i = 0; i < MAX_TELEPORT_ATTEMPTS; i++) {
			double dist = 2.0 + this.random.nextDouble() * 3.0;
			double spread = (this.random.nextDouble() - 0.5) * 4.0;

			// behind = opposite of where they're looking
			double tx = target.getX() - lookX * dist + lookZ * spread;
			double ty = target.getY() + (this.random.nextInt(5) - 2);
			double tz = target.getZ() - lookZ * dist - lookX * spread;

			if (this.randomTeleport(tx, ty, tz, true)) return;
		}

		// couldn't get behind them, just do a normal enderman teleport
		for (int i = 0; i < MAX_TELEPORT_ATTEMPTS; i++) {
			if (this.teleport()) return;
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		// immune to our own cloud damage
		if (source.getEntity() instanceof AreaEffectCloud cloud && cloud.getOwner() == this) {
			return false;
		}
		return super.hurt(source, amount);
	}

	@Override
	public boolean addEffect(MobEffectInstance effect, Entity source) {
		// ignore effects from our own clouds
		if (source instanceof AreaEffectCloud cloud && cloud.getOwner() == this) {
			return false;
		}
		return super.addEffect(effect, source);
	}

	private void spawnWarpCloud(double x, double y, double z) {
		AreaEffectCloud cloud = new AreaEffectCloud(this.level(), x, y, z);
		cloud.setRadius(CLOUD_RADIUS);
		cloud.setRadiusPerTick(-CLOUD_RADIUS / CLOUD_DURATION);
		cloud.setDuration(CLOUD_DURATION);
		cloud.setWaitTime(5);
		cloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 0));
		cloud.addEffect(new MobEffectInstance(MobEffects.CONFUSION, CLOUD_NAUSEA_DURATION, 0));
		cloud.setOwner(this);
		cloud.setParticle(ParticleTypes.WITCH);

		this.level().addFreshEntity(cloud);
	}
}
