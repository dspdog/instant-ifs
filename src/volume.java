/**
 * Created by Labrats on 6/4/14.
 */
public class volume {
    int width, height, depth;

    RenderMode renderMode;

    double totalSamples=0;
    double dataMax=0;
    double dataMaxReset = 0;

    double surfaceArea=0;

    public double volume[][][];
    public double XYProjection[][];
    public double XZProjection[][];
    public double YZProjection[][];

    public int depthLeanX, depthLeanY;

    ifsPt centroid;
    ifsPt highPt;

    ViewDirection preferredDirection;

    boolean antiAliasing;

    public volume(int w, int h, int d){
        width = w;
        height = h;
        depth = d;
        depthLeanX = 0;
        depthLeanY = 0;
        renderMode = RenderMode.SIDES_ONLY;
        preferredDirection = ViewDirection.XY;
        antiAliasing = true;
        XYProjection = new double[width][height];
        XZProjection = new double[width][depth];
        YZProjection = new double[height][depth];
        if(renderMode == RenderMode.VOLUMETRIC){
            volume = new double[width][height][depth];
        }

        centroid = new ifsPt(0,0,0);
    }

    public void clear(){
        totalSamples=1;
        centroid = new ifsPt(0,0,0);
        dataMax=dataMaxReset;

        switch (renderMode){
            case VOLUMETRIC:
                for(int x=0; x<width; x++){
                    for(int y=0; y<height; y++){
                        for(int z=0; z<depth; z++){
                            volume[x][y][z]=0;
                        }
                    }
                }
                break;
            case SIDES_ONLY:

                for(int x=0; x<width; x++){
                    for(int y=0; y<height; y++){
                        XYProjection[x][y]=0;
                    }
                }

                for(int x=0; x<width; x++){
                    for(int z=0; z<depth; z++){
                        XZProjection[x][z]=0;
                    }
                }

                for(int y=0; y<height; y++){
                    for(int z=0; z<depth; z++){
                        YZProjection[y][z]=0;
                    }
                }
                break;
        }
    }

