package spookipup.uniquemobs.entity.variant.enderman;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import spookipup.uniquemobs.entity.BlossomHelper;
import spookipup.uniquemobs.registry.ModEntities;

import java.util.Set;

public class BlossomEndermanEntity extends EnderMan {

	private static final int MAX_AFTERIMAGES = 2;

	private double prevTickX;
	private double prevTickY;
	private double prevTickZ;
	private int afterimageCooldown;

	public BlossomEndermanEntity(EntityType<? extends EnderMan> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return EnderMan.createAttributes()
			.add(Attributes.MAX_HEALTH, 32.0);
	}

	@Override
	public void aiStep() {
		prevTickX = getX();
		prevTickY = getY();
		prevTickZ = getZ();

		super.aiStep();
		BlossomHelper.tickNaturalSlowFall(this);

		if (!level().isClientSide()) {
			if (afterimageCooldown > 0) afterimageCooldown--;
			ServerLevel serverLevel = (ServerLevel) level();
			LivingEntity target = getTarget();

			double dx = getX() - prevTickX;
			double dy = getY() - prevTickY;
			double dz = getZ() - prevTickZ;
			if (dx * dx + dy * dy + dz * dz > 9.0) {
				spawnBlossomGust(prevTickX, prevTickY, prevTickZ, target);
				afterimageCooldown = Math.min(afterimageCooldown, 18);
			} else if (afterimageCooldown <= 0 && target != null && countAfterimages(serverLevel) < MAX_AFTERIMAGES) {
				if (tryAfterimageTeleport(serverLevel, target)) {
					afterimageCooldown = 22 + random.nextInt(18);
				}
			}
		} else if (isAlive() && random.nextInt(5) == 0) {
			level().addParticle(ParticleTypes.CHERRY_LEAVES,
				getRandomX(0.45), getRandomY(), getRandomZ(0.45),
				(random.nextDouble() - 0.5) * 0.02, -0.01, (random.nextDouble() - 0.5) * 0.02);
		}
	}

	private boolean tryAfterimageTeleport(ServerLevel serverLevel, LivingEntity target) {
		double oldX = getX();
		double oldY = getY();
		double oldZ = getZ();

		for (int attempt = 0; attempt < 12; attempt++) {
			boolean behind = attempt < 7;
			double angle = behind
				? Math.toRadians(target.getYRot()) + Math.PI + (random.nextDouble() - 0.5) * 0.9
				: random.nextDouble() * Math.PI * 2.0;
			double distance = behind ? 2.4 + random.nextDouble() * 2.0 : 2.0 + random.nextDouble() * 4.0;
			double x = target.getX() - Math.sin(angle) * distance;
			double z = target.getZ() + Math.cos(angle) * distance;
			double y = target.getY() + random.nextIntBetweenInclusive(-1, 1);
			float yRot = faceYawTo(x, z, target);

			if (teleportTo(serverLevel, x, y, z, Set.of(), yRot, getXRot(), true)) {
				setYHeadRot(yRot);
				yBodyRot = yRot;
				spawnBlossomGust(oldX, oldY, oldZ, target);
				return true;
			}
		}

		return false;
	}

	private void spawnBlossomGust(double x, double y, double z, LivingEntity target) {
		if (!(level() instanceof ServerLevel serverLevel)) return;
		if (countAfterimages(serverLevel) >= MAX_AFTERIMAGES) return;

		BlossomEndermanAfterimageEntity afterimage = new BlossomEndermanAfterimageEntity(ModEntities.BLOSSOM_ENDERMAN_AFTERIMAGE, level());
		afterimage.setPos(x, y, z);
		afterimage.setYRot(getYRot());
		afterimage.setXRot(getXRot());
		afterimage.yBodyRot = yBodyRot;
		afterimage.setYHeadRot(yHeadRot);
		afterimage.setSource(this, target);
		level().addFreshEntity(afterimage);
		BlossomHelper.spawnPetalBurst(serverLevel, x, y + 0.8, z, 0.75, 18);
	}

	private int countAfterimages(ServerLevel serverLevel) {
		return serverLevel.getEntitiesOfClass(BlossomEndermanAfterimageEntity.class, getBoundingBox().inflate(64.0),
			afterimage -> afterimage.isOwnedBy(this)).size();
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}

	private static float faceYawTo(double x, double z, LivingEntity target) {
		double dx = target.getX() - x;
		double dz = target.getZ() - z;
		return (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
	}
}
