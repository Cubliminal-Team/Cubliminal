package net.limit.cubliminal.block.state.pipeType;

import net.minecraft.util.StringIdentifiable;

public enum LargeHorizontalPipeTypes implements StringIdentifiable {
    STRAIGHT_PIPE("straight"),
    CORNER_PIPE("corner");

    private final String name;

    LargeHorizontalPipeTypes(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
