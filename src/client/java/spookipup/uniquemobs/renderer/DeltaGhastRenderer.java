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
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import spookipup.uniquemobs.UniqueMobs;
import spookipup.uniquemobs.entity.variant.ghast.DeltaGhastEntity;

// keep rendering when only the beam is in frame
public class DeltaGhastRenderer extends GhastRenderer {

	private static final Identifier BEAM_TEXTURE = Identifier.fromNamespaceAndPath(
		UniqueMobs.MOD_ID, "textures/entity/ghast/delta_ghast_beam.png");
	private static final RenderType BEAM_CORE_TYPE = RenderTypes.beaconBeam(BEAM_TEXTURE, false);
	private static final RenderType BEAM_GLOW_TYPE = RenderTypes.beaconBeam(BEAM_TEXTURE, true);

	private static final float MAX_BEAM_RANGE = 48.0F;
	private static final float BEAM_WIDTH = 0.35F;
	private static final float GLOW_WIDTH = 0.8F;

	private final Identifier texture;
	private final Identifier chargingTexture;

	public DeltaGhastRenderer(EntityRendererProvider.Context context, Identifier texture,
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
	public DeltaGhastRenderState createRenderState() {
		return new DeltaGhastRenderState();
	}

	@Override
	public void extractRenderState(Ghast ghast, GhastRenderState state, float partialTick) {
		super.extractRenderState(ghast, state, partialTick);
		state.isCharging = ghast.isCharging();

		if (state instanceof DeltaGhastRenderState delta && ghast instanceof DeltaGhastEntity entity) {
			delta.isFiring = entity.isFiring();
			delta.chargeTicks = entity.getChargeTicks();
			delta.eyeHeight = ghast.getEyeHeight();

			if (delta.isFiring || delta.isCharging) {
				Vec3 beamDir = entity.getBeamDirection(partialTick);
				delta.beamDirection = beamDir;

				if (delta.isFiring) {
					Vec3 eyePos = ghast.getEyePosition(partialTick);
					Vec3 mouthPos = eyePos.add(0, DeltaGhastEntity.MOUTH_Y_OFFSET, 0);
					Vec3 endPos = mouthPos.add(beamDir.scale(MAX_BEAM_RANGE));
					HitResult hit = ghast.level().clip(new ClipContext(
						mouthPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, ghast));
					delta.beamLength = (float) hit.getLocation().distanceTo(mouthPos);
				} else {
					delta.beamLength = MAX_BEAM_RANGE;
				}
			} else {
				delta.beamDirection = null;
				delta.beamLength = 0;
			}
		}
	}

	@Override
	public boolean shouldRender(Ghast ghast, Frustum frustum, double camX, double camY, double camZ) {
		if (super.shouldRender(ghast, frustum, camX, camY, camZ)) return true;

		if (ghast instanceof DeltaGhastEntity entity && entity.isFiring()) {
			Vec3 eye = ghast.getEyePosition().add(0, DeltaGhastEntity.MOUTH_Y_OFFSET, 0);
			Vec3 dir = entity.getBeamDirection(1.0F);
			Vec3 end = eye.add(dir.scale(MAX_BEAM_RANGE));
			AABB beamBox = new AABB(
				Math.min(eye.x, end.x) - 1, Math.min(eye.y, end.y) - 1, Math.min(eye.z, end.z) - 1,
				Math.max(eye.x, end.x) + 1, Math.max(eye.y, end.y) + 1, Math.max(eye.z, end.z) + 1);
			return frustum.isVisible(beamBox);
		}
		return false;
	}

	@Override
	public Identifier getTextureLocation(GhastRenderState state) {
		boolean angry = state.isCharging
			|| (state instanceof DeltaGhastRenderState delta && delta.isFiring);
		return angry && this.chargingTexture != null ? this.chargingTexture : this.texture;
	}

	@Override
	public void submit(GhastRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
					   CameraRenderState camera) {
		super.submit(state, poseStack, collector, camera);

		if (state instanceof DeltaGhastRenderState delta && delta.isFiring && delta.beamDirection != null) {
			poseStack.pushPose();
			poseStack.translate(0.0F, delta.eyeHeight + (float) DeltaGhastEntity.MOUTH_Y_OFFSET, 0.0F);
			renderBeam(poseStack, collector, delta.beamDirection, delta.beamLength, delta.ageInTicks);
			poseStack.popPose();
		}
	}

	private static void renderBeam(PoseStack poseStack, SubmitNodeCollector collector,
									Vec3 direction, float length, float ageInTicks) {
		Vec3 dir = direction.normalize();

		Vec3 arbitrary = Math.abs(dir.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
		Vec3 r = dir.cross(arbitrary).normalize();
		Vec3 u = r.cross(dir).normalize();

		float ex = (float) dir.x * length;
		float ey = (float) dir.y * length;
		float ez = (float) dir.z * length;

		float[][] ic = boxCorners(r, u, BEAM_WIDTH);
		float[][] oc = boxCorners(r, u, GLOW_WIDTH);

		float uvScroll = -1.0F + ageInTicks * 0.02F;
		float uvEnd = length * 2.5F + uvScroll;

		collector.submitCustomGeometry(poseStack, BEAM_CORE_TYPE, (pose, consumer) -> {
			boxFaces(consumer, pose, ic, ex, ey, ez, 140, 255, 255, 255, uvScroll, uvEnd);
		});
		collector.submitCustomGeometry(poseStack, BEAM_GLOW_TYPE, (pose, consumer) -> {
			boxFaces(consumer, pose, oc, ex, ey, ez, 40, 200, 255, 80, uvScroll, uvEnd);
		});
	}

	private static float[][] boxCorners(Vec3 r, Vec3 u, float halfWidth) {
		float[][] c = new float[4][3];
		for (int i = 0; i < 4; i++) {
			float rs = (i == 0 || i == 1) ? halfWidth : -halfWidth;
			float us = (i == 0 || i == 3) ? halfWidth : -halfWidth;
			c[i][0] = (float) (r.x * rs + u.x * us);
			c[i][1] = (float) (r.y * rs + u.y * us);
			c[i][2] = (float) (r.z * rs + u.z * us);
		}
		return c;
	}

	private static void boxFaces(VertexConsumer consumer, PoseStack.Pose pose, float[][] c,
								 float ex, float ey, float ez,
								 int r, int g, int b, int a, float uvStart, float uvEnd) {
		for (int i = 0; i < 4; i++) {
			int next = (i + 1) % 4;
			vertex(consumer, pose, c[i][0], c[i][1], c[i][2], r, g, b, a, 0.0F, uvStart);
			vertex(consumer, pose, c[next][0], c[next][1], c[next][2], r, g, b, a, 1.0F, uvStart);
			vertex(consumer, pose, c[next][0] + ex, c[next][1] + ey, c[next][2] + ez, r, g, b, a, 1.0F, uvEnd);
			vertex(consumer, pose, c[i][0] + ex, c[i][1] + ey, c[i][2] + ez, r, g, b, a, 0.0F, uvEnd);
		}
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
