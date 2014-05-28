import java.awt.*;

public class ifsOverlays {
    ifsys myIfsSys;
    Graphics theGraphics;

    public ifsOverlays(ifsys _ifsys, Graphics rg){
        myIfsSys = _ifsys;
        theGraphics = rg;
    }

    public void drawArcs(Graphics rg){
        for(int i=0;i<myIfsSys.shape.pointsInUse;i++){
            if(i==0){
                rg.setColor(Color.BLUE);
            }
            drawArc(rg, myIfsSys.shape.pts[i], i == myIfsSys.pointselected, myIfsSys.isDragging);
        }
    }

    public void drawCenterOfGravity(Graphics _rg){
        _rg.setColor(Color.CYAN);
        int width = 15;
        int height = 15;
        int x=0,y=0;

        switch (myIfsSys.viewMode){
            case 0:
                x=(int)(myIfsSys.centerOfGrav.x/myIfsSys.samplesThisFrame);
                y=(int)(myIfsSys.centerOfGrav.y/myIfsSys.samplesThisFrame);
                break;
            case 1:
                x=(int)(myIfsSys.centerOfGrav.x/myIfsSys.samplesThisFrame);
                y=(int)(myIfsSys.centerOfGrav.z/myIfsSys.samplesThisFrame);
                break;
            case 2:
                x=(int)(myIfsSys.centerOfGrav.y/myIfsSys.samplesThisFrame);
                y=(int)(myIfsSys.centerOfGrav.z/myIfsSys.samplesThisFrame);
                break;
        }

        _rg.drawRect(x-width/2,y-height/2,width,height);
    }

