package net.limit.cubliminal.block.custom.template;

import com.google.common.base.Predicates;
import net.limit.cubliminal.entity.SeatEntity;
import net.limit.cubliminal.init.CubliminalEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SeatBlock extends HorizontalFacingBlock {

    public SeatBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
    }

    public float getPassengerYaw(BlockState state, Entity entity) {
        return entity.getYaw();
    }

    public float seatHeight(BlockState state) {
        return 1;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this
                .getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        List<SeatEntity> list = world.getEntitiesByClass(SeatEntity.class, new Box(pos), Predicates.alwaysTrue());
        if (world.isClient()) {
            return ActionResult.CONSUME;
        } else if (list.isEmpty()) {
            SeatEntity seatEntity = CubliminalEntities.SEAT_ENTITY.spawn((ServerWorld) world, pos, SpawnReason.TRIGGERED);
            if (seatEntity != null) {
                seatEntity.setYaw(state.get(FACING).asRotation());
                player.startRiding(seatEntity, true);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
