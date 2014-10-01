package ifs;

import com.alee.extended.colorchooser.WebGradientColorChooser;
import ifs.flat.RenderBuffer;

import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;

final class volume {
    public static enum RenderMode {
        VOLUMETRIC, PROJECT_ONLY
    }

    int width, height, depth;

    public RenderMode renderMode = RenderMode.PROJECT_ONLY;

    long drawTime = 0;
    long totalSamples = 0;

    float totalSamplesAlpha =0;

    public long dataPoints = 0;

    float camRoll;
    float camYaw;
    float camPitch;
    float camScale;

    ifsPt camCenter;

    float savedPitch;
    float savedYaw;
    float savedRoll;

    ifsPt centroid;
    ifsPt highPt;
    ifsPt highPtVolumetric;

    boolean antiAliasing;
    boolean usePerspective;

    boolean useZBuffer = true;

    float zDarkenScaler;

    double accumilatedDistance = 0;
    long averageDistanceSamples = 0;
    double averageDistance = 0;

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
        theTriangles = new LinkedList<ifsTriangle>();
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

        colorPeriod = 512;

        reset();
    }

    public void reset(){

        drawTime=System.currentTimeMillis();
        totalSamples=1;
        totalSamplesAlpha =1;

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

    public void contributeToAverageDistance(double dist){
        accumilatedDistance+=dist;
        averageDistanceSamples++;
        averageDistance = accumilatedDistance/averageDistanceSamples;
    }

    public float getScore(ScoreParams sp){
        float avD = (float)averageDistance;
        float AvDS = (float)averageDistance/mySurfaceArea;
        float AvDV = (float)averageDistance/myVolume;
        float SV = (float)mySurfaceArea/myVolume;

        float score = myVolume * sp.volumeScale +
                      mySurfaceArea * sp.surfaceScale +
                      avD*sp.avD_Scale +
                      AvDS * sp.AvDS_Scale +
                      AvDV * sp.AvDV_Scale +
                      SV * sp.SV_Scale;
        return score;
    }

    public boolean volumeContains(ifsPt pt){
        return (pt.x>1 && pt.y>1 && pt.z>1 && pt.x<width-1 && pt.y<height-1);
    }

    public boolean croppedVolumeContains(ifsPt pt, RenderParams rp){
        return (pt.x>=rp.xMin && pt.y>=rp.yMin && pt.z>=rp.zMin && pt.x<=rp.xMax && pt.y<=rp.yMax && pt.z<=rp.zMax);
    }

    final static float PFf = (float)Math.PI;

    float colorPeriod = 0;

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

    protected void addCubeTriangles(int x, int y, int z) {

        myVolume++;

        boolean x1Valid=true;
        boolean y1Valid=true;
        boolean z1Valid=true;

        boolean xn1Valid=true;
        boolean yn1Valid=true;
        boolean zn1Valid=true;

        if(x1Valid && y1Valid && z1Valid && xn1Valid && yn1Valid && zn1Valid){}//skip "surrounded" cubes
        else{
            if(!zn1Valid){   //XY plane1
                mySurfaceArea++;
                theTriangles.add(new ifsTriangle(new ifsPt(x,y,z), new ifsPt(x,y+1,z), new ifsPt(x+1,y+1,z)));
                theTriangles.add(new ifsTriangle(new ifsPt(x,y,z), new ifsPt(x+1,y,z), new ifsPt(x+1,y+1,z)));
            }

            if(!z1Valid){   //XY plane2
                mySurfaceArea++;
                theTriangles.add(new ifsTriangle(new ifsPt(x,y,z+1), new ifsPt(x,y+1,z+1), new ifsPt(x+1,y+1,z+1)));
                theTriangles.add(new ifsTriangle(new ifsPt(x,y,z+1), new ifsPt(x+1,y,z+1), new ifsPt(x+1,y+1,z+1)));
            }

            if(!yn1Valid){   //XZ plane1
                mySurfaceArea++;
                theTriangles.add(new ifsTriangle(new ifsPt(x,y,z), new ifsPt(x+1,y,z), new ifsPt(x+1,y,z+1)));
                theTriangles.add(new ifsTriangle(new ifsPt(x,y,z), new ifsPt(x+1,y,z+1), new ifsPt(x,y,z+1)));
            }

            if(!y1Valid){   //XZ plane2
                mySurfaceArea++;
                theTriangles.add(new ifsTriangle(new ifsPt(x,y+1,z), new ifsPt(x+1,y+1,z), new ifsPt(x+1,y+1,z+1)));
                theTriangles.add(new ifsTriangle(new ifsPt(x,y+1,z), new ifsPt(x+1,y+1,z+1), new ifsPt(x,y+1,z+1)));
            }

            if(!xn1Valid){   //ZY plane1
                mySurfaceArea++;
                theTriangles.add(new ifsTriangle(new ifsPt(x,y,z), new ifsPt(x,y,z+1), new ifsPt(x,y+1,z+1)));
                theTriangles.add(new ifsTriangle(new ifsPt(x,y,z), new ifsPt(x,y+1,z), new ifsPt(x,y+1,z+1)));
            }

            if(!x1Valid){   //ZY plane2
                mySurfaceArea++;
                theTriangles.add(new ifsTriangle(new ifsPt(x+1,y,z), new ifsPt(x+1,y,z+1), new ifsPt(x+1,y+1,z+1)));
                theTriangles.add(new ifsTriangle(new ifsPt(x+1,y,z), new ifsPt(x+1,y+1,z), new ifsPt(x+1,y+1,z+1)));
            }
        }
    }

    public class ifsTriangle {
        ifsPt[] pts = new ifsPt[3];
        public ifsTriangle(ifsPt p1, ifsPt p2, ifsPt p3){
            pts[0]=new ifsPt(p1);
            pts[1]=new ifsPt(p2);
            pts[2]=new ifsPt(p3);
        }
    }

    public void _saveToBinarySTL(){
        mySurfaceArea=0;
        myVolume=0;

        String timeLog = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(startDate)+ ".stl";
        //File logFile = new File(timeLog);

        for(int _x=1; _x<width-1;_x++){
            for(int _y=1; _y<height-1;_y++){
                for(int _z=1; _z<depth-1;_z++){
                    //boolean currentValid=volume.isNotEmpty(_x,_y,_z);
                    //if(currentValid){
                    //    addCubeTriangles( _x, _y, _z);
                    //}
                }
            }

            if(_x%16==0){
                System.out.println(_x + "/" + width + " generated - " + (int)(100.0*_x/width)+"%");
            }
        }

        System.out.println("saving stl...");
        byte[] title = new byte[80];

        try(FileChannel ch=new RandomAccessFile(timeLog , "rw").getChannel())
        {
            int totalTriangles = theTriangles.size();

            ByteBuffer bb= ByteBuffer.allocate(10000).order(ByteOrder.LITTLE_ENDIAN);
            bb.put(title); // Header (80 bytes)
            bb.putInt(totalTriangles); // Number of triangles (UINT32)
            int triNo=0;
            for(ifsTriangle tri : theTriangles){
                if(triNo<totalTriangles){
                    bb.putFloat(0).putFloat(0).putFloat(0); //TODO normals?
                    bb.putFloat(tri.pts[0].x).putFloat(tri.pts[0].y).putFloat(tri.pts[0].z);
                    bb.putFloat(tri.pts[1].x).putFloat(tri.pts[1].y).putFloat(tri.pts[1].z);
                    bb.putFloat(tri.pts[2].x).putFloat(tri.pts[2].y).putFloat(tri.pts[2].z);
                    bb.putShort((short)12); //TODO colors?

                    bb.flip();
                    ch.write(bb);
                    bb.clear();

                    triNo++;
                    if(triNo%256==0){
                        System.out.println("TRI "+triNo+"/"+totalTriangles + " saved - " + (int)(100.0*triNo/totalTriangles)+"%");
                    }
                }
            }
            ch.close();

            System.out.println("done! " + timeLog);

            System.out.println("SURFACE " + mySurfaceArea);
            System.out.println("VOLUME " + myVolume);
            System.out.println("TRIANGLES " + theTriangles.size());

        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
