/**
 * Created by Labrats on 6/4/14.
 */
public class volume {
    int width, height, depth;

    public double volume[][][];

    public volume(){
        width = 512;
        height = 512;
        depth = 512;
        volume = new double[width][height][depth];
    }

    public volume(int w, int h, int d){
        width = w;
        height = h;
        depth = d;
        volume = new double[width][height][depth];
    }

    public void clear(){
        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                for(int z=0; z<depth; z++){
                    volume[x][y][z]=0;
                }
            }
        }
    }

    public void putPixel(double x, double y, double z, boolean antialiasing){
        x=Math.max(x,0);
        y=Math.max(y,0);
        z=Math.max(z,0);
        x=Math.min(x,width-1);
        y=Math.min(y,height-1);
        z=Math.min(z,depth-1);

        volume[(int)x][(int)y][(int)z]=1;
    }

    public double[] getFullSliceXY(){
        double[] slice = new double[width*height];

        int i=0;
        for(int y=0; y<height; y++){
            for(int x=0; x<width; x++){
                slice[i] = getSliceXY_Max(x, y);
                i++;
            }
        }

        return slice;
    }

    public double getSliceXY_Max(int x, int y){
        double max = 0;
        for(int z=0; z<depth; z++){
            max=Math.max(volume[x][y][z], max);
        }
        return max;
    }
}
