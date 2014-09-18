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

final class ifsys extends JPanel
    implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, FocusListener, ActionListener, Serializable
{
    animationThread theAnimationThread;
    paintThread thePaintThread;
    mainthread[] threads;
    evolutionThread theEvolutionThread;
    int numThreads = 1;//Runtime.getRuntime().availableProcessors()/2;
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
    //int[] buckets = new int[numBuckets]; //used for "load balancing" across the branches

    static ifsOverlays.DragAxis selectedMovementAxis = ifsOverlays.DragAxis.NONE;

    static boolean shiftDown;
    static boolean ctrlDown;
    static boolean altDown;

    static boolean isLeftPressed=false;
    static boolean isRightPressed=false;

    static boolean mousedown;
    static int mousex, mousey;
    int mouseScroll;

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

        //thePdf.thePdfComboMode = pdf3D.comboMode.MIN;

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
        setupMiniFrame(is.theMenu.pdfProperties,    200, 150,   is.rp.screenwidth,300, "Kernel", "cloud.png", desktop);
        setupMiniFrame(is.theMenu.pointProperties,  200, 300,   is.rp.screenwidth,0, "IFS Point", "anchors.png", desktop);
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
                    renderBuffer.updateTime(lastClearTime);
                    renderBuffer.shutterSpeed = rp.shutterPeriod;

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            //if(theVolume.totalSamples>5000000){
                                theMenu.updateSideMenu();
                                generatePixels();
                                repaint();
                            //}
                        }
                    });
                    //System.out.println(theVolume.changed);
                    sleep(10L);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
        }

        public paintThread(){
        }
    }

    public class animationThread extends Thread{

        public long lastShapeReload = 0;
        public int shapeReloadPeriod = 1000;
        public boolean shapeReload = false;

        public void run(){
            while(!quit)
                try{
                    if(rp.shapeVibrating){
                        shapeReloadPeriod = (int)rp.evolveLockPeriod;
                        if(shapeReload && System.currentTimeMillis() - lastShapeReload > shapeReloadPeriod){
                            float oldIntensity = rp.evolveIntensity;
                            float oldAnim = rp.evolveAnimationPeriod;
                            loadStuff("");

                            rp.shapeVibrating=true;
                            rp.evolveIntensity=oldIntensity;
                            rp.evolveAnimationPeriod=oldAnim;
                            lastShapeReload=System.currentTimeMillis();
                        }
                        //renderBuffer.totalLines=0;
                        theVolume.changed=true;
                        theShape = theShape.getPerturbedShape(eShape.mutationDescriptorPt.intensify(rp.evolveIntensity/100f), false);
                        rp.odbScale.smooth();
                        rp.odbRotationRoll.smooth();
                        rp.odbX.smooth();
                        rp.odbY.smooth();
                        rp.odbZ.smooth();
                        rp.odbScale.add(new OneDBuffer(10, rp.smearSmooth, System.currentTimeMillis()), 30); //scale
                        rp.odbRotationRoll.add(new OneDBuffer(20, rp.smearSmooth, System.currentTimeMillis()), 20); //rotation
                        rp.odbX.add(new OneDBuffer(30, rp.smearSmooth, System.currentTimeMillis()), 20); //offsetX
                        rp.odbY.add(new OneDBuffer(40, rp.smearSmooth, System.currentTimeMillis()), 20); //offsetY
                        rp.odbZ.add(new OneDBuffer(50, rp.smearSmooth, System.currentTimeMillis()), 20); //offsetZ
                        //if(!rp.renderThrottling || theVolume.totalSamples>rp.shutterPeriod *1000){
                            clearframe();
                            gamefunc();
                        //}
                    }

                    sleep((long)rp.evolveAnimationPeriod);
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
                                    //theMenu.updateEvolutionTable();
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
            try{
                while(!quit){
                    gamefunc();
                    sleep(100L);
                }
            }catch (InterruptedException e) {
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

        //for(int i=0; i< threads.length; i++){
            threads[0].start();
        //}

        theShape.setToPreset(0);

        started = true;

        thePaintThread.start();
        theEvolutionThread.start();
        theAnimationThread.start();
        clearframe();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        //theVolume.drawGrid(rp, renderBuffer);
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

            didProcess=true;
            renderBuffer.generatePixels((float)rp.brightnessMultiplier, rp.cartoonMode, rp.rightEye, rp.postProcess, rp.smearSize/8f,
                                        theVolume.camPitch, theVolume.camYaw, theVolume.camRoll, true,
                                        theVolume.camScale,
                                        theVolume.camCenter.x, theVolume.camCenter.y, theVolume.camCenter.z);

        if(didProcess)lastPostProcessTime=System.currentTimeMillis();
    }

    public void clearframe(){
        theVolume.totalSamples=0;
        renderBuffer.lastLineIndex=renderBuffer.lineIndex;

        theVolume.drawTime = System.currentTimeMillis();
        long wait = rp.shapeVibrating ? 40 : 10;
        if(!rp.holdFrame && System.currentTimeMillis() - lastClearTime > wait){

            if(theVolume.changed && theVolume.doneClearing){
                theVolume.clear();

                if(System.currentTimeMillis()-lastIndex>50){
                    renderBuffer.totalLines =renderBuffer.lineIndex;
                    renderBuffer.lineIndex=0;
                    indexFunction(0, rp.iterations, 1.0f, new ifsPt(0,0,0), new ifsPt(theShape.pts[0]));
                    lastIndex = System.currentTimeMillis();
                }
            }

            lastClearTime=System.currentTimeMillis();
            renderBuffer.clearZProjection();
        }
    }

    public void selectEvolutionDescriptorPt(){
        evolutionDescSelected=true;
    }

    public void unselectEvolutionDescriptorPt(){
        evolutionDescSelected=false;
    }

    public void gamefunc(){
        gamefunc(rp.iterations);
    }


    public void gamefunc(int _iterations){
        rp.guidesHidden = System.currentTimeMillis() - lastMoveTime > rp.linesHideTime;
        //if(System.currentTimeMillis()-lastIndex>50){
        //    indexFunction(0, _iterations, 1.0f, new ifsPt(0,0,0), new ifsPt(theShape.pts[0]));
        //    lastIndex = System.currentTimeMillis();
        //}
    }

    long lastIndex = System.currentTimeMillis();
    private void indexFunction(int _index, int _iterations, float _cumulativeScale, ifsPt _cumulativeRotation, ifsPt _dpt){
        ifsPt dpt = new ifsPt(_dpt);
        ifsPt cumulativeRotation = new ifsPt(_cumulativeRotation);
        ifsPt thePt = theShape.pts[_index];
        ifsPt centerPt = theShape.pts[0];

        cumulativeRotation = cumulativeRotation.add(new ifsPt(thePt.rotationPitch,thePt.rotationYaw,thePt.rotationRoll));

        ifsPt rpt = thePt.subtract(centerPt).scale(_cumulativeScale).getRotatedPt(cumulativeRotation);
        ifsPt odp = new ifsPt(dpt);
        dpt._add(rpt);

        ifsPt proj_odp = odp; //theVolume.getCameraDistortedPt(odp, rp.rightEye);
        ifsPt proj_dpt = dpt; //theVolume.getCameraDistortedPt(dpt, rp.rightEye);

        renderBuffer.lineX1[renderBuffer.lineIndex]=(short)dpt.x;
        renderBuffer.lineY1[renderBuffer.lineIndex]=(short)dpt.y;
        renderBuffer.lineZ1[renderBuffer.lineIndex]=(short)dpt.z;
        renderBuffer.lineS1[renderBuffer.lineIndex]=(short)(_cumulativeScale*thePt.scale/centerPt.scale*255f);

        renderBuffer.lineX2[renderBuffer.lineIndex]=(short)odp.x;
        renderBuffer.lineY2[renderBuffer.lineIndex]=(short)odp.y;
        renderBuffer.lineZ2[renderBuffer.lineIndex]=(short)odp.z;
        renderBuffer.lineS2[renderBuffer.lineIndex]=(short)(_cumulativeScale*255f);
        //renderBuffer.lineMag[renderBuffer.lineIndex]=(float)proj_odp.distTo(proj_dpt);
        renderBuffer.lineIndex++;
        renderBuffer.lineIndex=Math.min(renderBuffer.lineIndex, renderBuffer.lineX1.length-1);

        //theVolume.putPdfSample(dpt, cumulativeRotation, _cumulativeScale, thePt, theShape.pts[0], odp,
        //                        0, 0, 0, rp, thePdf, renderBuffer, rpt.magnitude(), theMenu.colorChooser, 1.0f);

        if(_iterations>1){
            _cumulativeScale *= thePt.scale/centerPt.scale;
            for(int i=0; i<theShape.pointsInUse; i++){
                indexFunction(i, _iterations-1, _cumulativeScale, cumulativeRotation, dpt);
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

        isDragging=false;
    }

    public void mouseEntered(MouseEvent e){}

    public void mouseExited(MouseEvent e){}

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
                            theShape.pts[i].rotationRoll = theShape.pts[i].savedPt.rotationRoll + xtra.x;
                        }else{
                            theShape.pts[i].rotationPitch = theShape.pts[i].savedPt.rotationPitch + xtra.x;
                            theShape.pts[i].rotationYaw = theShape.pts[i].savedPt.rotationYaw + xtra.y;
                        }

                    }
                }else{
                    switch (selectedMovementAxis){
                        case X:
                            theVolume.changed=true;
                            xtra.x+=xDelta/2.0f*(xPos?1:-1);
                            xtra.x+=yDelta/2.0f*(yPos?1:-1);
                            theShape.selectedPt.x = theShape.selectedPt.savedPt.x + xtra.x;
                            break;
                        case Y:
                            theVolume.changed=true;
                            xtra.y+=xDelta/2.0f*(xPos?1:-1);
                            xtra.y+=yDelta/2.0f*(yPos?1:-1);
                            theShape.selectedPt.y = theShape.selectedPt.savedPt.y + xtra.y;
                            break;
                        case Z:
                            theVolume.changed=true;
                            xtra.z+=xDelta/2.0f*(xPos?1:-1);
                            xtra.z+=yDelta/2.0f*(yPos?1:-1);
                            theShape.selectedPt.z = theShape.selectedPt.savedPt.z + xtra.z;
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
            theShape = theShape.loadFromFile("locked.shape");
        }else{
            theShape = theShape.loadFromFile(filename);
        }
        //rp = theShape.rp;
    }

    public void saveStuff(String filename){
        //theShape.rp = rp;
        if(filename==""){
            theShape.saveToFile("locked.shape");
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
            rp.odbScale = new OneDBuffer(10, rp.smearSmooth, rp.wobbleRandomSeed);
            rp.odbRotationRoll = new OneDBuffer(20, rp.smearSmooth, rp.wobbleRandomSeed);
            rp.odbX = new OneDBuffer(30, rp.smearSmooth, rp.wobbleRandomSeed);
            rp.odbY = new OneDBuffer(40, rp.smearSmooth, rp.wobbleRandomSeed);
            rp.odbZ = new OneDBuffer(50, rp.smearSmooth, rp.wobbleRandomSeed);
            clearframe();
        }

        if(e.getKeyChar() == 's'){
            saveStuff("");
            //imageUtils.saveImg(startTimeLog, rp.screenwidth, rp.screenheight, renderBuffer.pixels);
        }

        if(e.getKeyChar() == 'l'){
            loadStuff("");
        }

        if(e.getKeyChar() == 'v'){
            saveStuff("");
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
            clearframe();
            gamefunc();
            System.out.println("right eye: " + rp.rightEye);
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
                rp.cartoonMode = !rp.cartoonMode;
                break;
        }

        if(e.getKeyChar() == 'w'){
            eShape.parents(theShape);
            //theMenu.updateEvolutionTable();
        }

        if(e.getKeyChar() == 'e'){
            eShape.offSpring(theShape, rp.evolveIntensity);
            //theMenu.updateEvolutionTable();
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == 'z'){
            theShape=eShape.nextShape(0);
            //theMenu.updateEvolutionTable();
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == 'x'){
            theShape=eShape.prevShape(0);
            //theMenu.updateEvolutionTable();
            clearframe();
            gamefunc();
        }

        if(e.getKeyChar() == 'p'){
            rp.postProcess=!rp.postProcess;
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

        if(e.getKeyChar() == '2'){
            rp.twoD=!rp.twoD;
            clearframe();
            gamefunc();
            System.out.println("2d projections: " + rp.twoD);
        }

        if(e.getKeyChar() == '3'){
            rp.threeD=!rp.threeD;
            clearframe();
            gamefunc();
            System.out.println("3d projection: " + rp.threeD);
        }

        if(e.getKeyChar() == 'q'){
            //rp.zMin = 512;rp.zMax=1024;
            //rp.drawGrid=false;
            //theShape.setToPreset(0);
            theShape.setToPreset(9);
            theVolume.clear();
            rp.iterations=8;
            rp.cartoonMode =true;
            rp.brightnessMultiplier=1;
            rp.smearPDF=true;
            rp.renderThrottling=true;
            rp.shutterPeriod =500;
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
