package net.limit.cubliminal.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.limit.cubliminal.block.state.DocumentMode;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record WrittenDocContentComponent(DocumentMode mode, Optional<RawFilteredPair<Text>> text,
                                         Optional<Identifier> texture, boolean resolved) {

    public static final Codec<RawFilteredPair<Text>> TEXT_CODEC = RawFilteredPair.createCodec(WrittenBookContentComponent.PAGE_CODEC);

    public static final Codec<WrittenDocContentComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DocumentMode.CODEC.fieldOf("mode").forGetter(WrittenDocContentComponent::mode),
            TEXT_CODEC.optionalFieldOf("text").forGetter(WrittenDocContentComponent::text),
            Identifier.CODEC.optionalFieldOf("texture").forGetter(WrittenDocContentComponent::texture),
            Codec.BOOL.optionalFieldOf("resolved", false).forGetter(WrittenDocContentComponent::resolved)
    ).apply(instance, WrittenDocContentComponent::new));

    public static WrittenDocContentComponent createEmpty(DocumentMode mode) {
        return new WrittenDocContentComponent(mode, Optional.empty(), Optional.empty(), true);
    }

    @Nullable
    public WrittenDocContentComponent resolve(ServerCommandSource source, @Nullable PlayerEntity player) {
        if (resolved || text.isEmpty()) {
            return null;
        } else {
            Optional<RawFilteredPair<Text>> optional = resolve(source, player, text.get());
            if (optional.isEmpty()) {
                return null;
            }

            return new WrittenDocContentComponent(mode, optional, texture, true);
        }
    }

    private static Optional<RawFilteredPair<Text>> resolve(ServerCommandSource source, @Nullable PlayerEntity player, RawFilteredPair<Text> page) {
        return page.resolve(text -> {
            try {
                Text text2 = Texts.parse(source, text, player, 0);
                return exceedsSerializedLengthLimit(text2, source.getRegistryManager()) ? Optional.empty() : Optional.of(text2);
            } catch (Exception var4) {
                return Optional.of(text);
            }
        });
    }

    public WrittenDocContentComponent asResolved() {
        return new WrittenDocContentComponent(mode, text, texture, true);
    }

    private static boolean exceedsSerializedLengthLimit(Text text, RegistryWrapper.WrapperLookup registries) {
        return Text.Serialization.toJsonString(text, registries).length() > 32767;
    }

    public Text getText(boolean shouldFilter) {
        return this.text.orElse(RawFilteredPair.of(Text.empty())).get(shouldFilter);
    }

    public boolean isEmpty() {
        return text.isEmpty() && texture().isEmpty();
    }

}
