package ifs.volumetric;

public final class SubVolume {
    public static final int size = 16;
    public static final int sizeMask = 15;
    public static final int sizeLog2 = 4;

    int[][][] data;
    boolean inited;

    public SubVolume(){
        inited=false;
    }

    public void init(){
        data = new int[size][size][size];
        inited=true;
    }

    public int putData(int x, int y, int z, int val){
        if(!inited){
            init();
        }

        data[x][y][z]+=val;
        return data[x][y][z];
    }

    public float getData(int x, int y, int z){
        if(inited){
            return data[x][y][z];
        }else{
            return 0;
        }
    }
}
