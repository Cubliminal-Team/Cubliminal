package net.limit.cubliminal.mixin;

import net.limit.cubliminal.access.ServerWorldAccessor;
import net.limit.cubliminal.event.backrooms.BlackoutManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {

    @Shadow
    @Final
    private ServerWorld world;

    @Inject(method = "tickChunks(Lnet/minecraft/util/profiler/Profiler;JLjava/util/List;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/GameRules;getInt(Lnet/minecraft/world/GameRules$Key;)I", shift = At.Shift.AFTER))
    private void onTickChunks(Profiler profiler, long timeDelta, List<WorldChunk> chunks, CallbackInfo ci) {
        BlackoutManager blackoutManager = ((ServerWorldAccessor) this.world).blackoutManager();
        if (blackoutManager != null) {
            blackoutManager.tick();
        }
    }

}
