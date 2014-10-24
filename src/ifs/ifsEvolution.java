package ifs;

import ifs.thirdparty.deleteOldFiles;

public class ifsEvolution {
    ifsShape theShape; ifsPt mutationDescriptor; float evolveIntensity; recordsKeeper rk; ifsys is;

    public ifsEvolution(ifsShape _theShape, ifsPt _mutationDescriptor, float intensity, recordsKeeper _rk, ifsys _is){
        theShape = _theShape;
        mutationDescriptor = _mutationDescriptor;
        evolveIntensity = intensity;
        rk = _rk;
        is = _is;
    }

    public void start(){
        recordsKeeper rk = new recordsKeeper();
        long startTime = System.currentTimeMillis();

        System.out.println("start of evolution...");
        System.out.println("generation 1");

        int totalSibs = 10;
        int totalGens = 10;

        deleteOldFiles.deleteFilesOlderThanNMin(5, ".");

        for(int g=0; g<totalGens; g++){
            if(g>0){
                //TODO use simulated annealing?
                rk.sortRecordsBySuitability();
                theShape = new ifsShape().loadFromFile(rk.records.get(0).theShapeFile);
                System.out.println("generation " + g + "/" + totalGens + " best shape " + rk.records.get(0).theShapeFile + " score " + rk.records.get(0).evolutionScore());
            }
            for(int i=0; i<totalSibs; i++){
                theShape = theShape.getPerturbedShape(mutationDescriptor.intensify(evolveIntensity), false);
                is.clearframe();
                is.gamefunc();
                rk.getPotentials(is, 0, i);

                System.out.println("sibling " + (i+1) + "/" + totalSibs + " submitted.");
                System.out.println("Elapsed time: " + (int)((System.currentTimeMillis() - startTime)/60000) + "m " + ((System.currentTimeMillis() - startTime)/1000)%60 + "s");
            }
        }
    }

}
