package spookipup.uniquemobs.entity.variant.enderman;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.entity.ai.BlockBreakGoal;
import spookipup.uniquemobs.entity.ai.KnockupGoal;

// always hostile, uppercuts you and teleports to your landing spot.
// barely teleports on its own - it walks up and beats you senseless
public class EnragedEndermanEntity extends EnderMan {

	private static final int JUGGLE_ATTEMPTS = 5;
	private static final int FRUSTRATION_THRESHOLD = 100; // 5 seconds without hitting

	private int juggleCooldown;
	private int ticksSinceLastHit;

	public EnragedEndermanEntity(EntityType<? extends EnderMan> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return EnderMan.createAttributes()
			.add(Attributes.MAX_HEALTH, 50.0)
			.add(Attributes.ATTACK_DAMAGE, 9.0)
			.add(Attributes.MOVEMENT_SPEED, 0.38)
			.add(Attributes.FOLLOW_RANGE, 40.0);
	}

	@Override
	protected void registerGoals() {
		// don't call super - we're replacing the entire AI.
		// vanilla enderman has stare-to-provoke, idle teleport, etc. that we don't want
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(1, new KnockupGoal(this, 20, 1.2));
		this.goalSelector.addGoal(2, new BlockBreakGoal(this, 2.5F, 5));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, false));
	}

	@Override
	public void aiStep() {
		// skip vanilla enderman's persistent anger / idle teleport
		// go straight to Monster.aiStep()
		this.jumping = false;
		super.aiStep();

		if (this.level().isClientSide()) return;

		if (this.juggleCooldown > 0) this.juggleCooldown--;

		LivingEntity target = this.getTarget();
		if (target == null || !target.isAlive()) {
			this.ticksSinceLastHit = 0;
			return;
		}

		// track how long since we last hit them
		if (target.hurtTime == 10 && target.getLastHurtByMob() == this) {
			this.ticksSinceLastHit = 0;
		} else {
			this.ticksSinceLastHit++;
		}

		if (this.juggleCooldown > 0) return;

		// if target is airborne and falling, teleport to where they'll land
		Vec3 vel = target.getDeltaMovement();
		if (!target.onGround() && vel.y < -0.3) {
			Vec3 landing = predictLanding(target);
			if (landing != null && tryTeleportTo(landing)) {
				this.juggleCooldown = 10;
			}
		}

		// frustrated - can't reach the target, teleport somewhere near them
		if (this.ticksSinceLastHit >= FRUSTRATION_THRESHOLD) {
			double angle = this.random.nextDouble() * Math.PI * 2;
			double dist = 3.0 + this.random.nextDouble() * 5.0;
			Vec3 nearby = new Vec3(
				target.getX() + Math.cos(angle) * dist,
				target.getY(),
				target.getZ() + Math.sin(angle) * dist
			);
			if (this.randomTeleport(nearby.x, nearby.y, nearby.z, true)) {
				this.ticksSinceLastHit = 0;
			}
		}
	}

	private Vec3 predictLanding(LivingEntity target) {
		double x = target.getX();
		double y = target.getY();
		double z = target.getZ();
		double vy = target.getDeltaMovement().y;
		double vx = target.getDeltaMovement().x;
		double vz = target.getDeltaMovement().z;

		// simulate gravity to find approximate landing y
		for (int tick = 0; tick < 40; tick++) {
			vy -= 0.08; // gravity
			vy *= 0.98; // drag
			y += vy;
			x += vx;
			z += vz;
			vx *= 0.91;
			vz *= 0.91;

			if (y <= this.getY() + 1) {
				return new Vec3(x, this.getY(), z);
			}
		}
		return null;
	}

	private boolean tryTeleportTo(Vec3 pos) {
		for (int i = 0; i < JUGGLE_ATTEMPTS; i++) {
			double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
			double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;
			if (this.randomTeleport(pos.x + offsetX, pos.y, pos.z + offsetZ, true)) {
				return true;
			}
		}
		return false;
	}

}
