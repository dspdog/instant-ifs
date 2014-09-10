package ifs.flat;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

public final class RenderBuffer extends Kernel{

    public final long TBuffer[];
    public final float ZBuffer[];
    public final float RBuffer[];
    public final float GBuffer[];
    public final float BBuffer[];

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
        x=Math.min((int)x, width);

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

        if(frameNum%1000==0){
            System.out.println(this.getExecutionMode().toString() + " " + this.getExecutionTime());
        }
    }

    @Override
    public void run() {
        int edge = 4;
        int x = getGlobalId(0);
        int y = getGlobalId(1);
        float gradient=1.0f;

        if (x>edge && x<(width-edge) && y>edge && y<(height-edge)){

            if((TBuffer[x+y*width]<time-shutterSpeed) && TBuffer[x+y*width]<frameStartTime){
                ZBuffer[x+y*width]=0;
            }

            if(cartoon){
                int maxslope = getMaxSlope(x,y);
                if(maxslope>1){
                    maxslope=255;
                    if(ZBuffer[x+y*width]==0){
                        ZBuffer[x+y*width]=1;//outside edges
                    }
                }
                gradient = 1.0f-maxslope/255.0f;
            }


            if(ZBuffer[x+y*width]==0){ //leaves empty pixels transparent
                pixels[x+y*width]=0;
            }else{
                int color = getColor(x,y,gradient, 200);
                pixels[x+y*width] = color;
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

    int getMaxSlope(int x, int y){
        int central = (int) ZBuffer[x+y*width];
        int maxslope1 = max(max((int) ZBuffer[(x - 1) + (y) * width] - central, (int) ZBuffer[(x + 1) + (y) * width] - central),
                max((int) ZBuffer[(x) + (y - 1) * width] - central, (int) ZBuffer[(x) + (y + 1) * width] - central));
        int maxslope2 = max(max((int) ZBuffer[(x - 1) + (y - 1) * width] - central, (int) ZBuffer[(x + 1) + (y + 1) * width] - central),
                max((int) ZBuffer[(x - 1) + (y + 1) * width] - central, (int) ZBuffer[(x + 1) + (y - 1) * width] - central));
        return max(maxslope2, maxslope1);
    }

}
