package net.limit.cubliminal.client.screen.roomcreator.data;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.world.room.CompositeRoom;
import net.limit.cubliminal.world.room.ConnectingRoom;
import net.limit.cubliminal.world.room.Door;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FloorBuilder {
    private final List<ComponentBuilder> components = new ArrayList<>();
    private final List<DoorBuilder> doors = new ArrayList<>();

    public List<ComponentBuilder> getComponents() { return this.components; }

    public List<DoorBuilder> getDoors() { return this.doors; }

    public ComponentBuilder addComponent() {
        ComponentBuilder component = new ComponentBuilder();
        this.components.add(component);
        return component;
    }

    public void addComponent(CompositeRoom.Component component, List<Identifier> modTemplates) {
        this.components.add(ComponentBuilder.fromBuiltComponent(component, modTemplates));
    }

    public void removeComponent(ComponentBuilder component) {
        this.components.remove(component);
    }

    public DoorBuilder addDoor() {
        DoorBuilder door = new DoorBuilder();
        this.doors.add(door);
        return door;
    }

    public void addDoor(Door door) {
        this.doors.add(DoorBuilder.fromBuiltDoor(door));
    }

    public void removeDoor(DoorBuilder door) {
        this.doors.remove(door);
    }

    public void afterResizingRoom(int roomSizeX, int roomSizeZ) {
        this.doors.forEach(door -> door.afterResizingRoom(roomSizeX, roomSizeZ));
    }

    public List<Door> buildDoors() {
        return this.doors.stream().map(DoorBuilder::build).distinct().toList();
    }

    @Nullable
    public ConnectingRoom.FloorData build() {
        List<CompositeRoom.Component> builtComponents = new ArrayList<>();
        for (ComponentBuilder component : this.components) {
            if (!component.getTemplates().getValues().isEmpty()) {
                builtComponents.add(component.build());
            }
        }

        return builtComponents.isEmpty() ? null : new ConnectingRoom.FloorData(builtComponents, this.buildDoors());
    }

    public static FloorBuilder fromBuiltFloor(List<CompositeRoom.Component> components, List<Door> doors) {
        FloorBuilder builder = new FloorBuilder();
        List<Identifier> modTemplates = new ArrayList<>();
        RoomCreatorDataManager.INSTANCE.getStructureTemplates().forEach(pair -> {
            if (pair.getFirst().getNamespace().equals(Cubliminal.MOD_ID)) modTemplates.add(pair.getFirst());
        });
        components.forEach(component -> builder.addComponent(component, modTemplates));
        doors.forEach(builder::addDoor);
        return builder;
    }
}
