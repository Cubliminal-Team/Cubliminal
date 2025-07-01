package net.limit.cubliminal.level;

import net.limit.cubliminal.init.CubliminalRegistrar;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public enum Levels {
    LEVEL_0(CubliminalRegistrar.THE_LOBBY_KEY,
            new Level(32, 0, 1, 7, 8, 8)),

    LEVEL_1(CubliminalRegistrar.HABITABLE_ZONE_KEY,
            new LevelWithMaze(32, 0, 2, 16, 16,
                    16, 16, 16, 0));

    final RegistryKey<World> key;
    final Level level;
    Levels(RegistryKey<World> key, Level level) {
        this.key = key;
        this.level = level;
    }

    public static final BlockPos MANILA_ROOM = new BlockPos(7,  LEVEL_0.level.min_y
            + LEVEL_0.level.layer_height * LEVEL_0.level.layer_count + 3, 3);

    public Level getLevel() {
        return this.level;
    }
}
