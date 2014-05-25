class ifsPt {
    public double x, y, z;
    public double scale;
    public double degreesYaw;
    public double radius;
    public double rotationYaw;
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
        degreesYaw = 0D;
        radius = 1D;
        opacity = 1D;
    }

    public ifsPt(ifsPt pt){
        x=pt.x;
        y=pt.y;
        z=pt.z;
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

    public ifsPt getRotatedPt(double x, double y, double z){

        double sx, sy, sz, cx, cy, cz, theta;

        double Lx, Ly, Lz;
        double Ux, Uy, Uz;
        double Fx, Fy, Fz;

        theta = x;
        sx = sin(theta);
        cx = cos(theta);

        theta = y;
        sy = sin(theta);
        cy = cos(theta);

        theta = z;
        sz = sin(theta);
        cz = cos(theta);

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