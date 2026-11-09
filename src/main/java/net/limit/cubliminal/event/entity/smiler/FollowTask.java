package net.limit.cubliminal.event.entity.smiler;

public class FollowTask extends SmilerTask {

    public static final String ID = "Follow";

    @Override
    public float getChanceToBeChosen() {
        return 0.3f;
    }

    @Override
    public int getMinTicksToAllow() {
        return 6000;
    }

    @Override
    public int getDurationTicks() {
        return 400;
    }

    @Override
    public String toString() {
        return ID;
    }
}
