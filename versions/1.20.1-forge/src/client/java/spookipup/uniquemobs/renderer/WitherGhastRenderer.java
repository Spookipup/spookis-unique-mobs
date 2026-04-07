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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import spookipup.uniquemobs.UniqueMobs;
import spookipup.uniquemobs.entity.variant.ghast.WitherGhastEntity;

public class WitherGhastRenderer extends GhastRenderer {

	private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation(
		UniqueMobs.MOD_ID, "textures/entity/ghast/wither_ghast_beam.png");
	private static final RenderType CONE_CORE_TYPE = RenderType.beaconBeam(BEAM_TEXTURE, true);
	private static final RenderType CONE_GLOW_TYPE = RenderType.beaconBeam(BEAM_TEXTURE, true);

	private static final float ENTITY_SCALE = 0.8F;
	private static final float ENTITY_Y_OFFSET = 0.12F;
	private static final float MAX_BEAM_RANGE = 32.0F;
	private static final float CONE_START_WIDTH = 0.3F;
	private static final int CONE_SEGMENTS = 12;
	private static final int RING_SIDES = 8;
	private static final int FULL_BRIGHT = 15728880;

	private final ResourceLocation texture;
	private final ResourceLocation chargingTexture;

	public WitherGhastRenderer(EntityRendererProvider.Context context, ResourceLocation texture,
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
			|| (entity instanceof WitherGhastEntity wither && (wither.isFiring() || wither.isBiting()));
		return angry && this.chargingTexture != null ? this.chargingTexture : this.texture;
	}

	@Override
	public boolean shouldRender(Ghast ghast, Frustum frustum, double camX, double camY, double camZ) {
		if (super.shouldRender(ghast, frustum, camX, camY, camZ)) {
			return true;
		}

		if (ghast instanceof WitherGhastEntity wither && wither.isFiring()) {
			Vec3 eye = ghast.getEyePosition().add(0.0, WitherGhastEntity.MOUTH_Y_OFFSET, 0.0);
			Vec3 dir = wither.getBeamDirection(1.0F);
			Vec3 end = eye.add(dir.scale(MAX_BEAM_RANGE));
			double spread = MAX_BEAM_RANGE * Math.tan(Math.toRadians(12.0)) + 1.0;
			AABB coneBox = new AABB(
				Math.min(eye.x, end.x) - spread, Math.min(eye.y, end.y) - spread, Math.min(eye.z, end.z) - spread,
				Math.max(eye.x, end.x) + spread, Math.max(eye.y, end.y) + spread, Math.max(eye.z, end.z) + spread);
			return frustum.isVisible(coneBox);
		}

		return false;
	}

	@Override
	public void render(Ghast entity, float entityYaw, float partialTick, PoseStack poseStack,
					   MultiBufferSource buffer, int packedLight) {
		poseStack.pushPose();
		poseStack.translate(0.0F, ENTITY_Y_OFFSET, 0.0F);
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

		if (!(entity instanceof WitherGhastEntity wither) || !wither.isFiring()) {
			poseStack.popPose();
			return;
		}

		Vec3 beamDirection = wither.getBeamDirection(partialTick);
		float endRadius = MAX_BEAM_RANGE * (float) Math.tan(Math.toRadians(12.0));

		poseStack.pushPose();
		poseStack.translate(0.0F, entity.getEyeHeight() + (float) WitherGhastEntity.MOUTH_Y_OFFSET, 0.0F);
		renderCone(poseStack, buffer, beamDirection, MAX_BEAM_RANGE, endRadius, entity.tickCount + partialTick);
		poseStack.popPose();
		poseStack.popPose();
	}

	private static void renderCone(PoseStack poseStack, MultiBufferSource buffer,
								   Vec3 direction, float length, float endRadius, float ageInTicks) {
		Vec3 dir = direction.normalize();
		Vec3 arbitrary = Math.abs(dir.y) > 0.9 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
		Vec3 right = dir.cross(arbitrary).normalize();
		Vec3 up = right.cross(dir).normalize();

		float uvScroll = 1.0F - ageInTicks * 0.015F;
		float uvEnd = length * 1.5F + uvScroll;
		float pulse = 0.7F + 0.3F * Mth.sin(ageInTicks * 0.12F);

		PoseStack.Pose pose = poseStack.last();
		renderConeSegments(buffer.getBuffer(CONE_CORE_TYPE), pose, right, up, dir, length,
			CONE_START_WIDTH, endRadius * 0.4F, 255, 255, 255, 210,
			uvScroll, uvEnd, ageInTicks, pulse, 0.15F);
		renderConeSegments(buffer.getBuffer(CONE_GLOW_TYPE), pose, right, up, dir, length,
			CONE_START_WIDTH * 1.5F, endRadius, 187, 27, 255, 210,
			uvScroll, uvEnd, ageInTicks, pulse, 0.3F);
	}

