package net.limit.cubliminal.fluid;

import net.limit.cubliminal.block.fluid.CustomFluidBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.Optional;

public abstract class BackroomsFlowableFluid extends FlowableFluid {
    private final SoundEvent ambience;
    private final SoundCategory category;

    /**
     * Initializes the flowable fluid class.
     * @param ambience The ambience the fluid will play in the background.
     * @param category The sound category for the ambience.
     */
    public BackroomsFlowableFluid(SoundEvent ambience, SoundCategory category){
        this.ambience = ambience;
        this.category = category;
    }

    /**
     * Set the bottle item that will be traded with the player when the player
     * tries to interact with the fluid.
     * @return The item that will possibly contain the contained fluid and replace the empty glass bottle with.
     */
    public abstract ItemStack getBottleItem();

    /**
     * The fluid block to used. It's primarily use is so mixins can use the block settings.
     * @return The custom fluid block.
     */
    public abstract CustomFluidBlock getFluidBlock();

    @Override
    protected void randomDisplayTick(World world, BlockPos pos, FluidState state, net.minecraft.util.math.random.Random random) {
        // Checks to see if water is still and not falling.
        if (!state.isStill() && !(Boolean)state.get(FALLING)) {
            // Continues at a random tick.
            if (random.nextInt(64) == 0) {
                // At random, it will play the ambient sounds.
                world.playSound((double)pos.getX() + 0.5D,
                        (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D,
                        this.ambience, this.category,
                        random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F,
                        false);
            }
        } else if (random.nextInt(10) == 0) {
            world.addParticle(ParticleTypes.UNDERWATER, (double)pos.getX() + random.nextDouble(),
                    (double)pos.getY() + random.nextDouble(),
                    (double)pos.getZ() + random.nextDouble(),
                    0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView world) {
        return 1;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return this.getFluidBlock().getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !fluid.isIn(FluidTags.WATER);
    }

    @Override
    public int getTickRate(WorldView world) {
        return 5;
    }

    @Override
    protected float getBlastResistance() {
        return 100.0f;
    }

    @Override
    public boolean isStill(FluidState state) {
        return false;
    }

    @Override
    public int getLevel(FluidState state) {
        return 7;
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == getStill() || fluid == getFlowing();
    }

    @Override
    public Optional<SoundEvent> getBucketFillSound() {
        return Optional.of(SoundEvents.ITEM_BUCKET_FILL);
    }
}
