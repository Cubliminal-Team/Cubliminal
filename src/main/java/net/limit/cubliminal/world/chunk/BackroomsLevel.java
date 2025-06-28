package net.limit.cubliminal.world.chunk;

import net.limit.cubliminal.level.Level;
import net.limit.cubliminal.world.room.Room;
import net.minecraft.util.math.BlockPos;

public interface BackroomsLevel {

    default void saveRoom(BlockPos pos, Room.Instance room) {}

    Level getLevel();
}
