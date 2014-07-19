import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ifsys extends Panel
    implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, FocusListener, ActionListener, Serializable
{
    paintThread thePaintThread;
    mainthread[] threads;
    int numThreads = 2; //Runtime.getRuntime().availableProcessors()/2;
    boolean quit;

    int pixels[];
    Image render;
    Graphics rg;
    long frameNo;
    long fps;
    long framesThisSecond;
    long oneSecondAgo;
    static long lastMoveTime;
    static long lastRenderTime;
    static long lastClearTime;

    long lastPostProcessTime;

    volume theVolume;
    pdf3D thePdf;

    //user params

        int pointNearest, pointSelected;
        ifsPt selectedPt;

        static ifsOverlays.DragAxis selectedMovementAxis = ifsOverlays.DragAxis.NONE;

        static boolean shiftDown;
        static boolean ctrlDown;
        static boolean altDown;

        static boolean isLeftPressed=false;
        static boolean isRightPressed=false;

        static int mousex, mousey;
        int mouseScroll;
        int rotateMode;

        RenderParams rp;

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

        threads = new mainthread[numThreads];

        for(int i=0; i< threads.length; i++){
            threads[i] = new mainthread();
        }

        thePaintThread = new paintThread();

        pixels = new int[rp.screenwidth * rp.screenheight];

        maxPoints = 100;
        shape = new ifsShape(maxPoints);
        pointNearest =-1;
        pointSelected =-1;

        theVolume = new volume(rp.screenwidth, rp.screenheight, 1024);
        theVolume.clear();
        thePdf = new pdf3D();

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

    public class paintThread extends Thread{
        public void run(){
            while(!quit)
                try{
                    theMenu.updateSideMenu();
                    if(theVolume.totalSamples>50000){
                        repaint();
                    }
                    sleep(1L);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
        }

        public paintThread(){
        }
    }

