package ifs.flat;

import java.util.Random;

public final class OneDBuffer {

    float vals[];

    public OneDBuffer(){
        vals = new float[1024];
        init();
    }

    public void init(){ //random brownian line
        int seed = -1;
        Random rnd = new Random();
        if(seed>=0){
            rnd.setSeed(seed);
        }
        float max = 0;
        for(int i=1; i<vals.length; i++){
            vals[i]=vals[i-1]+(float)rnd.nextGaussian();
        }

        for(int i=1; i<vals.length; i++){ //skewing it so it ends up at zero
            //vals[i]=vals[i]-vals[vals.length-1]*i/vals.length;

            max = Math.max(Math.abs(vals[i]), max);
        }

        //System.out.println("1d inited w max " + max);
    }

    public void smooth(){

        float[] vals2 = new float[1024];

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

        for(int i=1; i<vals.length; i++){ //skewing it so it ends up at zero
           // vals[i]=vals[i]-vals[vals.length-1]*i/vals.length;
        }
    }

    public float valueAt(float x){ //x = 0 to 1
        float decX = x - (float)Math.floor(x);
        return (1-decX)*vals[(int)(x*vals.length)] + decX*vals[1+(int)(x*vals.length)];
    }
}
