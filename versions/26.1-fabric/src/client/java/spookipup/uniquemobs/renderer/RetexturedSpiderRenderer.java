package spookipup.uniquemobs.renderer;

import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.spider.Spider;

public class RetexturedSpiderRenderer extends SpiderRenderer<Spider> {

	private final Identifier texture;

	public RetexturedSpiderRenderer(EntityRendererProvider.Context context, Identifier texture, Identifier eyesTexture) {
		super(context);
		this.texture = texture;

		if (eyesTexture != null) {
			this.layers.removeIf(layer -> layer instanceof SpiderEyesLayer<?>);
			this.addLayer(new CustomEyesLayer<LivingEntityRenderState, SpiderModel>(this, eyesTexture));
		}
	}

	@Override
	public Identifier getTextureLocation(LivingEntityRenderState state) {
		return this.texture;
	}
}
