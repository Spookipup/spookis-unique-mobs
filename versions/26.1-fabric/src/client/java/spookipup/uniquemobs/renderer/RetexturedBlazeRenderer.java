package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class RetexturedBlazeRenderer extends BlazeRenderer {

	private final Identifier texture;

	public RetexturedBlazeRenderer(EntityRendererProvider.Context context, Identifier texture) {
		super(context);
		this.texture = texture;
	}

	@Override
	public Identifier getTextureLocation(LivingEntityRenderState state) {
		return this.texture;
	}
}
