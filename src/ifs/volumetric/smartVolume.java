package ifs.volumetric;

public class SmartVolume { //partitions the space into subVolumes but ignores empty space
    int size;
    int subRes;
    int subResSq;
    int subResCu;
    public int totalRegions;

    private SubVolume[] data;

    boolean inited = false;

    public SmartVolume(int _size){
        if(_size% SubVolume.size ==0){
            size=_size;
            subRes = size/ SubVolume.size;
            subResSq = subRes*subRes;
            subResCu = subRes*subRes*subRes;
            reset();
        }else{
            System.out.println("Volume size must be multiple of subvolume size!");
        }
    }

    public void reset(){
        totalRegions=0;
        if(!inited){
            data = new SubVolume[subResCu];
            inited=true;
        }
        for(int x=0; x<subResCu; x++){
            data[x] = new SubVolume();
            totalRegions++;
        }
    }

    public int getInitCount(){
        int count = 0;
        for(int x=0; x<subResCu; x++){
            if(data[x].inited)count++;
        }
        return count;
    }

    public boolean isNotEmpty(int x, int y, int z){
        return getData(x,y,z) > 0;
    }

    public boolean putData(int x, int y, int z, int increment){
        return data[(x>> SubVolume.sizeLog2)
                +(y>> SubVolume.sizeLog2)*subRes
                +(z>> SubVolume.sizeLog2)*subResSq].putData(x& SubVolume.sizeMask, y& SubVolume.sizeMask, z& SubVolume.sizeMask, increment);
    }

    public void clearData(int x, int y, int z){
        data[(x>> SubVolume.sizeLog2)
                +(y>> SubVolume.sizeLog2)*subRes
                +(z>> SubVolume.sizeLog2)*subResSq].clearData(x & SubVolume.sizeMask, y & SubVolume.sizeMask, z & SubVolume.sizeMask);
    }

    public void clipData(int x, int y, int z){
        data[(x>> SubVolume.sizeLog2)
                +(y>> SubVolume.sizeLog2)*subRes
                +(z>> SubVolume.sizeLog2)*subResSq].clipData(x & SubVolume.sizeMask, y & SubVolume.sizeMask, z & SubVolume.sizeMask);
    }
    
    public float getData(int x, int y, int z){
        return data[(x>> SubVolume.sizeLog2)
                +(y>> SubVolume.sizeLog2)*subRes
                +(z>> SubVolume.sizeLog2)*subResSq].getData(x& SubVolume.sizeMask, y& SubVolume.sizeMask, z& SubVolume.sizeMask);
    }
}
