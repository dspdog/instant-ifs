package ifs.flat;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.ProfileInfo;
import com.amd.aparapi.Range;

import java.util.List;

public final class RenderBuffer extends Kernel{

    final static float PFf = (float)Math.PI;

    public final int lineX1[];
    public final int lineY1[];
    public final int lineZ1[];
    public final int lineS1[];
    public final int lineX2[];
    public final int lineY2[];
    public final int lineZ2[];
    public final int lineS2[];

    public final short projX1[];
    public final short projY1[];
    public final short projZ1[];
    public final short projX2[];
    public final short projY2[];
    public final short projZ2[];

    public final int pixels[];
    public final short accumulator[];

    public int lineIndex = 0;
    public int lastLineIndex =0;

    boolean cartoon=false;
    final boolean shading = true;

    float brightness = 1.0f;

    int width, height;
    float perspectiveScale=200f;
    float maxColor = 0;

    public float camPitch, camYaw, camRoll, camScale, camCenterX, camCenterY, camCenterZ;

    private static long time = System.currentTimeMillis();
    private static long frameStartTime = System.currentTimeMillis();

    public long frameNum=0;

    public static int shutterSpeed = 50;

    public float scaleUp =1.0f;

    public boolean addSamples=true;
    public boolean usePerspective = true;

    final int NUM_LINES = 1024*1024/2;

    public int totalLines;

    int accumTime;

    public RenderBuffer(int w, int h){
        totalLines=100;
        width=1024; height=1024;

        projX1 = new short[NUM_LINES];
        projY1 = new short[NUM_LINES];
        projZ1 = new short[NUM_LINES];
        projX2 = new short[NUM_LINES];
        projY2 = new short[NUM_LINES];
        projZ2 = new short[NUM_LINES];

        lineX1 = new int[NUM_LINES];
        lineY1 = new int[NUM_LINES];
        lineZ1 = new int[NUM_LINES];
        lineS1 = new int[NUM_LINES];

        lineX2 = new int[NUM_LINES];
        lineY2 = new int[NUM_LINES];
        lineZ2 = new int[NUM_LINES];
        lineS2 = new int[NUM_LINES];

        cartoon=false;
        pixels=new int[width*height];
        accumulator=new short[width*height];

        addSamples=true;

        camPitch=0;
        camYaw=0;
        camRoll=0;

        this.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
    }

    public void updateTime(long _time){
        time = System.currentTimeMillis();
        frameStartTime=_time;
    }

    public boolean putPixel(float x, float y, float z, float R, float G, float B, float scale, float dark, boolean rightEye){
        return false;
    }

    public void drawLine(int _index){
        projX1[_index] = (short)lineX1[_index];
        projY1[_index] = (short)lineY1[_index];
        projZ1[_index] = (short)lineZ1[_index];
        projX2[_index] = (short)lineX2[_index];
        projY2[_index] = (short)lineY2[_index];
        projZ2[_index] = (short)lineZ2[_index];

        cameraDistort(_index, 1, 1);

            float X1 = projX1[_index];
            float X2 = projX2[_index];
            float Y1 = projY1[_index];
            float Y2 = projY2[_index];
            float Z1 = projZ1[_index];
            float Z2 = projZ2[_index];
            float S1 = lineS1[_index];
            float S2 = lineS2[_index];

            float mag = sqrt((X2-X1)*(X2-X1)+(Y2-Y1)*(Y2-Y1));

            mag = min(max(mag, 1), width*sqrt(2));

            if(X1>1 && Y1>1  && X1<width && Y1<height && X2>1 && Y2>1  && X2<width && Y2<height){//valid lines only
                float dx=0, dy=0, dz=0, ds=0;
                float olddx, olddy;

                for(int i=0; i<mag; i++){


                    cameraDistort(_index, i, (int)mag);

                    X1 = projX1[_index];
                    X2 = projX2[_index];
                    Y1 = projY1[_index];
                    Y2 = projY2[_index];
                    Z1 = projZ1[_index];
                    Z2 = projZ2[_index];
                    S1 = lineS1[_index];
                    S2 = lineS2[_index];




                    olddx=dx;
                    olddy=dy;
                    dx = X1 + (float)i*(X2 - X1)/mag;
                    dy = Y1 + (float)i*(Y2 - Y1)/mag;
                    dz = Z1 + (float)i*(Z2 - Z1)/mag;
                    ds = S1 + (float)i*(S2 - S1)/mag;

                    float sX1 = dx;
                    float sY1 = dy;
                    float sX2 = olddx;
                    float sY2 = olddy;

                    float offsetX = (sX1+sX2)/2f;
                    float offsetY = (sY1+sY2)/2f;
                    float sdx = sX2-sX1;
                    float sdy = sY2-sY1;
                    float downScale=scaleDownDistance(dz);
                    float _mag = ds/32f * downScale;
                    _mag = min(max(_mag, 1f), 32f);

                    float nx1 = -sdy*_mag + offsetX;
                    float ny1 = sdx*_mag + offsetY;
                    float nx2 = sdy*_mag + offsetX;
                    float ny2 = -sdx*_mag + offsetY;

                    if(nx1>1 && ny1>1  && nx1<width && ny1<height && nx2>1 && ny2>1  && nx2<width && ny2<height){//valid lines only
                        for(float _i=0; _i<_mag; _i+=max(_mag / 32, 1)){

                            float _dx = nx1 + _i*(nx2 - nx1)/_mag;
                            float _dy = ny1 + _i*(ny2 - ny1)/_mag;

                            int _x = min(max((int) _dx, 0), width);
                            int _y = min(max((int) _dy, 0), height);
                            int grayval = (int)(dz/16f);

                            if((pixels[_x+_y*width]&255)<=grayval)
                                pixels[_x+_y*width] = argb(255,1,(int)(_mag),grayval);
                        }
                    }

                }
            }


    }

