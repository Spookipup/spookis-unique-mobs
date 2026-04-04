package spookipup.uniquemobs.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ghast;

public class RetexturedGhastRenderer extends GhastRenderer {

	private final ResourceLocation texture;
	private final ResourceLocation shootingTexture;
	private float entityScale = 1.0F;
	private float visualYOffset = 0.0F;

	public RetexturedGhastRenderer(EntityRendererProvider.Context context, ResourceLocation texture,
								   ResourceLocation shootingTexture, ResourceLocation eyesTexture) {
		super(context);
		this.texture = texture;
		this.shootingTexture = shootingTexture;

		if (eyesTexture != null) {
			this.addLayer(new CustomEyesLayer<>(this, eyesTexture));
		}
	}

	// fullbright glow that swaps texture when charging (for ragelings etc)
	public RetexturedGhastRenderer(EntityRendererProvider.Context context, ResourceLocation texture,
								   ResourceLocation shootingTexture, ResourceLocation eyesTexture,
								   ResourceLocation chargingEyesTexture) {
		super(context);
		this.texture = texture;
		this.shootingTexture = shootingTexture;

		if (eyesTexture != null && chargingEyesTexture != null) {
			this.addLayer(new GhastChargingEyesLayer<>(this, eyesTexture, chargingEyesTexture));
		} else if (eyesTexture != null) {
			this.addLayer(new CustomEyesLayer<>(this, eyesTexture));
		}
	}

	public RetexturedGhastRenderer withScale(float scale) {
		return this.withScale(scale, 0.0F);
	}

	public RetexturedGhastRenderer withScale(float scale, float visualYOffset) {
		this.entityScale = scale;
		this.visualYOffset = visualYOffset;
		return this;
	}

	@Override
	public void render(Ghast entity, float entityYaw, float partialTick, PoseStack poseStack,
					   net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight) {
		poseStack.pushPose();
		if (this.visualYOffset != 0.0F) {
			poseStack.translate(0.0F, this.visualYOffset, 0.0F);
		}
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
		poseStack.popPose();
	}

	@Override
	protected void scale(Ghast entity, PoseStack poseStack, float partialTick) {
		float scale = 4.5F * this.entityScale;
		poseStack.scale(scale, scale, scale);
	}

	@Override
	public ResourceLocation getTextureLocation(Ghast entity) {
		return entity.isCharging() && this.shootingTexture != null ? this.shootingTexture : this.texture;
	}
}
