import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class RenderParams implements java.io.Serializable {

    int screenwidth;
    int screenheight;

    int dotSize;

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

    boolean drawGrid;
    long gridDrawTime;
    long gridRedrawTime;

    public RenderParams(){

        dotSize=0;
        smearPDF = false;

        drawGrid = true;
        gridRedrawTime=10;
        gridDrawTime=0;

        screenwidth = 1024;
        screenheight = 1024;

        framesHidden = true;
        infoHidden = false;
        usePDFSamples = true;
        guidesHidden = false;
        iterations = 1;
        brightnessMultiplier = 1;
        holdFrame=false;
        samplesPerFrame = 1024;

        usingGaussian =false;
        potentialRadius=0;

        renderThrottling=false;
        postProcessPeriod=100;
    }

    public void limitParams(){
        if(brightnessMultiplier <-16){
            brightnessMultiplier =-16;}
        if(brightnessMultiplier >16){
            brightnessMultiplier =16;}

        if(samplesPerFrame <2){
            samplesPerFrame =2;}
        if(samplesPerFrame >1310720){
            samplesPerFrame =1310720;}
    }
}
