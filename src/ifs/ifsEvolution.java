package ifs;

import ifs.thirdparty.deleteOldFiles;

import java.io.*;
import java.util.*;

public class ifsEvolution {
    ifsShape theShape; ifsPt mutationDescriptor; float evolveIntensity; recordsKeeper rk; ifsys is;
    String historyString = "";

    public ifsEvolution(ifsShape _theShape, ifsPt _mutationDescriptor, float intensity, recordsKeeper _rk, ifsys _is){
        theShape = _theShape;
        mutationDescriptor = _mutationDescriptor;
        evolveIntensity = intensity;
        rk = _rk;
        is = _is;
        historyString = "";
    }

    public void start(){
        recordsKeeper rk = new recordsKeeper().loadFromFile(recordsKeeper.defaultName);
        long startTime = System.currentTimeMillis();

        System.out.println("start of evolution...");

        int totalSibs = 2;
        int totalGens = 10000;

        Random rnd = new Random();
        deleteOldFiles.deleteFilesOlderThanNMin(1, ".");
        currentRecordScore=0;
        currentRecord="";

        for(int g=0; g<totalGens; g++){

            deleteOldFiles.deleteFilesOlderThanNMin(5, "./models/");
            System.out.println("\n\n///////////////////////////////GENERATION " + g);
            String seed = "";

            if(g>0){
                //TODO use simulated annealing?
                rk.sortRecordsBySuitability();
                seed = "./shapes/" + rk.records.get(0).theShapeFile;
                theShape = new ifsShape().loadFromFile(seed);
                recordHistory(g, totalGens, rk);
            }
            for(int i=0; i<totalSibs; i++){
                if(seed!="")theShape = new ifsShape().loadFromFile(seed);
                theShape = theShape.getPerturbedShape(mutationDescriptor.intensify((float)(rnd.nextGaussian()*evolveIntensity)), false);
                is.theShape = theShape;
                is.reIndex();
                is.clearframe();
                is.gamefunc();

                rk.getPotentials(is, g, i, theShape);

                System.out.println("sibling " + (i+1) + "/" + totalSibs + " submitted.");
                System.out.println("Elapsed time: " + (int)((System.currentTimeMillis() - startTime)/60000) + "m " + ((System.currentTimeMillis() - startTime)/1000)%60 + "s");
            }
        }
    }

    String currentRecord="";
    double currentRecordScore=0;
    long currentRecordTime=0;

    public void recordHistory(int g, int totalGens, recordsKeeper rk){
        rk.sortRecordsBySuitability();
        String addition = rk.records.get(0).timeStamp + ": generation " + g + "/" + totalGens + ": best "
                           + rk.records.get(0).theShapeFile + " score " + rk.records.get(0).evolutionScore() + " (delt " + (1.0f*rk.records.get(0).evolutionScore()/currentRecordScore) + "% pts, " +
                (int)(System.currentTimeMillis() - currentRecordTime)/60000+"m "+ (int)((System.currentTimeMillis() - currentRecordTime)/1000)%60+"s)<br/>\t\r\n";

        if(currentRecord!=rk.records.get(0).theShapeFile){
            currentRecord = rk.records.get(0).theShapeFile;
            currentRecordScore = rk.records.get(0).evolutionScore();
            currentRecordTime=System.currentTimeMillis();

            historyString = historyString + addition;

            PrintWriter historyOut = null;
            try {
                historyOut = new PrintWriter("history.html");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            historyOut.write(historyString);
            historyOut.close();
            System.out.println("HISTORY: " + addition);
        }
    }
}