	private static void renderConeSegments(VertexConsumer consumer, PoseStack.Pose pose,
										   Vec3 right, Vec3 up, Vec3 dir, float length,
										   float startRadius, float endRadius,
										   int red, int green, int blue, int baseAlpha,
										   float uvStart, float uvEnd,
										   float time, float pulse, float wobbleStrength) {
		for (int segment = 0; segment < CONE_SEGMENTS; segment++) {
			float t0 = (float) segment / CONE_SEGMENTS;
			float t1 = (float) (segment + 1) / CONE_SEGMENTS;

			float fade0 = t0 < 0.75F ? 1.0F : 1.0F - (t0 - 0.75F) / 0.25F;
			float fade1 = t1 < 0.75F ? 1.0F : 1.0F - (t1 - 0.75F) / 0.25F;
			int alpha0 = Math.max(1, (int) (baseAlpha * fade0 * pulse));
			int alpha1 = Math.max(1, (int) (baseAlpha * fade1 * pulse));

			float wobble0 = 1.0F + wobbleStrength * Mth.sin(time * 0.2F + t0 * 15.0F);
			float wobble1 = 1.0F + wobbleStrength * Mth.sin(time * 0.2F + t1 * 15.0F);
			float radius0 = Mth.lerp(t0, startRadius, endRadius) * wobble0;
			float radius1 = Mth.lerp(t1, startRadius, endRadius) * wobble1;

			float ox0 = (float) dir.x * length * t0;
			float oy0 = (float) dir.y * length * t0;
			float oz0 = (float) dir.z * length * t0;

			float ox1 = (float) dir.x * length * t1;
			float oy1 = (float) dir.y * length * t1;
			float oz1 = (float) dir.z * length * t1;

			float uv0 = Mth.lerp(t0, uvStart, uvEnd);
			float uv1 = Mth.lerp(t1, uvStart, uvEnd);
			float twist0 = time * 0.04F + t0 * 2.0F;
			float twist1 = time * 0.04F + t1 * 2.0F;

			for (int side = 0; side < RING_SIDES; side++) {
				float angle0 = (float) (side * 2.0 * Math.PI / RING_SIDES);
				float angle1 = (float) ((side + 1) * 2.0 * Math.PI / RING_SIDES);

				float[] p00 = ringPoint(right, up, radius0, angle0 + twist0, ox0, oy0, oz0);
				float[] p01 = ringPoint(right, up, radius0, angle1 + twist0, ox0, oy0, oz0);
				float[] p10 = ringPoint(right, up, radius1, angle0 + twist1, ox1, oy1, oz1);
				float[] p11 = ringPoint(right, up, radius1, angle1 + twist1, ox1, oy1, oz1);

				float uSide0 = (float) side / RING_SIDES;
				float uSide1 = (float) (side + 1) / RING_SIDES;

				vertex(consumer, pose, p00[0], p00[1], p00[2], red, green, blue, alpha0, uSide0, uv0);
				vertex(consumer, pose, p01[0], p01[1], p01[2], red, green, blue, alpha0, uSide1, uv0);
				vertex(consumer, pose, p11[0], p11[1], p11[2], red, green, blue, alpha1, uSide1, uv1);
				vertex(consumer, pose, p10[0], p10[1], p10[2], red, green, blue, alpha1, uSide0, uv1);
			}
		}
	}

	private static float[] ringPoint(Vec3 right, Vec3 up, float radius, float angle,
									 float ox, float oy, float oz) {
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		return new float[] {
			(float) (right.x * cos + up.x * sin) * radius + ox,
			(float) (right.y * cos + up.y * sin) * radius + oy,
			(float) (right.z * cos + up.z * sin) * radius + oz
		};
	}

	private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
							   float x, float y, float z,
							   int red, int green, int blue, int alpha,
							   float u, float v) {
		Matrix4f matrix4f = pose.pose();
		Matrix3f matrix3f = pose.normal();
		consumer.vertex(matrix4f, x, y, z)
			.color(red, green, blue, alpha)
			.uv(u, v)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(FULL_BRIGHT)
			.normal(matrix3f, 0.0F, 1.0F, 0.0F)
			.endVertex();
	}
}
