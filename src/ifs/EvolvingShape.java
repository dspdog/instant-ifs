package ifs;

import java.util.ArrayList;

public class EvolvingShape {

    static final float MINIMUM_SCORE = Float.MAX_VALUE*-1;

    ArrayList<ArrayList<ifsShape>> familyHistory;
    ArrayList<ifsShape> shapeList;
    ifsShape baseShape;
    int shapeIndex;
    boolean evolving;
    long evolvePeriod;
    int evolvedSibs=0;

    boolean alwaysNewShape;

    final int sibsPerGen = 4;

    ifsShape highestScoringShape;

    public EvolvingShape(ifsShape _baseShape){

        alwaysNewShape = true; //set to false to always spawn from the "record-holding" shape rather than just the best member of this generation

        familyHistory = new ArrayList<ArrayList<ifsShape>>();
        baseShape=_baseShape;
        shapeIndex=0;
        evolving=false;
        evolvePeriod=20000;
        evolvedSibs=0;
        highestScoringShape = new ifsShape();
    }

    public void offSpring(ifsShape _baseShape){
        boolean staySymmetric = false;
        evolvedSibs=0;
        baseShape=_baseShape;
        shapeList = baseShape.getPerturbedVersions(sibsPerGen,0.02f,  staySymmetric);

        familyHistory.add(shapeListCopy());
        System.out.println("Generation " + familyHistory.size() + " - " + shapeList.size() + " siblings");
    }

    public void parents(ifsShape _baseShape){
        evolvedSibs=0;
        baseShape=_baseShape;
        shapeList = familyHistory.get(familyHistory.size()-2);
        familyHistory.remove(familyHistory.size()-1);
        System.out.println("Generation " + familyHistory.size() + " - " + shapeList.size() + " siblings");
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
        System.out.println("Generation " + familyHistory.size() + " - Sibling " +shapeIndex + "/" + shapeList.size());
        return shapeList.get(shapeIndex);
    }

    public ifsShape nextShape(float score){
        shapeList.get(shapeIndex).score = score;
        shapeIndex++;shapeIndex=(shapeIndex+sibsPerGen)%sibsPerGen;
        System.out.println("Generation " + familyHistory.size() + " - Sibling " +shapeIndex + "/" + shapeList.size());
        return shapeList.get(shapeIndex);
    }

    public ArrayList<ifsShape> shapeListCopy(){
        ArrayList<ifsShape> newList = new ArrayList<ifsShape>();

        for (int i = 0; i < shapeList.size(); i++) {
            newList.add(new ifsShape(shapeList.get(i)));
        }

        return newList;
    }
}
