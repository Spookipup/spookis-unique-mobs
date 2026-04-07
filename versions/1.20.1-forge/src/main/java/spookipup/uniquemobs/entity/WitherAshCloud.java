package spookipup.uniquemobs.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import spookipup.uniquemobs.entity.variant.blaze.WitherBlazeEntity;
import spookipup.uniquemobs.registry.ModEntities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// ash clouds also refresh nearby wither blazes
public class WitherAshCloud extends AreaEffectCloud {

	private static final int HIT_COOLDOWN = 12;
	private static final int WITHER_DURATION = 70;
	private static final int EMPOWER_REFRESH = 16;
	private static final double CLOUD_HEIGHT = 2.6;

	private final Map<UUID, Integer> hitCooldowns = new HashMap<>();

	public WitherAshCloud(ServerLevel level, double x, double y, double z) {
		this(ModEntities.WITHER_ASH_CLOUD.get(), level);
		this.setPos(x, y, z);
	}

	public WitherAshCloud(EntityType<? extends AreaEffectCloud> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public void tick() {
		super.tick();

		if (!(this.level() instanceof ServerLevel serverLevel) || this.isWaiting()) return;

		this.hitCooldowns.entrySet().removeIf(entry -> entry.getValue() <= this.tickCount);

		float radius = this.getRadius();
		AABB box = new AABB(
			this.getX() - radius, this.getY() - 0.35, this.getZ() - radius,
			this.getX() + radius, this.getY() + CLOUD_HEIGHT, this.getZ() + radius
		);

		if (this.tickCount % 2 == 0) {
			serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
				this.getX(), this.getY() + 0.8, this.getZ(), 5, radius * 0.42, 0.45, radius * 0.42, 0.002);
			serverLevel.sendParticles(ParticleTypes.SMOKE,
				this.getX(), this.getY() + 1.15, this.getZ(), 8, radius * 0.6, 0.6, radius * 0.6, 0.002);
			serverLevel.sendParticles(ParticleTypes.WHITE_ASH,
				this.getX(), this.getY() + 0.95, this.getZ(), 12, radius * 0.65, 0.75, radius * 0.65, 0.002);
			serverLevel.sendParticles(ParticleTypes.ASH,
				this.getX(), this.getY() + 1.2, this.getZ(), 10, radius * 0.7, 0.85, radius * 0.7, 0.002);
		}

		List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, box, this::canHit);
		for (LivingEntity target : targets) {
			double dx = target.getX() - this.getX();
			double dz = target.getZ() - this.getZ();
			if (dx * dx + dz * dz > radius * radius) continue;

			target.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_DURATION, 0));
			this.hitCooldowns.put(target.getUUID(), this.tickCount + HIT_COOLDOWN);
		}

		List<WitherBlazeEntity> blazes = serverLevel.getEntitiesOfClass(WitherBlazeEntity.class, box, LivingEntity::isAlive);
		for (WitherBlazeEntity blaze : blazes) {
			double dx = blaze.getX() - this.getX();
			double dz = blaze.getZ() - this.getZ();
			if (dx * dx + dz * dz > radius * radius) continue;
			blaze.setEmpoweredTicks(Math.max(blaze.getEmpoweredTicks(), EMPOWER_REFRESH));
		}
	}

	private boolean canHit(LivingEntity target) {
		if (!target.isAlive()) return false;
		if (this.hitCooldowns.containsKey(target.getUUID())) return false;
		if (target instanceof WitherBlazeEntity) return false;

		LivingEntity owner = this.getOwner();
		return target != owner;
	}
}


