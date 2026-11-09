package net.limit.cubliminal.event.entity.smiler;

public class InvadeTask extends SmilerTask {

    public static final String ID = "Invade";

    @Override
    public float getChanceToBeChosen() {
        return 0.2f;
    }

    @Override
    public int getMinTicksToAllow() {
        return 3600;
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
