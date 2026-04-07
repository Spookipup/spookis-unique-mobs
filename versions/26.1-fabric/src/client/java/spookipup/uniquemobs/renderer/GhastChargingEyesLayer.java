package spookipup.uniquemobs.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

// fullbright layer that can either swap textures or only render while charging
public class GhastChargingEyesLayer<M extends EntityModel<GhastRenderState>> extends EyesLayer<GhastRenderState, M> {

	private final RenderType normalType;
	private final RenderType chargingType;

	private boolean charging;

	public GhastChargingEyesLayer(RenderLayerParent<GhastRenderState, M> parent,
								  Identifier normalTexture, Identifier chargingTexture) {
		super(parent);
		this.normalType = normalTexture == null ? null : RenderTypes.eyes(normalTexture);
		this.chargingType = RenderTypes.eyes(chargingTexture);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
					   GhastRenderState state, float yRot, float xRot) {
		this.charging = state.isCharging;
		if (!this.charging && this.normalType == null) return;
		super.submit(poseStack, collector, packedLight, state, yRot, xRot);
	}

	@Override
	public RenderType renderType() {
		return this.charging ? this.chargingType : this.normalType;
	}
}
