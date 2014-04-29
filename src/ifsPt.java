class ifsPt {
    public double x;
    public double y;
    public double scale;
    public double degrees;
    public double radius;
    public double rotation;
    public double opacity;

    public double savedopacity;
    public double savedx;
    public double savedy;
    public double savedscale;
    public double saveddegrees;
    public double savedradius;
    public double savedrotation;

    public ifsPt(){
        x = 0D;
        y = 0D;
        scale = 0.5D;
        rotation = 0.0D;
        degrees = 0D;
        radius = 1D;
        opacity = 1D;
    }

    public void saveState(){
        savedx = x;
        savedy = y;
        savedscale = scale;
        saveddegrees = degrees;
        savedradius = radius;
        savedrotation = rotation;
        savedopacity = opacity;
    }
}