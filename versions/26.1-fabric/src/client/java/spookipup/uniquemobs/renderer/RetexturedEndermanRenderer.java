package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.Identifier;

public class RetexturedEndermanRenderer extends EndermanRenderer {

	private final Identifier texture;

	public RetexturedEndermanRenderer(EntityRendererProvider.Context context, Identifier texture, Identifier eyesTexture) {
		super(context);
		this.texture = texture;

		if (eyesTexture != null) {
			this.layers.removeIf(layer -> layer instanceof EnderEyesLayer);
			this.addLayer(new CustomEyesLayer<>(this, eyesTexture));
		}
	}

	@Override
	public Identifier getTextureLocation(EndermanRenderState state) {
		return this.texture;
	}
}
