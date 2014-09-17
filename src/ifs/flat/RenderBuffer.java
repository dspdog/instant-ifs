package ifs.flat;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

public final class RenderBuffer extends Kernel{

    final static float PFf = (float)Math.PI;
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

    public final float lineX1[];
    public final float lineY1[];
    public final float lineZ1[];
    public final float lineX2[];
    public final float lineY2[];
    public final float lineZ2[];
    public final float lineMag[];
    public int lineIndex = 0;
    public int lastLineIndex =0;


    public final float projX1[];
    public final float projY1[];
    public final float projZ1[];

    public final int pixels[];

    boolean cartoon=false;

    final boolean shading = true;

    float brightness = 1.0f;

    int width, height;

    float maxColor = 0;

    public float camPitch, camYaw, camRoll;

    private static long time = System.currentTimeMillis();
    private static long frameStartTime = System.currentTimeMillis();

    public long frameNum=0;

    public static int shutterSpeed = 50;

    public int mode = 0; //z-process

    public float scaleUp =1.0f;

    public boolean addSamples=true;
    public boolean usePerspective = true;

    public RenderBuffer(int w, int h){
        width=1024; height=1024;
        RBuffer = new float[width*height];
        GBuffer = new float[width*height];
        BBuffer = new float[width*height];

        SBuffer = new float[width*height];
        ZBuffer = new float[width*height];
        TBuffer = new long[width*height];

        postZBuffer = new float[width*height];
        postRBuffer = new float[width*height];
        postGBuffer = new float[width*height];
        postBBuffer = new float[width*height];

        projX1 = new float[width*height];
        projY1 = new float[width*height];
        projZ1 = new float[width*height];

        lineX1 = new float[width*height];
        lineY1 = new float[width*height];
        lineZ1 = new float[width*height];
        lineX2 = new float[width*height];
        lineY2 = new float[width*height];
        lineZ2 = new float[width*height];
        lineMag = new float[width*height];

        clearZProjection();
        cartoon=false;
        pixels=new int[width*height];
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
        }
    }

    public void generatePixels(float _brightness, boolean useShadows, boolean rightEye, boolean _putSamples, float _size, float pitch, float yaw, float roll, boolean _usePerspective){
        camPitch=pitch;
        camRoll=roll;
        camYaw=yaw;
        usePerspective = _usePerspective;

        cartoon=useShadows;
        addSamples=_putSamples;
        brightness=_brightness;
        scaleUp=_size;

        Range range = Range.create2D(width,height);

        this.execute(range, 2);

        frameNum++;

        if(frameNum%1000==0){
            System.out.println(this.getExecutionMode().toString() + " " + this.getExecutionTime());
        }
    }

    public void drawDot(int _index){

        projX1[_index] = lineX1[_index];
        projY1[_index] = lineY1[_index];
        projZ1[_index] = lineZ1[_index];

        cameraDistort(_index);

        float dx = projX1[_index];
        float dy = projY1[_index];

        int x = min(max((int) dx, 1), width - 1);
        int y = min(max((int) dy, 1), height - 1);

        pixels[x+y*width] = white();
    }

    public void drawLine(int _index){
        float mag = lineMag[_index];
        mag = min(max(mag, 1), 1024);
        for(int i=0; i<mag; i++){
            float dx = lineX1[_index] + i*(lineX2[_index] - lineX1[_index])/mag;
            float dy = lineY1[_index] + i*(lineY2[_index] - lineY1[_index])/mag;
            float dz = lineZ1[_index] + i*(lineZ2[_index] - lineZ1[_index])/mag;


            int x = min(max((int) dx, 1), width - 1);
            int y = min(max((int) dy, 1), height - 1);
            dz = max(dz, pixels[x+y*width]&255);

            pixels[x+y*width] = gray(dz);
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

        projX1[_index]= _qx;
        projY1[_index]= _qy;
        projZ1[_index]= _qz;
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

        //params
            float camCenterX = 512f;
            float camCenterY = 512f;
            float camCenterZ = 512f;
            float camScale = 1.0f;

        projX1[_index] = lineX1[_index] - camCenterX;
        projY1[_index] = lineY1[_index] - camCenterY;
        projZ1[_index] = lineZ1[_index] - camCenterZ;

        rotate(_index, 1.0f, 0.0f, 0.0f, camPitch / 180.0f * PFf);
        rotate(_index, 0.0f, 1.0f, 0.0f, camYaw / 180.0f * PFf);
        rotate(_index, 0.0f, 0.0f, 1.0f, camRoll / 180.0f * PFf);

        projX1[_index] = projX1[_index]*camScale+camCenterX;
        projY1[_index] = projY1[_index]*camScale+camCenterY;
        projZ1[_index] = projZ1[_index]*camScale+camCenterZ;

        float vx = 512.0f; //vanishing pt onscreen
        float vy = 512.0f;
        float perspectiveScale=200f;

        if(usePerspective){
            float downScale=perspectiveScale*0.1f/(float)sqrt(1024f-projZ1[_index]);
            projX1[_index]=(projX1[_index]-vx)*downScale + vx;
            projY1[_index]=(projY1[_index]-vy)*downScale + vy;
        }
    }

    @Override
    public void run() {
        int x = getGlobalId(0);
        int y = getGlobalId(1);

        if(getPassId()==0){
            pixels[x+y*width]=black();
        }
        if(getPassId()==1){
            int myLineIndex = x+y*width;
            if(myLineIndex<min(lineIndex, 100000))
                drawDot(myLineIndex);
        }
    }

    int black(){
        int _argb = 255;

        _argb = (_argb << 8) + 1;
        _argb = (_argb << 8) + 1;
        _argb = (_argb << 8) + 1;

        return _argb;
    }

    int white(){
        int _argb = 255;

        _argb = (_argb << 8) + 255;
        _argb = (_argb << 8) + 255;
        _argb = (_argb << 8) + 255;

        return _argb;
    }

    int gray(float _g){
        int g = (int)_g;

        g = max(min(g, 255), 1);

        int _argb = 255;

        g=max(1, min(g, 255))  ;
        _argb = (_argb << 8) + g;
        _argb = (_argb << 8) + g;
        _argb = (_argb << 8) + g;

        return _argb;
    }
}
