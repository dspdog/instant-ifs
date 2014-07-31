import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

class ifsShape implements java.io.Serializable {
    public ifsPt pts[];

    public float unitScale;

    public int pointsInUse;
    public boolean autoUpdateCenterEnabled;
    public boolean stateSaved;
    public boolean autoScale;

    public RenderParams rp;

    public ifsShape(){
        autoUpdateCenterEnabled =false;
        stateSaved = false;
        pointsInUse = 1;
        unitScale = 115.47005383792515f; //distance from center to one of the points in preset #1
        autoScale = true;
        pts = new ifsPt[1000];
        for(int a=0; a< 1000; a++){
            pts[a] = new ifsPt();
        }
    }

    public void saveToFile(String filename){
        try{
            FileOutputStream fileOut =
                    new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
            System.out.println("saved to "+filename);
        }catch(Exception i){
            i.printStackTrace();
        }
    }

    public ifsShape loadFromFile(String filename){
        ifsShape loadedShape=null;
        try
        {
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            loadedShape = (ifsShape) in.readObject();
            in.close();
            fileIn.close();
            System.out.println("loaded " + filename);
        }catch(Exception i)
        {
            i.printStackTrace();
        }

        return loadedShape;
    }

    public ArrayList<ifsShape> getPerturbedVersions(int total, float intensity){
        ArrayList<ifsShape> _perturbedVersions;
        _perturbedVersions = new ArrayList<ifsShape>();
        for(int i=0; i<total; i++){
            _perturbedVersions.add(this.getPerturbedShape(intensity));
        }
        return  _perturbedVersions;
    }

    public ifsShape getPerturbedShape(float intensity){
        ifsShape pShape = new ifsShape();
        pShape.pointsInUse=this.pointsInUse;

        for(int i=0; i<pShape.pointsInUse; i++){
            pShape.pts[i] = new ifsPt(this.pts[i], true);
            pShape.pts[i].perturb(intensity);
        }

        return pShape;
    }

    /*public void centerByPt(int desiredX, int desiredY, int desiredZ, int centerX, int centerY, int centerZ){
        int offsetX = desiredX-centerX;
        int offsetY = desiredY-centerY;
        int offsetZ = desiredZ-centerZ;

        for(int i=0; i<pointsInUse; i++){
            pts[i].x-=offsetX;
            pts[i].y-=offsetY;
            pts[i].z-=offsetZ;
        }
        updateCenter();
    }*/

    public void saveState(){
        for(int a = 0; a < pointsInUse; a++){
            pts[a].saveState();
            stateSaved=true;
        }
    }

    public void addPoint(float x, float y, float z){
        pts[pointsInUse].x = x;
        pts[pointsInUse].y = y;
        pts[pointsInUse].z = z;
        pts[pointsInUse].scale = 0.5f;
        pts[pointsInUse].rotationYaw = 0.0f;
        pts[pointsInUse].rotationPitch = 0.0f;
        //pts[pointsInUse].rotationRoll = 0.0f;
        pts[pointsInUse].opacity = 1.0f;
        pointsInUse++;
        updateCenter();
    }

    public void addPoint(double x, double y, double z){
        addPoint((float)x, (float)y, (float)z);
    }

    public void addPointScaled(double x, double y, double z, double scale){
        addPointScaled((float) x, (float) y, (float) z, (float) scale);
    }

    public void addPointScaled(float x, float y, float z, float scale){
        pts[pointsInUse].x = x;
        pts[pointsInUse].y = y;
        pts[pointsInUse].z = z;
        pts[pointsInUse].scale = scale;
        pts[pointsInUse].rotationYaw = 0.0f;
        pts[pointsInUse].rotationPitch = 0.0f;
        //pts[pointsInUse].rotationRoll = 0.0f;
        pts[pointsInUse].opacity = 1.0f;
        pointsInUse++;
        updateCenter();
    }

