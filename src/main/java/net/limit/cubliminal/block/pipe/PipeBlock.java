package net.limit.cubliminal.block.pipe;

import net.limit.cubliminal.block.entity.PipeBlockEntity;
import net.limit.cubliminal.block.state.pipeType.LargeHorizontalPipeTypes;
import net.limit.cubliminal.init.CubliminalFluids;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Random;

public class PipeBlock extends Block implements Waterloggable, BlockEntityProvider {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final BooleanProperty LEAKING = BooleanProperty.of("leaking");
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    public PipeBlock(Settings settings) {
        super(settings);
        if (this.getDefaultState().contains(ConnectorPipeBlock.CONNECTOR)){
            this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false).with(ConnectorPipeBlock.CONNECTOR, false));
        } else {
            this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PipeBlockEntity pipeBlockEntity){
            if (pipeBlockEntity.getFluidContainer() == Fluids.EMPTY) {
                pipeBlockEntity.setFluidContainer(CubliminalFluids.ALMOND_WATER);
            }

            pipeBlockEntity.setPipeVariety(LargeHorizontalPipeTypes.STRAIGHT_PIPE);

        }
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        double chance = new Random().nextDouble();;
        boolean leakingDefault = chance < 0.2f;
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(LEAKING,leakingDefault);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder
                .add(WATERLOGGED)
                .add(FACING)
                .add(LEAKING);
    }

    public double getRandomPosition(double min, double max){
        Random random = new Random();
        double randomDouble = random.nextDouble() * (max - min) + min;
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(randomDouble));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        super.randomDisplayTick(state, world, pos, random);
        double chance = random.nextDouble();

        VoxelShape shape = state.getOutlineShape(world,pos);
        double minX = shape.getMin(Direction.Axis.X);
        double maxX = shape.getMax(Direction.Axis.X);
        double minZ = shape.getMin(Direction.Axis.Z);
        double maxZ = shape.getMax(Direction.Axis.Z);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (chance < 0.2f){
            boolean leaking = state.get(LEAKING);
            if (leaking){
                if (blockEntity instanceof PipeBlockEntity pipeBlockEntity){
                    double x = pos.getX() + getRandomPosition(minX, maxX);
                    double y = pos.getY();
                    double z = pos.getZ() + getRandomPosition(minZ, maxZ);
                    Fluid fluid = pipeBlockEntity.getFluidContainer();
                    if (!fluid.matchesType(Fluids.EMPTY)){
                        world.addParticle(pipeBlockEntity.getFluidContainer().getParticle(), x, y, z, 0.0f, 0.0f, 0.0f);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(pos, state);
    }
}
