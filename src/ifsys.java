import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;

public class ifsys extends Panel
    implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, FocusListener, ActionListener
{
    mainthread[] threads;
    int numThreads = Runtime.getRuntime().availableProcessors()/2;
    boolean quit;

    int pixels[];
    Image render;
    Graphics rg;
    static long tNo=0;
    long frameNo;
    long fps;
    long framesThisSecond;
    long oneSecondAgo;
    long lastMoveTime;


    long lastPostProcessTime;

    double lastProcessedProjection[][];

    volume theVolume;
    pdf3D thePdf;

    //user params

        int pointNearest, pointSelected;
        ifsPt selectedPt;

        boolean shiftDown;
        boolean ctrlDown;
        boolean altDown;
        int mousex, mousey;
        int mouseScroll;
        int rotateMode;

        RenderParams rp;


    double samplesPerPdfScaler;

    ifsShape shape;

    ifsMenu theMenu;

    int maxPoints;

    //drag vars
        int mousemode; //current mouse button

    ifsPt mousePt;
    ifsPt mouseStartDrag;

    boolean started;
    boolean isDragging;

    ifsOverlays overlays;


    public ifsys(){
        System.out.println(numThreads + " threads");

        rp = new RenderParams();

        started=false;
        oneSecondAgo =0;
        framesThisSecond = 0;
        altDown=false;
        ctrlDown=false;
        shiftDown=false;

        threads = new mainthread[numThreads];

        for(int i=0; i< threads.length; i++){
            threads[i] = new mainthread();
        }

        quit = false;

        pixels = new int[rp.screenwidth * rp.screenheight];

        mousemode = 0;

        maxPoints = 100;
        shape = new ifsShape(maxPoints);
        mouseScroll = 0;
        pointNearest =-1;
        pointSelected =-1;
        isDragging = false;

        theVolume = new volume(rp.screenwidth, rp.screenheight, 1024);
        theVolume.clear();
        thePdf = new pdf3D();

        rotateMode=0;
        lastMoveTime=0;



        samplesPerPdfScaler = 0.25; //decrease for higher fps while drawing PDFs


        thePdf.thePdfComboMode = pdf3D.comboMode.MIN;
    }

    public static void main(String[] args) {
        ifsys is = new ifsys();
        is.setSize(is.rp.screenwidth, is.rp.screenheight); // same size as defined in the HTML APPLET
        JFrame frame = new JFrame("");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel sideMenu = new JPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, is, sideMenu);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(is.rp.screenwidth);
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        frame.setSize(is.rp.screenwidth+200, is.rp.screenheight);
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

    public void findNearestPt(double minDist){
        for(int i=0; i<shape.pointsInUse; i++){

            ifsPt _pt = theVolume.getCameraDistortedPt(shape.pts[i]);
            double dist = _pt.distanceXY(new ifsPt(mousex, mousey, 0));

            if(dist<minDist){
                pointNearest=i;
                minDist=dist;
            }
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
                    tNo++;
                    if(tNo%numThreads==0){
                        repaint();
                    }
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
        render = createImage(rp.screenwidth, rp.screenheight);
        rg = render.getGraphics();

        overlays = new ifsOverlays(this, rg);

        //genRandomNums();

        clearframe();

        for(int i=0; i< threads.length; i++){
            threads[i].start();
        }

        shape.setToPreset(0);

        started = true;
    }

    public long randomLong() {
        long rl = System.nanoTime();
        rl ^= (rl << 21);
        rl ^= (rl >>> 35);
        rl ^= (rl << 4);
        return rl;
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

            rg.drawImage(createImage(new MemoryImageSource(rp.screenwidth, rp.screenheight, pixels, 0, rp.screenwidth)), 0, 0, rp.screenwidth, rp.screenheight, this);

            //rg.drawImage(thePdf.sampleImage, getWidth() - 50, 0, 50, 50, this);
            rg.setColor(Color.blue);

            if(!rp.guidesHidden){
                //overlays.drawArcs(rg);
                //overlays.drawSpecialPoints(rg);
                overlays.drawDraggyArrows(rg);
                overlays.drawBox(rg, pointSelected);
                overlays.drawBox(rg, pointNearest);
            }

            if(!rp.infoHidden && pointNearest >=0){
                overlays.drawInfoBox(rg);
            }

            if(lastMoveTime < theMenu.lastPdfPropertiesMouseMoved){
                overlays.drawPDF(rg);
            }

        }catch (Exception e){
            e.printStackTrace();
        }



        gr.drawImage(render, 0, 0, rp.screenwidth, rp.screenheight, this);
    }

