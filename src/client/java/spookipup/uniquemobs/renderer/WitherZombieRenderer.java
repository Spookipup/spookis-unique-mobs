package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import spookipup.uniquemobs.UniqueMobs;
import spookipup.uniquemobs.entity.variant.zombie.WitherZombieEntity;

public class WitherZombieRenderer extends MobRenderer<WitherZombieEntity, WitherZombieRenderState, WitherZombieModel> {

	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
		UniqueMobs.MOD_ID, "textures/entity/zombie/wither_zombie.png"
	);
	private static final Identifier EYES_TEXTURE = Identifier.fromNamespaceAndPath(
		UniqueMobs.MOD_ID, "textures/entity/zombie/wither_zombie_eyes.png"
	);

	public WitherZombieRenderer(EntityRendererProvider.Context context) {
		super(context, new WitherZombieModel(context.bakeLayer(WitherZombieModel.LAYER_LOCATION)), 0.6F);
		this.addLayer(new CustomEyesLayer<>(this, EYES_TEXTURE));
	}

	@Override
	public WitherZombieRenderState createRenderState() {
		return new WitherZombieRenderState();
	}

	@Override
	public void extractRenderState(WitherZombieEntity entity, WitherZombieRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.isAggressive = entity.isAggressive();

		float[] entityXRots = entity.getHeadXRots();
		float[] entityYRots = entity.getHeadYRots();
		System.arraycopy(entityXRots, 0, state.xHeadRots, 0, 2);
		System.arraycopy(entityYRots, 0, state.yHeadRots, 0, 2);
	}

	@Override
	public Identifier getTextureLocation(WitherZombieRenderState state) {
		return TEXTURE;
	}
}
