package ifs;

import ifs.flat.OneDBuffer;

import java.awt.*;

final class RenderParams implements java.io.Serializable {

    int screenwidth;
    int screenheight;

    int dotSize;

    Color bgColor;

    boolean shapeVibrating=false;

    boolean twoD;
    boolean threeD;

    boolean framesHidden;
    boolean infoHidden;
    boolean usePDFSamples;
    boolean guidesHidden;
    int samplesPerFrame;
    int iterations;

    double brightnessMultiplier;

    boolean holdFrame;

    boolean usingGaussian;
    int potentialRadius;

    boolean renderThrottling;
    int shutterPeriod;

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

    boolean noDark;
    boolean gradientColors;
    boolean rightEye;

    ScoreParams scoreParams;

    int dotsPerPDF;

    float evolveIntensity;
    float evolveAnimationPeriod;

    OneDBuffer odbScale = new OneDBuffer();
    OneDBuffer odbRotationRoll = new OneDBuffer();
    OneDBuffer odbX = new OneDBuffer();
    OneDBuffer odbY = new OneDBuffer();
    OneDBuffer odbZ = new OneDBuffer();
    public RenderParams(){

        twoD=false;
        threeD=true;

        dotsPerPDF = 128;

        odbScale = new OneDBuffer();
        odbRotationRoll = new OneDBuffer();
        odbX = new OneDBuffer();
        odbY = new OneDBuffer();
        odbZ = new OneDBuffer();
        rightEye=true;
        bgColor = new Color(0,112/2,184/2); //half darkened spanish blue
        scoreParams = new ScoreParams(ScoreParams.Presets.MIN_DistSurface);

        useShadows=false;
        savedDots=0;
        savingDots=false;
        saveInterval=2000;
        usingColors=true;

        noDark = false;

        linesHideTime=1000;

        evolveIntensity = 100f;
        evolveAnimationPeriod = 41f;

        dotSize=0;
        smearPDF = false;

        drawGrid = true;
        gridRedrawTime=10;
        gridDrawTime=0;

        screenwidth = 1024;
        screenheight = 1024;

        xMin=0; yMin=0; zMin=0;
        xMax=1024; yMax=1024; zMax=1024;

        gradientColors=false;

        framesHidden = true;
        infoHidden = false;
        usePDFSamples = true;
        guidesHidden = false;
        iterations = 1;
        brightnessMultiplier = 2;
        holdFrame=false;
        samplesPerFrame = 104;

        usingGaussian =false;
        potentialRadius=0;

        renderThrottling=false;
        shutterPeriod = 60;
    }
}
