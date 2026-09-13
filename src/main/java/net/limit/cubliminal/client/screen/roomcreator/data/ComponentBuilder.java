package net.limit.cubliminal.client.screen.roomcreator.data;

import com.mojang.datafixers.util.Pair;
import net.limit.cubliminal.util.Vec2b;
import net.limit.cubliminal.util.WeightedHolderSet;
import net.limit.cubliminal.world.room.CompositeRoom;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

    public void setWeight(String templateName, float newWeight) {
        ListIterator<Pair<Float, String>> listIterator = this.templates.getValues().listIterator();
        while (listIterator.hasNext()) {
            Pair<Float, String> pair = listIterator.next();
            if (pair.getSecond().equals(templateName)) {
                listIterator.set(Pair.of(newWeight, templateName));
            }
        }
    }

    public CompositeRoom.Component build() {
        return new CompositeRoom.Component(
                new Vec2b(this.relX, this.relZ),
                (byte) this.sizeX,
                (byte) this.sizeZ,
                this.templates.map(fullName -> {
                    String[] splitName = fullName.split("/", 4);
                    return splitName.length == 4 ? splitName[2] + ":" + splitName[3] : "";
                })
        );
    }

    public static ComponentBuilder fromBuiltComponent(CompositeRoom.Component component, List<Identifier> modTemplates) {
        ComponentBuilder builder = new ComponentBuilder();
        builder.setSizeX(component.sizeX());
        builder.setSizeZ(component.sizeZ());
        builder.setRelX(component.pos().x());
        builder.setRelZ(component.pos().y());
        builder.getTemplates().replace(findTemplateNames(component.templates().getValues(), modTemplates));
        return builder;
    }

    private static List<Pair<Float, String>> findTemplateNames(List<Pair<Float, String>> reducedNames, List<Identifier> modTemplates) {
        List<Pair<Float, String>> fullNames = new ArrayList<>();
        for (var pair : reducedNames) {
            String pathName = pair.getSecond().replaceFirst(":", "/");
            for (Identifier modTemplate : modTemplates) {
                String testedTemplate = modTemplate.toString();
                if (testedTemplate.endsWith(pathName)) {
                    fullNames.add(Pair.of(pair.getFirst(), testedTemplate));
                    break;
                }
            }
        }
        return fullNames;
    }
}