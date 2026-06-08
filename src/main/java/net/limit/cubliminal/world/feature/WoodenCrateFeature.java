package net.limit.cubliminal.world.feature;

import com.mojang.serialization.Codec;
import net.limit.cubliminal.block.custom.JumbledDocumentsBlock;
import net.limit.cubliminal.block.custom.template.BoardBlock;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.CountConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class WoodenCrateFeature extends Feature<CountConfig> {

    public WoodenCrateFeature(Codec<CountConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<CountConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        BlockPos.Mutable mutable = origin.mutableCopy();
        Random random = context.getRandom();
        int count = context.getConfig().getCount().get(random);

        int i = 0;
        for (int dy = 0; dy < 2; dy++) {
            mutable.setY(origin.getY() + dy);
            for (int k = 0; k < count; k++) {
                int dx = random.nextInt(5) - random.nextInt(5);
                int dz = random.nextInt(5) - random.nextInt(5);
                mutable.setX(origin.getX() + dx);
                mutable.setZ(origin.getZ() + dz);
                if (world.getBlockState(mutable.down()).isIn(CubliminalBlocks.FLOOR_PALETTE) &&
                        world.getBlockState(mutable).isIn(BlockTags.AIR)) {
                    i++;
                    switch (random.nextInt(18 + dy * 10)) {
                        case 0, 1 ->
                                world.setBlockState(mutable, CubliminalBlocks.WOODEN_CRATE.getDefaultState(), Block.NOTIFY_LISTENERS);
                        case 2 -> world.setBlockState(mutable, CubliminalBlocks.JUMBLED_DOCUMENTS.getDefaultState()
                                .with(JumbledDocumentsBlock.FACING, Direction.Type.HORIZONTAL.random(random)), Block.NOTIFY_LISTENERS);
                        case 3, 4, 5 -> world.setBlockState(mutable, CubliminalBlocks.WOODEN_PLANK.getDefaultState()
                                .with(BoardBlock.FACING, Direction.Type.HORIZONTAL.random(random))
                                .with(BoardBlock.FACE, BlockFace.FLOOR), Block.NOTIFY_LISTENERS);
                        case 6, 7 ->
                                world.setBlockState(mutable, Blocks.TRIPWIRE.getDefaultState(), Block.NOTIFY_LISTENERS);
                        case 9, 10 ->
                                world.setBlockState(random.nextBoolean() ? mutable : mutable.down(), Blocks.SPRUCE_PLANKS.getDefaultState(), Block.NOTIFY_LISTENERS);
                        case 11 ->
                                world.setBlockState(mutable, Blocks.SPRUCE_FENCE.getDefaultState(), Block.NOTIFY_LISTENERS);
                        case 12, 13 ->
                                world.setBlockState(random.nextBoolean() ? mutable : mutable.down(), Blocks.GRAVEL.getDefaultState(), Block.NOTIFY_LISTENERS);
                        case 14, 15 ->
                                world.setBlockState(mutable.down(), Blocks.TUFF.getDefaultState(), Block.NOTIFY_LISTENERS);
                        case 16 ->
                                world.setBlockState(mutable, CubliminalBlocks.CRATE_AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                        case 17 -> world.setBlockState(mutable, Blocks.TORCH.getDefaultState(), Block.NOTIFY_LISTENERS);
                        default -> {}
                    }
                }
            }
        }

        return i > 0;
    }
}
