package ifs.flat;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

public final class RenderBuffer extends Kernel{

    final static float PFf = (float)Math.PI;
    /*
    public final long TBuffer[];
    public final float ZBuffer[];
    public final float SBuffer[];
    public final float RBuffer[];
    public final float GBuffer[];
    public final float BBuffer[];

    public final float postRBuffer[];
    public final float postGBuffer[];
    public final float postBBuffer[];

    public final float postZBuffer[];
    */

    public final short lineX1[];
    public final short lineY1[];
    public final short lineZ1[];
    public final short lineS1[];
    public final short lineX2[];
    public final short lineY2[];
    public final short lineZ2[];
    public final short lineS2[];

    public final short projX1[];
    public final short projY1[];
    public final short projZ1[];
    public final short projX2[];
    public final short projY2[];
    public final short projZ2[];


    public final int pixels[];
    public final short zbuffer[];

    public int lineIndex = 0;
    public int lastLineIndex =0;

    boolean cartoon=false;

    final boolean shading = true;

    float brightness = 1.0f;

    int width, height;

    float maxColor = 0;

    public float camPitch, camYaw, camRoll, camScale, camCenterX, camCenterY, camCenterZ;

    private static long time = System.currentTimeMillis();
    private static long frameStartTime = System.currentTimeMillis();

    public long frameNum=0;

    public static int shutterSpeed = 50;

    public int mode = 0; //z-process

    public float scaleUp =1.0f;

    public boolean addSamples=true;
    public boolean usePerspective = true;

    final int NUM_LINES = 1024*1024/4;

    public int totalLines;

    public RenderBuffer(int w, int h){
        totalLines=100;
        width=1024; height=1024;

        projX1 = new short[NUM_LINES];
        projY1 = new short[NUM_LINES];
        projZ1 = new short[NUM_LINES];
        projX2 = new short[NUM_LINES];
        projY2 = new short[NUM_LINES];
        projZ2 = new short[NUM_LINES];

        lineX1 = new short[NUM_LINES];
        lineY1 = new short[NUM_LINES];
        lineZ1 = new short[NUM_LINES];
        lineS1 = new short[NUM_LINES];

        lineX2 = new short[NUM_LINES];
        lineY2 = new short[NUM_LINES];
        lineZ2 = new short[NUM_LINES];
        lineS2 = new short[NUM_LINES];

        clearZProjection();
        cartoon=false;
        pixels=new int[width*height];
        zbuffer= new short[width*height];
        addSamples=true;

        camPitch=0;
        camYaw=0;
        camRoll=0;

        this.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
    }

    public void clearZProjection(){
        maxColor=0;
    }

    public void updateTime(long _time){
        time = System.currentTimeMillis();
        frameStartTime=_time;
    }

    public boolean putPixel(float x, float y, float z, float R, float G, float B, float scale, float dark, boolean rightEye){
        /*
        x=Math.max((int) x, 0);
        x=Math.min((int)x, width-1);
        y=Math.max((int)y, 0);
        y=Math.min((int)y, height-1);

        if(ZBuffer[(int)x+(int)y*width] < z){
            RBuffer[(int)x+(int)y*width] = R*dark;
            GBuffer[(int)x+(int)y*width] = G*dark;
            BBuffer[(int)x+(int)y*width] = B*dark;
            ZBuffer[(int)x+(int)y*width] = z;
            TBuffer[(int)x+(int)y*width] = time;
            SBuffer[(int)x+(int)y*width] = scale;
            return true;
        }else{
            return false;
        }*/
        return false;
    }

    public void drawDot(int _index){

        projX1[_index] = lineX1[_index];
        projY1[_index] = lineY1[_index];
        projZ1[_index] = lineZ1[_index];
        projX2[_index] = lineX2[_index];
        projY2[_index] = lineY2[_index];
        projZ2[_index] = lineZ2[_index];

        cameraDistort(_index);

        float dx = projX1[_index];
        float dy = projY1[_index];

        int x = min(max((int) dx, 1), width - 1);
        int y = min(max((int) dy, 1), height - 1);
        int grayval = (int)((projZ1[_index]*projZ1[_index]/255/16));

        if((pixels[x+y*width]&255)<grayval)
            pixels[x+y*width] = gray((int)(grayval));
    }

