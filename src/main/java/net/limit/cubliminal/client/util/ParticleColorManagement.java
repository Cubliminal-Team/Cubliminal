package net.limit.cubliminal.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class ParticleColorManagement {
    // This color is used for the Almond water fluid.
    public static final float[] ALMOND_WATER = registerColor(0.933333F, 0.86F, 0.77F); // Light Warm Cream (#EEDBC4)
    // This color is used for the black sludge fluid.
    public static final float[] BLACK_SLUDGE = registerColor(0f, 0f, 0f); // Black (#000000)

    // This is a list of colors that are used for the contaminated water.
    private static final List<float[]> contaminatedWaterColors = new ArrayList<>(List.of(
            registerColor(0.384f, 0.408f, 0.275f), // Murky Green-Brown
            registerColor(0.659f, 0.353f, 0.173f), // Rusty Orange
            registerColor(0.235f, 0.255f, 0.267f), // Dark Grey
            registerColor(0.706f, 0.698f, 0.675f), // Cloudy White-Grey
            registerColor(0.569f, 0.588f, 0.255f), // Sickly Yellow-Green
            registerColor(0.157f, 0.235f, 0.412f)  // Oil-Slick Blue
    ));

    /**
     * Registers a rgb color
     * @param red The float value for red
     * @param green The float value for green
     * @param blue The float value for blue
     * @return A float array containing rgb values
     */
    public static float[] registerColor(float red, float green, float blue){
        return new float[]{red, green, blue};
    }

    /**
     * Chooses a random color that is used for the contaminated water fluid.
     * @return A float array containing rgb.
     */
    public static float[] chooseRandomContaminatedWaterColors(){
        // Init the random method.
        Random random = new Random();
        // Chooses a random integer from the list.
        int chosen = random.nextInt(0, contaminatedWaterColors.size());
        // Returns the picked color.
        return contaminatedWaterColors.get(chosen);
    }
}
