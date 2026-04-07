package spookipup.uniquemobs.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import spookipup.uniquemobs.UniqueMobs;
import spookipup.uniquemobs.entity.variant.zombie.WitherZombieEntity;

public class WitherZombieModel extends EntityModel<WitherZombieEntity> {

	private static final float DEG_TO_RAD = Mth.PI / 180.0F;

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
		new ResourceLocation(UniqueMobs.MOD_ID, "wither_zombie"), "main"
	);

	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart leftHead;
	private final ModelPart rightHead;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public WitherZombieModel(ModelPart root) {
		this.root = root;
		this.body = root.getChild("body");
		this.leftHead = root.getChild("left_head");
		this.rightHead = root.getChild("right_head");
		this.rightArm = root.getChild("right_arm");
		this.leftArm = root.getChild("left_arm");
		this.rightLeg = root.getChild("right_leg");
		this.leftLeg = root.getChild("left_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("body",
			CubeListBuilder.create()
				.texOffs(0, 32).addBox(-7.0F, 0.0F, -2.0F, 14.0F, 12.0F, 4.0F),
			PartPose.ZERO
		);

		root.addOrReplaceChild("left_head",
			CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
				.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
			PartPose.offset(-5.0F, 0.0F, 0.0F)
		);

		root.addOrReplaceChild("right_head",
			CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
				.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
			PartPose.offset(5.0F, 0.0F, 0.0F)
		);

		root.addOrReplaceChild("right_arm",
			CubeListBuilder.create()
				.texOffs(21, 16).addBox(-6.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(-5.0F, 2.0F, 0.0F)
		);

		root.addOrReplaceChild("left_arm",
			CubeListBuilder.create()
				.texOffs(21, 16).mirror().addBox(2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(5.0F, 2.0F, 0.0F)
		);

		root.addOrReplaceChild("right_leg",
			CubeListBuilder.create()
				.texOffs(0, 16).addBox(-3.0F, 0.0F, -2.0F, 5.0F, 12.0F, 4.0F),
			PartPose.offset(-1.9F, 12.0F, 0.0F)
		);

		root.addOrReplaceChild("left_leg",
			CubeListBuilder.create()
				.texOffs(1, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 5.0F, 12.0F, 4.0F),
			PartPose.offset(1.9F, 12.0F, 0.0F)
		);

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(WitherZombieEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;

		float sway = Mth.sin(ageInTicks * 0.067F) * 0.05F;
		float baseRaise = entity.isAggressive() ? -1.5F : -0.6F;

		rightArm.xRot = baseRaise + sway + Mth.cos(limbSwing * 0.6662F + Mth.PI) * limbSwingAmount * 0.5F;
		leftArm.xRot = baseRaise - sway + Mth.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.5F;
		rightArm.zRot = 0.0F;
		leftArm.zRot = 0.0F;

		float[] entityXRots = entity.getHeadXRots();
		float[] entityYRots = entity.getHeadYRots();

		leftHead.yRot = (entityYRots[0] - entity.yBodyRot) * DEG_TO_RAD;
		leftHead.xRot = entityXRots[0] * DEG_TO_RAD;

		rightHead.yRot = (entityYRots[1] - entity.yBodyRot) * DEG_TO_RAD;
		rightHead.xRot = entityXRots[1] * DEG_TO_RAD;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
