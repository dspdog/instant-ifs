package ifs.flat;

/**
 * Created by user on 8/11/14.
 */
public class RenderBuffer {

    public float ZBuffer[][];
    public float RBuffer[][];
    public float GBuffer[][];
    public float BBuffer[][];

    public int pixels[];

    int width, height;

    float maxColor = 0;

    public RenderBuffer(int w, int h){
        width=w; height=h;
        RBuffer = new float[width][height];
        GBuffer = new float[width][height];
        BBuffer = new float[width][height];
        clearZProjection();
        pixels = new int[width*height];
    }

    public void clearZProjection(){
        ZBuffer = new float[width][height];
        maxColor=0;
    }


    public boolean putPixel(float x, float y, float z, float R, float G, float B, float dark){
        /*
                pixelsData[(int)(x) + (int)(y) * screenwidth]+=(1.0-decX)*(1.0-decY);
                pixelsData[(int)(x+1) + (int)(y) * screenwidth]+=decX*(1.0-decY);
                pixelsData[(int)(x) + (int)(y+1) * screenwidth]+=decY*(1.0-decX);
                pixelsData[(int)(x+1) + (int)(y+1) * screenwidth]+=decY*decX;

         */

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

    public void generatePixels(float brightness, boolean useShadows){
        int argb;
        float gradient=1f;
        float maxslope=0;
        float maxslope2=0;

        for(int x = 1; x < width-1; x++){
            for(int y=1; y<height-1; y++){
                if(useShadows){
                    maxslope2 = 1.0f / (float)Math.sqrt(2.0) *
                            Math.max(
                                    Math.max((ZBuffer[x-1][y-1]-ZBuffer[x][y]),
                                             (ZBuffer[x+1][y+1]-ZBuffer[x][y])),

                                    Math.max((ZBuffer[x+1][y-1]-ZBuffer[x][y]),
                                             (ZBuffer[x-1][y+1]-ZBuffer[x][y])));

                    maxslope = Math.max(
                                        Math.max((ZBuffer[x-1][y]-ZBuffer[x][y]),
                                                 (ZBuffer[x+1][y]-ZBuffer[x][y])),

                                        Math.max((ZBuffer[x][y-1]-ZBuffer[x][y]),
                                                 (ZBuffer[x][y+1]-ZBuffer[x][y])));

                    maxslope = Math.max(maxslope,maxslope2);

                    if(maxslope>1){
                        maxslope=255;
                        if(ZBuffer[x][y]==0){ZBuffer[x][y]=1;}//outside edges
                    }

                    gradient = 1.0f-maxslope/255.0f;

                }

                if(ZBuffer[x][y]==0){
                    argb=0;
                }else{
                    argb = 255;
                    argb = (argb << 8) + (int)(RBuffer[x][y]*brightness*gradient);
                    argb = (argb << 8) + (int)(GBuffer[x][y]*brightness*gradient);
                    argb = (argb << 8) + (int)(BBuffer[x][y]*brightness*gradient);
                }

                pixels[x+y*width] = argb;
            }
        }
    }
}
