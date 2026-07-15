package net.limit.cubliminal.world.room;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.limit.cubliminal.util.Vec2b;
import net.limit.cubliminal.util.WeightedHolderSet;
import net.limit.cubliminal.world.room.CompositeRoom.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class used to construct rooms out of individual templates.
 */
public class RoomBuilder {
    public final Int2ObjectOpenHashMap<FloorBuilder> floors = new Int2ObjectOpenHashMap<>();

    public FloorBuilder create(int floor) {
        FloorBuilder floorBuilder = new FloorBuilder();
        this.floors.put(floor, floorBuilder);
        return floorBuilder;
    }

    @Nullable
    public FloorBuilder getFloor(int floor) {
        return this.floors.getOrDefault(floor, null);
    }

    public static class FloorBuilder {
        public final List<ComponentBuilder> components;
        public final List<Door> doors;

        public FloorBuilder() {
            this.components = new ArrayList<>();
            this.doors = new ArrayList<>();
        }

        @Nullable
        public ComponentBuilder getComponent(Vec2b pos) {
            for (ComponentBuilder component : this.components) {
                if (component.pos.equals(pos)) {
                    return component;
                }
            }

            return null;
        }

        // Returns null if there's a suitable component for this template at the specified location, otherwise
        // return the freshly created component
        @Nullable
        public ComponentBuilder add(Vec2b pos, byte width, byte height, String templateName, float weight) {
            for (ComponentBuilder component : this.components) {
                if (component.pos.equals(pos) && component.width == width && component.height == height) {
                    component.add(weight, templateName);
                    return null;
                }
            }

            ComponentBuilder component = new ComponentBuilder(pos, width, height, templateName);
            this.components.add(component);
            return component;
        }

        public FloorBuilder add(Door door) {
            this.doors.add(door);
            return this;
        }

        public FloorBuilder remove(Door door) {
            this.doors.remove(door);
            return this;
        }
    }

    public static class ComponentBuilder {
        public Vec2b pos;
        public final byte width;
        public final byte height;
        public final WeightedHolderSet<String> structures;

        // A whole ComponentBuilder will always be initialized when adding a single template to the Room at the
        // position of the template to allow the addition of multiple weighted templates in that same position if wanted
        public ComponentBuilder(Vec2b pos, byte width, byte height, String templateName) {
            if (pos.x() < 0 || pos.y() < 0) {
                throw new IllegalArgumentException("Room's relative position X: " + pos.x() + " and Z: " + pos.y() + " must be 0 or positive");
            }
            this.pos = pos;
            if (width < 1 || height < 1) {
                throw new IllegalArgumentException("Room width: " + width + " and height: " + height + " must be set above 0");
            }
            this.width = width;
            this.height = height;
            this.structures = new WeightedHolderSet<>(List.of(Pair.of(WeightedHolderSet.validateWeight(1.0f), templateName)));
        }

        public ComponentBuilder add(float weight, String templateName) {
            this.structures.add(weight, templateName);
            return this;
        }

        public ComponentBuilder remove(String templateName) {
            this.structures.remove(templateName);
            return this;
        }

        public ComponentBuilder setPos(byte x, byte y) {
            this.pos = new Vec2b(x, y);
            return this;
        }

        public Component build() {
            return new Component(this.pos, this.width, this.height, this.structures);
        }
    }

}
