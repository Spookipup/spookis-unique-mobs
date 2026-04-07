package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;

public class RetexturedSkeletonRenderer extends SkeletonRenderer<AbstractSkeleton> {

	private final ResourceLocation texture;

	public RetexturedSkeletonRenderer(EntityRendererProvider.Context context, ResourceLocation texture, ResourceLocation eyesTexture) {
		super(context);
		this.texture = texture;

		if (eyesTexture != null) {
			this.addLayer(new CustomEyesLayer<>(this, eyesTexture));
		}
	}

	@Override
	public ResourceLocation getTextureLocation(AbstractSkeleton entity) {
		return this.texture;
	}
}
