package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Blaze;
import spookipup.uniquemobs.entity.variant.blaze.WitherBlazeEntity;

public class WitherBlazeRenderer extends BlazeRenderer {

	private final Identifier texture;

	public WitherBlazeRenderer(EntityRendererProvider.Context context, Identifier texture) {
		super(context);
		this.texture = texture;
		this.addLayer(new WitherBlazeAuraLayer(this, context.getModelSet()));
	}

	@Override
	public Identifier getTextureLocation(LivingEntityRenderState state) {
		return this.texture;
	}

	@Override
	public WitherBlazeRenderState createRenderState() {
		return new WitherBlazeRenderState();
	}

	@Override
	public void extractRenderState(Blaze blaze, LivingEntityRenderState state, float partialTick) {
		super.extractRenderState(blaze, state, partialTick);
		if (state instanceof WitherBlazeRenderState witherState && blaze instanceof WitherBlazeEntity witherBlaze) {
			witherState.isEmpowered = witherBlaze.isEmpowered();
		}
	}
}
