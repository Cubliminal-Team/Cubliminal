package net.limit.cubliminal.client.particle.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleColorManagement {
    public static final float[] ALMOND_WATER = registerColor(0.933333F, 0.86F, 0.77F);
    public static final float[] BLACK_SLUDGE = registerColor(0f, 0f, 0f);

    private static final List<float[]> contaminatedWaterColors = new ArrayList<>(List.of(
            new float[] {0.384f, 0.408f, 0.275f}, // Murky Green-Brown
            new float[] {0.659f, 0.353f, 0.173f}, // Rusty Orange
            new float[] {0.235f, 0.255f, 0.267f}, // Dark Grey
            new float[] {0.706f, 0.698f, 0.675f}, // Cloudy White-Grey
            new float[] {0.569f, 0.588f, 0.255f}, // Sickly Yellow-Green
            new float[] {0.157f, 0.235f, 0.412f}  // Oil-Slick Blue
    ));

    public static float[] registerColor(float red, float green, float blue){
        return new float[]{red, green, blue};
    }

    public static float[] chooseRandomContaminatedWaterColors(){
        Random random = new Random();
        int chosen = random.nextInt(0, contaminatedWaterColors.size());
        return contaminatedWaterColors.get(chosen);
    }
}
