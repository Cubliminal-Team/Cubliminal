package net.limit.cubliminal.client.entity.model;

import net.limit.cubliminal.client.entity.state.CustomEntityRenderState;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;

public class SmilerModel extends EntityModel<CustomEntityRenderState> {
	private final ModelPart smiler;
	private final ModelPart Lfrontleg;
	private final ModelPart knee;
	private final ModelPart foot;
	private final ModelPart Lmidleg;
	private final ModelPart knee3;
	private final ModelPart foot2;
	private final ModelPart Lbackleg;
	private final ModelPart knee5;
	private final ModelPart Rtail;
	private final ModelPart Rtailjoint;
	private final ModelPart Ltail;
	private final ModelPart Ltailjoint;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart torso;
	private final ModelPart Rfrontleg;
	private final ModelPart knee2;
	private final ModelPart foot3;
	private final ModelPart Rmidleg;
	private final ModelPart knee4;
	private final ModelPart foot4;
	private final ModelPart Rbackleg;
	private final ModelPart knee6;

	public SmilerModel(ModelPart root) {
        super(root);
        this.smiler = root.getChild("smiler");
		this.Lfrontleg = this.smiler.getChild("Lfrontleg");
		this.knee = this.Lfrontleg.getChild("knee");
		this.foot = this.knee.getChild("foot");
		this.Lmidleg = this.smiler.getChild("Lmidleg");
		this.knee3 = this.Lmidleg.getChild("knee3");
		this.foot2 = this.knee3.getChild("foot2");
		this.Lbackleg = this.smiler.getChild("Lbackleg");
		this.knee5 = this.Lbackleg.getChild("knee5");
		this.Rtail = this.smiler.getChild("Rtail");
		this.Rtailjoint = this.Rtail.getChild("Rtailjoint");
		this.Ltail = this.smiler.getChild("Ltail");
		this.Ltailjoint = this.Ltail.getChild("Ltailjoint");
		this.body = this.smiler.getChild("body");
		this.head = this.body.getChild("head");
		this.torso = this.body.getChild("torso");
		this.Rfrontleg = this.smiler.getChild("Rfrontleg");
		this.knee2 = this.Rfrontleg.getChild("knee2");
		this.foot3 = this.knee2.getChild("foot3");
		this.Rmidleg = this.smiler.getChild("Rmidleg");
		this.knee4 = this.Rmidleg.getChild("knee4");
		this.foot4 = this.knee4.getChild("foot4");
		this.Rbackleg = this.smiler.getChild("Rbackleg");
		this.knee6 = this.Rbackleg.getChild("knee6");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData smiler = modelPartData.addChild("smiler", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData Lfrontleg = smiler.addChild("Lfrontleg", ModelPartBuilder.create().uv(24, 66).cuboid(-1.0F, -1.0F, -4.0F, 5.0F, 9.0F, 7.0F, new Dilation(0.0F)), ModelTransform.of(5.0F, -19.0F, -4.0F, 0.0F, 0.0F, -0.0436F));

		ModelPartData knee = Lfrontleg.addChild("knee", ModelPartBuilder.create().uv(94, 22).cuboid(-2.0F, -2.0F, -1.0F, 4.0F, 11.0F, 5.0F, new Dilation(-0.05F)), ModelTransform.of(1.6F, 8.0F, -2.0F, -0.0872F, 0.0038F, 0.0435F));

		ModelPartData foot = knee.addChild("foot", ModelPartBuilder.create(), ModelTransform.pivot(-0.2644F, 8.8527F, 1.0187F));

		ModelPartData cube_r1 = foot.addChild("cube_r1", ModelPartBuilder.create().uv(72, 85).cuboid(-1.0F, -2.0F, -2.0F, 0.0F, 2.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(2.0F, 3.0F, -4.0F, 0.2616F, -0.0076F, -0.0007F));

		ModelPartData cube_r2 = foot.addChild("cube_r2", ModelPartBuilder.create().uv(72, 85).cuboid(-0.0007F, -1.0353F, -2.2662F, 0.0F, 2.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(-1.8F, 1.0F, 1.0F, 0.2731F, 0.2872F, 0.0805F));

		ModelPartData cube_r3 = foot.addChild("cube_r3", ModelPartBuilder.create().uv(72, 85).cuboid(-1.0F, -2.0F, -2.0F, 0.0F, 2.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 3.0F, -4.0F, 0.2616F, -0.0076F, -0.0007F));

		ModelPartData cube_r4 = foot.addChild("cube_r4", ModelPartBuilder.create().uv(96, 44).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.2644F, 2.4473F, -1.7187F, 0.218F, 0.0094F, 0.001F));

		ModelPartData Lmidleg = smiler.addChild("Lmidleg", ModelPartBuilder.create().uv(78, 80).cuboid(-2.0F, -3.0F, -4.0F, 5.0F, 7.0F, 7.0F, new Dilation(0.0F)), ModelTransform.of(5.0F, -16.0F, 2.0F, 0.0F, 0.0F, -0.3054F));

		ModelPartData knee3 = Lmidleg.addChild("knee3", ModelPartBuilder.create().uv(94, 94).cuboid(-2.0F, -1.9128F, -0.9962F, 4.0F, 12.0F, 4.0F, new Dilation(-0.05F)), ModelTransform.of(0.5F, 4.0F, -1.0F, 0.394F, -0.0151F, 0.0007F));

		ModelPartData foot2 = knee3.addChild("foot2", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 9.6872F, 0.0038F));

		ModelPartData cube_r5 = foot2.addChild("cube_r5", ModelPartBuilder.create().uv(102, 62).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

		ModelPartData cube_r6 = foot2.addChild("cube_r6", ModelPartBuilder.create().uv(102, 84).cuboid(-0.5999F, -0.7554F, -2.5394F, 0.0F, 2.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(1.4119F, 0.2472F, -2.3057F, -0.1757F, -0.0198F, -0.087F));

		ModelPartData cube_r7 = foot2.addChild("cube_r7", ModelPartBuilder.create().uv(102, 84).cuboid(-0.5999F, -0.7554F, -2.5394F, 0.0F, 2.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(-0.5881F, 0.2472F, -2.3057F, -0.1757F, -0.0198F, -0.087F));

		ModelPartData Lbackleg = smiler.addChild("Lbackleg", ModelPartBuilder.create().uv(78, 65).cuboid(-3.0F, -3.0F, -4.0F, 6.0F, 9.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(6.0F, -16.0F, 13.0F, -0.0872F, -0.0038F, -0.0435F));

		ModelPartData knee5 = Lbackleg.addChild("knee5", ModelPartBuilder.create().uv(96, 0).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 6.0F, -1.0F, 0.1745F, 0.0F, 0.0F));

		ModelPartData cube_r8 = knee5.addChild("cube_r8", ModelPartBuilder.create().uv(96, 14).cuboid(-2.0F, 0.0F, -3.0F, 5.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 7.9F, -1.0F, -0.0436F, 0.0F, 0.0436F));

		ModelPartData Rtail = smiler.addChild("Rtail", ModelPartBuilder.create().uv(0, 95).cuboid(-1.5F, -2.5F, -2.0F, 3.0F, 5.0F, 7.0F, new Dilation(0.0F))
		.uv(72, 94).cuboid(-1.0F, -1.5F, 4.0F, 2.0F, 3.0F, 9.0F, new Dilation(0.0F)), ModelTransform.of(-2.5F, -18.5F, 18.0F, -0.48F, 0.0F, 0.0F));

		ModelPartData Rtailjoint = Rtail.addChild("Rtailjoint", ModelPartBuilder.create().uv(24, 82).cuboid(-1.0F, -1.5F, -0.4F, 2.0F, 3.0F, 10.0F, new Dilation(-0.05F)), ModelTransform.of(0.0F, 0.0F, 13.0F, -0.132F, -0.1298F, 0.0172F));

		ModelPartData cube_r9 = Rtailjoint.addChild("cube_r9", ModelPartBuilder.create().uv(40, 96).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 8.0F, new Dilation(-0.1F)), ModelTransform.of(0.0F, 0.0F, 9.6F, 0.2182F, 0.0F, 0.0F));

		ModelPartData Ltail = smiler.addChild("Ltail", ModelPartBuilder.create().uv(96, 52).cuboid(-1.5F, -2.6F, -2.0F, 3.0F, 5.0F, 5.0F, new Dilation(0.0F)), ModelTransform.of(2.5F, -18.4F, 18.0F, -0.3054F, 0.0F, 0.0F));

		ModelPartData Ltailjoint = Ltail.addChild("Ltailjoint", ModelPartBuilder.create().uv(20, 95).cuboid(-1.0F, -1.6F, 0.0F, 2.0F, 3.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 3.0F, -0.1314F, 0.0865F, -0.0114F));

		ModelPartData cube_r10 = Ltailjoint.addChild("cube_r10", ModelPartBuilder.create().uv(0, 83).cuboid(-1.0F, -1.1F, -1.0F, 2.0F, 2.0F, 10.0F, new Dilation(-0.05F)), ModelTransform.of(0.0F, 0.0F, 8.0F, 0.2182F, 0.0F, 0.0F));

		ModelPartData body = smiler.addChild("body", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

		ModelPartData head = body.addChild("head", ModelPartBuilder.create().uv(54, 24).cuboid(-7.0F, -8.0F, -6.0F, 14.0F, 16.0F, 6.0F, new Dilation(0.0F))
		.uv(56, 0).cuboid(-10.0F, -11.0F, -4.0F, 20.0F, 22.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -22.0F, -9.0F));

		ModelPartData cube_r11 = head.addChild("cube_r11", ModelPartBuilder.create().uv(66, 96).cuboid(-3.0F, -10.0F, 0.0F, 3.0F, 22.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-7.0F, -1.0F, -6.0F, 0.0F, -0.4363F, 0.0F));

		ModelPartData cube_r12 = head.addChild("cube_r12", ModelPartBuilder.create().uv(94, 41).cuboid(-7.0F, 0.0F, 0.0F, 14.0F, 3.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 8.0F, -6.0F, -0.4363F, 0.0F, 0.0F));

		ModelPartData cube_r13 = head.addChild("cube_r13", ModelPartBuilder.create().uv(94, 38).cuboid(-7.0F, -3.0F, 0.0F, 14.0F, 3.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -8.0F, -6.0F, 0.4363F, 0.0F, 0.0F));

		ModelPartData cube_r14 = head.addChild("cube_r14", ModelPartBuilder.create().uv(60, 96).cuboid(0.0F, -11.0F, 0.0F, 3.0F, 22.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(7.0F, 0.0F, -6.0F, 0.0F, 0.4363F, 0.0F));

		ModelPartData torso = body.addChild("torso", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -3.0F, -13.0F, 10.0F, 6.0F, 18.0F, new Dilation(0.0F))
		.uv(0, 24).cuboid(-7.0F, -4.0F, 0.0F, 14.0F, 11.0F, 13.0F, new Dilation(0.0F))
		.uv(48, 85).cuboid(-5.0F, -3.0F, 13.0F, 10.0F, 9.0F, 2.0F, new Dilation(0.0F))
		.uv(48, 65).cuboid(-0.1F, -8.0F, 0.0F, 0.0F, 5.0F, 15.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -20.0F, 3.0F));

		ModelPartData cube_r15 = torso.addChild("cube_r15", ModelPartBuilder.create().uv(0, 66).cuboid(0.0F, -4.0F, -12.0F, 0.0F, 5.0F, 12.0F, new Dilation(0.0F))
		.uv(0, 48).cuboid(-5.0F, 0.0F, -13.0F, 10.0F, 4.0F, 14.0F, new Dilation(-0.05F)), ModelTransform.of(0.0F, -4.0F, 0.0F, -0.1309F, 0.0F, 0.0F));

		ModelPartData cube_r16 = torso.addChild("cube_r16", ModelPartBuilder.create().uv(102, 80).cuboid(-2.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-5.0F, -2.0F, -6.0F, 0.0F, 0.1745F, 0.2618F));

		ModelPartData cube_r17 = torso.addChild("cube_r17", ModelPartBuilder.create().uv(102, 72).cuboid(-4.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-5.0F, -2.0F, -10.0F, 0.0F, -0.2182F, 0.2182F));

		ModelPartData cube_r18 = torso.addChild("cube_r18", ModelPartBuilder.create().uv(102, 76).cuboid(0.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(5.0F, -2.0F, -7.0F, 0.0306F, -0.1719F, -0.1772F));

		ModelPartData cube_r19 = torso.addChild("cube_r19", ModelPartBuilder.create().uv(102, 68).cuboid(-1.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(6.0F, -2.0F, -10.0F, 0.0F, 0.1745F, -0.1745F));

		ModelPartData cube_r20 = torso.addChild("cube_r20", ModelPartBuilder.create().uv(48, 48).cuboid(-5.0F, -2.0F, -13.0F, 10.0F, 3.0F, 14.0F, new Dilation(-0.05F)), ModelTransform.of(0.0F, 3.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

		ModelPartData Rfrontleg = smiler.addChild("Rfrontleg", ModelPartBuilder.create().uv(24, 66).mirrored().cuboid(-4.0F, -1.0F, -4.0F, 5.0F, 9.0F, 7.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-5.0F, -19.0F, -4.0F, 0.0F, 0.0F, 0.0436F));

		ModelPartData knee2 = Rfrontleg.addChild("knee2", ModelPartBuilder.create().uv(94, 22).mirrored().cuboid(-2.0F, -2.0F, -1.0F, 4.0F, 11.0F, 5.0F, new Dilation(-0.05F)).mirrored(false), ModelTransform.of(-1.6F, 8.0F, -2.0F, -0.0872F, -0.0038F, -0.0435F));

		ModelPartData foot3 = knee2.addChild("foot3", ModelPartBuilder.create(), ModelTransform.pivot(0.2644F, 8.8527F, 1.0187F));

		ModelPartData cube_r21 = foot3.addChild("cube_r21", ModelPartBuilder.create().uv(72, 85).mirrored().cuboid(0.0007F, -1.0353F, -2.2661F, 0.0F, 2.0F, 3.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(1.8072F, 0.9984F, 1.0371F, 0.2724F, -0.2796F, -0.08F));

		ModelPartData cube_r22 = foot3.addChild("cube_r22", ModelPartBuilder.create().uv(72, 85).mirrored().cuboid(1.0F, -2.0F, -2.0F, 0.0F, 2.0F, 3.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-2.0F, 3.0F, -4.0F, 0.2616F, 0.0076F, 0.0007F));

		ModelPartData cube_r23 = foot3.addChild("cube_r23", ModelPartBuilder.create().uv(72, 85).mirrored().cuboid(1.0F, -2.0F, -2.0F, 0.0F, 2.0F, 3.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(0.0F, 3.0F, -4.0F, 0.2616F, 0.0076F, 0.0007F));

		ModelPartData cube_r24 = foot3.addChild("cube_r24", ModelPartBuilder.create().uv(96, 44).mirrored().cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 6.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-0.2644F, 2.4473F, -1.7187F, 0.218F, -0.0094F, -0.001F));

		ModelPartData Rmidleg = smiler.addChild("Rmidleg", ModelPartBuilder.create().uv(78, 80).mirrored().cuboid(-3.0F, -3.0F, -4.0F, 5.0F, 7.0F, 7.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-5.0F, -16.0F, 2.0F, 0.0F, 0.0F, 0.3054F));

		ModelPartData knee4 = Rmidleg.addChild("knee4", ModelPartBuilder.create().uv(94, 94).mirrored().cuboid(-2.0F, -1.9128F, -0.9962F, 4.0F, 12.0F, 4.0F, new Dilation(-0.05F)).mirrored(false), ModelTransform.of(-0.5F, 4.0F, -1.0F, 0.394F, 0.0151F, -0.0007F));

		ModelPartData foot4 = knee4.addChild("foot4", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 9.6872F, 0.0038F));

		ModelPartData cube_r25 = foot4.addChild("cube_r25", ModelPartBuilder.create().uv(102, 62).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

		ModelPartData cube_r26 = foot4.addChild("cube_r26", ModelPartBuilder.create().uv(102, 84).mirrored().cuboid(0.5999F, -0.7554F, -2.5394F, 0.0F, 2.0F, 3.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-1.4119F, 0.2472F, -2.3057F, -0.1757F, 0.0198F, 0.087F));

		ModelPartData cube_r27 = foot4.addChild("cube_r27", ModelPartBuilder.create().uv(102, 84).mirrored().cuboid(0.5999F, -0.7554F, -2.5394F, 0.0F, 2.0F, 3.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(0.5881F, 0.2472F, -2.3057F, -0.1757F, 0.0198F, 0.087F));

		ModelPartData Rbackleg = smiler.addChild("Rbackleg", ModelPartBuilder.create().uv(78, 65).mirrored().cuboid(-3.0F, -3.0F, -4.0F, 6.0F, 9.0F, 6.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-6.0F, -16.0F, 13.0F, -0.0872F, 0.0038F, 0.0435F));

		ModelPartData knee6 = Rbackleg.addChild("knee6", ModelPartBuilder.create().uv(96, 0).mirrored().cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(0.0F, 6.0F, -1.0F, 0.1745F, 0.0F, 0.0F));

		ModelPartData cube_r28 = knee6.addChild("cube_r28", ModelPartBuilder.create().uv(96, 14).mirrored().cuboid(-3.0F, 0.0F, -3.0F, 5.0F, 2.0F, 6.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(0.0F, 7.9F, -1.0F, -0.0436F, 0.0F, -0.0436F));
		return TexturedModelData.of(modelData, 128, 128);
	}

	@Override
	public void setAngles(CustomEntityRenderState state) {
		super.setAngles(state);
		this.head.pitch = state.pitch * (float) (Math.PI / 180.0);
		this.head.yaw = state.yawDegrees * (float) (Math.PI / 180.0);
	}
}