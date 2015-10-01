package com.rookiedev.microwavetools.libs;

/**
 * Created by rookie on 9/30/2015.
 */
public class Line {
    private double metalWidth, metalLength, metalThick; // meter, meter, meter
    private double subEpsilon, subHeight; // n/a, meter
    private double Impedance, ElectricalLength; // ohms, degree
    private double Frequency; // Hz
    private double Delay; // s
    /* open end length correction */
    private double Deltal;
    private double kEff, loss, lossLen, skinDepth;
    private double alphaC, alphaD;

    /* incremental circuit model */
    private double Ls, Rs, Cs, Gs;

    /* the actual characteristic impedance is Ro + j Xo */
    private double Ro, Xo;

    private double rho,rough,tand;

    private int lineType;

    public Line(int type){
        lineType=type;
    }

    public int getLineType(){
        return lineType;
    }

    public void setMetalWidth(double width){
        metalWidth=width;
    }

    public double getMetalWidth(){
        return metalWidth;
    }

    public void setMetalLength(double length){
        metalLength=length;
    }

    public double getMetalLength(){
        return metalLength;
    }

    public void setMetalThick(double thick){
        metalThick=thick;
    }

    public double getMetalThick(){
        return metalThick;
    }

    public void setSubEpsilon(double epsilon){
        subEpsilon=epsilon;
    }

    public double getSubEpsilon(){
        return subEpsilon;
    }

    public void setSubHeight(double height){
        subHeight=height;
    }

    public double getSubHeight(){
        return subHeight;
    }

    public void setImpedance(double impedance){
        Impedance=impedance;
    }

    public double getImpedance(){
        return Impedance;
    }

    public void setElectricalLength(double electricalLength){
        ElectricalLength=electricalLength;
    }

    public double getElectricalLength(){
        return ElectricalLength;
    }

    public void setFrequency(double frequency){
        Frequency=frequency;
    }

    public double getFrequency(){
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
}
