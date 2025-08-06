package net.limit.cubliminal.access;

import net.limit.cubliminal.event.backrooms.BlackoutManager;
import org.jetbrains.annotations.Nullable;

public interface ServerWorldAccessor {
    @Nullable BlackoutManager blackoutManager();
}
