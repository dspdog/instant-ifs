/**
 * Created by user on 7/3/14.
 */
public class ColorSample {
    int a,r,g,b, rgb;
    public void ColorSample(int _a, int _r, int _g, int _b){
        this.a = _a;
        this.r = _r;
        this.g = _g;
        this.b = _b;
        this.rgb = getRGB();
    }

    public int getRGB(){
        return ((a&0x0ff)<<24)|((r&0x0ff)<<16)|((g&0x0ff)<<8)|(b&0x0ff);
    }
}