    public float distanceProj(int _index){
        float x = projX2[_index]-projX1[_index];
        float y = projY2[_index]-projY1[_index];
        return sqrt(x*x+y*y);
    }

    public boolean lineValid(int _index){ //TODO clip lines instead of not draw
        return (projX1[_index]>1 && projY1[_index]>1 && projX2[_index]>1 && projY2[_index]>1) &&
                (projX1[_index]<width && projY1[_index]<height && projX2[_index]<width && projY2[_index]<height);
    }

    public void drawLine(int _index){
        projX1[_index] = lineX1[_index];
        projY1[_index] = lineY1[_index];
        projZ1[_index] = lineZ1[_index];
        projX2[_index] = lineX2[_index];
        projY2[_index] = lineY2[_index];
        projZ2[_index] = lineZ2[_index];

        cameraDistort(_index);

        if(lineValid(_index)){
            float mag = distanceProj(_index);
            mag = min(max(mag, 1), 1024);
            for(int i=0; i<mag; i++){
                float dx = projX1[_index] + i*(projX2[_index] - projX1[_index])/mag;
                float dy = projY1[_index] + i*(projY2[_index] - projY1[_index])/mag;
                float dz = projZ1[_index] + i*(projZ2[_index] - projZ1[_index])/mag;
                float ds = lineS1[_index] + i*(lineS2[_index] - lineS1[_index])/mag;

                int x = min(max((int) dx, 1), width - 1);
                int y = min(max((int) dy, 1), height - 1);
                //dz = max(dz, pixels[x+y*width]&255);

                int grayval = (int)((dz*dz/255/16));

                //ds = max((pixels[x+y*width]>>8)&255, ds);

                if((pixels[x+y*width]&255)<grayval)
                    pixels[x+y*width] = argb(255,0,(int)(ds),grayval);
            }
        }
    }

    public void rotate(int _index, float x, float y, float z, float _a){ //quaternion rotate
         _a/=2;
        float sa2 = sin(_a);

        float qw=cos(_a);
        float qx=x*sa2;
        float qy=y*sa2;
        float qz=z*sa2;

        float _qw;
        float _qx;
        float _qy;
        float _qz;

        _qw = qTimes_W(qw, qx, qy, qz, 0, projX1[_index], projY1[_index], projZ1[_index]);
        _qx = qTimes_X(qw, qx, qy, qz, 0, projX1[_index], projY1[_index], projZ1[_index]);
        _qy = qTimes_Y(qw, qx, qy, qz, 0, projX1[_index], projY1[_index], projZ1[_index]);
        _qz = qTimes_Z(qw, qx, qy, qz, 0, projX1[_index], projY1[_index], projZ1[_index]);

        qw=_qw;qx=_qx;qy=_qy;qz=_qz;

        //_qw = qTimes_W(qw, qx, qy, qz, cos(_a), -x*sa2, -y*sa2, -z*sa2);
        _qx = qTimes_X(qw, qx, qy, qz, cos(_a), -x*sa2, -y*sa2, -z*sa2);
        _qy = qTimes_Y(qw, qx, qy, qz, cos(_a), -x*sa2, -y*sa2, -z*sa2);
        _qz = qTimes_Z(qw, qx, qy, qz, cos(_a), -x*sa2, -y*sa2, -z*sa2);

        projX1[_index]= (short)_qx;
        projY1[_index]= (short)_qy;
        projZ1[_index]= (short)_qz;
    }

