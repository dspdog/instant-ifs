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

    float jitter;

    double brightnessMultiplier;

    boolean holdFrame;

    boolean usingGaussian;
    int potentialRadius;

    boolean renderThrottling;
    int shutterPeriod;

    boolean smearPDF;

    boolean cartoonMode;

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
    boolean postProcess;

    ScoreParams scoreParams;

    int dotsPerPDF;

    int smearWobbleIntensity;
    int smearSmooth;
    long wobbleRandomSeed;
    float smearSize;

    float evolveIntensity;
    float evolveAnimationPeriod;
    float evolveLockPeriod;

    int pruneThresh;

    long randomSeed;
    long randomScale;
    float perspectiveScale;

    OneDBuffer odbScale = new OneDBuffer(10, 1,1);
    OneDBuffer odbRotationRoll = new OneDBuffer(20,1,1);
    OneDBuffer odbX = new OneDBuffer(30,1,1);
    OneDBuffer odbY = new OneDBuffer(40,1,1);
    OneDBuffer odbZ = new OneDBuffer(50,1,1);
    public RenderParams(){
        pruneThresh=50;
        randomSeed=System.currentTimeMillis()%(65535);
        randomScale=5;
        perspectiveScale = 200;
        smearSmooth =512;
        smearSize=32;
        postProcess=true;

        smearWobbleIntensity =3;
        jitter=4;
        twoD=false;
        threeD=true;
        wobbleRandomSeed=57;
        dotsPerPDF = 256;

        odbScale = new OneDBuffer(10, smearSmooth,wobbleRandomSeed);
        odbRotationRoll = new OneDBuffer(20, smearSmooth,wobbleRandomSeed);
        odbX = new OneDBuffer(30, smearSmooth,wobbleRandomSeed);
        odbY = new OneDBuffer(40, smearSmooth,wobbleRandomSeed);
        odbZ = new OneDBuffer(50, smearSmooth,wobbleRandomSeed);
        rightEye=true;
        bgColor = new Color(0,112/2,184/2); //half darkened spanish blue
        scoreParams = new ScoreParams(ScoreParams.Presets.MIN_DistSurface);

        cartoonMode=true;
        savedDots=0;
        savingDots=false;
        saveInterval=2000;
        usingColors=true;

        noDark = false;

        linesHideTime=200;

        evolveIntensity = 2f;
        evolveAnimationPeriod = 0f;
        evolveLockPeriod = 1000f;

        dotSize=0;
        smearPDF = false;

        drawGrid = false;
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
        iterations = 3;
        brightnessMultiplier = 2;
        holdFrame=false;
        samplesPerFrame = 8;

        usingGaussian =false;
        potentialRadius=0;

        renderThrottling=false;
        shutterPeriod = 32;
    }
}
