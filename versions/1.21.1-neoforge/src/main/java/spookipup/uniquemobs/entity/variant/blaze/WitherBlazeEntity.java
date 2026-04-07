package spookipup.uniquemobs.entity.variant.blaze;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import spookipup.uniquemobs.entity.ai.WitherBlazeAttackGoal;

import java.util.List;

// ash nearby is what powers this one up
public class WitherBlazeEntity extends Blaze {

	private static final EntityDataAccessor<Integer> DATA_EMPOWERED_TICKS =
		SynchedEntityData.defineId(WitherBlazeEntity.class, EntityDataSerializers.INT);

	private static final int AURA_INTERVAL = 12;
	private static final double AURA_RADIUS = 2.8;

	public WitherBlazeEntity(EntityType<? extends Blaze> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Blaze.createAttributes()
			.add(Attributes.MAX_HEALTH, 24.0)
			.add(Attributes.ATTACK_DAMAGE, 4.0)
			.add(Attributes.FOLLOW_RANGE, 40.0)
			.add(Attributes.MOVEMENT_SPEED, 0.24);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_EMPOWERED_TICKS, 0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new WitherBlazeAttackGoal(this));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public boolean canBeAffected(MobEffectInstance effect) {
		if (effect.getEffect() == MobEffects.WITHER) return false;
		return super.canBeAffected(effect);
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.level().isClientSide()) {
			int empoweredTicks = this.getEmpoweredTicks();
			if (empoweredTicks > 0) {
				this.setEmpoweredTicks(empoweredTicks - 1);
				if (this.tickCount % AURA_INTERVAL == 0 && this.level() instanceof ServerLevel serverLevel) {
					applyEmpoweredAura(serverLevel);
				}
			}
		}

		if (this.getTarget() instanceof Player player && (player.isCreative() || player.isSpectator())) {
			this.setTarget(null);
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level().isClientSide() && this.isAlive() && this.isEmpowered()) {
			this.level().addParticle(ParticleTypes.SMOKE, this.getRandomX(0.45), this.getRandomY(), this.getRandomZ(0.45),
				0.0, 0.01, 0.0);
			this.level().addParticle(ParticleTypes.WHITE_ASH, this.getRandomX(0.55), this.getRandomY() - 0.1, this.getRandomZ(0.55),
				0.0, 0.01, 0.0);
		}
	}

	private void applyEmpoweredAura(ServerLevel serverLevel) {
		AABB box = this.getBoundingBox().inflate(AURA_RADIUS);
		List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, box,
			entity -> entity != this && entity.isAlive() && !(entity instanceof WitherBlazeEntity));

		for (LivingEntity target : targets) {
			if (target.distanceToSqr(this) > AURA_RADIUS * AURA_RADIUS) continue;
			target.addEffect(new MobEffectInstance(MobEffects.WITHER, 45, 0));
		}
	}

	public boolean isEmpowered() {
		return this.getEmpoweredTicks() > 0;
	}

	public int getEmpoweredTicks() {
		return this.entityData.get(DATA_EMPOWERED_TICKS);
	}

	public void setEmpoweredTicks(int ticks) {
		this.entityData.set(DATA_EMPOWERED_TICKS, Math.max(0, ticks));
	}
}