    public void drawArc(Graphics _rg, ifsPt pt, boolean isSelected, boolean dragging){
        int viewMode = myIfsSys.viewMode;
        int steps = 50;

        int[] xPts1 = new int[steps];
        int[] yPts1 = new int[steps];
        int[] zPts1 = new int[steps];

        int[] xPts2 = new int[steps];
        int[] yPts2 = new int[steps];
        int[] zPts2 = new int[steps];

        int[] xPts3 = new int[steps];
        int[] yPts3 = new int[steps];
        int[] zPts3 = new int[steps];

        double d1=999,d2=999,d3=999, min_d; //distances from arcs to mouse cursor
        int selectedD=1;

        min_d=100000;

        switch (viewMode){
            case 0:
                xPts1[0] = (int)pt.x;
                yPts1[0] = (int)pt.y;
                zPts1[0] = (int)pt.z;

                xPts2[0] = (int)pt.x;
                yPts2[0] = (int)pt.y;
                zPts2[0] = (int)pt.z;

                xPts3[0] = (int)pt.x;
                yPts3[0] = (int)pt.y;
                zPts3[0] = (int)pt.z;
                break;
            case 1:
                xPts1[0] = (int)pt.x;
                yPts1[0] = (int)pt.z;
                zPts1[0] = (int)pt.y;

                xPts2[0] = (int)pt.x;
                yPts2[0] = (int)pt.z;
                zPts2[0] = (int)pt.y;

                xPts3[0] = (int)pt.x;
                yPts3[0] = (int)pt.z;
                zPts3[0] = (int)pt.y;
                break;
            case 2:
                xPts1[0] = (int)pt.y;
                yPts1[0] = (int)pt.z;
                zPts1[0] = (int)pt.x;

                xPts2[0] = (int)pt.y;
                yPts2[0] = (int)pt.z;
                zPts2[0] = (int)pt.x;

                xPts3[0] = (int)pt.y;
                yPts3[0] = (int)pt.z;
                zPts3[0] = (int)pt.x;
                break;
        }

        for(int i=1; i<steps; i++){
            //yaw
            xPts1[i] = (int)((Math.cos(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));
            yPts1[i] = (int)((Math.sin(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));
            zPts1[i] = 10*i/steps;

            //pitch
            xPts2[i] = (int)((Math.cos(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));
            yPts2[i] = 10*i/steps;
            zPts2[i] = (int)((Math.sin(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));

            //roll
            xPts3[i] = 10*i/steps;
            yPts3[i] = (int)((Math.cos(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));
            zPts3[i] = (int)((Math.sin(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));

            ifsPt rotatedPt1 = new ifsPt(xPts1[i],yPts1[i],zPts1[i]).getRotatedPt(-pt.rotationRoll,-pt.rotationPitch,-pt.rotationYaw);
            ifsPt rotatedPt2 = new ifsPt(xPts2[i],yPts2[i],zPts2[i]).getRotatedPt(-pt.rotationRoll,-pt.rotationPitch,-pt.rotationYaw);
            ifsPt rotatedPt3 = new ifsPt(xPts3[i],yPts3[i],zPts3[i]).getRotatedPt(-pt.rotationRoll,-pt.rotationPitch,-pt.rotationYaw);

            switch (viewMode){
                case 0:
                    xPts1[i] = (int)(rotatedPt1.x + pt.x);
                    yPts1[i] = (int)(rotatedPt1.y + pt.y);
                    zPts1[i] = (int)(rotatedPt1.z + pt.z);

                    xPts2[i] = (int)(rotatedPt2.x + pt.x);
                    yPts2[i] = (int)(rotatedPt2.y + pt.y);
                    zPts2[i] = (int)(rotatedPt2.z + pt.z);

                    xPts3[i] = (int)(rotatedPt3.x + pt.x);
                    yPts3[i] = (int)(rotatedPt3.y + pt.y);
                    zPts3[i] = (int)(rotatedPt3.z + pt.z);

                    d1 = distance(myIfsSys.mousex - rotatedPt1.x - pt.x, myIfsSys.mousey - rotatedPt1.y - pt.y);
                    d2 = distance(myIfsSys.mousex - rotatedPt2.x - pt.x, myIfsSys.mousey - rotatedPt2.y - pt.y);
                    d3 = distance(myIfsSys.mousex - rotatedPt3.x - pt.x, myIfsSys.mousey - rotatedPt3.y - pt.y);

                    break;
                case 1:
                    xPts1[i] = (int)(rotatedPt1.x + pt.x);
                    zPts1[i] = (int)(rotatedPt1.y + pt.y);
                    yPts1[i] = (int)(rotatedPt1.z + pt.z);

                    xPts2[i] = (int)(rotatedPt2.x + pt.x);
                    zPts2[i] = (int)(rotatedPt2.y + pt.y);
                    yPts2[i] = (int)(rotatedPt2.z + pt.z);

                    xPts3[i] = (int)(rotatedPt3.x + pt.x);
                    zPts3[i] = (int)(rotatedPt3.y + pt.y);
                    yPts3[i] = (int)(rotatedPt3.z + pt.z);

                    d1 = distance(myIfsSys.mousex - rotatedPt1.x - pt.x, myIfsSys.mousey - rotatedPt1.z - pt.z);
                    d2 = distance(myIfsSys.mousex - rotatedPt2.x - pt.x, myIfsSys.mousey - rotatedPt2.z - pt.z);
                    d3 = distance(myIfsSys.mousex - rotatedPt3.x - pt.x, myIfsSys.mousey - rotatedPt3.z - pt.z);
                    break;
                case 2:
                    zPts1[i] = (int)(rotatedPt1.x + pt.x);
                    xPts1[i] = (int)(rotatedPt1.y + pt.y);
                    yPts1[i] = (int)(rotatedPt1.z + pt.z);

                    zPts2[i] = (int)(rotatedPt2.x + pt.x);
                    xPts2[i] = (int)(rotatedPt2.y + pt.y);
                    yPts2[i] = (int)(rotatedPt2.z + pt.z);

                    zPts3[i] = (int)(rotatedPt3.x + pt.x);
                    xPts3[i] = (int)(rotatedPt3.y + pt.y);
                    yPts3[i] = (int)(rotatedPt3.z + pt.z);

                    d1 = distance(myIfsSys.mousex - rotatedPt1.y - pt.y, myIfsSys.mousey - rotatedPt1.z - pt.z);
                    d2 = distance(myIfsSys.mousex - rotatedPt2.y - pt.y, myIfsSys.mousey - rotatedPt2.z - pt.z);
                    d3 = distance(myIfsSys.mousex - rotatedPt3.y - pt.y, myIfsSys.mousey - rotatedPt3.z - pt.z);
                    break;
            }

            if(d1<min_d){
                min_d=d1;
                selectedD=0;
            }
            if(d2<min_d){
                min_d=d2;
                selectedD=1;
            }
            if(d3<min_d){
                min_d=d3;
                selectedD=2;
            }
        }

        switch (viewMode){
            case 0:
                xPts1[steps-1] = (int)pt.x;
                yPts1[steps-1] = (int)pt.y;
                xPts2[steps-1] = (int)pt.x;
                yPts2[steps-1] = (int)pt.y;
                xPts3[steps-1] = (int)pt.x;
                yPts3[steps-1] = (int)pt.y;
                break;
            case 1:
                xPts1[steps-1] = (int)pt.x;
                yPts1[steps-1] = (int)pt.z;
                xPts2[steps-1] = (int)pt.x;
                yPts2[steps-1] = (int)pt.z;
                xPts3[steps-1] = (int)pt.x;
                yPts3[steps-1] = (int)pt.z;
                break;
            case 2:
                xPts1[steps-1] = (int)pt.y;
                yPts1[steps-1] = (int)pt.z;
                xPts2[steps-1] = (int)pt.y;
                yPts2[steps-1] = (int)pt.z;
                xPts3[steps-1] = (int)pt.y;
                yPts3[steps-1] = (int)pt.z;
                break;
        }

        if(isSelected){
            if(!dragging){
                myIfsSys.rotateMode = selectedD;
            }

            _rg.setColor(Color.red);
            drawPolylineBolded(_rg, xPts1, yPts1, steps, myIfsSys.rotateMode==0);
            _rg.setColor(Color.green);
            drawPolylineBolded(_rg, xPts2, yPts2, steps, myIfsSys.rotateMode==1);
            _rg.setColor(Color.blue);
            drawPolylineBolded(_rg, xPts3, yPts3, steps, myIfsSys.rotateMode==2);
        }else{
            _rg.setColor(Color.darkGray);
            _rg.drawPolyline(xPts1, yPts1, steps);
            _rg.drawPolyline(xPts2, yPts2, steps);
            _rg.drawPolyline(xPts3, yPts3, steps);
        }
    }

    public void drawPolylineBolded(Graphics rg, int[] xPts, int[] yPts, int steps, boolean isSelected){
        rg.drawPolyline(xPts, yPts, steps);

        if(isSelected){
                for(int i=0; i<steps; i++){
                    xPts[i]++;
                    //yPts[i]++;
                }
                rg.drawPolyline(xPts, yPts, steps);
                for(int i=0; i<steps; i++){
                    //xPts[i]++;
                    yPts[i]++;
                }
                rg.drawPolyline(xPts, yPts, steps);
        }
    }

    public void drawInfoBox(Graphics rg){

        ifsPt selectedPt = myIfsSys.selectedPt;

        rg.setColor(Color.white);
        rg.setColor(myIfsSys.invertColors ? Color.black : Color.white);
        rg.drawString("Point " + String.valueOf(myIfsSys.pointselected + 1), 5, 15*1);
        rg.drawString("X: " + String.valueOf((double)(int)(selectedPt.x * 1000D) / 1000D), 5, 15*2);
        rg.drawString("Y: " + String.valueOf((double)(int)(selectedPt.y * 1000D) / 1000D), 5, 15*3);
        rg.drawString("Z: " + String.valueOf((double)(int)(selectedPt.z * 1000D) / 1000D), 5, 15*4);
        rg.drawString("Scale: " + String.valueOf((double)(int)(selectedPt.scale * 1000D) / 1000D), 5, 15*5);
        rg.drawString("Rotation Yaw: " + String.valueOf((double)(int)((((selectedPt.rotationYaw / Math.PI) * 180D + 36000000D) % 360D) * 1000D) / 1000D), 5, 15*6);
        rg.drawString("Rotation Pitch: " + String.valueOf((double)(int)((((selectedPt.rotationPitch / Math.PI) * 180D + 36000000D) % 360D) * 1000D) / 1000D), 5, 15*7);
        rg.drawString("Rotation Roll: " + String.valueOf((double)(int)((((selectedPt.rotationRoll / Math.PI) * 180D + 36000000D) % 360D) * 1000D) / 1000D), 5, 15*8);
        rg.drawString("CenterOfGrav: ("
                + (int)(myIfsSys.centerOfGrav.x/myIfsSys.samplesThisFrame) + ", "
                + (int)(myIfsSys.centerOfGrav.y/myIfsSys.samplesThisFrame) + ", "
                + (int)(myIfsSys.centerOfGrav.z/myIfsSys.samplesThisFrame) + ")", 5, 15*9);


        rg.drawString("Exposure: " + String.valueOf(myIfsSys.samplesThisFrame/myIfsSys.samplesNeeded), 5, 15*10);
        rg.drawString("Opacity: " + String.valueOf(selectedPt.opacity), 5, 15*11);
        rg.drawString("Iterations (. /): " + String.valueOf(myIfsSys.iterations), 5, 15*12);
        rg.drawString("Samples (nm): " + String.valueOf(myIfsSys.sampletotal), 4, 15*13);
        //rg.drawString("Expected Done %" + String.valueOf((int)Math.min(100*samplesThisFrame/samplesNeeded/Math.E, 100)), 5, 135); //TODO is dividing by E the right thing to do here?
        rg.drawString("FPS " + String.valueOf(myIfsSys.fps), 5, 15*14);
        rg.drawString("Gamma " + String.valueOf(myIfsSys.gamma), 5, 15*15);

        rg.drawString("Area " + String.valueOf((int)myIfsSys.shapeArea), 5, 15*16);
        rg.drawString("AreaDelta " + String.valueOf((int)myIfsSys.shapeAreaDelta), 5, 15*17);
        rg.drawString("DataMax " + String.valueOf((int)myIfsSys.dataMax), 5, 15*18);

        int screenheight = myIfsSys.screenheight;
        int screenwidth = myIfsSys.screenwidth;

        if(myIfsSys.viewMode==0){ //XY axis
            rg.setColor(Color.green);
            rg.drawLine(10, screenheight-65, 10, screenheight-65-50);
            rg.drawString("Y+", 10, screenheight-50);
            rg.setColor(Color.red);
            rg.drawLine(10, screenheight-65-50, 10+50, screenheight-65-50);
            rg.drawString("X+", 10+55, screenheight-50-60);
        }
        if(myIfsSys.viewMode==1){ //XZ axis
            rg.setColor(Color.blue);
            rg.drawLine(10, screenheight-65, 10, screenheight-65-50);
            rg.drawString("Z+", 10, screenheight-50);
            rg.setColor(Color.red);
            rg.drawLine(10, screenheight-65-50, 10+50, screenheight-65-50);
            rg.drawString("X+", 10+55, screenheight-50-60);
        }
        if(myIfsSys.viewMode==2){ //YZ axis
            rg.setColor(Color.green);
            rg.drawLine(10, screenheight-65, 10, screenheight-65-50);
            rg.drawString("Y+", 10, screenheight-50);
            rg.setColor(Color.blue);
            rg.drawLine(10, screenheight-65-50, 10+50, screenheight-65-50);
            rg.drawString("Z+", 10+55, screenheight-50-60);
        }
    }

    public double distance(double x, double y){
        return Math.sqrt(x * x + y * y);
    }
}
