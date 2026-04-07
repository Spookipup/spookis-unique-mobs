package spookipup.uniquemobs.renderer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import spookipup.uniquemobs.UniqueMobs;

public class WitherZombieModel extends EntityModel<WitherZombieRenderState> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
		Identifier.fromNamespaceAndPath(UniqueMobs.MOD_ID, "wither_zombie"), "main"
	);

	private final ModelPart body;
	private final ModelPart leftHead;
	private final ModelPart rightHead;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public WitherZombieModel(ModelPart root) {
		super(root);
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
	public void setupAnim(WitherZombieRenderState state) {
		super.setupAnim(state);

		float walkPos = state.walkAnimationPos;
		float walkSpeed = state.walkAnimationSpeed;

		rightLeg.xRot = Mth.cos(walkPos * 0.6662F) * 1.4F * walkSpeed;
		leftLeg.xRot = Mth.cos(walkPos * 0.6662F + Mth.PI) * 1.4F * walkSpeed;

		float sway = Mth.sin(state.ageInTicks * 0.067F) * 0.05F;
		float baseRaise = state.isAggressive ? -1.5F : -0.6F;

		rightArm.xRot = baseRaise + sway + Mth.cos(walkPos * 0.6662F + Mth.PI) * walkSpeed * 0.5F;
		leftArm.xRot = baseRaise - sway + Mth.cos(walkPos * 0.6662F) * walkSpeed * 0.5F;
		rightArm.zRot = 0.0F;
		leftArm.zRot = 0.0F;

		leftHead.yRot = (state.yHeadRots[0] - state.bodyRot) * Mth.DEG_TO_RAD;
		leftHead.xRot = state.xHeadRots[0] * Mth.DEG_TO_RAD;

		rightHead.yRot = (state.yHeadRots[1] - state.bodyRot) * Mth.DEG_TO_RAD;
		rightHead.xRot = state.xHeadRots[1] * Mth.DEG_TO_RAD;
	}
}
