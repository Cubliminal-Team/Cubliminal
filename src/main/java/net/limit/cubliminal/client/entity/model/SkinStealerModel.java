package net.limit.cubliminal.client.entity.model;

import net.limit.cubliminal.client.entity.state.SkinStealerRenderState;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings("unused")
public class SkinStealerModel extends EntityModel<SkinStealerRenderState> {

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public SkinStealerModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData head = modelPartData.addChild("head", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -11.3F, 0.0F));

        ModelPartData head_r1 = head.addChild("head_r1", ModelPartBuilder.create().uv(68, 55).cuboid(-2.5F, -4.7F, -2.0F, 5.0F, 7.0F, 4.0F, new Dilation(0.0F))
                .uv(2, 63).cuboid(-1.0F, -3.7F, 0.3F, 2.0F, 3.0F, 5.0F, new Dilation(0.0F))
                .uv(69, 39).cuboid(-2.0F, -1.7F, 1.5F, 4.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(2, 24).cuboid(-3.0F, -7.7F, -1.0F, 6.0F, 6.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        ModelPartData body = modelPartData.addChild("body", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 5.0F, 0.0F));

        ModelPartData spine_r1 = body.addChild("spine_r1", ModelPartBuilder.create().uv(40, 56).cuboid(0.0F, -16.0F, -8.0F, 0.0F, 15.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -16.0F, -4.0F, 10.0F, 14.0F, 7.0F, new Dilation(0.25F))
                .uv(32, 21).cuboid(-5.0F, -5.0F, -3.0F, 10.0F, 5.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        ModelPartData left_arm = modelPartData.addChild("left_arm", ModelPartBuilder.create(), ModelTransform.pivot(-5.0F, -9.0F, 0.0F));

        ModelPartData shoulder_r1 = left_arm.addChild("shoulder_r1", ModelPartBuilder.create().uv(3, 74).cuboid(5.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new Dilation(0.3F))
                .uv(66, 17).cuboid(4.5F, 6.0F, -2.5F, 5.0F, 11.0F, 5.0F, new Dilation(0.0F))
                .uv(50, 32).cuboid(5.0F, -2.0F, -2.0F, 4.0F, 18.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(5.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        ModelPartData right_arm = modelPartData.addChild("right_arm", ModelPartBuilder.create(), ModelTransform.pivot(5.0F, -9.0F, 0.0F));

        ModelPartData shoulder_r2 = right_arm.addChild("shoulder_r2", ModelPartBuilder.create().uv(79, 2).cuboid(-9.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new Dilation(0.3F))
                .uv(56, 0).cuboid(-9.5F, 4.0F, -2.5F, 5.0F, 12.0F, 5.0F, new Dilation(0.0F))
                .uv(50, 54).cuboid(-9.0F, -2.0F, -2.0F, 4.0F, 17.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(-5.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        ModelPartData left_leg = modelPartData.addChild("left_leg", ModelPartBuilder.create(), ModelTransform.pivot(-2.7F, 5.0F, 0.0F));

        ModelPartData left_leg_fur_r1 = left_leg.addChild("left_leg_fur_r1", ModelPartBuilder.create().uv(99, 24).cuboid(0.0F, 7.2F, -2.3F, 6.0F, 12.0F, 5.0F, new Dilation(0.3F))
                .uv(34, 0).cuboid(0.0F, 7.2F, -2.3F, 6.0F, 12.0F, 5.0F, new Dilation(0.0F))
                .uv(32, 32).cuboid(0.2F, -2.0F, -2.0F, 5.0F, 20.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(2.7F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        ModelPartData right_leg = modelPartData.addChild("right_leg", ModelPartBuilder.create(), ModelTransform.pivot(2.7F, 5.0F, 0.0F));

        ModelPartData right_leg_fur_r1 = right_leg.addChild("right_leg_fur_r1", ModelPartBuilder.create().uv(100, 3).cuboid(-6.1F, 9.0F, -2.5F, 6.0F, 10.0F, 5.0F, new Dilation(0.3F))
                .uv(18, 56).cuboid(-6.1F, 9.0F, -2.5F, 6.0F, 10.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 39).cuboid(-5.2F, -2.0F, -2.0F, 5.0F, 18.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(-2.7F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));
        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void setAngles(SkinStealerRenderState state) {
        super.setAngles(state);
        this.setHeadAngles(state.yawDegrees, state.pitch);
        //TODO: add idle, walking and attack animations
        //this.animateWalking();
        //this.animate();
    }

    private void setHeadAngles(float headYaw, float headPitch){
        headYaw = MathHelper.clamp(headYaw, -30.0F, 30.0F);
        headPitch = MathHelper.clamp(headPitch, -25.0F, 45.0F);

        this.head.yaw = headYaw * 0.017453292F;
        this.head.pitch = headPitch * 0.017453292F;
    }
}
