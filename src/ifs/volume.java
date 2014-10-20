package ifs;

import java.util.*;

final class volume {
    public static enum RenderMode {
        VOLUMETRIC, PROJECT_ONLY
    }

    int width, height, depth;

    public RenderMode renderMode = RenderMode.PROJECT_ONLY;

    long drawTime = 0;
    long totalSamples = 0;

    float camRoll;
    float camYaw;
    float camPitch;
    float camScale;

    ifsPt camCenter;

    float savedPitch;
    float savedYaw;
    float savedRoll;

    boolean antiAliasing;
    boolean usePerspective;

    float zDarkenScaler;

    double accumilatedDistance = 0;
    long averageDistanceSamples = 0;

    Date startDate;

    long myVolume, mySurfaceArea;
    long myVolumeOneSecondAgo, myVolumeChange;

    LinkedList<ifsTriangle> theTriangles;

    boolean changed;
    boolean doneClearing = true;

    public volume(int w, int h, int d){
        doneClearing=true;
        myVolumeOneSecondAgo=0;
        myVolumeChange = 0;
        changed=false;
        theTriangles = new LinkedList<>();
        myVolume=0;
        mySurfaceArea=0;

        startDate = Calendar.getInstance().getTime();

        width = w;
        height = h;
        depth = d;

        renderMode = RenderMode.PROJECT_ONLY;
        antiAliasing = true;

        camPitch=0;
        camRoll=-90;
        camYaw=0;

        usePerspective=true;

        camScale=2.0f;

        camCenter = new ifsPt(512.0f,512.0f,512.0f);

        zDarkenScaler=512f;

        reset();
    }

    public void reset(){

        drawTime=System.currentTimeMillis();
        totalSamples=1;

        if(renderMode == renderMode.VOLUMETRIC){
            mySurfaceArea=0;
            myVolume=0;

        }
    }

    public void clear(){
        doneClearing=false;
        //System.out.println("clear volume " + System.currentTimeMillis());
        accumilatedDistance = 0;
        averageDistanceSamples = 0;

        reset();
        changed=false;
        doneClearing=true;
     }

    public boolean volumeContains(ifsPt pt){
        return (pt.x>1 && pt.y>1 && pt.z>1 && pt.x<width-1 && pt.y<height-1);
    }

    final static float PFf = (float)Math.PI;

    public ifsPt getCameraDistortedPt(ifsPt _pt, boolean rightEye, float perspectiveScale){
        ifsPt pt;

        if(rightEye){
            pt = _pt
                    .subtract(camCenter)
                    .getRotatedPt_Right(camPitch / 180.0f * PFf, camYaw / 180.0f * PFf, camRoll / 180.0f * PFf)
                    .scale(camScale)
                    .add(camCenter);

        }else{
            pt = _pt
                    .subtract(camCenter)
                    .getRotatedPt_Left(camPitch / 180.0f * PFf, camYaw / 180.0f * PFf, camRoll / 180.0f * PFf)
                    .scale(camScale)
                    .add(camCenter);

        }

        float vx = 512.0f; //vanishing pt onscreen
        float vy = 512.0f;

        if(usePerspective){
            float downScale=perspectiveScale*0.1f/(float)Math.sqrt(1024f-pt.z);
            pt.x = (pt.x-vx)*downScale + vx;
            pt.y = (pt.y-vy)*downScale + vy;
        }

        pt.z /= 8.0;
        pt.z = Math.min(pt.z, 1020);
        pt.z = Math.max(pt.z, 4);

        return pt;
    }

    public void saveCam(){
        camCenter.saveState();
        savedPitch = camPitch;
        savedYaw = camYaw;
        savedRoll = camRoll;
    }

    public class ifsTriangle {
        ifsPt[] pts = new ifsPt[3];
        public ifsTriangle(ifsPt p1, ifsPt p2, ifsPt p3){
            pts[0]=new ifsPt(p1);
            pts[1]=new ifsPt(p2);
            pts[2]=new ifsPt(p3);
        }
    }
}
