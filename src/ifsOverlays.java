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
            drawArc(rg, myIfsSys.shape.pts[i], i == myIfsSys.pointselected);
        }
    }

    public void drawArc(Graphics _rg, ifsPt pt, boolean isSelected){
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
                    break;
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
            _rg.setColor(Color.red);
            _rg.drawPolyline(xPts1, yPts1, steps);
            _rg.setColor(Color.green);
            _rg.drawPolyline(xPts2, yPts2, steps);
            _rg.setColor(Color.blue);
            _rg.drawPolyline(xPts3, yPts3, steps);
        }else{
            _rg.setColor(Color.darkGray);
            _rg.drawPolyline(xPts1, yPts1, steps);
            _rg.drawPolyline(xPts2, yPts2, steps);
            _rg.drawPolyline(xPts3, yPts3, steps);
        }
    }

    public void drawInfoBox(Graphics rg){

        ifsPt selectedPt = myIfsSys.selectedPt;

        rg.setColor(Color.white);
        rg.setColor(myIfsSys.invertColors ? Color.black : Color.white);
        rg.drawString("Point " + String.valueOf(myIfsSys.pointselected + 1), 5, 15);
        rg.drawString("X: " + String.valueOf((double)(int)(selectedPt.x * 1000D) / 1000D), 5, 30);
        rg.drawString("Y: " + String.valueOf((double)(int)(selectedPt.y * 1000D) / 1000D), 5, 45);
        rg.drawString("Z: " + String.valueOf((double)(int)(selectedPt.z * 1000D) / 1000D), 5, 60);
        rg.drawString("Scale: " + String.valueOf((double)(int)(selectedPt.scale * 1000D) / 1000D), 5, 75);
        rg.drawString("Rotation Roll: " + String.valueOf((double)(int)((((selectedPt.rotationRoll / Math.PI) * 180D + 36000000D) % 360D) * 1000D) / 1000D), 5, 90);
        rg.drawString("Opacity: " + String.valueOf(selectedPt.opacity), 5, 105);
        rg.drawString("Iterations (. /): " + String.valueOf(myIfsSys.iterations), 5, 120);
        rg.drawString("Samples (nm): " + String.valueOf(myIfsSys.sampletotal), 4, 135);
        //rg.drawString("Expected Done %" + String.valueOf((int)Math.min(100*samplesThisFrame/samplesNeeded/Math.E, 100)), 5, 135); //TODO is dividing by E the right thing to do here?
        rg.drawString("FPS " + String.valueOf(myIfsSys.fps), 5, 150);
        rg.drawString("Gamma " + String.valueOf(myIfsSys.gamma), 5, 165);

        rg.drawString("Area " + String.valueOf((int)myIfsSys.shapeArea), 5, 195);
        rg.drawString("AreaDelta " + String.valueOf((int)myIfsSys.shapeAreaDelta), 5, 210);
        rg.drawString("DataMax " + String.valueOf((int)myIfsSys.dataMax), 5, 225);

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
}
