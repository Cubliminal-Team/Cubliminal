package net.limit.cubliminal.block.custom.pipe;

import net.limit.cubliminal.block.state.IdentifierProperty;
import net.limit.cubliminal.mixin.FluidAccessor;
import net.ludocrypt.limlib.api.world.LimlibHelper;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class PipeBlock extends Block implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final BooleanProperty LEAKING = BooleanProperty.of("leaking");
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final IdentifierProperty FLUID_CONTAINER = IdentifierProperty.of("fluid_container",
            () -> Registries.FLUID.getIds().stream().filter(id -> !id.getPath().contains("flowing")).toList());

    public PipeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(WATERLOGGED, false)
                .with(LEAKING, false)
                .with(FLUID_CONTAINER, toFluidContainer(Fluids.EMPTY)));
    }

    public static String toFluidContainer(Fluid fluid) {
        return IdentifierProperty.encode(Registries.FLUID.getId(fluid));
    }

    public static Fluid toFluid(BlockState state) {
        if (!state.contains(FLUID_CONTAINER)) {
            throw new IllegalArgumentException("BlockState: " + state + " doesn't contain a fluid");
        } else {
            return Registries.FLUID.get(IdentifierProperty.decode(state.get(FLUID_CONTAINER)));
        }
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        double chance = Random.create(LimlibHelper.blockSeed(ctx.getBlockPos())).nextDouble();
        boolean leakingDefault = chance < 0.2f;
        return this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(LEAKING, leakingDefault);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING, LEAKING, FLUID_CONTAINER);
    }

    public double getRandomPosition(double min, double max) {
        Random random = Random.create();
        double raw = random.nextDouble() * (max - min) + min;
        return Math.round(raw * 100.0) / 100.0;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED)) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (state.get(LEAKING) && random.nextDouble() < 0.2f) {
            VoxelShape shape = state.getOutlineShape(world,pos);
            double x = pos.getX() + getRandomPosition(shape.getMin(Direction.Axis.X), shape.getMax(Direction.Axis.X));
            double y = pos.getY();
            double z = pos.getZ() + getRandomPosition(shape.getMin(Direction.Axis.Z), shape.getMax(Direction.Axis.Z));
            Fluid fluid = Registries.FLUID.get(IdentifierProperty.decode(state.get(FLUID_CONTAINER)));
            if (!fluid.matchesType(Fluids.EMPTY)) {
                world.addParticle(((FluidAccessor) fluid).invokeGetParticle(), x, y, z, 0.0f, 0.0f, 0.0f);
            }
        }
    }
}
