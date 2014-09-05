package ifs;

import ifs.flat.OneDBuffer;
import ifs.flat.RenderBuffer;
import ifs.utils.ImageUtils;
import ifs.volumetric.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.amd.aparapi.Kernel;

public class ifsys extends JPanel
    implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, FocusListener, ActionListener, Serializable
{
    animationThread theAnimationThread;
    paintThread thePaintThread;
    mainthread[] threads;
    evolutionThread theEvolutionThread;
    int numThreads = 2; //Runtime.getRuntime().availableProcessors()/2;
    boolean quit;

    static ImageUtils imageUtils = new ImageUtils();

    Image renderImage;
    Graphics2D renderGraphics;
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
    static int numBucketsAnimated = 1_000_000;
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

    boolean evolutionDescSelected;

    ifsPt mousePt;
    ifsPt mouseStartDrag;

    boolean started;
    boolean isDragging;

    MemoryImageSource renderImageSource;
    Image misImage;

    public ifsys(){
        evolutionDescSelected=false;
        rp = new RenderParams();
        renderBuffer = new RenderBuffer(rp.screenwidth, rp.screenheight);

        System.out.println(numThreads + " threads");

        renderImageSource = new MemoryImageSource(rp.screenwidth, rp.screenheight, renderBuffer.pixels, 0, rp.screenwidth);
        renderImageSource.setAnimated(true);
        renderImageSource.setFullBufferUpdates(true);

        misImage = this.createImage(renderImageSource);

        threads = new mainthread[numThreads];

        for(int i=0; i< threads.length; i++){
            threads[i] = new mainthread();
        }

        thePaintThread = new paintThread();
        theAnimationThread = new animationThread();
        theEvolutionThread = new evolutionThread();

        theShape = new ifsShape();

        theVolume = new volume(1024, 1024, 1024);
        theVolume.clear();
        thePdf = new pdf3D();

        thePdf.thePdfComboMode = pdf3D.comboMode.MIN;

        eShape = new EvolvingShape(theShape, this);
        System.out.println("dubbuff?" + this.isDoubleBuffered());

        this.setDoubleBuffered(true);
        this.setSize(this.rp.screenwidth, this.rp.screenheight); // same size as defined in the HTML APPLET
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel("com.alee.laf.WebLookAndFeel");
            //"com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"
            //"com.alee.laf.WebLookAndFeel"
        } catch (Exception e) {
            e.printStackTrace();
        }
        final RenderParams rp = new RenderParams();
        ifsys is = new ifsys();

        JDesktopPane desktop = new javax.swing.JDesktopPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                //g.setColor(rp.bgColor);
                //g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        desktop.add(is);
        //desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        JFrame parentFrame = new JFrame();
        parentFrame.getContentPane().add(desktop, BorderLayout.CENTER);
        parentFrame.setSize(is.rp.screenwidth+400+16, is.rp.screenheight);
        parentFrame.setVisible(true);
        parentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        is.theMenu = new ifsMenu(parentFrame, is);

