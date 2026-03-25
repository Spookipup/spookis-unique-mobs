package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Creeper;

public class RetexturedCreeperRenderer extends CreeperRenderer {

	private final ResourceLocation texture;

	public RetexturedCreeperRenderer(EntityRendererProvider.Context context, ResourceLocation texture, ResourceLocation eyesTexture) {
		super(context);
		this.texture = texture;

		if (eyesTexture != null) {
			this.addLayer(new CustomEyesLayer<>(this, eyesTexture));
		}
	}

	@Override
	public ResourceLocation getTextureLocation(Creeper entity) {
		return this.texture;
	}
}
