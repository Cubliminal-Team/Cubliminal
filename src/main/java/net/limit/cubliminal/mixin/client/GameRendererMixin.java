package net.limit.cubliminal.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.access.PEAccessor;
import net.limit.cubliminal.client.hud.NoclipHudOverlay;
import net.limit.cubliminal.client.sound.NoclipSoundInstance;
import net.limit.cubliminal.config.CubliminalConfig;
import net.limit.cubliminal.init.CubliminalEffects;
import net.ludocrypt.limlib.impl.shader.PostProcesser;
import net.ludocrypt.limlib.impl.shader.PostProcesserManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Pool;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private Pool pool;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawEntityOutlinesFramebuffer()V", shift = At.Shift.AFTER))
    private void cubliminal$renderPostEffects(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (client.player != null && client.world != null && !CubliminalConfig.get().disableAggressiveGraphics) {
            ClientPlayerEntity player = client.player;
            if (((PEAccessor) player).getNoclipEngine().isClipping()) {
                PostProcesser shader = PostProcesserManager.INSTANCE.find(Cubliminal.id("noclip"));
                shader.setUniform("NoclipTicks", (float) Math.abs(((PEAccessor) player).getNoclipEngine().getTicksToNc()));
                shader.render(client.getFramebuffer(), this.pool);
            } else if (NoclipHudOverlay.INSTANCE.shouldRender()) {
                for (int i = 0; i < 2; i++) {
                    if ((player.getWorld().getTime() + i) % 6 == 0) {
                        PostProcesserManager.INSTANCE.find(Cubliminal.id("noclip")).render(client.getFramebuffer(), this.pool);
                        if (!client.getSoundManager().isPlaying(NoclipSoundInstance.WALL_CLIPPING)) {
                            client.getSoundManager().play(NoclipSoundInstance.WALL_CLIPPING);
                        }
                        break;
                    }
                }
            } else if (player.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(CubliminalEffects.PARANOIA))) {
                PostProcesserManager.INSTANCE.find(Cubliminal.id("paranoia")).render(client.getFramebuffer(), this.pool);
            }
        }
    }
}
