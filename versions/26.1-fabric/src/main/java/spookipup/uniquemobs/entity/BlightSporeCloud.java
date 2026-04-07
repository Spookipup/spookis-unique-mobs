package spookipup.uniquemobs.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import spookipup.uniquemobs.entity.variant.ghast.BlightlingEntity;
import spookipup.uniquemobs.registry.ModEntities;
import spookipup.uniquemobs.registry.ModEffects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// extra hit cooldown so this doesn't stack too fast
public class BlightSporeCloud extends AreaEffectCloud {

	private static final int HIT_COOLDOWN = 14;
	private static final double CLOUD_HEIGHT = 2.4;

	private final Map<UUID, Integer> hitCooldowns = new HashMap<>();

	public BlightSporeCloud(ServerLevel level, double x, double y, double z) {
		this(ModEntities.BLIGHT_SPORE_CLOUD, level);
		this.setPos(x, y, z);
	}

	public BlightSporeCloud(EntityType<? extends AreaEffectCloud> entityType, Level level) {
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
			serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
				this.getX(), this.getY() + 0.9, this.getZ(), 6, radius * 0.4, 0.4, radius * 0.4, 0.002);
			serverLevel.sendParticles(ParticleTypes.MYCELIUM,
				this.getX(), this.getY() + 1.0, this.getZ(), 12, radius * 0.65, 0.7, radius * 0.65, 0.002);
			serverLevel.sendParticles(ParticleTypes.ITEM_SLIME,
				this.getX(), this.getY() + 0.7, this.getZ(), 3, radius * 0.22, 0.28, radius * 0.22, 0.001);
		}

		List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, box, this::canHit);
		for (LivingEntity target : targets) {
			double dx = target.getX() - this.getX();
			double dz = target.getZ() - this.getZ();
			if (dx * dx + dz * dz > radius * radius) continue;

			ModEffects.addBlight(target, 1);
			this.hitCooldowns.put(target.getUUID(), this.tickCount + HIT_COOLDOWN);
		}
	}

	private boolean canHit(LivingEntity target) {
		if (!target.isAlive()) return false;
		if (this.hitCooldowns.containsKey(target.getUUID())) return false;

		LivingEntity owner = this.getOwner();
		if (target == owner) return false;
		if (target instanceof BlightlingEntity) return false;
		return true;
	}
}
