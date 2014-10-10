package ifs.flat;

import com.amd.aparapi.Kernel;

public final class ShapeAnalyzer extends Kernel{

    public final int lineXY1[]; //X,Y coords
    public final int lineZS1[]; //z coord, scale
    public final int lineXY2[];
    public final int lineZS2[];
    public final int lineDI[]; //distance, iterations

    final int NUM_LINES = 1024*1024/2;

    public ShapeAnalyzer(){

        lineXY1 = new int[NUM_LINES];
        lineZS1 = new int[NUM_LINES];
        lineXY2 = new int[NUM_LINES];
        lineZS2 = new int[NUM_LINES];
        lineDI = new int[NUM_LINES];

        this.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
    }

    @Override
    public void run() {
        int x = getGlobalId(0);
        int y = getGlobalId(1);
    }


    public void updateGeometry(){
        this.put(lineXY1).put(lineZS1).put(lineDI)
            .put(lineXY2).put(lineZS2);
    }



    private int getX1(int index){
        return lineXY1[index]>>16;
    }

    private int getY1(int index){
        return lineXY1[index]&65535;
    }

    private int getX2(int index){
        return lineXY2[index]>>16;
    }

    private int getY2(int index){
        return lineXY2[index]&65535;
    }

    private int getZ1(int index){
        return lineZS1[index]>>16;
    }

    private int getS1(int index){
        return lineZS1[index]&65535;
    }

    private int getZ2(int index){
        return lineZS2[index]>>16;
    }

    private int getS2(int index){
        return lineZS2[index]&65535;
    }
}
