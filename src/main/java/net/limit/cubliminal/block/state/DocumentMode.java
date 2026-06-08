package net.limit.cubliminal.block.state;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum DocumentMode implements StringIdentifiable {

    TEXT("text"),
    IMAGE("image");

    public static final Codec<DocumentMode> CODEC = StringIdentifiable.createCodec(DocumentMode::values);

    final String asString;

    DocumentMode(String asString) {
        this.asString = asString;
    }

    @Override
    public String asString() {
        return asString;
    }
}
