package com.rookiedev.microwavetools.libs;

public class Line {
    public static final int MLIN = 1, CMLIN =2;
    public static final int SYN_W = 0, SYN_H = 1, SYN_Er = 2, SYN_L = 3;
    public static final int LUnitmil = 0, LUnitmm = 1, LUnitcm = 2, LUnitm = 3;
    public static final int FUnitMHz = 0, FUnitGHz = 1, FunitHz = 2;
    private double metalWidth, metalLength, metalThick, metalSpace; // meter, meter, meter, meter
    private double subEpsilon, subHeight; // n/a, meter
    private double Impedance, ElectricalLength; // ohms, degree
    private double Frequency; // Hz
    private double Delay; // s
    /* open end length correction */
    private double Deltal,deltalEven,deltalOdd;
    private double kEff, loss, lossEven, lossOdd, lossLen, skinDepth;
    private double alphaC, alphaD;
    private double lossLenEven, lossLenOdd;
    private double impedanceEven, impedanceOdd;
    private double kEven,kOdd,couplingFactor;

    /* incremental circuit model */
    private double Ls, Rs, Cs, Gs;
    private double LEven, LOdd, REven, ROdd, CEven, COdd, GEven, GOdd;

    /* the actual characteristic impedance is Ro + j Xo */
    private double Ro, Xo;

    private double rho, rough, tand;

    private int lineType;

    public Line(int type) {
        lineType = type;
    }

    public int getLineType() {
        return lineType;
    }

    public void setMetalWidth(double width, int unit) {
        metalWidth = para2m(width, unit);
    }

    public double getMetalWidth() {
        return metalWidth;
    }

    public void setMetalLength(double length, int unit) {
        metalLength = para2m(length, unit);
    }

    public double getMetalLength() {
        return metalLength;
    }

    public void setMetalThick(double thick, int unit) {
        metalThick = para2m(thick, unit);
    }

    public double getMetalThick() {
        return metalThick;
    }

    public void setSubEpsilon(double epsilon) {
        subEpsilon = epsilon;
    }

    public double getSubEpsilon() {
        return subEpsilon;
    }

    public void setSubHeight(double height, int unit) {
        subHeight = para2m(height, unit);
    }

    public double getSubHeight() {
        return subHeight;
    }

    public void setImpedance(double impedance) {
        Impedance = impedance;
    }

    public double getImpedance() {
        return Impedance;
    }

    public void setElectricalLength(double electricalLength) {
        ElectricalLength = electricalLength;
    }

    public double getElectricalLength() {
        return ElectricalLength;
    }

    public void setFrequency(double frequency, int unit) {
        Frequency = para2hz(frequency, unit);
    }

    public double getFrequency() {
        return Frequency;
    }

    public double getDelay() {
        return Delay;
    }

    public void setDelay(double delay) {
        Delay = delay;
    }

    public double getDeltal() {
        return Deltal;
    }

    public void setDeltal(double deltal) {
        Deltal = deltal;
    }

    public double getkEff() {
        return kEff;
    }

    public void setkEff(double kEff) {
        this.kEff = kEff;
    }

    public double getLoss() {
        return loss;
    }

    public void setLoss(double loss) {
        this.loss = loss;
    }

    public double getLossLen() {
        return lossLen;
    }

    public void setLossLen(double lossLen) {
        this.lossLen = lossLen;
    }

    public double getSkinDepth() {
        return skinDepth;
    }

    public void setSkinDepth(double skinDepth) {
        this.skinDepth = skinDepth;
    }

    public double getAlphaC() {
        return alphaC;
    }

    public void setAlphaC(double alphaC) {
        this.alphaC = alphaC;
    }

    public double getAlphaD() {
        return alphaD;
    }

    public void setAlphaD(double alphaD) {
        this.alphaD = alphaD;
    }

    public double getLs() {
        return Ls;
    }

    public void setLs(double ls) {
        Ls = ls;
    }

    public double getRs() {
        return Rs;
    }

    public void setRs(double rs) {
        Rs = rs;
    }

    public double getCs() {
        return Cs;
    }

    public void setCs(double cs) {
        Cs = cs;
    }

    public double getGs() {
        return Gs;
    }

    public void setGs(double gs) {
        Gs = gs;
    }

    public double getRo() {
        return Ro;
    }

    public void setRo(double ro) {
        Ro = ro;
    }

    public double getXo() {
        return Xo;
    }

