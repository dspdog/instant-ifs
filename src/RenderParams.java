
public class RenderParams {

    boolean framesHidden;
    boolean infoHidden;
    boolean usePDFSamples;
    boolean guidesHidden;
    double samplesPerFrame;
    int iterations;

    double brightnessMultiplier;

    boolean holdFrame;

    boolean usingFindEdges;
    boolean usingThreshold;
    int threshold;

    boolean usingGaussian;
    int potentialRadius;

    boolean renderThrottling;
    long postProcessPeriod;

    public RenderParams(){
        framesHidden = true;
        infoHidden = false;
        usePDFSamples = true;
        guidesHidden = false;
        iterations = 1;
        brightnessMultiplier = 1;
        holdFrame=false;
        samplesPerFrame = 4096;
        usingThreshold = false;
        usingFindEdges = false;
        threshold = 64;
        usingGaussian =false;
        potentialRadius=0;

        renderThrottling=false;
        postProcessPeriod=1000;
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
