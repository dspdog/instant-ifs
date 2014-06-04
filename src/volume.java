/**
 * Created by Labrats on 6/4/14.
 */
public class volume {
    int width, height, depth;

    double totalSamples=0;

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
    }

    public void putPixel(ifsPt pt, double alpha, boolean antialiasing){
        pt.x=Math.max(pt.x,0);
        pt.y=Math.max(pt.y,0);
        pt.z=Math.max(pt.z,0);
        pt.x=Math.min(pt.x, width - 1);
        pt.y=Math.min(pt.y,height-1);
        pt.z=Math.min(pt.z,depth-1);

        totalSamples+=alpha;

        centerOfGravity.x+=pt.x*alpha;
        centerOfGravity.y+=pt.y*alpha;
        centerOfGravity.z+=pt.z*alpha;

        volume[(int)pt.x][(int)pt.y][(int)pt.z]=alpha;
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
