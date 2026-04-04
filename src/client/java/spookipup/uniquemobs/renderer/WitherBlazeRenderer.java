package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Blaze;

public class WitherBlazeRenderer extends BlazeRenderer {

	private final ResourceLocation texture;

	public WitherBlazeRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
		super(context);
		this.texture = texture;
		this.addLayer(new WitherBlazeAuraLayer(this, context.getModelSet()));
	}

	@Override
	public ResourceLocation getTextureLocation(Blaze entity) {
		return this.texture;
	}
}
