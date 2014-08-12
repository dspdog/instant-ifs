package ifs;

import ifs.volumetric.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;

public class volume {
    public static enum RenderMode {
        VOLUMETRIC, PROJECT_ONLY
    }

    int width, height, depth;

    public RenderMode renderMode = RenderMode.PROJECT_ONLY;

    long drawTime = 0;
    long totalSamples = 0;

    float totalSamplesAlpha =0;
    float perspectiveScale;

    public SmartVolume volume;

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

    float minX, minY, minZ, maxX, maxY, maxZ;

    public volume(int w, int h, int d){
        myVolumeOneSecondAgo=0;
        myVolumeChange = 0;

        theTriangles = new LinkedList<ifsTriangle>();
        myVolume=0;
        mySurfaceArea=0;
        perspectiveScale=160f;
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

        volume = new SmartVolume(width);

        zDarkenScaler=512f;


        reset();
    }

    public void reset(){

        minX=width;
        minY=height;
        minZ=depth;

        maxX=0;
        maxY=0;
        maxZ=0;

        drawTime=System.currentTimeMillis();
        totalSamples=1;
        totalSamplesAlpha =1;

        if(renderMode == renderMode.VOLUMETRIC){
            mySurfaceArea=0;
            myVolume=0;
            volume.reset();
        }
    }

    public void clear(){
        accumilatedDistance = 0;
        averageDistanceSamples = 0;
        reset();
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
        //return (pt.x>1 && pt.y>1 && pt.z>1 && pt.x<width-1 && pt.y<height-1);
        return (pt.x>=rp.xMin && pt.y>=rp.yMin && pt.z>=rp.zMin && pt.x<=rp.xMax && pt.y<=rp.yMax && pt.z<=rp.zMax);
    }

    public boolean volumeContains(ifsPt pt, int prune){
        return (pt.x>prune && pt.y>prune && pt.z>prune && pt.x<width-prune && pt.y<height-prune);
    }

