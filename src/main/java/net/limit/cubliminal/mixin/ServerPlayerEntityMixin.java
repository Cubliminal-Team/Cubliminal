package net.limit.cubliminal.mixin;

import net.limit.cubliminal.init.CubliminalGameRules;
import net.limit.cubliminal.util.CorpseManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Redirect(
            method = "onDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V"
            )
    )
    private void preventDrop(ServerPlayerEntity player, ServerWorld world, DamageSource source) {
        // Checks to see if the game rule is enabled for corpses to spawn on player's death
        if (world.getGameRules().getBoolean(CubliminalGameRules.CORPSES_ON_DEATH)){
            // Spawn a corpse block at the player's position
            CorpseManager.createCorpse((ServerPlayerEntity)(Object)this, player.getInventory(), source);
        } else {
            ((LivingEntityAccessor) player).invokeDrop(world, source);
        }
    }
}
