package net.limit.cubliminal.client.screen.roomcreator.data;

import net.limit.cubliminal.util.Vec2b;
import net.limit.cubliminal.util.WeightedHolderSet;
import net.limit.cubliminal.world.room.CompositeRoom;

public class ComponentBuilder {
    private int sizeX = 1;
    private int sizeZ = 1;
    private int relX = 0;
    private int relZ = 0;
    private final WeightedHolderSet<String> templates = new WeightedHolderSet<>(4);

    public void setSizeX(int size) {
        this.sizeX = Math.clamp(size, 1, Byte.MAX_VALUE);
    }

    public void setSizeZ(int size) {
        this.sizeZ = Math.clamp(size, 1, Byte.MAX_VALUE);
    }

    public void setRelX(int x) {
        this.relX = Math.clamp(x, 0, Byte.MAX_VALUE);
    }

    public void setRelZ(int z) {
        this.relZ = Math.clamp(z, 0, Byte.MAX_VALUE);
    }

    public int getSizeX() {
        return this.sizeX;
    }

    public int getSizeZ() {
        return this.sizeZ;
    }

    public int getRelX() {
        return this.relX;
    }

    public int getRelZ() {
        return this.relZ;
    }

    public ComponentBuilder add(String templateName) {
        this.templates.add(0.0f, templateName);
        return this;
    }

    public ComponentBuilder add(float weight, String templateName) {
        this.templates.add(weight, templateName);
        return this;
    }

    public ComponentBuilder remove(String templateName) {
        this.templates.remove(templateName);
        return this;
    }

    public WeightedHolderSet<String> getTemplates() {
        return this.templates;
    }

    public boolean hasNotTemplate(String templateName) {
        return this.templates.getValues().stream().noneMatch(pair -> pair.getSecond().equals(templateName));
    }

    public CompositeRoom.Component build() {
        return new CompositeRoom.Component(new Vec2b(this.relX, this.relZ), (byte) this.sizeX, (byte) this.sizeZ, this.templates);
    }
}