package net.limit.cubliminal.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ludocrypt.limlib.impl.shader.PostProcesser;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PostProcesser.class)
public abstract class PostProcesserMixin {

    @Shadow
    protected PostEffectProcessor shader;

    @Shadow(remap = false)
    public abstract void init();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cubliminal$initShader(Identifier location, CallbackInfo ci) {
        this.init();
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(Lnet/minecraft/client/render/FrameGraphBuilder;IILnet/minecraft/client/gl/PostEffectProcessor$FramebufferSet;)V"), remap = false)
    private void cubliminal$fixNullShader(PostEffectProcessor instance, FrameGraphBuilder builder, int textureWidth,
                                          int textureHeight, PostEffectProcessor.FramebufferSet framebufferSet,
                                          Operation<Void> original, @Local(argsOnly = true) Framebuffer framebuffer) {
        this.shader.render(builder, textureWidth, textureHeight, PostEffectProcessor.FramebufferSet
                .singleton(PostEffectProcessor.MAIN, builder.createObjectNode("main", framebuffer)));
    }
}
