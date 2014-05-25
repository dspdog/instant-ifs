import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;

public class ifsys extends Panel
    implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, FocusListener, ActionListener
{
    mainthread game;
    boolean quit;
    int screenwidth;
    int screenheight;

    double pixelsData[];
    double dataMax = 0;
    double gamma = 0;
    int pixels[];
    Image render;
    Graphics rg;
    long fps;
    long framesThisSecond;
    long oneSecondAgo;

    long samplesThisFrame;
    double samplesNeeded;

    pdf3D thePdf;

    //user params
        boolean framesHidden;
        boolean leavesHidden;
        boolean antiAliasing;
        boolean trailsHidden;
        boolean spokesHidden;
        boolean infoHidden;
        boolean usePDFSamples;
        boolean guidesHidden;
        boolean invertColors;
        int sampletotal;
        int iterations;
        int pointselected;
        ifsPt selectedPt;

        boolean shiftDown;
        boolean ctrlDown;
        boolean altDown;
        int mousex, mousey, mousez;
        int mouseScroll;

        int viewMode;
        int rotateMode;

    ifsShape shape;
    double shapeArea;
    double shapeAreaDelta;

    int maxPoints;
    int maxLineLength;

    //drag vars
        int mousemode; //current mouse button
        double startDragX, startDragY, startDragZ;
        double startDragPX, startDragPY, startDragPZ;
        double startDragDist;
        double startDragAngleYaw;
        double startDragScale;

    boolean started;
    int preset;

    public ifsys(){
        started=false;
        samplesThisFrame=0;
        oneSecondAgo =0;
        framesThisSecond = 0;
        altDown=false;
        ctrlDown=false;
        shiftDown=false;
        game = new mainthread();
        quit = false;
        antiAliasing = true;
        framesHidden = true;
        spokesHidden = true;
        trailsHidden = true;
        leavesHidden = true;
        infoHidden = false;
        usePDFSamples = true;
        guidesHidden = false;
        invertColors = false;
        screenwidth = 1024;
        screenheight = 1024;
        pixels = new int[screenwidth * screenheight];
        pixelsData = new double[screenwidth * screenheight];
        sampletotal = 512;
        iterations = 8;
        mousemode = 0;
        samplesNeeded = 1;
        maxLineLength = screenwidth;
        maxPoints = 100;
        shape = new ifsShape(maxPoints);
        mouseScroll = 0;
        gamma = 1.0D;
        pointselected=-1;

        thePdf = new pdf3D();
        viewMode=0;
        rotateMode=0;
    }

