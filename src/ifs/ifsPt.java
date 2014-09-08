package ifs;

import ifs.utils.MathVirtualizer;
import ifs.utils.Quaternion;

import java.util.Random;

final class ifsPt implements java.io.Serializable{

    final static MathVirtualizer mv = new MathVirtualizer();

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

    public Quaternion rotationQ;

    static ifsPt X_UNIT = new ifsPt(1.0f,0,0);
    static ifsPt Y_UNIT = new ifsPt(0,1.0f,0);
    static ifsPt Z_UNIT = new ifsPt(0,0,1.0f);

    public ifsPt(){
        rotationQ = new Quaternion(0,0,0,0);
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

    public ifsPt(float _x, float _y, float _z, float _pitch, float _yaw, float _roll){
        x = _x; y = _y; z = _z;
        rotationPitch = _pitch; rotationRoll = _roll; rotationYaw = _yaw;
    }

    public ifsPt(double _x, double _y, double _z){
        x = (float)_x; y = (float)_y; z = (float)_z;
    }

    public ifsPt intensify(float x){
        ifsPt pt= new ifsPt(this, true);
        pt.x*=x;pt.y*=x;pt.z*=x;
        pt.scale*=x;
        pt.rotationPitch*=x;
        pt.rotationRoll*=x;
        pt.rotationYaw*=x;
        return pt;
    }

    public void perturb(ifsPt mutation, long seed){
        Random rnd = new Random();
        if(seed>=0){
            rnd.setSeed(seed);
        }

        //change all the properties slightly...
        scale*=(1+rnd.nextGaussian()*mutation.scale);

        rotationPitch+=rnd.nextGaussian()*mutation.rotationPitch/180*Math.PI;
        rotationYaw+=rnd.nextGaussian()*mutation.rotationYaw/180*Math.PI;
        rotationRoll+=rnd.nextGaussian()*mutation.rotationRoll/180*Math.PI;

        x+=rnd.nextGaussian()*mutation.x;
        y+=rnd.nextGaussian()*mutation.y;
        z+=rnd.nextGaussian()*mutation.z;
    }

    public float magnitude(){
        return (float)dist(this.x, this.y, this.z);
    }

    public ifsPt add(ifsPt pt){
        return new ifsPt(this.x+pt.x, this.y+pt.y, this.z+pt.z);
    }

    public void _add(ifsPt rpt){
        this.x+=rpt.x;
        this.y+=rpt.y;
        this.z+=rpt.z;
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

    public ifsPt interpolateTo(ifsPt dest, float factor, float outerScale, float outerRotation){
        //go from this to dest as factor goes from 0 to 1
        ifsPt res = this.add(dest.subtract(this).scale(factor));

        res.radius = this.radius + (dest.radius-this.radius)*factor;
        res.scale = this.scale + (dest.scale-this.scale)*factor + outerScale;// + (float)Math.cos(factor*10)/5;

        res.rotationPitch = this.rotationPitch + (dest.rotationPitch-this.rotationPitch)*factor;
        res.rotationYaw = this.rotationPitch + (dest.rotationPitch-this.rotationPitch)*factor;
        res.rotationRoll = this.rotationPitch + (dest.rotationPitch-this.rotationPitch)*factor + outerRotation;

        return res;
    }

    public ifsPt getRotatedPt_Right(float pitch, float yaw, float roll){
        return this.qRotate(ifsPt.X_UNIT, pitch).qRotate(ifsPt.Y_UNIT, yaw).qRotate(ifsPt.Z_UNIT,roll);
    }

    public ifsPt getRotatedPt_Left(float pitch, float yaw, float roll){
        return this.qRotate(ifsPt.X_UNIT, pitch).qRotate(ifsPt.Y_UNIT, yaw).qRotate(ifsPt.Z_UNIT,roll).qRotate(ifsPt.Y_UNIT, 0.1f);
    }

    public ifsPt getRotatedPt(ifsPt rpt){
        return this.qRotate(ifsPt.X_UNIT, rpt.x).qRotate(ifsPt.Y_UNIT, rpt.y).qRotate(ifsPt.Z_UNIT,rpt.z);
    }

    public ifsPt getRotation(){
        return new ifsPt(this.rotationPitch, this.rotationYaw, this.rotationRoll);
    }

    public ifsPt qRotate(ifsPt r, float a){
        a/=2;
        float sa2 = mv.virtualSin(a);
        Quaternion q2 = new Quaternion(mv.virtualCos(a), r.x*sa2, r.y*sa2, r.z*sa2);
        Quaternion q3 = q2.times(new Quaternion(0, this.x, this.y, this.z)).times(q2.conjugate());
        return new ifsPt(q3.x, q3.y, q3.z);
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