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





    public double metaPotential(double x, double y, double z){
        double potential = 0;
        for(int i=0; i<5; i++){
            potential+=1.0/(distSqToLineByIndex(x, y, z, i));
        }
        return potential;
    }

    public double distSqToLineByIndex(double x, double y, double z, int lineIndex){ //
        float X1 = getX1(lineIndex);
        float Y1 = getY1(lineIndex);
        float Z1 = getZ1(lineIndex);

        float X2 = getX2(lineIndex);
        float Y2 = getY2(lineIndex);
        float Z2 = getZ2(lineIndex);

        return distSq_Point_to_Segment(x, y, z, X1, Y1, Z1, X2, Y2, Z2);
    }

    double dot_product(double x0, double y0, double z0, double x1, double y1, double z1){
        return x0*x1 + y0*y1 + z0*z1;
    }

    double distSq_Point_to_Segment(double px, double py, double pz, double s0x, double s0y, double s0z, double s1x, double s1y, double s1z)
    {
        // distSq_Point_to_Segment(): get the distance of a point to a segment
        //     Input:  a Point P and a Segment S (in any dimension)
        //     Return: the shortest distance from P to S
        //P --> px, etc
        //S --> s0x, s1x
        //Vector v = S.P1 - S.P0;
        double vx = s1x - s0x;
        double vy = s1y - s0y;
        double vz = s1z - s0z;

        //Vector w = P - S.P0;
        double wx = px - s0x;
        double wy = py - s0y;
        double wz = pz - s0z;


        double c1 = dot_product(wx, wy, wz, vx, vy, vz);
        if ( c1 <= 0 )
            return distance3d_squared(px, py, pz, s0x, s0y, s0z);

        double c2 = dot_product(vx, vy, vz, vx, vy, vz);
        if ( c2 <= c1 )
            return distance3d_squared(px, py, pz, s1x, s1y, s1z);

        double b = c1 / c2;

        //Point Pb = S.P0 + b * v;
        double pbx = s0x + b*vx;
        double pby = s0y + b*vy;
        double pbz = s0z + b*vz;

        return distance3d_squared(px, py, pz, pbx, pby, pbz);
    }

    public double potentialFunction(double x, double y, double z){
        return metaPotential(x,y,z);
        //return distance3d_squared(512, 512, 512, x, y, z);
    }

    private double distance3d_squared(double X1, double Y1, double Z1, double X2, double Y2, double Z2){ //SQUARED DISTANCE
        return ((X2-X1)*(X2-X1)+(Y2-Y1)*(Y2-Y1)+(Z2-Z1)*(Z2-Z1));
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
