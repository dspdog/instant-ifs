package ifs.flat;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.OpenCL;
import com.amd.aparapi.Range;

/**
 * Created by user on 8/11/14.
 */
public class RenderBuffer {

    public float ZBuffer_Left[][];
    public float ZBuffer_Right[][];

    public final float RBuffer_Left[][];
    public final float GBuffer_Left[][];
    public final float BBuffer_Left[][];

    public final float RBuffer_Right[][];
    public final float GBuffer_Right[][];
    public final float BBuffer_Right[][];

    public final int pixels[];

    int width, height;

    float maxColor = 0;

    public RenderBuffer(int w, int h){
        width=w; height=h;
        RBuffer_Left = new float[width][height];
        GBuffer_Left = new float[width][height];
        BBuffer_Left = new float[width][height];
        RBuffer_Right = new float[width][height];
        GBuffer_Right = new float[width][height];
        BBuffer_Right = new float[width][height];
        clearZProjection();

        pixels = new int[width*height];
    }

    public void clearZProjection(){
        ZBuffer_Left = new float[width][height];
        ZBuffer_Right = new float[width][height];
        maxColor=0;
    }


    public boolean putPixel(float x, float y, float z, float R, float G, float B, float dark, boolean rightEye){

        float[][] theZBuffer = rightEye ? ZBuffer_Right : ZBuffer_Left;
        float[][] theRBuffer = rightEye ? RBuffer_Right : RBuffer_Left;
        float[][] theGBuffer = rightEye ? GBuffer_Right : GBuffer_Left;
        float[][] theBBuffer = rightEye ? BBuffer_Right : BBuffer_Left;

        if(theZBuffer[(int)x][(int)y] < z){
            theRBuffer[(int)x][(int)y] = R*dark;
            theGBuffer[(int)x][(int)y] = G*dark;
            theBBuffer[(int)x][(int)y] = B*dark;
            theZBuffer[(int)x][(int)y] = z;
            return true;
        }else{
            return false;
        }
    }

    public void generatePixels(float brightness, boolean useShadows, boolean rightEye){
        int argb;
        float gradient=1f;
        float maxslope=0;
        float maxslope2=0;

        float[][] theZBuffer = rightEye ? ZBuffer_Right : ZBuffer_Left;
        float[][] theRBuffer = rightEye ? RBuffer_Right : RBuffer_Left;
        float[][] theGBuffer = rightEye ? GBuffer_Right : GBuffer_Left;
        float[][] theBBuffer = rightEye ? BBuffer_Right : BBuffer_Left;

        for(int x = 1; x < width-1; x++){
            for(int y=1; y<height-1; y++){
                if(useShadows){
                    maxslope2 = 1.0f / (float)Math.sqrt(2.0) *
                            Math.max(
                                    Math.max((theZBuffer[x-1][y-1]-theZBuffer[x][y]),
                                             (theZBuffer[x+1][y+1]-theZBuffer[x][y])),

                                    Math.max((theZBuffer[x+1][y-1]-theZBuffer[x][y]),
                                             (theZBuffer[x-1][y+1]-theZBuffer[x][y])));

                    maxslope = Math.max(
                                        Math.max((theZBuffer[x-1][y]-theZBuffer[x][y]),
                                                 (theZBuffer[x+1][y]-theZBuffer[x][y])),

                                        Math.max((theZBuffer[x][y-1]-theZBuffer[x][y]),
                                                 (theZBuffer[x][y+1]-theZBuffer[x][y])));

                    maxslope = Math.max(maxslope,maxslope2);

                    if(maxslope>1){
                        maxslope=255;
                        if(theZBuffer[x][y]==0){theZBuffer[x][y]=1;}//outside edges
                    }

                    gradient = 1.0f-maxslope/255.0f;
                }

                if(theZBuffer[x][y]==0){
                    argb=0;
                }else{
                    argb = 255;
                    argb = (argb << 8) + (int)(theRBuffer[x][y]*brightness*gradient);
                    argb = (argb << 8) + (int)(theGBuffer[x][y]*brightness*gradient);
                    argb = (argb << 8) + (int)(theBBuffer[x][y]*brightness*gradient);
                }

                pixels[x+y*width] = argb;
            }
        }
/*
        final int WIDTH=128;
        final int HEIGHT=64;
        final int in[] = new int[WIDTH*HEIGHT];
        final int out[] = new int[WIDTH*HEIGHT];
        Kernel kernel = new Kernel(){
            public void run(){
                int x = getGlobalId(0);
                int y = getGlobalId(1);
                if (x>0 && x<(getGlobalSize(0)-1) && y>0 && y<(getGlobalSize(0)-1)){
                    int sum = 0;
                    for (int dx =-1; dx<2; dx++){
                        for (int dy =-1; dy<2; dy++){
                            sum+=in[(y+dy)*getGlobalSize(0)+(x+dx)];
                        }
                    }
                    out[y*getGlobalSize(0)+x] = sum/9;
                }
            }

        };
        Range range = Range.create2D(WIDTH, HEIGHT);
        kernel.execute(range);*/
    }
}
