package net.limit.cubliminal.client.screen.roomcreator.data;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class DoorBuilder {
    private int x = 0;
    private int z = 0;
    private int index = 0;
    private Direction facing = Direction.NORTH;

    // This is run AFTER updating and clamping the index
    public void recalculateData(RoomBuilder room, int index, Direction facing) {
        this.index = index;
        this.facing = facing;

        if (facing.getAxis() == Direction.Axis.X) {
            int maxX = room.getSizeX() - 1;
            this.x = index;
            this.z = facing == Direction.EAST ? maxX : 0;
        } else {
            int maxZ = room.getSizeZ() - 1;
            this.x = facing == Direction.SOUTH ? maxZ : 0;
            this.z = index;
        }
    }

    public void afterResizingRoom(RoomBuilder room) {
        this.x = MathHelper.clamp(this.x, 0, room.getSizeX() - 1);
        this.z = MathHelper.clamp(this.z, 0, room.getSizeZ() - 1);
        this.index = this.facing.getAxis() == Direction.Axis.X ? this.x : this.z;
    }

    public int getIndex() {
        return this.index;
    }

    public Direction getFacing() {
        return this.facing;
    }
}