    public class mainthread extends Thread{
        public void run(){
            while(!quit) 
                try{
                    gamefunc();
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

        clearframe();

        for(int i=0; i< threads.length; i++){
            threads[i].start();
        }

        shape.setToPreset(0);

        started = true;

        thePaintThread.start();
    }

    public void update(Graphics gr){
        drawGrid();
        paint(gr);
    }


    public void saveImg(){
        BufferedWriter writer = null;
        try {
            //create a temporary file
            String timeLog = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(Calendar.getInstance().getTime()) + ".png";
            File outputfile = new File(timeLog);
            System.out.println("saved - " + outputfile.getAbsolutePath());
            ImageIO.write(toBufferedImage(createImage(new MemoryImageSource(rp.screenwidth, rp.screenheight, pixels, 0, rp.screenwidth))), "png", outputfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public void paint(Graphics gr){
        frameNo++;
        framesThisSecond++;
        if(System.currentTimeMillis()- oneSecondAgo >=1000){
            oneSecondAgo = System.currentTimeMillis();
            fps= framesThisSecond;
            framesThisSecond =0;
        }

        generatePixels();
        try{ //TODO why does this err?
            rg.setColor(Color.green);
            rg.fillRect(0,0,1024,1024);

            rg.drawImage(createImage(new MemoryImageSource(rp.screenwidth, rp.screenheight, pixels, 0, rp.screenwidth)), 0, 0, rp.screenwidth, rp.screenheight, this);

            rg.setColor(Color.blue);

            if(!rp.guidesHidden){
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
        lastRenderTime = System.currentTimeMillis();
    }

    public void generatePixels(){
        double scaler = 1;//255/theVolume.dataMax * brightnessMultiplier;
        double area = 0;
        int scaledColor = 0;

        float[][][] projections = theVolume.getScaledProjections(Math.pow(2, rp.brightnessMultiplier));
        float[][] zProjection = projections[3];
        float[][] rProjection = projections[0];
        float[][] gProjection = projections[1];
        float[][] bProjection = projections[2];

        boolean didProcess=false;

        if(!rp.renderThrottling || System.currentTimeMillis()-lastPostProcessTime>rp.postProcessPeriod){
            didProcess=true;

            int argb;

            for(int x = 0; x < zProjection.length; x++){
                for(int y=0; y<zProjection[x].length; y++){

                    if(zProjection[x][y]==0){ //"half darkened spanish blue" for background
                        argb = 255;
                        argb = (argb << 8) + 0;
                        argb = (argb << 8) + 112/2;
                        argb = (argb << 8) + 184/2;
                    }else{
                        argb = 255;
                        argb = (argb << 8) + (int)rProjection[x][y];
                        argb = (argb << 8) + (int)gProjection[x][y];
                        argb = (argb << 8) + (int)bProjection[x][y];
                    }

                    pixels[x+y*zProjection.length] = argb;
                    area+=scaler*zProjection[x][y];
                }
            }
        }

        if(didProcess)lastPostProcessTime=System.currentTimeMillis();
    }

    public void clearframe(){
        if(!rp.holdFrame && System.currentTimeMillis() - lastClearTime > 20){
            theVolume.clear();
            lastClearTime=System.currentTimeMillis();
        }
    }

    public void putPdfSample(ifsPt _dpt, double cumulativeRotationYaw, double cumulativeRotationPitch, double cumulativeScale, double cumulativeOpacity, ifsPt _thePt, ifsPt theOldPt, double scaleDown, int index, ifsPt odpt){
        ifsPt dpt = _dpt;
        ifsPt thePt = _thePt;

        if(rp.smearPDF){
            float factor = (float)Math.random();
            dpt = _dpt.interpolateTo(odpt, factor);
            thePt = _thePt.interpolateTo(theOldPt, factor);
            if(odpt.x<1){dpt=_dpt;}//hack to prevent smearing from first pt
        }

        int duds = 0;

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

        int iters = (int)(scale*scale/scaleDown)+1;//(int)(Math.min(samplesPerPdfScaler, Math.PI*scale*scale/4/scaleDown)+1);
        iters=iters&(4095); //limit to 4095

        double uncertaintyX = uncertainty*Math.random()-uncertainty/2;
        double uncertaintyY = uncertainty*Math.random()-uncertainty/2;
        double uncertaintyZ = uncertainty*Math.random()-uncertainty/2;
        double distScaleDown = rp.usingGaussian ? 1.0/(uncertaintyX*uncertaintyX+uncertaintyY*uncertaintyY+uncertaintyZ*uncertaintyZ) : 1.0;

        if(distScaleDown>1){distScaleDown=1;}

        int seqIndex;
        double dx=Math.random()-0.5;
        double dy=Math.random()-0.5;
        double dz=Math.random()-0.5;

        //if(uncertainty==0){
        //    dx=0;dy=0;dz=0;
        //}

        if(!rp.usePDFSamples){
            dx=0;dy=0;dz=0;
        }

        seqIndex = (int)(Math.random()*thePdf.validValues);

        sampleX = thePdf.validPts[seqIndex].x+dx;
        sampleY = thePdf.validPts[seqIndex].y+dy;
        sampleZ = thePdf.validPts[seqIndex].z+dz;

/*
        ifsPt nxArrow = theVolume.getCameraDistortedPt(thePt.add(ifsPt.X_UNIT.scale(thePt.radius * thePt.scale)));
        ifsPt nyArrow = theVolume.getCameraDistortedPt(thePt.add(ifsPt.Y_UNIT.scale(thePt.radius * thePt.scale)));
        ifsPt nzArrow = theVolume.getCameraDistortedPt(thePt.add(ifsPt.Z_UNIT.scale(thePt.radius * thePt.scale)));
        ifsPt pxArrow = theVolume.getCameraDistortedPt(thePt.subtract(ifsPt.X_UNIT.scale(thePt.radius * thePt.scale)));
        ifsPt pyArrow = theVolume.getCameraDistortedPt(thePt.subtract(ifsPt.Y_UNIT.scale(thePt.radius * thePt.scale)));
        ifsPt pzArrow = theVolume.getCameraDistortedPt(thePt.subtract(ifsPt.Z_UNIT.scale(thePt.radius * thePt.scale)));

        float minZ = 10000;
        int minDim = 0;
        float[] zs = {pxArrow.z,pyArrow.z,pzArrow.z,nxArrow.z,nyArrow.z,nzArrow.z};

        for(int i=0; i<zs.length; i++){
            if(zs[i]<minZ){
                minDim=i;
                minZ=zs[i];
            }
        }

        switch (minDim){
            case 0:
                //System.out.println("+X");
                break;
            case 1:
                //System.out.println("+Y");
                break;
            case 2:
                //System.out.println("+Z");
                break;
            case 3:
                //System.out.println("-X");
                break;
            case 4:
                //System.out.println("-Y");
                break;
            case 5:
                //System.out.println("-Z");
                break;
        }*/

        for(int iter=0; iter<iters; iter++){

            ptColor = thePdf.getVolumePt(sampleX,sampleY,sampleZ);//[(int)sampleX+(int)sampleY+(int)sampleZ];
            ptColor = ptColor/255.0*cumulativeOpacity/scaleDown*exposureAdjust*exposureAdjust*distScaleDown;
            rpt = new ifsPt((sampleX-centerX)*scale,(sampleY-centerY)*scale,(sampleZ-centerZ)*scale).getRotatedPt(-pointDegreesPitch, -pointDegreesYaw); //placed point

            //put pixel


            //if(index==pointSelected){colorDark=4;}

            float r=255;
            float g=255;
            float b=255;

            if(rp.usingColors){
                r=dpt.x;
                g=dpt.y;
                b=dpt.z;
            }

            ifsPt theDot = new ifsPt(dpt.x+rpt.x+(float)uncertaintyX,
                    dpt.y+rpt.y+(float)uncertaintyY,
                    dpt.z+rpt.z+(float)uncertaintyZ);

            if(theVolume.putPixel(theDot,
                                    r,
                                    g,
                                    b,
                                    (float)ptColor, rp.dotSize)){ //Z
                seqIndex++;
            }else{
                duds++;
                seqIndex = (int)(Math.random()*thePdf.validValues);
                sampleX = thePdf.validPts[seqIndex].x+dx;
                sampleY = thePdf.validPts[seqIndex].y+dy;
                sampleZ = thePdf.validPts[seqIndex].z+dz;
            }

            /*if(rp.savingDots){
                rp.savedDots++;
                if(theDot.x>10)
                    rp.savedString+=theDot.coordString()+"\n";
                if(rp.savedDots%rp.saveInterval==0){
                    rp.savedDots++;
                    theVolume.saveToAscii(rp.savedString);
                    rp.savedString="";
                    System.out.println(rp.savedDots + " dots saved...");
                }
            }else{*/
                if(duds>4){iter=iters;} //skips occluded pdfs
            //}

        }
    }

    public void gamefunc(){
        rp.guidesHidden = System.currentTimeMillis() - lastMoveTime > rp.linesHideTime;

        if(shape.pointsInUse != 0){

            for(int a = 0; a < rp.samplesPerFrame; a++){
                int randomIndex = 0;
                ifsPt dpt = new ifsPt(shape.pts[randomIndex]);
                ifsPt rpt;

                double size, yaw, pitch;//, roll;

                double cumulativeScale = 1.0;
                double cumulativeOpacity = 1;

                double cumulativeRotationYaw = 0;
                double cumulativeRotationPitch = 0;
                //double cumulativeRotationRoll = 0;

                double scaleDownMultiplier = Math.pow(shape.pointsInUse,rp.iterations); //this variable is used to tone down repeated pixels so leaves and branches are equally exposed

                for(int d = 0; d < rp.iterations; d++){


                    int oldRandomIndex = randomIndex;
                    randomIndex = 1 + (int)(Math.random() * (double) (shape.pointsInUse-1));

                    if(d==0){randomIndex=0;}

                    ifsPt olddpt = new ifsPt();

                    if(d!=0){
                        size = shape.pts[randomIndex].radius * cumulativeScale;
                        yaw = Math.PI/2D - shape.pts[randomIndex].degreesYaw + cumulativeRotationYaw;
                        pitch = Math.PI/2D - shape.pts[randomIndex].degreesPitch + cumulativeRotationPitch;

                        rpt = new ifsPt(size,0,0).getRotatedPt(-pitch, -yaw);

                        olddpt = new ifsPt(dpt);

                        dpt.x += rpt.x;
                        dpt.y += rpt.y;
                        dpt.z -= rpt.z;
                    }

                    if(!(rp.smearPDF && d==0)){ //skips first iteration PDF if smearing
                        try{//TODO why the err?
                            putPdfSample(dpt, cumulativeRotationYaw,cumulativeRotationPitch, cumulativeScale, cumulativeOpacity, shape.pts[randomIndex], shape.pts[oldRandomIndex], scaleDownMultiplier, randomIndex, olddpt);
                        }catch (Exception e){

                        }

                    }
                    scaleDownMultiplier/=shape.pointsInUse;

                    cumulativeScale *= shape.pts[randomIndex].scale/shape.pts[0].scale;
                    cumulativeOpacity *= shape.pts[randomIndex].opacity;

                    cumulativeRotationYaw += shape.pts[randomIndex].rotationYaw;
                    cumulativeRotationPitch += shape.pts[randomIndex].rotationPitch;
                }
            }
        }
    }

    public void drawGrid(){
        if(rp.drawGrid && System.currentTimeMillis() -  rp.gridDrawTime > rp.gridRedrawTime){
            double xmax = 1024;
            double ymax = 1024;
            double gridspace = 32;
            rp.gridDrawTime = System.currentTimeMillis();
            int z = 0;
            for(int x=0; x<xmax/gridspace; x++){
                for(int y=0; y<ymax; y+=4){
                    theVolume.putPixel(new ifsPt(
                            x*gridspace,
                            y,
                            z), 1.00, 64, 64, 64);
                    theVolume.putPixel(new ifsPt(
                            y,
                            x*gridspace,
                            z), 1.00, 64, 64, 64);
                }
            }
        }
     }

    public void mouseClicked(MouseEvent mouseevent){

    }

    public void mousePressed(MouseEvent e){
        if (SwingUtilities.isLeftMouseButton (e))
        {
            isLeftPressed = true;
        }
        else if (SwingUtilities.isRightMouseButton (e))
        {
            isRightPressed = true;
        }

        mousemode = e.getButton();
        theVolume.saveCam();
        getMouseXYZ(e);

        selectedNearestPt();

        if(e.getClickCount()==2){
            theVolume.camCenter = new ifsPt(selectedPt);
            clearframe();
        }

        mouseStartDrag = new ifsPt(mousex, mousey, 0);
        shape.saveState();

        if(pointSelected>-1){
            overlays.updateDraggyArrows();
        }
    }

    public void mouseReleased(MouseEvent e){
        if (SwingUtilities.isLeftMouseButton (e))
        {
            isLeftPressed = false;
        }
        else if (SwingUtilities.isRightMouseButton (e))
        {
            isRightPressed = false;
        }

        setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        //mousemode = 0;
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
        if(System.currentTimeMillis()-lastMoveTime>20){
            getMouseXYZ(e);
            if(ctrlDown){
                selectedMovementAxis=ifsOverlays.DragAxis.NONE;
                if(isLeftPressed && isRightPressed){
                    selectedMovementAxis=ifsOverlays.DragAxis.Z;
                    selectedPt.z = selectedPt.savedz - (mousePt.y-mouseStartDrag.y)/2.0f;
                }else if(isLeftPressed){
                    selectedMovementAxis=ifsOverlays.DragAxis.Y;
                    selectedPt.y = selectedPt.savedy + (mousePt.y-mouseStartDrag.y)/2.0f;
                }else if(isRightPressed){
                    selectedMovementAxis=ifsOverlays.DragAxis.X;
                    selectedPt.x = selectedPt.savedx + (mousePt.x-mouseStartDrag.x)/2.0f;
                }
            }else{
                selectedMovementAxis=ifsOverlays.DragAxis.NONE;

                if(isLeftPressed && isRightPressed){
                    theVolume.camCenter.z=theVolume.camCenter.savedz - ifsPt.Z_UNIT.getRotatedPt(theVolume.camPitch,theVolume.camYaw,theVolume.camRoll).scale((mousePt.y-mouseStartDrag.y)/2.0f).z;
                }else if(isLeftPressed){
                    theVolume.camCenter.x=theVolume.camCenter.savedx - ifsPt.X_UNIT.getRotatedPt(theVolume.camPitch,theVolume.camYaw,theVolume.camRoll).scale((mousePt.x-mouseStartDrag.x)/2.0f).x;
                    theVolume.camCenter.y=theVolume.camCenter.savedy - ifsPt.Y_UNIT.getRotatedPt(theVolume.camPitch,theVolume.camYaw,theVolume.camRoll).scale((mousePt.y-mouseStartDrag.y)/2.0f).y;

                }else if(isRightPressed){ //rotate camera
                    theVolume.camPitch=theVolume.savedPitch - (mousePt.x-mouseStartDrag.x)/3.0f;
                    theVolume.camRoll=theVolume.savedRoll + (mousePt.y-mouseStartDrag.y)/3.0f;
                }
            }

            theMenu.camPitchSpinner.setValue(theMenu.camPitchSpinner.getValue());
            clearframe();
            lastMoveTime = System.currentTimeMillis();
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {

        lastMoveTime = System.currentTimeMillis();

        mouseScroll += e.getWheelRotation();
        double scaleChangeFactor = 0.98;
        double camChangeFactor = 0.9;

        if(ctrlDown){
            if(e.getWheelRotation()>0){ //scroll down
                selectedPt.scale*=scaleChangeFactor;
            }else{ //scroll up
                selectedPt.scale/=scaleChangeFactor;
            }
        }else{
            if(e.getWheelRotation()>0){ //scroll down
                theVolume.camScale*=camChangeFactor;
            }else{ //scroll up
                theVolume.camScale/=camChangeFactor;
            }

            theVolume.camScale =(float) Math.max(0.1, theVolume.camScale);

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

    public void loadStuff(String filename){
        if(filename==""){
            shape = shape.loadFromFile("shape.ser");
        }else{
            shape = shape.loadFromFile(filename);
        }
        rp = shape.rp;
    }

    public void saveStuff(String filename){
        shape.rp = rp;
        if(filename==""){
            shape.saveToFile("shape.ser");
        }else{
            shape.saveToFile(filename);
        }
    }

    public void keyReleased(KeyEvent e){
        if(e.getKeyCode()==KeyEvent.VK_ALT)
            altDown=false;
        if(e.getKeyCode()==KeyEvent.VK_CONTROL)
            ctrlDown=false;
        if(e.getKeyCode()==KeyEvent.VK_SHIFT)
            shiftDown=false;

        if(e.getKeyChar() == 'p'){
            rp.usePDFSamples = !rp.usePDFSamples;
            clearframe();
        }

        if(e.getKeyChar() == 'g'){
            rp.drawGrid = !rp.drawGrid;
            clearframe();
        }

        if(e.getKeyChar() == 's'){
            saveStuff("");
        }

        if(e.getKeyChar() == 'l'){
            loadStuff("");
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


        if(e.getKeyChar() == '9'){
            clearframe();
            shape.setToPreset(9);
        }
        if(e.getKeyChar() == '8'){
            clearframe();
            shape.setToPreset(8);
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

        if(e.getKeyChar() == 'n'){
            System.out.println("adding pt!");
            shape.addPoint(512, 512, 512);
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == 'm'){
            System.out.println("deleting pt " + pointSelected);
            shape.deletePoint(pointSelected);
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == 'z'){
            theVolume.useZBuffer = !theVolume.useZBuffer;
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == '-'){
            theVolume.zDarkenScaler/=0.9;
            System.out.println(theVolume.zDarkenScaler);
            clearframe();
            gamefunc();
        }
        if(e.getKeyChar() == '='){
            theVolume.zDarkenScaler*=0.9;
            System.out.println(theVolume.zDarkenScaler);
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == '\\'){
            rp.usingColors = !rp.usingColors;
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == 'i'){
            rp.infoHidden = !rp.infoHidden;
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == 's'){
            saveImg();
        }

        if(e.getKeyChar() == 'r'){
            rp.savingDots=!rp.savingDots;
            //rp.savedDots=0;
            if(!rp.savingDots){
                theVolume._saveToAscii();
            }
            theVolume.renderMode = rp.savingDots ? volume.RenderMode.VOLUMETRIC : volume.RenderMode.PROJECT_ONLY;
            System.out.println("render mode: " + theVolume.renderMode);
        }
    }

    public void focusGained(FocusEvent focusevent){}
    public void focusLost(FocusEvent focusevent){}
}
