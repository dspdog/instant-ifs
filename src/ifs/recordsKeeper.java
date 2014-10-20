package ifs;

import ifs.flat.TetraMarcher;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class recordsKeeper implements java.io.Serializable{

    public ArrayList<row> records = new ArrayList<>();
    public static final String defaultName = "recordskeeper.shapes";

    public recordsKeeper(){
        records = new ArrayList<>();
    }

    public class row implements java.io.Serializable{
        public double theVolume=0;
        public double theSurfaceArea=0;
        public String theFileName = "";
        public String timeStamp;
        public long totalTime =0;
        public String theShapeFile;

        public row(double vol, double surface, String fileName, long time, ifsShape shape){
            theVolume=vol;
            theSurfaceArea=surface;
            theFileName=fileName;
            totalTime=time;
            Date startDate = Calendar.getInstance().getTime();
            timeStamp = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(startDate);
            theShapeFile=System.currentTimeMillis()+"_"+(int)((Math.random()*10000)%10000)+".shape";
            shape.saveToFile(theShapeFile);
        }
    }

    public void submit(ifsShape theShape, TetraMarcher tm){
        row newrow = new row(tm.theVolume, tm.theSurfaceArea, tm.theFileName, tm.totalTime, theShape);
        records.add(newrow);
        saveToFile(defaultName);
        System.out.println("total records: " + records.size());
    }

    public void getPotentials(ifsys is){
        int res = (1024/(int)is.theShape.isoStepSize);
        System.out.println("getting potentials! step size " + (int)is.theShape.isoStepSize + " res " + res);
        TetraMarcher tm = new TetraMarcher();
        tm.getPotentials(is.shapeAnalyzer, is.zLists, is.xMin, is.xMax, is.yMin, is.yMax, is.zMin, is.zMax, (int)is.theShape.isoStepSize);
        if(tm.shapeInvalid){
            System.out.println("shape invalid, ignoring...");
        }else{
            System.out.println("saving shape to records...");

            loadFromFile(recordsKeeper.defaultName);
            submit(is.theShape, tm);
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
            System.out.println("records saved to "+filename);
        }catch(Exception i){
            i.printStackTrace();
        }
    }

    public recordsKeeper loadFromFile(String filename){
        recordsKeeper loadedRecords=new recordsKeeper();
        try
        {
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            loadedRecords = (recordsKeeper) in.readObject();
            in.close();
            fileIn.close();
            //System.out.println("loaded " + filename);
        }catch(Exception i)
        {
            i.printStackTrace();
        }

        return loadedRecords;
    }
}
