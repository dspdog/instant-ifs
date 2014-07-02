import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class volume {
    int width, height, depth;

    RenderMode renderMode;

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

    public volume(int w, int h, int d){
        width = w;
        height = h;
        depth = d;
        depthLeanX = 0;
        depthLeanY = 0;
        renderMode = RenderMode.PROJECT_ONLY;
        antiAliasing = true;
        XYProjection = new float[width][height];
        ZBuffer = new float[width][height];
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
    }

    public void reset(){
        drawTime=System.currentTimeMillis();
        totalSamples=1;
        totalSamplesAlpha =1;
        centroid = new ifsPt(0,0,0);
        dataMax=dataMaxReset;
        dataMaxVolumetric=dataMaxReset;

        if(renderMode == renderMode.VOLUMETRIC){
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
                XYProjection[x][y]*=a;
                ZBuffer[x][y]*=a;
                RBuffer[x][y]*=a;
                GBuffer[x][y]*=a;
                BBuffer[x][y]*=a;
            }
        }
    }

    public boolean volumeContains(ifsPt pt){
        return (pt.x>1 && pt.y>1 && pt.z>1 && pt.x<width-1 && pt.y<height-1 && pt.z<depth-1);
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

    public boolean putPixel(ifsPt _pt, double alpha){
        return putPixel(_pt, (float)alpha);
    }

    public boolean putPixel(ifsPt _pt, float alpha){

        ifsPt pt = getCameraDistortedPt(_pt);

        centroid.x+=pt.x*alpha;
        centroid.y+=pt.y*alpha;
        centroid.z+=pt.z*alpha;

        dataPoints++;

        if(volumeContains(pt)){
            if(renderMode==renderMode.VOLUMETRIC){
                if(antiAliasing){
                    float xDec = pt.x - (int)pt.x;
                    float yDec = pt.y - (int)pt.y;
                    float zDec = pt.z - (int)pt.z;

                    volume.putData((int) pt.x, (int) pt.y, (int) pt.z, alpha * (1 - xDec) * (1 - yDec) * (1 - zDec));
                    volume.putData((int) pt.x + 1, (int) pt.y, (int) pt.z, alpha * xDec * (1 - yDec) * (1 - zDec));
                    volume.putData((int) pt.x, (int) pt.y + 1, (int) pt.z, alpha * (1 - xDec) * yDec * (1 - zDec));
                    volume.putData((int) pt.x + 1, (int) pt.y + 1, (int) pt.z, alpha * xDec * yDec * (1 - zDec));

                    volume.putData((int) pt.x, (int) pt.y, (int) pt.z + 1, alpha * (1 - xDec) * (1 - yDec) * zDec);
                    volume.putData((int) pt.x + 1, (int) pt.y, (int) pt.z + 1, alpha * xDec * (1 - yDec) * zDec);
                    volume.putData((int) pt.x, (int) pt.y + 1, (int) pt.z + 1, alpha * (1 - xDec) * yDec * zDec);
                    volume.putData((int) pt.x + 1, (int) pt.y + 1, (int) pt.z + 1, alpha * xDec * yDec * zDec);

                }else{
                    volume.putData((int) pt.x, (int) pt.y, (int) pt.z + 1, alpha);
                }

                if(volume.getData((int)pt.x, (int)pt.y, (int)pt.y)>dataMaxVolumetric){
                    dataMaxVolumetric= volume.getData((int)pt.x, (int)pt.y, (int)pt.y);//volume[(int)pt.x][(int)pt.y][(int)pt.z];
                    highPtVolumetric = new ifsPt(pt);
                }
            }

            totalSamples++;
            totalSamplesAlpha +=alpha;

            XYProjection[(int)pt.x][(int)pt.y]+=alpha;

            if(XYProjection[(int)pt.x][(int)pt.y]>dataMax){
                dataMax= XYProjection[(int)pt.x][(int)pt.y];
                highPt = new ifsPt(pt);
            }

            if(useZBuffer){
                float xDec = pt.x - (int)pt.x;
                float yDec = pt.y - (int)pt.y;

                boolean res=false;

                if(pt.z * (1 - xDec) * (1 - yDec) > ZBuffer[(int) pt.x][(int) pt.y]){
                    res=true;
                    ZBuffer[(int)pt.x][(int)pt.y] = pt.z * (1 - xDec) * (1 - yDec);
                }

                ZBuffer[(int)pt.x+1][(int)pt.y] = Math.max(pt.z * xDec * (1 - yDec), ZBuffer[(int) pt.x + 1][(int) pt.y]);
                ZBuffer[(int)pt.x][(int)pt.y+1] = Math.max(pt.z * (1 - xDec) * yDec, ZBuffer[(int) pt.x][(int) pt.y + 1]);
                ZBuffer[(int)pt.x+1][(int)pt.y+1] = Math.max(pt.z * xDec * yDec, ZBuffer[(int) pt.x + 1][(int) pt.y + 1]);

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

    public float[][] getScaledProjection(double brightness){
        float[][] scaled = new float[width][height];

        double scaler = brightness;

        if(useZBuffer){
            for(int x=0; x<width; x++){
                for(int y=0; y<height; y++){
                    scaled[x][y]= Math.min((int)(scaler*ZBuffer[x][y]), 255);
                }
            }
        }else{
            for(int x=0; x<width; x++){
                for(int y=0; y<height; y++){
                    scaled[x][y]= Math.min((int)(scaler*XYProjection[x][y]), 255);
                }
            }
        }

        return scaled;
    }

    public static void saveToAscii(smartVolume map){
        BufferedWriter writer = null;
        try {
            //create a temporary file
            String timeLog = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(Calendar.getInstance().getTime()) + ".txt";
            File logFile = new File(timeLog);

            // This will output the full path where the file will be written to...
            System.out.println(logFile.getCanonicalPath());

            writer = new BufferedWriter(new FileWriter(logFile));
            writer.write("Hello world!");
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

    public static enum RenderMode {
        VOLUMETRIC, PROJECT_ONLY
    }
}
