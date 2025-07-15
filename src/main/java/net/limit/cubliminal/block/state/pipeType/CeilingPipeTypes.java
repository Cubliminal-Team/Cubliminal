package net.limit.cubliminal.block.state.pipeType;

import net.minecraft.util.StringIdentifiable;

public enum CeilingPipeTypes implements StringIdentifiable {
    ONE_LAYER_STRAIGHT("one_layer_straight"),
    ONE_LAYER_VERTICAL_PIPE_CONNECTION("one_layer_vertical_pipe_connection"),
    TWO_LAYER_STRAIGHT("two_layer_straight"),
    TWO_LAYER_CORNER("two_layer_corner"),
    TWO_LAYER_UP("two_layer_up");

    private final String name;

    CeilingPipeTypes(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