    private void lineRotate(int _index, float x, float y, float z, float _a){ //quaternion rotate
        _a/=2;
        float sa2 = sin(_a);
        float ca = cos(_a);

        float qw=ca;
        float qx=x*sa2;
        float qy=y*sa2;
        float qz=z*sa2;
        float qw2;
        float qx2;
        float qy2;
        float qz2;

        float _qw = qTimes_W(qw, qx, qy, qz, 0, projX1[_index], projY1[_index], projZ1[_index]);
        float _qx = qTimes_X(qw, qx, qy, qz, 0, projX1[_index], projY1[_index], projZ1[_index]);
        float _qy = qTimes_Y(qw, qx, qy, qz, 0, projX1[_index], projY1[_index], projZ1[_index]);
        float _qz = qTimes_Z(qw, qx, qy, qz, 0, projX1[_index], projY1[_index], projZ1[_index]);

        float _qw2 = qTimes_W(qw, qx, qy, qz, 0, projX2[_index], projY2[_index], projZ2[_index]);
        float _qx2 = qTimes_X(qw, qx, qy, qz, 0, projX2[_index], projY2[_index], projZ2[_index]);
        float _qy2 = qTimes_Y(qw, qx, qy, qz, 0, projX2[_index], projY2[_index], projZ2[_index]);
        float _qz2 = qTimes_Z(qw, qx, qy, qz, 0, projX2[_index], projY2[_index], projZ2[_index]);

        qw=_qw;qx=_qx;qy=_qy;qz=_qz;
        qw2=_qw2;qx2=_qx2;qy2=_qy2;qz2=_qz2;

        projX1[_index]= (short)qTimes_X(qw, qx, qy, qz, ca, -x*sa2, -y*sa2, -z*sa2);
        projY1[_index]= (short)qTimes_Y(qw, qx, qy, qz, ca, -x*sa2, -y*sa2, -z*sa2);
        projZ1[_index]= (short)qTimes_Z(qw, qx, qy, qz, ca, -x*sa2, -y*sa2, -z*sa2);

        projX2[_index]= (short)qTimes_X(qw2, qx2, qy2, qz2, ca, -x*sa2, -y*sa2, -z*sa2);
        projY2[_index]= (short)qTimes_Y(qw2, qx2, qy2, qz2, ca, -x*sa2, -y*sa2, -z*sa2);
        projZ2[_index]= (short)qTimes_Z(qw2, qx2, qy2, qz2, ca, -x*sa2, -y*sa2, -z*sa2);
    }

    private float qTimes_W(float aW, float aX, float aY, float aZ, float w, float x, float y, float z){
        return aW *w - aX *x - aY *y - aZ *z;
    }

    private float qTimes_X(float aW, float aX, float aY, float aZ, float w, float x, float y, float z){
        return aW *x + aX *w + aY *z - aZ *y;
    }

    private float qTimes_Y(float aW, float aX, float aY, float aZ, float w, float x, float y, float z){
        return aW *y - aX *z + aY *w + aZ *x;
    }

    private float qTimes_Z(float aW, float aX, float aY, float aZ, float w, float x, float y, float z){
        return aW *z + aX *y - aY *x + aZ *w;
    }

