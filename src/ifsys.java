import javax.swing.*;
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

    int pixels[];
    Image render;
    Graphics rg;
    long fps;
    long framesThisSecond;
    long oneSecondAgo;
    long lastMoveTime;

    volume theVolume;
    pdf3D thePdf;

    //user params
        boolean framesHidden;
        boolean infoHidden;
        boolean usePDFSamples;
        boolean guidesHidden;
        double samplesPerFrame;
        int iterations;
        int pointNearest, pointSelected;
        ifsPt selectedPt;

        boolean shiftDown;
        boolean ctrlDown;
        boolean altDown;
        int mousex, mousey, mousez;
        int mouseScroll;
        int rotateMode;
        double brightnessMultiplier;

    double randomDoubles[];
    int randomInts[];
    int rndNum;

    double samplesPerPdfScaler;

    ifsShape shape;

    ifsMenu theMenu;

    int maxPoints;

    //drag vars
        int mousemode; //current mouse button
        double startDragX, startDragY, startDragZ;
        double startDragPX, startDragPY, startDragPZ;
        double startDragDist;
        double startDragAngleYaw;
        double startDragAnglePitch;
        double startDragScale;

    boolean started;
    boolean isDragging;

    ifsOverlays overlays;

    boolean holdFrame;

    boolean usingFindEdges;
    boolean usingThreshold;
    int threshold;

    boolean usingPotential;
    int potentialRadius;

    int overlayHideTime;

    public ifsys(){

        usingPotential=false;
        potentialRadius=4;

        overlayHideTime=1000;
        started=false;
        oneSecondAgo =0;
        framesThisSecond = 0;
        altDown=false;
        ctrlDown=false;
        shiftDown=false;
        game = new mainthread();
        quit = false;
        framesHidden = true;
        infoHidden = false;
        usePDFSamples = true;
        guidesHidden = false;
        screenwidth = 1024;
        screenheight = 1024;
        pixels = new int[screenwidth * screenheight];

        iterations = 2;
        mousemode = 0;

        maxPoints = 100;
        shape = new ifsShape(maxPoints);
        mouseScroll = 0;
        pointNearest =-1;
        pointSelected =-1;
        isDragging = false;

        theVolume = new volume(screenwidth, screenheight, 1024);
        theVolume.clear();
        thePdf = new pdf3D();

        rotateMode=0;
        lastMoveTime=0;
        brightnessMultiplier = 1;

        randomDoubles = new double[4096*4096];
        rndNum=0;

        samplesPerFrame = 512;
        samplesPerPdfScaler = 0.25; //decrease for higher fps while drawing PDFs

        holdFrame=false;

        usingThreshold = false;
        usingFindEdges = false;
        threshold = 64;
    }

    public static void main(String[] args) {
        ifsys is = new ifsys();
        is.setSize(is.screenwidth, is.screenheight); // same size as defined in the HTML APPLET
        JFrame frame = new JFrame("");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel sideMenu = new JPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, is, sideMenu);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(is.screenwidth);
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        frame.setSize(is.screenwidth+200, is.screenheight);
        frame.setVisible(true);

        is.theMenu = new ifsMenu(frame, is, sideMenu);

        is.init();
    }

    public void init() {
        start();
        shape.updateCenter();
        clearframe();
        gamefunc();
    }

    public void findNearestPt(){
        switch (theVolume.preferredDirection){
            case XY:
                pointNearest = shape.getNearestPtIndexXY(mousex, mousey);
                break;
            case YZ:
                pointNearest = shape.getNearestPtIndexYZ(mousey, mousez);
                break;
            case XZ:
                pointNearest = shape.getNearestPtIndexXZ(mousex, mousez);
                break;
        }
    }

    public void selectedNearestPt(){
        selectedPt = shape.pts[pointNearest];
        pointSelected = pointNearest;
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

        overlays = new ifsOverlays(this, rg);

        //genRandomNums();

        clearframe();
        game.start();
        shape.setToPreset(0);

        started = true;
    }

    public void genRandomNums(){
        for(int i=0; i<randomDoubles.length; i++){
            randomDoubles[i]=Math.random();
        }
    }

    public double randomDouble(){
        return Math.random();
        //return randomDoubles[(rndNum++)&16777215];
    }

    public void update(Graphics gr){
        paint(gr);
    }

    public void paint(Graphics gr){
        framesThisSecond++;
        if(System.currentTimeMillis()- oneSecondAgo >=1000){
            oneSecondAgo = System.currentTimeMillis();
            fps= framesThisSecond;
            //target framerate:
            //samplesPerFrame *= fps/50.0;
            //samplesPerFrame = Math.floor(samplesPerFrame);
            framesThisSecond =0;
        }

        generatePixels();
        try{ //TODO why does this err?
            rg.drawImage(createImage(new MemoryImageSource(screenwidth, screenheight, pixels, 0, screenwidth)), 0, 0, screenwidth, screenheight, this);
            rg.drawImage(thePdf.sampleImage, getWidth() - 50, 0, 50, 50, this);
            rg.setColor(Color.blue);

            if(!guidesHidden){
                overlays.drawArcs(rg);
                overlays.drawSpecialPoints(rg);
                overlays.drawBox(rg, pointSelected);
                overlays.drawBox(rg, pointNearest);
            }

            if(!infoHidden && pointNearest >=0){
                overlays.drawInfoBox(rg);
            }

        }catch (Exception e){

        }



        gr.drawImage(render, 0, 0, screenwidth, screenheight, this);
    }

    public void generatePixels(){

        double scaler = 1;//255/theVolume.dataMax * brightnessMultiplier;
        double area = 0;
        int scaledColor = 0;
        double[][] projection = theVolume.getScaledProjection(brightnessMultiplier);

        if(usingPotential){
            projection = theVolume.getPotential(projection, potentialRadius);
        }

        if(usingThreshold){
            projection = theVolume.getThreshold(projection, threshold);
        }

        if(usingFindEdges){
            projection = theVolume.findEdges(projection);
        }

        int argb;

        for(int x = 0; x < projection.length; x++){
            for(int y=0; y<projection[x].length; y++){
                argb = 255;
                scaledColor = (int)projection[x][y];
                //if(usingThreshold) scaledColor = projection[x][y] > threshold ? 255 : 0;
                argb = (argb << 8) + scaledColor;
                argb = (argb << 8) + scaledColor;
                argb = (argb << 8) + scaledColor;
                pixels[x+y*projection.length] = argb;
                area+=scaler*projection[x][y];
            }
        }
    }

    public void clearframe(){
        if(!holdFrame){
            for(int a = 0; a < screenwidth * screenheight; a++){
                pixels[a] = 0xff000000;
            }
            theVolume.clear();
        }
    }

    public void putPdfSample(ifsPt dpt, double cumulativeRotationYaw, double cumulativeRotationPitch, double cumulativeScale, double cumulativeOpacity, ifsPt thePt, double scaleDown, int index){
        double centerX = thePdf.sampleWidth/2;
        double centerY = thePdf.sampleHeight/2;
        double centerZ = thePdf.sampleDepth/2;
        double exposureAdjust = cumulativeScale*thePt.scale*thePt.radius;

        double sampleX, sampleY, sampleZ;
        double ptColor, scale, pointDegreesYaw, pointDegreesPitch;
        ifsPt rpt;

        //rotate/scale the point
        //double pointDist = shape.distance(sampleX, sampleY, 0)*cumulativeScale*thePt.scale*thePt.radius/thePdf.sampleWidth;

        scale = cumulativeScale*thePt.scale*thePt.radius/thePdf.sampleWidth;

        pointDegreesYaw = thePt.rotationYaw +cumulativeRotationYaw;
        pointDegreesPitch = thePt.rotationPitch +cumulativeRotationPitch;//Math.PI/2+thePt.rotationPitch -thePt.degreesPitch+cumulativeRotationPitch;

        int iters = (int)(samplesPerPdfScaler * scale*scale/scaleDown)+1;//(int)(Math.min(samplesPerPdfScaler, Math.PI*scale*scale/4/scaleDown)+1);

        iters=iters&(4095); //limit to 4095

        for(int iter=0; iter<iters; iter++){
            sampleX = randomDouble()*thePdf.sampleWidth;
            sampleY = randomDouble()*thePdf.sampleHeight;
            sampleZ = randomDouble()*thePdf.sampleDepth;
            //modulate with image
            ptColor = thePdf.volume[(int)sampleX][(int)sampleY][(int)sampleZ];
            if(ptColor>0){
                ptColor = ptColor/255.0*cumulativeOpacity/scaleDown*exposureAdjust*exposureAdjust;
                rpt = new ifsPt((sampleX-centerX)*scale,(sampleY-centerY)*scale,(sampleZ-centerZ)*scale).getRotatedPt(-pointDegreesPitch, -pointDegreesYaw); //placed point
                //put pixel
                theVolume.putPixel(new ifsPt(dpt.x+rpt.x,dpt.y+rpt.y, dpt.z+rpt.z),ptColor);
            }
        }
    }

    public void limitParams(){
        if(brightnessMultiplier <1.0/64.0){
            brightnessMultiplier =1.0/64.0;}
        if(brightnessMultiplier >128){
            brightnessMultiplier =128;}

        if(samplesPerFrame <2){
            samplesPerFrame =2;}
        if(samplesPerFrame >32768){
            samplesPerFrame =32768;}

        if(potentialRadius>16){
            potentialRadius=16;
        }
        if(potentialRadius<1){
            potentialRadius=1;
        }
    }

    public void gamefunc(){

        limitParams();

        theMenu.updateSideMenu();

        guidesHidden = System.currentTimeMillis() - lastMoveTime > overlayHideTime;

        if(shape.pointsInUse != 0){

            for(int a = 0; a < samplesPerFrame*samplesPerPdfScaler; a++){
                int randomIndex = 0;
                ifsPt dpt = new ifsPt(shape.pts[randomIndex]);
                ifsPt rpt;

                double size, yaw, pitch;//, roll;

                double cumulativeScale = 1.0;
                double cumulativeOpacity = 1;

                double cumulativeRotationYaw = 0;
                double cumulativeRotationPitch = 0;
                //double cumulativeRotationRoll = 0;

                double scaleDownMultiplier = Math.pow(shape.pointsInUse,iterations-1); //this variable is used to tone down repeated pixels so leaves and branches are equally exposed

                for(int d = 0; d < iterations; d++){
                    scaleDownMultiplier/=shape.pointsInUse;

                    randomIndex = 1 + (int)(randomDouble() * (double) (shape.pointsInUse-1));

                    if(d==0){randomIndex=0;}

                    if(d!=0){
                        size = shape.pts[randomIndex].radius * cumulativeScale;
                        yaw = Math.PI/2D - shape.pts[randomIndex].degreesYaw + cumulativeRotationYaw;
                        pitch = Math.PI/2D - shape.pts[randomIndex].degreesPitch + cumulativeRotationPitch;

                        rpt = new ifsPt(size,0,0).getRotatedPt(-pitch, -yaw);

                        dpt.x += rpt.x;
                        dpt.y += rpt.y;
                        dpt.z -= rpt.z;
                    }

                    if(usePDFSamples)
                        putPdfSample(dpt, cumulativeRotationYaw,cumulativeRotationPitch, cumulativeScale, cumulativeOpacity, shape.pts[randomIndex], scaleDownMultiplier, randomIndex);
                    cumulativeScale *= shape.pts[randomIndex].scale/shape.pts[0].scale;
                    cumulativeOpacity *= shape.pts[randomIndex].opacity;

                    cumulativeRotationYaw += shape.pts[randomIndex].rotationYaw;
                    cumulativeRotationPitch += shape.pts[randomIndex].rotationPitch;
                }

                theVolume.putPixel(dpt, cumulativeOpacity);
            }
        }
    }

    public void mouseClicked(MouseEvent mouseevent){
    }

    public void mousePressed(MouseEvent e){
        mousemode = e.getButton();
        getMouseXYZ(e);

        selectedNearestPt();
        //findNearestPt();

        if(e.getClickCount()==2){
            if(mousemode == 1){ //add point w/ double click
                shape.addPoint(mousex, mousey, mousez);
                clearframe();
                gamefunc();
            }else if(mousemode == 3){ //remove point w/ double right click
                shape.deletePoint(pointNearest);
                clearframe();
                gamefunc();
            }
        }else{
            startDragX = mousex;
            startDragY = mousey;
            startDragZ = mousez;
            shape.updateCenter();

            startDragPX = selectedPt.x;
            startDragPY = selectedPt.y;
            startDragPZ = selectedPt.z;
            switch (theVolume.preferredDirection){
                case XY:
                    startDragDist = shape.distance(startDragX - selectedPt.x, startDragY - selectedPt.y, 0);
                    startDragAngleYaw = selectedPt.rotationYaw + Math.atan2(startDragX - selectedPt.x, startDragY - selectedPt.y);
                    startDragAnglePitch = selectedPt.rotationPitch + Math.atan2(startDragX - selectedPt.x, startDragY - selectedPt.y);
                    break;
                case XZ:
                    startDragDist = shape.distance(startDragX - selectedPt.x, 0, startDragZ - selectedPt.z);
                    startDragAngleYaw = selectedPt.rotationYaw + Math.atan2(startDragX - selectedPt.x, startDragZ - selectedPt.z);
                    startDragAnglePitch = selectedPt.rotationPitch + Math.atan2(startDragX - selectedPt.x, startDragZ - selectedPt.z);
                    break;
                case YZ:
                    startDragDist = shape.distance(0, startDragY - selectedPt.y, startDragZ - selectedPt.z);
                    startDragAngleYaw = selectedPt.rotationYaw + Math.atan2(startDragY - selectedPt.y, startDragZ - selectedPt.z);
                    startDragAnglePitch = selectedPt.rotationPitch + Math.atan2(startDragY - selectedPt.y, startDragZ - selectedPt.z);
                    break;
            }

            startDragScale = selectedPt.scale;

            requestFocus();
        }
    }

    public void mouseReleased(MouseEvent e){
        setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        mousemode = 0;
        isDragging=false;
    }

    public void mouseEntered(MouseEvent e){
        setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void mouseExited(MouseEvent e){
        setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void getMouseXYZ(MouseEvent e){
        switch (theVolume.preferredDirection){
            case XY:
                mousex = e.getX();
                mousey = e.getY();
                mousez = 0;
                break;
            case YZ:
                mousey = e.getX();
                mousez = e.getY();
                mousex = 0;
                break;
            case XZ:
                mousex = e.getX();
                mousez = e.getY();
                mousey = 0;
                break;
        }
    }

    public void mouseDragged(MouseEvent e){
        getMouseXYZ(e);

        isDragging=true;
        lastMoveTime = System.currentTimeMillis();
        if(mousemode == 1){ //left click to move a point/set
            setCursor (Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            selectedPt.x = startDragPX + (mousex - startDragX);
            selectedPt.y = startDragPY + (mousey - startDragY);
            selectedPt.z = startDragPZ + (mousez - startDragZ);
        }
        else if(mousemode == 3){ //right click to rotate point/set
            setCursor (Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
            double scaleDelta = 0;//shape.distance(mousex - selectedPt.x, mousey - selectedPt.y, mousez - shape.pts[0].z)/startDragDist;

            switch (theVolume.preferredDirection){
                case XY:
                    scaleDelta = shape.distance(mousex - selectedPt.x, mousey - selectedPt.y, 0)/startDragDist;
                    break;
                case YZ:
                    scaleDelta = shape.distance(0, mousey - selectedPt.y, mousez - shape.pts[0].z)/startDragDist;
                    break;
                case XZ:
                    scaleDelta = shape.distance(mousex - selectedPt.x, 0, mousez - shape.pts[0].z)/startDragDist;
                    break;
            }

            if(rotateMode==0){
                double rotationDelta = (Math.atan2(mousex - selectedPt.x, mousey - selectedPt.y)- startDragAngleYaw);
                selectedPt.rotationYaw = Math.PI * 2 - rotationDelta;
            }else if(rotateMode==1){
                double rotationDelta = (Math.atan2(mousex - selectedPt.x, mousey - selectedPt.y)- startDragAnglePitch);
                selectedPt.rotationPitch = Math.PI * 2 - rotationDelta;
            }

            selectedPt.scale = startDragScale*scaleDelta;
        }

        shape.updateCenter();
        clearframe();
        gamefunc();
    }

    public void mouseWheelMoved(MouseWheelEvent e) {

        lastMoveTime = System.currentTimeMillis();

        mouseScroll += e.getWheelRotation();

        double changeFactor = 0.9;

        if(e.getWheelRotation()>0){ //scroll down
            //decrease point opacity
            selectedPt.opacity*=changeFactor;

        }else{ //scroll up
            //increase point opacity
            selectedPt.opacity/=changeFactor;

            if(selectedPt.opacity>1){ //values above 1 break the line function so instead we reduce the other points for the same effect
                selectedPt.opacity=1.0D;
                for(int i=0; i<shape.pointsInUse; i++){
                    shape.pts[i].opacity*=changeFactor;
                }
            }
        }

        clearframe();
        gamefunc();
    }

    public void mouseMoved(MouseEvent e){
        findNearestPt();
        getMouseXYZ(e);
        lastMoveTime = System.currentTimeMillis();
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
        //clearframe();
        gamefunc();
    }

    public void keyReleased(KeyEvent e){
        if(e.getKeyCode()==KeyEvent.VK_ALT)
            altDown=false;
        if(e.getKeyCode()==KeyEvent.VK_CONTROL)
            ctrlDown=false;
        if(e.getKeyCode()==KeyEvent.VK_SHIFT)
            shiftDown=false;


        if(e.getKeyChar() == 'd'){
            theVolume.depthLeanX-=20;
            clearframe();
        }

        if(e.getKeyChar() == 'f'){
            theVolume.depthLeanX+=20;
            clearframe();
        }

        if(e.getKeyChar() == 'w'){
            theVolume.depthLeanY-=20;
            clearframe();
        }

        if(e.getKeyChar() == 's'){
            theVolume.depthLeanY+=20;
            clearframe();
        }

        if(e.getKeyChar() == '1'){
            clearframe();
            shape.setToPreset(1);
        }

        if(e.getKeyChar() == '2'){
            clearframe();
            shape.setToPreset(2);
        }

        if(e.getKeyChar() == '3'){
            clearframe();
            shape.setToPreset(3);
        }

        if(e.getKeyChar() == '4'){
            clearframe();
            shape.setToPreset(4);
        }

        if(e.getKeyChar() == '5'){
            clearframe();
            shape.setToPreset(5);
        }

        if(e.getKeyChar() == '6'){
            clearframe();
            shape.setToPreset(6);
        }

        if(e.getKeyChar() == '0'){
            shape.setToPreset(0);
            theVolume.clear();
            iterations=8;
            brightnessMultiplier=1;
            clearframe();
            gamefunc();
        }


    }

    public void focusGained(FocusEvent focusevent){}
    public void focusLost(FocusEvent focusevent){}
}
