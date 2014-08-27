package ifs;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class EvolvingShape {

    static final float MINIMUM_SCORE = Float.MAX_VALUE*-1;

    ArrayList<ArrayList<ifsShape>> familyHistory;
    ArrayList<ifsShape> shapeList;
    ifsShape baseShape;
    int shapeIndex;
    boolean evolving;
    long evolvePeriod;
    int evolvedSibs=0;

    public ifsPt mutationDescriptorPt;

    boolean alwaysNewShape;

    final int sibsPerGen = 100;

    ifsShape highestScoringShape;

    public String evolutionHistory[][];
    public int historyIndex;

    public ScoreParams myScoreParams;

    DecimalFormat gen_f = new DecimalFormat("00000");
    DecimalFormat sib_f = new DecimalFormat("00");

    public EvolvingShape(ifsShape _baseShape){

        myScoreParams = new ScoreParams(ScoreParams.Presets.MAX_SURFACE);

        historyIndex=0;
        alwaysNewShape = true; //set to false to always spawn from the "record-holding" shape rather than just the best member of this generation

        familyHistory = new ArrayList<ArrayList<ifsShape>>();
        shapeList = new ArrayList<ifsShape>();
        baseShape=_baseShape;
        shapeIndex=0;
        evolving=false;
        evolvePeriod=20000;
        evolvedSibs=0;
        highestScoringShape = new ifsShape();

        mutationDescriptorPt = new ifsPt(1,1,1, 1,1,1);

        evolutionHistory = new String[1000][5];
    }

    public void offSpring(ifsShape _baseShape, float intensity){
        boolean staySymmetric = false;
        evolvedSibs=0;
        baseShape=_baseShape;

        shapeList = baseShape.getPerturbedVersions(sibsPerGen, mutationDescriptorPt,  staySymmetric);

        familyHistory.add(shapeListCopy());
        updateEvolutionHistory(myScoreParams);
        System.out.println("Generation " + familyHistory.size() + " - " + shapeList.size() + " siblings");
    }

    public void parents(ifsShape _baseShape){
        if(familyHistory.size()>0){
            evolvedSibs=0;
            baseShape=_baseShape;
            shapeList = familyHistory.get(familyHistory.size()-2);
            familyHistory.remove(familyHistory.size()-1);
            updateEvolutionHistory(myScoreParams);
            System.out.println("Generation " + familyHistory.size() + " - " + shapeList.size() + " siblings");
        }
    }

    public ifsShape getHighestScoreShape(){
        float highestScore;

        if(alwaysNewShape){
            highestScore = MINIMUM_SCORE;
        }else{
            highestScore =  highestScoringShape.score;
        }

        for (int i = 0; i < evolvedSibs; i++) {
             if(shapeList.get(i).score>highestScore){
                 highestScoringShape = shapeList.get(i);
                 highestScore = highestScoringShape.score;
             }
        }
        System.out.println("high score " + highestScore);
        return highestScoringShape;
    }

    public ifsShape prevShape(float score){
        shapeList.get(shapeIndex).score = score;
        shapeIndex--;shapeIndex=(shapeIndex+sibsPerGen)%sibsPerGen;
        updateEvolutionHistory(myScoreParams);
        System.out.println("Generation " + familyHistory.size() + " - Sibling " +shapeIndex + "/" + shapeList.size());
        return shapeList.get(shapeIndex);
    }

    public ifsShape nextShape(float score){
        shapeList.get(shapeIndex).score = score;
        shapeIndex++;shapeIndex=(shapeIndex+sibsPerGen)%sibsPerGen;
        updateEvolutionHistory(myScoreParams);
        System.out.println("Generation " + familyHistory.size() + " - Sibling " +shapeIndex + "/" + shapeList.size());
        return shapeList.get(shapeIndex);
    }

    public void updateEvolutionHistory(ScoreParams sp){
        evolutionHistory[historyIndex][0]= new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        evolutionHistory[historyIndex][1]="G"+gen_f.format(familyHistory.size()) + "_S" + sib_f.format(shapeIndex);
        evolutionHistory[historyIndex][2]="0";
        evolutionHistory[historyIndex][3]="999";
        evolutionHistory[historyIndex][4]=sp.presetName;
        historyIndex++;
        historyIndex = historyIndex % evolutionHistory.length;
    }

    public ArrayList<ifsShape> shapeListCopy(){
        ArrayList<ifsShape> newList = new ArrayList<ifsShape>();

        for (int i = 0; i < shapeList.size(); i++) {
            newList.add(new ifsShape(shapeList.get(i)));
        }

        return newList;
    }
}