    public static void main(String[] args) {
        Frame f = new Frame();
        f.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            };
        });
        ifsys is = new ifsys();
        is.setSize(is.screenwidth, is.screenheight); // same size as defined in the HTML APPLET
        f.add(is);
        f.pack();
        is.init();
        f.setSize(is.screenwidth, is.screenheight + 20); // add 20, seems enough for the Frame title,
        f.show();
        ifsMenu theMenu = new ifsMenu(f, is);
    }

    public void init() {
        start();
        shape.updateCenter();
        clearframe();
        gamefunc();
    }

    public void findSelectedPoint(){
        pointselected = shape.getNearestPtIndex(mousex, mousey, mousez);
        selectedPt = shape.pts[pointselected];
    }

    public void actionPerformed(ActionEvent e) {

    }



    public class mainthread extends Thread{
        public void run(){
            while(!quit) 
                try{
                    gamefunc();
                    repaint();
                    sleep(1L);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
        }

        public mainthread(){
        }
    }

    public void start(){
        setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        render = createImage(screenwidth, screenheight);
        rg = render.getGraphics();
        preset = 1;
        clearframe();
        game.start();
        shape.setToPreset(1);

        started = true;
    }

    public void update(Graphics gr){
        paint(gr);
    }

    public void drawArc(Graphics _rg, ifsPt pt){
        int steps = 50;
        int[] xPts = new int[steps];
        int[] yPts = new int[steps];
        int[] zPts = new int[steps];

        xPts[0] = (int)pt.x;
        yPts[0] = (int)pt.y;
        zPts[0] = (int)pt.z;

        for(int i=1; i<steps; i++){

            xPts[i] = (int)((Math.cos(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));
            yPts[i] = (int)((Math.sin(i*2*Math.PI/(steps-1))*pt.scale*pt.radius));
            zPts[i] = 0;

            ifsPt rotatedPt = new ifsPt(xPts[i],yPts[i],zPts[i]).getRotatedPt( 0,0,-pt.rotationYaw);
            xPts[i] = (int)(rotatedPt.x + pt.x);
            yPts[i] = (int)(rotatedPt.y + pt.y);
            zPts[i] = (int)(rotatedPt.z + pt.z);
        }

        xPts[steps-1] = (int)pt.x;
        yPts[steps-1] = (int)pt.y;

        _rg.drawPolyline(xPts, yPts, steps);
    }

    public void paint(Graphics gr){
        framesThisSecond++;
        if(System.currentTimeMillis()- oneSecondAgo >=1000){
            oneSecondAgo = System.currentTimeMillis();
            fps= framesThisSecond;
            framesThisSecond =0;
        }

        generatePixels();

        rg.drawImage(createImage(new MemoryImageSource(screenwidth, screenheight, pixels, 0, screenwidth)), 0, 0, screenwidth, screenheight, this);
        rg.drawImage(thePdf.sampleImage, getWidth() - 50, 0, 50, 50, this);

        int circleWidth;

        rg.setColor(Color.blue);
        ifsPt thePt;

        if(!guidesHidden)
        for(int i=0;i<shape.pointsInUse;i++){
            if(i==pointselected){
                rg.setColor(Color.LIGHT_GRAY);
            }else{
                if(i==0){
                    rg.setColor(Color.BLUE);
                    rg.setPaintMode();
                }else{
                    rg.setColor(Color.darkGray);
                }
            }

            thePt = shape.pts[i];
            circleWidth = (int)(thePt.scale*thePt.radius*2);
            drawArc(rg, thePt);
            //rg.drawOval((int)thePt.x-circleWidth/2, (int)thePt.y-circleWidth/2, circleWidth, circleWidth);
            //rg.drawLine((int)thePt.x, (int)thePt.y, (int)(thePt.x + Math.sin(-thePt.rotationYaw)*thePt.scale*thePt.radius),
            //                                        (int)(thePt.y + Math.cos(-thePt.rotationYaw)*thePt.scale*thePt.radius));
        }

        if(!infoHidden && pointselected>=0){
            rg.setColor(Color.white);
            rg.setColor(invertColors ? Color.black : Color.white);
            rg.drawString("Point " + String.valueOf(pointselected + 1), 5, 15);
            rg.drawString("X: " + String.valueOf((double)(int)(selectedPt.x * 1000D) / 1000D), 5, 30);
            rg.drawString("Y: " + String.valueOf((double)(int)(selectedPt.y * 1000D) / 1000D), 5, 45);
            rg.drawString("Z: " + String.valueOf((double)(int)(selectedPt.z * 1000D) / 1000D), 5, 60);
            rg.drawString("Scale: " + String.valueOf((double)(int)(selectedPt.scale * 1000D) / 1000D), 5, 75);
            rg.drawString("Rotation Yaw: " + String.valueOf((double)(int)((((selectedPt.rotationYaw / Math.PI) * 180D + 36000000D) % 360D) * 1000D) / 1000D), 5, 90);
            rg.drawString("Opacity: " + String.valueOf(selectedPt.opacity), 5, 105);
            rg.drawString("Iterations (. /): " + String.valueOf(iterations), 5, 120);
            rg.drawString("Samples (nm): " + String.valueOf(sampletotal), 4, 135);
            //rg.drawString("Expected Done %" + String.valueOf((int)Math.min(100*samplesThisFrame/samplesNeeded/Math.E, 100)), 5, 135); //TODO is dividing by E the right thing to do here?
            rg.drawString("FPS " + String.valueOf(fps), 5, 150);
            rg.drawString("Gamma " + String.valueOf(gamma), 5, 165);

            rg.drawString("Area " + String.valueOf((int)shapeArea), 5, 195);
            rg.drawString("AreaDelta " + String.valueOf((int)shapeAreaDelta), 5, 210);
            rg.drawString("DataMax " + String.valueOf((int)dataMax), 5, 225);

            if(viewMode==0){ //XY axis
                rg.setColor(Color.green);
                rg.drawLine(10, screenheight-65, 10, screenheight-65-50);
                rg.drawString("Y+", 10, screenheight-50);
                rg.setColor(Color.red);
                rg.drawLine(10, screenheight-65-50, 10+50, screenheight-65-50);
                rg.drawString("X+", 10+55, screenheight-50-60);
            }
            if(viewMode==1){ //XZ axis
                rg.setColor(Color.yellow);
                rg.drawLine(10, screenheight-65, 10, screenheight-65-50);
                rg.drawString("Z+", 10, screenheight-50);
                rg.setColor(Color.red);
                rg.drawLine(10, screenheight-65-50, 10+50, screenheight-65-50);
                rg.drawString("X+", 10+55, screenheight-50-60);
            }
            if(viewMode==2){ //YZ axis
                rg.setColor(Color.green);
                rg.drawLine(10, screenheight-65, 10, screenheight-65-50);
                rg.drawString("Y+", 10, screenheight-50);
                rg.setColor(Color.yellow);
                rg.drawLine(10, screenheight-65-50, 10+50, screenheight-65-50);
                rg.drawString("Z+", 10+55, screenheight-50-60);
            }
        }

        gr.drawImage(render, 0, 0, screenwidth, screenheight, this);
    }

    public void generatePixels(){
        double scaler = 255/dataMax;
        double area = 0;
        int scaledColor = 0;

        if(invertColors){
            for(int a = 0; a < screenwidth * screenheight; a++){
                int argb = 255;
                scaledColor = (int)(255-scaler*pixelsData[a]);
                argb = (argb << 8) + scaledColor;
                argb = (argb << 8) + scaledColor;
                argb = (argb << 8) + scaledColor;
                pixels[a] = argb;
                area+=scaler*pixelsData[a];
            }
        }else{
            for(int a = 0; a < screenwidth * screenheight; a++){
                int argb = 255;
                scaledColor = (int)(scaler*pixelsData[a]);
                argb = (argb << 8) + scaledColor;
                argb = (argb << 8) + scaledColor;
                argb = (argb << 8) + scaledColor;
                pixels[a] = argb;
                area+=scaler*pixelsData[a];
            }
        }

        shapeAreaDelta = area - shapeArea;
        shapeArea = area;
    }

    public void clearframe(){
        if(invertColors){
            for(int a = 0; a < screenwidth * screenheight; a++){
                pixels[a] = 0xffffffff;
                pixelsData[a] = 1;
            }
        }else{
            for(int a = 0; a < screenwidth * screenheight; a++){
                pixels[a] = 0xff000000;
                pixelsData[a] = 0;
            }
        }

        samplesThisFrame=0;
        dataMax = 0;
    }

    public boolean putPixel(ifsPt pt, double alpha){
        double decX, decY, decZ; //decimal parts of coordinates
        double x = pt.x; double y = pt.y; double z = pt.z;

        if(x < (double)(screenwidth - 1) &&
            y < (double)(screenheight - 1) &&
            x > 0.0D && y > 0.0D){

            decX = x - Math.floor(x);
            decY = y - Math.floor(y);

            if(antiAliasing){
                //each point contributes to 4 pixels

                pixelsData[(int)(x) + (int)(y) * screenwidth]+=alpha*(1.0-decX)*(1.0-decY);
                pixelsData[(int)(x+1) + (int)(y) * screenwidth]+=alpha*decX*(1.0-decY);
                pixelsData[(int)(x) + (int)(y+1) * screenwidth]+=alpha*decY*(1.0-decX);
                pixelsData[(int)(x+1) + (int)(y+1) * screenwidth]+=alpha*decY*decX;

                if(dataMax<pixelsData[(int)x + (int)y * screenwidth]/gamma){dataMax = pixelsData[(int)x + (int)y * screenwidth]/gamma;}
            }else{
                if(alpha>0.49)
                pixelsData[(int)(x) + (int)(y) * screenwidth]=Math.max(pixelsData[(int)(x) + (int)(y) * screenwidth], 1);
            }

            samplesThisFrame++;

            return true; //pixel is in screen bounds
        }else{
            return false; //pixel outside of screen bounds
        }

    }

    public void putPdfSample(ifsPt dpt, double cumulativeRotationYaw, double cumulativeScale, double cumulativeOpacity, ifsPt thePt, double scaleDown){
        //generate random coords

        double x=dpt.x;
        double y=dpt.y;
        double z=dpt.z;

        double sampleX = Math.random()*thePdf.sampleWidth;
        double sampleY = Math.random()*thePdf.sampleHeight;
        double sampleZ = Math.random()*thePdf.sampleDepth;

        //modulate with image
        double exposureAdjust = cumulativeScale*thePt.scale*thePt.radius;
        double ptColor = thePdf.getSliceXY_Sum((int)sampleX,(int)sampleY)/255.0*cumulativeOpacity/scaleDown*exposureAdjust*exposureAdjust;

        //rotate/scale the point
        double pointDegreesYaw = Math.atan2(sampleX - thePdf.sampleWidth/2, sampleY - thePdf.sampleHeight/2)+cumulativeRotationYaw+thePt.rotationYaw -thePt.degreesYaw;
        double pointDist = shape.distance(sampleX - thePdf.sampleWidth/2, sampleY - thePdf.sampleHeight/2, sampleZ - thePdf.sampleDepth/2)*cumulativeScale*thePt.scale*thePt.radius/thePdf.sampleWidth;
        double placedX = Math.cos(pointDegreesYaw)*pointDist;
        double placedY = Math.sin(pointDegreesYaw)*pointDist;
        double placedZ = 0;

        //put pixel
        putPixel(new ifsPt(x+placedX,y+placedY, z+placedZ), ptColor);
    }

    public void putLine(ifsPt p0, ifsPt p1, double alpha){ //TODO start/end alpha values?
        double steps = (int)shape.distance(p0.x-p1.x, p0.y-p1.y, p0.z-p1.z);
        double dx, dy, dz;

        boolean startedInScreen = false;

        if(steps>maxLineLength){steps=maxLineLength;}

        samplesThisFrame++;

        for(int i=0; i<steps; i++){
            dx = p0.x + i*(p1.x-p0.x)/steps;
            dy = p0.y + i*(p1.y-p0.y)/steps;
            dz = p0.z + i*(p1.z-p0.z)/steps;

            if(putPixel(new ifsPt(dx, dy, dz), alpha)){ //stop drawing if pixel is outside bounds
                startedInScreen = true;
            }else{
                if(startedInScreen)break;
            };
        }
    }

    public void gamefunc(){
        samplesNeeded = Math.pow(shape.pointsInUse, iterations);

        if(shape.pointsInUse != 0){


            if(!spokesHidden){ //center spokes
                for(int a=0; a<shape.pointsInUse; a++){
                    putLine(shape.pts[0], shape.pts[a], shape.pts[a].opacity);
                }
            }

            if(!framesHidden){ //center outline
                for(int a=0; a<shape.pointsInUse; a++){
                    int nextPt = (a+1)%shape.pointsInUse;
                    putLine(shape.pts[a], shape.pts[nextPt], shape.pts[nextPt].opacity);
                }
            }


            for(int a = 0; a < sampletotal; a++){
                int randomIndex = 0;
                ifsPt dpt = new ifsPt(shape.pts[randomIndex]);
                double cumulativeScale = 1.0; //shape.pts[randomIndex].scale;
                double cumulativeRotationYaw = 0; //shape.pts[randomIndex].rotationYaw;
                double cumulativeOpacity = 1; //shape.pts[randomIndex].opacity;

                double scaleDownMultiplier = Math.pow(shape.pointsInUse,iterations-1); //this variable is used to tone down repeated pixels so leaves and branches are equally exposed

                for(int d = 0; d < iterations; d++){
                    scaleDownMultiplier/=shape.pointsInUse;

                    randomIndex = 1 + (int)(Math.random() * (double) (shape.pointsInUse-1));

                    if(d==0){randomIndex=0;}

                    if(d!=0){
                        dpt.x += Math.cos((Math.PI/2D - shape.pts[randomIndex].degreesYaw) + cumulativeRotationYaw) * shape.pts[randomIndex].radius * cumulativeScale;
                        dpt.y += Math.sin((Math.PI/2D - shape.pts[randomIndex].degreesYaw) + cumulativeRotationYaw) * shape.pts[randomIndex].radius * cumulativeScale;
                        dpt.z += 0;
                    }

                    if(!trailsHidden && d < iterations-1)
                        putPixel(dpt, shape.pts[randomIndex].opacity);
                    if(usePDFSamples)
                        putPdfSample(dpt, cumulativeRotationYaw, cumulativeScale, cumulativeOpacity, shape.pts[randomIndex], scaleDownMultiplier);
                    cumulativeScale *= shape.pts[randomIndex].scale/shape.pts[0].scale;
                    cumulativeRotationYaw += shape.pts[randomIndex].rotationYaw;
                    cumulativeOpacity *= shape.pts[randomIndex].opacity;

                }
                if(!leavesHidden)
                    putPixel(dpt, cumulativeOpacity);
            }
        }
    }

    public void mouseClicked(MouseEvent mouseevent){
    }

    public void mousePressed(MouseEvent e){
        mousemode = e.getButton();

        mousex = e.getX();
        mousey = e.getY();
        mousez = 0;
        findSelectedPoint();

        if(e.getClickCount()==2){
            if(mousemode == 1){ //add point w/ double click
                shape.addPoint(mousex, mousey, mousez);
                clearframe();
                gamefunc();
            }else if(mousemode == 3){ //remove point w/ double right click
                shape.deletePoint(pointselected);
                clearframe();
                gamefunc();
            }
        }else{
            startDragX = e.getX();
            startDragY = e.getY();
            startDragZ = mousez;
            shape.updateCenter();

            if(ctrlDown || shiftDown){
                shape.saveState();
                startDragPX = shape.pts[0].x;
                startDragPY = shape.pts[0].y;
                startDragPZ = shape.pts[0].z;
                startDragDist = shape.distance(startDragX - shape.pts[0].x, startDragY - shape.pts[0].y, startDragZ - shape.pts[0].z);
                startDragAngleYaw = 0 + Math.atan2(startDragX - shape.pts[0].x, startDragY - shape.pts[0].y);
                startDragScale = 1.0;
            }else{
                startDragPX = selectedPt.x;
                startDragPY = selectedPt.y;
                startDragPZ = selectedPt.z;
                startDragDist = shape.distance(startDragX - selectedPt.x, startDragY - selectedPt.y, startDragZ - selectedPt.z);
                startDragAngleYaw = selectedPt.rotationYaw + Math.atan2(startDragX - selectedPt.x, startDragY - selectedPt.y);
                startDragScale = selectedPt.scale;
            }

            requestFocus();
        }
    }

    public void mouseReleased(MouseEvent e){
        setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        mousemode = 0;
    }

    public void mouseEntered(MouseEvent e){
        setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void mouseExited(MouseEvent e){
        setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void mouseDragged(MouseEvent e){
        if(mousemode == 1){ //left click to move a point/set
            setCursor (Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            selectedPt.x = startDragPX + (e.getX() - startDragX);
            selectedPt.y = startDragPY + (e.getY() - startDragY);
            selectedPt.z = startDragPZ + (mousez - startDragZ);
        }
        else if(mousemode == 3){ //right click to rotate point/set
            setCursor (Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));

            if(ctrlDown){ //rotate the set
                double rotationDelta = (Math.atan2(e.getX() - shape.pts[0].x , e.getY() - shape.pts[0].y )- startDragAngleYaw);
                double scaleDelta = shape.distance(e.getX() - shape.pts[0].x , e.getY() - shape.pts[0].y, mousez - shape.pts[0].z )/startDragDist;

                for(int i=1; i<shape.pointsInUse; i++){
                    shape.pts[i].x = shape.pts[0].x + scaleDelta * shape.pts[i].savedradius*Math.cos(Math.PI / 2 - shape.pts[i].saveddegrees - rotationDelta);
                    shape.pts[i].y = shape.pts[0].x + scaleDelta * shape.pts[i].savedradius*Math.sin(Math.PI / 2 - shape.pts[i].saveddegrees - rotationDelta);
                }
            }else if(shiftDown){ //rotate all points in unison
                double rotationDelta = (Math.atan2(e.getX() - shape.pts[0].x, e.getY() - shape.pts[0].y)- startDragAngleYaw);
                double scaleDelta = shape.distance(e.getX() - shape.pts[0].x, e.getY() - shape.pts[0].y, mousez - shape.pts[0].z)/startDragDist;

                for(int i=1; i<shape.pointsInUse; i++){
                    shape.pts[i].rotationYaw = shape.pts[i].savedrotation + (Math.PI * 2 - rotationDelta);
                    shape.pts[i].scale = shape.pts[i].savedscale*scaleDelta;
                }
            }else{ //move a single point
                double rotationDelta = (Math.atan2(e.getX() - selectedPt.x, e.getY() - selectedPt.y)- startDragAngleYaw);
                double scaleDelta = shape.distance(e.getX() - selectedPt.x, e.getY() - selectedPt.y, mousez - shape.pts[0].z)/startDragDist;

                selectedPt.rotationYaw = Math.PI * 2 - rotationDelta;
                selectedPt.scale = startDragScale*scaleDelta;
            }
        }

        shape.updateCenter();
        clearframe();
        gamefunc();
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        mouseScroll += e.getWheelRotation();

        double changeFactor = 0.9;

        if(e.getWheelRotation()>0){ //scroll down
            if(shiftDown || ctrlDown){//decrease gamma
                gamma*=0.9;
            }else{//decrease point opacity
                selectedPt.opacity*=changeFactor;
            }
        }else{ //scroll up
            if(shiftDown || ctrlDown){//increase gamma
                gamma/=0.9;
            }else{//increase point opacity
                selectedPt.opacity/=changeFactor;

                if(selectedPt.opacity>1){ //values above 1 break the line function so instead we reduce the other points for the same effect
                    selectedPt.opacity=1.0D;
                    for(int i=0; i<shape.pointsInUse; i++){
                        shape.pts[i].opacity*=changeFactor;
                    }
                }
            }
        }

        clearframe();
        gamefunc();
    }

    public void mouseMoved(MouseEvent e){
        findSelectedPoint();
        mousex = e.getX();
        mousey = e.getY();
    }

    public void keyTyped(KeyEvent e){
    }

    public void keyPressed(KeyEvent e){
        if(e.getKeyCode()==KeyEvent.VK_ALT)
            altDown=true;
        if(e.getKeyCode()==KeyEvent.VK_CONTROL)
            ctrlDown=true;
        if(e.getKeyCode()==KeyEvent.VK_SHIFT)
            shiftDown=true;
        shape.updateCenter();
        clearframe();
        gamefunc();
    }

    public void keyReleased(KeyEvent e){
        if(e.getKeyCode()==KeyEvent.VK_ALT)
            altDown=false;
        if(e.getKeyCode()==KeyEvent.VK_CONTROL)
            ctrlDown=false;
        if(e.getKeyCode()==KeyEvent.VK_SHIFT)
            shiftDown=false;

        if(e.getKeyChar() == '/')
            iterations++;
        if(e.getKeyChar() == '.' && iterations > 1)
            iterations--;

        if(e.getKeyChar() == 'm')
            sampletotal *= 2;
        if(e.getKeyChar() == 'n' && sampletotal > 1)
            sampletotal /= 2;

        if(sampletotal<2){sampletotal=2;}
        if(sampletotal>32768){sampletotal=32768;}

        if(e.getKeyChar() == '1')
            shape.setToPreset(1);
        if(e.getKeyChar() == '2')
            shape.setToPreset(2);
        if(e.getKeyChar() == '3')
            shape.setToPreset(3);
        if(e.getKeyChar() == '4')
            shape.setToPreset(4);
        if(e.getKeyChar() == '5')
            shape.setToPreset(5);
        if(e.getKeyChar() == '6')
            shape.setToPreset(6);
    }

    public void focusGained(FocusEvent focusevent){}
    public void focusLost(FocusEvent focusevent){}
}
