package ifs.flat;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

/**
 * Created by user on 8/11/14.
 */
public class RenderBuffer extends Kernel{

    public final float ZBuffer[];
    public final float RBuffer[];
    public final float GBuffer[];
    public final float BBuffer[];

    boolean cartoon=false;

    public final int pixels[];

    int width, height;

    float maxColor = 0;

    public RenderBuffer(int w, int h){
        width=w; height=h;
        RBuffer = new float[width*height];
        GBuffer = new float[width*height];
        BBuffer = new float[width*height];

        ZBuffer = new float[width*height];

        clearZProjection();
        cartoon=false;
        pixels = new int[width*height];
    }

    public void clearZProjection(){
        for(int i=0; i<width*height; i++){
            ZBuffer[i]=0;
        }
        maxColor=0;
    }


    public boolean putPixel(float x, float y, float z, float R, float G, float B, float dark, boolean rightEye){
        if(ZBuffer[(int)x+(int)y*width] < z){
            ZBuffer[(int)x+(int)y*width] = R*dark;
            GBuffer[(int)x+(int)y*width] = G*dark;
            BBuffer[(int)x+(int)y*width] = B*dark;
            ZBuffer[(int)x+(int)y*width] = z;
            return true;
        }else{
            return false;
        }
    }

    public void generatePixels(float brightness, boolean useShadows, boolean rightEye){
        cartoon=useShadows;
        Range range = Range.create2D(width,height);
        this.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
        this.execute(range);
    }

    @Override
    public void run() {
        int x = getGlobalId(0);
        int y = getGlobalId(1);

        if (x>0 && x<(getGlobalSize(0)-1) && y>0 && y<(getGlobalSize(1)-1)){
            float gradient=1.0f;
            int brightness = 1;

            if(cartoon){
                int central = (int) ZBuffer[x+y*width];
                int maxslope1 = max(max((int) ZBuffer[(x - 1) + (y) * width] - central, (int) ZBuffer[(x + 1) + (y) * width] - central),
                        max((int) ZBuffer[(x) + (y - 1) * width] - central, (int) ZBuffer[(x) + (y + 1) * width] - central));
                int maxslope2 = max(max((int) ZBuffer[(x - 1) + (y - 1) * width] - central, (int) ZBuffer[(x + 1) + (y + 1) * width] - central),
                        max((int) ZBuffer[(x - 1) + (y + 1) * width] - central, (int) ZBuffer[(x + 1) + (y - 1) * width] - central));
                int maxslope = max(maxslope2, maxslope1);

                if(maxslope>1){
                    maxslope=255;
                    if(ZBuffer[x+y*width]==0){
                        ZBuffer[x+y*width]=1;}//outside edges
                }
                gradient = 1.0f-maxslope/255.0f;
            }

            int _argb = 255;
            if(ZBuffer[x+y*width]==0){
                _argb=0;
            }else{
                _argb = (_argb << 8) + (int)(RBuffer[x+y*width]*brightness*gradient);
                _argb = (_argb << 8) + (int)(GBuffer[x+y*width]*brightness*gradient);
                _argb = (_argb << 8) + (int)(BBuffer[x+y*width]*brightness*gradient);
            }

            pixels[x+y*getGlobalSize(0)] = _argb;
        }
    }

    public int max(int x, int y){
        return x-((x-y)&((x-y)>>31));
    }
}
