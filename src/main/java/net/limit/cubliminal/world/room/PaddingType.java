package net.limit.cubliminal.world.room;

import net.minecraft.util.InvalidIdentifierException;

public enum PaddingType {
    ABOVE("above", 1, 0),
    BELOW("below", 0, 1),
    ABOVE_AND_BELOW("above_and_below", 1, 1);

    final String asString;
    public final int above;
    public final int below;

    PaddingType(String name, int above, int below) {
        this.asString = name;
        this.above = above;
        this.below = below;
    }

    @Override
    public String toString() {
        return this.asString;
    }

    public String translatableEntry() {
        return "gui.cubliminal.padding_type." + this.toString();
    }

    public static PaddingType fromString(String name) {
        for (PaddingType paddingType : PaddingType.values()) {
            if (paddingType.asString.equals(name)) {
                return paddingType;
            }
        }

        throw new InvalidIdentifierException("Padding type: " + name + " doesn't exist");
    }
}
