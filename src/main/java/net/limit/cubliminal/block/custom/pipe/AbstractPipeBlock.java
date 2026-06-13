package net.limit.cubliminal.block.custom.pipe;

import net.limit.cubliminal.block.custom.template.BlockVariantHolder;
import net.limit.cubliminal.block.state.IdentifierProperty;
import net.limit.cubliminal.init.CubliminalFluids;
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
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The base class of all pipe blocks. The held fluid is stored as a block state property to avoid unnecessary block entities.
 * Note that only leaking pipes can have fluids in them.
 */
public abstract class AbstractPipeBlock extends HorizontalFacingBlock implements Waterloggable, BlockVariantHolder {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final BooleanProperty LEAKING = BooleanProperty.of("leaking");
    public static IdentifierProperty FLUID_CONTAINER = IdentifierProperty.of("fluid_container", () -> {
        CubliminalFluids.registerAll();
        return Registries.FLUID.getIds().stream()
                .filter(id -> !id.getPath().contains("flowing"))
                .toList();
    });

    public AbstractPipeBlock(Settings settings) {
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

    // TODO northernlimit, add documentation for this.
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

    /**
     * Gets a random position between the minimum and maximum
     * <b>Note:</b> This method may be moved into a separate class in the future.
     * @param min The minimum number to check for
     * @param max The maximum number to check for
     * @return The random double number that is between the min and max.
     */
    public double getRandomPosition(double min, double max) {
        // Init the random
        Random random = Random.create();
        // Gets a random number between the min and max.
        double raw = random.nextDouble() * (max - min) + min;
        // Rounds out the raw and returns it.
        return Math.round(raw * 100.0) / 100.0;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED)) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    // This random display tick checks to see if a pipe is leaking, sees what fluid it contains, then, it creates dripping
    // particles that matches.
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        // Checks to see if the pipe's state is leaking and sees if the random double is greater than 0.2.
        // The random double ensures it isn't leaking every tick. Ensure it only leaks with a 0.2 chance.
        if (state.get(LEAKING) && random.nextDouble() < 0.2f) {
            // Grabs the voxel shape of the pipe.
            VoxelShape shape = state.getOutlineShape(world,pos);
            // Gets a random position on the x-axis.
            double x = pos.getX() + getRandomPosition(shape.getMin(Direction.Axis.X), shape.getMax(Direction.Axis.X));
            // Gets the y position
            double y = pos.getY();
            // Gets a random position on the z-axis.
            double z = pos.getZ() + getRandomPosition(shape.getMin(Direction.Axis.Z), shape.getMax(Direction.Axis.Z));
            // Checks the state to see what fluid is contained within the pipe.
            Fluid fluid = Registries.FLUID.get(IdentifierProperty.decode(state.get(FLUID_CONTAINER)));
            // Checks to see if the fluid container isn't empty.
            if (!fluid.matchesType(Fluids.EMPTY)) {
                // Adds a fluid drip particle at the designated location.
                world.addParticle(((FluidAccessor) fluid).invokeGetParticle(), x, y, z, 0.0f, 0.0f, 0.0f);
            }
        }
    }

    @Override
    public void changeToVariant(ChunkRegion region, BlockState prevState, BlockPos pos, Random random) {
        if (random.nextFloat() < 0.1) {
            Fluid fluid = toFluid(prevState);
            region.setBlockState(pos, prevState
                            .with(LEAKING, true)
                            .with(FLUID_CONTAINER, toFluidContainer(fluid.equals(Fluids.EMPTY) ? CubliminalFluids.ALMOND_WATER : fluid)),
                    Block.FORCE_STATE);
        }
    }
}
