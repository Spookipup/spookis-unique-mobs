package spookipup.uniquemobs.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import spookipup.uniquemobs.entity.StormArcEntity;

public class StormArcRenderer extends EntityRenderer<StormArcEntity> {

	private static final ResourceLocation EMPTY_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/white.png");

	public StormArcRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(StormArcEntity entity) {
		return EMPTY_TEXTURE;
	}

	@Override
	public boolean shouldRender(StormArcEntity entity, Frustum frustum, double camX, double camY, double camZ) {
		if (super.shouldRender(entity, frustum, camX, camY, camZ)) {
			return true;
		}

		Vec3 start = entity.position();
		Vec3 end = start.add(entity.getEndOffset());
		AABB box = new AABB(
			Math.min(start.x, end.x) - 1.0, Math.min(start.y, end.y) - 1.0, Math.min(start.z, end.z) - 1.0,
			Math.max(start.x, end.x) + 1.0, Math.max(start.y, end.y) + 1.0, Math.max(start.z, end.z) + 1.0);
		return frustum.isVisible(box);
	}

	@Override
	public void render(StormArcEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
					   MultiBufferSource buffer, int packedLight) {
		Vec3 endOffset = entity.getEndOffset();
		float boltLength = (float) endOffset.length();
		if (boltLength <= 0.01F) {
			return;
		}

		Vector3f direction = new Vector3f((float) endOffset.x, (float) endOffset.y, (float) endOffset.z);
		direction.normalize();

		poseStack.pushPose();
		poseStack.mulPose(new Quaternionf().rotateTo(new Vector3f(0.0F, 1.0F, 0.0F), direction));
		renderBolt(entity.getSeedValue(), boltLength, poseStack.last().pose(), buffer.getBuffer(RenderType.lightning()));
		poseStack.popPose();

		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void renderBolt(long seed, float boltLength, Matrix4f pose, VertexConsumer consumer) {
		float amplitude = Math.max(0.06F, Math.min(0.28F, boltLength * 0.045F));
		float branchAmplitude = amplitude * 1.35F;
		float[] xOffsets = new float[8];
		float[] zOffsets = new float[8];
		float currentX = 0.0F;
		float currentZ = 0.0F;
		RandomSource random = RandomSource.create(seed);
		float segmentLength = boltLength / 8.0F;

		for (int index = 7; index >= 0; --index) {
			xOffsets[index] = currentX;
			zOffsets[index] = currentZ;
			currentX += (random.nextFloat() - 0.5F) * 2.0F * amplitude;
			currentZ += (random.nextFloat() - 0.5F) * 2.0F * amplitude;
		}

		float originX = currentX;
		float originZ = currentZ;
		for (int layer = 0; layer < 4; ++layer) {
			RandomSource layerRandom = RandomSource.create(seed);

			for (int branch = 0; branch < 3; ++branch) {
				int startIndex = 7;
				int endIndex = 0;
				if (branch > 0) {
					startIndex = 7 - branch;
					endIndex = startIndex - 2;
				}

				float nextX = xOffsets[startIndex] - originX;
				float nextZ = zOffsets[startIndex] - originZ;
				for (int segment = startIndex; segment >= endIndex; --segment) {
					float previousX = nextX;
					float previousZ = nextZ;
					if (branch == 0) {
						nextX += (layerRandom.nextFloat() - 0.5F) * 2.0F * amplitude;
						nextZ += (layerRandom.nextFloat() - 0.5F) * 2.0F * amplitude;
					} else {
						nextX += (layerRandom.nextFloat() - 0.5F) * 2.0F * branchAmplitude;
						nextZ += (layerRandom.nextFloat() - 0.5F) * 2.0F * branchAmplitude;
					}

					float widthA = 0.028F + layer * 0.05F;
					if (branch == 0) {
						widthA *= segment * 0.1F + 1.0F;
					}
					float widthB = 0.028F + layer * 0.05F;
					if (branch == 0) {
						widthB *= (segment - 1.0F) * 0.1F + 1.0F;
					}

					quad(pose, consumer, nextX, nextZ, segment, previousX, previousZ, widthA, widthB, segmentLength, false, false, true, false);
					quad(pose, consumer, nextX, nextZ, segment, previousX, previousZ, widthA, widthB, segmentLength, true, false, true, true);
					quad(pose, consumer, nextX, nextZ, segment, previousX, previousZ, widthA, widthB, segmentLength, true, true, false, true);
					quad(pose, consumer, nextX, nextZ, segment, previousX, previousZ, widthA, widthB, segmentLength, false, true, false, false);
				}
			}
		}
	}

	private static void quad(Matrix4f pose, VertexConsumer consumer,
							 float x1, float z1, int yIndex, float x2, float z2,
							 float width1, float width2, float segmentLength,
							 boolean flipX1, boolean flipZ1, boolean flipX2, boolean flipZ2) {
		addVertex(consumer, pose, x1 + (flipX1 ? width2 : -width2), yIndex * segmentLength, z1 + (flipZ1 ? width2 : -width2));
		addVertex(consumer, pose, x2 + (flipX1 ? width1 : -width1), (yIndex + 1) * segmentLength, z2 + (flipZ1 ? width1 : -width1));
		addVertex(consumer, pose, x2 + (flipX2 ? width1 : -width1), (yIndex + 1) * segmentLength, z2 + (flipZ2 ? width1 : -width1));
		addVertex(consumer, pose, x1 + (flipX2 ? width2 : -width2), yIndex * segmentLength, z1 + (flipZ2 ? width2 : -width2));
	}

	private static void addVertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z) {
		consumer.addVertex(pose, x, y, z).setColor(0.45F, 0.45F, 0.5F, 0.3F);
	}
}
