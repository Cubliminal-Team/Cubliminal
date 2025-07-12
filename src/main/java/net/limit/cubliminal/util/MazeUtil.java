package net.limit.cubliminal.util;

import net.ludocrypt.limlib.api.world.maze.MazeComponent;
import net.ludocrypt.limlib.api.world.maze.MazeComponent.Face;
import net.minecraft.util.math.Direction;

public class MazeUtil {

    public static Direction getDirection(char direction) {
        return switch (direction) {
            case 'e' -> Direction.EAST;
            case 'w' -> Direction.WEST;
            case 'n' -> Direction.NORTH;
            default -> Direction.SOUTH;
        };
    }

    public static Face getFace(char direction) {
        return switch (direction) {
            case 'e' -> Face.UP;
            case 'w' -> Face.DOWN;
            case 'n' -> Face.LEFT;
            default -> Face.RIGHT;
        };
    }

    public static String rotString(Face face) {
        return switch (face) {
            case UP -> "e";
            case DOWN -> "w";
            case LEFT -> "n";
            case RIGHT -> "s";
        };
    }

    // Direction utils
    public static byte ordinal(Direction direction) {
        return (byte) ("ewns".indexOf(direction.getName().charAt(0)));
    }

    public static boolean fits(MazeComponent.Vec2i vec, int mazeWidth, int mazeHeight) {
        return vec.x() >= 0 && vec.x() < mazeWidth && vec.y() >= 0 && vec.y() < mazeHeight;
    }
}
