class ifsPt implements java.io.Serializable{
    public float x, y, z;
    public float scale;
    public float degreesYaw;
    public float degreesPitch;
    public float radius;
    public float rotationYaw;
    public float rotationPitch;
    public float opacity;

    public float savedopacity;
    public float savedx, savedy, savedz;
    public float savedscale;
    public float saveddegrees;
    public float savedradius;
    public float savedrotation;

    static ifsPt X_UNIT = new ifsPt(1.0f,0,0);
    static ifsPt Y_UNIT = new ifsPt(0,1.0f,0);
    static ifsPt Z_UNIT = new ifsPt(0,0,1.0f);

    public ifsPt(){
        x = 0f; y = 0f; z = 0f;
        scale = 1f;
        rotationYaw = 0.0f;
        rotationPitch = 0.0f;
        degreesYaw = 0f;
        degreesPitch = 0f;
        radius = 1f;
        opacity = 1f;
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

    public ifsPt XYOnly(){
        return new ifsPt(this.x,this.y,0);
    }

    public ifsPt getRotatedPt(double y, double z){
        return getRotatedPt((float)y, (float)z);
    }

    public ifsPt getRotatedPt(float y, float z){

        float sx, sy, sz, cx, cy, cz;

        float Lx, Ly, Lz;
        float Ux, Uy, Uz;
        float Fx, Fy, Fz;

        sx = sin(0);//0-->x
        cx = cos(0);

        sy = sin(y);
        cy = cos(y);

        sz = sin(z);
        cz = cos(z);

        // determine left axis
        Lx = cy*cz;
        Ly = sx*sy*cz + cx*sz;
        Lz = -cx*sy*cz + sx*sz;

        // determine up axis
        Ux = -cy*sz;
        Uy = -sx*sy*sz + cx*cz;
        Uz = cx*sy*sz + sx*cz;

        // determine forward axis
        Fx = sy;
        Fy = -sx*cy;
        Fz = cx*cy;

        float xp = (float)(this.x*Lx + this.y*Ly + this.z*Lz);
        float yp = this.x*Ux + this.y*Uy + this.z*Uz;
        float zp = this.x*Fx + this.y*Fy + this.z*Fz;

        return new ifsPt(xp,yp,zp);
    }

    public ifsPt getRotatedPt(float y, float z, float x){

        float sx, sy, sz, cx, cy, cz;

        float Lx, Ly, Lz;
        float Ux, Uy, Uz;
        float Fx, Fy, Fz;

        sx = sin(x);//0-->x
        cx = cos(x);

        sy = sin(y);
        cy = cos(y);

        sz = sin(z);
        cz = cos(z);

        // determine left axis
        Lx = cy*cz;
        Ly = sx*sy*cz + cx*sz;
        Lz = -cx*sy*cz + sx*sz;

        // determine up axis
        Ux = -cy*sz;
        Uy = -sx*sy*sz + cx*cz;
        Uz = cx*sy*sz + sx*cz;

        // determine forward axis
        Fx = sy;
        Fy = -sx*cy;
        Fz = cx*cy;

        float xp = this.x*Lx + this.y*Ly + this.z*Lz;
        float yp = this.x*Ux + this.y*Uy + this.z*Uz;
        float zp = this.x*Fx + this.y*Fy + this.z*Fz;

        return new ifsPt(xp,yp,zp);
    }

    public void saveState(){
        savedx = x; savedy = y; savedz = z;
        savedscale = scale;
        saveddegrees = degreesYaw;
        savedradius = radius;
        savedrotation = rotationYaw;
        savedopacity = opacity;
    }
}