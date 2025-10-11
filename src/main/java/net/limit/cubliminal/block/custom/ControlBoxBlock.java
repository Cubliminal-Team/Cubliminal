package net.limit.cubliminal.block.custom;

import net.limit.cubliminal.access.ServerWorldAccessor;
import net.limit.cubliminal.block.custom.template.RotatableBlock;
import net.limit.cubliminal.event.backrooms.BlackoutManager;
import net.limit.cubliminal.init.CubliminalSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;

import java.util.Optional;

public class ControlBoxBlock extends RotatableBlock {

    public static final BooleanProperty OPEN = Properties.OPEN;

    public ControlBoxBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(OPEN, false));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        boolean open = state.get(OPEN);
        if (open && this.tryToggleBlackout(state, world, pos)) {
            return ActionResult.SUCCESS;
        }
        if (!world.isClient()) {
            world.setBlockState(pos, state.with(OPEN, !open), Block.NOTIFY_LISTENERS);
        }
        CubliminalSounds.blockPlaySound(world, pos, open ? SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN : SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE);
        return ActionResult.SUCCESS;
    }

    private boolean tryToggleBlackout(BlockState state, World world, BlockPos pos) {
        if (world instanceof ServerWorldAccessor accessor) {
            BlackoutManager blackoutManager = accessor.blackoutManager();
            if (blackoutManager != null && world.getRandom().nextFloat() < 0.05) {
                RegistryKey<Biome> biome = world.getGeneratorStoredBiome(
                        BiomeCoords.fromBlock(pos.getX()), BiomeCoords.fromBlock(pos.getY()), BiomeCoords.fromBlock(pos.getZ())
                ).getKey().orElseThrow();
                Optional<BlackoutManager.Entry> optional = blackoutManager.forBiome(biome);

                if (optional.isPresent() && !blackoutManager.areLightsOff(optional.get())) {
                    CubliminalSounds.blockPlaySound(world, pos, CubliminalSounds.SHORT_CIRCUIT.value());
                    blackoutManager.toggleState(optional.get(), true);
                    // TODO flame particles could be spawned here
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(OPEN);
    }
}
