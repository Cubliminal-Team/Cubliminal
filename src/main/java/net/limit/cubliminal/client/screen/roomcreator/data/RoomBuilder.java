package net.limit.cubliminal.client.screen.roomcreator.data;

import com.mojang.datafixers.util.Pair;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.level.Level;
import net.limit.cubliminal.networking.c2s.SaveRoomC2SPayload;
import net.limit.cubliminal.util.Vec2b;
import net.limit.cubliminal.util.WeightedHolderSet;
import net.limit.cubliminal.world.room.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class used to construct rooms out of individual templates.
 */
public class RoomBuilder {
    private final List<FloorBuilder> floors = new ArrayList<>();
    private String name = "";
    private int sizeX = 3;
    private int sizeZ = 3;
    private RoomType.Type roomType = RoomType.Type.SIMPLE;
    private PaddingType paddingType = PaddingType.ABOVE;
    private int paddingCount = 0;

    public FloorBuilder addFloor() {
        FloorBuilder floor = new FloorBuilder();
        this.floors.add(floor);
        return floor;
    }

    public void removeFloor(FloorBuilder floor) {
        this.floors.remove(floor);
    }

    public List<FloorBuilder> getFloors() {
        return this.floors;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSizeX() {
        return this.sizeX;
    }

    public int getSizeZ() {
        return this.sizeZ;
    }

    public void setSizeX(int size) {
        this.sizeX = Math.clamp(size, 1, Byte.MAX_VALUE);
    }

    public void setSizeZ(int size) {
        this.sizeZ = Math.clamp(size, 1, Byte.MAX_VALUE);
    }

    public RoomType.Type getRoomType() {
        return this.roomType;
    }

    public void setRoomType(RoomType.Type type) {
        this.roomType = type;
    }

    public PaddingType getPaddingType() {
        return this.paddingType;
    }

    public void setPaddingType(PaddingType type) {
        this.paddingType = type;
    }

    public int getPaddingCount() {
        return this.paddingCount;
    }

    public void setPaddingCount(int paddingCount) {
        this.paddingCount = paddingCount;
    }

    public static Text getUnableToConvertText(RoomType.Type type) {
        return Text
                .literal("Couldn't save the Room as a " + type.toString() + " Room Type")
                .formatted(Formatting.BOLD, Formatting.RED);
    }

    public List<Text> trySaveRoom(Level level) {
        return this.trySaveRoomInternal(level).stream().map(Text::of).toList();
    }

    private List<String> trySaveRoomInternal(Level level) {
        List<String> optionalFixes = new ArrayList<>();
        if (this.name.isBlank()) optionalFixes.add("Type a Room Name");

        if (roomType == RoomType.Type.CONNECTING) {
            if (paddingCount * (paddingType.above + paddingType.below) + this.floors.size() > level.layer_count) {
                optionalFixes.add("Remove Floors and Padding until they sum " + level.layer_count);
            }

            ConnectingRoom.FloorData[] builtFloors = new ConnectingRoom.FloorData[this.floors.size()];

            boolean emptyRoom = true;
            for (int floorIndex = 0; floorIndex < builtFloors.length; floorIndex++) {
                ConnectingRoom.FloorData builtFloor = this.floors.get(floorIndex).build();
                if (builtFloor == null) continue;
                builtFloors[floorIndex] = builtFloor;
                emptyRoom = false;
            }

            if (emptyRoom) {
                optionalFixes.add("Add at least one Component with a Template Name");
                return optionalFixes;
            }

            if (!optionalFixes.isEmpty()) return optionalFixes;

            SaveRoomC2SPayload.sendPacket(this.name, new ConnectingRoom(
                    builtFloors,
                    (byte) this.sizeX,
                    (byte) this.sizeZ,
                    (byte) paddingCount,
                    paddingType
            ));
        } else {
            if (this.floors.size() > 1) {
                optionalFixes.add("Leave just one Floor");
                return optionalFixes;
            }

            FloorBuilder floor = this.floors.getFirst();
            if (floor.getComponents().isEmpty()) {
                optionalFixes.add("Add at least one Component");
                return optionalFixes;
            }

            if (roomType == RoomType.Type.SIMPLE) {
                if (floor.getComponents().size() > 1) {
                    optionalFixes.add("Leave just one Component");
                    return optionalFixes;
                }

                ComponentBuilder component = floor.getComponents().getFirst();
                if (component.getSizeX() != this.sizeX || component.getSizeZ() != this.sizeZ || component.getRelX() != 0 || component.getRelZ() != 0) {
                    optionalFixes.add("Adjust the size and position of the Component to fill exactly the area of the Room");
                    return optionalFixes;
                }

                if (component.getTemplates().getValues().isEmpty()) {
                    optionalFixes.add("Add one Template Name to the Component");
                    return optionalFixes;
                } else if (component.getTemplates().size() > 1) {
                    optionalFixes.add("The Component must reference only one Template Name");
                    return optionalFixes;
                }

                if (!optionalFixes.isEmpty()) return optionalFixes;

                SaveRoomC2SPayload.sendPacket(this.name, new SimpleRoom(
                        component.build().templates().getValues().getFirst().getSecond(),
                        (byte) this.sizeX,
                        (byte) this.sizeZ,
                        floor.buildDoors()
                ));
            } else if (roomType == RoomType.Type.COMPOSITE) {
                ConnectingRoom.FloorData builtFloor = floor.build();

                if (builtFloor == null) {
                    optionalFixes.add("Add at least one Template Name to a Component");
                    return optionalFixes;
                }

                if (!optionalFixes.isEmpty()) return optionalFixes;

                SaveRoomC2SPayload.sendPacket(
                        this.name,
                        new CompositeRoom(builtFloor.components(), (byte) this.sizeX, (byte) this.sizeZ, builtFloor.doors())
                );
            }
        }

        return optionalFixes;
    }

    public static RoomBuilder fromBuiltRoom(Room room, String roomName) {
        RoomBuilder builder = new RoomBuilder();
        builder.setName(roomName);
        builder.setSizeX(room.getSizeX());
        builder.setSizeZ(room.getSizeZ());

        switch (room) {
            case SimpleRoom simpleRoom -> {
                builder.setRoomType(RoomType.Type.SIMPLE);
                CompositeRoom.Component component = new CompositeRoom.Component(
                        Vec2b.ZERO,
                        room.getSizeX(),
                        room.getSizeZ(),
                        new WeightedHolderSet<>(List.of(Pair.of(1.0f, simpleRoom.template())))
                );
                builder.addBuiltFloor(0, List.of(component), simpleRoom.doors());
            }
            case CompositeRoom compositeRoom -> {
                builder.setRoomType(RoomType.Type.COMPOSITE);
                builder.addBuiltFloor(0, compositeRoom.components(), compositeRoom.doors());
            }
            case ConnectingRoom connectingRoom -> {
                builder.setRoomType(RoomType.Type.CONNECTING);
                builder.setPaddingType(connectingRoom.paddingType());
                builder.setPaddingCount(connectingRoom.padding());
                for (int i = 0; i < connectingRoom.getFloors(); i++) {
                    ConnectingRoom.FloorData floor = connectingRoom.floorData()[i];
                    builder.addBuiltFloor(i, floor.components(), floor.doors());
                }
            }
            default -> Cubliminal.LOGGER.error("How did you come up with a new Room Type??");
        }

        return builder;
    }

    private void addBuiltFloor(int index, List<CompositeRoom.Component> components, List<Door> doors) {
        this.floors.add(index, FloorBuilder.fromBuiltFloor(components, doors));
    }

}
