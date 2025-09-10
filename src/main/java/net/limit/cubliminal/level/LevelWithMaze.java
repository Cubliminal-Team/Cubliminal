package net.limit.cubliminal.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class LevelWithMaze extends Level {
    public static Codec<LevelWithMaze> LEVEL_WITH_MAZE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("world_height", 256).forGetter(level -> level.world_height),
            Codec.INT.optionalFieldOf("min_y", 0).forGetter(level -> level.min_y),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("max_layer_count", 0).forGetter(level -> level.layer_count),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("layer_height", 16).forGetter(level -> level.layer_height),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("spacing_x", 16).forGetter(level -> level.spacing_x),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("spacing_z", 16).forGetter(level -> level.spacing_z),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("maze_width", 4).forGetter(level -> level.maze_width),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("maze_height", 4).forGetter(level -> level.maze_height),
            Codec.LONG.optionalFieldOf("maze_seed_modifier", 0L).forGetter(level -> level.maze_seed_modifier)
    ).apply(instance, instance.stable(LevelWithMaze::new)));

    public final int maze_width;
    public final int maze_height;
    public final long maze_seed_modifier;

    public LevelWithMaze(int world_height, int min_y, int max_layer_count, int layer_height,
                         int spacing_x, int spacing_z, int maze_width, int maze_height, long maze_seed_modifier) {
        super(world_height, min_y, max_layer_count, layer_height, spacing_x, spacing_z);
        if (maze_width > 0) {
            this.maze_width = maze_width;
        } else {
            throw new IllegalStateException("Maze width should always greater than 0");
        }
        if (maze_height > 0) {
            this.maze_height = maze_height;
        } else {
            throw new IllegalStateException("Maze width should always greater than 0");
        }
        this.maze_seed_modifier = maze_seed_modifier;
    }

}
