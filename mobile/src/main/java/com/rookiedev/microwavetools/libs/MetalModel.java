package com.rookiedev.microwavetools.libs;

/**
 * Represents a metal model with width, length, and thickness.
 */
public class MetalModel {
    private double metalWidth, metalLength, metalThick; // meter, meter, meter

    /**
     * Default constructor for MetalModel.
     */
    public MetalModel() {
        // Default constructor
    }

    /**
     * Gets the width of the metal.
     * 
     * @return the width of the metal in meters.
     */
    public double getMetalWidth() {
        return metalWidth;
    }

    /**
     * Sets the width of the metal.
     * 
     * @param metalWidth the width of the metal in meters.
     */
    public void setMetalWidth(double metalWidth) {
        this.metalWidth = metalWidth;
    }

    /**
     * Gets the length of the metal.
     * 
     * @return the length of the metal in meters.
     */
    public double getMetalLength() {
        return metalLength;
    }

    /**
     * Sets the length of the metal.
     * 
     * @param metalLength the length of the metal in meters.
     */
    public void setMetalLength(double metalLength) {
        this.metalLength = metalLength;
    }

    /**
     * Gets the thickness of the metal.
     * 
     * @return the thickness of the metal in meters.
     */
    public double getMetalThick() {
        return metalThick;
    }

    /**
     * Sets the thickness of the metal.
     * 
     * @param metalThick the thickness of the metal in meters.
     */
    public void setMetalThick(double metalThick) {
        this.metalThick = metalThick;
    }
}
