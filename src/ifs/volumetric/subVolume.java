package ifs.volumetric;

public class subVolume {
    public static final int size = 16;
    public static final int sizeMask = 15;
    public static final int sizeLog2 = 4;

    float[][][] data;
    boolean inited;

    public subVolume(){
        inited=false;
    }

    public void init(){
        data = new float[size][size][size];
        inited=true;
    }

    public boolean putData(int x, int y, int z, float val){
        if(!inited){
            init();
        }
        boolean isFirst = data[x][y][z]<0.01;
        data[x][y][z]+=val;
        return isFirst;
    }

    public float getData(int x, int y, int z){
        if(inited){
            return data[x][y][z];
        }else{
            return 0;
        }
    }

    public void clearData(int x, int y, int z){
        if(inited){
            data[x][y][z]=0;
        }
    }

    public void clipData(int x, int y, int z){
        if(inited){
            data[x][y][z]=Math.min(data[x][y][z], 255);
        }
    }
}
