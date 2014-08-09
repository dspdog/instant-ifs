import java.util.ArrayList;
import java.util.Collections;

public class EvolvingShape {
    ArrayList<ArrayList<ifsShape>> familyHistory;
    ArrayList<ifsShape> shapeList;
    ifsShape baseShape;
    int shapeIndex;
    boolean evolving;
    long evolvePeriod;
    int evolvedSibs=0;

    final int sibsPerGen = 20;

    public EvolvingShape(ifsShape _baseShape){
        familyHistory = new ArrayList<ArrayList<ifsShape>>();
        baseShape=_baseShape;
        shapeIndex=0;
        evolving=false;
        evolvePeriod=60000;
        evolvedSibs=0;
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
        //int highestIndex=0;
        float highestScore=Float.MAX_VALUE*-1;
        ifsShape highestScoringShape = new ifsShape();

        for (int i = 0; i < evolvedSibs; i++) {
             if(shapeList.get(i).score>highestScore){
                 highestScoringShape = shapeList.get(i);
                 highestScore = highestScoringShape.score;
                 //highestIndex = i;
             }
        }
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
