package ifs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ifsys extends Panel
    implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, FocusListener, ActionListener, Serializable
{
    paintThread thePaintThread;
    mainthread[] threads;
    evolutionThread theEvolutionThread;
    int numThreads = 2; //Runtime.getRuntime().availableProcessors()/2;
    boolean quit;

    Image renderImage;
    Graphics renderGraphics;
    long frameNo;
    long fps;
    long framesThisSecond;
    long oneSecondAgo;
    static String startTimeLog = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(Calendar.getInstance().getTime());
    static long lastMoveTime;
    static long lastRenderTime;
    static long lastClearTime;

    long lastPostProcessTime;

    volume theVolume;
    pdf3D thePdf;
    RenderBuffer renderBuffer;
    RenderParams rp;
    ifsShape theShape;
    EvolvingShape eShape;
    ifsMenu theMenu;
    ifsOverlays overlays;

    static int numBuckets = 10_000_000;
    int[] buckets = new int[numBuckets]; //used for "load balancing" across the branches

    static ifsOverlays.DragAxis selectedMovementAxis = ifsOverlays.DragAxis.NONE;

    static boolean shiftDown;
    static boolean ctrlDown;
    static boolean altDown;

    static boolean isLeftPressed=false;
    static boolean isRightPressed=false;

    static boolean mousedown;
    static int mousex, mousey;
    int mouseScroll;
    int rotateMode;

    ifsPt mousePt;
    ifsPt mouseStartDrag;

    boolean started;
    boolean isDragging;

