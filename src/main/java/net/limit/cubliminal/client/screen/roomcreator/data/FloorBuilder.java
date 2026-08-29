package net.limit.cubliminal.client.screen.roomcreator.data;

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

    public void removeComponent(ComponentBuilder component) {
        this.components.remove(component);
    }

    public DoorBuilder addDoor() {
        DoorBuilder door = new DoorBuilder();
        this.doors.add(door);
        return door;
    }

    public void removeDoor(DoorBuilder door) {
        this.doors.remove(door);
    }
}
