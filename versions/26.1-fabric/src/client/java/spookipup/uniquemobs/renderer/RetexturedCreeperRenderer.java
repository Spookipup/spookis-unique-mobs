package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;

public class RetexturedCreeperRenderer extends CreeperRenderer {

	private final Identifier texture;

	public RetexturedCreeperRenderer(EntityRendererProvider.Context context, Identifier texture, Identifier eyesTexture) {
		super(context);
		this.texture = texture;

		if (eyesTexture != null) {
			this.addLayer(new CustomEyesLayer<>(this, eyesTexture));
		}
	}

	@Override
	public Identifier getTextureLocation(CreeperRenderState state) {
		return this.texture;
	}
}