    public void putPixel(ifsPt pt, double alpha){
        centroid.x+=pt.x*alpha;
        centroid.y+=pt.y*alpha;
        centroid.z+=pt.z*alpha;

        switch (preferredDirection){
            case XY:
                pt.x += (pt.z-depth/2)/(depth/2)*depthLeanX;
                pt.y += (pt.z-depth/2)/(depth/2)*depthLeanY;
                break;
            case XZ:
                pt.x += (pt.y-height/2)/(height/2)*depthLeanX;
                pt.z += (pt.y-height/2)/(height/2)*depthLeanY;
                break;
            case YZ:
                pt.y += (pt.x-width/2)/(width/2)*depthLeanX;
                pt.z += (pt.x-width/2)/(width/2)*depthLeanY;
                break;
        }

        if(pt.x>1 && pt.y>1 && pt.z>1 && pt.x<width-1 && pt.y<height-1 && pt.z<depth-1){

            switch (renderMode){
                case VOLUMETRIC:

                    totalSamples+=alpha;
                    if(antiAliasing){
                        double xDec = pt.x - (int)pt.x;
                        double yDec = pt.y - (int)pt.y;
                        double zDec = pt.z - (int)pt.z;

                        volume[(int)pt.x][(int)pt.y][(int)pt.z] += alpha*(1-xDec)*(1-yDec)*(1-zDec);
                        volume[(int)pt.x+1][(int)pt.y][(int)pt.z] += alpha*xDec*(1-yDec)*(1-zDec);
                        volume[(int)pt.x][(int)pt.y+1][(int)pt.z] += alpha*(1-xDec)*yDec*(1-zDec);
                        volume[(int)pt.x+1][(int)pt.y+1][(int)pt.z] += alpha*xDec*yDec*(1-zDec);

                        volume[(int)pt.x][(int)pt.y][(int)pt.z+1] += alpha*(1-xDec)*(1-yDec)*zDec;
                        volume[(int)pt.x+1][(int)pt.y][(int)pt.z+1] += alpha*xDec*(1-yDec)*zDec;
                        volume[(int)pt.x][(int)pt.y+1][(int)pt.z+1] += alpha*(1-xDec)*yDec*zDec;
                        volume[(int)pt.x+1][(int)pt.y+1][(int)pt.z+1] += alpha*xDec*yDec*zDec;
                    }else{
                        volume[(int)pt.x][(int)pt.y][(int)pt.z]+=alpha;
                    }

                    if(volume[(int)pt.x][(int)pt.y][(int)pt.z]>dataMax){
                        dataMax=volume[(int)pt.x][(int)pt.y][(int)pt.z];
                        highPt = new ifsPt(pt);
                    }

                    break;
                case SIDES_ONLY:

                    totalSamples+=alpha;
                    if(antiAliasing){
                        double xDec = pt.x - (int)pt.x;
                        double yDec = pt.y - (int)pt.y;
                        double zDec = pt.z - (int)pt.z;

                        switch (preferredDirection){
                            case XY:
                                XYProjection[(int)pt.x][(int)pt.y] += alpha*(1-xDec)*(1-yDec);
                                XYProjection[(int)pt.x+1][(int)pt.y] += alpha*xDec*(1-yDec);
                                XYProjection[(int)pt.x][(int)pt.y+1] += alpha*(1-xDec)*yDec;
                                XYProjection[(int)pt.x+1][(int)pt.y+1] += alpha*xDec*yDec;
                                break;
                            case XZ:
                                XZProjection[(int)pt.x][(int)pt.z] += alpha*(1-xDec)*(1-zDec);
                                XZProjection[(int)pt.x+1][(int)pt.z] += alpha*xDec*(1-zDec);
                                XZProjection[(int)pt.x][(int)pt.z+1] += alpha*(1-xDec)*zDec;
                                XZProjection[(int)pt.x+1][(int)pt.z+1] += alpha*xDec*zDec;
                                break;
                            case YZ:
                                YZProjection[(int)pt.y][(int)pt.z] += alpha*(1-yDec)*(1-zDec);
                                YZProjection[(int)pt.y+1][(int)pt.z] += alpha*yDec*(1-zDec);
                                YZProjection[(int)pt.y][(int)pt.z+1] += alpha*(1-yDec)*zDec;
                                YZProjection[(int)pt.y+1][(int)pt.z+1] += alpha*yDec*zDec;
                                break;
                        }
                    }else{
                        XYProjection[(int)pt.x][(int)pt.y]+=alpha;
                        XZProjection[(int)pt.x][(int)pt.z] += alpha;
                        YZProjection[(int)pt.y][(int)pt.z] += alpha;
                    }

                    if(XYProjection[(int)pt.x][(int)pt.y]>dataMax){
                        dataMax= XYProjection[(int)pt.x][(int)pt.y];
                        highPt = new ifsPt(pt);
                    }

                    if(XZProjection[(int)pt.x][(int)pt.z]>dataMax){
                        dataMax= XZProjection[(int)pt.x][(int)pt.z];
                        highPt = new ifsPt(pt);
                    }

                    if(YZProjection[(int)pt.y][(int)pt.z]>dataMax){
                        dataMax= YZProjection[(int)pt.y][(int)pt.z];
                        highPt = new ifsPt(pt);
                    }
                    break;

            }
        }
    }

    public ifsPt getCentroid(){
        return new ifsPt(centroid.x/totalSamples, centroid.y/totalSamples, centroid.z/totalSamples);
    }

    public ifsPt getProjectedPt(ifsPt pt){

        ifsPt res = new ifsPt(pt, true);

        switch (preferredDirection){
            case XY:
                res.x=pt.x;
                res.y=pt.y;
                res.z=pt.z;
                break;
            case XZ:
                res.x=pt.x;
                res.y=pt.z;
                res.z=pt.y;
                break;
            case YZ:
                res.x=pt.y;
                res.y=pt.z;
                res.z=pt.x;
                break;
        }

        return res;
    }

