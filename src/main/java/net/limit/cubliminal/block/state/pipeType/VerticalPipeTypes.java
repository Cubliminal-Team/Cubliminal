package net.limit.cubliminal.block.state.pipeType;

import net.minecraft.util.StringIdentifiable;

public enum VerticalPipeTypes implements StringIdentifiable {
    STRAIGHT("straight"),
    PLACEHOLDER("placeholder");

    private final String name;
    VerticalPipeTypes(String name){
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
