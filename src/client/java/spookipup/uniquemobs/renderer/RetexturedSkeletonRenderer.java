package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;

public class RetexturedSkeletonRenderer extends SkeletonRenderer {

	private final Identifier texture;

	public RetexturedSkeletonRenderer(EntityRendererProvider.Context context, Identifier texture, Identifier eyesTexture) {
		super(context);
		this.texture = texture;

		if (eyesTexture != null) {
			this.addLayer(new CustomEyesLayer<>(this, eyesTexture));
		}
	}

	@Override
	public Identifier getTextureLocation(SkeletonRenderState state) {
		return this.texture;
	}
}