    public double[][] getProjection(){
        switch (preferredDirection){
            case XY:
                return XYProjection;
            case XZ:
                return XZProjection;
            case YZ:
                return YZProjection;
            default:
                return XYProjection;
        }
    }

    public double[][] getScaledProjection(double brightness){
        double[][] proj = getProjection();
        double[][] scaled = new double[width][height];

        double scaler = 255.0/dataMax * brightness;

        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                scaled[x][y]= Math.min((int)(scaler*proj[x][y]), 255);
            }
        }

        return scaled;
    }

    public double[][] getPotential(double[][] map, int radius){ // map must be square!
        int width = map.length;
        double[][] res = new double[width][width];
        double invDistance[][] = new double[radius*2][radius*2];
        int x,y;
        int x2,y2;

        double d2;

        for(x2=-radius; x2<radius; x2++){
            for(y2=-radius; y2<radius; y2++){
                d2 = (x2*x2+y2*y2);
                if(d2<1){d2=1;}
                invDistance[x2+radius][y2+radius] = 1.0/d2;
                if(d2>radius){invDistance[x2+radius][y2+radius]=0;}
            }
        }

        for(x=radius; x<width-radius; x++){
            for(y=radius; y<width-radius; y++){
                res[x][y]=0;
                for(x2=-radius; x2<radius; x2++){
                    for(y2=-radius; y2<radius; y2++){
                        res[x][y]+=map[(x+x2)][(y+y2)]*invDistance[x2+radius][y2+radius]/2;
                    }
                }
                res[x][y]=Math.min(res[x][y],255);
            }
        }

        return res;
    }

    public double[][] findEdges(double[][] map){
        int width = map.length;

        double total =0;

        double[][] res = new double[width][width];
        int x,y;

        for(x=3; x<width-3; x++){
            for(y=3; y<width-3; y++){
                //double edges = Math.abs(map[x][y]-map[x-1][y]) + Math.abs(map[x][y]-map[x+1][y]) + Math.abs(map[x][y]-map[x][y-1]) + Math.abs(map[x][y]-map[x][y+1])/4;

                double edges1 = Math.max(
                                Math.max(
                                        (map[x][y]-map[x-1][y]),
                                        (map[x][y] - map[x + 1][y])),
                                Math.max((map[x][y]-map[x][y-1]),
                                        (map[x][y]-map[x][y+1]))
                );

                double edges2 = Math.max(
                        Math.max(
                                (map[x][y]-map[x-1][y-1]),
                                (map[x][y] - map[x + 1][y+1])),
                        Math.max((map[x][y]-map[x+1][y-1]),
                                (map[x][y]-map[x-1][y+1]))
                );

                double edges = Math.max(edges1, edges2 * 1.0 / Math.sqrt(2));
                total+=edges;

                edges = Math.min(edges, 255);
                res[x][y]=edges;
            }
        }

        surfaceArea = total;

        return res;
    }

    public double[][] getThreshold(double[][] map, int threshold){
        int width = map.length;

        double[][] res = new double[width][width];
        int x,y;

        for(x=0; x<width; x++){
            for(y=0; y<width; y++){
                res[x][y]=map[x][y]>threshold?255:0;
            }
        }

        return res;
    }

    public double[] getFullSliceXY(){
        double[] slice = new double[width*height];
        int i=0;

        for(int y=0; y<height; y++){
            for(int x=0; x<width; x++){
                slice[i] = getColumnMax(x, y);
                i++;
            }
        }

        return slice;
    }

    private double getColumnMax(int x, int y){
        double max = 0;
        for(int z=0; z<depth; z++){
            max=Math.max(volume[x][y][z], max);
        }
        return max;
    }

    public enum RenderMode {
        VOLUMETRIC, SIDES_ONLY
    }

    public enum ViewDirection{
        XY, XZ, YZ
    }

}
