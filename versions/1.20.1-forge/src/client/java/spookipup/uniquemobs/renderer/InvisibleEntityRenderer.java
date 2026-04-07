package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class InvisibleEntityRenderer<T extends Entity> extends EntityRenderer<T> {

	private static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation("minecraft", "textures/misc/white.png");

	public InvisibleEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return EMPTY_TEXTURE;
	}
}
