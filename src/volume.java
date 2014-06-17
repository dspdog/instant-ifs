
public class volume {
    int width, height, depth;

    RenderMode renderMode;

    long drawTime = 0;
    long totalSamples = 0;

    double totalSamplesAlpha =0;
    double dataMax=0;
    double dataMaxVolumetric=0;
    double dataMaxReset = 0;

    double surfaceArea=0;

    public smartVolume volume;

    public double XYProjection[][];
    public double XZProjection[][];
    public double YZProjection[][];

    public int depthLeanX, depthLeanY;

    ifsPt centroid;
    ifsPt highPt;
    ifsPt highPtVolumetric;

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
        //if(renderMode == RenderMode.VOLUMETRIC){
            //volume = new double[width][height][depth];
            volume = new smartVolume(width);
        //}

        centroid = new ifsPt(0,0,0);
    }

    public void clear(){
        drawTime=System.currentTimeMillis();
        totalSamples=1;
        totalSamplesAlpha =1;
        centroid = new ifsPt(0,0,0);
        dataMax=dataMaxReset;
        dataMaxVolumetric=dataMaxReset;

        if(renderMode == renderMode.VOLUMETRIC){
            volume.reset();
        }

        double[][] proj = getProjection();

        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                proj[x][y]=0;
            }
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

            if(renderMode==renderMode.VOLUMETRIC){
                if(antiAliasing){
                    double xDec = pt.x - (int)pt.x;
                    double yDec = pt.y - (int)pt.y;
                    double zDec = pt.z - (int)pt.z;

                    volume.putData((int) pt.x, (int) pt.y, (int) pt.z, alpha * (1 - xDec) * (1 - yDec) * (1 - zDec));
                    volume.putData((int) pt.x + 1, (int) pt.y, (int) pt.z, alpha * xDec * (1 - yDec) * (1 - zDec));
                    volume.putData((int) pt.x, (int) pt.y + 1, (int) pt.z, alpha * (1 - xDec) * yDec * (1 - zDec));
                    volume.putData((int) pt.x + 1, (int) pt.y + 1, (int) pt.z, alpha * xDec * yDec * (1 - zDec));

                    volume.putData((int) pt.x, (int) pt.y, (int) pt.z + 1, alpha * (1 - xDec) * (1 - yDec) * zDec);
                    volume.putData((int) pt.x + 1, (int) pt.y, (int) pt.z + 1, alpha * xDec * (1 - yDec) * zDec);
                    volume.putData((int) pt.x, (int) pt.y + 1, (int) pt.z + 1, alpha * (1 - xDec) * yDec * zDec);
                    volume.putData((int) pt.x + 1, (int) pt.y + 1, (int) pt.z + 1, alpha * xDec * yDec * zDec);

                }else{
                    volume.putData((int) pt.x, (int) pt.y, (int) pt.z + 1, alpha);
                }

                if(volume.getData((int)pt.x, (int)pt.y, (int)pt.y)>dataMaxVolumetric){
                    dataMaxVolumetric= volume.getData((int)pt.x, (int)pt.y, (int)pt.y);//volume[(int)pt.x][(int)pt.y][(int)pt.z];
                    highPtVolumetric = new ifsPt(pt);
                }
            }

            totalSamples++;
            totalSamplesAlpha +=alpha;
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
        }
    }

    public ifsPt getCentroid(){
        return new ifsPt(centroid.x/ totalSamplesAlpha, centroid.y/ totalSamplesAlpha, centroid.z/ totalSamplesAlpha);
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

    public static double[][] getPotential2D(double[][] map, int radius){ // map must be square!
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

    public static smartVolume getPotential3D(smartVolume map, int radius){ // map must be square!
        int size = map.size;

        smartVolume res = new smartVolume(size);

        double invDistance[][][] = new double[radius*2][radius*2][radius*2];
        int x,y,z;
        int x2,y2,z2;

        double d2;

        for(x2=-radius; x2<radius; x2++){
            for(y2=-radius; y2<radius; y2++){
                for(z2=-radius; z2<radius; z2++){
                    d2 = (x2*x2 + y2*y2 + z2*z2);
                    if(d2<1){d2=1;}
                    invDistance[x2+radius][y2+radius][z2+radius] = 1.0/d2;
                    if(d2>radius){invDistance[x2+radius][y2+radius][z2+radius]=0;}
                }
            }
        }

        double addition;
        int x1,y1,z1;
        //iterate through sub-domains, skipping empty ones

        for(x1=0; x1<map.subRes; x1++){
            for(y1=0; y1<map.subRes; y1++){
                for(z1=0; z1<map.subRes; z1++){

                    if(map.isNotEmpty(x1,y1,z1)){ //skip empty domains
                        for(x=x1*subVolume.size; x<(x1+1)*subVolume.size; x++){
                            for(y=y1*subVolume.size; y<(y1+1)*subVolume.size; y++){
                                for(z=z1*subVolume.size; z<(z1+1)*subVolume.size; z++){
                                    res.clearData(x,y,z);
                                    addition=0;
                                    for(x2=-radius; x2<radius; x2++){
                                        for(y2=-radius; y2<radius; y2++){
                                            for(z2=-radius; z2<radius; z2++){
                                                addition+=map.getData(x+x2,y+y2,z+z2)*invDistance[x2+radius][y2+radius][z2+radius];
                                            }
                                        }
                                    }
                                    if(addition>0){
                                        res.putData(x,y,z,addition/2);
                                        res.clipData(x, y, z);
                                    }
                                }
                            }
                        }
                    }


                }
            }
            if(x1%10==0)
                System.out.println("3D POTENTIAL " + x1 + "/" + map.subRes);
        }



        return res;
    }

    public static double[][] getProjectionCopy(double[][] map){
        int width = map.length;

        double[][] res = new double[width][width];
        int x,y;

        for(x=0; x<width; x++){
            for(y=0; y<width; y++){
                res[x][y]=map[x][y]+0;
            }
        }

        return res;
    }

    public double[][] findEdges2D(double[][] map){
        int width = map.length;

        double total =0;

        double[][] res = new double[width][width];
        int x,y;

        for(x=3; x<width-3; x++){
            for(y=3; y<width-3; y++){

                double edges1 = Math.max(
                                Math.max(
                                        (map[x][y]-map[x-1][y]),
                                        (map[x][y] - map[x + 1][y])),
                                Math.max((map[x][y]-map[x][y-1]),
                                        (map[x][y]-map[x][y+1]))
                );

                total+=edges1/255.0;

                edges1 = Math.min(edges1, 255);
                res[x][y]=edges1;
            }
        }

        surfaceArea = total;

        return res;
    }

    public static double[][] getThreshold2D(double[][] map, int threshold){
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

    public static enum RenderMode {
        VOLUMETRIC, SIDES_ONLY
    }

    public static enum ViewDirection{
        XY, XZ, YZ
    }

}
