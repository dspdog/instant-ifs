import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class RenderParams implements java.io.Serializable {

    int screenwidth;
    int screenheight;

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

    long gridDrawTime;
    long gridRedrawTime;

    public RenderParams(){

        gridRedrawTime=0;
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

    public void saveToFile(){
        try
        {
            FileOutputStream fileOut =
                    new FileOutputStream("renderparams.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);

            out.close();
            fileOut.close();
            System.out.println("saved to renderparams.ser");
        }catch(Exception i)
        {
            i.printStackTrace();
        }
    }

    public RenderParams loadFromFile(){
        RenderParams loadedShape=null;
        try
        {
            FileInputStream fileIn = new FileInputStream("renderparams.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            loadedShape = (RenderParams) in.readObject();
            in.close();
            fileIn.close();
            System.out.println("loaded renderparams.ser");
        }catch(Exception i)
        {
            i.printStackTrace();
        }

        return loadedShape;
    }
}
