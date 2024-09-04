package net.limit.cubliminal.mixin.client;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.util.NoClipEngine;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
	@Shadow
	public Input input;

	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(method = "updateWaterSubmersionState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;updateWaterSubmersionState()Z", shift = At.Shift.AFTER), cancellable = true)
	private void onUpdateWaterSubmersionState(CallbackInfoReturnable<Boolean> cir) {
		if (NoClipEngine.isNoClipping(this)) cir.setReturnValue(this.isSubmergedInWater);
	}

	@Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setSprinting(Z)V", ordinal = 3, shift = At.Shift.AFTER))
	private void onTickMovementWater(CallbackInfo ci) {
		if (NoClipEngine.isNoClipping(this) && this.input.hasForwardMovement()) this.setSprinting(true);
	}

	@ModifyArg(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 0), index = 0)
	private int onSubmerged(int perTick) {
		return NoClipEngine.isNoClipping(this) ? perTick + (this.isSpectator() ? 0 : 10 - 1) : perTick;
	}
}
