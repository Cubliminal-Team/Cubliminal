package net.limit.cubliminal.client.screen.roomcreator.data;

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

}
