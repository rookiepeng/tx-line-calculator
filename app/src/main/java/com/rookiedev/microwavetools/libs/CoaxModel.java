package com.rookiedev.microwavetools.libs;

public class CoaxModel {
    private SubstrateModel substrate;
    private double impedance, electricalLength; // ohms, degree
    private double frequency; // Hz
    private double coreRadius, coreOffset, metalLength;

    public CoaxModel() {
        substrate = new SubstrateModel();
    }

    void setSynthesizeParameter(double para, int flag) {
        switch (flag) {

            case Constant.Synthesize_Height:
                substrate.setSubHeight(para);
                break;

            case Constant.Synthesize_Er:
                substrate.setSubEpsilon(para);
                break;

            case Constant.Synthesize_CoreRadius:
                coreRadius = para;
                break;

            case Constant.Synthesize_CoreOffset:
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

    public double getElectricalLength() {
        return electricalLength;
    }

    public void setElectricalLength(double electricalLength) {
        this.electricalLength = electricalLength;
    }

    public void setFrequency(double frequency, int unit) {
        this.frequency = Constant.value2Hz(frequency, unit);
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
        substrate.setSubHeight(Constant.value2meter(subHeight, unit));
    }

    public double getCoreRadius() {
        return coreRadius;
    }

    public void setCoreRadius(double coreRadius, int unit) {
        this.coreRadius = Constant.value2meter(coreRadius, unit);
    }

    public double getCoreOffset() {
        return coreOffset;
    }

    public void setCoreOffset(double coreOffset, int unit) {
        this.coreOffset = Constant.value2meter(coreOffset, unit);
    }

    public double getMetalLength() {
        return metalLength;
    }

    public void setMetalLength(double metalLength, int unit) {
        this.metalLength = Constant.value2meter(metalLength, unit);
    }
}
