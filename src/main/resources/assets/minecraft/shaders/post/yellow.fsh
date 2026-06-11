#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

#define ONE_OVER_MAX_BRIGHTNESS_3 0.577350269
#define ONE_OVER_MAX_BRIGHTNESS_2 0.707106781

void main() {
    vec4 color = texture(InSampler, texCoord);

    // 1. Get luminance to drive the intensity map
    float luminance = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));

    // 2. Heavy-handed intensity curve.
    // This creates a wide, powerful mask that peaks quickly in lit areas.
    float effectStrength = pow(luminance, 0.5); // 0.5 aggressively boosts the midtones

    // 3. Process the channels individually to craft the precise hue.
    // We heavily boost Red, moderately boost Green to shape it into Yellow,
    // and slightly suppress Blue to kill the cold tones in lit areas.
    vec3 finalRGB;
    finalRGB.r = pow(color.r, mix(1.0, 0.65, effectStrength)) + (0.05 * effectStrength);
    finalRGB.g = pow(color.g, mix(1.0, 0.78, effectStrength));
    finalRGB.b = pow(color.b, mix(1.0, 1.20, effectStrength)); // Pushes blue down in bright spots

    // 4. Global ambient warmth injection (Simulating the fluorescent hum)
    // This gently lifts the lower-midtones into the yellow spectrum.
    vec3 ambientHum = vec3(0.04, 0.03, -0.02) * effectStrength;
    finalRGB += ambientHum;

    // 5. Safety clamp
    finalRGB = clamp(finalRGB, 0.0, 1.0);

    fragColor = vec4(finalRGB, color.a);
}