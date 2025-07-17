package net.limit.cubliminal.util;

public class ColorManager {
    private final int hexColor;
    private int red;
    private int green;
    private int blue;

    public ColorManager(int hexColor){
        this.hexColor = hexColor;
        this.calculateRGB();
    }

    private void calculateRGB(){
        this.red = (this.hexColor >> 16) & 0xFF;
        this.green = (this.hexColor >> 8) & 0xFF;
        this.blue = this.hexColor & 0xFF;
    }

    public int getHexColor(){
        return this.hexColor;
    }

    public float getRedDecimal(){
        return this.red / 250.0F;
    }

    public float getGreenDecimal(){
        return this.green / 250.0F;
    }

    public float getBlueDecimal(){
        return this.blue / 250.0F;
    }
}
