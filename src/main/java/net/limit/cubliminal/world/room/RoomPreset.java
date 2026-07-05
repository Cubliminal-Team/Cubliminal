package net.limit.cubliminal.world.room;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.util.WeightedHolderSet;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.NoSuchFileException;
import java.util.Optional;

public record RoomPreset(float spacing, WeightedHolderSet<Room> holder) {

    private static final Codec<WeightedHolderSet<String>> RAW_SET_CODEC = WeightedHolderSet.createCodec(Codec.STRING, "name");

    public static final Codec<Pair<Float, WeightedHolderSet<String>>> CODEC = Codec.pair(
            Codec.floatRange(0.0f, Float.MAX_VALUE).optionalFieldOf("spacing", 1.0f).codec(),
            RAW_SET_CODEC.fieldOf("rooms").codec()
    );

    public static RoomPreset parse(ResourceManager resourceManager, float spacing, WeightedHolderSet<String> resourceIds) {
        return new RoomPreset(spacing, resourceIds.map(resourceId -> {
            Identifier path = Cubliminal.id("worldgen/room/rooms/" + resourceId + ".json");
            Optional<Resource> resourceOptional = resourceManager.getResource(path);
            if (resourceOptional.isPresent()) {
                Resource resource = resourceOptional.get();
                try (Reader reader = resource.getReader()) {
                    DataResult<Room> decodedRoom = Room.CODEC.parse(JsonOps.INSTANCE, JsonHelper.deserialize(reader));
                    return decodedRoom.getOrThrow();
                } catch (IOException e) {
                    Cubliminal.LOGGER.error("Couldn't decode room definition in {}", path);
                }
            } else {
                Cubliminal.LOGGER.error("Couldn't find room json file in {}", path);
                throw new RuntimeException(new NoSuchFileException(resourceId));
            }

            return null;
        }));
    }
}