    public void lineRotate(int _index, float x, float y, float z, float _a){ //quaternion rotate
        _a/=2;
        float sa2 = sin(_a);

        float qw=cos(_a);
        float qx=x*sa2;
        float qy=y*sa2;
        float qz=z*sa2;
        float qw2=cos(_a);
        float qx2=x*sa2;
        float qy2=y*sa2;
        float qz2=z*sa2;

        float _qw;
        float _qx;
        float _qy;
        float _qz;

        float _qw2;
        float _qx2;
        float _qy2;
        float _qz2;

        _qw = qTimes_W(qw, qx, qy, qz, 0, projX1[_index], projY1[_index], projZ1[_index]);
        _qx = qTimes_X(qw, qx, qy, qz, 0, projX1[_index], projY1[_index], projZ1[_index]);
        _qy = qTimes_Y(qw, qx, qy, qz, 0, projX1[_index], projY1[_index], projZ1[_index]);
        _qz = qTimes_Z(qw, qx, qy, qz, 0, projX1[_index], projY1[_index], projZ1[_index]);

        _qw2 = qTimes_W(qw, qx, qy, qz, 0, projX2[_index], projY2[_index], projZ2[_index]);
        _qx2 = qTimes_X(qw, qx, qy, qz, 0, projX2[_index], projY2[_index], projZ2[_index]);
        _qy2 = qTimes_Y(qw, qx, qy, qz, 0, projX2[_index], projY2[_index], projZ2[_index]);
        _qz2 = qTimes_Z(qw, qx, qy, qz, 0, projX2[_index], projY2[_index], projZ2[_index]);


        qw=_qw;qx=_qx;qy=_qy;qz=_qz;
        qw2=_qw2;qx2=_qx2;qy2=_qy2;qz2=_qz2;

        //_qw = qTimes_W(qw, qx, qy, qz, cos(_a), -x*sa2, -y*sa2, -z*sa2);
        _qx = qTimes_X(qw, qx, qy, qz, cos(_a), -x*sa2, -y*sa2, -z*sa2);
        _qy = qTimes_Y(qw, qx, qy, qz, cos(_a), -x*sa2, -y*sa2, -z*sa2);
        _qz = qTimes_Z(qw, qx, qy, qz, cos(_a), -x*sa2, -y*sa2, -z*sa2);

        _qx2 = qTimes_X(qw2, qx2, qy2, qz2, cos(_a), -x*sa2, -y*sa2, -z*sa2);
        _qy2 = qTimes_Y(qw2, qx2, qy2, qz2, cos(_a), -x*sa2, -y*sa2, -z*sa2);
        _qz2 = qTimes_Z(qw2, qx2, qy2, qz2, cos(_a), -x*sa2, -y*sa2, -z*sa2);

        projX1[_index]= (short)_qx;
        projY1[_index]= (short)_qy;
        projZ1[_index]= (short)_qz;

        projX2[_index]= (short)_qx2;
        projY2[_index]= (short)_qy2;
        projZ2[_index]= (short)_qz2;
    }

    public float qTimes_W(float aW, float aX, float aY, float aZ, float w, float x, float y, float z){
        return aW *w - aX *x - aY *y - aZ *z;
    }

    public float qTimes_X(float aW, float aX, float aY, float aZ, float w, float x, float y, float z){
        return aW *x + aX *w + aY *z - aZ *y;
    }

    public float qTimes_Y(float aW, float aX, float aY, float aZ, float w, float x, float y, float z){
        return aW *y - aX *z + aY *w + aZ *x;
    }

    public float qTimes_Z(float aW, float aX, float aY, float aZ, float w, float x, float y, float z){
        return aW *z + aX *y - aY *x + aZ *w;
    }

