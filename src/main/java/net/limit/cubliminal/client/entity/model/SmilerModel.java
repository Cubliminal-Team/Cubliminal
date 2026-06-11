// Made with Blockbench 4.10.3
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package net.limit.cubliminal.client.entity.model;

import net.limit.cubliminal.client.entity.state.CustomEntityRenderState;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings({"unused"})
public class SmilerModel extends EntityModel<CustomEntityRenderState> {
	private final ModelPart face;

	public SmilerModel(ModelPart root) {
        super(root);
        this.face = root.getChild("face");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData face = modelPartData.addChild("face", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData face_r1 = face.addChild("face_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -39.0F, -8.0F, 0.0F, 39.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}

	public ModelPart getPart() {
		return this.face;
	}

	@Override
	public void setAngles(CustomEntityRenderState state) {
		super.setAngles(state);
		// TODO: Implement animations.
	}

	private void setHeadAngles(float headYaw, float headPitch){
		headYaw = MathHelper.clamp(headYaw, -30.0F, 30.0F);
		headPitch = MathHelper.clamp(headPitch, -25.0F, 45.0F);

		this.face.yaw = headYaw * 0.017453292F;
		this.face.pitch = headPitch * 0.017453292F;
	}
}