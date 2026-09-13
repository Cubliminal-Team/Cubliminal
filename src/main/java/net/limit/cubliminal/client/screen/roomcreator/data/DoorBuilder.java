package net.limit.cubliminal.client.screen.roomcreator.data;

import net.limit.cubliminal.util.MazeUtil;
import net.limit.cubliminal.util.Vec2b;
import net.limit.cubliminal.world.room.Door;
import net.ludocrypt.limlib.api.world.maze.MazeComponent;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class DoorBuilder {
    private final float thickness = 0.2f;

    private int x = 0;
    private int z = 0;
    private int index = 0;
    private Direction facing = Direction.NORTH;

    // This is run AFTER updating and clamping the index
    public void recalculateData(int roomSizeX, int roomSizeZ, int index, Direction facing) {
        this.index = index;
        this.facing = facing;

        if (facing.getAxis() == Direction.Axis.X) {
            int maxX = roomSizeX - 1;
            this.x = facing == Direction.EAST ? maxX : 0;
            this.z = index;
        } else {
            int maxZ = roomSizeZ - 1;
            this.x = index;
            this.z = facing == Direction.SOUTH ? maxZ : 0;
        }
    }

    public void afterResizingRoom(int roomSizeX, int roomSizeZ) {
        this.x = MathHelper.clamp(this.x, 0, roomSizeX - 1);
        this.z = MathHelper.clamp(this.z, 0, roomSizeZ - 1);
        this.index = this.facing.getAxis() == Direction.Axis.X ? this.z : this.x;
    }

    public float minX() {
        return this.facing == Direction.EAST ? (float) this.x - thickness + 1f : this.x;
    }

    public float minZ() {
        return this.facing == Direction.SOUTH ? (float) this.z - thickness + 1f : this.z;
    }

    public float sizeX() {
        return this.facing.getAxis() == Direction.Axis.X ? thickness : 1.0f;
    }

    public float sizeZ() {
        return this.facing.getAxis() == Direction.Axis.Z ? thickness : 1.0f;
    }

    public int getIndex() {
        return this.index;
    }

    public Direction getFacing() {
        return this.facing;
    }

    public Door build() {
        return new Door(new Vec2b(this.x, this.z), MazeUtil.ordinal(this.facing));
    }

    public static DoorBuilder fromBuiltDoor(Door door) {
        DoorBuilder builder = new DoorBuilder();
        builder.x = door.relativePos().x();
        builder.z = door.relativePos().y();
        builder.facing = MazeComponent.dirId(door.facing());
        builder.index = builder.facing.getAxis() == Direction.Axis.X ? builder.z : builder.x;
        return builder;
    }
}
