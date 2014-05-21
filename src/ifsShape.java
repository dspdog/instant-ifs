class ifsShape{
    public ifsPt pts[];
    public double centerx, centery, offsetx, offsety;
    public int pointsInUse;
    public boolean autoUpdateCenterEnabled;
    public boolean stateSaved;
    public boolean autoScale;

    public ifsShape(int maxPoints){
        offsetx=0; offsety=0;
        autoUpdateCenterEnabled =false;
        stateSaved = false;
        pointsInUse = 0;
        autoScale = false;
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
            pts[a].radius = autoScale ? distance(pts[a].x - centerx, pts[a].y - centery) : 100;
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

    public int getNearestPtIndex(double x, double y){
        double olddist = 1000D;
        int ptSelected = -1;
        for(int a = 0; a < this.pointsInUse; a++)
        {
            double currentdist = this.distance((double) x - this.pts[a].x, (double) y - this.pts[a].y);
            if(currentdist < olddist){
                olddist = currentdist;
                ptSelected = a;
            }
        }
        return ptSelected;
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
            case 2: // '\002'
                pointsInUse = 4;
                pts[0].x = 190D;
                pts[0].y = 120D;
                pts[0].scale = 0.33333333333333331D;
                pts[1].x = 430D;
                pts[1].y = 360D;
                pts[1].scale = 0.33333333333333331D;
                pts[2].x = 430D;
                pts[2].y = 120D;
                pts[2].scale = 0.33333333333333331D;
                pts[3].x = 190D;
                pts[3].y = 360D;
                pts[3].scale = 0.33333333333333331D;
                break;

            case 3: // '\003'
                pointsInUse = 8;
                pts[0].x = 190D;
                pts[0].y = 120D;
                pts[0].scale = 0.33333333333333331D;
                pts[1].x = 430D;
                pts[1].y = 360D;
                pts[1].scale = 0.33333333333333331D;
                pts[2].x = 430D;
                pts[2].y = 120D;
                pts[2].scale = 0.33333333333333331D;
                pts[3].x = 190D;
                pts[3].y = 360D;
                pts[3].scale = 0.33333333333333331D;
                pts[4].x = 310D;
                pts[4].y = 120D;
                pts[4].scale = 0.33333333333333331D;
                pts[5].x = 310D;
                pts[5].y = 360D;
                pts[5].scale = 0.33333333333333331D;
                pts[6].x = 190D;
                pts[6].y = 240D;
                pts[6].scale = 0.33333333333333331D;
                pts[7].x = 430D;
                pts[7].y = 240D;
                pts[7].scale = 0.33333333333333331D;
                break;

            case 4: // '\004'
                pointsInUse = 2;
                pts[0].x = 430D;
                pts[0].y = 240D;
                pts[0].scale = 0.33333333333333331D;
                pts[1].x = 190D;
                pts[1].y = 240D;
                pts[1].scale = 0.33333333333333331D;
                break;

            case 5: // '\005'
                pointsInUse = 7;
                pts[0].x = Math.cos(0.0D) * 100D + (double)(1024 / 2);
                pts[0].y = Math.sin(0.0D) * 100D + (double)(1024 / 2);
                pts[0].scale = 0.33333333333333331D;
                pts[1].x = Math.cos((60D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[1].y = Math.sin((60D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[1].scale = 0.33333333333333331D;
                pts[2].x = Math.cos((120D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[2].y = Math.sin((120D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[2].scale = 0.33333333333333331D;
                pts[3].x = Math.cos((180D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[3].y = Math.sin((180D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[3].scale = 0.33333333333333331D;
                pts[4].x = Math.cos((240D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[4].y = Math.sin((240D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[4].scale = 0.33333333333333331D;
                pts[5].x = Math.cos((300D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[5].y = Math.sin((300D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[5].scale = 0.33333333333333331D;
                pts[6].x = 1024 / 2;
                pts[6].y = 1024 / 2;
                pts[6].scale = 0.33333333333333331D;
                break;
            case 6: // '\007'
                pointsInUse = 5;
                pts[0].x = 320D;
                pts[0].y = 240D;
                pts[0].scale = 0.33333333333333331D;
                pts[1].x = 190D;
                pts[1].y = 240D;
                pts[1].scale = 0.33333333333333331D;
                pts[2].x = 450D;
                pts[2].y = 240D;
                pts[2].scale = 0.33333333333333331D;
                pts[3].x = 320D;
                pts[3].y = 370D;
                pts[3].scale = 0.33333333333333331D;
                pts[4].x = 320D;
                pts[4].y = 110D;
                pts[4].scale = 0.33333333333333331D;
                break;
        }

        updateCenterOnce();
    }

    public double distance(double x2, double y2){
        return Math.sqrt(x2 * x2 + y2 * y2);
    }
}