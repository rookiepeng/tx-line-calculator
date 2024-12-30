package com.rookiedev.microwavetools.libs;

/**
 * Represents a CPW (Coplanar Waveguide) model with various parameters.
 */
public class CpwModel {
    private SubstrateModel substrate;
    private MetalModel metal;
    private double metalSpace;
    private double impedance, phase; // ohms, degree
    private double frequency; // Hz
    private Constants.ERROR errorCode;

    /**
     * Default constructor initializing substrate and metal models.
     */
    public CpwModel() {
        substrate = new SubstrateModel();
        metal = new MetalModel();
    }

    /**
     * Sets the synthesize parameter based on the provided flag.
     *
     * @param para the parameter value
     * @param flag the flag indicating which parameter to set
     */
    void setSynthesizeParameter(double para, int flag) {
        switch (flag) {
        case Constants.Synthesize_Width:
            metal.setMetalWidth(para);
            break;

        case Constants.Synthesize_Height:
            substrate.setSubHeight(para);
            break;

        case Constants.Synthesize_Er:
            substrate.setSubEpsilon(para);
            break;

        case Constants.Synthesize_Length:
            metal.setMetalLength(para);
            break;

        case Constants.Synthesize_Gap:
            metalSpace = para;
            break;

        default:
            break;
        }
    }

    /**
     * Sets the metal width.
     *
     * @param width the width value
     * @param unit the unit of the width
     */
    public void setMetalWidth(double width, String unit) {
        metal.setMetalWidth(Constants.value2meter(width, unit));
    }

    /**
     * Gets the metal width.
     *
     * @return the metal width
     */
    public double getMetalWidth() {
        return metal.getMetalWidth();
    }

    /**
     * Sets the metal length.
     *
     * @param length the length value
     * @param unit the unit of the length
     */
    public void setMetalLength(double length, String unit) {
        metal.setMetalLength(Constants.value2meter(length, unit));
    }

    /**
     * Gets the metal length.
     *
     * @return the metal length
     */
    public double getMetalLength() {
        return metal.getMetalLength();
    }

    /**
     * Sets the metal thickness.
     *
     * @param thick the thickness value
     * @param unit the unit of the thickness
     */
    public void setMetalThick(double thick, String unit) {
        metal.setMetalThick(Constants.value2meter(thick, unit));
    }

    /**
     * Gets the metal thickness.
     *
     * @return the metal thickness
     */
    double getMetalThick() {
        return metal.getMetalThick();
    }

    /**
     * Gets the impedance.
     *
     * @return the impedance
     */
    public double getImpedance() {
        return impedance;
    }

    /**
     * Sets the impedance.
     *
     * @param impedance the impedance value
     */
    public void setImpedance(double impedance) {
        this.impedance = impedance;
    }

    /**
     * Gets the phase.
     *
     * @return the phase
     */
    public double getPhase() {
        return phase;
    }

    /**
     * Sets the phase.
     *
     * @param phase the phase value
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
     * @return the frequency
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
     * @param subEpsilon the substrate epsilon value
     */
    public void setSubEpsilon(double subEpsilon) {
        substrate.setSubEpsilon(subEpsilon);
    }

    /**
     * Gets the substrate height.
     *
     * @return the substrate height
     */
    public double getSubHeight() {
        return substrate.getSubHeight();
    }

    /**
     * Sets the substrate height.
     *
     * @param subHeight the substrate height value
     * @param unit the unit of the height
     */
    public void setSubHeight(double subHeight, String unit) {
        substrate.setSubHeight(Constants.value2meter(subHeight, unit));
    }

    /**
     * Gets the substrate model.
     *
     * @return the substrate model
     */
    public SubstrateModel getSubstrate() {
        return substrate;
    }

    /**
     * Sets the substrate model.
     *
     * @param substrate the substrate model
     */
    public void setSubstrate(SubstrateModel substrate) {
        this.substrate = substrate;
    }

    /**
     * Gets the metal model.
     *
     * @return the metal model
     */
    public MetalModel getMetal() {
        return metal;
    }

    /**
     * Sets the metal model.
     *
     * @param metal the metal model
     */
    public void setMetal(MetalModel metal) {
        this.metal = metal;
    }

    /**
     * Gets the metal space.
     *
     * @return the metal space
     */
    public double getMetalSpace() {
        return metalSpace;
    }

    /**
     * Sets the metal space.
     *
     * @param metalSpace the metal space value
     * @param unit the unit of the space
     */
    public void setMetalSpace(double metalSpace, String unit) {
        this.metalSpace = Constants.value2meter(metalSpace, unit);
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
