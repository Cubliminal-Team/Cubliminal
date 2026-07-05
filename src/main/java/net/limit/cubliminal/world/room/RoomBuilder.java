package net.limit.cubliminal.world.room;

import com.mojang.datafixers.util.Pair;
import net.limit.cubliminal.util.Vec2b;
import net.limit.cubliminal.util.WeightedHolderSet;
import net.limit.cubliminal.world.room.CompositeRoom.Component;

import java.util.List;

/**
 * A helper class used to construct rooms from individual templates.
 */
public class RoomBuilder {

    public class FloorBuilder {
        public List<ComponentBuilder> components;
        public List<Door> doors;
    }

    public class ComponentBuilder {
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
