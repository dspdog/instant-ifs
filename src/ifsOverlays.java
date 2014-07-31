import java.awt.*;
import java.text.DecimalFormat;

public class ifsOverlays {
    ifsys myIfsSys;
    Graphics theGraphics;
    DragAxis selectedAxis;
    double minInterestDist=0;

    ifsPt draggyPtArrow;
    ifsPt draggyPtCenter;

    int hideTime;

    public ifsOverlays(ifsys _ifsys, Graphics rg){
        hideTime = 5000;
        myIfsSys = _ifsys;
        theGraphics = rg;
        selectedAxis = DragAxis.X;
    }

    public void drawArcs(Graphics rg){
        for(int i=0;i<myIfsSys.shape.pointsInUse;i++){
            drawArc(rg, myIfsSys.shape.pts[i], i == myIfsSys.pointNearest, myIfsSys.isDragging, i == 0);
        }
    }

    public void drawRegularPolygon(Graphics rg, int x, int y, int rad, int sides, double rotation){
        for(int i=0; i<sides; i++){
            rg.drawLine(
                    (int)(Math.cos(i*Math.PI*2.0/sides+rotation)*rad+x),
                    (int)(Math.sin(i*Math.PI*2.0/sides+rotation)*rad+y),
                    (int)(Math.cos((i+1)*Math.PI*2.0/sides+rotation)*rad+x),
                    (int)(Math.sin((i+1)*Math.PI*2.0/sides+rotation)*rad+y)
            );
        }
    }

    public void drawHighPt(Graphics _rg, int x, int y){
        int radius = 10;
        int width = (int)(10/Math.sqrt(2));
        int height = (int)(10/Math.sqrt(2));
        _rg.setColor(Color.YELLOW);
        drawRegularPolygon(_rg, x - width / 2, y - height / 2, radius, 3, -Math.PI / 2); //upward pointd triangle
    }

    public void drawCentroid(Graphics _rg, int x, int y){
        int width = 15;
        int height = 15;
        _rg.setColor(Color.CYAN);
        _rg.drawOval(x - width / 2, y - height / 2, width, height);
    }

