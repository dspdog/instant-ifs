class ifsShape{
    public ifsPt pts[];
    public double centerx, centery;
    public int pointsInUse;

    public ifsShape(int maxPoints){
        pointsInUse = 0;
        pts = new ifsPt[maxPoints];
        for(int a=0; a< maxPoints; a++){
            pts[a] = new ifsPt();
        }
    }

    public void addPoint(double x, double y){
        pts[pointsInUse].x = x;
        pts[pointsInUse].y = y;
        pts[pointsInUse].scale = 0.5D;
        pts[pointsInUse].rotation = 0.0D;
        pointsInUse++;
        updateCenter();
    }

    public void deletePoint(int selectedPoint){
        for(int a = selectedPoint; a < pointsInUse; a++){
            pts[a].x = pts[a + 1].x;
            pts[a].y = pts[a + 1].y;

            pts[a].scale = pts[a + 1].scale;
            pts[a].rotation = pts[a + 1].rotation;
        }

        pts[pointsInUse].x = 0.0D;
        pts[pointsInUse].y = 0.0D;

        pts[pointsInUse].scale = 0.5D;
        pts[pointsInUse].rotation = 0.0D;
        pointsInUse--;

        updateCenter();
    }

    void updateCenter(){
        double x = 0, y = 0;

        if(pointsInUse != 0){
            for(int a = 0; a < pointsInUse; a++){
                x += pts[a].x;
                y +=  pts[a].y;
            }

            centerx = x / pointsInUse;
            centery = y / pointsInUse;
        } else{
            centerx = pts[0].x;
            centery = pts[0].y;
        }
    }
}