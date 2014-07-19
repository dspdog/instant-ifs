public class subVolume {
    static final int size = 64;
    static final int sizeMask = 63;
    static final int sizeLog2 = 6;

    static final int sizeSq = size*size;
    static final int sizeCu = size*size*size;

    float[] data;
    boolean[] isInterior;
    boolean inited;

    public subVolume(){
        inited=false;
    }

    public void init(){
        data = new float[sizeCu];
        isInterior = new boolean[sizeCu];
        inited=true;
    }

    public boolean putData(int x, int y, int z, float val){
        if(!inited){
            init();
        }
        boolean isFirst = data[x+y*size+z*sizeSq]<0.01;
        data[x+y*size+z*sizeSq]+=val;
        return isFirst;
    }

    public void flagInterior(int x, int y, int z){
        if(!inited){
            init();
        }
        isInterior[x+y*size+z*sizeSq]=true;
    }

    public float getData(int x, int y, int z){
        if(inited){
            return data[x+y*size+z*sizeSq];
        }else{
            return 0;
        }
    }

    public boolean getIsInterior(int x, int y, int z){
        if(inited){
            return isInterior[x+y*size+z*sizeSq];
        }else{
            return false;
        }
    }

    public void clearData(int x, int y, int z){
        if(inited){
            data[x+y*size+z*sizeSq]=0;
            isInterior[x+y*size+z*sizeSq]=false;
        }
    }

    public void clipData(int x, int y, int z){
        if(inited){
            data[x+y*size+z*sizeSq]=Math.min(data[x+y*size+z*sizeSq], 255);
        }
    }
}
