package com.rookiedev.microwavetools.libs;

public class CoaxModel {
    private SubstrateModel substrate;
    private double impedance, phase; // ohms, degree
    private double frequency; // Hz
    private double coreRadius, coreOffset, metalLength;
    private Constants.ERROR errorCode;

    /**
     * Constructor for CoaxModel.
     */
    public CoaxModel() {
        substrate = new SubstrateModel();
    }

    /**
     * Sets the synthesis parameter based on the provided flag.
     * 
     * @param para the parameter value
     * @param flag the flag indicating which parameter to set
     */
    void setSynthesizeParameter(double para, int flag) {
        switch (flag) {

        case Constants.Synthesize_Height:
            substrate.setSubHeight(para);
            break;

        case Constants.Synthesize_Er:
            substrate.setSubEpsilon(para);
            break;

        case Constants.Synthesize_CoreRadius:
            coreRadius = para;
            break;

        case Constants.Synthesize_CoreOffset:
            coreOffset = para;
            break;

        default:
            break;
        }
    }

    /**
     * Gets the impedance.
     * 
     * @return the impedance in ohms
     */
    public double getImpedance() {
        return impedance;
    }

    /**
     * Sets the impedance.
     * 
     * @param impedance the impedance in ohms
     */
    public void setImpedance(double impedance) {
        this.impedance = impedance;
    }

    /**
     * Gets the phase.
     * 
     * @return the phase in degrees
     */
    public double getPhase() {
        return phase;
    }

    /**
     * Sets the phase.
     * 
     * @param phase the phase in degrees
     */
    public void setPhase(double phase) {
        this.phase = phase;
    }

    /**
     * Sets the frequency.
     * 
     * @param frequency the frequency value
     * @param unit the unit of the frequency
     */
    public void setFrequency(double frequency, String unit) {
        this.frequency = Constants.value2Hz(frequency, unit);
    }

    /**
     * Gets the frequency.
     * 
     * @return the frequency in Hz
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * Gets the substrate epsilon.
     * 
     * @return the substrate epsilon
     */
    public double getSubEpsilon() {
        return substrate.getSubEpsilon();
    }

    /**
     * Sets the substrate epsilon.
     * 
     * @param subEpsilon the substrate epsilon
     */
    public void setSubEpsilon(double subEpsilon) {
        substrate.setSubEpsilon(subEpsilon);
    }

    /**
     * Gets the substrate radius.
     * 
     * @return the substrate radius
     */
    public double getSubRadius() {
        return substrate.getSubHeight();
    }

    /**
     * Sets the substrate radius.
     * 
     * @param subHeight the substrate height
     * @param unit the unit of the height
     */
    public void setSubRadius(double subHeight, String unit) {
        substrate.setSubHeight(Constants.value2meter(subHeight, unit));
    }

    /**
     * Gets the core radius.
     * 
     * @return the core radius
     */
    public double getCoreRadius() {
        return coreRadius;
    }

    /**
     * Sets the core radius.
     * 
     * @param coreRadius the core radius
     * @param unit the unit of the radius
     */
    public void setCoreRadius(double coreRadius, String unit) {
        this.coreRadius = Constants.value2meter(coreRadius, unit);
    }

    /**
     * Gets the core offset.
     * 
     * @return the core offset
     */
    public double getCoreOffset() {
        return coreOffset;
    }

    /**
     * Sets the core offset.
     * 
     * @param coreOffset the core offset
     * @param unit the unit of the offset
     */
    public void setCoreOffset(double coreOffset, String unit) {
        this.coreOffset = Constants.value2meter(coreOffset, unit);
    }

    /**
     * Gets the metal length.
     * 
     * @return the metal length
     */
    public double getMetalLength() {
        return metalLength;
    }

    /**
     * Sets the metal length.
     * 
     * @param metalLength the metal length
     * @param unit the unit of the length
     */
    public void setMetalLength(double metalLength, String unit) {
        this.metalLength = Constants.value2meter(metalLength, unit);
    }

    /**
     * Gets the error code.
     * 
     * @return the error code
     */
    public Constants.ERROR getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the error code.
     * 
     * @param errorCode the error code
     */
    public void setErrorCode(Constants.ERROR errorCode) {
        this.errorCode = errorCode;
    }
}
