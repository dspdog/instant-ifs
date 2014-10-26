package ifs;

import ifs.flat.CubeMarcher;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class recordsKeeper implements java.io.Serializable{

    public ArrayList<row> records = new ArrayList<>();
    public static final String defaultName = "recordskeeper.shapes";

    public recordsKeeper(){
        records = new ArrayList<>();
    }

    public class row implements java.io.Serializable{
        public double theVolume=0;
        public double theSurfaceArea=0;
        public double theDiagonal=0;
        public String theFileName = "";
        public String timeStamp;
        public long ltimeStamp;
        public long totalTime =0;
        public long generation;
        public String theShapeFile;

        public row(double vol, double surface, double diag, String fileName, long time, ifsShape shape, long gen, long sib){
            ltimeStamp = System.currentTimeMillis();
            theVolume=vol+0;
            theDiagonal = diag;
            theSurfaceArea=surface+0;
            theFileName=fileName+"";
            totalTime=time+0;
            generation=gen+0;
            Date startDate = Calendar.getInstance().getTime();
            timeStamp = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(startDate);
            theShapeFile="gen" + gen + "_" + System.currentTimeMillis()%1000+"_"+(int)((Math.random()*1000)%1000)+".shape";
            shape.saveToFile(theShapeFile);
        }

        public double evolutionScore(){
            return theSurfaceArea/theVolume;
        }
    }

    public row submit(ifsShape theShape, CubeMarcher tm, int generationNo, int sibNo, double recordScore){
        row newrow = new row(tm.theVolume * 1.0, tm.theSurfaceArea, tm.theDiagonal, tm.theFileName, tm.totalTime, theShape, generationNo, sibNo);

        if(!tm.shapeInvalid && newrow.evolutionScore() > recordScore && Math.abs(tm.theVolume - 30000)<5000){
            records.add(newrow);
            saveToFile(defaultName);
        }
        System.out.println("total records: " + records.size());
        return newrow;
    }

    public void sortRecordsBySuitability(){
        Collections.sort(records, new Comparator<row>() {
            @Override
            public int compare(row o1, row o2) {
                return new Double(o2.evolutionScore()).compareTo(new Double(o1.evolutionScore()));
            }
        });
    }

    public void getPotentials(ifsys is, int generationNo, int sibNo, ifsShape theShape, double recordScore){
        System.out.println("getting potentials! step size " + (int)(1024/is.shapeAnalyzer.width) + " res " + is.shapeAnalyzer.width);
        CubeMarcher tm = new CubeMarcher();
        tm.getPotentials(is.shapeAnalyzer, is.zLists, is.xMin, is.xMax, is.yMin, is.yMax, is.zMin, is.zMax);
        row newRow = submit(theShape, tm, generationNo, sibNo, recordScore);

        if(newRow.evolutionScore() > recordScore){
            tm.saveToBinarySTL();
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
            return new recordsKeeper();
            //i.printStackTrace();
        }

        return loadedRecords;
    }
}
