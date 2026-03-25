package spookipup.uniquemobs.renderer;

import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Spider;

public class RetexturedSpiderRenderer extends SpiderRenderer<Spider> {

	private final ResourceLocation texture;

	public RetexturedSpiderRenderer(EntityRendererProvider.Context context, ResourceLocation texture, ResourceLocation eyesTexture) {
		super(context);
		this.texture = texture;

		if (eyesTexture != null) {
			this.layers.removeIf(layer -> layer instanceof SpiderEyesLayer);
			this.addLayer(new CustomEyesLayer<Spider, SpiderModel<Spider>>(this, eyesTexture));
		}
	}

	@Override
	public ResourceLocation getTextureLocation(Spider entity) {
		return this.texture;
	}
}
