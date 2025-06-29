package net.limit.cubliminal.world.room;

import net.limit.cubliminal.util.MazeUtil;
import net.limit.cubliminal.util.Vec2b;
import net.ludocrypt.limlib.api.world.maze.MazeComponent;

public record Door(Vec2b relativePos, byte facing) {

    public Door transform(PosTransformation translation, RotTransformation rotation) {
        return new Door(translation.translate(relativePos), MazeUtil.ordinal(rotation.rotate(MazeUtil.byId(facing))));
    }

    public record Instance(MazeComponent.Vec2i roomPos, MazeComponent.Face facing) {
        public static Instance of(MazeComponent.Vec2i roomPos, byte facing) {
            return new Instance(roomPos, MazeUtil.getById(facing));
        }

        public MazeComponent.Vec2i entry(int x, int y) {
            return (new MazeComponent.Vec2i(x, y)).go(facing);
        }
    }
}