    public void cameraDistort(int _index, int sector, int totalSectors){

        float _dx = lineX1[_index] + sector*(lineX2[_index] - lineX1[_index])/totalSectors;
        float _dy = lineY1[_index] + sector*(lineY2[_index] - lineY1[_index])/totalSectors;

        projX1[_index] = (short)(lineX1[_index] - camCenterX);
        projY1[_index] = (short)(lineY1[_index] - camCenterY);
        projZ1[_index] = (short)(lineZ1[_index] - camCenterZ);
        projX2[_index] = (short)(lineX2[_index] - camCenterX);
        projY2[_index] = (short)(lineY2[_index] - camCenterY);
        projZ2[_index] = (short)(lineZ2[_index] - camCenterZ);

        lineRotate(_index, 1.0f, 0.0f, 0.0f, camPitch / 180.0f * PFf);
        lineRotate(_index, 0.0f, 1.0f, 0.0f, camYaw / 180.0f * PFf);
        lineRotate(_index, 0.0f, 0.0f, 1.0f, camRoll / 180.0f * PFf);

        projX1[_index] =(short)( projX1[_index]*camScale+camCenterX);
        projY1[_index] =(short)( projY1[_index]*camScale+camCenterY);
        projZ1[_index] =(short)( projZ1[_index]*camScale+camCenterZ);
        projX2[_index] =(short)( projX2[_index]*camScale+camCenterX);
        projY2[_index] =(short)( projY2[_index]*camScale+camCenterY);
        projZ2[_index] =(short)( projZ2[_index]*camScale+camCenterZ);

        float vx = width/2; //vanishing pt onscreen
        float vy = height/2;

        if(usePerspective){
            float downScale1=scaleDownDistance(projZ1[_index]);
            float downScale2=scaleDownDistance(projZ2[_index]);
            projX1[_index]=(short)((projX1[_index]-vx)*downScale1 + vx);
            projY1[_index]=(short)((projY1[_index]-vy)*downScale1 + vy);
            projX2[_index]=(short)((projX2[_index]-vx)*downScale2 + vx);
            projY2[_index]=(short)((projY2[_index]-vy)*downScale2 + vy);
        }
    }

    public float scaleDownDistance(float input){
        return perspectiveScale*0.1f/(float)sqrt(1024f-input);
    }

    public void updateGeometry(){
        this.put(lineX1).put(lineY1).put(lineZ1).put(lineS1)
            .put(lineX2).put(lineY2).put(lineZ2).put(lineS2);
    }

    public void generatePixels(float _brightness, boolean useShadows, boolean rightEye, boolean _putSamples, float _size, float pitch, float yaw, float roll, boolean _usePerspective,
                               float scale, float camX, float camY, float camZ, float perspScale, int _accumTime){
        camPitch=pitch;
        camRoll=roll;
        camYaw=yaw;
        camCenterX = camX;
        camCenterY = camY;
        camCenterZ = camZ;
        camScale = scale;
        usePerspective = _usePerspective;

        perspectiveScale = perspScale;

        cartoon=useShadows;
        addSamples=_putSamples;
        brightness=_brightness;
        scaleUp=_size;
        accumTime = _accumTime;

        this.setExplicit(true);

        Range range = Range.create2D(width, height);

        this.execute(range, 3);
        this.get(pixels);

        frameNum++;

        if(frameNum%1000==0){
            System.out.println(this.getExecutionMode().toString() + " " + this.getExecutionTime() + " lines: " + totalLines);
        }
    }

    @Override
    public void run() {
        int x = getGlobalId(0);
        int y = getGlobalId(1);

        if(getPassId()==0){ //clear frame
            pixels[x+y*width]=black();
        }
        if(getPassId()==1){ //draw skeleton
            int myLineIndex = x+y*width;
            if(myLineIndex<NUM_LINES && myLineIndex<totalLines)
                drawLine(myLineIndex);
        }
        if(getPassId()==2){ //z-process

           // float gradient=1.0f;

           // float gms = x>1&&y>1&&x<width-1&&y<height-1 ? getMaxSlope(x,y)/16f : 0;
           // float maxslope = min(gms*255f, 255f);

           //  gradient = shading ? 1.0f-maxslope/255.0f : 1.0f;
            getColor(x, y, 1.0f);
        }
    }

    int getMaxSlope(int x, int y){
        int central =  (int)(pixels[x+y*width]);
        int maxslope1 = (int)max(max(pixels[(x - 1) + (y) * width] - central, pixels[(x + 1) + (y) * width] - central),
                        max(pixels[(x) + (y - 1) * width] - central, pixels[(x) + (y + 1) * width] - central));
        int maxslope2 = (int)(1.0f/sqrt(2.0f)*max(max(pixels[(x - 1) + (y - 1) * width] - central, pixels[(x + 1) + (y + 1) * width] - central),
                        max(pixels[(x - 1) + (y + 1) * width] - central, pixels[(x + 1) + (y - 1) * width] - central)));
        return max(maxslope2, maxslope1);
    }

    void getColor(int x, int y, float gradient){
        int val = pixels[(x)+(y)*width]&255;
        float time = accumTime;
        accumulator[(x)+(y)*width]+=(int) (gradient * val * val / 16f)&255;
        accumulator[x+y*width]*=(1.0f-1.0f/time);

        pixels[(x)+(y)*width] = gray((int)(accumulator[x+y*width]/time));

    }

