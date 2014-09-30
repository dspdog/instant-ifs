package ifs.flat;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

public final class RenderBuffer extends Kernel{

    final static float PFf = (float)Math.PI;

    public final int lineXY1[];
    public final int lineZS1[];
    public final int lineXY2[];
    public final int lineZS2[];

    public final float projX[];
    public final float projY[];
    public final float projZ[];

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

        projX = new float[NUM_LINES];
        projY = new float[NUM_LINES];
        projZ = new float[NUM_LINES];

        lineXY1 = new int[NUM_LINES];
        lineZS1 = new int[NUM_LINES];
        lineXY2 = new int[NUM_LINES];
        lineZS2 = new int[NUM_LINES];

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

    private boolean withinBounds(float x, float y, float x2, float y2){
        return x>1 && y>1 && x<width && y<height &&
               x2>1 && y2>1 && x2<width && y2<height;
    }

    private float length(float X1, float Y1, float X2, float Y2){
        return sqrt((X2-X1)*(X2-X1)+(Y2-Y1)*(Y2-Y1));
    }

    private void drawLine(int _index){
        cameraDistort(_index, 0, 1, false);
        float X1 = projX[_index];
        float Y1 = projY[_index];
        cameraDistort(_index, 0, 1, true);
        float X2 = projX[_index];
        float Y2 = projY[_index];
        float S1, S2, Z1, Z2;

        if(withinBounds(X1, Y1, X2, Y2)){

            int segs = 1;
            for(int i=0; i<segs; i++){
                cameraDistort(_index, i, segs, false);
                X1 = projX[_index];
                Y1 = projY[_index];
                Z1 = projZ[_index];
                cameraDistort(_index, i, segs, true);
                X2 = projX[_index];
                Y2 = projY[_index];
                Z2 = projZ[_index];

                S1 = getS1(_index) + (i)*(getS2(_index) - getS1(_index))/segs;
                S2 = getS1(_index) + (i+1)*(getS2(_index) - getS1(_index))/segs;

                plotLineThick(X1, Y1, X2, Y2, Z1, Z2, S1, S2,0);
            }
        }
    }

    private void plotLineThick(float X1, float Y1, float X2, float Y2, float Z1, float Z2, float S1, float S2, int maxSegments){
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

            int color = (int)(dz/16f);

            if(min(thickness,maxSegments)<1){
                if((pixels[(int)subX1+(int)subY1*width]&255)<=color)
                    pixels[(int)subX1+(int)subY1*width] = argb(255,1,1,(int)color);
            }else{
                float sdx = subX2-subX1;
                float sdy = subY2-subY1;
                float X1normal = -sdy*thickness + subX1;
                float Y1normal =  sdx*thickness + subY1;
                float X2normal =  sdy*thickness + subX1;
                float Y2normal = -sdx*thickness + subY1;

                plotLine(X1normal, Y1normal, X2normal, Y2normal, color, maxSegments);
            }
        }
    }

    private void plotLine(float X1, float Y1, float X2, float Y2, float color, int maxLen){
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

    private void qRotate(int _index, float x, float y, float z, float _a){ //quaternion rotate
        _a/=2;
        float sa2 = sin(_a);
        float ca = cos(_a);
        float qw=ca;
        float qx=x*sa2;
        float qy=y*sa2;
        float qz=z*sa2;

        float _qw = qTimes_W(qw, qx, qy, qz, 0, projX[_index], projY[_index], projZ[_index]);
        float _qx = qTimes_X(qw, qx, qy, qz, 0, projX[_index], projY[_index], projZ[_index]);
        float _qy = qTimes_Y(qw, qx, qy, qz, 0, projX[_index], projY[_index], projZ[_index]);
        float _qz = qTimes_Z(qw, qx, qy, qz, 0, projX[_index], projY[_index], projZ[_index]);
        qw=_qw;qx=_qx;qy=_qy;qz=_qz;

        projX[_index]= qTimes_X(qw, qx, qy, qz, ca, -x*sa2, -y*sa2, -z*sa2);
        projY[_index]= qTimes_Y(qw, qx, qy, qz, ca, -x*sa2, -y*sa2, -z*sa2);
        projZ[_index]= qTimes_Z(qw, qx, qy, qz, ca, -x*sa2, -y*sa2, -z*sa2);
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

    private int getX1(int index){
        return lineXY1[index]>>16;
    }

    private int getY1(int index){
        return lineXY1[index]&65535;
    }

    private int getX2(int index){
        return lineXY2[index]>>16;
    }

    private int getY2(int index){
        return lineXY2[index]&65535;
    }

    private int getZ1(int index){
        return lineZS1[index]>>16;
    }

    private int getS1(int index){
        return lineZS1[index]&65535;
    }

    private int getZ2(int index){
        return lineZS2[index]>>16;
    }

    private int getS2(int index){
        return lineZS2[index]&65535;
    }

    private void cameraDistort(int _index, int sector, int totalSectors, boolean end){
        sector = min(totalSectors-1,sector);

        if(end)sector++;

        projX[_index] = getX1(_index) + sector*(getX2(_index) - getX1(_index))/totalSectors - camCenterX;
        projY[_index] = getY1(_index) + sector*(getY2(_index) - getY1(_index))/totalSectors - camCenterY;
        projZ[_index] = getZ1(_index) + sector*(getZ2(_index) - getZ1(_index))/totalSectors - camCenterZ;

        qRotate(_index, 1.0f, 0.0f, 0.0f, camPitch / 180.0f * PFf);
        qRotate(_index, 0.0f, 1.0f, 0.0f, camYaw / 180.0f * PFf);
        qRotate(_index, 0.0f, 0.0f, 1.0f, camRoll / 180.0f * PFf);

        projX[_index] = projX[_index]*camScale+camCenterX;
        projY[_index] = projY[_index]*camScale+camCenterY;
        projZ[_index] = projZ[_index]*camScale+camCenterZ;

        float vx = width/2; //vanishing pt onscreen
        float vy = height/2;

        if(usePerspective){
            float downScale1=scaleDownDistance(projZ[_index]);
            projX[_index]=((projX[_index]-vx)*downScale1 + vx);
            projY[_index]=((projY[_index]-vy)*downScale1 + vy);
        }
    }

    private float scaleDownDistance(float input){
        return perspectiveScale*0.1f/(float)sqrt(1024f-input);
    }

    public void updateGeometry(){
        this.put(lineXY1).put(lineZS1)
            .put(lineXY2).put(lineZS2);
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

    private void getColor(int x, int y, float gradient){
        int val = pixels[(x)+(y)*width]&255;
        float time = accumTime;
        accumulator[(x)+(y)*width]+=(int) (gradient * val * val / 16f)&255;
        accumulator[x+y*width]*=(1.0f-1.0f/time);

        pixels[(x)+(y)*width] = gray((int)(accumulator[x+y*width]/time));

    }

    private void putNormal(int x, int y){
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

    private void putX(int x, int y){
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
    private void putCircle(int x, int y){
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
