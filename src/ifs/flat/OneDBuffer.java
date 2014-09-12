package ifs.flat;

import java.io.Serializable;
import java.util.Random;

public final class OneDBuffer implements Serializable{

    float vals[];
    float intensity;
    int so=0;
    static int entropy=0;
    int segs=0;
    long mySeed=0;

    public OneDBuffer(int seedOffset, int smooth, long seed){
        //entropy +=12345;
        segs=2000;
        mySeed=seed;
        intensity=1.0f;
        vals = new float[segs];
        so=seedOffset;
        init();
    }

    public void init(){ //random brownian line
        long seed = mySeed+so+entropy;
        Random rnd = new Random();
        if(seed>=0){
            rnd.setSeed(seed);
        }
        for(int i=1; i<segs; i++){
            vals[i]=vals[i-1]+(float)rnd.nextGaussian()*intensity*5;
        }
    }

    public void setIntensity(float i){
        intensity=i;
        init();
    }

    public void smooth(){

        float[] vals2 = new float[vals.length];

        for(int i=1; i<segs; i++){
            vals2[i] = (vals[i]+vals[i-1])/2;
        }

        for(int i=0; i<segs; i++){
            vals[i] = vals2[i];
        }
    }

    public void add(OneDBuffer od, float scaleDown){
        for(int i=0; i<segs; i++){
            vals[i]+=od.vals[i]/scaleDown;
        }
    }

    public void deSkew(){
        for(int i=1; i<segs; i++){ //skewing it so it ends up at zero
             vals[i]=vals[i]-vals[segs-1]*i/segs;
        }
    }

    public float valueAt(float x){ //x = 0 to 1
        float decX = (float)(x*segs - Math.floor(x*segs));
        return (1f-decX)*vals[(int)(x*segs)%segs]+(decX)*vals[(int)((x*segs)+1)%segs];
    }
}
