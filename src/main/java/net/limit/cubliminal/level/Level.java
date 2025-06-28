package net.limit.cubliminal.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Level {
    public static Codec<Level> LEVEL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("world_height", 256).forGetter(level -> level.world_height),
            Codec.INT.optionalFieldOf("min_y", 0).forGetter(level -> level.min_y),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("max_layer_count", 0).forGetter(level -> level.layer_count),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("layer_height", 16).forGetter(level -> level.layer_height),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("spacing_x", 16).forGetter(level -> level.spacing_x),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("spacing_z", 16).forGetter(level -> level.spacing_z)
    ).apply(instance, instance.stable(Level::new)));

    public final int world_height;
    public final int min_y;
    public final int layer_height;
    public final int layer_count;
    public final int spacing_x;
    public final int spacing_z;

    public Level(int world_height, int min_y, int max_layer_count, int layer_height, int spacing_x, int spacing_z) {
        if (world_height % 16 == 0) {
            this.world_height = world_height;
        } else {
            throw new IllegalStateException("World height must be a multiple of 16");
        }
        if (min_y % 16 == 0) {
            this.min_y = min_y;
        } else {
            throw new IllegalStateException("Min Y must be a multiple of 16");
        }
        if (layer_height <= 0) {
            throw new IllegalStateException("Layer height should always be greater than 0");
        } else {
            this.layer_height = layer_height;
        }
        if (max_layer_count < 0) {
            throw new IllegalStateException("Max layer count should always be positive");
        } else {
            this.layer_count = this.calcLayerCount(Math.abs(this.min_y) + this.world_height, max_layer_count);
        }
        if (spacing_x <= 0) {
            throw new IllegalStateException("Spacing X should always be greater than 0");
        } else {
            this.spacing_x = spacing_x;
        }
        if (spacing_z <= 0) {
            throw new IllegalStateException("Spacing Z should always be greater than 0");
        } else {
            this.spacing_z = spacing_z;
        }
    }

    protected int calcLayerCount(int verticalRange, int maxLayerCount) {
        for (int layers = verticalRange / this.layer_height; layers >= 0; layers--) {
            if (layers <= maxLayerCount) {
                return layers;
            }
        }
        return 0;
    }
}
