public class smartVolume { //partitions the space into subVolumes but ignores empty space
    int size;
    int subRes;
    int subResSq;
    int subResCu;
    int totalRegions;

    private subVolume[] data;

    boolean inited = false;

    public smartVolume(int _size){
        if(_size%subVolume.size ==0){
            size=_size;
            subRes = size/subVolume.size;
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
            data = new subVolume[subResCu];
            inited=true;
        }
        for(int x=0; x<subResCu; x++){
            data[x] = new subVolume();
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

    public boolean putData(int x, int y, int z, float increment){
        return data[(x>>subVolume.sizeLog2)
                +(y>>subVolume.sizeLog2)*subRes
                +(z>>subVolume.sizeLog2)*subResSq].putData(x&subVolume.sizeMask, y&subVolume.sizeMask, z&subVolume.sizeMask, increment);
    }

    public void clearData(int x, int y, int z){
        data[(x>>subVolume.sizeLog2)
                +(y>>subVolume.sizeLog2)*subRes
                +(z>>subVolume.sizeLog2)*subResSq].clearData(x & subVolume.sizeMask, y & subVolume.sizeMask, z & subVolume.sizeMask);
    }

    public void clipData(int x, int y, int z){
        data[(x>>subVolume.sizeLog2)
                +(y>>subVolume.sizeLog2)*subRes
                +(z>>subVolume.sizeLog2)*subResSq].clipData(x & subVolume.sizeMask, y & subVolume.sizeMask, z & subVolume.sizeMask);
    }
    
    public float getData(int x, int y, int z){
        return data[(x>>subVolume.sizeLog2)
                +(y>>subVolume.sizeLog2)*subRes
                +(z>>subVolume.sizeLog2)*subResSq].getData(x&subVolume.sizeMask, y&subVolume.sizeMask, z&subVolume.sizeMask);
    }
}