    void putNormal(int x, int y){
        int size = (int)(((pixels[(x)+(y)*width]>>8)&255)*camScale); //size is green channel
        short z = (short)((pixels[(x)+(y)*width])&255); //z is blue channel
        float scale = scaleDownDistance(z*16f);

        size=(int)(size*scale/4f);
        size = max(1, min(size, 64));
        pixels[(x)+(y)*width]=z;

        float sofar=0;
        if(x>size && y>size && x<(width-size) && y<(height-size)){
            short startingVal=z;
            int _x,_y;
            float total = size;
            if(startingVal>0){
                for(sofar=0; sofar<(int)total;sofar++){
                    _x = 0;
                    _y = (int)(sofar-total/2);
                    if(((pixels[(x+_x)+(y+_y)*width]>>16)&255) != 1){ //if is not line
                        if(startingVal>(pixels[(x+_x)+(y+_y)*width]&255)){
                            pixels[(x+_x)+(y+_y)*width]=startingVal;
                        }
                    }
                    _x = (int)(sofar-total/2);
                    _y = 0;
                    if(((pixels[(x+_x)+(y+_y)*width]>>16)&255) != 1){ //if is not line
                        if(startingVal>(pixels[(x+_x)+(y+_y)*width]&255)){
                            pixels[(x+_x)+(y+_y)*width]=startingVal;
                        }
                    }
                }
            }
        }
    }

    void putX(int x, int y){
        int size = (int)(((pixels[(x)+(y)*width]>>8)&255)*camScale); //size is green channel
        short z = (short)((pixels[(x)+(y)*width])&255); //z is blue channel
        float scale = scaleDownDistance(z*16f);

        size=(int)(size*scale/4f);
        size = max(1, min(size, 64));
        pixels[(x)+(y)*width]=z;

        float sofar=0;
        if(x>size && y>size && x<(width-size) && y<(height-size)){
            short startingVal=z;
            int _x,_y;
            float total = size;
            if(startingVal>0){
                for(sofar=0; sofar<(int)total;sofar++){
                    _x = 0;
                    _y = (int)(sofar-total/2);
                    if(((pixels[(x+_x)+(y+_y)*width]>>16)&255) != 1){ //if is not line
                        if(startingVal>(pixels[(x+_x)+(y+_y)*width]&255)){
                            pixels[(x+_x)+(y+_y)*width]=startingVal;
                        }
                    }
                    _x = (int)(sofar-total/2);
                    _y = 0;
                    if(((pixels[(x+_x)+(y+_y)*width]>>16)&255) != 1){ //if is not line
                        if(startingVal>(pixels[(x+_x)+(y+_y)*width]&255)){
                            pixels[(x+_x)+(y+_y)*width]=startingVal;
                        }
                    }
                }
            }
        }
    }

    final float twoPI = 2f*3.14159265f;
    void putCircle(int x, int y){
        int size = (int)(((pixels[(x)+(y)*width]>>8)&255)*camScale); //size is green channel
        short z = (short)((pixels[(x)+(y)*width])&255); //z is blue channel
        float scale = scaleDownDistance(z*16f);

        size=(int)(size*scale/4f);
        size = max(1, min(size, 64));
        pixels[(x)+(y)*width]=z;

        float sofar=0;
        if(x>size && y>size && x<(width-size) && y<(height-size)){
            short startingVal=z;
            int _x,_y;

            float total = twoPI*size;
            if(startingVal>0){
                for(sofar=0; sofar<(int)total;sofar++){
                    _x = (int)(cos(twoPI*sofar/total)*size);
                    _y = (int)(sin(twoPI*sofar/total)*size);
                    if(((pixels[(x+_x)+(y+_y)*width]>>16)&255) != 1){ //if is not line
                        if(startingVal>(pixels[(x+_x)+(y+_y)*width]&255)){
                            pixels[(x+_x)+(y+_y)*width]=startingVal;
                        }
                    }
                }
            }
        }
    }

    int black(){
        int _argb = 255;

        _argb = (_argb << 8) + 0;
        _argb = (_argb << 8) + 0;
        _argb = (_argb << 8) + 0;

        return _argb;
    }

    int argb(int a, int r, int g, int b){

        a=max(1, min(a, 255))  ;
        r=max(1, min(r, 255))  ;
        g=max(1, min(g, 255))  ;
        b=max(1, min(b, 255))  ;

        int _argb = a;
        _argb = (_argb << 8) + r;
        _argb = (_argb << 8) + g;
        _argb = (_argb << 8) + b;

        return _argb;
    }

    int gray(int g){

        int _argb = 255;

        g=max(1, min(g, 255))  ;
        _argb = (_argb << 8) + g;
        _argb = (_argb << 8) + g;
        _argb = (_argb << 8) + g;

        return _argb;
    }
}
