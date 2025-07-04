package net.limit.cubliminal.util;

import com.mojang.serialization.Codec;
import net.ludocrypt.limlib.api.world.maze.MazeComponent.Face;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.List;
import java.util.Objects;

public record Vec2b(byte x, byte y) {
    public static final Codec<Vec2b> CODEC = Codec.BYTE.listOf().comapFlatMap(list -> Util
                    .decodeFixedLengthList(list, 2)
                    .map(coords -> new Vec2b(coords.get(0), coords.get(1))),
            vec -> List.of(vec.x(), vec.y()));

    public Vec2b(int x, int y) {
        this((byte) x, (byte) y);
    }

    public static Vec2b ZERO = new Vec2b(0, 0);

    public Vec2b(Vec3i pos) {
        this(pos.getX(), pos.getZ());
    }

    public BlockPos toBlock() {
        return new BlockPos(x, 0, y);
    }

    public Vec2b add(int x, int y) {
        return new Vec2b(this.x + x, this.y + y);
    }

    public Vec2b add(Vec2b vec) {
        return new Vec2b(x + vec.x(), y + vec.y());
    }

    public Vec2b sub(int x, int y) {
        return new Vec2b(this.x - x, this.y - y);
    }

    public Vec2b sub(Vec2b vec) {
        return new Vec2b(x - vec.x(), y - vec.y());
    }

    public Vec2b mult(int scalar) {
        return new Vec2b(x * scalar, y * scalar);
    }

    public double mag() {
        return Math.sqrt(x * x + y * y);
    }

    public double sqMag() {
        return x * x + y * y;
    }

    public Vec2b inv() {
        return new Vec2b(-x, -y);
    }

    public Vec2b swap() {
        return new Vec2b(y, x);
    }

    public double dot(Vec2b vec) {
        return x * vec.x() + y * vec.y();
    }

    public double cross(Vec2b vec) {
        return y * vec.x() - x * vec.y();
    }

    public double dist(Vec2b vec) {
        return this.sub(vec).mag();
    }

    public double sqDist(Vec2b vec) {
        return this.sub(vec).sqMag();
    }

    public Vec2b up() {
        return new Vec2b(x + 1, y);
    }

    public Vec2b down() {
        return new Vec2b(x - 1, y);
    }

    public Vec2b left() {
        return new Vec2b(x, y - 1);
    }

    public Vec2b right() {
        return new Vec2b(x, y + 1);
    }

    public Vec2b up(int d) {
        return new Vec2b(x + d, y);
    }

    public Vec2b down(int d) {
        return new Vec2b(x - d, y);
    }

    public Vec2b left(int d) {
        return new Vec2b(x, y - d);
    }

    public Vec2b right(int d) {
        return new Vec2b(x, y + d);
    }

    public Vec2b go(Face face) {
        return switch (face) {
            case DOWN -> this.down();
            case LEFT -> this.left();
            case RIGHT -> this.right();
            case UP -> this.up();
        };
    }

    public Vec2b go(Face face, int d) {
        return switch (face) {
            case DOWN -> this.down(d);
            case LEFT -> this.left(d);
            case RIGHT -> this.right(d);
            case UP -> this.up(d);
        };
    }

    public Face normal(Vec2b b) {
        if (b.equals(this.up())) {
            return Face.UP;
        } else if (b.equals(this.left())) {
            return Face.LEFT;
        } else if (b.equals(this.right())) {
            return Face.RIGHT;
        } else if (b.equals(this.down())) {
            return Face.DOWN;
        }
        throw new IllegalArgumentException("Cannot find the normal between two non-adjacent vectors");
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
}
