import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class volume {
    public static enum RenderMode {
        VOLUMETRIC, PROJECT_ONLY
    }

    public static enum VolumeProjection{
        Z,R,G,B,C
    }

    int width, height, depth;

    public RenderMode renderMode = RenderMode.PROJECT_ONLY;

    long drawTime = 0;
    long totalSamples = 0;

    float totalSamplesAlpha =0;
    float dataMax=0;
    float dataMaxVolumetric=0;
    float dataMaxReset = 0;

    float surfaceArea=0;

    public smartVolume volume;

    public float XYProjection[][];

    public float ZBuffer[][];
    public float PBuffer[][]; //point selection buffer...
    public float RBuffer[][];
    public float GBuffer[][];
    public float BBuffer[][];

    //public long ZBufferTime[][];
    public long dataPoints = 0;

    public int depthLeanX, depthLeanY;

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

    Date startDate;

    long myVolume, mySurfaceArea;

    public volume(int w, int h, int d){

        myVolume=0;
        mySurfaceArea=0;

        startDate = Calendar.getInstance().getTime();
        width = w;
        height = h;
        depth = d;
        depthLeanX = 0;
        depthLeanY = 0;
        renderMode = RenderMode.PROJECT_ONLY;
        antiAliasing = true;
        XYProjection = new float[width][height];
        ZBuffer = new float[width][height];
        PBuffer = new float[width][height];
        RBuffer = new float[width][height];
        GBuffer = new float[width][height];
        BBuffer = new float[width][height];
        camPitch=0;
        camRoll=0;
        camYaw=0;
        usePerspective=true;

        camScale=2.0f;

        camCenter = new ifsPt(512.0f,512.0f,512.0f);

        volume = new smartVolume(width);
        centroid = new ifsPt(0,0,0);
        zDarkenScaler=512f;
    }

    public void reset(){
        drawTime=System.currentTimeMillis();
        totalSamples=1;
        totalSamplesAlpha =1;
        centroid = new ifsPt(0,0,0);
        dataMax=dataMaxReset;
        dataMaxVolumetric=dataMaxReset;

        if(renderMode == renderMode.VOLUMETRIC){
            mySurfaceArea=0;
            myVolume=0;
            volume.reset();
        }
    }

    public void clear(){
        reset();
        clearProj(0);
    }

    public void clear(double a){
        reset();
        clearProj(a);
    }

    public void clearProj(double a){
        dataPoints*=a;
        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                ZBuffer[x][y]=0;
                PBuffer[x][y]=-1;
                RBuffer[x][y]=0;
                GBuffer[x][y]=0;
                BBuffer[x][y]=0;
            }
        }
    }

    public boolean volumeContains(ifsPt pt){
        return (pt.x>1 && pt.y>1 && pt.z>1 && pt.x<width-1 && pt.y<height-1);
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
            pt.x = (pt.x-vx)/(float)Math.sqrt(1024f-pt.z)*16.0f+vx;
            pt.y = (pt.y-vy)/(float)Math.sqrt(1024f-pt.z)*16.0f+vy;
        }

        pt.z /= 8.0;
        pt.z = Math.min(pt.z, 1020);
        pt.z = Math.max(pt.z, 4);

        return pt;
    }

    public boolean putPixel(ifsPt _pt, double alpha, float ptR, float ptG, float ptB){
        return putPixel(_pt, (float)alpha, ptR, ptG, ptB);
    }

    public boolean putPixel(ifsPt _pt, float alpha, float ptR, float ptG, float ptB){
        return old_putPixel(_pt, (float)alpha, ptR, ptG, ptB);
    }

    public boolean putPixel(ifsPt _pt, float ptR, float ptG, float ptB, float alpha, int ptRadius){

        if(ptRadius==0){return putPixel(_pt, alpha, ptR, ptG, ptB);}

        ifsPt pt = getCameraDistortedPt(_pt);

        centroid.x+=pt.x*alpha;
        centroid.y+=pt.y*alpha;
        centroid.z+=pt.z*alpha;

        dataPoints++;

        float dark = pt.z/512f;

        if(volumeContains(pt, ptRadius)){

            totalSamples++;
            totalSamplesAlpha +=alpha;

            if(useZBuffer){
                boolean res=false;

                if(pt.z > ZBuffer[(int) pt.x][(int) pt.y]){
                    res=true;

                    for(int x1=-ptRadius; x1<ptRadius; x1++){
                        for(int y1=-ptRadius; y1<ptRadius; y1++){
                            //if(x1*x1+y1*y1<ptRadius)
                            if(ZBuffer[(int)pt.x+x1][(int)pt.y+y1]<pt.z){
                                ZBuffer[(int)pt.x+x1][(int)pt.y+y1] = pt.z;
                                RBuffer[(int)pt.x+x1][(int)pt.y+y1] = ptR*dark;
                                GBuffer[(int)pt.x+x1][(int)pt.y+y1] = ptG*dark;
                                BBuffer[(int)pt.x+x1][(int)pt.y+y1] = ptB*dark;
                            }

                        }
                    }
                }

                return res;
            }
        }

        return false;
    }

    public boolean old_putPixel(ifsPt _pt, float alpha, float ptR, float ptG, float ptB){

        ifsPt pt = getCameraDistortedPt(_pt);

        dataPoints++;
        float dark = pt.z/zDarkenScaler;

        if(volumeContains(_pt)){
            if(renderMode==renderMode.VOLUMETRIC){
                if(volume.putData((int) _pt.x, (int) _pt.y, (int) _pt.z, alpha)){//if its the first point there
                    myVolume++; //add it to volume

                }

                if(volume.getData((int)pt.x, (int)pt.y, (int)pt.y)>dataMaxVolumetric){
                    dataMaxVolumetric= volume.getData((int)pt.x, (int)pt.y, (int)pt.y);//volume[(int)pt.x][(int)pt.y][(int)pt.z];
                    highPtVolumetric = new ifsPt(pt);
                }
            }

            totalSamples++;
            totalSamplesAlpha +=alpha;


            if(useZBuffer){
                boolean res=false;

                if(pt.z> ZBuffer[(int) pt.x][(int) pt.y]){
                    res=true;
                    ZBuffer[(int)pt.x][(int)pt.y] = pt.z;
                    RBuffer[(int)pt.x][(int)pt.y] = ptR*dark;
                    GBuffer[(int)pt.x][(int)pt.y] = ptG*dark;
                    BBuffer[(int)pt.x][(int)pt.y] = ptB*dark;
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

    public float[][][] getScaledProjections(double brightness){
        float[][][] scaled = new float[4][width][height];

        int r=0;
        int g=1;
        int b=2;
        int z=3;

        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                scaled[r][x][y]= Math.min((int)(brightness*RBuffer[x][y]), 255);
                scaled[g][x][y]= Math.min((int)(brightness*GBuffer[x][y]), 255);
                scaled[b][x][y]= Math.min((int)(brightness*BBuffer[x][y]), 255);
                scaled[z][x][y]= Math.min((int)(brightness*ZBuffer[x][y]), 255);
            }
        }

        return scaled;
    }

    public void _saveToAscii(){ //save point cloud to ascii

        BufferedWriter writer = null;
        try {
            String timeLog = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(startDate) + ".xyz";
            File logFile = new File(timeLog);

            writer = new BufferedWriter(new FileWriter(logFile, true));

            for(int x=1; x<width-1;x++){
                for(int y=1; y<height-1;y++){
                    for(int z=1; z<depth-1;z++){
                        if(volume.isNotEmpty(x,y,z) && volume.edges(x, y, z, 1)>6){
                            writer.append(x + " " + y + " " + z + "\n");
                        }
                    }
                }

                if(x%16==0){
                    System.out.println(x + "/" + width + " saved - " + (int)(100.0*x/width)+"%");
                }
            }

            System.out.println(logFile.getCanonicalPath());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    public void _saveToAsciiSTL(){

        /*
         solid cube_corner
          facet normal 0.0 -1.0 0.0
            outer loop
              vertex 0.0 0.0 0.0
              vertex 1.0 0.0 0.0
              vertex 0.0 0.0 1.0
            endloop
          endfacet
        endsolid
         */

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

                            float x=_x;
                            float y=_y;
                            float z=_z;

                            boolean x1Valid=true;//volume.isNotEmpty(_x+1,_y,_z) && !volume.isInterior(_x+1,_y,_z);
                            boolean y1Valid=true;//volume.isNotEmpty(_x,_y+1,_z) && !volume.isInterior(_x,_y+1,_z);
                            boolean z1Valid=true;//volume.isNotEmpty(_x,_y,_z+1) && !volume.isInterior(_x,_y,_z+1);

                            boolean x1z1Valid=true;//volume.isNotEmpty(_x+1,_y,_z+1) && !volume.isInterior(_x+1,_y,_z+1);
                            boolean x1y1Valid=true;//volume.isNotEmpty(_x+1,_y+1,_z) && !volume.isInterior(_x+1,_y+1,_z);
                            boolean y1z1Valid=true;//volume.isNotEmpty(_x,_y+1,_z+1) && !volume.isInterior(_x,_y+1,_z+1);

                            if(x1Valid && y1Valid && x1y1Valid){
                                writer.append("facet normal 0.0 0.0 1.0\nouter loop\n");
                                writer.append("vertex " + x + " " + y + " " + z +"\n");
                                writer.append("vertex " + x + " " + (y+1) + " " + z +"\n");
                                writer.append("vertex " + (x+1) + " " + (y+1) + " " + z +"\n");
                                //writer.append("vertex " + (x+1) + " " + y + " " + z +"\n");
                                writer.append("endloop\nendfacet\n");
                            }
                            if(x1Valid && z1Valid && x1z1Valid){
                                writer.append("facet normal 0.0 1.0 0.0\nouter loop\n");
                                writer.append("vertex " + x + " " + y + " " + z +"\n");
                                writer.append("vertex " + (x+1) + " " + y + " " + z +"\n");
                                writer.append("vertex " + (x+1) + " " + y + " " + (z+1) +"\n");
                                //writer.append("vertex " + x + " " + y + " " + (z+1) +"\n");
                                writer.append("endloop\nendfacet\n");
                            }
                            if(z1Valid && y1Valid && y1z1Valid){
                                writer.append("facet normal 1.0 0.0 0.0\nouter loop\n");
                                writer.append("vertex " + x + " " + y + " " + z +"\n");
                                writer.append("vertex " + x + " " + y + " " + (z+1) +"\n");
                                writer.append("vertex " + x + " " + (y+1) + " " + (z+1) +"\n");
                                //writer.append("vertex " + x + " " + (y+1) + " " + z +"\n");
                                writer.append("endloop\nendfacet\n");
                            }
                        }
                    }
                }

                if(_x%16==0){
                    System.out.println(_x + "/" + width + " saved - " + (int)(100.0*_x/width)+"%");
                }
            }

            writer.append("endsolid ifs_shape\n");

            System.out.println(logFile.getCanonicalPath());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    public static float[][] getProjectionCopy(float[][] map){
        int width = map.length;

        float[][] res = new float[width][width];
        int x,y;

        for(x=0; x<width; x++){
            for(y=0; y<width; y++){
                res[x][y]=map[x][y]+0;
            }
        }

        return res;
    }

    public float[][] findEdges2D(float[][] map){
        int width = map.length;

        float total =0;

        float[][] res = new float[width][width];
        int x,y;

        for(x=3; x<width-3; x++){
            for(y=3; y<width-3; y++){

                double edges1 = Math.max(
                                Math.max(
                                        (map[x][y]-map[x-1][y]),
                                        (map[x][y] - map[x + 1][y])),
                                Math.max((map[x][y]-map[x][y-1]),
                                        (map[x][y]-map[x][y+1]))
                );

                total+=edges1/255.0;

                edges1 = Math.min(edges1, 255);
                res[x][y]=(float)edges1;
            }
        }

        surfaceArea = total;

        return res;
    }

    public static float[][] getThreshold2D(float[][] map, int threshold){
        int width = map.length;

        float[][] res = new float[width][width];
        int x,y;

        for(x=0; x<width; x++){
            for(y=0; y<width; y++){
                res[x][y]=map[x][y]>threshold?255:0;
            }
        }

        return res;
    }
}