    final static float PFf = (float)Math.PI;
    public ifsPt getCameraDistortedPt(ifsPt _pt){

        ifsPt pt = _pt
                .subtract(camCenter)
                .getRotatedPt(camPitch / 180.0f * PFf, camYaw / 180.0f * PFf, camRoll / 180.0f * PFf)
                .scale(camScale)
                .add(camCenter);

        //pt.x += Math.random()*Math.random()*(pt.z-depth/2)/(depth/2)*250;
        //pt.y += Math.random()*Math.random()*(pt.z-depth/2)/(depth/2)*250;

        float vx = 512.0f; //vanishing pt onscreen
        float vy = 512.0f;
       // pt.z = Math.sqrt(pt.z)*16;

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

    public boolean putPixel(ifsPt _pt, float ptR, float ptG, float ptB, RenderParams rp, boolean useCrop, ifsys is){
        return old_putPixel(_pt,ptR, ptG, ptB, rp, useCrop, false, false, is);
    }

    public boolean putPixel(ifsPt _pt, float ptR, float ptG, float ptB, RenderParams rp, boolean useCrop, boolean noDark, boolean noVolumetric, ifsys is){
        return old_putPixel(_pt, ptR, ptG, ptB, rp, useCrop, noDark, noVolumetric, is);
    }
    public void putDataUpdateSurfaceVolume(ifsPt _pt){
        if(volume.putData((int) _pt.x, (int) _pt.y, (int) _pt.z, 1)==1){//if its the first point there
            myVolume++; //add it to ifs.volume
            if(volume.getData((int)_pt.x+1, (int)_pt.y, (int)_pt.z)>0){mySurfaceArea--;}else{mySurfaceArea++;}
            if(volume.getData((int)_pt.x,   (int)_pt.y+1, (int)_pt.z)>0){mySurfaceArea--;}else{mySurfaceArea++;}
            if(volume.getData((int)_pt.x,   (int)_pt.y, (int)_pt.z+1)>0){mySurfaceArea--;}else{mySurfaceArea++;}
            if(volume.getData((int)_pt.x-1, (int)_pt.y, (int)_pt.z)>0){mySurfaceArea--;}else{mySurfaceArea++;}
            if(volume.getData((int)_pt.x,   (int)_pt.y-1, (int)_pt.z)>0){mySurfaceArea--;}else{mySurfaceArea++;}
            if(volume.getData((int)_pt.x,   (int)_pt.y, (int)_pt.z-1)>0){mySurfaceArea--;}else{mySurfaceArea++;}
        }
    }

    public void pushBounds(ifsPt pt){
        if(pt.x<minX){minX=pt.x;}
        if(pt.y<minY){minY=pt.y;}
        if(pt.z<minZ){minZ=pt.z;}
        if(pt.x>maxX){maxX=pt.x;}
        if(pt.y>maxY){maxY=pt.y;}
        if(pt.z>maxZ){maxZ=pt.z;}
    }

    public boolean old_putPixel(ifsPt _pt, float ptR, float ptG, float ptB, RenderParams rp, boolean useCrop, boolean noDark, boolean noVolumetric, ifsys myIfSys){
        ifsPt pt = getCameraDistortedPt(_pt);

        dataPoints++;
        float dark = pt.z/zDarkenScaler;
        if(noDark){dark = 1.0f;}
        boolean doDraw = false;

        if(useCrop){doDraw=croppedVolumeContains(_pt, rp);}else{doDraw=volumeContains(pt);}
        if(doDraw){
            if(renderMode==renderMode.VOLUMETRIC && !noVolumetric){
                putDataUpdateSurfaceVolume(_pt);
            }

            totalSamples++;

            if(useZBuffer){
                boolean res=false;

                if(pt.z> myIfSys.renderBuffer.ZBuffer[(int) pt.x][(int) pt.y]){
                    res=true;
                    myIfSys.renderBuffer.ZBuffer[(int)pt.x][(int)pt.y] = pt.z;
                    myIfSys.renderBuffer.RBuffer[(int)pt.x][(int)pt.y] = ptR*dark;
                    myIfSys.renderBuffer.GBuffer[(int)pt.x][(int)pt.y] = ptG*dark;
                    myIfSys.renderBuffer.BBuffer[(int)pt.x][(int)pt.y] = ptB*dark;
                }

                return res;
            }
        }

        return false;
    }


    public void saveCam(){
        camCenter.saveState();
        savedPitch = camPitch;
        savedYaw = camYaw;
        savedRoll = camRoll;
    }

    public ifsPt getCentroid(){
        return new ifsPt(centroid.x/ totalSamplesAlpha, centroid.y/ totalSamplesAlpha, centroid.z/ totalSamplesAlpha);
    }

    protected void addCubeAscii(BufferedWriter writer, int x, int y, int z) throws IOException {

        myVolume++;

        boolean x1Valid=volume.isNotEmpty(x+1,y,z);
        boolean y1Valid=volume.isNotEmpty(x,y+1,z);
        boolean z1Valid=volume.isNotEmpty(x,y,z+1);

        boolean xn1Valid=volume.isNotEmpty(x-1,y,z);
        boolean yn1Valid=volume.isNotEmpty(x,y-1,z);
        boolean zn1Valid=volume.isNotEmpty(x,y,z-1);

        if(x1Valid && y1Valid && z1Valid && xn1Valid && yn1Valid && zn1Valid){}//skip "surrounded" cubes
        else{
            if(!zn1Valid){   //XY plane1
                mySurfaceArea++;
                writer.append("facet normal 0.0 0.0 1.0\nouter loop\n");
                writer.append("vertex " + x + " " + y + " " + z +"\n");
                writer.append("vertex " + x + " " + (y+1) + " " + z +"\n");
                writer.append("vertex " + (x+1) + " " + (y+1) + " " + z +"\n");
                writer.append("endloop\nendfacet\n");

                writer.append("facet normal 0.0 0.0 1.0\nouter loop\n");
                writer.append("vertex " + x + " " + y + " " + z +"\n");
                writer.append("vertex " + (x+1) + " " + y + " " + z +"\n");
                writer.append("vertex " + (x+1) + " " + (y+1) + " " + z +"\n");
                writer.append("endloop\nendfacet\n");
            }

            if(!z1Valid){   //XY plane2
                mySurfaceArea++;
                writer.append("facet normal 0.0 0.0 1.0\nouter loop\n");
                writer.append("vertex " + x + " " + y + " " + (z+1) +"\n");
                writer.append("vertex " + x + " " + (y+1) + " " + (z+1) +"\n");
                writer.append("vertex " + (x+1) + " " + (y+1) + " " + (z+1) +"\n");
                writer.append("endloop\nendfacet\n");

                writer.append("facet normal 0.0 0.0 1.0\nouter loop\n");
                writer.append("vertex " + x + " " + y + " " + (z+1) +"\n");
                writer.append("vertex " + (x+1) + " " + y + " " + (z+1) +"\n");
                writer.append("vertex " + (x+1) + " " + (y+1) + " " + (z+1) +"\n");
                writer.append("endloop\nendfacet\n");
            }

            if(!yn1Valid){   //XZ plane1
                mySurfaceArea++;
                writer.append("facet normal 0.0 1.0 0.0\nouter loop\n");
                writer.append("vertex " + x + " " + y + " " + z +"\n");
                writer.append("vertex " + (x+1) + " " + y + " " + z +"\n");
                writer.append("vertex " + (x+1) + " " + y + " " + (z+1) +"\n");
                writer.append("endloop\nendfacet\n");

                writer.append("facet normal 0.0 1.0 0.0\nouter loop\n");
                writer.append("vertex " + x + " " + y + " " + z +"\n");
                writer.append("vertex " + (x+1) + " " + y + " " + (z+1) +"\n");
                writer.append("vertex " + x + " " + y + " " + (z+1) +"\n");
                writer.append("endloop\nendfacet\n");
            }

            if(!y1Valid){   //XZ plane2
                mySurfaceArea++;
                writer.append("facet normal 0.0 1.0 0.0\nouter loop\n");
                writer.append("vertex " + x + " " + (y+1) + " " + z +"\n");
                writer.append("vertex " + (x+1) + " " + (y+1) + " " + z +"\n");
                writer.append("vertex " + (x+1) + " " + (y+1) + " " + (z+1) +"\n");
                writer.append("endloop\nendfacet\n");

                writer.append("facet normal 0.0 1.0 0.0\nouter loop\n");
                writer.append("vertex " + x + " " + (y+1) + " " + z +"\n");
                writer.append("vertex " + (x+1) + " " + (y+1) + " " + (z+1) +"\n");
                writer.append("vertex " + x + " " + (y+1) + " " + (z+1) +"\n");
                writer.append("endloop\nendfacet\n");
            }

            if(!xn1Valid){   //ZY plane1
                mySurfaceArea++;
                writer.append("facet normal 1.0 0.0 0.0\nouter loop\n");
                writer.append("vertex " + x + " " + y + " " + z +"\n");
                writer.append("vertex " + x + " " + y + " " + (z+1) +"\n");
                writer.append("vertex " + x + " " + (y+1) + " " + (z+1) +"\n");
                writer.append("endloop\nendfacet\n");

                writer.append("facet normal 1.0 0.0 0.0\nouter loop\n");
                writer.append("vertex " + x + " " + y + " " + z +"\n");
                writer.append("vertex " + x + " " + (y+1) + " " + z +"\n");
                writer.append("vertex " + x + " " + (y+1) + " " + (z+1) +"\n");
                writer.append("endloop\nendfacet\n");
            }

            if(!x1Valid){   //ZY plane2
                mySurfaceArea++;
                writer.append("facet normal 1.0 0.0 0.0\nouter loop\n");
                writer.append("vertex " + (x+1) + " " + y + " " + z +"\n");
                writer.append("vertex " + (x+1) + " " + y + " " + (z+1) +"\n");
                writer.append("vertex " + (x+1) + " " + (y+1) + " " + (z+1) +"\n");
                writer.append("endloop\nendfacet\n");

                writer.append("facet normal 1.0 0.0 0.0\nouter loop\n");
                writer.append("vertex " + (x+1) + " " + y + " " + z +"\n");
                writer.append("vertex " + (x+1) + " " + (y+1) + " " + z +"\n");
                writer.append("vertex " + (x+1) + " " + (y+1) + " " + (z+1) +"\n");
                writer.append("endloop\nendfacet\n");
            }
        }
    }

    protected void addCubeTriangles(int x, int y, int z) {

        myVolume++;

        boolean x1Valid=volume.isNotEmpty(x+1,y,z);
        boolean y1Valid=volume.isNotEmpty(x,y+1,z);
        boolean z1Valid=volume.isNotEmpty(x,y,z+1);

        boolean xn1Valid=volume.isNotEmpty(x-1,y,z);
        boolean yn1Valid=volume.isNotEmpty(x,y-1,z);
        boolean zn1Valid=volume.isNotEmpty(x,y,z-1);

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

    public void _saveToAsciiSTL(){
        mySurfaceArea=0;
        myVolume=0;

        BufferedWriter writer = null;
        try {
            String timeLog = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(startDate) + ".stl";
            File logFile = new File(timeLog);

            writer = new BufferedWriter(new FileWriter(logFile, true));
            writer.append("solid ifs_shape\n");
            for(int _x=1; _x<width-1;_x++){
                for(int _y=1; _y<height-1;_y++){
                    for(int _z=1; _z<depth-1;_z++){
                        boolean currentValid=volume.isNotEmpty(_x,_y,_z);
                        if(currentValid){
                            addCubeAscii(writer, _x, _y, _z);
                            //addCubeTriangles(theTriangles, _x, _y, _z);
                        }
                    }
                }

                if(_x%16==0){
                    System.out.println(_x + "/" + width + " saved - " + (int)(100.0*_x/width)+"%");
                }
            }

            writer.append("endsolid ifs_shape\n");

            System.out.println(logFile.getCanonicalPath());
            System.out.println("SURFACE " + mySurfaceArea);
            System.out.println("VOLUME " + myVolume);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
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
                    boolean currentValid=volume.isNotEmpty(_x,_y,_z);
                    if(currentValid){
                        addCubeTriangles( _x, _y, _z);
                    }
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

    public void drawGrid(RenderParams rp, ifsys is){
        if(rp.drawGrid && System.currentTimeMillis() -  rp.gridDrawTime > rp.gridRedrawTime){
            double xmax = 1024;
            double ymax = 1024;
            double gridspace = SubVolume.size;
            rp.gridDrawTime = System.currentTimeMillis();
            int z = 0;

            for(int x=0; x<xmax/gridspace; x++){
                for(int y=0; y<ymax; y+=4){
                    this.putPixel(new ifsPt(
                            x * gridspace,
                            y,
                            z), 64, 64, 64, rp, false, false, true, is);
                    this.putPixel(new ifsPt(
                            y,
                            x * gridspace,
                            z), 64, 64, 64, rp, false, false, true, is);


                    this.putPixel(new ifsPt(
                            x * gridspace,
                            z,
                            y),  64, 64, 64, rp, false, false, true, is);
                    this.putPixel(new ifsPt(
                            y,
                            z,
                            x * gridspace),  64, 64, 64, rp, false, false, true, is);

                    this.putPixel(new ifsPt(
                            z,
                            y,
                            x * gridspace),  64, 64, 64, rp, false, false, true, is);
                    this.putPixel(new ifsPt(
                            z,
                            x * gridspace,
                            y), 64, 64, 64, rp, false, false, true, is);
                }
            }


            for(int i=0; i<ymax; i+=2){
                this.putPixel(new ifsPt(
                        rp.xMin,
                        i,
                        0), 64, 0, 0, rp, false, true, true, is);
                this.putPixel(new ifsPt(
                        rp.xMax,
                        i,
                        0), 64, 0, 0, rp, false, true, true, is);

                this.putPixel(new ifsPt(
                        0,
                        rp.yMin,
                        i), 0, 64, 0, rp, false, true, true, is);
                this.putPixel(new ifsPt(
                        0,
                        rp.yMax,
                        i), 0, 64, 0, rp, false, true, true, is);

                this.putPixel(new ifsPt(
                        i,
                        0,
                        rp.zMin), 0, 0, 64, rp, false, true, true, is);
                this.putPixel(new ifsPt(
                        i,
                        0,
                        rp.zMax), 0, 0, 64, rp, false, true, true, is);
            }

        }
    }
}
