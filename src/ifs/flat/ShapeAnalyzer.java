package ifs.flat;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

public final class ShapeAnalyzer extends Kernel{

    public final int lineXY1[]; //X,Y coords
    public final int lineZS1[]; //z coord, scale
    public final int lineXY2[];
    public final int lineZS2[];
    public final int lineDI[]; //distance, iterations

    int zListTotal=0;
    public final int ZList[]; //ZList denotes which lines (index) affect current z plane

    public final double linePot[];

    public final int width = 1024/4;

    final int NUM_LINES = width*width;

    double myZ=0;
    int cutoffDist=0;

    public ShapeAnalyzer(){
        zListTotal=0;
        lineXY1 = new int[NUM_LINES];
        lineZS1 = new int[NUM_LINES];
        lineXY2 = new int[NUM_LINES];
        lineZS2 = new int[NUM_LINES];
        lineDI = new int[NUM_LINES];
        linePot = new double[NUM_LINES];
        ZList = new int[NUM_LINES];

        this.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
    }

    @Override
    public void run() {
        int x = getGlobalId(0);
        int y = getGlobalId(1);

        int stepSize = 1024/width;

        double res = metaPotential(x*stepSize, y*stepSize, myZ, cutoffDist/stepSize);
        linePot[x+y*width] = res;
    }

    public void getAllPotentialsByZ(double z, int _maxDist){
        this.setExplicit(true);

        cutoffDist=_maxDist;
        myZ = z;

        Range range = Range.create2D(width,width);

        this.execute(range);
        this.get(linePot);
    }

    public void updateZList(int total){
        this.put(ZList);
        zListTotal = total;
    }

    public void updateGeometry(){
        this.put(lineXY1).put(lineZS1).put(lineDI)
            .put(lineXY2).put(lineZS2);
    }

    public double singlePotential(double x, double y, double z, double X1,double  Y1, double Z1, double S1, double X2,double  Y2, double Z2, double S2){
        return 1.0/(distSq_Point_to_Segment(x, y, z, X1, Y1, Z1, S1, X2, Y2, Z2, S2));
    }

    public double metaPotential(double x, double y, double z, int maxDist){
        double potential = 0;
        int x1,x2,y1,y2,z1,z2, _min, _max;

        float s1, s2;

        for(int i=0; i<zListTotal; i++){
            int _i = ZList[i];
            z1 = getZ1(_i);
            z2 = getZ2(_i);
            x1 = getX1(_i);
            x2 = getX2(_i);
            _min= min(x1,x2)-maxDist;
            _max= max(x1,x2)+maxDist;
            if(_min<x && _max>x){
                y1 = getY1(_i);
                y2 = getY2(_i);
                _min= min(y1,y2)-maxDist;
                _max= max(y1,y2)+maxDist;
                if(_min<y && _max>y){
                    s1 = getS1(_i);
                    s2 = getS2(_i);
                    //potential+=singlePotential(x,y,z,x1,y1,z1,s1,x2,y2,z2,s2); //"meta balls" mode
                    potential=max(potential,singlePotential(x,y,z,x1,y1,z1,s1,x2,y2,z2,s2)); //"tubes" mode
                }
            }
        }

        return potential;
    }

    double dot_product(double x0, double y0, double z0, double x1, double y1, double z1){
        return x0*x1 + y0*y1 + z0*z1;
    }

    double distSq_Point_to_Segment(double px, double py, double pz, double s0x, double s0y, double s0z, double s0s, double s1x, double s1y, double s1z, double s1s)//todo update name as this also scales
    {
        if(s0x<1)return 1000000d;
        // distSq_Point_to_Segment(): get the distance of a point to a segment
        //     Input:  a Point P and a Segment S (in any dimension)
        //     Return: the shortest distance from P to S
        //P --> px, etc
        //S --> s0x, s1x
        //Vector v = S.P1 - S.P0;
        double vx = s1x - s0x;
        double vy = s1y - s0y;
        double vz = s1z - s0z;
        double vs = s1s - s0s;

        //Vector w = P - S.P0;
        double wx = px - s0x;
        double wy = py - s0y;
        double wz = pz - s0z;

        double c1 = dot_product(wx, wy, wz, vx, vy, vz);
        if ( c1 <= 0 )
            return scaleDist(distance3d_squared(px, py, pz, s0x, s0y, s0z), s0s);

        double c2 = dot_product(vx, vy, vz, vx, vy, vz);
        if ( c2 <= c1 )
            return scaleDist(distance3d_squared(px, py, pz, s1x, s1y, s1z), s1s);

        double b = c1 / c2;

        //Point Pb = S.P0 + b * v;
        double pbx = s0x + b*vx;
        double pby = s0y + b*vy;
        double pbz = s0z + b*vz;
        double pbs = s0s + b*vs;

        return scaleDist(distance3d_squared(px, py, pz, pbx, pby, pbz), pbs);
    }

    double scaleDist(double dist, double scale){
        return dist/scale;
    }

    private double distance3d_squared(double X1, double Y1, double Z1, double X2, double Y2, double Z2){ //SQUARED DISTANCE
        return (X2-X1)*(X2-X1)+(Y2-Y1)*(Y2-Y1)+(Z2-Z1)*(Z2-Z1);
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

    private float getS1(int index){
        return (lineZS1[index]&65535)/256f;
    }

    private int getZ2(int index){
        return lineZS2[index]>>16;
    }

    private float getS2(int index){
        return (lineZS2[index]&65535)/256f;
    }
}
