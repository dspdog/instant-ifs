package ifs.flat;

import java.io.Serializable;
import java.util.Random;

public final class OneDBuffer implements Serializable{

    float vals[];
    float intensity;
    int so=0;
    static int entropy=0;

    public OneDBuffer(int seedOffset){
        entropy +=12345;
        intensity=1.0f;
        vals = new float[1028];
        so=seedOffset;
        init();
    }

    public void init(){ //random brownian line
        long seed = System.currentTimeMillis()+so+ entropy;
        Random rnd = new Random();
        if(seed>=0){
            rnd.setSeed(seed);
        }
        for(int i=1; i<vals.length; i++){
            vals[i]=vals[i-1]+(float)rnd.nextGaussian()*intensity;
        }
    }

    public void setIntensity(float i){
        intensity=i;
        init();
    }

    public void smooth(){

        float[] vals2 = new float[vals.length];

        for(int i=1; i<vals.length; i++){
            vals2[i] = (vals[i]+vals[i-1])/2;
        }

        for(int i=0; i<vals.length; i++){
            vals[i] = vals2[i];
        }
    }

    public void add(OneDBuffer od, float scaleDown){
        for(int i=0; i<vals.length; i++){
            vals[i]+=od.vals[i]/scaleDown;
        }
    }

    public void deSkew(){
        for(int i=1; i<vals.length; i++){ //skewing it so it ends up at zero
             vals[i]=vals[i]-vals[vals.length-1]*i/vals.length;
        }
    }

    public float valueAt(float x){ //x = 0 to 1
        float decX = (float)(x*vals.length - Math.floor(x*vals.length));
        return (1f-decX)*vals[(int)(x*vals.length)%vals.length]+(decX)*vals[(int)((x*vals.length)+1)%vals.length];
    }
}
