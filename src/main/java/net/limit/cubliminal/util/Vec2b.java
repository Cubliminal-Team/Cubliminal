package net.limit.cubliminal.util;

import com.mojang.serialization.Codec;
import net.ludocrypt.limlib.api.world.maze.MazeComponent.Face;
import net.ludocrypt.limlib.api.world.maze.MazeComponent.Vec2i;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Objects;

public record Vec2b(byte x, byte y) {
    public static final Codec<Vec2b> CODEC = Codec.BYTE.listOf().comapFlatMap(
            list -> Util.decodeFixedLengthList(list, 2).map(coords -> new Vec2b(coords.get(0), coords.get(1))),
            vec -> List.of(vec.x(), vec.y()));

    public static Vec2b ZERO = new Vec2b((byte) 0, (byte) 0);

    public BlockPos toBlock() {
        return new BlockPos(x, 0, y);
    }

    public Vec2b add(byte x, byte y) {
        return new Vec2b((byte) (this.x + x), (byte) (this.y + y));
    }

    public Vec2b up() {
        return up((byte) 1);
    }

    public Vec2b down() {
        return down((byte) 1);
    }

    public Vec2b left() {
        return left((byte) 1);
    }

    public Vec2b right() {
        return right((byte) 1);
    }

    public Vec2b up(byte d) {
        return add(d, (byte) 0);
    }

    public Vec2b down(byte d) {
        return add((byte) -d, (byte) 0);
    }

    public Vec2b left(byte d) {
        return add((byte) 0, (byte) -d);
    }

    public Vec2b right(byte d) {
        return add((byte) 0, d);
    }

    public Vec2b go(Face face) {
        return go(face, (byte) 1);
    }

    public Vec2b go(Face face, byte d) {
        return switch (face) {
            case DOWN -> this.down(d);
            case LEFT -> this.left(d);
            case RIGHT -> this.right(d);
            case UP -> this.up(d);
        };
    }

    public Vec2b invert() {
        return new Vec2b(this.y, this.x);
    }

    public Face normal(Vec2b b) {
        if (b.bEquals(this.up())) {
            return Face.UP;
        } else if (b.bEquals(this.left())) {
            return Face.LEFT;
        } else if (b.bEquals(this.right())) {
            return Face.RIGHT;
        } else if (b.bEquals(this.down())) {
            return Face.DOWN;
        }
        throw new IllegalArgumentException("Cannot find the normal between two non-adjacent vectors");
    }

    public Vec2i iAdd(int x, int y) {
        return new Vec2i(this.x + x, this.y + y);
    }

    public Vec2i iUp() {
        return iUp(1);
    }

    public Vec2i iDown() {
        return iDown(1);
    }

    public Vec2i iLeft() {
        return iLeft(1);
    }

    public Vec2i iRight() {
        return iRight(1);
    }

    public Vec2i iUp(int d) {
        return iAdd(d, 0);
    }

    public Vec2i iDown(int d) {
        return iAdd( -d, 0);
    }

    public Vec2i iLeft(int d) {
        return iAdd(0, -d);
    }

    public Vec2i iRight(int d) {
        return iAdd(0, d);
    }

    public Vec2i iGo(Face face) {
        return iGo(face, 1);
    }

    public Vec2i iGo(Face face, int d) {
        return switch (face) {
            case DOWN -> this.iDown(d);
            case LEFT -> this.iLeft(d);
            case RIGHT -> this.iRight(d);
            case UP -> this.iUp(d);
        };
    }

    public Face iNormal(Vec2i b) {
        if (b.equals(this.iUp())) {
            return Face.UP;
        } else if (b.equals(this.iLeft())) {
            return Face.LEFT;
        } else if (b.equals(this.iRight())) {
            return Face.RIGHT;
        } else if (b.equals(this.iDown())) {
            return Face.DOWN;
        }
        throw new IllegalArgumentException("Cannot find the normal between two non-adjacent vectors");
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vec2b vec) {
            return this.bEquals(vec);
        } else if (obj instanceof Vec2i vec) {
            return this.iEquals(vec);
        }
        return Objects.equals(this, obj);
    }

    public boolean bEquals(Vec2b vec) {
        return vec.x == this.x && vec.y == this.y;
    }

    public boolean iEquals(Vec2i vec) {
        return vec.x() == this.x && vec.y() == this.y;
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
}
