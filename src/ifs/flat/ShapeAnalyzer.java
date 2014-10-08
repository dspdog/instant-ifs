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

    /*
    // dist_Point_to_Segment(): get the distance of a point to a segment
    //     Input:  a Point P and a Segment S (in any dimension)
    //     Return: the shortest distance from P to S
    float
    dist_Point_to_Segment( Point P, Segment S)
    {
         Vector v = S.P1 - S.P0;
         Vector w = P - S.P0;

         double c1 = dot(w,v);
         if ( c1 <= 0 )
              return d(P, S.P0);

         double c2 = dot(v,v);
         if ( c2 <= c1 )
              return d(P, S.P1);

         double b = c1 / c2;
         Point Pb = S.P0 + b * v;
         return d(P, Pb);
    }

    * */

    double dot_product(double x0, double y0, double z0, double x1, double y1, double z1){
        return x0*x1 + y0*y1 + z0*z1;
    }

    double dist_Point_to_Segment( double px, double py, double pz, double s0x, double s0y, double s0z, double s1x, double s1y, double s1z)
    {   //P --> px, etc
        //S --> s0x, s1x
        Vector v = S.P1 - S.P0;
        Vector w = P - S.P0;

        double c1 = dot(w,v);
        if ( c1 <= 0 )
            return d(P, S.P0);

        double c2 = dot(v,v);
        if ( c2 <= c1 )
            return d(P, S.P1);

        double b = c1 / c2;
        Point Pb = S.P0 + b * v;
        return d(P, Pb);
    }

    public double potentialFunction(double x, double y, double z){
        return distance3d(512, 512, 512, x, y, z);
    }

    private double distance3d(double X1, double Y1, double Z1, double X2, double Y2, double Z2){
        return sqrt((X2-X1)*(X2-X1)+(Y2-Y1)*(Y2-Y1)+(Z2-Z1)*(Z2-Z1));
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
