package net.limit.cubliminal.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.access.GameRendererAccessor;
import net.limit.cubliminal.access.PEAccessor;
import net.limit.cubliminal.client.sound.NoclipSoundInstance;
import net.limit.cubliminal.config.CubliminalConfig;
import net.limit.cubliminal.init.CubliminalEffects;
import net.ludocrypt.limlib.api.effects.LookupGrabber;
import net.ludocrypt.limlib.api.effects.post.PostEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Pool;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;


@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements GameRendererAccessor {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private Pool pool;

    @Unique
    private boolean clippingIntoWall = false;

    @Unique
    private boolean triggeredNoclip = false;

    @Override
    public void setClippingIntoWall(boolean bl) {
        this.clippingIntoWall = bl;
    }

    @Override
    public void setTriggered(boolean bl) {
        this.triggeredNoclip = bl;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawEntityOutlinesFramebuffer()V", shift = At.Shift.AFTER))
    private void cubliminal$renderPostEffects(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (client.player != null && client.world != null) {
            ClientPlayerEntity player = client.player;
            if (((PEAccessor) player).getNoclipEngine().isClipping()) {
                Optional<PostEffect> optional = snatch(Cubliminal.id("noclip"));
                if (optional.isPresent()) {
                    PostEffect effect = optional.get();
                    if (effect.shouldRender()) {
                        effect.beforeRender();
                        effect.render(
                                this.client.getShaderLoader().loadPostEffect(effect.getShaderLocation(), DefaultFramebufferSet.MAIN_ONLY),
                                this.client.getFramebuffer(),
                                this.pool,
                                tickCounter,
                                true
                        );
                    }
                }
            } else if (this.shouldRenderNoClip()) {
                Optional<PostEffect> optional = snatch(Cubliminal.id("noclip"));
                if (optional.isPresent()) {
                    PostEffect effect = optional.get();
                    if (effect.shouldRender() && (player.getWorld().getTime()) % 3 == 0) {
                        effect.beforeRender();
                        effect.render(
                                this.client.getShaderLoader().loadPostEffect(effect.getShaderLocation(), DefaultFramebufferSet.MAIN_ONLY),
                                this.client.getFramebuffer(),
                                this.pool,
                                tickCounter,
                                false
                        );
                        if (!client.getSoundManager().isPlaying(NoclipSoundInstance.WALL_CLIPPING)) {
                            client.getSoundManager().play(NoclipSoundInstance.WALL_CLIPPING);
                        }
                    }
                }
            } else if (player.hasStatusEffect(CubliminalEffects.PARANOIA)) {
                Optional<PostEffect> optional = snatch(Cubliminal.id("paranoia"));
                if (optional.isPresent()) {
                    PostEffect effect = optional.get();
                    if (effect.shouldRender()) {
                        effect.beforeRender();
                        effect.render(
                                this.client.getShaderLoader().loadPostEffect(effect.getShaderLocation(), DefaultFramebufferSet.MAIN_ONLY),
                                this.client.getFramebuffer(),
                                this.pool,
                                tickCounter,
                                tick
                        );
                    }
                }
            }
        }
    }

    @Unique
    private boolean shouldRenderNoClip() {
        return clippingIntoWall || triggeredNoclip;
    }

    @Unique
    private Optional<PostEffect> snatch(Identifier shaderId) {
        return LookupGrabber.snatch(
                client.world.getRegistryManager().getOptional(PostEffect.POST_EFFECT_KEY).get(),
                RegistryKey.of(PostEffect.POST_EFFECT_KEY, shaderId)
        );
    }
}
