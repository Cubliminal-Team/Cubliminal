package net.limit.cubliminal.util;

import com.mojang.serialization.Codec;
import net.ludocrypt.limlib.api.world.maze.MazeComponent.Face;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.List;
import java.util.Objects;

public record Vec3b(byte x, byte y, byte z) {
    public static final Codec<Vec3b> CODEC = Codec.BYTE.listOf().comapFlatMap(list -> Util
                    .decodeFixedLengthList(list, 3)
                    .map(coords -> new Vec3b(coords.get(0), coords.get(1), coords.get(2))),
            vec -> List.of(vec.x(), vec.y(), vec.z()));

    public Vec3b(int x, int y, int z) {
        this((byte) x, (byte) y, (byte) z);
    }

    public static Vec3b ZERO = new Vec3b(0, 0, 0);

    public Vec3b(Vec3i pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vec2b toVec2() {
        return new Vec2b(x, z);
    }

    public BlockPos toBlock() {
        return new BlockPos(x, y, z);
    }

    public Vec3b add(int x, int y, int z) {
        return new Vec3b(this.x + x, this.y + y, this.z + z);
    }

    public Vec3b add(Vec3b vec) {
        return new Vec3b(x + vec.x(), y + vec.y(), z + vec.z());
    }

    public Vec3b sub(int x, int y, int z) {
        return new Vec3b(this.x - x, this.y - y, this.z - z);
    }

    public Vec3b sub(Vec3b vec) {
        return new Vec3b(x - vec.x(), y - vec.y(), z - vec.z());
    }

    public Vec3b mult(int scalar) {
        return new Vec3b(x * scalar, y * scalar, z * scalar);
    }

    public double mag() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double sqMag() {
        return x * x + y * y + z * z;
    }

    public Vec3b inv() {
        return new Vec3b(-x, -y, -z);
    }

    public Vec3b swap() {
        return new Vec3b(z, y, x);
    }

    public double dot(Vec3b vec) {
        return x * vec.x() + y * vec.y() + z * vec.z();
    }

    public Vec3b cross(Vec3b vec) {
        return new Vec3b(y * vec.z() - z * vec.y(), z * vec.x() - x * vec.z(), x * vec.y() - y * vec.x());
    }

    public double dist(Vec3b vec) {
        return this.sub(vec).mag();
    }

    public double sqDist(Vec3b vec) {
        return this.sub(vec).sqMag();
    }

    public Vec3b up() {
        return new Vec3b(x, y + 1, z);
    }

    public Vec3b down() {
        return new Vec3b(x, y - 1, z);
    }

    public Vec3b east() {
        return new Vec3b(x + 1, y, z);
    }

    public Vec3b west() {
        return new Vec3b(x - 1, y, z);
    }
    
    public Vec3b south() {
        return new Vec3b(x, y, z + 1);
    }

    public Vec3b north() {
        return new Vec3b(x, y, z - 1);
    }

    public Vec3b up(int d) {
        return new Vec3b(x, y + d, z);
    }

    public Vec3b down(int d) {
        return new Vec3b(x, y - d, z);
    }

    public Vec3b east(int d) {
        return new Vec3b(x + d, y, z);
    }

    public Vec3b west(int d) {
        return new Vec3b(x - d, y, z);
    }

    public Vec3b south(int d) {
        return new Vec3b(x, y, z + d);
    }

    public Vec3b north(int d) {
        return new Vec3b(x, y, z - d);
    }

    public Vec3b go(Face face) {
        return switch (face) {
            case DOWN -> this.west();
            case LEFT -> this.north();
            case RIGHT -> this.south();
            case UP -> this.east();
        };
    }

    public Vec3b go(Face face, int d) {
        return switch (face) {
            case DOWN -> this.west(d);
            case LEFT -> this.north(d);
            case RIGHT -> this.south(d);
            case UP -> this.east(d);
        };
    }

    public Vec3b go(Direction dir) {
        return switch (dir) {
            case DOWN -> this.down();
            case UP -> this.up();
            case NORTH -> this.north();
            case SOUTH -> this.south();
            case WEST -> this.west();
            case EAST -> this.east();
        };
    }

    public Vec3b go(Direction dir, int d) {
        return switch (dir) {
            case DOWN -> this.down(d);
            case UP -> this.up(d);
            case NORTH -> this.north(d);
            case SOUTH -> this.south(d);
            case WEST -> this.west(d);
            case EAST -> this.east(d);
        };
    }

    public Direction normal(Vec3b b) {
        if (b.equals(this.up())) {
            return Direction.UP;
        } else if (b.equals(this.down())) {
            return Direction.DOWN;
        } else if (b.equals(this.east())) {
            return Direction.EAST;
        } else if (b.equals(this.west())) {
            return Direction.WEST;
        } else if (b.equals(this.south())) {
            return Direction.SOUTH;
        } else if (b.equals(this.north())) {
            return Direction.NORTH;
        }
        throw new IllegalArgumentException("Cannot find the normal between two non-adjacent vectors");
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }
}
