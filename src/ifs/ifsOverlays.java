package ifs;

import java.awt.*;
import java.text.DecimalFormat;

final class ifsOverlays {
    ifsys myIfsSys;
    Graphics theGraphics;
    DragAxis selectedAxis;
    double minInterestDist=0;

    ifsPt draggyPtArrow;
    ifsPt draggyPtCenter;

    int hideTime;
    int currentLine;

    DecimalFormat df = new DecimalFormat("##.###");

    public ifsOverlays(ifsys _ifsys, Graphics rg){
        hideTime = 5000;
        currentLine=0;
        myIfsSys = _ifsys;
        theGraphics = rg;
        selectedAxis = DragAxis.X;
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

    public void drawPolyLineRotated(int[] xPts1, int[] yPts1, int[] zPts1, Graphics _rg, int steps){
        drawPolyline(xPts1, yPts1, zPts1, steps, _rg);
    }

    public void drawDraggyArrows(Graphics rg){
        for(int i=0;i<myIfsSys.theShape.pointsInUse;i++){
            drawArrows(rg, myIfsSys.theShape.pts[i], i == myIfsSys.theShape.pointSelected, myIfsSys.isDragging, i == 0, i==myIfsSys.theShape.pointNearest, true);
        }
    }

    public void updateDraggyArrows(){
        for(int i=0;i<myIfsSys.theShape.pointsInUse;i++){
            drawArrows(null, myIfsSys.theShape.pts[i], i == myIfsSys.theShape.pointSelected, myIfsSys.isDragging, i == 0, i==myIfsSys.theShape.pointNearest, false);
        }
    }

    public void drawArrows(Graphics rg, ifsPt pt, boolean isSelected, boolean isDragging, boolean isCenter, boolean isNearest, boolean draw){
        double dx=1000, dy=1000, dz=1000, minDis=32;

        ifsPt centerPt = myIfsSys.theVolume.getCameraDistortedPt(pt, myIfsSys.rp.rightEye, myIfsSys.rp.perspectiveScale);
        ifsPt xArrow = myIfsSys.theVolume.getCameraDistortedPt(pt.add(ifsPt.X_UNIT.scale(pt.radius * pt.scale)), myIfsSys.rp.rightEye, myIfsSys.rp.perspectiveScale);
        ifsPt yArrow = myIfsSys.theVolume.getCameraDistortedPt(pt.add(ifsPt.Y_UNIT.scale(pt.radius * pt.scale)), myIfsSys.rp.rightEye, myIfsSys.rp.perspectiveScale);
        ifsPt zArrow = myIfsSys.theVolume.getCameraDistortedPt(pt.add(ifsPt.Z_UNIT.scale(pt.radius * pt.scale)), myIfsSys.rp.rightEye, myIfsSys.rp.perspectiveScale);

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
            ipt1 = myIfsSys.theVolume.getCameraDistortedPt(p1.add(p2.subtract(p1).scale(1.0f / subDivs).scale(i)), myIfsSys.rp.rightEye, myIfsSys.rp.perspectiveScale);
            ipt2 = myIfsSys.theVolume.getCameraDistortedPt(p1.add(p2.subtract(p1).scale(1.0f / subDivs).scale(i + 1)), myIfsSys.rp.rightEye, myIfsSys.rp.perspectiveScale);
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
            ifsPt thePt =  myIfsSys.theShape.pts[ptIndex];
            ifsPt _thePt =  myIfsSys.theVolume.getCameraDistortedPt(myIfsSys.theShape.pts[ptIndex], myIfsSys.rp.rightEye, myIfsSys.rp.perspectiveScale);
            double wobbleFreq = 6;
            double wobbleSize = 5;
            double width = thePt.scale * thePt.radius;
            double size = Math.cos(System.currentTimeMillis()/1000.0*Math.PI*wobbleFreq)*wobbleSize + width;
            if(ptIndex==myIfsSys.theShape.pointSelected){
                rg.setColor(Color.CYAN);
            }else{
                rg.setColor(Color.DARK_GRAY);
            }
            rg.drawString("Point "+String.valueOf(ptIndex), (int)(_thePt.x-width/2-wobbleSize*2), (int)(_thePt.y-width/2-wobbleSize*2));
            drawBoxBrackets(rg, (int)(_thePt.x - size/2), (int)(_thePt.y-size/2), (int)size, (int)size, (int)(size/10));
        }
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
        currentLine=0;

        rg.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        rg.setColor(Color.white);

        if(myIfsSys.theVolume.renderMode == volume.RenderMode.VOLUMETRIC){
            drawStringOverlay(rg, log10String("Volume ", myIfsSys.theVolume.myVolume) + "U^3 Δ" + myIfsSys.theVolume.myVolumeChange);
            drawStringOverlay(rg, log10String("Surface", myIfsSys.theVolume.mySurfaceArea) + "U^2");
            drawStringOverlay(rg, log10String("AvDist ", (float) myIfsSys.theVolume.averageDistance) + "U^1");
            drawStringOverlay(rg, log10String("AvD/S  ", (float) myIfsSys.theVolume.averageDistance / (float) myIfsSys.theVolume.mySurfaceArea) + "U-1");
            drawStringOverlay(rg, log10String("AvD/V  ", (float) myIfsSys.theVolume.averageDistance / (float) myIfsSys.theVolume.myVolume) + "U-1");
            drawStringOverlay(rg, log10String("S/V    ", (float) myIfsSys.theVolume.mySurfaceArea / (float) myIfsSys.theVolume.myVolume) + "U-1");
            drawStringOverlay(rg, "Score   " + myIfsSys.theVolume.getScore(myIfsSys.rp.scoreParams));

            drawStringOverlay(rg, " ");
            if(myIfsSys.eShape.evolving){
                drawStringOverlay(rg, "Generation " + myIfsSys.eShape.familyHistory.size() + " Sibling " + (myIfsSys.eShape.evolvedSibs) + "/" + myIfsSys.eShape.shapeList.size());
            }
        }

        drawStringOverlay(rg, log10String("Dots   ", myIfsSys.theVolume.totalSamples));

        double memoryUsedMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024*1024);
        double memoryMaxMB = Runtime.getRuntime().maxMemory()/(1024*1024);

        drawStringOverlay(rg, "Memory " + (int)memoryUsedMB +" / " + (int)memoryMaxMB + "MB (" + (int)(100*memoryUsedMB/memoryMaxMB) + "%)");

        double time = (System.currentTimeMillis()-myIfsSys.theVolume.drawTime)/1000.0;

        drawStringOverlay(rg, "FPS " + String.valueOf(myIfsSys.fps));
        drawStringOverlay(rg, "RenderTime " + df.format(time) + "s");
        drawStringOverlay(rg, log10String("Dots/s", (float) (myIfsSys.theVolume.totalSamples / time)));
    }

    public void drawStringOverlay(Graphics rg, String s){
        int lineSize = 15;
        int xPad = 5;
        currentLine++;
        rg.drawString(s, xPad, lineSize * currentLine);
    }

    public String log10String(String title, float value){
        return title + " 10^" + df.format(Math.log10(value));// + " (" + value + ")";
    }
}
