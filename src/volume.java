/**
 * Created by Labrats on 6/4/14.
 */
public class volume {
    int width, height, depth;

    RenderMode renderMode;

    double totalSamples=0;
    double dataMax=0;

    public double volume[][][];
    public double XYProjection[][];
    public double XZProjection[][];
    public double YZProjection[][];

    public int depthLeanX, depthLeanY;

    ifsPt centerOfGravity;
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
        volume = new double[width][height][depth];
        centerOfGravity = new ifsPt(0,0,0);
    }

    public void clear(){
        totalSamples=1;
        centerOfGravity = new ifsPt(0,0,0);
        dataMax=1;

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
        centerOfGravity.x+=pt.x*alpha;
        centerOfGravity.y+=pt.y*alpha;
        centerOfGravity.z+=pt.z*alpha;

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

    public ifsPt getCenterOfGravity(){
        return new ifsPt(centerOfGravity.x/totalSamples, centerOfGravity.y/totalSamples, centerOfGravity.z/totalSamples);
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