    public void drawSpecialPoints(Graphics _rg){//centroid and maximum
        if(myIfsSys.theVolume.totalSamplesAlpha >5000){
            ifsPt projectedCentroid = myIfsSys.theVolume.getCameraDistortedPt(myIfsSys.theVolume.getCentroid());
            ifsPt projectedHighPt;

            if(myIfsSys.theVolume.renderMode == volume.RenderMode.PROJECT_ONLY){
                projectedHighPt =  myIfsSys.theVolume.getCameraDistortedPt(myIfsSys.theVolume.highPt);
            }else{
                projectedHighPt = myIfsSys.theVolume.getCameraDistortedPt(myIfsSys.theVolume.highPtVolumetric);
            }

            int x=(int)projectedCentroid.x;
            int y=(int)projectedCentroid.y;
            drawCentroid(_rg,x,y);
            x=(int)projectedHighPt.x;
            y=(int)projectedHighPt.y;
            drawHighPt(_rg, x, y);
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

        ifsPt centerPt = myIfsSys.theVolume.getCameraDistortedPt(pt);

        xPts1[0] = (int)centerPt.x;
        yPts1[0] = (int)centerPt.y;
        zPts1[0] = (int)centerPt.z;

        xPts2[0] = (int)centerPt.x;
        yPts2[0] = (int)centerPt.y;
        zPts2[0] = (int)centerPt.z;

        for(int i=1; i<steps; i++){
            //yaw
            xPts1[i] = (int)((Math.cos(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));
            yPts1[i] = (int)((Math.sin(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));
            zPts1[i] = 10*i/steps;

            //pitch
            xPts2[i] = (int)((Math.cos(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));
            yPts2[i] = 10*i/steps;
            zPts2[i] = (int)((Math.sin(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));

            ifsPt rotatedPt1 = new ifsPt(xPts1[i],yPts1[i],zPts1[i]).getRotatedPt(0, -pt.rotationYaw).add(pt);
            ifsPt rotatedPt2 = new ifsPt(xPts2[i],yPts2[i],zPts2[i]).getRotatedPt(-pt.rotationPitch, -pt.rotationYaw).add(pt);

            rotatedPt1 = myIfsSys.theVolume.getCameraDistortedPt(rotatedPt1);
            rotatedPt2 = myIfsSys.theVolume.getCameraDistortedPt(rotatedPt2);

            xPts1[i] = (int)(rotatedPt1.x);
            yPts1[i] = (int)(rotatedPt1.y);
            zPts1[i] = (int)(rotatedPt1.z);

            xPts2[i] = (int)(rotatedPt2.x);
            yPts2[i] = (int)(rotatedPt2.y);
            zPts2[i] = (int)(rotatedPt2.z);

            d1 = distance(myIfsSys.mousex - rotatedPt1.x - pt.x, myIfsSys.mousey - rotatedPt1.y - pt.y);
            d2 = distance(myIfsSys.mousex - rotatedPt2.x - pt.x, myIfsSys.mousey - rotatedPt2.y - pt.y);

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

        //pt = myIfsSys.theVolume.getCameraDistortedPt(pt);

        xPts1[steps-1] = (int)centerPt.x;
        yPts1[steps-1] = (int)centerPt.y;
        xPts2[steps-1] = (int)centerPt.x;
        yPts2[steps-1] = (int)centerPt.y;

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
        drawPolyline(xPts1, yPts1, zPts1, steps, _rg);
    }

    public void drawDraggyArrows(Graphics rg){
        for(int i=0;i<myIfsSys.shape.pointsInUse;i++){
            drawArrows(rg, myIfsSys.shape.pts[i], i == myIfsSys.pointSelected, myIfsSys.isDragging, i == 0, i==myIfsSys.pointNearest, true);
        }
    }

    public void updateDraggyArrows(){
        for(int i=0;i<myIfsSys.shape.pointsInUse;i++){
            drawArrows(null, myIfsSys.shape.pts[i], i == myIfsSys.pointSelected, myIfsSys.isDragging, i == 0, i==myIfsSys.pointNearest, false);
        }
    }

    public void drawArrows(Graphics rg, ifsPt pt, boolean isSelected, boolean isDragging, boolean isCenter, boolean isNearest, boolean draw){
        double dx=1000, dy=1000, dz=1000, minDis=32;

        ifsPt centerPt = myIfsSys.theVolume.getCameraDistortedPt(pt);
        ifsPt xArrow = myIfsSys.theVolume.getCameraDistortedPt(pt.add(ifsPt.X_UNIT.scale(pt.radius * pt.scale)));
        ifsPt yArrow = myIfsSys.theVolume.getCameraDistortedPt(pt.add(ifsPt.Y_UNIT.scale(pt.radius * pt.scale)));
        ifsPt zArrow = myIfsSys.theVolume.getCameraDistortedPt(pt.add(ifsPt.Z_UNIT.scale(pt.radius * pt.scale)));

        ifsPt centerPt2 = (pt);
        ifsPt xArrow2 = (pt.add(ifsPt.X_UNIT.scale(pt.radius * pt.scale)));
        ifsPt yArrow2 = (pt.add(ifsPt.Y_UNIT.scale(pt.radius * pt.scale)));
        ifsPt zArrow2 = (pt.add(ifsPt.Z_UNIT.scale(pt.radius * pt.scale)));

        if(draw)rg.setColor(Color.gray);
        int buffer=8;
        if(isSelected){
            if(draw)rg.setColor(Color.RED);
            if(draw)rg.drawString("X", (int)xArrow.x+buffer, (int)xArrow.y+buffer);
        }
        if(draw)dx = drawLine3D(centerPt2, xArrow2, rg, isNearest && !isSelected, isSelected);

        if(isSelected){
            if(draw)rg.setColor(Color.GREEN);
            if(draw)rg.drawString("Y", (int)yArrow.x+buffer, (int)yArrow.y+buffer);
        }
        if(draw)dy = drawLine3D(centerPt2, yArrow2, rg, isNearest && !isSelected, isSelected);

        if(isSelected){
            if(draw)rg.setColor(Color.BLUE);
            if(draw)rg.drawString("Z", (int)zArrow.x+buffer, (int)zArrow.y+buffer);
        }
        if(draw)dz = drawLine3D(centerPt2, zArrow2, rg, isNearest && !isSelected, isSelected);

        minInterestDist=minDis;


        if(isSelected){
            if(!myIfsSys.isDragging){
                int selectedMinDist = 20;
                if(dz<dx && dz<dy){selectedAxis = DragAxis.Z; if(dz>selectedMinDist){selectedAxis=DragAxis.NONE;}}
                if(dy<dx && dy<dz){selectedAxis = DragAxis.Y; if(dy>selectedMinDist){selectedAxis=DragAxis.NONE;}}
                if(dx<dz && dx<dy){selectedAxis = DragAxis.X; if(dx>selectedMinDist){selectedAxis=DragAxis.NONE;}}
                myIfsSys.selectedMovementAxis = selectedAxis;
            }

            draggyPtCenter = new ifsPt(centerPt);
            switch (selectedAxis){
                case X:
                    if(draw)rg.setColor(Color.RED);
                    if(draw)drawLine3D(centerPt2, xArrow2, rg, true, true);
                    selectedAxis = DragAxis.X;
                    draggyPtArrow = new ifsPt(xArrow);
                    break;
                case Y:
                    if(draw)rg.setColor(Color.GREEN);
                    if(draw)drawLine3D(centerPt2, yArrow2, rg, true, true);
                    selectedAxis = DragAxis.Y;
                    draggyPtArrow = new ifsPt(yArrow);
                    break;
                case Z:
                    if(draw)rg.setColor(Color.BLUE);
                    if(draw)drawLine3D(centerPt2, zArrow2, rg, true, true);
                    selectedAxis = DragAxis.Z;
                    draggyPtArrow = new ifsPt(zArrow);
                    break;
                default:
                    break;
            }
        }
    }

    enum DragAxis{
        X, Y, Z, NONE
    }

    public void drawPolyline(int[] x, int[] y, int[] z, int steps, Graphics rg){
        for (int i = 0; i < steps; i++) {
            if(myIfsSys.theVolume.volumeContains(new ifsPt(x[i],y[i],z[i]))){
                if(myIfsSys.theVolume.volumeContains(new ifsPt(x[(i+1)%steps],y[(i+1)%steps],z[(i+1)%steps]))){
                    rg.drawLine(x[i],y[i],x[(i+1)%steps],y[(i+1)%steps]);
                }
            }

        }
    }

    public int drawLine3D(ifsPt p1, ifsPt p2, Graphics rg, boolean isBold, boolean arrowHead){
        int subDivs = Math.min(50, (int)p1.distTo(p2));
        int distMin = 1000, dist;
        ifsPt ipt1;
        ifsPt ipt2;
        ifsPt mpt = new ifsPt(myIfsSys.mousePt);

        for(int i=0; i<subDivs-1; i++){
            ipt1 = myIfsSys.theVolume.getCameraDistortedPt(p1.add(p2.subtract(p1).scale(1.0f / subDivs).scale(i)));
            ipt2 = myIfsSys.theVolume.getCameraDistortedPt(p1.add(p2.subtract(p1).scale(1.0f / subDivs).scale(i + 1)));
            mpt.z=ipt1.z;
            dist = (int)ipt1.distTo(mpt);
            if(dist < distMin){
                distMin=dist;
            }
            drawline(ipt1, ipt2,rg, isBold, arrowHead && i==subDivs-2);
        }
        return distMin;
      }
    
    public void drawline(ifsPt p1, ifsPt p2, Graphics rg, boolean isBold, boolean arrowHead){
        if(myIfsSys.theVolume.volumeContains(p1)){
            if(myIfsSys.theVolume.volumeContains(p2)){
                rg.drawLine((int)p1.x,(int)p1.y,(int)p2.x,(int)p2.y);
                if(isBold){
                    rg.drawLine((int)p1.x+1,(int)p1.y+1,(int)p2.x+1,(int)p2.y+1);
                    rg.drawLine((int)p1.x-1,(int)p1.y-1,(int)p2.x-1,(int)p2.y-1);

                    rg.drawLine((int)p1.x-1,(int)p1.y+1,(int)p2.x-1,(int)p2.y+1);
                    rg.drawLine((int)p1.x-1,(int)p1.y+1,(int)p2.x-1,(int)p2.y+1);

                    rg.drawLine((int)p1.x+1,(int)p1.y-1,(int)p2.x+1,(int)p2.y-1);
                    rg.drawLine((int)p1.x+1,(int)p1.y-1,(int)p2.x+1,(int)p2.y-1);
                }

                if(arrowHead){
                    ifsPt pUnit = p2.subtract(p1).scale(0.2f);

                    double divisions = 20;
                    int baseSize = isBold ? 15 : 10;
                    for(int i=0; i<divisions-1; i++){
                        double width = baseSize*(1.0-i/divisions);
                        ifsPt circle = p2.add(pUnit.scale(i));

                        rg.fillOval((int)(circle.x-width/2),(int)(circle.y-width/2), (int)width,(int)width);
                    }
                }
            }
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

    public void drawBox(Graphics rg, int ptIndex){
        if(ptIndex>-1){
            ifsPt thePt =  myIfsSys.shape.pts[ptIndex];
            ifsPt _thePt =  myIfsSys.theVolume.getCameraDistortedPt(myIfsSys.shape.pts[ptIndex]);
            double wobbleFreq = 6;
            double wobbleSize = 5;
            double width = thePt.scale * thePt.radius;
            double size = Math.cos(System.currentTimeMillis()/1000.0*Math.PI*wobbleFreq)*wobbleSize + width;
            if(ptIndex==myIfsSys.pointSelected){
                rg.setColor(Color.CYAN);
            }else{
                rg.setColor(Color.DARK_GRAY);
            }
            rg.drawString("Point "+String.valueOf(ptIndex), (int)(_thePt.x-width/2-wobbleSize*2), (int)(_thePt.y-width/2-wobbleSize*2));
            drawBoxBrackets(rg, (int)(_thePt.x - size/2), (int)(_thePt.y-size/2), (int)size, (int)size, (int)(size/10));
        }
    }

    public void drawPDF(Graphics rg){
        int size = 128;
        int x=myIfsSys.rp.screenwidth-size*3;
        int y=myIfsSys.rp.screenheight-size*2;
        int pady = 5;
        rg.setColor(Color.WHITE);
        rg.drawImage(myIfsSys.thePdf.sampleImageX, x, y, size, size, myIfsSys);
        rg.drawString("PDF X", x, y-pady);
        rg.drawImage(myIfsSys.thePdf.sampleImageY, x + size, y, size, size, myIfsSys);
        rg.drawString("PDF Y", x + size, y - pady);
        rg.drawImage(myIfsSys.thePdf.sampleImageZ, x + size * 2, y, size, size, myIfsSys);
        rg.drawString("PDF Z", x + size * 2, y - pady);
    }

    public void drawBoxBrackets(Graphics rg, int x, int y, int width, int height, int bracketSize){
        //upper left
        rg.drawLine(x,y,x+bracketSize,y);
        rg.drawLine(x,y,x,y+bracketSize);

        //lower left
        rg.drawLine(x, y + height, x + bracketSize, y + height);
        rg.drawLine(x, y + height, x, y + height - bracketSize);

        //upper right
        rg.drawLine(x + width, y, x + width - bracketSize, y);
        rg.drawLine(x + width, y, x + width, y + bracketSize);

        //lower right
        rg.drawLine(x + width, y + height, x + width - bracketSize, y + height);
        rg.drawLine(x + width, y + height, x + width, y + height - bracketSize);
    }

    public void drawInfoBox(Graphics rg){
        int lineSize = 15;
        int xPad = 5;

        DecimalFormat df = new DecimalFormat("##.###");

        rg.setColor(Color.white);

        /*rg.drawString("Centroid: ("
                + (int)(myIfsSys.theVolume.getCentroid().x) + ", "
                + (int)(myIfsSys.theVolume.getCentroid().y) + ", "
                + (int)(myIfsSys.theVolume.getCentroid().z) + ")", xPad, lineSize*4);*/

        if(myIfsSys.theVolume.renderMode == volume.RenderMode.VOLUMETRIC){
            /*rg.drawString("HighPt: ("
                    + (int)(myIfsSys.theVolume.highPtVolumetric.x) + ", "
                    + (int)(myIfsSys.theVolume.highPtVolumetric.y) + ", "
                    + (int)(myIfsSys.theVolume.highPtVolumetric.z) + ")", xPad,+ lineSize*5);

            */
            rg.drawString("DataMax 10^" + df.format(Math.log10(myIfsSys.theVolume.dataMaxVolumetric)), xPad, lineSize * 6);
            rg.drawString("Volume 10^" + df.format(Math.log10(myIfsSys.theVolume.myVolume)) + " (" + myIfsSys.theVolume.myVolume + ")", xPad, lineSize * 1);
            //rg.drawString("Surface Area 10^" + df.format(Math.log10(myIfsSys.theVolume.mySurfaceArea)) + " (" + myIfsSys.theVolume.mySurfaceArea + ")", xPad, lineSize * 2);
            rg.drawString("Regions " + myIfsSys.theVolume.volume.getInitCount() + "/" + myIfsSys.theVolume.volume.totalRegions + " (" + (100*myIfsSys.theVolume.volume.getInitCount()/myIfsSys.theVolume.volume.totalRegions) + "%)", xPad, lineSize*14);
        }else{
            /*rg.drawString("HighPt2D: ("
                    + (int)(myIfsSys.theVolume.highPt.x) + ", "
                    + (int)(myIfsSys.theVolume.highPt.y) + ", "
                    + (int)(myIfsSys.theVolume.highPt.z) + ")", xPad,+ lineSize*5);

            rg.drawString("DataMax2D: 10^" + df.format(Math.log10(myIfsSys.theVolume.dataMax)), xPad, lineSize * 6);*/
        }

        rg.drawString("Dots: 10^" + df.format(Math.log10(myIfsSys.theVolume.totalSamples)), xPad, lineSize * 7);

        //if(myIfsSys.rp.usingFindEdges)rg.drawString("Surface Area: 10^" + df.format(Math.log10(myIfsSys.theVolume.surfaceArea)), xPad, lineSize * 8);

        double memoryUsedMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024*1024);
        double memoryMaxMB = Runtime.getRuntime().maxMemory()/(1024*1024);
        
        //rg.drawString("Render " + myIfsSys.theVolume.width + "x" + myIfsSys.theVolume.height + "x" + myIfsSys.theVolume.depth + " - " + myIfsSys.theVolume.renderMode.toString(), xPad, lineSize * 9);
        rg.drawString("Memory " + (int)memoryUsedMB +" / " + (int)memoryMaxMB + "MB (" + (int)(100*memoryUsedMB/memoryMaxMB) + "%)", xPad, lineSize * 9);

        double time = (System.currentTimeMillis()-myIfsSys.theVolume.drawTime)/1000.0;

        rg.drawString("FPS " + String.valueOf(myIfsSys.fps), xPad, lineSize*10);
        rg.drawString("RenderTime " + df.format(time) + "s", xPad, lineSize*11);
        rg.drawString("Dots/s 10^" + df.format(Math.log10((myIfsSys.theVolume.totalSamples/time))), xPad, lineSize*12);
    }

    public double distance(double x, double y){
        return Math.sqrt(x * x + y * y);
    }
}