    public void setXo(double xo) {
        Xo = xo;
    }

    public double getRho() {
        return rho;
    }

    public void setRho(double rho) {
        this.rho = rho;
    }

    public double getRough() {
        return rough;
    }

    public void setRough(double rough) {
        this.rough = rough;
    }

    public double getTand() {
        return tand;
    }

    public void setTand(double tand) {
        this.tand = tand;
    }

    public void setParameter(double para, int flag) {
        switch (flag) {
            case SYN_W:
                metalWidth = para;
                break;

            case SYN_H:
                subHeight = para;
                break;

            case SYN_Er:
                subEpsilon = para;
                break;

            case SYN_L:
                metalLength = para;
                break;

            default:
                break;
        }
    }

    private double para2m(double para, int unit) {
        double l = 0;
        switch (unit) {
            case LUnitmil:
                l = para / 39370.0787402;
                break;
            case LUnitmm:
                l = para / 1000;
                break;
            case LUnitcm:
                l = para / 100;
                break;
            case LUnitm:
                l = para;
                break;
        }
        return l;
    }

    private double para2hz(double para, int unit) {
        double l = 0;
        switch (unit) {
            case FUnitMHz:
                l = para * 1e6;
                break;
            case FUnitGHz:
                l = para * 1e9;
                break;
            case FunitHz:
                l = para;
                break;
        }
        return l;
    }

    public double getMetalSpace() {
        return metalSpace;
    }

    public void setMetalSpace(double metalSpace, int unit) {
        this.metalSpace = para2m(metalSpace, unit);
    }

    public double getLossLenEven() {
        return lossLenEven;
    }

    public void setLossLenEven(double lossLenEven) {
        this.lossLenEven = lossLenEven;
    }

    public double getLossLenOdd() {
        return lossLenOdd;
    }

    public void setLossLenOdd(double lossLenOdd) {
        this.lossLenOdd = lossLenOdd;
    }

    public double getImpedanceEven() {
        return impedanceEven;
    }

    public void setImpedanceEven(double impedanceEven) {
        this.impedanceEven = impedanceEven;
    }

    public double getImpedanceOdd() {
        return impedanceOdd;
    }

    public void setImpedanceOdd(double impedanceOdd) {
        this.impedanceOdd = impedanceOdd;
    }

    public double getkEven() {
        return kEven;
    }

    public void setkEven(double kEven) {
        this.kEven = kEven;
    }

    public double getkOdd() {
        return kOdd;
    }

    public void setkOdd(double kOdd) {
        this.kOdd = kOdd;
    }

    public double getDeltalEven() {
        return deltalEven;
    }

    public void setDeltalEven(double deltalEven) {
        this.deltalEven = deltalEven;
    }

    public double getDeltalOdd() {
        return deltalOdd;
    }

    public void setDeltalOdd(double deltalOdd) {
        this.deltalOdd = deltalOdd;
    }

    public double getLEven() {
        return LEven;
    }

    public void setLEven(double LEven) {
        this.LEven = LEven;
    }

    public double getLOdd() {
        return LOdd;
    }

    public void setLOdd(double LOdd) {
        this.LOdd = LOdd;
    }

    public double getREven() {
        return REven;
    }

    public void setREven(double REven) {
        this.REven = REven;
    }

    public double getROdd() {
        return ROdd;
    }

    public void setROdd(double ROdd) {
        this.ROdd = ROdd;
    }

    public double getCEven() {
        return CEven;
    }

    public void setCEven(double CEven) {
        this.CEven = CEven;
    }

    public double getCOdd() {
        return COdd;
    }

    public void setCOdd(double COdd) {
        this.COdd = COdd;
    }

    public double getGEven() {
        return GEven;
    }

    public void setGEven(double GEven) {
        this.GEven = GEven;
    }

    public double getGOdd() {
        return GOdd;
    }

    public void setGOdd(double GOdd) {
        this.GOdd = GOdd;
    }

    public double getLossEven() {
        return lossEven;
    }

    public void setLossEven(double lossEven) {
        this.lossEven = lossEven;
    }

    public double getLossOdd() {
        return lossOdd;
    }

    public void setLossOdd(double lossOdd) {
        this.lossOdd = lossOdd;
    }

    public double getCouplingFactor() {
        return couplingFactor;
    }

    public void setCouplingFactor(double couplingFactor) {
        this.couplingFactor = couplingFactor;
    }

}
