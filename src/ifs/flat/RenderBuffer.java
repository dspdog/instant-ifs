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

    public boolean withinBounds(float x, float y, float x2, float y2){
        return x>1 && y>1 && x<width && y<height &&
               x2>1 && y2>1 && x2<width && y2<height;
    }

    public float length(float X1, float Y1, float X2, float Y2){
        return sqrt((X2-X1)*(X2-X1)+(Y2-Y1)*(Y2-Y1));
    }

    public void drawLine(int _index){
        cameraDistort(_index, 0, 1);

        float X1 = projX1[_index];
        float X2 = projX2[_index];
        float Y1 = projY1[_index];
        float Y2 = projY2[_index];
        float S1, S2, Z1, Z2;

        if(withinBounds(X1, Y1, X2, Y2)){

            int segs = 10;
            for(int i=0; i<segs; i++){
                cameraDistort(_index, i, segs);

                //TODO distort section-by-section

                X1 = projX1[_index];
                X2 = projX2[_index];
                Y1 = projY1[_index];
                Y2 = projY2[_index];
                Z1 = projZ1[_index];
                Z2 = projZ2[_index];

                S1 = lineS1[_index] + (i)*(lineS2[_index] - lineS1[_index])/segs;
                S2 = lineS1[_index] + (i+1)*(lineS2[_index] - lineS1[_index])/segs;

                plotLine(X1, Y1, X2, Y2, 255, 256);


            }

        }
    }

    void plotLineThick(float X1, float Y1, float X2, float Y2, float Z1, float Z2, float S1, float S2){
        float dx=0, dy=0, dz=0, ds=0;
        float olddx, olddy;

        float subLength = length(X1, Y1, X2, Y2);

        for(int i=0; i<subLength; i++){
            olddx=dx;
            olddy=dy;
            dx = X1 + (float)i*(X2 - X1)/subLength;
            dy = Y1 + (float)i*(Y2 - Y1)/subLength;
            dz = Z1 + (float)i*(Z2 - Z1)/subLength;
            ds = S1 + (float)i*(S2 - S1)/subLength;

            float subX1 = olddx;
            float subY1 = olddy;
            float subX2 = dx;
            float subY2 = dy;

            float thickness = ds/32f * scaleDownDistance(dz);

            float sdx = subX2-subX1;
            float sdy = subY2-subY1;
            float X1normal = -sdy*thickness + subX1;
            float Y1normal =  sdx*thickness + subY1;
            float X2normal =  sdy*thickness + subX1;
            float Y2normal = -sdx*thickness + subY1;

            plotLine(X1normal, Y1normal, X2normal, Y2normal, (int)(dz/32f), 16);
        }
    }

    void plotLine(float X1, float Y1, float X2, float Y2, float color, int maxLen){
        if(withinBounds(X1, Y1, X2, Y2)){
            float fullLength = length(X1, Y1, X2, Y2);
            for(float _i=0; _i<fullLength; _i+=max(fullLength/maxLen, 1)){
                float _dx = X1 + _i*(X2 - X1)/fullLength;
                float _dy = Y1 + _i*(Y2 - Y1)/fullLength;
                if((pixels[(int)_dx+(int)_dy*width]&255)<=color)
                    pixels[(int)_dx+(int)_dy*width] = argb(255,1,(int)(fullLength),(int)color);
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
        sector = min(totalSectors-1,sector);
        float sx1 = lineX1[_index] + sector*(lineX2[_index] - lineX1[_index])/totalSectors;
        float sy1 = lineY1[_index] + sector*(lineY2[_index] - lineY1[_index])/totalSectors;
        float sz1 = lineZ1[_index] + sector*(lineZ2[_index] - lineZ1[_index])/totalSectors;
        float sx2 = lineX1[_index] + (sector+1)*(lineX2[_index] - lineX1[_index])/totalSectors;
        float sy2 = lineY1[_index] + (sector+1)*(lineY2[_index] - lineY1[_index])/totalSectors;
        float sz2 = lineZ1[_index] + (sector+1)*(lineZ2[_index] - lineZ1[_index])/totalSectors;

        projX1[_index] = (short)(sx1 - camCenterX);
        projY1[_index] = (short)(sy1 - camCenterY);
        projZ1[_index] = (short)(sz1 - camCenterZ);
        projX2[_index] = (short)(sx2 - camCenterX);
        projY2[_index] = (short)(sy2 - camCenterY);
        projZ2[_index] = (short)(sz2 - camCenterZ);

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
