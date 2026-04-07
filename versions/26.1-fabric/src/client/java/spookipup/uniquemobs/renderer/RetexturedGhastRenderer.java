package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Ghast;

public class RetexturedGhastRenderer extends GhastRenderer {

	private final Identifier texture;
	private final Identifier shootingTexture;

	public RetexturedGhastRenderer(EntityRendererProvider.Context context, Identifier texture,
								   Identifier shootingTexture, Identifier eyesTexture) {
		super(context);
		this.texture = texture;
		this.shootingTexture = shootingTexture;

		if (eyesTexture != null) {
			this.addLayer(new CustomEyesLayer<>(this, eyesTexture));
		}
	}

	// fullbright glow that swaps texture when charging (for ragelings etc)
	public RetexturedGhastRenderer(EntityRendererProvider.Context context, Identifier texture,
								   Identifier shootingTexture, Identifier eyesTexture,
								   Identifier chargingEyesTexture) {
		super(context);
		this.texture = texture;
		this.shootingTexture = shootingTexture;

		if (chargingEyesTexture != null) {
			this.addLayer(new GhastChargingEyesLayer<>(this, eyesTexture, chargingEyesTexture));
		} else if (eyesTexture != null) {
			this.addLayer(new CustomEyesLayer<>(this, eyesTexture));
		}
	}

	@Override
	public void extractRenderState(Ghast ghast, GhastRenderState state, float partialTick) {
		super.extractRenderState(ghast, state, partialTick);
		state.isCharging = ghast.isCharging();
	}

	@Override
	public Identifier getTextureLocation(GhastRenderState state) {
		return state.isCharging && this.shootingTexture != null ? this.shootingTexture : this.texture;
	}
}
