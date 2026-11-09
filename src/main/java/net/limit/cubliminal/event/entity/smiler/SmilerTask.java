package net.limit.cubliminal.event.entity.smiler;

import net.limit.cubliminal.entity.hostile.SmilerEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class SmilerTask {

    @Nullable
    public static SmilerTask fromNbt(NbtCompound nbt) {
        String asString = nbt.getString("SmilerTask");
        return switch (asString) {
            case InvadeTask.ID -> new InvadeTask();
            case WatchTask.ID -> new WatchTask();
            case FollowTask.ID -> new FollowTask();
            case StareTask.ID -> new StareTask();
            case ChaseTask.ID -> new ChaseTask();
            default -> null;
        };
    }

    public abstract float getChanceToBeChosen();

    public abstract int getMinTicksToAllow();

    public abstract int getDurationTicks();

    public void begin() {}

    public void tick(SmilerEntity smiler) {}

    public Optional<SmilerTask> end() {
        return Optional.empty();
    }
}
