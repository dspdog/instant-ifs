import java.awt.*;
import java.text.DecimalFormat;

public class ifsOverlays {
    ifsys myIfsSys;
    Graphics theGraphics;

    public ifsOverlays(ifsys _ifsys, Graphics rg){
        myIfsSys = _ifsys;
        theGraphics = rg;
    }

    public void drawArcs(Graphics rg){
        for(int i=0;i<myIfsSys.shape.pointsInUse;i++){
            drawArc(rg, myIfsSys.shape.pts[i], i == myIfsSys.pointNearest, myIfsSys.isDragging, i==0);
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
        drawRegularPolygon(_rg, x - width / 2, y - height / 2, radius, 3, -Math.PI/2); //upward pointd triangle
    }

    public void drawCentroid(Graphics _rg, int x, int y){
        int width = 15;
        int height = 15;
        _rg.setColor(Color.CYAN);
        _rg.drawOval(x - width / 2, y - height / 2, width, height);
    }

    public void drawSpecialPoints(Graphics _rg){//centroid and maximum
        volume vol = myIfsSys.theVolume;
        ifsPt projectedCentroid = vol.getCentroid();

        ifsPt projectedHighPt;

        if(vol.renderMode == volume.RenderMode.PROJECT_ONLY){
            projectedHighPt = vol.highPt;
        }else{
            projectedHighPt = vol.highPtVolumetric;
        }

        if(myIfsSys.theVolume.totalSamplesAlpha >5000){
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

            ifsPt rotatedPt1 = new ifsPt(xPts1[i],yPts1[i],zPts1[i]).getRotatedPt(0, -pt.rotationYaw);
            ifsPt rotatedPt2 = new ifsPt(xPts2[i],yPts2[i],zPts2[i]).getRotatedPt(-pt.rotationPitch, -pt.rotationYaw);

            xPts1[i] = (int)(rotatedPt1.x + pt.x);
            yPts1[i] = (int)(rotatedPt1.y + pt.y);
            zPts1[i] = (int)(rotatedPt1.z + pt.z);

            xPts2[i] = (int)(rotatedPt2.x + pt.x);
            yPts2[i] = (int)(rotatedPt2.y + pt.y);
            zPts2[i] = (int)(rotatedPt2.z + pt.z);

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
        _rg.drawPolyline(xPts1, yPts1, steps);
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
            double wobbleFreq = 6;
            double wobbleSize = 5;
            double width = thePt.scale * thePt.radius;
            double size = Math.cos(System.currentTimeMillis()/1000.0*Math.PI*wobbleFreq)*wobbleSize + width;
            if(ptIndex==myIfsSys.pointSelected){
                rg.setColor(Color.CYAN);
            }else{
                rg.setColor(Color.DARK_GRAY);
            }
            rg.drawString("Point "+String.valueOf(ptIndex), (int)(thePt.x-width/2-wobbleSize*2), (int)(thePt.y-width/2-wobbleSize*2));
            drawBoxBrackets(rg, (int)(thePt.x - size/2), (int)(thePt.y-size/2), (int)size, (int)size, (int)(size/10));
        }
    }

    public void drawPDF(Graphics rg){
        int size = 128;
        int x=myIfsSys.screenwidth-size*3;
        int y=520;
        int pady = 5;
        rg.setColor(Color.WHITE);
        rg.drawImage(myIfsSys.thePdf.sampleImageX, x, y, size, size, myIfsSys);
        rg.drawString("PDF X", x, y-pady);
        rg.drawImage(myIfsSys.thePdf.sampleImageY, x+size, y, size, size, myIfsSys);
        rg.drawString("PDF Y", x+size, y-pady);
        rg.drawImage(myIfsSys.thePdf.sampleImageZ, x+size*2, y, size, size, myIfsSys);
        rg.drawString("PDF Z", x+size*2, y-pady);
    }

    public void drawBoxBrackets(Graphics rg, int x, int y, int width, int height, int bracketSize){
        //upper left
        rg.drawLine(x,y,x+bracketSize,y);
        rg.drawLine(x,y,x,y+bracketSize);

        //lower left
        rg.drawLine(x,y+height,x+bracketSize,y+height);
        rg.drawLine(x,y+height,x,y+height-bracketSize);

        //upper right
        rg.drawLine(x+width,y,x+width-bracketSize,y);
        rg.drawLine(x+width,y,x+width,y+bracketSize);

        //lower right
        rg.drawLine(x+width,y+height,x+width-bracketSize,y+height);
        rg.drawLine(x+width,y+height,x+width,y+height-bracketSize);
    }

    public void drawInfoBox(Graphics rg){
        int lineSize = 15;
        int xPad = 5;

        DecimalFormat df = new DecimalFormat("##.###");
        ifsPt selectedPt = myIfsSys.selectedPt;

        rg.setColor(Color.white);
        rg.drawString("Mouse XYZ (" + myIfsSys.mousex + ", " + myIfsSys.mousey + ", " + myIfsSys.mousez + ")", xPad, lineSize*1);
        //rg.drawString("DepthLeanX (df): " + myIfsSys.theVolume.depthLeanX, xPad, lineSize*2);
        //rg.drawString("DepthLeanY (ws): " + myIfsSys.theVolume.depthLeanY, xPad, lineSize*3);
        rg.drawString("Centroid: ("
                + (int)(myIfsSys.theVolume.getCentroid().x) + ", "
                + (int)(myIfsSys.theVolume.getCentroid().y) + ", "
                + (int)(myIfsSys.theVolume.getCentroid().z) + ")", xPad, lineSize*4);

        if(myIfsSys.theVolume.renderMode == volume.RenderMode.VOLUMETRIC){
            rg.drawString("HighPt: ("
                    + (int)(myIfsSys.theVolume.highPtVolumetric.x) + ", "
                    + (int)(myIfsSys.theVolume.highPtVolumetric.y) + ", "
                    + (int)(myIfsSys.theVolume.highPtVolumetric.z) + ")", xPad,+ lineSize*5);


            rg.drawString("DataMax 10^" + df.format(Math.log10(myIfsSys.theVolume.dataMaxVolumetric)), xPad, lineSize * 6);

            rg.drawString("Regions " + myIfsSys.theVolume.volume.getInitCount() + "/" + myIfsSys.theVolume.volume.totalRegions + " (" + (100*myIfsSys.theVolume.volume.getInitCount()/myIfsSys.theVolume.volume.totalRegions) + "%)", xPad, lineSize*14);
        }else{
            rg.drawString("HighPt2D: ("
                    + (int)(myIfsSys.theVolume.highPt.x) + ", "
                    + (int)(myIfsSys.theVolume.highPt.y) + ", "
                    + (int)(myIfsSys.theVolume.highPt.z) + ")", xPad,+ lineSize*5);

            rg.drawString("DataMax2D: 10^" + df.format(Math.log10(myIfsSys.theVolume.dataMax)), xPad, lineSize * 6);
        }

        rg.drawString("Dots: 10^" + df.format(Math.log10(myIfsSys.theVolume.totalSamples)), xPad, lineSize * 7);

        if(myIfsSys.usingFindEdges)rg.drawString("Surface Area: 10^" + df.format(Math.log10(myIfsSys.theVolume.surfaceArea)), xPad, lineSize * 8);

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
