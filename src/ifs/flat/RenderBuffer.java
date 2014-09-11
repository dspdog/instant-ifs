package ifs.flat;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

public final class RenderBuffer extends Kernel{

    public final long TBuffer[];
    public final float ZBuffer[];
    public final float RBuffer[];
    public final float GBuffer[];
    public final float BBuffer[];

    public final float postZBuffer[];

    public final int pixels[];

    boolean cartoon=false;
    boolean clearZBuffer;

    float brightness = 1.0f;

    int width, height;

    float maxColor = 0;

    private static long time = System.currentTimeMillis();
    private static long frameStartTime = System.currentTimeMillis();

    public long frameNum=0;

    public static int shutterSpeed = 50;

    public RenderBuffer(int w, int h){
        width=w; height=h;
        RBuffer = new float[width*height];
        GBuffer = new float[width*height];
        BBuffer = new float[width*height];

        ZBuffer = new float[width*height];
        TBuffer = new long[width*height];

        postZBuffer = new float[width*height];

        clearZProjection();
        cartoon=false;
        pixels=new int[width*height];
    }

    public void clearZProjection(){
        maxColor=0;
        clearZBuffer = true; //request Z-buffer clear from GPU kernal
    }

    public void updateTime(long _time){
        time = System.currentTimeMillis();
        frameStartTime=_time;
    }

    public boolean putPixel(float x, float y, float z, float R, float G, float B, float dark, boolean rightEye){

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
            return true;
        }else{
            return false;
        }
    }

    public void generatePixels(float _brightness, boolean useShadows, boolean rightEye){
        cartoon=useShadows;
        brightness=_brightness;
        Range range = Range.create2D(width,height);
        this.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
        this.execute(range);
        frameNum++;
        clearZBuffer=false; //de-request z-buffer clear

        for(int i=0; i<width*width; i++){
            pixels[i]=gray(Math.max(1,(int)postZBuffer[i]));
        }
        for(int i=0; i<width*width; i++){
            postZBuffer[i]=0;
        }
        if(frameNum%1000==0){
            System.out.println(this.getExecutionMode().toString() + " " + this.getExecutionTime());
        }
    }

    @Override
    public void run() {

        int edge = 32;
        int x = getGlobalId(0);
        int y = getGlobalId(1);

        if (x>edge && x<(width-edge) && y>edge && y<(height-edge)){

            if((TBuffer[x+y*width]<time-shutterSpeed) && TBuffer[x+y*width]<frameStartTime){
                ZBuffer[x+y*width]=0;
            }

            if(ZBuffer[x+y*width]>0){
                postZBuffer[x+y*width]=(int)ZBuffer[x+y*width];
                //putThing(x,y,ZBuffer[x+y*width]);
            }

        }
    }

    void putThing(int x, int y, float origz){
        int size = (int)(64*origz/512);
        for(int _x=-size; _x<size+1; _x++){
            for(int _y=-size; _y<size+1; _y++){
                if(_x*_x+_y*_y<size*size){
                    postZBuffer[(x+_x)+(y+_y)*width]=max(origz, postZBuffer[(x+_x)+(y+_y)*width]);
                }
            }
        }
    }

    int getColor(int x, int y, float gradient, int opacity){
        int _argb = opacity;

        _argb = (_argb << 8) + (int)(RBuffer[x+y*width]*brightness*gradient);
        _argb = (_argb << 8) + (int)(GBuffer[x+y*width]*brightness*gradient);
        _argb = (_argb << 8) + (int)(BBuffer[x+y*width]*brightness*gradient);

        return _argb;
    }

    int gray(int g){
        int _argb = 255;

        _argb = (_argb << 8) + g;
        _argb = (_argb << 8) + g;
        _argb = (_argb << 8) + g;

        return _argb;
    }

    int getMaxSlope(int x, int y){
        int central = (int) (ZBuffer[x+y*width]);
        int maxslope1 = max(max((int) ZBuffer[(x - 1) + (y) * width] - central, (int) ZBuffer[(x + 1) + (y) * width] - central),
                max((int) ZBuffer[(x) + (y - 1) * width] - central, (int) ZBuffer[(x) + (y + 1) * width] - central));
        int maxslope2 = max(max((int) ZBuffer[(x - 1) + (y - 1) * width] - central, (int) ZBuffer[(x + 1) + (y + 1) * width] - central),
                max((int) ZBuffer[(x - 1) + (y + 1) * width] - central, (int) ZBuffer[(x + 1) + (y - 1) * width] - central));
        return max(maxslope2, maxslope1);
    }

}
