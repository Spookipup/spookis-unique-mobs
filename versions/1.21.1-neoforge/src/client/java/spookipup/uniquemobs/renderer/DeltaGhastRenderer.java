package spookipup.uniquemobs.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import spookipup.uniquemobs.UniqueMobs;
import spookipup.uniquemobs.entity.variant.ghast.DeltaGhastEntity;

public class DeltaGhastRenderer extends GhastRenderer {

	private static final ResourceLocation BEAM_TEXTURE = ResourceLocation.fromNamespaceAndPath(
		UniqueMobs.MOD_ID, "textures/entity/ghast/delta_ghast_beam.png");
	private static final RenderType BEAM_CORE_TYPE = RenderType.beaconBeam(BEAM_TEXTURE, false);
	private static final RenderType BEAM_GLOW_TYPE = RenderType.beaconBeam(BEAM_TEXTURE, true);

	private static final float ENTITY_SCALE = 0.8F;
	private static final float ENTITY_Y_OFFSET = 0.12F;
	private static final float MAX_BEAM_RANGE = 48.0F;
	private static final float BEAM_WIDTH = 0.35F;
	private static final float GLOW_WIDTH = 0.8F;
	private static final int FULL_BRIGHT = 15728880;

	private final ResourceLocation texture;
	private final ResourceLocation chargingTexture;

	public DeltaGhastRenderer(EntityRendererProvider.Context context, ResourceLocation texture,
							  ResourceLocation chargingTexture, ResourceLocation eyesTexture,
							  ResourceLocation chargingEyesTexture) {
		super(context);
		this.texture = texture;
		this.chargingTexture = chargingTexture;

		if (eyesTexture != null && chargingEyesTexture != null) {
			this.addLayer(new GhastChargingEyesLayer<>(this, eyesTexture, chargingEyesTexture));
		} else if (eyesTexture != null) {
			this.addLayer(new CustomEyesLayer<>(this, eyesTexture));
		}
	}

	@Override
	protected void scale(Ghast entity, PoseStack poseStack, float partialTick) {
		float scale = 4.5F * ENTITY_SCALE;
		poseStack.scale(scale, scale, scale);
	}

	@Override
	public ResourceLocation getTextureLocation(Ghast entity) {
		boolean angry = entity.isCharging()
			|| (entity instanceof DeltaGhastEntity delta && delta.isFiring());
		return angry && this.chargingTexture != null ? this.chargingTexture : this.texture;
	}

	@Override
	public boolean shouldRender(Ghast ghast, Frustum frustum, double camX, double camY, double camZ) {
		if (super.shouldRender(ghast, frustum, camX, camY, camZ)) {
			return true;
		}

		if (ghast instanceof DeltaGhastEntity delta && delta.isFiring()) {
			Vec3 eye = ghast.getEyePosition().add(0.0, DeltaGhastEntity.MOUTH_Y_OFFSET, 0.0);
			Vec3 dir = delta.getBeamDirection(1.0F);
			Vec3 end = eye.add(dir.scale(MAX_BEAM_RANGE));
			AABB beamBox = new AABB(
				Math.min(eye.x, end.x) - 1.0, Math.min(eye.y, end.y) - 1.0, Math.min(eye.z, end.z) - 1.0,
				Math.max(eye.x, end.x) + 1.0, Math.max(eye.y, end.y) + 1.0, Math.max(eye.z, end.z) + 1.0);
			return frustum.isVisible(beamBox);
		}

		return false;
	}

