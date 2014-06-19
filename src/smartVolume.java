public class smartVolume { //partitions the space into subVolumes but ignores empty space
    int size;
    int subRes;
    int totalRegions;

    private subVolume[][][] data;

    boolean inited = false;

    public smartVolume(int _size){
        if(_size%subVolume.size ==0){
            size=_size;
            subRes = size/subVolume.size;
            reset();
        }else{
            System.out.println("Volume size must be multiple of subvolume size!");
        }
    }

    public void reset(){
        totalRegions=0;
        if(!inited){
            data = new subVolume[subRes][subRes][subRes];
            inited=true;
        }
        for(int x=0; x<subRes; x++){
            for(int y=0; y<subRes; y++){
                for(int z=0; z<subRes; z++){
                    data[x][y][z] = new subVolume();
                    totalRegions++;
                }
            }
        }
    }

    public int getInitCount(){
        int count = 0;
        for(int x=0; x<subRes; x++){
            for(int y=0; y<subRes; y++){
                for(int z=0; z<subRes; z++){
                    if(data[x][y][z].inited)count++;
                }
            }
        }
        return count;
    }

    public boolean isNotEmpty(int x, int y, int z){
        return data[x][y][z].inited;
    }

    public void putData(int x, int y, int z, double val){
        if(val>0){
            data[x>>subVolume.sizeLog2][y>>subVolume.sizeLog2][z>>subVolume.sizeLog2].putData(x & subVolume.sizeMask, y & subVolume.sizeMask, z & subVolume.sizeMask, val);
        }
    }

    public void incrementData(int x, int y, int z, double increment){
        data[x>>subVolume.sizeLog2][y>>subVolume.sizeLog2][z>>subVolume.sizeLog2].incrementData(x & subVolume.sizeMask, y & subVolume.sizeMask, z & subVolume.sizeMask, increment);
    }

    public void clearData(int x, int y, int z){
        data[x>>subVolume.sizeLog2][y>>subVolume.sizeLog2][z>>subVolume.sizeLog2].clearData(x & subVolume.sizeMask, y & subVolume.sizeMask, z & subVolume.sizeMask);
    }

    public void clipData(int x, int y, int z){
        data[x>>subVolume.sizeLog2][y>>subVolume.sizeLog2][z>>subVolume.sizeLog2].clipData(x & subVolume.sizeMask, y & subVolume.sizeMask, z & subVolume.sizeMask);
    }
    
    public double getData(int x, int y, int z){
        return data[x>>subVolume.sizeLog2][y>>subVolume.sizeLog2][z>>subVolume.sizeLog2].getData(x&subVolume.sizeMask, y&subVolume.sizeMask, z&subVolume.sizeMask);
    }

    int validX[];
    int validY[];
    int validZ[];
    boolean regionsUpToDate=false;
    int regionsCount =0;

    public void findRegionsList(){

        validX = new int[subRes*subRes*subRes];
        validY = new int[subRes*subRes*subRes];
        validZ = new int[subRes*subRes*subRes];

        System.out.println("finding regions...");
        regionsCount=0;

        for(int x=0; x<subRes; x++){
            for(int y=0; y<subRes; y++){
                for(int z=0; z<subRes; z++){
                    if(data[x][y][z].inited){
                        validX[regionsCount]=x;
                        validY[regionsCount]=y;
                        validZ[regionsCount]=z;
                        regionsCount++;
                    }
                }
            }
        }
        regionsUpToDate=true;
    }

    public ifsPt rndPt(){
        if(!regionsUpToDate){
            findRegionsList();
            System.out.println(regionsCount + " / " + subRes*subRes*subRes + " REGIONS");
        }
        return new ifsPt(validX[(int)(Math.random()*regionsCount)]*subVolume.size+Math.random()*subVolume.size,
                         validY[(int)(Math.random()*regionsCount)]*subVolume.size+Math.random()*subVolume.size,
                         validZ[(int)(Math.random()*regionsCount)]*subVolume.size+Math.random()*subVolume.size);
    }
}
