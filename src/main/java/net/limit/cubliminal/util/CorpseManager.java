package net.limit.cubliminal.util;

import net.limit.cubliminal.block.custom.CorpseBlock;
import net.limit.cubliminal.block.entity.CorpseBlockEntity;
import net.limit.cubliminal.entity.hostile.SkinStealerEntity;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class CorpseManager {
    /**
     * Creates a corpse block at the player's position
     * @param player The server player entity
     * @param inventory The player inventory
     * @param source The damage source (Primarily used for skin stealing)
     */
    public static void createCorpse(ServerPlayerEntity player, PlayerInventory inventory, DamageSource source){
        // Get the world
        World world = player.getEntityWorld();
        // Gets server world
        ServerWorld serverWorld = player.getServerWorld();

        // Spawn a corpse block at the player's position
        world.setBlockState(player.getBlockPos(), CubliminalBlocks.CORPSE.getDefaultState());

        // Gets the block entity at the player's position
        BlockEntity blockEntity = world.getBlockEntity(player.getBlockPos());
        // Gets the block state at the player's position
        BlockState blockState = world.getBlockState(player.getBlockPos());

        // Check if the block entity is a CorpseBlockEntity
        if (blockEntity instanceof CorpseBlockEntity corpseBlockEntity){
            DebugLogger.debug("Hello???");
            // Set the player name and UUID for corpse block entity
            corpseBlockEntity.setPlayerName(player.getName().getString());
            corpseBlockEntity.setUuid(player.getUuid());
            // Set the inventory for corpse block entity if keep inventory is disabled
            if (!serverWorld.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)){
                corpseBlockEntity.setInventory(inventory);
            }

            // Check if the attacker is a SkinStealerEntity
            if (source.getAttacker() instanceof SkinStealerEntity){
                // Set the skin stolen block state to true
                world.setBlockState(player.getBlockPos(), blockState.with(CorpseBlock.SKIN_STOLEN, true));
            }
        }
    }
}
