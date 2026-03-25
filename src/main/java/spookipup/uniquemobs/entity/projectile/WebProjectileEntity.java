package spookipup.uniquemobs.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import spookipup.uniquemobs.entity.variant.spider.WebSpinnerSpiderEntity;
import spookipup.uniquemobs.registry.ModEntities;

// places a temp cobweb where it lands
public class WebProjectileEntity extends ThrowableProjectile implements ItemSupplier {

	private static final ItemStack ICON = new ItemStack(Items.STRING);

	public WebProjectileEntity(EntityType<? extends WebProjectileEntity> type, Level level) {
		super(type, level);
	}

	public WebProjectileEntity(Level level, LivingEntity shooter) {
		super(ModEntities.WEB_PROJECTILE, level);
		this.setOwner(shooter);
		this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public ItemStack getItem() {
		return ICON;
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		if (!this.level().isClientSide()) {
			placeWeb(result.getEntity().blockPosition());
		}
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (!this.level().isClientSide()) {
			if (result instanceof BlockHitResult blockHit) {
				BlockPos hitPos = blockHit.getBlockPos().relative(blockHit.getDirection());
				placeWeb(hitPos);
			}
			this.level().broadcastEntityEvent(this, (byte) 3);
			this.discard();
		}
	}

	private void placeWeb(BlockPos pos) {
		BlockState state = this.level().getBlockState(pos);
		if (state.isAir() || state.canBeReplaced()) {
			this.level().setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
			if (this.getOwner() instanceof WebSpinnerSpiderEntity spider) {
				spider.trackPlacedWeb(pos);
			}
		}
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 3) {
			for (int i = 0; i < 6; i++) {
				this.level().addParticle(
					ParticleTypes.ITEM_SNOWBALL,
					this.getX(), this.getY(), this.getZ(),
					(this.random.nextDouble() - 0.5) * 0.08,
					(this.random.nextDouble() - 0.5) * 0.08,
					(this.random.nextDouble() - 0.5) * 0.08
				);
			}
		}
	}

	@Override
	protected double getDefaultGravity() {
		return 0.03;
	}
}
