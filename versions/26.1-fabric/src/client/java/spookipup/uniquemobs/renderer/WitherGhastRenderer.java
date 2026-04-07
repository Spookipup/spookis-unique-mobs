package spookipup.uniquemobs.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.UniqueMobs;
import spookipup.uniquemobs.entity.variant.ghast.WitherGhastEntity;

// keep rendering when only the tractor cone is in frame
public class WitherGhastRenderer extends GhastRenderer {

	private static final Identifier BEAM_TEXTURE = Identifier.fromNamespaceAndPath(
		UniqueMobs.MOD_ID, "textures/entity/ghast/wither_ghast_beam.png");
	private static final RenderType CONE_CORE_TYPE = RenderTypes.beaconBeam(BEAM_TEXTURE, true);
	private static final RenderType CONE_GLOW_TYPE = RenderTypes.beaconBeam(BEAM_TEXTURE, true);

	private static final float MAX_BEAM_RANGE = 32.0F;
	private static final float CONE_START_WIDTH = 0.3F;
	private static final int CONE_SEGMENTS = 12;
	private static final int RING_SIDES = 8;

	private final Identifier texture;
	private final Identifier chargingTexture;

	public WitherGhastRenderer(EntityRendererProvider.Context context, Identifier texture,
							   Identifier chargingTexture, Identifier eyesTexture,
							   Identifier chargingEyesTexture) {
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
	public WitherGhastRenderState createRenderState() {
		return new WitherGhastRenderState();
	}

	@Override
	public void extractRenderState(Ghast ghast, GhastRenderState state, float partialTick) {
		super.extractRenderState(ghast, state, partialTick);
		if (state instanceof WitherGhastRenderState wither && ghast instanceof WitherGhastEntity entity) {
			wither.isFiring = entity.isFiring();
			wither.isBiting = entity.isBiting();
			state.isCharging = entity.isCharging() || wither.isFiring || wither.isBiting;
			wither.chargeTicks = entity.getChargeTicks();
			wither.eyeHeight = ghast.getEyeHeight();

			if (wither.isFiring || wither.isCharging) {
				Vec3 beamDir = entity.getBeamDirection(partialTick);
				wither.beamDirection = beamDir;

				if (wither.isFiring) {
					wither.beamLength = MAX_BEAM_RANGE;
					wither.coneEndRadius = wither.beamLength * (float) Math.tan(Math.toRadians(12.0));
				} else {
					wither.beamLength = 0;
					wither.coneEndRadius = 0;
				}
			} else {
				wither.beamDirection = null;
				wither.beamLength = 0;
				wither.coneEndRadius = 0;
			}
		}
	}

	@Override
	public boolean shouldRender(Ghast ghast, Frustum frustum, double camX, double camY, double camZ) {
		if (super.shouldRender(ghast, frustum, camX, camY, camZ)) return true;

		if (ghast instanceof WitherGhastEntity entity && entity.isFiring()) {
			Vec3 eye = ghast.getEyePosition().add(0, WitherGhastEntity.MOUTH_Y_OFFSET, 0);
			Vec3 dir = entity.getBeamDirection(1.0F);
			Vec3 end = eye.add(dir.scale(MAX_BEAM_RANGE));
			double spread = MAX_BEAM_RANGE * Math.tan(Math.toRadians(12.0)) + 1.0;
			AABB beamBox = new AABB(
				Math.min(eye.x, end.x) - spread, Math.min(eye.y, end.y) - spread, Math.min(eye.z, end.z) - spread,
				Math.max(eye.x, end.x) + spread, Math.max(eye.y, end.y) + spread, Math.max(eye.z, end.z) + spread);
			return frustum.isVisible(beamBox);
		}
		return false;
	}

	@Override
	public Identifier getTextureLocation(GhastRenderState state) {
		boolean angry = state.isCharging
			|| (state instanceof WitherGhastRenderState wither && (wither.isFiring || wither.isBiting));
		return angry && this.chargingTexture != null ? this.chargingTexture : this.texture;
	}

	@Override
	public void submit(GhastRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
					   CameraRenderState camera) {
		super.submit(state, poseStack, collector, camera);

		if (state instanceof WitherGhastRenderState wither && wither.isFiring && wither.beamDirection != null) {
			poseStack.pushPose();
			poseStack.translate(0.0F, wither.eyeHeight + (float) WitherGhastEntity.MOUTH_Y_OFFSET, 0.0F);
			renderCone(poseStack, collector, wither.beamDirection, wither.beamLength,
				wither.coneEndRadius, wither.ageInTicks);
			poseStack.popPose();
		}
	}

	private static void renderCone(PoseStack poseStack, SubmitNodeCollector collector,
								   Vec3 direction, float length, float endRadius, float ageInTicks) {
		Vec3 dir = direction.normalize();

		Vec3 arbitrary = Math.abs(dir.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
		Vec3 r = dir.cross(arbitrary).normalize();
		Vec3 u = r.cross(dir).normalize();

		float uvScroll = 1.0F - ageInTicks * 0.015F;
		float uvEnd = length * 1.5F + uvScroll;

		float pulse = 0.7F + 0.3F * Mth.sin(ageInTicks * 0.12F);

		collector.submitCustomGeometry(poseStack, CONE_CORE_TYPE, (pose, consumer) -> {
			renderConeSegments(consumer, pose, r, u, dir, length, CONE_START_WIDTH,
				endRadius * 0.4F, 255, 255, 255, 210, uvScroll, uvEnd, ageInTicks, pulse, 0.15F);
		});

		collector.submitCustomGeometry(poseStack, CONE_GLOW_TYPE, (pose, consumer) -> {
			renderConeSegments(consumer, pose, r, u, dir, length, CONE_START_WIDTH * 1.5F,
				endRadius, 187, 27, 255, 210, uvScroll, uvEnd, ageInTicks, pulse, 0.3F);
		});
	}

	private static void renderConeSegments(VertexConsumer consumer, PoseStack.Pose pose,
										   Vec3 r, Vec3 u, Vec3 dir, float length,
										   float startRadius, float endRadius,
										   int red, int green, int blue, int baseAlpha,
										   float uvStart, float uvEnd,
										   float time, float pulse, float wobbleStrength) {
		for (int seg = 0; seg < CONE_SEGMENTS; seg++) {
			float t0 = (float) seg / CONE_SEGMENTS;
			float t1 = (float) (seg + 1) / CONE_SEGMENTS;

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
				float angle0 = (float) (side * 2 * Math.PI / RING_SIDES);
				float angle1 = (float) ((side + 1) * 2 * Math.PI / RING_SIDES);

				float[] p00 = ringPoint(r, u, radius0, angle0 + twist0, ox0, oy0, oz0);
				float[] p01 = ringPoint(r, u, radius0, angle1 + twist0, ox0, oy0, oz0);
				float[] p10 = ringPoint(r, u, radius1, angle0 + twist1, ox1, oy1, oz1);
				float[] p11 = ringPoint(r, u, radius1, angle1 + twist1, ox1, oy1, oz1);

				float uSide0 = (float) side / RING_SIDES;
				float uSide1 = (float) (side + 1) / RING_SIDES;

				vertex(consumer, pose, p00[0], p00[1], p00[2], red, green, blue, alpha0, uSide0, uv0);
				vertex(consumer, pose, p01[0], p01[1], p01[2], red, green, blue, alpha0, uSide1, uv0);
				vertex(consumer, pose, p11[0], p11[1], p11[2], red, green, blue, alpha1, uSide1, uv1);
				vertex(consumer, pose, p10[0], p10[1], p10[2], red, green, blue, alpha1, uSide0, uv1);
			}
		}
	}

	private static float[] ringPoint(Vec3 r, Vec3 u, float radius, float angle,
									 float ox, float oy, float oz) {
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		return new float[] {
			(float) (r.x * cos + u.x * sin) * radius + ox,
			(float) (r.y * cos + u.y * sin) * radius + oy,
			(float) (r.z * cos + u.z * sin) * radius + oz
		};
	}

	private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
							   float x, float y, float z,
							   int r, int g, int b, int a, float u, float v) {
		consumer.addVertex(pose, x, y, z)
			.setColor(r, g, b, a)
			.setUv(u, v)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(15728880)
			.setNormal(pose, 0.0F, 1.0F, 0.0F);
	}
}