    public ifsys(){
        rp = new RenderParams();
        renderBuffer = new RenderBuffer(rp.screenwidth, rp.screenheight);

        System.out.println(numThreads + " threads");


        threads = new mainthread[numThreads];

        for(int i=0; i< threads.length; i++){
            threads[i] = new mainthread();
        }

        thePaintThread = new paintThread();
        theEvolutionThread = new evolutionThread();

        theShape = new ifsShape();

        theVolume = new volume(1024, 1024, 1024);
        theVolume.clear();
        thePdf = new pdf3D();

        thePdf.thePdfComboMode = pdf3D.comboMode.MIN;

        eShape = new EvolvingShape(theShape);

        this.setSize(this.rp.screenwidth, this.rp.screenheight); // same size as defined in the HTML APPLET
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());//"com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"
        } catch (Exception e) {
            e.printStackTrace();
        }
        final RenderParams rp = new RenderParams();
        ifsys is = new ifsys();

        JDesktopPane desktop = new javax.swing.JDesktopPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(rp.bgColor);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        desktop.add(is);
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        JFrame parentFrame = new JFrame();
        parentFrame.getContentPane().add(desktop, BorderLayout.CENTER);
        parentFrame.setSize(is.rp.screenwidth+400+16, is.rp.screenheight);
        parentFrame.setVisible(true);
        parentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        is.theMenu = new ifsMenu(parentFrame, is);

        is.init();

        setupMiniFrame(is.theMenu.renderProperties, 200, 450,   is.rp.screenwidth,0, "Render", desktop);
        setupMiniFrame(is.theMenu.cameraProperties, 200, 250,   is.rp.screenwidth+200,0, "Camera", desktop);
        setupMiniFrame(is.theMenu.pdfProperties, 200, 200,      is.rp.screenwidth+200,250, "PDF", desktop);
        setupMiniFrame(is.theMenu.pointProperties, 200, 250,    is.rp.screenwidth+200,450, "IFS Point", desktop);
    }

    static void setupMiniFrame(JPanel panel, int width, int height, int x, int y, String title, JDesktopPane desktop){
        boolean resizable = false;
        boolean closeable = false;
        boolean maximizable = false;
        boolean iconifiable = true;

        JInternalFrame theInternalFrame = new JInternalFrame(title, resizable, closeable, maximizable,
                iconifiable);
        desktop.add(theInternalFrame);
        theInternalFrame.setSize(width, height);
        theInternalFrame.setLocation(x, y);
        //theInternalFrame.setFrameIcon(null);
        theInternalFrame.setVisible(true);
        theInternalFrame.getContentPane().add(panel);
        theInternalFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public void init() {
        frameNo=0;

        start();
        theShape.updateCenter();
        clearframe();
        gamefunc();
    }

    public void actionPerformed(ActionEvent e) {

    }

    public class paintThread extends Thread{
        public void run(){
            while(!quit)
                try{
                    if(theVolume.totalSamples>50000){
                        theMenu.updateSideMenu();
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

    public class evolutionThread extends  Thread{
        public void run(){
            while(!quit)
                try{
                    if(eShape.evolving){

                        if(theShape.disqualified){
                            theShape.score = Float.MAX_VALUE*-1;
                        }else{
                            theShape.score = theVolume.getScore(rp.scoreParams);
                        }

                        float oldScore = theShape.score+0;
                        eShape.evolvedSibs++;
                        System.out.println("score " + oldScore + " - highscore " + eShape.getHighestScoreShape().score);

                        theShape = eShape.nextShape(theShape.score);
                        if(eShape.shapeIndex==0){
                            System.out.println("new generation...");
                            saveImg();
                            eShape.offSpring(eShape.getHighestScoreShape());
                        }

                        clearframe();
                    }
                    sleep(eShape.evolvePeriod);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
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
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        renderImage = createImage(rp.screenwidth, rp.screenheight);
        renderGraphics = renderImage.getGraphics();
        overlays = new ifsOverlays(this, renderGraphics);

        clearframe();

        for(int i=0; i< threads.length; i++){
            threads[i].start();
        }

        theShape.setToPreset(0);

        started = true;

        thePaintThread.start();
        theEvolutionThread.start();
    }

    public void update(Graphics gr){
        theVolume.drawGrid(rp, this);
        paint(gr);
    }


    public void saveImg(){

        DecimalFormat df = new DecimalFormat("000000");

        BufferedWriter writer = null;
        try {
            //create a temporary file

            //String timeLog = new SimpleDateFormat("yyyy_MM_dd_HH-mm-ss").format(Calendar.getInstance().getTime()) + ".png";
            File outputdir = new File(startTimeLog);

            if (!outputdir.exists()) {
                outputdir.mkdir();
            }

            String frameNumberStr = df.format(outputdir.list().length) + ".png"; //counting files in dir to allow for sequential use in ffmpeg later, and to chain runs together possibly
            File outputfile = new File(startTimeLog+"/"+frameNumberStr);

            ImageIO.write(toBufferedImage(createImage(new MemoryImageSource(rp.screenwidth, rp.screenheight, renderBuffer.pixels, 0, rp.screenwidth))), "png", outputfile);

            System.out.println("saved - " + outputfile.getAbsolutePath());
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
            theVolume.myVolumeChange = theVolume.myVolume - theVolume.myVolumeOneSecondAgo;
            theVolume.myVolumeOneSecondAgo = theVolume.myVolume;
        }

        generatePixels();

        try{ //TODO why does this err?
            renderGraphics.setColor(rp.bgColor);
            renderGraphics.fillRect(0, 0, rp.screenwidth, rp.screenheight);

            renderGraphics.drawImage(createImage(new MemoryImageSource(rp.screenwidth, rp.screenheight, renderBuffer.pixels, 0, rp.screenwidth)), 0, 0, rp.screenwidth, rp.screenheight, this);

            renderGraphics.setColor(Color.blue);

            if(!rp.guidesHidden){
                overlays.drawDraggyArrows(renderGraphics);
                overlays.drawBox(renderGraphics, theShape.pointNearest);
                overlays.drawBox(renderGraphics, theShape.pointNearest);
            }

            if(!rp.infoHidden && theShape.pointNearest >=0){
                overlays.drawInfoBox(renderGraphics);
            }

            if(lastMoveTime < theMenu.lastPdfPropertiesMouseMoved){
                overlays.drawPDF(renderGraphics);
            }

        }catch (Exception e){
            e.printStackTrace();
        }



        gr.drawImage(renderImage, 0, 0, rp.screenwidth, rp.screenheight, this);
        lastRenderTime = System.currentTimeMillis();
    }

    public void generatePixels(){
        boolean didProcess=false;
        if(!rp.renderThrottling || System.currentTimeMillis()-lastPostProcessTime>rp.postProcessPeriod){
            didProcess=true;
            renderBuffer.generatePixels((float)rp.brightnessMultiplier, rp.useShadows);
        }

        if(didProcess)lastPostProcessTime=System.currentTimeMillis();
    }

    public void clearframe(){
        if(!rp.holdFrame && System.currentTimeMillis() - lastClearTime > 20){
            resetBuckets();
            theVolume.clear();
            lastClearTime=System.currentTimeMillis();
            renderBuffer.clearProjections();
        }
    }

    public void putPdfSample(ifsPt _dpt,
                             double cumulativeRotationYaw,
                             double cumulativeRotationPitch,
                             double cumulativeRotationRoll,
                             double cumulativeScale,
                             ifsPt _thePt, ifsPt theOldPt, ifsPt odpt, int bucketVal, int bucketId, float distance){
        ifsPt dpt = _dpt;
        ifsPt thePt = _thePt;
        float factor = 1.0f;
        if(rp.smearPDF){
            float smearSubdivisions = 4;
            factor = (float)((1.0/smearSubdivisions*((bucketVal+bucketId)%smearSubdivisions))+Math.random()/smearSubdivisions);
            dpt = _dpt.interpolateTo(odpt, factor);
            thePt = _thePt.interpolateTo(theOldPt, factor);
            if(odpt.x<1){dpt=_dpt;}//hack to prevent smearing from first pt
        }

        int duds = 0;

        double uncertainty = rp.potentialRadius;

        double centerX = thePdf.sampleWidth/2;
        double centerY = thePdf.sampleHeight/2;
        double centerZ = thePdf.sampleDepth/2;
        //double exposureAdjust = cumulativeScale*thePt.scale*thePt.radius;

        double sampleX, sampleY, sampleZ;
        double ptColor, scale, pointDegreesYaw, pointDegreesPitch, pointDegreesRoll;
        ifsPt rpt;

        //rotate/scale the point
        //double pointDist = theShape.distance(sampleX, sampleY, 0)*cumulativeScale*thePt.scale*thePt.radius/thePdf.sampleWidth;

        scale = cumulativeScale*thePt.scale*thePt.radius/thePdf.sampleWidth;

        pointDegreesYaw = thePt.rotationYaw +cumulativeRotationYaw;
        pointDegreesPitch = thePt.rotationPitch +cumulativeRotationPitch;//Math.PI/2+thePt.rotationPitch -thePt.degreesPitch+cumulativeRotationPitch;
        pointDegreesRoll = thePt.rotationRoll +cumulativeRotationRoll;//Math.PI/2+thePt.rotationPitch -thePt.degreesPitch+cumulativeRotationPitch;

        int iters;// = (int)(scale*scale/scaleDown)+1;//(int)(Math.min(samplesPerPdfScaler, Math.PI*scale*scale/4/scaleDown)+1);
        //iters=iters&(4095); //limit to 4095
        //if(rp.smearPDF){
            iters=Math.min(1000000, thePdf.edgeValues);
        //}

        double uncertaintyX = uncertainty*Math.random()-uncertainty/2;
        double uncertaintyY = uncertainty*Math.random()-uncertainty/2;
        double uncertaintyZ = uncertainty*Math.random()-uncertainty/2;
        //double distScaleDown = rp.usingGaussian ? 1.0/(uncertaintyX*uncertaintyX+uncertaintyY*uncertaintyY+uncertaintyZ*uncertaintyZ) : 1.0;

        //if(distScaleDown>1){distScaleDown=1;}

        int seqIndex;
        double dx=Math.random()-0.5;
        double dy=Math.random()-0.5;
        double dz=Math.random()-0.5;

        seqIndex = (int)(Math.random()*(thePdf.edgeValues));
        sampleX = thePdf.edgePts[seqIndex].x+dx;
        sampleY = thePdf.edgePts[seqIndex].y+dy;
        sampleZ = thePdf.edgePts[seqIndex].z+dz;

        if(theVolume.renderMode == volume.RenderMode.VOLUMETRIC){
            dx=0;dy=0;dz=0;
        }

        for(int iter=0; iter<iters; iter++){
            //ptColor = thePdf.getVolumePt(sampleX,sampleY,sampleZ);//[(int)sampleX+(int)sampleY+(int)sampleZ];
            //ptColor = ptColor/255.0*cumulativeOpacity/scaleDown*exposureAdjust*exposureAdjust*distScaleDown;
            rpt = new ifsPt((sampleX-centerX)*scale,(sampleY-centerY)*scale,(sampleZ-centerZ)*scale).getRotatedPt(-(float)pointDegreesPitch, -(float)pointDegreesYaw, -(float)pointDegreesRoll); //placed point

            float r=255;
            float g=255;
            float b=255;

            ifsPt theDot = new ifsPt(dpt.x+rpt.x+(float)uncertaintyX,
                    dpt.y+rpt.y+(float)uncertaintyY,
                    dpt.z+rpt.z+(float)uncertaintyZ);

            if(rp.usingColors){
                float thisPointsDistance = distance-rpt.magnitude()*factor;
                r=(theDot.x - theVolume.minX)/(theVolume.maxX-theVolume.minX)*512;
                g=(theDot.y - theVolume.minY)/(theVolume.maxY-theVolume.minY)*512;
                b=(theDot.z - theVolume.minZ)/(theVolume.maxZ-theVolume.minZ)*512;
                theVolume.contributeToAverageDistance(thisPointsDistance);
            }

            if(theVolume.putPixel(theDot,
                                    r,
                                    g,
                                    b, rp, true, this)){ //Z
                theVolume.pushBounds(theDot);
                seqIndex++;
            }else{
                duds++;
                seqIndex = (int)(Math.random()*thePdf.edgeValues);
                sampleX = thePdf.edgePts[seqIndex].x+dx;
                sampleY = thePdf.edgePts[seqIndex].y+dy;
                sampleZ = thePdf.edgePts[seqIndex].z+dz;
            }

            if(duds>4 && theVolume.renderMode != volume.RenderMode.VOLUMETRIC){iter=iters;} //skips occluded pdfs unless in ifs.volume mode
        }
    }


    public void resetBuckets(){
        buckets = new int[numBuckets];
    }

    public int smallestIndexAtThisNode(int node){
        int min=Integer.MAX_VALUE;
        int minIndex=0;
        ArrayList<Integer> winners = new ArrayList<Integer>(); //chooses a random "winner" in the event of a "tie"

        for(int i=node; i<node+ theShape.pointsInUse-1; i++){
            if(buckets[i]<=min){
                if(buckets[i]==min){
                    winners.add(i-node);
                }else{
                    winners.clear();
                }
                min=buckets[i];
                minIndex=i-node;
            }
        }

        if(winners.size()>0){
            return winners.get((int)(Math.random()*winners.size()));
        }else{
            return minIndex;
        }
    }



    public void gamefunc(){
        rp.guidesHidden = System.currentTimeMillis() - lastMoveTime > rp.linesHideTime;

        if(theShape.pointsInUse != 0){

            for(int a = 0; a < rp.samplesPerFrame; a++){
                int randomIndex = 0;
                ifsPt dpt = new ifsPt(theShape.pts[randomIndex]);
                ifsPt rpt;

                float size, yaw, pitch, roll;

                float cumulativeScale = 1.0f;

                float cumulativeRotationYaw = 0;
                float cumulativeRotationPitch = 0;
                float cumulativeRotationRoll = 0;

                int bucketIndex=0;
                int nextBucketIndex=0;

                float distance = 0.0f;

                for(int d = 0; d < rp.iterations; d++){
                    int oldRandomIndex = randomIndex;
                    if(bucketIndex*(theShape.pointsInUse-1)<buckets.length){
                        randomIndex = smallestIndexAtThisNode(bucketIndex*(theShape.pointsInUse-1))+1; //send new data where its needed most...
                    }else{
                        randomIndex = 1 + (int)(Math.random() * (double) (theShape.pointsInUse-1));
                    }

                    nextBucketIndex = bucketIndex*(theShape.pointsInUse-1)+randomIndex-1;
                    if(nextBucketIndex<buckets.length){
                        bucketIndex=nextBucketIndex;
                    }

                    buckets[bucketIndex]++;

                    if(d==0){randomIndex=0;}

                    ifsPt olddpt = new ifsPt();

                    if(d!=0){
                        size = theShape.pts[randomIndex].radius * cumulativeScale;
                        yaw = (float)(Math.PI/2f - theShape.pts[randomIndex].degreesYaw + cumulativeRotationYaw);
                        pitch = (float)(Math.PI/2f - theShape.pts[randomIndex].degreesPitch + cumulativeRotationPitch);
                        roll = (float)(Math.PI/2f - theShape.pts[randomIndex].rotationRoll + cumulativeRotationRoll);

                        rpt = new ifsPt(size,0,0).getRotatedPt(-pitch, -yaw, -roll);

                        olddpt = new ifsPt(dpt);

                        distance += rpt.magnitude();

                        dpt.x += rpt.x;
                        dpt.y += rpt.y;
                        dpt.z -= rpt.z;
                    }

                    if(!theVolume.croppedVolumeContains(dpt, rp)){ //skip points if they leave the cropped area -- TODO make this optional
                        theShape.disqualified = true;
                        break;
                    }else{
                        if(!(rp.smearPDF && d==0)){ //skips first iteration PDF if smearing
                            try{//TODO why the err?
                                putPdfSample(dpt, cumulativeRotationYaw,cumulativeRotationPitch,cumulativeRotationRoll, cumulativeScale, theShape.pts[randomIndex], theShape.pts[oldRandomIndex], olddpt, buckets[bucketIndex], bucketIndex, distance);
                            }catch (Exception e){
                                //e.printStackTrace();
                            }
                        }

                        cumulativeScale *= theShape.pts[randomIndex].scale/ theShape.pts[0].scale;

                        cumulativeRotationYaw += theShape.pts[randomIndex].rotationYaw;
                        cumulativeRotationPitch += theShape.pts[randomIndex].rotationPitch;
                        cumulativeRotationRoll += theShape.pts[randomIndex].rotationRoll;
                    }
                }
            }
        }
    }

    public void mouseClicked(MouseEvent mouseevent){

    }

    public void mousePressed(MouseEvent e){
        isDragging=true;
        if (SwingUtilities.isLeftMouseButton (e))
        {
            mousedown = true;
            isLeftPressed = true;
        }
        else if (SwingUtilities.isRightMouseButton (e))
        {
            mousedown = true;
            isRightPressed = true;
        }

        theVolume.saveCam();
        getMouseXYZ(e);

        theShape.selectedNearestPt();

        if(e.getClickCount()==2){
            theVolume.camCenter = new ifsPt(theShape.selectedPt);
            clearframe();
        }

        mouseStartDrag = new ifsPt(mousex, mousey, 0);
        theShape.saveState();

        if(theShape.pointSelected>-1){
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

        //setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        //mousemode = 0;
        isDragging=false;
    }

    public void mouseEntered(MouseEvent e){
        //setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void mouseExited(MouseEvent e){
        //setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void getMouseXYZ(MouseEvent e){
        mousex = e.getX();
        mousey = e.getY();

        mousePt = new ifsPt(mousex, mousey, 0);
    }

    public void mouseDragged(MouseEvent e){
        if(System.currentTimeMillis()-lastMoveTime>20){
            getMouseXYZ(e);
            float xDelta = (mousePt.x-mouseStartDrag.x);
            float yDelta = (mousePt.y-mouseStartDrag.y);

            boolean xPos = overlays.draggyPtCenter.x<overlays.draggyPtArrow.x;
            boolean yPos = overlays.draggyPtCenter.y<overlays.draggyPtArrow.y;

            if(isRightPressed){ //rotate camera
                theVolume.camPitch=theVolume.savedPitch - (mousePt.x-mouseStartDrag.x)/3.0f;
                theVolume.camRoll=theVolume.savedRoll + (mousePt.y-mouseStartDrag.y)/3.0f;
            }else{
                ifsPt xtra = new ifsPt(0,0,0);

                if(ctrlDown){
                    xtra.x+=xDelta/100.0f;
                    xtra.y+=yDelta/100.0f;
                    theShape.selectedPt.rotationPitch = theShape.selectedPt.savedrotationpitch + xtra.y;
                    theShape.selectedPt.rotationYaw = theShape.selectedPt.savedrotationyaw + xtra.x;
                }else if(altDown){
                    xtra.x+=xDelta/100.0f;
                    xtra.y+=yDelta/100.0f;

                    for(int i=1; i< theShape.pointsInUse; i++){
                        theShape.pts[i].rotationPitch = theShape.pts[i].savedrotationpitch + xtra.y;
                        theShape.pts[i].rotationYaw = theShape.pts[i].savedrotationyaw + xtra.x;
                    }
                }else{
                    switch (selectedMovementAxis){
                        case X:
                            xtra.x+=xDelta/2.0f*(xPos?1:-1);
                            xtra.x+=yDelta/2.0f*(yPos?1:-1);
                            theShape.selectedPt.x = theShape.selectedPt.savedx + xtra.x;
                            break;
                        case Y:
                            xtra.y+=xDelta/2.0f*(xPos?1:-1);
                            xtra.y+=yDelta/2.0f*(yPos?1:-1);
                            theShape.selectedPt.y = theShape.selectedPt.savedy + xtra.y;
                            break;
                        case Z:
                            xtra.z+=xDelta/2.0f*(xPos?1:-1);
                            xtra.z+=yDelta/2.0f*(yPos?1:-1);
                            theShape.selectedPt.z = theShape.selectedPt.savedz + xtra.z;
                            break;
                        default:
                            break;
                    }
                }
            }

            theShape.updateRadiusDegrees();
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
                theShape.selectedPt.scale*=scaleChangeFactor;
            }else{ //scroll up
                theShape.selectedPt.scale/=scaleChangeFactor;
            }
        }else if(altDown){
            if(e.getWheelRotation()>0){ //scroll down
                for(int i=1; i< theShape.pointsInUse; i++){
                    theShape.pts[i].radius*=scaleChangeFactor;
                }
            }else{ //scroll up
                for(int i=1; i< theShape.pointsInUse; i++){
                    theShape.pts[i].radius/=scaleChangeFactor;
                }
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
        getMouseXYZ(e);
        theShape.findNearestPt(mousex, mousey, overlays.minInterestDist, theVolume);
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
        theShape.updateCenter();
        //clearframe();
        gamefunc();
    }

    public void loadStuff(String filename){
        if(filename==""){
            theShape = theShape.loadFromFile("theShape.ser");
        }else{
            theShape = theShape.loadFromFile(filename);
        }
        rp = theShape.rp;
    }

    public void saveStuff(String filename){
        theShape.rp = rp;
        if(filename==""){
            theShape.saveToFile("theShape.ser");
        }else{
            theShape.saveToFile(filename);
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
        //    saveStuff("");
            saveImg();
        }

        if(e.getKeyChar() == 'l'){
            loadStuff("");
        }

      //  if(e.getKeyChar() == 's'){
           // ifs.volume.saveToAscii(theVolume.ifs.volume);
     //   }

        if(e.getKeyChar() == '0'){
            theShape.setToPreset(0);
            theVolume.clear();
            rp.iterations=8;
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == '9'){
            theShape.setToPreset(9);
            theVolume.clear();
            rp.iterations=8;
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == 'n'){
            System.out.println("adding pt!");
            theShape.addPoint(512, 512, 512);
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == 'm'){
            System.out.println("deleting pt " + theShape.pointSelected);
            theShape.deletePoint(theShape.pointSelected);
            clearframe();
            gamefunc();
        }

       // if(e.getKeyChar() == 'z'){
       //     theVolume.useZBuffer = !theVolume.useZBuffer;
       //     clearframe();
       //     gamefunc();
       // }

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

        switch(e.getKeyChar()){
            case 'i':
                rp.infoHidden = !rp.infoHidden;
                clearframe();
                gamefunc();
                break;
            case '\\':
                rp.usingColors = !rp.usingColors;
                clearframe();
                gamefunc();
                break;
            case 'h':
                rp.useShadows = !rp.useShadows;
                break;
        }

        if(e.getKeyChar() == 'w'){
            eShape.parents(theShape);
        }

        if(e.getKeyChar() == 'e'){
            eShape.offSpring(theShape);
        }

        if(e.getKeyChar() == 'z'){
            theShape=eShape.nextShape(0);
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == 'x'){
            theShape=eShape.prevShape(0);
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == 'r'){
            rp.savingDots=!rp.savingDots;
            //rp.savedDots=0;
            if(!rp.savingDots){
                theVolume._saveToBinarySTL();
            }
            theVolume.renderMode = rp.savingDots ? volume.RenderMode.VOLUMETRIC : volume.RenderMode.PROJECT_ONLY;
            System.out.println("render mode: " + theVolume.renderMode);
        }

        if(e.getKeyChar() == 't'){
            rp.savingDots=!rp.savingDots;
            rp.savedDots=0;

            theVolume.renderMode = rp.savingDots ? volume.RenderMode.VOLUMETRIC : volume.RenderMode.PROJECT_ONLY;
            System.out.println("render mode: " + theVolume.renderMode);
        }

        if(e.getKeyChar() == 'q'){
            //rp.zMin = 512;rp.zMax=1024;
            //rp.drawGrid=false;
            //theShape.setToPreset(0);
            theShape.setToPreset(9);
            theVolume.clear();
            rp.iterations=8;
            rp.useShadows=true;
            rp.brightnessMultiplier=1;
            rp.smearPDF=true;
            rp.renderThrottling=true;
            rp.postProcessPeriod=1000;
            rp.savingDots=true;
            rp.savedDots=0;
            theVolume.renderMode = volume.RenderMode.VOLUMETRIC;
            eShape.offSpring(theShape);
            eShape.evolving=!eShape.evolving;
            System.out.println("evolving: " + eShape.evolving);
        }
    }

    public void focusGained(FocusEvent focusevent){}
    public void focusLost(FocusEvent focusevent){}
}
