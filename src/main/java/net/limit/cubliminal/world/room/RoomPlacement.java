package net.limit.cubliminal.world.room;

import net.limit.cubliminal.util.MazeUtil;
import net.ludocrypt.limlib.api.world.Manipulation;
import net.minecraft.util.math.random.Random;

import java.util.function.Function;

public record RoomPlacement(Function<Random, String> id, byte packedManipulation) {

    public String get(Random random) {
        return this.id.apply(random);
    }

    public Manipulation manipulation() {
        return MazeUtil.unpack(this.packedManipulation);
    }
}
