package com.rookiedev.microwavetools.libs;

/**
 * Represents a substrate model with properties for permittivity (epsilon) and height.
 */
public class SubstrateModel {
    private double subEpsilon, subHeight; // n/a, meter

    /**
     * Default constructor for SubstrateModel.
     */
    public SubstrateModel() {
    }

    /**
     * Gets the permittivity (epsilon) of the substrate.
     * 
     * @return the permittivity of the substrate.
     */
    public double getSubEpsilon() {
        return subEpsilon;
    }

    /**
     * Sets the permittivity (epsilon) of the substrate.
     * 
     * @param subEpsilon the permittivity to set.
     */
    public void setSubEpsilon(double subEpsilon) {
        this.subEpsilon = subEpsilon;
    }

    /**
     * Gets the height of the substrate.
     * 
     * @return the height of the substrate.
     */
    public double getSubHeight() {
        return subHeight;
    }

    /**
     * Sets the height of the substrate.
     * 
     * @param subHeight the height to set.
     */
    public void setSubHeight(double subHeight) {
        this.subHeight = subHeight;
    }
}
