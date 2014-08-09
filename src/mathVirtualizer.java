public class mathVirtualizer {

    static boolean virtualized = false;
    static final int num_buckets = 512;
    static final float TWOPI = (float)Math.PI*2;
    static final float ITWOPI = 1/TWOPI;

    static float[] _cos = new float[num_buckets];
    static float[] _sin = new float[num_buckets];

    public mathVirtualizer(){
        if(!virtualized)
        virtualizeMath();
        virtualized=true;
    }

    public void virtualizeMath(){
        for(int i=0; i<num_buckets; i++){
            _cos[i] = (float)Math.cos(i*TWOPI/num_buckets);
            _sin[i] = (float)Math.sin(i * TWOPI / num_buckets);
        }
        System.out.println("Math virtualized!");
    }

    public float virtualSin(float arg){
        return _sin[(int)(num_buckets * (arg+TWOPI)*ITWOPI)&num_buckets-1];
    }

    public float virtualCos(float arg){
        return _cos[(int)(num_buckets * (arg+TWOPI)*ITWOPI)&num_buckets-1];
    }
}