    public void generatePixels(){
        double scaler = 1;//255/theVolume.dataMax * brightnessMultiplier;
        double area = 0;
        int scaledColor = 0;
        int scaledColor2=0;
        //double[][] projection2 = theVolume.getScaledProjection(Math.pow(2,brightnessMultiplier));
        double[][] projection1 = theVolume.getScaledDepthProjection(Math.pow(2, rp.brightnessMultiplier));
        boolean didProcess=false;

        if(!rp.renderThrottling || System.currentTimeMillis()-lastPostProcessTime>rp.postProcessPeriod){
            didProcess=true;
            if(rp.usingThreshold){
                projection1 = theVolume.getThreshold2D(projection1, rp.threshold);
            }
            if(rp.usingFindEdges){
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
        if(!rp.holdFrame){
            theVolume.clear();
        }
    }

    public void putPdfSample(ifsPt dpt, double cumulativeRotationYaw, double cumulativeRotationPitch, double cumulativeScale, double cumulativeOpacity, ifsPt thePt, double scaleDown, int index){

        double uncertainty = rp.potentialRadius;

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

        int iters = (int)(samplesPerPdfScaler * scale*scale*scale/scaleDown)+1;//(int)(Math.min(samplesPerPdfScaler, Math.PI*scale*scale/4/scaleDown)+1);

        iters=iters&(4095); //limit to 4095

        double uncertaintyX = uncertainty*Math.random()-uncertainty/2;
        double uncertaintyY = uncertainty*Math.random()-uncertainty/2;
        double uncertaintyZ = uncertainty*Math.random()-uncertainty/2;
        double distScaleDown = rp.usingGaussian ? 1.0/(uncertaintyX*uncertaintyX+uncertaintyY*uncertaintyY+uncertaintyZ*uncertaintyZ) : 1.0;

        if(distScaleDown>1){distScaleDown=1;}

        int rndIndex;
        double dx=Math.random()-0.5;
        double dy=Math.random()-0.5;
        double dz=Math.random()-0.5;

        rndIndex = (int)(Math.random()*thePdf.validValues);

        sampleX = thePdf.validX[rndIndex]+dx;
        sampleY = thePdf.validY[rndIndex]+dy;
        sampleZ = thePdf.validZ[rndIndex]+dz;

        for(int iter=0; iter<iters; iter++){
            if(iter%256==0){
                rndIndex = (int)(Math.random()*thePdf.validValues);
            }

            ptColor = thePdf.volume[(int)sampleX][(int)sampleY][(int)sampleZ];

            ptColor = ptColor/255.0*cumulativeOpacity/scaleDown*exposureAdjust*exposureAdjust*distScaleDown;
            rpt = new ifsPt((sampleX-centerX)*scale,(sampleY-centerY)*scale,(sampleZ-centerZ)*scale).getRotatedPt(-pointDegreesPitch, -pointDegreesYaw); //placed point

            //put pixel

            if(theVolume.putPixel(new ifsPt(dpt.x+rpt.x+uncertaintyX,
                                         dpt.y+rpt.y+uncertaintyY,
                                         dpt.z+rpt.z+uncertaintyZ),ptColor)){
                rndIndex+=Math.random()*8+1;

            }else{
                rndIndex+=thePdf.validValues/thePdf.sampleHeight/2;
            }
            rndIndex=rndIndex%(thePdf.validValues | 1);
        }
    }



    public void gamefunc(){
        theMenu.updateSideMenu();
        rp.guidesHidden = System.currentTimeMillis() - lastMoveTime > overlays.hideTime;

        if(shape.pointsInUse != 0){

            for(int a = 0; a < rp.samplesPerFrame*samplesPerPdfScaler; a++){
                int randomIndex = 0;
                ifsPt dpt = new ifsPt(shape.pts[randomIndex]);
                ifsPt rpt;

                double size, yaw, pitch;//, roll;

                double cumulativeScale = 1.0;
                double cumulativeOpacity = 1;

                double cumulativeRotationYaw = 0;
                double cumulativeRotationPitch = 0;
                //double cumulativeRotationRoll = 0;

                double scaleDownMultiplier = Math.pow(shape.pointsInUse,rp.iterations-1); //this variable is used to tone down repeated pixels so leaves and branches are equally exposed

                for(int d = 0; d < rp.iterations; d++){
                    scaleDownMultiplier/=shape.pointsInUse;

                    randomIndex = 1 + (int)(Math.random() * (double) (shape.pointsInUse-1));

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

                    if(rp.usePDFSamples)
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
        mouseStartDrag = new ifsPt(mousex, mousey, 0);
        shape.saveState();

        if(e.getClickCount()==2){
            if(mousemode == 1){ //add point w/ double click
                shape.addPoint(mousex, mousey, 0);
                clearframe();
                gamefunc();
            }else if(mousemode == 3){ //remove point w/ double right click
                shape.deletePoint(pointNearest);
                clearframe();
                gamefunc();
            }
        }else{
            requestFocus();
        }

        if(pointSelected>-1){
            overlays.updateDraggyArrows();
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

        mousePt = new ifsPt(mousex, mousey, 0);
    }

    public void mouseDragged(MouseEvent e){
        getMouseXYZ(e);
        overlays.updateDraggyArrows();
        double dragRatio = 0;

        try{
            dragRatio = mousePt.distTo(overlays.draggyPtCenter.XYOnly()) - overlays.draggyPtCenter.XYOnly().distTo(overlays.draggyPtArrow.XYOnly());

            switch (overlays.selectedAxis){
                case X:
                    selectedPt.x += dragRatio;
                    break;
                case Y:
                    selectedPt.y += dragRatio;
                    break;
                case Z:
                    selectedPt.z += dragRatio;
                    break;
            }

            //other cases for negative motion...

        }catch (Exception ex){
            ex.printStackTrace();
        }

        shape.updateCenter();
        clearframe();

        if(System.currentTimeMillis()-lastMoveTime>100){gamefunc();}
        lastMoveTime = System.currentTimeMillis();
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
        findNearestPt(overlays.minInterestDist);
        getMouseXYZ(e);
        if(System.currentTimeMillis()-lastMoveTime>100){gamefunc();}
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

        if(e.getKeyChar() == 's'){
            shape.saveToFile();
            rp.saveToFile();
        }

        if(e.getKeyChar() == 'l'){
            shape = shape.loadFromFile();
            rp = rp.loadFromFile();
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

      //  if(e.getKeyChar() == 's'){
           // volume.saveToAscii(theVolume.volume);
     //   }

        if(e.getKeyChar() == '0'){
            shape.setToPreset(0);
            theVolume.clear();
            rp.iterations=8;
            rp.brightnessMultiplier=1;
            clearframe();
            gamefunc();
        }


    }

    public void focusGained(FocusEvent focusevent){}
    public void focusLost(FocusEvent focusevent){}
}
