package spookipup.uniquemobs.renderer;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.blaze.BlazeModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class WitherBlazeAuraLayer extends EnergySwirlLayer<LivingEntityRenderState, BlazeModel> {

	private static final Identifier WITHER_ARMOR_LOCATION =
		Identifier.withDefaultNamespace("textures/entity/wither/wither_armor.png");

	private final BlazeModel model;

	public WitherBlazeAuraLayer(RenderLayerParent<LivingEntityRenderState, BlazeModel> parent, EntityModelSet modelSet) {
		super(parent);
		this.model = new BlazeModel(modelSet.bakeLayer(ModelLayers.BLAZE));
	}

	@Override
	protected boolean isPowered(LivingEntityRenderState state) {
		return state instanceof WitherBlazeRenderState witherState && witherState.isEmpowered;
	}

	@Override
	protected float xOffset(float ageInTicks) {
		return Mth.cos(ageInTicks * 0.02F) * 3.0F;
	}

	@Override
	protected Identifier getTextureLocation() {
		return WITHER_ARMOR_LOCATION;
	}

	@Override
	protected BlazeModel model() {
		return this.model;
	}
}
