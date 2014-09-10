package ifs.volumetric;

public final class SmartVolume { //partitions the space into subVolumes but ignores empty space
    int size;
    int subRes;
    int subResSq;
    int subResCu;
    public int totalRegions;

    //private static final int size2=1000;
    //private static final float[] fdata = new float[size2*size2*size2];

    private SubVolume[] data;

    public SmartVolume(int _size){
        if(_size% SubVolume.size ==0){
            size=_size;
            subRes = size/ SubVolume.size;
            subResSq = subRes*subRes;
            subResCu = subRes*subRes*subRes;
            firstTimeReset();
        }else{
            System.out.println("Volume size must be multiple of subvolume size!");
        }
    }

    public void firstTimeReset(){
        //for(int i=0; i<fdata.length; i++){
        //    fdata[i]=0.1f;
        //}
        System.out.println("reset....");
        totalRegions=0;
        data = new SubVolume[subResCu];

        for(int x=0; x<subResCu; x++){
            data[x] = new SubVolume();
            totalRegions++;
        }
    }

    public void reset(){
        totalRegions=0;
        for(int x=0; x<subResCu; x++){
            if(data[x] != null && data[x].inited){data[x] = new SubVolume();}
            totalRegions++;
        }
    }

    public int getInitCount(){
        int count = 0;
        for(int x=0; x<subResCu; x++){
            if(data[x] != null && data[x].inited)count++;
        }
        return count;
    }

    public boolean isNotEmpty(int x, int y, int z){
        return getData(x,y,z) > 0;
    }

    public int putData(int x, int y, int z, int increment){
        return data[(x>> SubVolume.sizeLog2)
                +(y>> SubVolume.sizeLog2)*subRes
                +(z>> SubVolume.sizeLog2)*subResSq].putData(x& SubVolume.sizeMask, y& SubVolume.sizeMask, z& SubVolume.sizeMask, increment);
    }

    public float getData(int x, int y, int z){
        return data[(x>> SubVolume.sizeLog2)
                +(y>> SubVolume.sizeLog2)*subRes
                +(z>> SubVolume.sizeLog2)*subResSq].getData(x& SubVolume.sizeMask, y& SubVolume.sizeMask, z& SubVolume.sizeMask);
    }
}
