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
            drawArc(rg, myIfsSys.shape.pts[i], i == myIfsSys.pointSelected, myIfsSys.isDragging, i==0);
        }
    }

    public void drawSpecialPoints(Graphics _rg){//center of gravity and maximum
        _rg.setColor(Color.CYAN);
        int width = 15;
        int height = 15;
        int x=0,y=0;

        if(myIfsSys.theVolume.totalSamples>1000){
            switch (myIfsSys.theVolume.preferredDirection){
                case XY:
                    x=(int)(myIfsSys.theVolume.getCenterOfGravity().x);
                    y=(int)(myIfsSys.theVolume.getCenterOfGravity().y);
                    _rg.drawRect(x-width/2,y-height/2,width,height);
                    _rg.setColor(Color.YELLOW);
                    x=(int)(myIfsSys.theVolume.highPt.x);
                    y=(int)(myIfsSys.theVolume.highPt.y);
                    _rg.drawRect(x-width/2,y-height/2,width,height);
                    break;
                case YZ:
                    x=(int)(myIfsSys.theVolume.getCenterOfGravity().y);
                    y=(int)(myIfsSys.theVolume.getCenterOfGravity().z);
                    _rg.drawRect(x-width/2,y-height/2,width,height);
                    _rg.setColor(Color.YELLOW);
                    x=(int)(myIfsSys.theVolume.highPt.y);
                    y=(int)(myIfsSys.theVolume.highPt.z);
                    _rg.drawRect(x-width/2,y-height/2,width,height);
                    break;
                case XZ:
                    x=(int)(myIfsSys.theVolume.getCenterOfGravity().x);
                    y=(int)(myIfsSys.theVolume.getCenterOfGravity().z);
                    _rg.drawRect(x-width/2,y-height/2,width,height);
                    _rg.setColor(Color.YELLOW);
                    x=(int)(myIfsSys.theVolume.highPt.x);
                    y=(int)(myIfsSys.theVolume.highPt.z);
                    _rg.drawRect(x-width/2,y-height/2,width,height);
                    break;
            }
        }
    }

    public void drawArc(Graphics _rg, ifsPt pt, boolean isSelected, boolean dragging, boolean isCenter){
        int steps = 50;

        int[] xPts1 = new int[steps];
        int[] yPts1 = new int[steps];
        int[] zPts1 = new int[steps];

        int[] xPts2 = new int[steps];
        int[] yPts2 = new int[steps];
        int[] zPts2 = new int[steps];

        double d1=999,d2=999,d3=999, min_d; //distances from arcs to mouse cursor
        int selectedD=1;

        min_d=100000;

        xPts1[0] = (int)pt.x;
        yPts1[0] = (int)pt.y;
        zPts1[0] = (int)pt.z;

        xPts2[0] = (int)pt.x;
        yPts2[0] = (int)pt.y;
        zPts2[0] = (int)pt.z;

        for(int i=1; i<steps; i++){
            //yaw
            xPts1[i] = (int)((Math.cos(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));
            yPts1[i] = (int)((Math.sin(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));
            zPts1[i] = 10*i/steps;

            //pitch
            xPts2[i] = (int)((Math.cos(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));
            yPts2[i] = 10*i/steps;
            zPts2[i] = (int)((Math.sin(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));

            ifsPt rotatedPt1 = new ifsPt(xPts1[i],yPts1[i],zPts1[i]).getRotatedPt(0,-pt.rotationYaw);
            ifsPt rotatedPt2 = new ifsPt(xPts2[i],yPts2[i],zPts2[i]).getRotatedPt(-pt.rotationPitch,-pt.rotationYaw);

            xPts1[i] = (int)(rotatedPt1.x + pt.x);
            yPts1[i] = (int)(rotatedPt1.y + pt.y);
            zPts1[i] = (int)(rotatedPt1.z + pt.z);

            xPts2[i] = (int)(rotatedPt2.x + pt.x);
            yPts2[i] = (int)(rotatedPt2.y + pt.y);
            zPts2[i] = (int)(rotatedPt2.z + pt.z);

            switch (myIfsSys.theVolume.preferredDirection){
                case XY:
                    d1 = distance(myIfsSys.mousex - rotatedPt1.x - pt.x, myIfsSys.mousey - rotatedPt1.y - pt.y);
                    d2 = distance(myIfsSys.mousex - rotatedPt2.x - pt.x, myIfsSys.mousey - rotatedPt2.y - pt.y);
                    break;
                case XZ:
                    d1 = distance(myIfsSys.mousex - rotatedPt1.x - pt.x, myIfsSys.mousey - rotatedPt1.z - pt.z);
                    d2 = distance(myIfsSys.mousex - rotatedPt2.x - pt.x, myIfsSys.mousey - rotatedPt2.z - pt.z);
                    break;
                case YZ:
                    d1 = distance(myIfsSys.mousex - rotatedPt1.y - pt.y, myIfsSys.mousey - rotatedPt1.z - pt.z);
                    d2 = distance(myIfsSys.mousex - rotatedPt2.y - pt.y, myIfsSys.mousey - rotatedPt2.z - pt.z);
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
            //if(d3<min_d){
            //    min_d=d3;
            //   selectedD=2;
            //}
        }

        xPts1[steps-1] = (int)pt.x;
        yPts1[steps-1] = (int)pt.y;
        xPts2[steps-1] = (int)pt.x;
        yPts2[steps-1] = (int)pt.y;

        if(isSelected){
            if(!dragging){
                myIfsSys.rotateMode = selectedD;
            }

            _rg.setColor(Color.red);
            drawPolylineBolded(_rg, xPts1, yPts1, zPts1, steps, myIfsSys.rotateMode==0);
            _rg.setColor(Color.green);
            drawPolylineBolded(_rg, xPts2, yPts2, zPts2, steps, myIfsSys.rotateMode==1);
        }else{
            _rg.setColor(Color.darkGray);
            if(isCenter){
                _rg.setColor(Color.BLUE);
            }
            drawPolyLineRotated(xPts1, yPts1, zPts1, _rg, steps);
            drawPolyLineRotated(xPts2, yPts2, zPts2, _rg, steps);
        }
    }

    public void drawPolyLineRotated(int[] xPts1, int[] yPts1, int[] zPts1, Graphics _rg, int steps){
        switch (myIfsSys.theVolume.preferredDirection){
            case XY:
                _rg.drawPolyline(xPts1, yPts1, steps);
                break;
            case XZ:
                _rg.drawPolyline(xPts1, zPts1, steps);
                break;
            case YZ:
                _rg.drawPolyline(yPts1, zPts1, steps);
                break;
        }
    }

    public void drawPolylineBolded(Graphics rg, int[] xPts, int[] yPts, int[] zPts, int steps, boolean isSelected){
        drawPolyLineRotated(xPts, yPts, zPts, rg, steps);

        if(isSelected){
                for(int i=0; i<steps; i++){
                    xPts[i]++;
                }
                drawPolyLineRotated(xPts, yPts, zPts, rg, steps);
                for(int i=0; i<steps; i++){
                    yPts[i]++;
                }
                drawPolyLineRotated(xPts, yPts, zPts, rg, steps);
                for(int i=0; i<steps; i++){
                    zPts[i]++;
                }
                drawPolyLineRotated(xPts, yPts, zPts, rg, steps);
        }
    }

    public void drawInfoBox(Graphics rg){

        ifsPt selectedPt = myIfsSys.selectedPt;

        rg.setColor(Color.white);
        rg.drawString("Mouse XYZ (" + myIfsSys.mousex + ", " + myIfsSys.mousey + ", " + myIfsSys.mousez + ")", 5, 15*1);
        rg.drawString("DepthLeanX (df): " + myIfsSys.theVolume.depthLeanX, 5, 15*2);
        rg.drawString("DepthLeanY (ws): " + myIfsSys.theVolume.depthLeanY, 5, 15*3);

        rg.drawString("CenterOfGrav: ("
                + (int)(myIfsSys.theVolume.getCenterOfGravity().x) + ", "
                + (int)(myIfsSys.theVolume.getCenterOfGravity().y) + ", "
                + (int)(myIfsSys.theVolume.getCenterOfGravity().z) + ")", 5, 15*4);

        rg.drawString("HighPt: ("
                + (int)(myIfsSys.theVolume.highPt.x) + ", "
                + (int)(myIfsSys.theVolume.highPt.y) + ", "
                + (int)(myIfsSys.theVolume.highPt.z) + ")", 5, 15*5);

        rg.drawString("Opacity: " + String.valueOf(selectedPt.opacity), 5, 15*6);
        rg.drawString("Iterations (. /): " + String.valueOf(myIfsSys.iterations), 5, 15*7);
        rg.drawString("Samples (nm): " + String.valueOf(myIfsSys.samplesPerFrame), 4, 15*8);
        rg.drawString("FPS " + String.valueOf(myIfsSys.fps), 5, 15*9);
        rg.drawString("Brightness (jk): " + String.valueOf(myIfsSys.brightnessMultiplier), 5, 15*10);

        rg.drawString("DataMax " + String.valueOf((int)myIfsSys.theVolume.dataMax), 5, 15*11);

        drawAxis(rg);
    }

    public void drawAxis(Graphics rg){
        int screenheight = myIfsSys.screenheight;
        int screenwidth = myIfsSys.screenwidth;
        switch (myIfsSys.theVolume.preferredDirection){
            case XY:
                rg.setColor(Color.red);
                rg.drawLine(10, screenheight-65-50, 10+50, screenheight-65-50);
                rg.drawString("X+", 10+55, screenheight-50-60);

                rg.setColor(Color.green);
                rg.drawLine(10, screenheight-65, 10, screenheight-65-50);
                rg.drawString("Y+", 10, screenheight-50);
                break;
            case YZ:
                rg.setColor(Color.green);
                rg.drawLine(10, screenheight-65-50, 10+50, screenheight-65-50);
                rg.drawString("Y+", 10+55, screenheight-50-60);

                rg.setColor(Color.blue);
                rg.drawLine(10, screenheight-65, 10, screenheight-65-50);
                rg.drawString("Z+", 10, screenheight-50);
                break;
            case XZ:
                rg.setColor(Color.red);
                rg.drawLine(10, screenheight-65-50, 10+50, screenheight-65-50);
                rg.drawString("X+", 10+55, screenheight-50-60);

                rg.setColor(Color.blue);
                rg.drawLine(10, screenheight-65, 10, screenheight-65-50);
                rg.drawString("Z+", 10, screenheight-50);
                break;
        }
    }

    public double distance(double x, double y){
        return Math.sqrt(x * x + y * y);
    }
}