    public void cameraDistort(int _index){
        //old code
        // pt = _pt
        //--         .subtract(camCenter)
        //         .getRotatedPt_Right(camPitch / 180.0f * PFf, camYaw / 180.0f * PFf, camRoll / 180.0f * PFf)
        //--         .scale(camScale)
        //--         .add(camCenter);

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

        float vx = 512.0f; //vanishing pt onscreen
        float vy = 512.0f;
        float perspectiveScale=200f;

        if(usePerspective){
            float downScale1=perspectiveScale*0.1f/(float)sqrt(1024f-projZ1[_index]);
            float downScale2=perspectiveScale*0.1f/(float)sqrt(1024f-projZ2[_index]);
            projX1[_index]=(short)((projX1[_index]-vx)*downScale1 + vx);
            projY1[_index]=(short)((projY1[_index]-vy)*downScale1 + vy);
            projX2[_index]=(short)((projX2[_index]-vx)*downScale2 + vx);
            projY2[_index]=(short)((projY2[_index]-vy)*downScale2 + vy);
        }
    }

    public void generatePixels(float _brightness, boolean useShadows, boolean rightEye, boolean _putSamples, float _size, float pitch, float yaw, float roll, boolean _usePerspective,
                               float scale, float camX, float camY, float camZ){
        camPitch=pitch;
        camRoll=roll;
        camYaw=yaw;
        camCenterX = camX;
        camCenterY = camY;
        camCenterZ = camZ;
        camScale = scale;
        usePerspective = _usePerspective;

        cartoon=useShadows;
        addSamples=_putSamples;
        brightness=_brightness;
        scaleUp=_size;

        Range range = Range.create2D(width,height);

        this.execute(range, 4);

        frameNum++;

        if(frameNum%1000==0){
            System.out.println(this.getExecutionMode().toString() + " " + this.getExecutionTime());
        }
    }

    @Override
    public void run() {
        int x = getGlobalId(0);
        int y = getGlobalId(1);

        if(getPassId()==0){ //clear frame
            pixels[x+y*width]=black();
            zbuffer[(x)+(y)*width]=0;
        }
        if(getPassId()==1){ //draw skeleton
            int myLineIndex = x+y*width;
            if(myLineIndex<NUM_LINES && myLineIndex<totalLines)
                drawLine(myLineIndex);
        }
        if(getPassId()==2){ //draw flesh
            putSprite(x, y);
        }
        if(getPassId()==3){ //z-process

            float gradient=1.0f;

            float gms = x>1&&y>1&&x<width-1&&y<height-1 ? getMaxSlope(x,y)/16f : 0;
            float maxslope = min(gms*255f, 255f);

            gradient = shading ? 1.0f-maxslope/255.0f : 1.0f;

            getColor(x, y, gradient);
        }
    }

    int getMaxSlope(int x, int y){
        int central =  (zbuffer[x+y*width]);
        int maxslope1 = max(max(zbuffer[(x - 1) + (y) * width] - central, zbuffer[(x + 1) + (y) * width] - central),
                        max(zbuffer[(x) + (y - 1) * width] - central, zbuffer[(x) + (y + 1) * width] - central));
        int maxslope2 = (int)(1.0f/sqrt(2.0f)*max(max(zbuffer[(x - 1) + (y - 1) * width] - central, zbuffer[(x + 1) + (y + 1) * width] - central),
                        max(zbuffer[(x - 1) + (y + 1) * width] - central, zbuffer[(x + 1) + (y - 1) * width] - central)));
        return max(maxslope2, maxslope1);
    }

    void getColor(int x, int y, float gradient){
        pixels[(x)+(y)*width] = gray((int)(gradient*zbuffer[(x)+(y)*width]));
    }

    void putSprite(int x, int y){
        int size = (pixels[(x)+(y)*width]>>8)&255; //size is green channel
        size=(int)(size/8.0);
        size = max(1, min(size, 8));
        if(x>size && y>size && x<(width-size) && y<(height-size)){
            short startingVal=(short)(pixels[(x)+(y)*width]&255);//depth is blue channe 
            if(startingVal>0)
            for(int _x=-size; _x<size+1; _x++){
                for(int _y=-size; _y<size+1; _y++){
                    if(_x*_x+_y*_y<size*size && startingVal>zbuffer[(x+_x)+(y+_y)*width]){
                        zbuffer[(x+_x)+(y+_y)*width]=startingVal;
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
