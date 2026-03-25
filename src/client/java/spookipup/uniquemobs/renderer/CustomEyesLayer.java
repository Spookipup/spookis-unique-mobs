package spookipup.uniquemobs.renderer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class CustomEyesLayer<S extends EntityRenderState, M extends EntityModel<S>> extends EyesLayer<S, M> {

	private final RenderType eyesRenderType;

	public CustomEyesLayer(RenderLayerParent<S, M> parent, Identifier texture) {
		super(parent);
		this.eyesRenderType = RenderTypes.eyes(texture);
	}

	@Override
	public RenderType renderType() {
		return this.eyesRenderType;
	}
}