        is.init();
        setupMiniFrame(is.theMenu.evolveProperties, 400, 350,   is.rp.screenwidth, 450, "Evolution", "invader.png", desktop);
        setupMiniFrame(is.theMenu.renderProperties, 200, 450,   is.rp.screenwidth+200,0, "Render", "camera.png", desktop);
        setupMiniFrame(is.theMenu.pdfProperties, 200, 200,      is.rp.screenwidth,250, "PDF", "cloud.png", desktop);
        setupMiniFrame(is.theMenu.pointProperties, 200, 250,    is.rp.screenwidth,0, "IFS Point", "anchors.png", desktop);
    }

    static void setupMiniFrame(JPanel panel, int width, int height, int x, int y, String title, String iconName, JDesktopPane desktop){
        boolean resizable = false;
        boolean closeable = false;
        boolean maximizable = false;
        boolean iconifiable = false;

        JInternalFrame theInternalFrame = new JInternalFrame(title, resizable, closeable, maximizable,
                iconifiable);
        desktop.add(theInternalFrame);

        theInternalFrame.setSize(width, height);
        theInternalFrame.setLocation(x, y);
        theInternalFrame.setFrameIcon(new ImageIcon("./instant-ifs/icons/" + iconName));
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

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            //if(theVolume.totalSamples>50000){
                                theMenu.updateSideMenu();
                                repaint();
                            //}
                        }
                    });

                    sleep(41L);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
        }

        public paintThread(){
        }
    }

    public class animationThread extends Thread{
        public void run(){
            while(!quit)
                try{
                    if(rp.shapeVibrating){
                        theShape = theShape.getPerturbedShape(eShape.mutationDescriptorPt, false);
                        rp.odbScale.smooth();
                        rp.odbRotationRoll.smooth();
                        rp.odbX.smooth();
                        rp.odbY.smooth();
                        rp.odbZ.smooth();
                        rp.odbScale.add(new OneDBuffer(), 30); //scale
                        rp.odbRotationRoll.add(new OneDBuffer(), 20); //rotation
                        rp.odbX.add(new OneDBuffer(), 20); //offsetX
                        rp.odbY.add(new OneDBuffer(), 20); //offsetY
                        rp.odbZ.add(new OneDBuffer(), 20); //offsetZ
                        if(theVolume.totalSamples>500000){
                            clearframe();
                            gamefunc();
                        }
                    }

                    sleep(41);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
        }

        public animationThread(){
        }
    }

    public class evolutionThread extends  Thread{
        public void run(){
            while(!quit)
                try{
                    if(eShape.evolving){
                        if(theVolume.myVolumeChange < 5000){
                            if(theVolume.doneClearing){
                                if(theShape.disqualified){
                                    theShape.evolutionDisqualified = true;
                                }else{
                                    theShape.score = theVolume.getScore(rp.scoreParams);
                                }

                                float oldScore = theShape.score+0;
                                eShape.evolvedSibs++;
                                System.out.println("new sib! score " + oldScore + " - highscore " + eShape.getHighestScoreShape().score);

                                theShape = eShape.nextShape(theShape.score);
                                if(eShape.genRollOver){
                                    System.out.println("new generation...");
                                    imageUtils.saveImg(startTimeLog, rp.screenwidth, rp.screenheight, renderBuffer.pixels);
                                    eShape.offSpring(eShape.getHighestScoreShape(), rp.evolveIntensity);
                                }else{
                                    theMenu.updateEvolutionTable();
                                }

                                clearframe();
                                gamefunc();
                                theVolume.clear();
                                theVolume.myVolumeChange=9999999;
                            }
                        }
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
        renderGraphics = (Graphics2D)renderImage.getGraphics();
        overlays = new ifsOverlays(this, renderGraphics);

        clearframe();

        for(int i=0; i< threads.length; i++){
            threads[i].start();
        }

        theShape.setToPreset(0);

        started = true;

        thePaintThread.start();
        theEvolutionThread.start();
        theAnimationThread.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        theVolume.drawGrid(rp, renderBuffer);
        draw(g);
    }

    public void draw(Graphics gr){
        frameNo++;
        framesThisSecond++;
        if(System.currentTimeMillis()- oneSecondAgo >=1000){
            oneSecondAgo = System.currentTimeMillis();
            fps= framesThisSecond;
            framesThisSecond =0;
            theVolume.myVolumeChange = theVolume.myVolume - theVolume.myVolumeOneSecondAgo;
            theVolume.myVolumeOneSecondAgo = theVolume.myVolume;
        }

        if(renderImage != null){
            renderGraphics = (Graphics2D)renderImage.getGraphics();

            generatePixels();

            renderGraphics.setColor(rp.bgColor);
            renderGraphics.fillRect(0, 0, rp.screenwidth, rp.screenheight);

            renderImageSource.newPixels();
            renderGraphics.drawImage(misImage, 0, 0, rp.screenwidth, rp.screenheight, this);
            renderGraphics.setColor(Color.blue);

            if(!rp.guidesHidden){
                overlays.drawDraggyArrows(renderGraphics);
                overlays.drawBox(renderGraphics, theShape.pointNearest);
                overlays.drawBox(renderGraphics, theShape.pointNearest);
            }

            //if(!rp.infoHidden && theShape.pointNearest >=0){
            overlays.drawInfoBox(renderGraphics);
            //}

            gr.drawImage(renderImage, 0, 0, rp.screenwidth, rp.screenheight, this);
            lastRenderTime = System.currentTimeMillis();
        }
    }

    public void generatePixels(){
        boolean didProcess=false;
        if(!rp.renderThrottling || System.currentTimeMillis()-lastPostProcessTime>rp.postProcessPeriod){
            didProcess=true;

            renderBuffer.generatePixels((float)rp.brightnessMultiplier, rp.useShadows, rp.rightEye);
        }

        if(didProcess)lastPostProcessTime=System.currentTimeMillis();
    }

    public void clearframe(){
        long wait = rp.shapeVibrating ? 40 : 10;
        if(!rp.holdFrame && System.currentTimeMillis() - lastClearTime > wait){
            resetBuckets();
            if(theVolume.changed && theVolume.doneClearing)theVolume.clear();
            lastClearTime=System.currentTimeMillis();
            renderBuffer.clearZProjection();
        }
    }

    public void resetBuckets(){
        buckets = new int[rp.shapeVibrating ? numBucketsAnimated : numBuckets];
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

    public void selectEvolutionDescriptorPt(){
        evolutionDescSelected=true;
    }

    public void unselectEvolutionDescriptorPt(){
        evolutionDescSelected=false;
    }

    public void gamefunc(){
        rp.guidesHidden = System.currentTimeMillis() - lastMoveTime > rp.linesHideTime;

        if(theShape.pointsInUse != 0){

            for(int a = 0; a < rp.samplesPerFrame; a++){
                int randomIndex = 0;
                ifsPt dpt = new ifsPt(theShape.pts[randomIndex]);
                ifsPt rpt = new ifsPt();

                float cumulativeScale = 1.0f;

                ifsPt cumulativeRotation = new ifsPt(0,0,0);

                int bucketIndex=0;
                int nextBucketIndex;

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
                    ifsPt thePt = theShape.pts[randomIndex];
                    ifsPt centerPt = theShape.pts[0];
                    if(d!=0){
                        cumulativeRotation = cumulativeRotation.add(new ifsPt(thePt.rotationPitch,thePt.rotationYaw,thePt.rotationRoll));
                        rpt = thePt.subtract(centerPt).scale(cumulativeScale).getRotatedPt(cumulativeRotation);
                        olddpt = new ifsPt(dpt);
                        distance += rpt.magnitude();
                        dpt._add(rpt);
                    }

                    if(!theVolume.croppedVolumeContains(dpt, rp)){ //skip points if they leave the cropped area
                        theShape.disqualified = true;
                        break;
                    }else{
                        if(!(rp.smearPDF && d==0)){ //skips first iteration PDF if smearing
                            try{//TODO why the err?
                                theVolume.putPdfSample(dpt, cumulativeRotation, cumulativeScale, thePt, theShape.pts[oldRandomIndex], olddpt,
                                        buckets[bucketIndex], bucketIndex, distance, rp, thePdf, renderBuffer, rpt.magnitude(), theMenu.colorChooser);
                            }catch (Exception e){
                                //e.printStackTrace();
                            }
                        }
                        cumulativeScale *= thePt.scale/centerPt.scale;
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

        unselectEvolutionDescriptorPt();
        theShape.selectedNearestPt();

        if(e.getClickCount()==2){
            theVolume.camCenter = new ifsPt(theShape.selectedPt);
            clearframe();
        }

        mouseStartDrag = new ifsPt(mousex, mousey, 0);
        theShape.saveState();

        if(theShape.pointSelected>-1){
            overlays.updateDraggyArrows();
        }else{
            theShape.pointSelected=0;
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

            if(isRightPressed){

            }else{
                ifsPt xtra = new ifsPt(0,0,0);

                if(ctrlDown){
                    if(shiftDown){
                        theVolume.camRoll=theVolume.savedRoll - (mousePt.x-mouseStartDrag.x)/3.0f;

                    }else{
                        theVolume.camPitch=theVolume.savedPitch - (mousePt.x-mouseStartDrag.x)/3.0f;
                        theVolume.camYaw=theVolume.savedYaw - (mousePt.y-mouseStartDrag.y)/3.0f;
                    }
                }else if(altDown){
                    xtra.x+=xDelta/100.0f;
                    xtra.y+=yDelta/100.0f;

                    for(int i=1; i< theShape.pointsInUse; i++){
                        theVolume.changed=true;

                        if(shiftDown){
                            theShape.pts[i].rotationRoll = theShape.pts[i].savedrotationroll + xtra.x;
                        }else{
                            theShape.pts[i].rotationPitch = theShape.pts[i].savedrotationpitch + xtra.x;
                            theShape.pts[i].rotationYaw = theShape.pts[i].savedrotationyaw + xtra.y;
                        }

                    }
                }else{
                    switch (selectedMovementAxis){
                        case X:
                            theVolume.changed=true;
                            xtra.x+=xDelta/2.0f*(xPos?1:-1);
                            xtra.x+=yDelta/2.0f*(yPos?1:-1);
                            theShape.selectedPt.x = theShape.selectedPt.savedx + xtra.x;
                            break;
                        case Y:
                            theVolume.changed=true;
                            xtra.y+=xDelta/2.0f*(xPos?1:-1);
                            xtra.y+=yDelta/2.0f*(yPos?1:-1);
                            theShape.selectedPt.y = theShape.selectedPt.savedy + xtra.y;
                            break;
                        case Z:
                            theVolume.changed=true;
                            xtra.z+=xDelta/2.0f*(xPos?1:-1);
                            xtra.z+=yDelta/2.0f*(yPos?1:-1);
                            theShape.selectedPt.z = theShape.selectedPt.savedz + xtra.z;
                            break;
                        default: //rotate camera

                            //theVolume.camPitch=theVolume.savedPitch - (mousePt.x-mouseStartDrag.x)/3.0f;
                            //theVolume.camYaw=theVolume.savedYaw - (mousePt.y-mouseStartDrag.y)/3.0f;

                            break;
                    }
                }
            }

            theShape.updateRadiusDegrees();
            //theMenu.camPitchSpinner.setValue(theMenu.camPitchSpinner.getValue());
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
            theVolume.changed=true;
            if(e.getWheelRotation()>0){ //scroll down
                theShape.selectedPt.scale*=scaleChangeFactor;
            }else{ //scroll up
                theShape.selectedPt.scale/=scaleChangeFactor;
            }
        }else if(altDown){
            theVolume.changed=true;
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
        this.setFocusable(true);
        this.requestFocusInWindow();

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
        if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
            selectEvolutionDescriptorPt();
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

        if(e.getKeyChar() == 'b'){
            rp.odbScale = new OneDBuffer();
            rp.odbRotationRoll = new OneDBuffer();
            rp.odbX = new OneDBuffer();
            rp.odbY = new OneDBuffer();
            rp.odbZ = new OneDBuffer();
            clearframe();
        }

        if(e.getKeyChar() == 's'){
        //    saveStuff("");
            imageUtils.saveImg(startTimeLog, rp.screenwidth, rp.screenheight, renderBuffer.pixels);
        }

        if(e.getKeyChar() == 'l'){
            loadStuff("");
        }

        if(e.getKeyChar() == 'v'){
            rp.shapeVibrating = !rp.shapeVibrating;
        }

      //  if(e.getKeyChar() == 's'){
           // ifs.volume.saveToAscii(theVolume.ifs.volume);
     //   }

        if(e.getKeyChar() == '0'){
            rp.smearPDF=true;
            theShape.setToPreset(0);
            theVolume.clear();
            rp.iterations=8;
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == '9'){
            rp.smearPDF=true;
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

        /*if(e.getKeyChar() == 'd'){
            rp.noDark=!rp.noDark;
            clearframe();
            gamefunc();
        }*/

        if(e.getKeyChar() == 'g'){
            rp.gradientColors=!rp.gradientColors;
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == ' '){
            rp.rightEye=!rp.rightEye;
            //clearframe();
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
            theMenu.updateEvolutionTable();
        }

        if(e.getKeyChar() == 'e'){
            eShape.offSpring(theShape, rp.evolveIntensity);
            theMenu.updateEvolutionTable();
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == 'z'){
            theShape=eShape.nextShape(0);
            theMenu.updateEvolutionTable();
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == 'x'){
            theShape=eShape.prevShape(0);
            theMenu.updateEvolutionTable();
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
            rp.postProcessPeriod=500;
            rp.savingDots=true;
            rp.savedDots=0;
            theVolume.renderMode = volume.RenderMode.VOLUMETRIC;
            eShape.offSpring(theShape, rp.evolveIntensity);
            eShape.evolving=!eShape.evolving;
            System.out.println("evolving: " + eShape.evolving);
        }
    }

    public void focusGained(FocusEvent focusevent){}
    public void focusLost(FocusEvent focusevent){}

}
