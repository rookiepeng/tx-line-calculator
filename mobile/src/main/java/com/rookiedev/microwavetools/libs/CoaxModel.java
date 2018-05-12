package com.rookiedev.microwavetools.libs;

public class CoaxModel {
    private SubstrateModel substrate;
    private double impedance, phase; // ohms, degree
    private double frequency; // Hz
    private double coreRadius, coreOffset, metalLength;
    private Constants.ERROR errorCode;

    public CoaxModel() {
        substrate = new SubstrateModel();
    }

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

    public double getImpedance() {
        return impedance;
    }

    public void setImpedance(double impedance) {
        this.impedance = impedance;
    }

    public double getPhase() {
        return phase;
    }

    public void setPhase(double phase) {
        this.phase = phase;
    }

    public void setFrequency(double frequency, int unit) {
        this.frequency = Constants.value2Hz(frequency, unit);
    }

    public double getFrequency() {
        return frequency;
    }

    public double getSubEpsilon() {
        return substrate.getSubEpsilon();
    }

    public void setSubEpsilon(double subEpsilon) {
        substrate.setSubEpsilon(subEpsilon);
    }

    public double getSubRadius() {
        return substrate.getSubHeight();
    }

    public void setSubRadius(double subHeight, int unit) {
        substrate.setSubHeight(Constants.value2meter(subHeight, unit));
    }

    public double getCoreRadius() {
        return coreRadius;
    }

    public void setCoreRadius(double coreRadius, int unit) {
        this.coreRadius = Constants.value2meter(coreRadius, unit);
    }

    public double getCoreOffset() {
        return coreOffset;
    }

    public void setCoreOffset(double coreOffset, int unit) {
        this.coreOffset = Constants.value2meter(coreOffset, unit);
    }

    public double getMetalLength() {
        return metalLength;
    }

    public void setMetalLength(double metalLength, int unit) {
        this.metalLength = Constants.value2meter(metalLength, unit);
    }

    public Constants.ERROR getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Constants.ERROR errorCode) {
        this.errorCode = errorCode;
    }
}
