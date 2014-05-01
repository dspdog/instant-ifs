class ifsShape{
    public ifsPt pts[];
    public double centerx, centery, offsetx, offsety;
    public int pointsInUse;
    public boolean autoUpdateCenterEnabled;
    public boolean stateSaved;

    public ifsShape(int maxPoints){
        offsetx=0; offsety=0;
        autoUpdateCenterEnabled =false;
        stateSaved = false;
        pointsInUse = 0;
        pts = new ifsPt[maxPoints];
        for(int a=0; a< maxPoints; a++){
            pts[a] = new ifsPt();
        }
    }

    public void saveState(){
        for(int a = 0; a < pointsInUse; a++){
            pts[a].saveState();
            stateSaved=true;
        }
    }

    public void addPoint(double x, double y){
        pts[pointsInUse].x = x;
        pts[pointsInUse].y = y;
        pts[pointsInUse].scale = 0.5D;
        pts[pointsInUse].rotation = 0.0D;
        pts[pointsInUse].opacity = 1.0D;
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

    void updateRadiusDegrees(){
        for(int a = 0; a < pointsInUse; a++){
            pts[a].degrees = Math.atan2(pts[a].x - centerx, pts[a].y - centery);
            pts[a].radius = distance(pts[a].x - centerx, pts[a].y - centery);
        }
    }

    void updateCenter(){
        double x = 0, y = 0;

        if(autoUpdateCenterEnabled){
            if(pointsInUse != 0){
                for(int a = 0; a < pointsInUse; a++){
                    x += pts[a].x;
                    y += pts[a].y;
                }

                centerx = x / pointsInUse;
                centery = y / pointsInUse;
            } else{
                centerx = pts[0].x;
                centery = pts[0].y;
            }
        }

        updateRadiusDegrees();
    }

    void updateCenterOnce(){
        boolean oldState = autoUpdateCenterEnabled;

        autoUpdateCenterEnabled =true;
        updateCenter();
        autoUpdateCenterEnabled =oldState;
    }

    public void setToPreset(int preset){
        switch(preset){
            case 1: // '\001'
                pointsInUse = 3;
                pts[0].x = 320D;
                pts[0].y = 160D;
                pts[0].scale = 0.5D;
                pts[1].x = 420D;
                pts[1].y = 160D + 100D * Math.sqrt(3D);
                pts[1].scale = 0.5D;
                pts[2].x = 220D;
                pts[2].y = 160D + 100D * Math.sqrt(3D);
                pts[2].scale = 0.5D;
                break;
        }
        updateCenterOnce();
    }

    public double distance(double x2, double y2){
        return Math.sqrt(x2 * x2 + y2 * y2);
    }
}