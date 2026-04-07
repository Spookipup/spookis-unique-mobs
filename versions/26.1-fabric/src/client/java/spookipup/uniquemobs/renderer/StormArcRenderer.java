package spookipup.uniquemobs.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import spookipup.uniquemobs.entity.StormArcEntity;

// vanilla-style lightning, just stretched between two fixed points
public class StormArcRenderer extends EntityRenderer<StormArcEntity, StormArcRenderState> {

	public StormArcRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public StormArcRenderState createRenderState() {
		return new StormArcRenderState();
	}

	@Override
	public void extractRenderState(StormArcEntity entity, StormArcRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.seed = entity.getSeedValue();
		state.endOffset = entity.getEndOffset();
		state.boltLength = (float) state.endOffset.length();
	}

	@Override
	public void submit(StormArcRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
					   CameraRenderState camera) {
		if (state.boltLength <= 0.01F) return;

		Vec3 endOffset = state.endOffset;
		Vector3f direction = new Vector3f((float) endOffset.x, (float) endOffset.y, (float) endOffset.z);
		direction.normalize();

		poseStack.pushPose();
		poseStack.mulPose(new Quaternionf().rotateTo(new Vector3f(0.0F, 1.0F, 0.0F), direction));
		collector.submitCustomGeometry(poseStack, RenderTypes.lightning(),
			(pose, consumer) -> renderBolt(state.seed, state.boltLength, pose.pose(), consumer));
		poseStack.popPose();
	}

	@Override
	protected boolean affectedByCulling(StormArcEntity entity) {
		return false;
	}

	private static void renderBolt(long seed, float boltLength, Matrix4fc pose, VertexConsumer consumer) {
		float amplitude = Math.max(0.06F, Math.min(0.28F, boltLength * 0.045F));
		float branchAmplitude = amplitude * 1.35F;
		float[] xOffsets = new float[8];
		float[] zOffsets = new float[8];
		float currentX = 0.0F;
		float currentZ = 0.0F;
		RandomSource random = RandomSource.createThreadLocalInstance(seed);
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
			RandomSource layerRandom = RandomSource.createThreadLocalInstance(seed);

			for (int branch = 0; branch < 3; ++branch) {
				int startIndex = 7;
				int endIndex = 0;
				if (branch > 0) {
					startIndex = 7 - branch;
				}
				if (branch > 0) {
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

	private static void quad(Matrix4fc pose, VertexConsumer consumer,
							 float x1, float z1, int yIndex, float x2, float z2,
							 float width1, float width2, float segmentLength,
							 boolean flipX1, boolean flipZ1, boolean flipX2, boolean flipZ2) {
		addVertex(consumer, pose, x1 + (flipX1 ? width2 : -width2), yIndex * segmentLength, z1 + (flipZ1 ? width2 : -width2));
		addVertex(consumer, pose, x2 + (flipX1 ? width1 : -width1), (yIndex + 1) * segmentLength, z2 + (flipZ1 ? width1 : -width1));
		addVertex(consumer, pose, x2 + (flipX2 ? width1 : -width1), (yIndex + 1) * segmentLength, z2 + (flipZ2 ? width1 : -width1));
		addVertex(consumer, pose, x1 + (flipX2 ? width2 : -width2), yIndex * segmentLength, z1 + (flipZ2 ? width2 : -width2));
	}

	private static void addVertex(VertexConsumer consumer, Matrix4fc pose, float x, float y, float z) {
		consumer.addVertex(pose, x, y, z).setColor(0.45F, 0.45F, 0.5F, 0.3F);
	}
}
