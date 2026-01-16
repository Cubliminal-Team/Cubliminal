package net.limit.cubliminal.block.state;

import net.ludocrypt.limlib.api.world.LimlibHelper;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;

public enum RandRot implements StringIdentifiable {

    ROT_0("rot_0", 45.0f),
    ROT_1("rot_1", 22.5f),
    NO_ROT("no_rot", 0.0f),
    ROT_3("rot_3", -22.5f),
    ROT_4("rot_4", -45.0f);

    final String name;
    final float rotation;

    RandRot(String name, float rotation) {
        this.name = name;
        this.rotation = rotation;
    }

    public float rotation() {
        return rotation;
    }

    public static RandRot random(BlockPos pos) {
        long seed = Math.abs(LimlibHelper.blockSeed(pos));
        return values()[Math.toIntExact(seed % values().length)];
    }

    @Override
    public String asString() {
        return name;
    }
}
