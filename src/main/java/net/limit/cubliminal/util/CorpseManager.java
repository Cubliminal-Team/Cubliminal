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
    public static void createCorpse(ServerPlayerEntity player, PlayerInventory inventory, DamageSource source){
        World world = player.getEntityWorld();
        ServerWorld serverWorld = player.getServerWorld();
        world.setBlockState(player.getBlockPos(), CubliminalBlocks.CORPSE.getDefaultState());
        BlockEntity blockEntity = world.getBlockEntity(player.getBlockPos());
        BlockState blockState = world.getBlockState(player.getBlockPos());

        if (blockEntity instanceof CorpseBlockEntity corpseBlockEntity){
            corpseBlockEntity.setPlayerName(player.getName().getString());
            corpseBlockEntity.setUuid(player.getUuid());
            if (!serverWorld.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)){
                corpseBlockEntity.setInventory(inventory);
            }

            if (source.getAttacker() instanceof SkinStealerEntity){
                world.setBlockState(player.getBlockPos(), blockState.with(CorpseBlock.SKIN_STOLEN, true));
            }
        }
    }
}
