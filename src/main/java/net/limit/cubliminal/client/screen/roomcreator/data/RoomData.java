package net.limit.cubliminal.client.screen.roomcreator.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class RoomData {
    private final List<FloorData> floors = new ArrayList<>();
    private String name = "";
    private int boundsX = 16;
    private int boundsY = 8;
    private int boundsZ = 16;

    public List<FloorData> getFloors() { return this.floors; }
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public void setBoundingBox(int x, int y, int z) {
        this.boundsX = x;
        this.boundsY = y;
        this.boundsZ = z;
    }

    public FloorData addFloor() {
        FloorData floor = new FloorData();
        this.floors.add(floor);
        return floor;
    }

    public void removeFloor(FloorData floor) {
        this.floors.remove(floor);
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("name", this.name);

        JsonObject bounds = new JsonObject();
        bounds.addProperty("x", this.boundsX);
        bounds.addProperty("y", this.boundsY);
        bounds.addProperty("z", this.boundsZ);
        root.add("bounds", bounds);

        JsonArray floorsArray = new JsonArray();
        for (FloorData floor : this.floors) {
            JsonArray componentsArray = new JsonArray();
            for (ComponentData component : floor.getComponents()) {
                JsonObject componentJson = new JsonObject();
                componentJson.addProperty("relX", component.getRelX());
                componentJson.addProperty("relZ", component.getRelZ());
                componentJson.addProperty("width", component.getWidth());
                componentJson.addProperty("length", component.getLength());

                JsonArray templates = new JsonArray();
                component.getTemplateNames().forEach(templates::add);
                componentJson.add("templates", templates);

                componentsArray.add(componentJson);
            }
            JsonObject floorJson = new JsonObject();
            floorJson.add("components", componentsArray);
            floorsArray.add(floorJson);
        }
        root.add("floors", floorsArray);

        return root;
    }
}