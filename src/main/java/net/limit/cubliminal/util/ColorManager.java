package net.limit.cubliminal.util;

public class ColorManager {
    private final int hexColor;
    private int red;
    private int green;
    private int blue;

    /**
     * Creates a new color management object.
     * @param hexColor The hex color to use
     */
    public ColorManager(int hexColor){
        this.hexColor = hexColor;
        this.calculateRGB();
    }

    /**
     * Converts hex color to a more readable RGB format.
     */
    private void calculateRGB(){
        this.red = (this.hexColor >> 16) & 0xFF;
        this.green = (this.hexColor >> 8) & 0xFF;
        this.blue = this.hexColor & 0xFF;
    }

    /**
     * Gets the hex color
     * @return Hex color
     */
    public int getHexColor(){
        return this.hexColor;
    }

    /**
     * Returns how much of a red variant.
     * @return Returns a float of the red variant
     */
    public float getRedDecimal(){
        return this.red / 250.0F;
    }

    /**
     * Returns how much of a green variant.
     * @return Returns a float of the green variant
     */
    public float getGreenDecimal(){
        return this.green / 250.0F;
    }

    /**
     * Returns how much of a blue variant.
     * @return Returns a float of the blue variant
     */
    public float getBlueDecimal(){
        return this.blue / 250.0F;
    }
}
