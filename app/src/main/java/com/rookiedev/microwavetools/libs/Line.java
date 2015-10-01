package com.rookiedev.microwavetools.libs;

/**
 * Created by rookie on 9/30/2015.
 */
public class Line {
    private double metalWidth, metalLength, metalThick;
    private double subEpsilon, subHeight;
    private double Impedance, ElectricalLength;
    private double Frequency;
    private int lineType;
    public Line(int type){
        lineType=type;
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

    public int getLineType(){
        return lineType;
    }
}