    public void deletePoint(int selectedPoint){
        for(int a = selectedPoint; a < pointsInUse; a++){
            pts[a].x = pts[a + 1].x;
            pts[a].y = pts[a + 1].y;

            pts[a].scale = pts[a + 1].scale;
            pts[a].rotationYaw = pts[a + 1].rotationYaw;
        }

        pts[pointsInUse].x = 0.0f;
        pts[pointsInUse].y = 0.0f;

        pts[pointsInUse].scale = 0.5f;
        pts[pointsInUse].rotationYaw = 0.0f;
        pts[pointsInUse].rotationPitch = 0.0f;
        pointsInUse--;

        updateCenter();
    }

    public void clearPts(){

        for(int a = 0; a < pointsInUse; a++){
            deletePoint(pointsInUse-a);
        }
        pointsInUse=0;
    }

    void updateRadiusDegrees(){
        //pts[0].degreesYaw = 0;
        pts[0].degreesPitch = (float)Math.PI/2;
        pts[0].radius = unitScale*pts[0].scale;

        for(int a = 1; a < pointsInUse; a++){
            pts[a].radius = autoScale ? distance(pts[a].x - pts[0].x, pts[a].y - pts[0].y,  pts[a].z - pts[0].z) : pts[0].radius;
            pts[a].degreesYaw = (float)Math.atan2(pts[a].x - pts[0].x, pts[a].y - pts[0].y);
            pts[a].degreesPitch = (float)Math.atan2(pts[a].radius, pts[a].z - pts[0].z);
        }
    }

    void updateCenter(){
        float x = 0, y = 0;

        if(autoUpdateCenterEnabled){
            if(pointsInUse != 0){
                for(int a = 1; a < pointsInUse; a++){
                    x += pts[a].x;
                    y += pts[a].y;
                }
                pts[0].x  = x / (pointsInUse-1);
                pts[0].y  = y / (pointsInUse-1);
            }
        }

        updateRadiusDegrees();
    }

    void updateCenterOnce(){
        boolean oldState = autoUpdateCenterEnabled;

        autoUpdateCenterEnabled =true;
        updateCenter();
        autoUpdateCenterEnabled =oldState;
    }

    public void setToPreset(int preset){
        switch(preset){
            case 0: // '\000'
                clearPts();
                pointsInUse=1;
                int centerx=512;
                int centery=512;
                int centerz=512;

                for(int i=0; i<4; i++){
                    this.addPoint(
                            Math.cos(Math.PI/4+i*Math.PI/2)*200+centerx,
                            Math.sin(Math.PI/4+i*Math.PI/2)*200+centery,
                            centerz-256);
                }

                for(int i=0; i<4; i++){
                    this.addPoint(
                            Math.cos(Math.PI/4+i*Math.PI/2)*200+centerx,
                            Math.sin(Math.PI/4+i*Math.PI/2)*200+centery,
                            centerz+256);
                }

                this.pts[0].z=centerz;
                break;

            case 9: // '\009'
                clearPts();
                pointsInUse=1;
                centerx=512;
                centery=512;
                centerz=512;

                double div = 3;

                for(int i=0; i<div; i++){
                    this.addPoint(
                            Math.cos(Math.PI/div+i*Math.PI/2)*200+centerx,
                            Math.sin(Math.PI/div+i*Math.PI/2)*200+centery,
                            centerz-256);
                }

                this.pts[0].z=centerz;
                break;

            case 1: // '\001'

                clearPts();
                pointsInUse=1;
                centerx=512;
                centery=512;
                centerz=512;

                for(int i=0; i<4; i++){
                    this.addPointScaled(
                            Math.cos(Math.PI/4+i*Math.PI/2)*256+centerx,
                            Math.sin(Math.PI/4+i*Math.PI/2)*256+centery,
                            256, 1.0/Math.sqrt(3.0));
                    this.addPointScaled(
                            Math.cos(Math.PI/4+i*Math.PI/2)*256+centerx,
                            Math.sin(Math.PI/4+i*Math.PI/2)*256+centery,
                            256+512, 1.0/Math.sqrt(3.0));
                }
                this.pts[0].z=centerz;
                break;

        }

        updateCenterOnce();
    }

    public static float distance(float x, float y, float z){
        return (float)Math.sqrt(x * x + y * y + z * z);
    }
}