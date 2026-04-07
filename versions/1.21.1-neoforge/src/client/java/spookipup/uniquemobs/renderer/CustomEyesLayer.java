package spookipup.uniquemobs.renderer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class CustomEyesLayer<T extends LivingEntity, M extends EntityModel<T>> extends EyesLayer<T, M> {

	private final RenderType eyesRenderType;

	public CustomEyesLayer(RenderLayerParent<T, M> parent, ResourceLocation texture) {
		super(parent);
		this.eyesRenderType = RenderType.eyes(texture);
	}

	@Override
	public RenderType renderType() {
		return this.eyesRenderType;
	}
}
