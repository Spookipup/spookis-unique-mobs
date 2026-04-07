package spookipup.uniquemobs.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ghast;

// fullbright layer that can either swap textures or only render while charging
public class GhastChargingEyesLayer<T extends Ghast, M extends EntityModel<T>> extends EyesLayer<T, M> {

	private final RenderType normalType;
	private final RenderType chargingType;

	private boolean charging;

	public GhastChargingEyesLayer(RenderLayerParent<T, M> parent,
								  ResourceLocation normalTexture, ResourceLocation chargingTexture) {
		super(parent);
		this.normalType = normalTexture == null ? null : RenderType.eyes(normalTexture);
		this.chargingType = RenderType.eyes(chargingTexture);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
					   T entity, float limbSwing, float limbSwingAmount,
					   float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		this.charging = entity.isCharging();
		if (!this.charging && this.normalType == null) return;
		super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount,
			partialTick, ageInTicks, netHeadYaw, headPitch);
	}

	@Override
	public RenderType renderType() {
		return this.charging ? this.chargingType : this.normalType;
	}
}
