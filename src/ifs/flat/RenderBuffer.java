package ifs.flat;

/**
 * Created by user on 8/11/14.
 */
public class RenderBuffer {

    public float ZBuffer_Left[][];
    public float RBuffer_Left[][];
    public float GBuffer_Left[][];
    public float BBuffer_Left[][];

    public float ZBuffer_Right[][];
    public float RBuffer_Right[][];
    public float GBuffer_Right[][];
    public float BBuffer_Right[][];

    public int pixels[];

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
    }
}
