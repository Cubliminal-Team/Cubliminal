package net.limit.cubliminal.block.custom;

import com.mojang.serialization.MapCodec;
import net.limit.cubliminal.block.custom.template.HorizontalConnectingBlock;
import net.minecraft.block.BlockState;

public class VentilationDuctBlock extends HorizontalConnectingBlock {

    public static final MapCodec<VentilationDuctBlock> CODEC = VentilationDuctBlock.createCodec(VentilationDuctBlock::new);

    public VentilationDuctBlock(Settings settings) {
        super(5.0f, 5.0f, 15.0f, 15.0f, 15.0f, 8.0f, settings);
    }

    @Override
    protected MapCodec<? extends HorizontalConnectingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected boolean connectsTo(BlockState state) {
        return state.getBlock() instanceof VentilationDuctBlock;
    }

}
