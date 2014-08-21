package ifs.flat;

/**
 * Created by user on 8/11/14.
 */
public class RenderBuffer {

    public float ZBuffer[][];
    public float RBuffer[][];
    public float GBuffer[][];
    public float BBuffer[][];

    public float ZBuffer2[][];
    public float RBuffer2[][];
    public float GBuffer2[][];
    public float BBuffer2[][];

    public int pixels[];

    int width, height;

    float maxColor = 0;

    public RenderBuffer(int w, int h){
        width=w; height=h;
        RBuffer = new float[width][height];
        GBuffer = new float[width][height];
        BBuffer = new float[width][height];
        RBuffer2 = new float[width][height];
        GBuffer2 = new float[width][height];
        BBuffer2 = new float[width][height];
        clearZProjection();
        pixels = new int[width*height];
    }

    public void clearZProjection(){
        ZBuffer = new float[width][height];
        ZBuffer2 = new float[width][height];
        maxColor=0;
    }


    public boolean putPixel(float x, float y, float z, float R, float G, float B, float dark, boolean altBuffer){
        /*
                pixelsData[(int)(x) + (int)(y) * screenwidth]+=(1.0-decX)*(1.0-decY);
                pixelsData[(int)(x+1) + (int)(y) * screenwidth]+=decX*(1.0-decY);
                pixelsData[(int)(x) + (int)(y+1) * screenwidth]+=decY*(1.0-decX);
                pixelsData[(int)(x+1) + (int)(y+1) * screenwidth]+=decY*decX;

         */
        if(altBuffer){
            if(ZBuffer2[(int)x][(int)y] < z){
                RBuffer2[(int)x][(int)y] = R*dark;
                GBuffer2[(int)x][(int)y] = G*dark;
                BBuffer2[(int)x][(int)y] = B*dark;
                ZBuffer2[(int)x][(int)y] = z;
                return true;
            }else{
                return false;
            }
        }else{
            if(ZBuffer[(int)x][(int)y] < z){
                RBuffer[(int)x][(int)y] = R*dark;
                GBuffer[(int)x][(int)y] = G*dark;
                BBuffer[(int)x][(int)y] = B*dark;
                ZBuffer[(int)x][(int)y] = z;
                return true;
            }else{
                return false;
            }
        }
    }

    public void generatePixels(float brightness, boolean useShadows, boolean altBuffer){
        int argb;
        float gradient=1f;
        float maxslope=0;
        float maxslope2=0;

        float[][] theZBuffer = altBuffer ? ZBuffer2 : ZBuffer;
        float[][] theRBuffer = altBuffer ? RBuffer2 : RBuffer;
        float[][] theGBuffer = altBuffer ? GBuffer2 : GBuffer;
        float[][] theBBuffer = altBuffer ? BBuffer2 : BBuffer;

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
