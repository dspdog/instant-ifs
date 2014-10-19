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

    public int[] getPixels(int width, int height, float xScale, float yScale){
        int[] pixels = new int[width*height];
        for(int x=0; x<width;x++){
            for(int y=0; y<height; y++){
                if(valueAt((float)xScale*x/width)*yScale+0.5f>(float)y/height){
                    pixels[x+y*width]=argb(255, 0, 0, 0);
                }else{
                    pixels[x+y*width]=argb(255, 255, 255, 255);
                }
            }
        }

        return pixels;
    }

    int argb(int a, int r, int g, int b){

        a=Math.max(1, Math.min(a, 255))  ;
        r=Math.max(1, Math.min(r, 128))  ;
        g=Math.max(1, Math.min(g, 64))  ;
        b=Math.max(1, Math.min(b, 255))  ;

        int _argb = a;
        _argb = (_argb << 8) + r;
        _argb = (_argb << 8) + g;
        _argb = (_argb << 8) + b;

        return _argb;
    }

    public OneDBuffer(int seedOffset, int smooth, long seed){
        //entropy +=12345;
        segs=100;
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

        System.arraycopy(vals2, 0, vals, 0, segs);
    }

    public void add(OneDBuffer od, float scaleDown){
        for(int i=0; i<segs; i++){
            vals[i]+=od.vals[i]/scaleDown;
        }
    }

    public void set(float what, float where){
        int index = (int)(where*segs)%segs;
        vals[index] =what;
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
