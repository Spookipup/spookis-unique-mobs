package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Blaze;

public class RetexturedBlazeRenderer extends BlazeRenderer {

	private final ResourceLocation texture;

	public RetexturedBlazeRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
		super(context);
		this.texture = texture;
	}

	@Override
	public ResourceLocation getTextureLocation(Blaze entity) {
		return this.texture;
	}
}
