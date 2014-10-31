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

    public void start() throws InterruptedException {
        recordsKeeper rk = new recordsKeeper();//.loadFromFile(recordsKeeper.defaultName);
        long startTime = System.currentTimeMillis();

        System.out.println("start of evolution...");

        int totalSibs = 2;
        int totalGens = 10000;

        Random rnd = new Random();
        deleteOldFiles.deleteFilesOlderThanNMin(1, ".");
        currentRecordScore=0;
        currentRecord=0;

        for(int g=0; g<totalGens; g++){
            is.theShape.clearZLists();

            //deleteOldFiles.deleteFilesOlderThanNMin(5, "./models/");
            System.out.println("\n\n///////////////////////////////GENERATION " + g);
            //String seed = "";

            if(g>1){
                //TODO use simulated annealing?
                rk.sortRecordsBySuitability();
                //seed = "./shapes/" + recordHistory(g, totalGens, rk).theShapeFile;
                theShape =  new ifsShape(recordHistory(g, totalGens, rk).theShape);//.loadFromFile(seed);
            }
            ifsShape firstVersion = new ifsShape(theShape);
            for(int i=0; i<totalSibs; i++){
                //if(seed!="")theShape = new ifsShape().loadFromFile(seed);
                theShape = new ifsShape(firstVersion).getPerturbedShape(mutationDescriptor.intensify((float)(rnd.nextGaussian()*evolveIntensity/10f)), false, true);
                is.theShape = new ifsShape(theShape);
                is.theShape.reIndex(is.renderBuffer, is.shapeAnalyzer, is.rp);
                is.clearframe();
                is.gamefunc();
                System.out.println("score " + currentRecordScore);
                rk.getPotentials(is, g, i, new ifsShape(theShape), currentRecordScore);

                System.out.println("sibling " + (i+1) + "/" + totalSibs + " submitted. iters " + theShape.iterations/100f + " score " + currentRecordScore);
                System.out.println("Elapsed time: " + (int)((System.currentTimeMillis() - startTime)/60000) + "m " + ((System.currentTimeMillis() - startTime)/1000)%60 + "s");
            }
        }
    }

    long currentRecord=0;
    double currentRecordScore=0;
    long currentRecordTime=0;

    public recordsKeeper.row recordHistory(int g, int totalGens, recordsKeeper rk){

        rk.sortRecordsBySuitability();

        int winningIndex=0;int r=0;
        for(r=0; r<100; r++){
            if(rk.records.get(r).evolutionScore() == 0 || new Double(rk.records.get(r).evolutionScore()).isNaN() || new Double(rk.records.get(r).evolutionScore()).isInfinite()){
                System.out.println("bad score");
                System.exit(1);
            }else{
                winningIndex=r;
                break;
            }
        }

        recordsKeeper.row winningRow = rk.records.get(winningIndex);

        String addition = winningRow.timeStamp + ": generation " + g + "/" + totalGens + ": best "
                           +winningRow.theShapeFile + " score " + winningRow.evolutionScore() + " (delt " + (100.0f*winningRow.evolutionScore()/currentRecordScore) + "% pts, " +
                (int)(System.currentTimeMillis() - currentRecordTime)/60000+"m "+ (int)((System.currentTimeMillis() - currentRecordTime)/1000)%60+"s)<br/>\t\r\n";

        if(currentRecord!=winningRow.ltimeStamp){
            currentRecord = winningRow.ltimeStamp;
            currentRecordScore = winningRow.evolutionScore();
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

        return winningRow;
    }
}
