package net.limit.cubliminal.client.screen.roomcreator.data;

import java.util.ArrayList;
import java.util.List;

public class FloorData {
    private final List<ComponentData> components = new ArrayList<>();

    public List<ComponentData> getComponents() { return this.components; }

    public ComponentData addComponent() {
        ComponentData component = new ComponentData();
        this.components.add(component);
        return component;
    }

    public void removeComponent(ComponentData component) {
        this.components.remove(component);
    }
}