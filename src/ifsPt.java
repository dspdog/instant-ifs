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

    public ifsPt(double _x, double _y, double _z){
        x = _x; y = _y; z = _z;
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