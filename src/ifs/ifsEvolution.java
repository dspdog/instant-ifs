package ifs;

import ifs.thirdparty.deleteOldFiles;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;

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
        recordsKeeper rk = new recordsKeeper();
        long startTime = System.currentTimeMillis();

        System.out.println("start of evolution...");
        System.out.println("generation 1");

        int totalSibs = 4;
        int totalGens = 1000;

        deleteOldFiles.deleteFilesOlderThanNMin(5, "./models/");

        for(int g=0; g<totalGens; g++){
            if(g>0){
                //TODO use simulated annealing?
                rk.sortRecordsBySuitability();
                theShape = new ifsShape().loadFromFile("./shapes/" + rk.records.get(0).theShapeFile);
                recordHistory(g, totalGens, rk);
            }
            for(int i=0; i<totalSibs; i++){
                theShape.resetShape();
                theShape.updateCenterOnce();
                theShape = theShape.getPerturbedShape(mutationDescriptor.intensify(evolveIntensity), false);
                is.clearframe();
                is.gamefunc();
                rk.getPotentials(is, 0, i);

                System.out.println("sibling " + (i+1) + "/" + totalSibs + " submitted.");
                System.out.println("Elapsed time: " + (int)((System.currentTimeMillis() - startTime)/60000) + "m " + ((System.currentTimeMillis() - startTime)/1000)%60 + "s");
            }
        }
    }

    public void recordHistory(int g, int totalGens, recordsKeeper rk){
        String addition = "generation " + g + "/" + totalGens + ": best "
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
        System.out.println("HISTORY: " + historyString);
    }

}
