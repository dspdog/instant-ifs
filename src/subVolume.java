public class subVolume {
    static final int size = 16;
    static final int sizeMask = 15;
    static final int sizeLog2 = 4;

    double[][][] data;
    boolean inited;

    public subVolume(){
        inited=false;
    }

    public void init(){
        data = new double[size][size][size];
        for(int x=0; x<size; x++){
            for(int y=0; x<size; x++){
                for(int z=0; x<size; x++){
                    data[x][y][z]=0;
                }
            }
        }
        inited=true;
    }

    public void putData(int x, int y, int z, double val){
        if(inited){
            data[x][y][z]=val;
        }else{
            init();
            data[x][y][z]=val;
        }
    }

    public void incrementData(int x, int y, int z, double val){
        if(inited){
            data[x][y][z]+=val;
        }else{
            init();
            data[x][y][z]+=val;
        }
    }

    public double getData(int x, int y, int z){
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
