package ifs.flat;

import com.amd.aparapi.Kernel;

//TODO this class should provide potential data for cube marcher

public final class ShapeAnalyzer extends Kernel{

    final int NUM_CELLS = 1000000; //100x100x100 to start...
    final int MAX_TRIANGLES = NUM_CELLS * 5; //polygonize function can return at most 5 triangles per cell

    final float[] triangleX1, triangleY1, triangleZ1;
    final float[] triangleX2, triangleY2, triangleZ2;
    final float[] triangleX3, triangleY3, triangleZ3;

    public ShapeAnalyzer(int w, int h){
        triangleX1 = new float[MAX_TRIANGLES];
        triangleY1 = new float[MAX_TRIANGLES];
        triangleZ1 = new float[MAX_TRIANGLES];

        triangleX2 = new float[MAX_TRIANGLES];
        triangleY2 = new float[MAX_TRIANGLES];
        triangleZ2 = new float[MAX_TRIANGLES];

        triangleX3 = new float[MAX_TRIANGLES];
        triangleY3 = new float[MAX_TRIANGLES];
        triangleZ3 = new float[MAX_TRIANGLES];
        this.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
    }

    @Override
    public void run() {
        int x = getGlobalId(0);
        int y = getGlobalId(1);
    }


    public float potentialFunction(float x, float y, float z){
        return distance3d(0, 0, 0, x, y, z);
    }

    private float distance3d(float X1, float Y1, float Z1, float X2, float Y2, float Z2){
        return sqrt((X2-X1)*(X2-X1)+(Y2-Y1)*(Y2-Y1)+(Z2-Z1)*(Z2-Z1));
    }
}
