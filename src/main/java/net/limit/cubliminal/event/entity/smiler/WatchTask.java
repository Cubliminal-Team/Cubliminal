package net.limit.cubliminal.event.entity.smiler;

import net.limit.cubliminal.entity.hostile.SmilerEntity;

import java.util.Optional;

public class WatchTask extends SmilerTask {

    public static final String ID = "Watch";

    @Override
    public float getChanceToBeChosen() {
        return 0.3f;
    }

    @Override
    public int getMinTicksToAllow() {
        return 0;
    }

    @Override
    public int getDurationTicks() {
        return 400;
    }

    @Override
    public String toString() {
        return ID;
    }

    @Override
    public void begin() {
        System.out.println("Beginning Watch Task");
    }

    @Override
    public void tick(SmilerEntity smiler) {
        super.tick(smiler);
    }

    @Override
    public Optional<SmilerTask> end() {
        return super.end();
    }
}