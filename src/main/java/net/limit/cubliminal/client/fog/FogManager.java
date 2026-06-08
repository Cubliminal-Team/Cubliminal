package net.limit.cubliminal.client.fog;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import org.joml.Vector2f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public class FogManager {
    public static FogManager INSTANCE = new FogManager();

    private float time = Float.MAX_VALUE;
    private final Vector2f prevEndings = new Vector2f();
    private FogSettings prevSettings = null;
    private FogSettings targetSettings = FogSettings.DUMMY;

    public Fog tick(FogSettings newSettings, Vector4f color, float tickDelta) {
        // If the biome has changed, update the fog manager
        if (restart(newSettings)) {
            update(newSettings);
        }
        // If the fog isn't as it should depending on the biome, perform a tick with a smoothstep function
        if (canContinue()) {
            time += (1 + tickDelta);

            float t = time / targetSettings.fadeTicks();
            prevEndings.set(
                    smoothStep(prevSettings.fogStart(), targetSettings.fogStart(), t),
                    smoothStep(prevSettings.fogEnd(), targetSettings.fogEnd(), t));

            return new Fog(prevEndings.x(), prevEndings.y(), FogShape.SPHERE, color.x, color.y, color.z, color.w);
        }
        // Else return directly the determined fog
        return new Fog(targetSettings.fogStart(), targetSettings.fogEnd(), FogShape.SPHERE, color.x, color.y, color.z, color.w);
    }


    public boolean restart(FogSettings newSettings) {
        return !targetSettings.equals(newSettings);
    }

    public boolean canContinue() {
        return time < targetSettings.fadeTicks();
    }

    public void update(FogSettings newSettings) {
        if (targetSettings != FogSettings.DUMMY) {
            prevSettings = canContinue() ? new FogSettings(prevEndings.x(), prevEndings.y(), prevSettings.fadeTicks()) : targetSettings;
            time = 0;
        }
        targetSettings = newSettings;
    }

    public static float smoothStep(float a, float b, float t) {
        return a + (b - a) * t*t * (3 - 2*t);
    }
}
