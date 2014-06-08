class ifsShape{
    public ifsPt pts[];

    public double unitScale;

    public int pointsInUse;
    public boolean autoUpdateCenterEnabled;
    public boolean stateSaved;
    public boolean autoScale;

    public ifsShape(int maxPoints){
        autoUpdateCenterEnabled =false;
        stateSaved = false;
        pointsInUse = 1;
        unitScale = 115.47005383792515; //distance from center to one of the points in preset #1
        autoScale = true;
        pts = new ifsPt[maxPoints];
        for(int a=0; a< maxPoints; a++){
            pts[a] = new ifsPt();
        }
    }

    /*public void centerByPt(int desiredX, int desiredY, int desiredZ, int centerX, int centerY, int centerZ){
        int offsetX = desiredX-centerX;
        int offsetY = desiredY-centerY;
        int offsetZ = desiredZ-centerZ;

        for(int i=0; i<pointsInUse; i++){
            pts[i].x-=offsetX;
            pts[i].y-=offsetY;
            pts[i].z-=offsetZ;
        }
        updateCenter();
    }*/

    public void saveState(){
        for(int a = 0; a < pointsInUse; a++){
            pts[a].saveState();
            stateSaved=true;
        }
    }

    public void addPoint(double x, double y, double z){
        pts[pointsInUse].x = x;
        pts[pointsInUse].y = y;
        pts[pointsInUse].z = z;
        pts[pointsInUse].scale = 0.5D;
        pts[pointsInUse].rotationYaw = 0.0D;
        pts[pointsInUse].rotationPitch = 0.0D;
        //pts[pointsInUse].rotationRoll = 0.0D;
        pts[pointsInUse].opacity = 1.0D;
        pointsInUse++;
        updateCenter();
    }

    public void deletePoint(int selectedPoint){
        for(int a = selectedPoint; a < pointsInUse; a++){
            pts[a].x = pts[a + 1].x;
            pts[a].y = pts[a + 1].y;

            pts[a].scale = pts[a + 1].scale;
            pts[a].rotationYaw = pts[a + 1].rotationYaw;
        }

        pts[pointsInUse].x = 0.0D;
        pts[pointsInUse].y = 0.0D;

        pts[pointsInUse].scale = 0.5D;
        pts[pointsInUse].rotationYaw = 0.0D;
        pts[pointsInUse].rotationPitch = 0.0D;
        pointsInUse--;

        updateCenter();
    }

    public void clearPts(){

        for(int a = 0; a < pointsInUse; a++){
            deletePoint(pointsInUse-a);
        }
        pointsInUse=0;
    }

    void updateRadiusDegrees(){
        //pts[0].degreesYaw = 0;
        pts[0].degreesPitch = Math.PI/2;
        pts[0].radius = unitScale*pts[0].scale;

        for(int a = 1; a < pointsInUse; a++){
            pts[a].radius = autoScale ? distance(pts[a].x - pts[0].x, pts[a].y - pts[0].y,  pts[a].z - pts[0].z) : pts[0].radius;
            pts[a].degreesYaw = Math.atan2(pts[a].x - pts[0].x, pts[a].y - pts[0].y);
            pts[a].degreesPitch = Math.atan2(pts[a].radius, pts[a].z - pts[0].z);
        }
    }

    void updateCenter(){
        double x = 0, y = 0;

        if(autoUpdateCenterEnabled){
            if(pointsInUse != 0){
                for(int a = 1; a < pointsInUse; a++){
                    x += pts[a].x;
                    y += pts[a].y;
                }
                pts[0].x  = x / (pointsInUse-1);
                pts[0].y  = y / (pointsInUse-1);
            }
        }

        updateRadiusDegrees();
    }

    public int getNearestPtIndexXY(double x, double y){
        double olddist = 100000D;
        int ptSelected = -1;
        for(int a = 0; a < this.pointsInUse; a++)
        {
            double currentdist = this.distance((double) x - this.pts[a].x, (double) y - this.pts[a].y, 0);
            if(currentdist < olddist){
                olddist = currentdist;
                ptSelected = a;
            }
        }
        return ptSelected;
    }

    public int getNearestPtIndexXZ(double x, double y){
        double olddist = 100000D;
        int ptSelected = -1;
        for(int a = 0; a < this.pointsInUse; a++)
        {
            double currentdist = this.distance((double) x - this.pts[a].x, (double) y - this.pts[a].z, 0);
            if(currentdist < olddist){
                olddist = currentdist;
                ptSelected = a;
            }
        }
        return ptSelected;
    }

    public int getNearestPtIndexYZ(double x, double y){
        double olddist = 100000D;
        int ptSelected = -1;
        for(int a = 0; a < this.pointsInUse; a++)
        {
            double currentdist = this.distance((double) x - this.pts[a].y, (double) y - this.pts[a].z, 0);
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
            case 0: // '\000'
                clearPts();
                pointsInUse=1;
                int centerx=512;
                int centery=512;
                int centerz=512;

                for(int i=0; i<4; i++){
                    this.addPoint(
                            Math.cos(Math.PI/4+i*Math.PI/2)*200+centerx,
                            Math.sin(Math.PI/4+i*Math.PI/2)*200+centery,
                            centerz);
                }
                this.pts[0].z=centerz;
                System.out.println(pointsInUse);
                break;

            case 1: // '\001'

                pointsInUse = 4;
                pts[0].scale = 1.0;
                pts[1].x = 320D;
                pts[1].y = 160D;
                pts[1].z = 0D;
                pts[1].scale = 0.5D;
                pts[2].x = 420D;
                pts[2].y = 160D + 100D * Math.sqrt(3D);
                pts[2].z = 0;
                pts[2].scale = 0.5D;
                pts[3].x = 220D;
                pts[3].y = 160D + 100D * Math.sqrt(3D);
                pts[3].z = 0;
                pts[3].scale = 0.5D;

                break;
            case 2: // '\002'
                pointsInUse = 5;
                pts[1].x = 190D;
                pts[1].y = 120D;
                pts[1].scale = 0.33333333333333331D;
                pts[2].x = 430D;
                pts[2].y = 360D;
                pts[2].scale = 0.33333333333333331D;
                pts[3].x = 430D;
                pts[3].y = 120D;
                pts[3].scale = 0.33333333333333331D;
                pts[4].x = 190D;
                pts[4].y = 360D;
                pts[4].scale = 0.33333333333333331D;
                break;

            case 3: // '\003'
                pointsInUse = 9;
                pts[1].x = 190D;
                pts[1].y = 120D;
                pts[1].scale = 0.33333333333333331D;
                pts[2].x = 430D;
                pts[2].y = 360D;
                pts[2].scale = 0.33333333333333331D;
                pts[3].x = 430D;
                pts[3].y = 120D;
                pts[3].scale = 0.33333333333333331D;
                pts[4].x = 190D;
                pts[4].y = 360D;
                pts[4].scale = 0.33333333333333331D;
                pts[5].x = 310D;
                pts[5].y = 120D;
                pts[5].scale = 0.33333333333333331D;
                pts[6].x = 310D;
                pts[6].y = 360D;
                pts[6].scale = 0.33333333333333331D;
                pts[7].x = 190D;
                pts[7].y = 240D;
                pts[7].scale = 0.33333333333333331D;
                pts[8].x = 430D;
                pts[8].y = 240D;
                pts[8].scale = 0.33333333333333331D;
                break;

            case 4: // '\004'
                pointsInUse = 3;
                pts[1].x = 430D;
                pts[1].y = 240D;
                pts[1].scale = 0.33333333333333331D;
                pts[2].x = 190D;
                pts[2].y = 240D;
                pts[2].scale = 0.33333333333333331D;
                break;

            case 5: // '\005'
                pointsInUse = 8;
                pts[1].x = Math.cos(0.0D) * 100D + (double)(1024 / 2);
                pts[1].y = Math.sin(0.0D) * 100D + (double)(1024 / 2);
                pts[1].scale = 0.33333333333333331D;
                pts[2].x = Math.cos((60D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[2].y = Math.sin((60D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[2].scale = 0.33333333333333331D;
                pts[3].x = Math.cos((120D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[3].y = Math.sin((120D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[3].scale = 0.33333333333333331D;
                pts[4].x = Math.cos((180D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[4].y = Math.sin((180D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[4].scale = 0.33333333333333331D;
                pts[5].x = Math.cos((240D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[5].y = Math.sin((240D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[5].scale = 0.33333333333333331D;
                pts[6].x = Math.cos((300D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[6].y = Math.sin((300D * Math.PI) / 180D) * 100D + (double)(1024 / 2);
                pts[6].scale = 0.33333333333333331D;
                pts[7].x = 1024 / 2;
                pts[7].y = 1024 / 2;
                pts[7].scale = 0.33333333333333331D;
                break;
            case 6: // '\007'
                pointsInUse = 6;
                pts[1].x = 320D;
                pts[1].y = 240D;
                pts[1].scale = 0.33333333333333331D;
                pts[2].x = 190D;
                pts[2].y = 240D;
                pts[2].scale = 0.33333333333333331D;
                pts[3].x = 450D;
                pts[3].y = 240D;
                pts[3].scale = 0.33333333333333331D;
                pts[4].x = 320D;
                pts[4].y = 370D;
                pts[4].scale = 0.33333333333333331D;
                pts[5].x = 320D;
                pts[5].y = 110D;
                pts[5].scale = 0.33333333333333331D;
                break;
        }

        updateCenterOnce();
    }

    public double distance(double x, double y, double z){
        return Math.sqrt(x * x + y * y + z * z);
    }
}