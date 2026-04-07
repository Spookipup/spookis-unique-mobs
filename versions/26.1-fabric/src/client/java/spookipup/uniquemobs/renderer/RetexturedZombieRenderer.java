package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

public class RetexturedZombieRenderer extends ZombieRenderer {

	private final Identifier texture;

	public RetexturedZombieRenderer(EntityRendererProvider.Context context, Identifier texture, Identifier eyesTexture) {
		super(context);
		this.texture = texture;

		if (eyesTexture != null) {
			this.addLayer(new CustomEyesLayer<>(this, eyesTexture));
		}
	}

	@Override
	public Identifier getTextureLocation(ZombieRenderState state) {
		return this.texture;
	}
}