	@Override
	public void render(Ghast entity, float entityYaw, float partialTick, PoseStack poseStack,
					   MultiBufferSource buffer, int packedLight) {
		poseStack.pushPose();
		poseStack.translate(0.0F, ENTITY_Y_OFFSET, 0.0F);
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

		if (!(entity instanceof DeltaGhastEntity delta) || !delta.isFiring()) {
			poseStack.popPose();
			return;
		}

		Vec3 beamDirection = delta.getBeamDirection(partialTick);
		Vec3 eyePos = entity.getEyePosition(partialTick);
		Vec3 mouthPos = eyePos.add(0.0, DeltaGhastEntity.MOUTH_Y_OFFSET, 0.0);
		Vec3 endPos = mouthPos.add(beamDirection.scale(MAX_BEAM_RANGE));
		HitResult hit = entity.level().clip(new ClipContext(
			mouthPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
		float beamLength = (float) hit.getLocation().distanceTo(mouthPos);

		poseStack.pushPose();
		poseStack.translate(0.0F, entity.getEyeHeight() + (float) DeltaGhastEntity.MOUTH_Y_OFFSET, 0.0F);
		renderBeam(poseStack, buffer, beamDirection, beamLength, entity.tickCount + partialTick);
		poseStack.popPose();
		poseStack.popPose();
	}

	private static void renderBeam(PoseStack poseStack, MultiBufferSource buffer,
								   Vec3 direction, float length, float ageInTicks) {
		Vec3 dir = direction.normalize();
		Vec3 arbitrary = Math.abs(dir.y) > 0.9 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
		Vec3 right = dir.cross(arbitrary).normalize();
		Vec3 up = right.cross(dir).normalize();

		float ex = (float) dir.x * length;
		float ey = (float) dir.y * length;
		float ez = (float) dir.z * length;

		float[][] innerCorners = boxCorners(right, up, BEAM_WIDTH);
		float[][] outerCorners = boxCorners(right, up, GLOW_WIDTH);

		float uvScroll = -1.0F + ageInTicks * 0.02F;
		float uvEnd = length * 2.5F + uvScroll;

		PoseStack.Pose pose = poseStack.last();
		boxFaces(buffer.getBuffer(BEAM_CORE_TYPE), pose, innerCorners, ex, ey, ez,
			140, 255, 255, 255, uvScroll, uvEnd);
		boxFaces(buffer.getBuffer(BEAM_GLOW_TYPE), pose, outerCorners, ex, ey, ez,
			40, 200, 255, 80, uvScroll, uvEnd);
	}

	private static float[][] boxCorners(Vec3 right, Vec3 up, float halfWidth) {
		float[][] corners = new float[4][3];
		for (int i = 0; i < 4; i++) {
			float rightSign = (i == 0 || i == 1) ? halfWidth : -halfWidth;
			float upSign = (i == 0 || i == 3) ? halfWidth : -halfWidth;
			corners[i][0] = (float) (right.x * rightSign + up.x * upSign);
			corners[i][1] = (float) (right.y * rightSign + up.y * upSign);
			corners[i][2] = (float) (right.z * rightSign + up.z * upSign);
		}
		return corners;
	}

	private static void boxFaces(VertexConsumer consumer, PoseStack.Pose pose, float[][] corners,
								 float ex, float ey, float ez,
								 int red, int green, int blue, int alpha,
								 float uvStart, float uvEnd) {
		for (int i = 0; i < 4; i++) {
			int next = (i + 1) % 4;
			vertex(consumer, pose, corners[i][0], corners[i][1], corners[i][2], red, green, blue, alpha, 0.0F, uvStart);
			vertex(consumer, pose, corners[next][0], corners[next][1], corners[next][2], red, green, blue, alpha, 1.0F, uvStart);
			vertex(consumer, pose, corners[next][0] + ex, corners[next][1] + ey, corners[next][2] + ez, red, green, blue, alpha, 1.0F, uvEnd);
			vertex(consumer, pose, corners[i][0] + ex, corners[i][1] + ey, corners[i][2] + ez, red, green, blue, alpha, 0.0F, uvEnd);
		}
	}

	private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
							   float x, float y, float z,
							   int red, int green, int blue, int alpha,
							   float u, float v) {
		Matrix4f matrix4f = pose.pose();
		consumer.addVertex(matrix4f, x, y, z)
			.setColor(red, green, blue, alpha)
			.setUv(u, v)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(FULL_BRIGHT)
			.setNormal(pose, 0.0F, 1.0F, 0.0F)
			;
	}
}
