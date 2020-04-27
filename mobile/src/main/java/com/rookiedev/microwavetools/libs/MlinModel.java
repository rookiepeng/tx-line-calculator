package com.rookiedev.microwavetools.libs;

public class MlinModel {
    private SubstrateModel substrate;
    private MetalModel metal;
    private double impedance, phase; // ohms, degree
    private double frequency; // Hz
    private Constants.ERROR errorCode;

    public MlinModel() {
        substrate = new SubstrateModel();
        metal = new MetalModel();
    }

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

        default:
            break;
        }
    }

    public void setMetalWidth(double width, String unit) {
        metal.setMetalWidth(Constants.value2meter(width, unit));
    }

    public double getMetalWidth() {
        return metal.getMetalWidth();
    }

    public void setMetalLength(double length, String unit) {
        metal.setMetalLength(Constants.value2meter(length, unit));
    }

    public double getMetalLength() {
        return metal.getMetalLength();
    }

    public void setMetalThick(double thick, String unit) {
        metal.setMetalThick(Constants.value2meter(thick, unit));
    }

    double getMetalThick() {
        return metal.getMetalThick();
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

    public void setPhase(double electricalLength) {
        this.phase = electricalLength;
    }

    public void setFrequency(double frequency, String unit) {
        this.frequency = Constants.value2Hz(frequency, unit);
    }

    public double getFrequency() {
        return frequency;
    }

    double getSubEpsilon() {
        return substrate.getSubEpsilon();
    }

    public void setSubEpsilon(double subEpsilon) {
        substrate.setSubEpsilon(subEpsilon);
    }

    public double getSubHeight() {
        return substrate.getSubHeight();
    }

    public void setSubHeight(double subHeight, String unit) {
        substrate.setSubHeight(Constants.value2meter(subHeight, unit));
    }

    public Constants.ERROR getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Constants.ERROR errorCode) {
        this.errorCode = errorCode;
    }
}
