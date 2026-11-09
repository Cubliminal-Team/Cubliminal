package net.limit.cubliminal.event.entity.smiler;

public class ChaseTask extends SmilerTask {

    public static final String ID = "Chase";

    @Override
    public float getChanceToBeChosen() {
        return 0f;
    }

    @Override
    public int getMinTicksToAllow() {
        return 9600;
    }

    @Override
    public int getDurationTicks() {
        return 600;
    }

    @Override
    public String toString() {
        return ID;
    }
}
