package ifs;

import java.util.Random;

class ifsPt implements java.io.Serializable{

    static mathVirtualizer mv = new mathVirtualizer();

    public float x, y, z;
    public float scale;
    public float degreesYaw;
    public float degreesPitch;
    public float degreesRoll;
    public float radius;
    public float rotationYaw;
    public float rotationPitch;
    public float rotationRoll;
    public float opacity;

    public float savedopacity;
    public float savedx, savedy, savedz;
    public float savedscale;
    public float saveddegrees;
    public float savedradius;
    public float savedrotationyaw;
    public float savedrotationpitch;
    public float savedrotationroll;

    static ifsPt X_UNIT = new ifsPt(1.0f,0,0);
    static ifsPt Y_UNIT = new ifsPt(0,1.0f,0);
    static ifsPt Z_UNIT = new ifsPt(0,0,1.0f);

    public ifsPt(){
        x = 0f; y = 0f; z = 0f;
        scale = 1f;
        rotationYaw = 0.0f;
        rotationPitch = 0.0f;
        rotationRoll = 0.0f;
        degreesYaw = 0f;
        degreesPitch = 0f;
        degreesRoll = 0f;
        radius = 1f;
        opacity = 1f;
    }

    public String coordString(){
        return this.x + " " + this.y + " " + this.z;
    }

    public ifsPt(ifsPt pt){
        x=pt.x;
        y=pt.y;
        z=pt.z;
    }

    public ifsPt(ifsPt pt, boolean full){
        x=pt.x;
        y=pt.y;
        z=pt.z;

        if(full){
            scale = pt.scale;
            rotationYaw = pt.rotationYaw;
            rotationPitch = pt.rotationPitch;
            degreesYaw = pt.degreesYaw;
            degreesPitch = pt.degreesPitch;
            degreesRoll = pt.degreesRoll;
            radius = pt.radius;
            opacity = pt.opacity;
        }
    }

    public ifsPt(float _x, float _y, float _z){
        x = _x; y = _y; z = _z;
    }

    public ifsPt(double _x, double _y, double _z){
        x = (float)_x; y = (float)_y; z = (float)_z;
    }

    public void perturb(float intensity, long seed){
        Random rnd = new Random();
        if(seed>=0){
            rnd.setSeed(seed);
        }

        //change all the properties slightly...
        scale*=(1+rnd.nextGaussian()*intensity);
        rotationPitch+=rnd.nextGaussian()*intensity;
        rotationYaw+=rnd.nextGaussian()*intensity;
        rotationRoll+=rnd.nextGaussian()*intensity;
        x+=rnd.nextGaussian()*intensity*10;
        y+=rnd.nextGaussian()*intensity*10;
        z+=rnd.nextGaussian()*intensity*10;
    }

    public float magnitude(){
        return (float)dist(this.x, this.y, this.z);
    }

    public float cos(float i){
        return (float)Math.cos(i);
    }

    public float sin(float i){
        return (float)Math.sin(i);
    }

    public ifsPt add(ifsPt pt){
        return new ifsPt(this.x+pt.x, this.y+pt.y, this.z+pt.z);
    }

    public ifsPt subtract(ifsPt pt){
        return new ifsPt(this.x-pt.x, this.y-pt.y, this.z-pt.z);
    }

    public ifsPt scale(float s){
        return new ifsPt(this.x*s, this.y*s, this.z*s);
    }

    public double distanceXY(ifsPt pt){
        return dist(pt.x - this.x, pt.y - this.y, 0);
    }

    public static double dist(float x, float y, float z){
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double distTo(ifsPt p){
        return Math.sqrt((p.x-this.x) * (p.x-this.x) + (p.y-this.y) * (p.y-this.y) + (p.z-this.z) * (p.z-this.z));
    }

    public ifsPt interpolateTo(ifsPt dest, float factor){
        //go from this to dest as factor goes from 0 to 1
        ifsPt res = this.add(dest.subtract(this).scale(factor));

        res.radius = this.radius + (dest.radius-this.radius)*factor;
        res.scale = this.scale + (dest.scale-this.scale)*factor;

        return res;
    }

    public ifsPt getRotatedPt(double y, double z){
        return getRotatedPt((float)y, (float)z);
    }

    public ifsPt getRotatedPt(float y, float z){
        return getRotatedPt(y, z, 0);
    }

    public ifsPt getRotatedPt(float y, float z, float x){

        float sx = mv.virtualSin(x),
              sy = mv.virtualSin(y),
              sz = mv.virtualSin(z),
              cx = mv.virtualCos(x),
              cy = mv.virtualCos(y),
              cz = mv.virtualCos(z);

        float sxsy = sx*sy;
        float cxsy = cx*sy;

        return new ifsPt(
                this.x * cy*cz +
                this.y * (sxsy*cz + cx*sz) +
                this.z * (-cxsy*cz + sx*sz),

                this.x*-cy*sz +
                this.y*(-sxsy*sz + cx*cz) +
                this.z*(cxsy*sz + sx*cz),

                this.x*sy +
                this.y*(-sx*cy) +
                this.z*(cx*cy));
    }

    public void saveState(){
        savedx = x; savedy = y; savedz = z;
        savedscale = scale;
        saveddegrees = degreesYaw;
        savedradius = radius;
        savedrotationyaw = rotationYaw;
        savedrotationpitch = rotationPitch;
        savedrotationroll = rotationRoll;
        savedopacity = opacity;
    }
}