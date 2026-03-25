package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.EnderMan;

public class RetexturedEndermanRenderer extends EndermanRenderer {

	private final ResourceLocation texture;

	public RetexturedEndermanRenderer(EntityRendererProvider.Context context, ResourceLocation texture, ResourceLocation eyesTexture) {
		super(context);
		this.texture = texture;

		if (eyesTexture != null) {
			this.layers.removeIf(layer -> layer instanceof EnderEyesLayer);
			this.addLayer(new CustomEyesLayer<>(this, eyesTexture));
		}
	}

	@Override
	public ResourceLocation getTextureLocation(EnderMan entity) {
		return this.texture;
	}
}
