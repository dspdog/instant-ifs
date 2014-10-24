package ifs;

import ifs.flat.ShapeAnalyzer;
import ifs.thirdparty.deleteOldFiles;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
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

        int totalSibs = 3;
        int totalGens = 10000;

        for(int g=0; g<totalGens; g++){

            deleteOldFiles.deleteFilesOlderThanNMin(5, "./models/");

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
                theShape = theShape.getPerturbedShape(mutationDescriptor.intensify(evolveIntensity), false);
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

    public void recordHistory(int g, int totalGens, recordsKeeper rk){
        rk.sortRecordsBySuitability();
        String addition = rk.records.get(0).timeStamp + ": generation " + g + "/" + totalGens + ": best "
                           + rk.records.get(0).theShapeFile + " score " + rk.records.get(0).evolutionScore() + "<br/>\t\r\n";
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
