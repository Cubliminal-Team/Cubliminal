package net.limit.cubliminal.level;

import net.minecraft.util.math.BlockPos;

public class Levels {

    public static Level LEVEL_0 = new Level(32, 0, 1, 7, 8, 8);
    public static LevelWithMaze LEVEL_1 = new LevelWithMaze(48, 0, 3, 16, 16, 16, 16, 16, 0);

    public static final BlockPos MANILA_ROOM = new BlockPos(7,  LEVEL_0.min_y + LEVEL_0.layer_height * LEVEL_0.layer_count + 3, 3);
}
