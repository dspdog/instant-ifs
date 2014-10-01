package ifs.flat;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

public final class ShapeAnalyzer extends Kernel{

    public ShapeAnalyzer(int w, int h){

        this.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
    }

    @Override
    public void run() {
        int x = getGlobalId(0);
        int y = getGlobalId(1);
    }

    int argb(int a, int r, int g, int b){

        a=max(1, min(a, 255))  ;
        r=max(1, min(r, 255))  ;
        g=max(1, min(g, 255))  ;
        b=max(1, min(b, 255))  ;

        int _argb = a;
        _argb = (_argb << 8) + r;
        _argb = (_argb << 8) + g;
        _argb = (_argb << 8) + b;

        return _argb;
    }

    int gray(int g){

        int _argb = 255;

        g=max(1, min(g, 255))  ;
        _argb = (_argb << 8) + g;
        _argb = (_argb << 8) + g;
        _argb = (_argb << 8) + g;

        return _argb;
    }

    int black(){
        int _argb = 255;

        _argb = (_argb << 8) + 0;
        _argb = (_argb << 8) + 0;
        _argb = (_argb << 8) + 0;

        return _argb;
    }
}
