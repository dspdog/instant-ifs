package ifs;

import java.awt.*;

public class RenderParams implements java.io.Serializable {

    int screenwidth;
    int screenheight;

    int dotSize;

    Color bgColor;

    boolean framesHidden;
    boolean infoHidden;
    boolean usePDFSamples;
    boolean guidesHidden;
    double samplesPerFrame;
    int iterations;

    double brightnessMultiplier;

    boolean holdFrame;

    boolean usingGaussian;
    int potentialRadius;

    boolean renderThrottling;
    long postProcessPeriod;

    boolean smearPDF;

    boolean useShadows;

    boolean drawGrid;
    long gridDrawTime;
    long gridRedrawTime;

    long linesHideTime;

    boolean usingColors;

    boolean savingDots;
    int saveInterval;
    long savedDots;
    String savedString="";

    int xMin, xMax, yMin, yMax, zMin, zMax;

    ScoreParams scoreParams;

    float evolveIntensity;

    public RenderParams(){
        bgColor = new Color(0,112/2,184/2); //half darkened spanish blue
        scoreParams = new ScoreParams(ScoreParams.Presets.MIN_DistSurface);

        useShadows=false;
        savedDots=0;
        savingDots=false;
        saveInterval=2000;
        usingColors=true;

        linesHideTime=1000;

        evolveIntensity = 0.4f;

        dotSize=0;
        smearPDF = false;

        drawGrid = true;
        gridRedrawTime=10;
        gridDrawTime=0;

        screenwidth = 1024;
        screenheight = 1024;

        xMin=0; yMin=0; zMin=0;
        xMax=1024; yMax=1024; zMax=1024;

        framesHidden = true;
        infoHidden = false;
        usePDFSamples = true;
        guidesHidden = false;
        iterations = 1;
        brightnessMultiplier = 2;
        holdFrame=false;
        samplesPerFrame = 1024;

        usingGaussian =false;
        potentialRadius=0;

        renderThrottling=false;
        postProcessPeriod=100;
    }

    public void limitParams(){
        //if(brightnessMultiplier <-16){
        //    brightnessMultiplier =-16;}
        //if(brightnessMultiplier >16){
        //    brightnessMultiplier =16;}

        if(samplesPerFrame <2){
            samplesPerFrame =2;}
        if(samplesPerFrame >1310720){
            samplesPerFrame =1310720;}
        if(xMin>xMax){
            xMax=xMin+1;
        }
        if(yMin>yMax){
            yMax=yMin+1;
        }
        if(zMin>zMax){
            zMax=zMin+1;
        }
    }
}
