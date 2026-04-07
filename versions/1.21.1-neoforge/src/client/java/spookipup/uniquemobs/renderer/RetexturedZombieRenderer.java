package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

public class RetexturedZombieRenderer extends ZombieRenderer {

	private final ResourceLocation texture;

	public RetexturedZombieRenderer(EntityRendererProvider.Context context, ResourceLocation texture, ResourceLocation eyesTexture) {
		super(context);
		this.texture = texture;

		if (eyesTexture != null) {
			this.addLayer(new CustomEyesLayer<>(this, eyesTexture));
		}
	}

	@Override
	public ResourceLocation getTextureLocation(Zombie entity) {
		return this.texture;
	}
}
