class ifsPt {
    public double x, y, z;
    public double scale;
    public double degreesYaw;
    public double degreesPitch;
    public double radius;
    public double rotationYaw;
    public double rotationPitch;
    public double opacity;

    public double savedopacity;
    public double savedx, savedy, savedz;
    public double savedscale;
    public double saveddegrees;
    public double savedradius;
    public double savedrotation;

    public ifsPt(){
        x = 0D; y = 0D; z = 0D;
        scale = 1D;
        rotationYaw = 0.0D;
        rotationPitch = 0.0D;
        degreesYaw = 0D;
        degreesPitch = 0D;
        radius = 1D;
        opacity = 1D;
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

    public ifsPt(double _x, double _y, double _z){
        x = _x; y = _y; z = _z;
    }

    public double cos(double i){
        return Math.cos(i);
    }

    public double sin(double i){
        return Math.sin(i);
    }

    public ifsPt add(ifsPt pt){
        return new ifsPt(this.x+pt.x, this.y+pt.y, this.z+pt.z);
    }

    public ifsPt getRotatedPt(double y, double z){

        double sx, sy, sz, cx, cy, cz;

        double Lx, Ly, Lz;
        double Ux, Uy, Uz;
        double Fx, Fy, Fz;

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

        double xp = this.x*Lx + this.y*Ly + this.z*Lz;
        double yp = this.x*Ux + this.y*Uy + this.z*Uz;
        double zp = this.x*Fx + this.y*Fy + this.z*Fz;

        return new ifsPt(xp,yp,zp);
    }

    public ifsPt getCameraRotatedPt(double pitch, double yaw, double roll){
        ifsPt __pt = new ifsPt(this.x-512.0,this.y-512.0,this.z-512.0);
        ifsPt ___pt = __pt.getRotatedPt(pitch, yaw, roll);
        return new ifsPt(___pt.x+512.0,___pt.y+512.0,___pt.z+512.0);
    }

    public ifsPt getRotatedPt(double y, double z, double x){

        double sx, sy, sz, cx, cy, cz;

        double Lx, Ly, Lz;
        double Ux, Uy, Uz;
        double Fx, Fy, Fz;

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

        double xp = this.x*Lx + this.y*Ly + this.z*Lz;
        double yp = this.x*Ux + this.y*Uy + this.z*Uz;
        double zp = this.x*Fx + this.y*Fy + this.z*Fz;

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