package com.rookiedev.microwavetools.libs;


public class CpwModel {
    private SubstrateModel substrate;
    private MetalModel metal;
    private double metalSpace;
    private double impedance, electricalLength; // ohms, degree
    private double frequency; // Hz

    public CpwModel() {
        substrate = new SubstrateModel();
        metal = new MetalModel();
    }

    void setSynthesizeParameter(double para, int flag) {
        switch (flag) {
            case Constant.Synthesize_Width:
                metal.setMetalWidth(para);
                break;

            case Constant.Synthesize_Height:
                substrate.setSubHeight(para);
                break;

            case Constant.Synthesize_Er:
                substrate.setSubEpsilon(para);
                break;

            case Constant.Synthesize_Length:
                metal.setMetalLength(para);
                break;

            default:
                break;
        }
    }

    public void setMetalWidth(double width, int unit) {
        metal.setMetalWidth(Constant.value2meter(width, unit));
    }

    public double getMetalWidth() {
        return metal.getMetalWidth();
    }

    public void setMetalLength(double length, int unit) {
        metal.setMetalLength(Constant.value2meter(length, unit));
    }

    public double getMetalLength() {
        return metal.getMetalLength();
    }

    public void setMetalThick(double thick, int unit) {
        metal.setMetalThick(Constant.value2meter(thick, unit));
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

    public double getSubHeight() {
        return substrate.getSubHeight();
    }

    public void setSubHeight(double subHeight, int unit) {
        substrate.setSubHeight(Constant.value2meter(subHeight, unit));
    }

    public SubstrateModel getSubstrate() {
        return substrate;
    }

    public void setSubstrate(SubstrateModel substrate) {
        this.substrate = substrate;
    }

    public MetalModel getMetal() {
        return metal;
    }

    public void setMetal(MetalModel metal) {
        this.metal = metal;
    }

    public double getMetalSpace() {
        return metalSpace;
    }

    public void setMetalSpace(double metalSpace, int unit) {
        this.metalSpace = Constant.value2meter(metalSpace, unit);
    }
}
