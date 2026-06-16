package net.limit.cubliminal.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.limit.cubliminal.event.backrooms.PlayerSkinDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.world.PersistentStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Unique
    private final PlayerSkinDataManager playerSkinDataManager = new PlayerSkinDataManager();

    @Inject(
            method = "createWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;initScoreboard(Lnet/minecraft/world/PersistentStateManager;)V"
            )
    )
    private void initPlayerSkinDataManager(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci, @Local PersistentStateManager persistentStateManager) {
        persistentStateManager.getOrCreate(this.playerSkinDataManager.getPersistentStateType(), "player_skin_manager");
    }
}
