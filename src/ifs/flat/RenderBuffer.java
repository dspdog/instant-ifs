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

    public RenderBuffer(int w, int h){
        width=w; height=h;
        ZBuffer = new float[width][height];
        RBuffer = new float[width][height];
        GBuffer = new float[width][height];
        BBuffer = new float[width][height];
        pixels = new int[width*height];
    }

    public void clearProjections(){
        /*for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                ZBuffer[x][y]=0;
                RBuffer[x][y]=0;
                GBuffer[x][y]=0;
                BBuffer[x][y]=0;
            }
        }*/
        ZBuffer = new float[width][height];
        RBuffer = new float[width][height];
        GBuffer = new float[width][height];
        BBuffer = new float[width][height];
    }

    public void generatePixels(float brightness, boolean useShadows){
        int argb;
        float gradient=1f;
        float maxslope=0;
        float maxslope2=0;

        for(int x = 1; x < width-1; x++){
            for(int y=1; y<height-1; y++){
                if(useShadows){
                    maxslope2 = 1.0f / (float)Math.sqrt(2.0) * Math.max(Math.max((ZBuffer[x-1][y-1]-ZBuffer[x][y]),
                            (ZBuffer[x+1][y+1]-ZBuffer[x][y])),
                            Math.max((ZBuffer[x+1][y-1]-ZBuffer[x][y]),
                                    (ZBuffer[x-1][y+1]-ZBuffer[x][y])));

                    maxslope = Math.max(Math.max((ZBuffer[x-1][y]-ZBuffer[x][y]),
                            (ZBuffer[x+1][y]-ZBuffer[x][y])),
                            Math.max((ZBuffer[x][y-1]-ZBuffer[x][y]),
                                    (ZBuffer[x][y+1]-ZBuffer[x][y])));

                    maxslope = Math.max(maxslope,maxslope2);

                    if(maxslope>5){maxslope=255;}

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
