package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import spookipup.uniquemobs.UniqueMobs;
import spookipup.uniquemobs.entity.variant.zombie.WitherZombieEntity;

public class WitherZombieRenderer extends MobRenderer<WitherZombieEntity, WitherZombieModel> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(
		UniqueMobs.MOD_ID, "textures/entity/zombie/wither_zombie.png"
	);
	private static final ResourceLocation EYES_TEXTURE = new ResourceLocation(
		UniqueMobs.MOD_ID, "textures/entity/zombie/wither_zombie_eyes.png"
	);

	public WitherZombieRenderer(EntityRendererProvider.Context context) {
		super(context, new WitherZombieModel(context.bakeLayer(WitherZombieModel.LAYER_LOCATION)), 0.6F);
		this.addLayer(new CustomEyesLayer<>(this, EYES_TEXTURE));
	}

	@Override
	public ResourceLocation getTextureLocation(WitherZombieEntity entity) {
		return TEXTURE;
	}
}
