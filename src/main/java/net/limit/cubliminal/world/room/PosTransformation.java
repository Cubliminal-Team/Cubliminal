package net.limit.cubliminal.world.room;

import net.limit.cubliminal.util.Vec2b;

@FunctionalInterface
public interface PosTransformation {
    Vec2b translate(Vec2b position);
}