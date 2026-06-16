package net.limit.cubliminal.block.state;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;

public enum ConnectionMode implements StringIdentifiable {

    FLAT("flat"),
    UP("up"),
    DOWN("down");

    final String name;

    ConnectionMode(String name) {
        this.name = name;
    }

    public BlockPos offset(BlockPos origin) {
        return switch (this) {
            case UP -> origin.up();
            case FLAT -> origin;
            case DOWN -> origin.down();
        };
    }

    @Override
    public String asString() {
        return name;
    }
}
