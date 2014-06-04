/**
 * Created by Labrats on 6/4/14.
 */
public class volume {
    int width, height, depth;

    double totalSamples=0;
    double dataMax=0;

    public double volume[][][];

    ifsPt centerOfGravity;

    public volume(int w, int h, int d){
        width = w;
        height = h;
        depth = d;
        volume = new double[width][height][depth];
        centerOfGravity = new ifsPt(0,0,0);
    }

    public void clear(){
        totalSamples=1;
        centerOfGravity = new ifsPt(0,0,0);
        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                for(int z=0; z<depth; z++){
                    volume[x][y][z]=0;
                }
            }
        }
        dataMax=1;
    }

    public void putPixel(ifsPt pt, double alpha, boolean antialiasing){
        pt.x=Math.max(pt.x,1);
        pt.y=Math.max(pt.y,1);
        pt.z=Math.max(pt.z,1);
        pt.x=Math.min(pt.x, width-2);
        pt.y=Math.min(pt.y,height-2);
        pt.z=Math.min(pt.z,depth-2);

        totalSamples+=alpha;

        centerOfGravity.x+=pt.x*alpha;
        centerOfGravity.y+=pt.y*alpha;
        centerOfGravity.z+=pt.z*alpha;

        if(antialiasing){
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
}
