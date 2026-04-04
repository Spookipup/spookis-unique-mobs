package spookipup.uniquemobs.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import spookipup.uniquemobs.entity.variant.blaze.SoulBlazeEntity;
import spookipup.uniquemobs.entity.variant.ghast.SkitterlingEntity;
import spookipup.uniquemobs.registry.ModEntities;
import spookipup.uniquemobs.registry.ModEffects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// trail clouds hit fast, so gate repeat hits per target
public class SoulFireTrailCloud extends AreaEffectCloud {

	private static final int HIT_COOLDOWN = 10;
	private static final float CONTACT_DAMAGE = 2.0F;
	private static final int SOUL_SCORCH_DURATION = 90;

	private final Map<UUID, Integer> hitCooldowns = new HashMap<>();

	public SoulFireTrailCloud(ServerLevel level, double x, double y, double z) {
		this(ModEntities.SOUL_FIRE_TRAIL_CLOUD, level);
		this.setPos(x, y, z);
	}

	public SoulFireTrailCloud(EntityType<? extends AreaEffectCloud> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public void tick() {
		super.tick();

		if (!(this.level() instanceof ServerLevel serverLevel) || this.isWaiting()) return;

		this.hitCooldowns.entrySet().removeIf(entry -> entry.getValue() <= this.tickCount);

		float radius = this.getRadius();
		AABB box = new AABB(
			this.getX() - radius, this.getY() - 0.4, this.getZ() - radius,
			this.getX() + radius, this.getY() + 1.2, this.getZ() + radius
		);

		LivingEntity owner = this.getOwner();
		List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, box, this::canHit);
		for (LivingEntity target : targets) {
			if (target.distanceToSqr(this.getX(), target.getY(), this.getZ()) > radius * radius) continue;

			target.hurt(this.damageSources().magic(), CONTACT_DAMAGE);
			target.addEffect(new MobEffectInstance(ModEffects.SOUL_SCORCH, SOUL_SCORCH_DURATION, 0));
			this.hitCooldowns.put(target.getUUID(), this.tickCount + HIT_COOLDOWN);
		}
	}

	private boolean canHit(LivingEntity target) {
		if (!target.isAlive()) return false;
		if (this.hitCooldowns.containsKey(target.getUUID())) return false;

		LivingEntity owner = this.getOwner();
		if (target == owner) return false;
		if (target instanceof SkitterlingEntity) return false;
		if (target instanceof SoulBlazeEntity) return false;
		return true;
	}
}


