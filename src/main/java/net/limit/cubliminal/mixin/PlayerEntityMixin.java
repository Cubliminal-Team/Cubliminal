package net.limit.cubliminal.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.access.PEAccessor;
import net.limit.cubliminal.advancements.AdvancementHelper;
import net.limit.cubliminal.block.entity.USBlockBlockEntity;
import net.limit.cubliminal.event.noclip.NoclipEngine;
import net.limit.cubliminal.event.sanity.SanityManager;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerEntity.class, priority = 1500)
public abstract class PlayerEntityMixin extends LivingEntity implements PEAccessor {

	@Shadow
	public abstract boolean isCreative();

	@Shadow
	public abstract boolean isSpectator();

	@Unique
	private final NoclipEngine noclipEngine = new NoclipEngine();
	@Unique
	private final SanityManager sanityManager = new SanityManager();

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
		super(type, world);
	}

	@Override
	public NoclipEngine getNoclipEngine() {
		return this.noclipEngine;
	}

	@Override
	public SanityManager getSanityManager() {
		return this.sanityManager;
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;update(Lnet/minecraft/server/network/ServerPlayerEntity;)V", shift = At.Shift.AFTER))
	private void onUpdateHunger(CallbackInfo ci, @Local ServerPlayerEntity player) {
		boolean vulnerableInBackrooms = !this.isCreative() && !this.isSpectator() && this.getWorld().getRegistryKey().getValue().getNamespace().equals(Cubliminal.MOD_ID);
		if (vulnerableInBackrooms && player.getServer().getTicks() % 4 == 0) {
			if (!this.getWorld().getDifficulty().equals(Difficulty.PEACEFUL)) this.sanityManager.update(player);
		}
		if (this.noclipEngine.isClipping() || (vulnerableInBackrooms && AdvancementHelper.visitedManilaRoom(player))) {
			this.noclipEngine.update(player);
		}
	}

	@Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;noClip:Z", shift = At.Shift.AFTER))
	private void onTickAfterNoClip(CallbackInfo ci) {
		if (this.noclipEngine.isClipping()) {
			this.noClip = true;
			this.fallDistance = 0;
			this.setOnGround(false);
			this.setFireTicks(0);
		}
	}

	@Inject(method = "readCustomDataFromNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;readNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
	private void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
		this.sanityManager.readNbt(nbt);
		this.noclipEngine.readNbt(nbt);
	}

	@Inject(method = "writeCustomDataToNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;writeNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
	private void onWriteNbt(NbtCompound nbt, CallbackInfo ci) {
		this.sanityManager.writeNbt(nbt);
		this.noclipEngine.writeNbt(nbt);
	}

	@Inject(method = "updatePose", at = @At("HEAD"), cancellable = true)
	private void onUpdatePose(CallbackInfo ci) {
		if (this.noclipEngine.isClipping()) {
			this.setPose(EntityPose.STANDING);
			ci.cancel();
		}
	}

	@Inject(method = "onSwimmingStart", at = @At("HEAD"), cancellable = true)
	private void onOnSwimmingStart(CallbackInfo ci) {
		if (this.noclipEngine.isClipping()) ci.cancel();
	}

	@Inject(method = "shouldSwimInFluids", at = @At("HEAD"), cancellable = true)
	private void canSwimInFluids(CallbackInfoReturnable<Boolean> cir) {
		if (this.noclipEngine.isClipping()) cir.setReturnValue(false);
	}


	@Override
	public void openUSBlockScreen(USBlockBlockEntity blockEntity) {
	}


}
