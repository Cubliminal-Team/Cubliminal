package net.limit.cubliminal.client.screen.roomcreator.data;

import java.util.ArrayList;
import java.util.List;

public class ComponentData {
    private final List<String> templateNames = new ArrayList<>();
    private int relX;
    private int relZ;
    private int width = 1;  // footprint size along X
    private int length = 1; // footprint size along Z

    public List<String> getTemplateNames() { return this.templateNames; }
    public int getRelX() { return this.relX; }
    public int getRelZ() { return this.relZ; }
    public int getWidth() { return this.width; }
    public int getLength() { return this.length; }
    public void setRelX(int relX) { this.relX = relX; }
    public void setRelZ(int relZ) { this.relZ = relZ; }
    public void setWidth(int width) { this.width = width; }
    public void setLength(int length) { this.length = length; }
}