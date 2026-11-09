package net.limit.cubliminal.access;

import net.limit.cubliminal.event.blackout.BlackoutManager;
import net.limit.cubliminal.event.entity.smiler.SmilerEventHandler;
import org.jetbrains.annotations.Nullable;

public interface ServerWorldAccessor {
    @Nullable BlackoutManager blackoutManager();
    @Nullable SmilerEventHandler smilerEventHandler();
}
