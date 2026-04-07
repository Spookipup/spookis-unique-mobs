package spookipup.uniquemobs.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Blaze;
import spookipup.uniquemobs.entity.variant.blaze.WitherBlazeEntity;

public class WitherBlazeAuraLayer extends RenderLayer<Blaze, BlazeModel<Blaze>> {

	private static final ResourceLocation WITHER_ARMOR_LOCATION =
		ResourceLocation.withDefaultNamespace("textures/entity/wither/wither_armor.png");

	private final BlazeModel<Blaze> model;

	public WitherBlazeAuraLayer(RenderLayerParent<Blaze, BlazeModel<Blaze>> parent, EntityModelSet modelSet) {
		super(parent);
		this.model = new BlazeModel<>(modelSet.bakeLayer(ModelLayers.BLAZE));
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Blaze entity,
					   float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
					   float netHeadYaw, float headPitch) {
		if (!(entity instanceof WitherBlazeEntity witherBlaze) || !witherBlaze.isEmpowered()) {
			return;
		}

		float swirlAge = entity.tickCount + partialTick;
		this.getParentModel().copyPropertiesTo(this.model);
		this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
		this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

		VertexConsumer vertexConsumer = buffer.getBuffer(
			RenderType.energySwirl(WITHER_ARMOR_LOCATION, xOffset(swirlAge), swirlAge * 0.01F % 1.0F)
		);
		this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFF808080);
	}

	private float xOffset(float ageInTicks) {
		return Mth.cos(ageInTicks * 0.02F) * 3.0F;
	}
}
