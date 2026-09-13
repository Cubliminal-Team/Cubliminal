package net.limit.cubliminal.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

/**
 * An inheritor of {@link Level} that uses a maze-like layout.
 */

public class LevelWithMaze extends Level {
    public static Codec<LevelWithMaze> LEVEL_WITH_MAZE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("name").forGetter(level -> level.name),
            Codec.INT.optionalFieldOf("world_height", 256).forGetter(level -> level.world_height),
            Codec.INT.optionalFieldOf("min_y", 0).forGetter(level -> level.min_y),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("max_layer_count", 0).forGetter(level -> level.layer_count),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("layer_height", 16).forGetter(level -> level.layer_height),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("spacing_x", 16).forGetter(level -> level.spacing_x),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("spacing_z", 16).forGetter(level -> level.spacing_z),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("maze_size_x", 4).forGetter(level -> level.maze_size_x),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("maze_size_z", 4).forGetter(level -> level.maze_size_z),
            Codec.LONG.optionalFieldOf("maze_seed_modifier", 0L).forGetter(level -> level.maze_seed_modifier)
    ).apply(instance, instance.stable(LevelWithMaze::new)));

    public final int maze_size_x;
    public final int maze_size_z;
    public final long maze_seed_modifier;

    /**
     * Note that {@code maze_width} and {@code maze_height} are in cells, not in blocks.
     * @param name Internal name of the Level.
     * @param world_height World height in blocks. Must be a multiple of 16.
     * @param min_y Minimum height in blocks. Must be a multiple of 16.
     * @param max_layer_count Maximum number of floors within the top and bottom boundaries of the world. Note that there won't be more layers than those that fit.
     * @param layer_height Per layer height in blocks. One layer refers to a sheet of tiled cells.
     * @param spacing_x How many blocks a cell occupies in the X axis.
     * @param spacing_z How many blocks a cell occupies in the Z axis.
     * @param maze_size_x How many cells tiled horizontally in the Z axis takes up the maze.
     * @param maze_size_z How many cells tiled horizontally in the X axis takes up the maze.
     * @param maze_seed_modifier A number that slightly modifies the pseudorandom number sequence.
     */
    public LevelWithMaze(Identifier name, int world_height, int min_y, int max_layer_count, int layer_height,
                         int spacing_x, int spacing_z, int maze_size_x, int maze_size_z, long maze_seed_modifier) {
        super(name, world_height, min_y, max_layer_count, layer_height, spacing_x, spacing_z);
        if (maze_size_x > 0) {
            this.maze_size_x = maze_size_x;
        } else {
            throw new IllegalStateException("Maze size X should always greater than 0");
        }
        if (maze_size_z > 0) {
            this.maze_size_z = maze_size_z;
        } else {
            throw new IllegalStateException("Maze size Z should always greater than 0");
        }
        this.maze_seed_modifier = maze_seed_modifier;
    }

}
