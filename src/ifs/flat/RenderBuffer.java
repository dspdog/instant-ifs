package ifs.flat;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

public final class RenderBuffer extends Kernel{

    public final long TBuffer[];
    public final float ZBuffer[];
    public final float SBuffer[];
    public final float RBuffer[];
    public final float GBuffer[];
    public final float BBuffer[];

    public final float postRBuffer[];
    public final float postGBuffer[];
    public final float postBBuffer[];

    public final float postZBuffer[];

    public final int pixels[];

    boolean cartoon=false;

    final boolean shading = true;

    float brightness = 1.0f;

    int width, height;

    float maxColor = 0;

    private static long time = System.currentTimeMillis();
    private static long frameStartTime = System.currentTimeMillis();

    public long frameNum=0;

    public static int shutterSpeed = 50;

    public int mode = 0; //z-process

    public float scaleUp =1.0f;

    public boolean addSamples=true;

    public RenderBuffer(int w, int h){
        width=w; height=h;
        RBuffer = new float[width*height];
        GBuffer = new float[width*height];
        BBuffer = new float[width*height];

        SBuffer = new float[width*height];
        ZBuffer = new float[width*height];
        TBuffer = new long[width*height];

        postZBuffer = new float[width*height];
        postRBuffer = new float[width*height];
        postGBuffer = new float[width*height];
        postBBuffer = new float[width*height];

        clearZProjection();
        cartoon=false;
        pixels=new int[width*height];
        addSamples=true;
        this.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
    }

    public void clearZProjection(){
        maxColor=0;
    }

    public void updateTime(long _time){
        time = System.currentTimeMillis();
        frameStartTime=_time;
    }

    public boolean putPixel(float x, float y, float z, float R, float G, float B, float scale, float dark, boolean rightEye){

        x=Math.max((int)x, 0);
        x=Math.min((int)x, width-1);
        y=Math.max((int)y, 0);
        y=Math.min((int)y, height-1);

        if(ZBuffer[(int)x+(int)y*width] < z){
            RBuffer[(int)x+(int)y*width] = R*dark;
            GBuffer[(int)x+(int)y*width] = G*dark;
            BBuffer[(int)x+(int)y*width] = B*dark;
            ZBuffer[(int)x+(int)y*width] = z;
            TBuffer[(int)x+(int)y*width] = time;
            SBuffer[(int)x+(int)y*width] = scale;
            return true;
        }else{
            return false;
        }
    }

    public void generatePixels(float _brightness, boolean useShadows, boolean rightEye, boolean _putSamples, float _size){
        cartoon=useShadows;
        addSamples=_putSamples;
        brightness=_brightness;
        scaleUp=_size;

        Range range = Range.create2D(width,height);

        //pass 1 Z-PROCESS
        //pass 2 GENERATE PIXELS
        this.execute(range, 2);

        frameNum++;

        if(frameNum%1000==0){
            System.out.println(this.getExecutionMode().toString() + " " + this.getExecutionTime());
        }
    }

    @Override
    public void run() {

        int x = getGlobalId(0);
        int y = getGlobalId(1);

        if(getPassId()==0){ //Z-PROCESS
            int edge = 32;
            if (x>edge && x<(width-edge) && y>edge && y<(height-edge)){
                if((TBuffer[x+y*width]<time-shutterSpeed) && TBuffer[x+y*width]<frameStartTime){
                    ZBuffer[x+y*width]=0;
                }

                if(ZBuffer[x+y*width]>0){
                    if(addSamples){
                        putThing(x,y);
                    }else{
                        postZBuffer[x+y*width]=(int)ZBuffer[x+y*width];
                        postRBuffer[x+y*width]=(int)RBuffer[x+y*width];
                        postGBuffer[x+y*width]=(int)GBuffer[x+y*width];
                        postBBuffer[x+y*width]=(int)BBuffer[x+y*width];
                    }
                }
            }
        }else if(getPassId() == 1){//GENERATE PIXELS
            float gradient=1.0f;

            float gms = getMaxSlope(x,y);
            float maxslope = min(gms*255f, 255f);

            if(cartoon){
                if(gms>1){
                    maxslope=254f;
                    if(postZBuffer[x+y*width]==0){
                        postZBuffer[x+y*width]=1;//outside edges
                    }
                }
            }

            gradient = shading ? 1.0f-maxslope/255.0f : 1.0f;

            if(postZBuffer[x+y*width]==0){
                int _argb = 255;

                _argb = (_argb << 8) + (int)(0);
                _argb = (_argb << 8) + (int)(89);
                _argb = (_argb << 8) + (int)(114);

                pixels[x+y*width]=_argb;
            }else{
                pixels[x+y*width]=getColor(x,y,gradient);
            }

            postZBuffer[x+y*width]=0; //clear post buffer
            postRBuffer[x+y*width]=0;
            postGBuffer[x+y*width]=0;
            postBBuffer[x+y*width]=0;
        }

    }

    void putThing(int x, int y){
        float origz = ZBuffer[x+y*width];
        float origr = RBuffer[x+y*width];
        float origg = GBuffer[x+y*width];
        float origb = BBuffer[x+y*width];
        float newVal=0;
        int size = (int)( max(1,(int)(scaleUp*SBuffer[x+y*width]*(origz*origz)/1024f)));
        for(int _x=-size; _x<size+1; _x++){
            for(int _y=-size; _y<size+1; _y++){
                newVal=origz;//sprite function goes here

                if(_x*_x+_y*_y<size*size && newVal>postZBuffer[(x+_x)+(y+_y)*width]){
                    postZBuffer[(x+_x)+(y+_y)*width]=newVal;
                    postRBuffer[(x+_x)+(y+_y)*width]=origr;
                    postGBuffer[(x+_x)+(y+_y)*width]=origg;
                    postBBuffer[(x+_x)+(y+_y)*width]=origb;
                }
            }
        }
    }

    int getColor(int x, int y, float gradient){
        int _argb = 255;

        _argb = (_argb << 8) + (int)(postRBuffer[x+y*width]*brightness*gradient);
        _argb = (_argb << 8) + (int)(postGBuffer[x+y*width]*brightness*gradient);
        _argb = (_argb << 8) + (int)(postBBuffer[x+y*width]*brightness*gradient);

        return _argb;
    }

    int gray(int g){
        int _argb = 255;

        _argb = (_argb << 8) + g;
        _argb = (_argb << 8) + g;
        _argb = (_argb << 8) + g;

        return _argb;
    }

    float getMaxSlope(int x, int y){
        float central =  (postZBuffer[x+y*width]);
        float maxslope1 = max(max((float) postZBuffer[(x - 1) + (y) * width] - central, (float) postZBuffer[(x + 1) + (y) * width] - central),
                max((float) postZBuffer[(x) + (y - 1) * width] - central, (float) postZBuffer[(x) + (y + 1) * width] - central));
        float maxslope2 = max(max((float) postZBuffer[(x - 1) + (y - 1) * width] - central, (float) postZBuffer[(x + 1) + (y + 1) * width] - central),
                max((float) postZBuffer[(x - 1) + (y + 1) * width] - central, (float) postZBuffer[(x + 1) + (y - 1) * width] - central));
        return max(maxslope2, maxslope1);
    }

}
