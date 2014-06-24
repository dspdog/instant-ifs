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
    long frameNo;
    long fps;
    long framesThisSecond;
    long oneSecondAgo;
    long lastMoveTime;

    boolean renderThrottling;
    long postProcessPeriod;
    long lastPostProcessTime;

    double lastProcessedProjection[][];

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

    boolean usingGaussian;
    int potentialRadius;

    int overlayHideTime;

    public ifsys(){


        overlayHideTime=5000;
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

        iterations = 1;
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

        samplesPerFrame = 4096;
        samplesPerPdfScaler = 0.25; //decrease for higher fps while drawing PDFs

        holdFrame=false;

        usingThreshold = false;
        usingFindEdges = false;
        threshold = 64;


        usingGaussian =false;
        potentialRadius=0;

        renderThrottling=false;
        postProcessPeriod=1000;

        thePdf.thePdfComboMode = pdf3D.comboMode.MIN;
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
        frameNo=0;
        start();
        shape.updateCenter();
        clearframe();
        gamefunc();
    }

    public void findNearestPt(){
        pointNearest = shape.getNearestPtIndexXY(mousex, mousey);
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
                    //if(frameNo%2==0)
                    //clearframe();
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

    public long randomLong() {
        long rl = System.nanoTime();
        rl ^= (rl << 21);
        rl ^= (rl >>> 35);
        rl ^= (rl << 4);
        return rl;
    }

    public double randomDouble(){
        return Math.random();
        //return randomLong()/Long.MAX_VALUE;
        //return randomDoubles[(rndNum++)&16777215];
    }

    public void update(Graphics gr){
        paint(gr);
    }

    public void paint(Graphics gr){
        frameNo++;
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
            rg.setColor(Color.green);
            rg.fillRect(0,0,1024,1024);

            rg.drawImage(createImage(new MemoryImageSource(screenwidth, screenheight, pixels, 0, screenwidth)), 0, 0, screenwidth, screenheight, this);

            //rg.drawImage(thePdf.sampleImage, getWidth() - 50, 0, 50, 50, this);
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

            if(lastMoveTime < theMenu.lastPdfPropertiesMouseMoved){
                overlays.drawPDF(rg);
            }

        }catch (Exception e){
            e.printStackTrace();
        }



        gr.drawImage(render, 0, 0, screenwidth, screenheight, this);
    }

    public void generatePixels(){
        double scaler = 1;//255/theVolume.dataMax * brightnessMultiplier;
        double area = 0;
        int scaledColor = 0;
        int scaledColor2=0;
        //double[][] projection2 = theVolume.getScaledProjection(Math.pow(2,brightnessMultiplier));
        double[][] projection1 = theVolume.getScaledDepthProjection(Math.pow(2, brightnessMultiplier));
        boolean didProcess=false;

        if(!renderThrottling || System.currentTimeMillis()-lastPostProcessTime>postProcessPeriod){
            didProcess=true;
            //if(usingGaussian){
                //projection = volume.getPotential2D(projection, potentialRadius);
                //theVolume.volume = volume.getPotential3D(theVolume.volume, potentialRadius);
            //}
            if(usingThreshold){
                projection1 = theVolume.getThreshold2D(projection1, threshold);
            }
            if(usingFindEdges){
                projection1 = theVolume.findEdges2D(projection1);
            }

            lastProcessedProjection = theVolume.getProjectionCopy(projection1);

            int argb;

            for(int x = 0; x < projection1.length; x++){
                for(int y=0; y<projection1[x].length; y++){

                    if(projection1[x][y]==0){ //"half darkened spanish blue" for background
                        argb = 255;

                        argb = (argb << 8) + 0;
                        argb = (argb << 8) + 112/2;
                        argb = (argb << 8) + 184/2;
                    }else{
                        scaledColor = (int)projection1[x][y];
                        argb = 255;

                        argb = (argb << 8) + scaledColor;
                        argb = (argb << 8) + scaledColor;
                        argb = (argb << 8) + scaledColor;
                    }

                    pixels[x+y*projection1.length] = argb;
                    area+=scaler*projection1[x][y];
                }
            }
        }else{
            projection1 = lastProcessedProjection;
        }

        if(didProcess)lastPostProcessTime=System.currentTimeMillis();
    }

    public void clearframe(){
        //if(frameNo%2==0)
        if(!holdFrame){
            theVolume.clear();
        }
    }

    public void putPdfSample(ifsPt dpt, double cumulativeRotationYaw, double cumulativeRotationPitch, double cumulativeScale, double cumulativeOpacity, ifsPt thePt, double scaleDown, int index){

        double uncertainty = potentialRadius;

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

        double uncertaintyX = uncertainty*Math.random()-uncertainty/2;
        double uncertaintyY = uncertainty*Math.random()-uncertainty/2;
        double uncertaintyZ = uncertainty*Math.random()-uncertainty/2;
        double distScaleDown = usingGaussian ? 1.0/(uncertaintyX*uncertaintyX+uncertaintyY*uncertaintyY+uncertaintyZ*uncertaintyZ) : 1.0;

        if(distScaleDown>1){distScaleDown=1;}

        int rndIndex;
        double dx=randomDouble()-0.5;
        double dy=randomDouble()-0.5;
        double dz=randomDouble()-0.5;
        rndIndex = (int)(Math.random()*thePdf.validValues);

        for(int iter=0; iter<iters; iter++){
            if(iter%256==0){
                rndIndex = (int)(Math.random()*thePdf.validValues);
            }

            sampleX = thePdf.validX[rndIndex]+dx;
            sampleY = thePdf.validY[rndIndex]+dy;
            sampleZ = thePdf.validZ[rndIndex]+dz;
            ptColor = thePdf.volume[(int)sampleX][(int)sampleY][(int)sampleZ];

            ptColor = ptColor/255.0*cumulativeOpacity/scaleDown*exposureAdjust*exposureAdjust*distScaleDown;
            rpt = new ifsPt((sampleX-centerX)*scale,(sampleY-centerY)*scale,(sampleZ-centerZ)*scale).getRotatedPt(-pointDegreesPitch, -pointDegreesYaw); //placed point

            //put pixel
            if(theVolume.putPixel(new ifsPt(dpt.x+rpt.x+uncertaintyX,
                                         dpt.y+rpt.y+uncertaintyY,
                                         dpt.z+rpt.z+uncertaintyZ),ptColor)){
                rndIndex+=(int)(Math.random()*4+1);
                rndIndex=rndIndex%(thePdf.validValues | 1);
                //dx=randomDouble()-0.5;
                //dy=randomDouble()-0.5;
                //dz=randomDouble()-0.5;
            }else{
                rndIndex = (int)(Math.random()*thePdf.validValues);
            }
        }
    }

    public void limitParams(){
        if(brightnessMultiplier <-16){
            brightnessMultiplier =-16;}
        if(brightnessMultiplier >16){
            brightnessMultiplier =16;}

        if(samplesPerFrame <2){
            samplesPerFrame =2;}
        if(samplesPerFrame >1310720){
            samplesPerFrame =1310720;}

        if(potentialRadius>512){
            potentialRadius=512;
        }
        if(potentialRadius<0){
            potentialRadius=0;
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

            startDragDist = shape.distance(startDragX - selectedPt.x, startDragY - selectedPt.y, 0);
            startDragAngleYaw = selectedPt.rotationYaw + Math.atan2(startDragX - selectedPt.x, startDragY - selectedPt.y);
            startDragAnglePitch = selectedPt.rotationPitch + Math.atan2(startDragX - selectedPt.x, startDragY - selectedPt.y);

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
        mousex = e.getX();
        mousey = e.getY();
        mousez = 0;
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

            scaleDelta = shape.distance(mousex - selectedPt.x, mousey - selectedPt.y, 0)/startDragDist;

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
        gamefunc();
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

        /*
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
        */

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

      //  if(e.getKeyChar() == 's'){
           // volume.saveToAscii(theVolume.volume);
     //   }

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
