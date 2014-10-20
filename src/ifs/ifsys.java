package ifs;

import ifs.flat.OneDBuffer;
import ifs.flat.RenderBuffer;
import ifs.flat.ShapeAnalyzer;
import ifs.flat.TetraMarcher;
import ifs.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

final class ifsys extends JPanel
    implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, FocusListener, ActionListener, Serializable
{
    animationThread theAnimationThread;
    paintThread thePaintThread;
    mainthread[] threads;

    int numThreads = 1;//Runtime.getRuntime().availableProcessors()/2;
    boolean quit;

    boolean shapeAnalyzing = true;

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

    long indexCount;

    volume theVolume;

    ShapeAnalyzer shapeAnalyzer;
    RenderBuffer renderBuffer;
    RenderParams rp;
    ifsShape theShape;

    ifsPt mutationDescriptorPt = new ifsPt(1,1,1, 1,1,1);

    ifsMenu theMenu;
    ifsOverlays overlays;

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
    boolean iterationDescSelected;

    ifsPt mousePt;
    ifsPt mouseStartDrag;

    boolean started;
    boolean isDragging;

    MemoryImageSource renderImageSource;
    Image misImage;

    public ifsys(){

        initZLists();

        evolutionDescSelected=false;
        iterationDescSelected=false;
        rp = new RenderParams();
        renderBuffer = new RenderBuffer(rp.screenwidth, rp.screenheight);
        shapeAnalyzer = new ShapeAnalyzer();

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

        theShape = new ifsShape();

        theVolume = new volume(1024, 1024, 1024);
        theVolume.clear();

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
        setupMiniFrame(is.theMenu.shapeProperties, 200, 350,   is.rp.screenwidth, 300, "Shape", "invader.png", desktop);
        setupMiniFrame(is.theMenu.renderProperties, 200, 300,   is.rp.screenwidth+200,0, "Camera", "camera.png", desktop);
        //setupMiniFrame(is.theMenu.pdfProperties,    200, 150,   is.rp.screenwidth,300, "Kernel", "cloud.png", desktop);
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
                        theShape = theShape.getPerturbedShape(mutationDescriptorPt.intensify(rp.evolveIntensity/100f), false);
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
        float jitter = rp.jitter;
        renderBuffer.generatePixels((float)rp.brightnessMultiplier, rp.cartoonMode, rp.rightEye, rp.postProcess, rp.smearSize/8f,
                                    theVolume.camPitch + (float)(Math.random()-0.5)*jitter, theVolume.camYaw + (float)(Math.random()-0.5)*jitter, theVolume.camRoll, theVolume.usePerspective,
                                    theVolume.camScale,
                                    theVolume.camCenter.x, theVolume.camCenter.y, theVolume.camCenter.z, rp.perspectiveScale, rp.shutterPeriod);
        lastPostProcessTime=System.currentTimeMillis();
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
                    reIndex();
                    renderBuffer.updateGeometry();
                    if(shapeAnalyzing){
                        shapeAnalyzer.updateGeometry();
                    }
                    lastIndex = System.currentTimeMillis();
                }
            }

            lastClearTime=System.currentTimeMillis();
        }
    }

    public void reIndex(){
        Random rnd = new Random();
        rnd.setSeed(rp.randomSeed);
        indexCount=0;
        clearZLists();
        xMin = 1024;
        yMin = 1024;
        zMin = 1024;
        xMax = 0;
        yMax = 0;
        zMax = 0;
        indexFunction(0, (int)(rp.iterations/10)+1, rp.iterations%10,  1.0f, new ifsPt(0,0,0), new ifsPt(theShape.pts[0]), rnd, rp.randomScale, (short)0, rp.maxBranchDist);
    }

    ArrayList<Integer>[] zLists = new ArrayList[1024];

    public void initZLists(){
        for(int i=0; i<1024; i++){
            zLists[i]= new ArrayList<>();
        }
    }

    public void clearZLists(){
        for(int i=0; i<1024; i++){
            zLists[i].clear();
        }
    }
    long count=0;
    public void addLineToZList(int index, int z1, int z2, int max){
        int _z1 = Math.max(Math.min(z1, z2)-max, 1);
        int _z2 = Math.min(Math.max(z1, z2)+max, 1023);
        for(int i=_z1; i<_z2; i++){
            zLists[i].add(index);
            count++;
        }
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

    int zMin, zMax, xMin, xMax, yMin, yMax;

    private void indexFunction(int _index, int _iterations, int _subiters, float _cumulativeScale, ifsPt _cumulativeRotation, ifsPt _dpt, Random _rnd, float rndScale, short dist, int maxBranchDist){

        ifsPt dpt = new ifsPt(_dpt);
        ifsPt cumulativeRotation = new ifsPt(_cumulativeRotation);
        ifsPt thePt = theShape.pts[_index];
        ifsPt centerPt = theShape.pts[0];

        cumulativeRotation = cumulativeRotation.add(
                new ifsPt(thePt.rotationPitch+(float)(_rnd.nextGaussian())*rndScale/1000f,
                            thePt.rotationYaw+(float)(_rnd.nextGaussian())*rndScale/1000f,
                            thePt.rotationRoll+(float)(_rnd.nextGaussian())*rndScale/1000f));

        ifsPt rpt = thePt.subtract(centerPt).scale(_cumulativeScale).getRotatedPt(cumulativeRotation);
        ifsPt odp = new ifsPt(dpt);
        dpt._add(rpt);

        //ifsPt proj_odp = odp; //theVolume.getCameraDistortedPt(odp, rp.rightEye);
        //ifsPt proj_dpt = dpt; //theVolume.getCameraDistortedPt(dpt, rp.rightEye);

        renderBuffer.lineDI[renderBuffer.lineIndex]=(((short)(dist))<<16) + ((short)_iterations);

        renderBuffer.lineXY1[renderBuffer.lineIndex]=(((short)(dpt.x))<<16) + ((short)dpt.y);
        renderBuffer.lineZS1[renderBuffer.lineIndex]=(((short)(dpt.z))<<16) + ((short)(256f *_cumulativeScale*thePt.scale/centerPt.scale));
        renderBuffer.lineXY2[renderBuffer.lineIndex]=(((short)(odp.x))<<16) + ((short)odp.y);
        renderBuffer.lineZS2[renderBuffer.lineIndex]=(((short)(odp.z))<<16) + ((short)(256f * _cumulativeScale));

        int maxDist = 16+1;

        addLineToZList(renderBuffer.lineIndex, (int)odp.z, (int)dpt.z, maxDist);

        xMin = (int)Math.max(0,Math.min(Math.min(xMin,odp.x-maxDist),Math.min(xMin,dpt.x-maxDist)));
        yMin = (int)Math.max(0,Math.min(Math.min(yMin,odp.y-maxDist),Math.min(yMin,dpt.y-maxDist)));
        zMin = (int)Math.max(0,Math.min(Math.min(zMin,odp.z-maxDist),Math.min(zMin,dpt.z-maxDist)));

        xMax = (int)Math.min(1023,Math.max(Math.max(xMax,odp.x+maxDist),Math.max(xMax,dpt.x+maxDist)));
        yMax = (int)Math.min(1023,Math.max(Math.max(yMax,odp.y+maxDist),Math.max(yMax,dpt.y+maxDist)));
        zMax = (int)Math.min(1023,Math.max(Math.max(zMax,odp.z+maxDist),Math.max(zMax,dpt.z+maxDist)));

        if(shapeAnalyzing){
            shapeAnalyzer.lineXY1[renderBuffer.lineIndex] = renderBuffer.lineDI[renderBuffer.lineIndex];
            shapeAnalyzer.lineXY1[renderBuffer.lineIndex] = renderBuffer.lineXY1[renderBuffer.lineIndex];
            shapeAnalyzer.lineZS1[renderBuffer.lineIndex] = renderBuffer.lineZS1[renderBuffer.lineIndex];
            shapeAnalyzer.lineXY2[renderBuffer.lineIndex] = renderBuffer.lineXY2[renderBuffer.lineIndex];
            shapeAnalyzer.lineZS2[renderBuffer.lineIndex] = renderBuffer.lineZS2[renderBuffer.lineIndex];
        }

        renderBuffer.lineIndex++;
        renderBuffer.lineIndex=Math.min(renderBuffer.lineIndex, renderBuffer.lineXY1.length-1);

        //theVolume.putPdfSample(dpt, cumulativeRotation, _cumulativeScale, thePt, theShape.pts[0], odp,
        //                        0, 0, 0, rp, thePdf, renderBuffer, rpt.magnitude(), theMenu.colorChooser, 1.0f);

        if(_iterations>1){

            double branchThresh=rp.pruneThresh*0.01d;
            Random branchRandom = new Random();

            _cumulativeScale *= thePt.scale/centerPt.scale;
            _cumulativeScale *= (float)(1.0f - _rnd.nextGaussian()*rndScale/3000f);
            for(int i=1; i<theShape.pointsInUse; i++){

                branchRandom.setSeed(indexCount*rp.randomSeed*i);
                indexCount++;
                if(branchRandom.nextDouble()>branchThresh){
                    dist+=_cumulativeScale*256; //256 is arbitrary
                    indexFunction(i, _iterations-1, _subiters, _cumulativeScale, cumulativeRotation, dpt, _rnd, rndScale, dist, maxBranchDist);
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

        evolutionDescSelected=false;
        iterationDescSelected=false;

        if(e.getClickCount()==2){
            theShape.selectNearestPt();
            //theVolume.camCenter = new ifsPt(theShape.selectedPt);
            //clearframe();
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
        theShape.findNearestPt(mousex, mousey, overlays.minInterestDist, theVolume, rp);
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
            evolutionDescSelected=true;
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
            imageUtils.saveImg(startTimeLog, rp.screenwidth, rp.screenheight, renderBuffer.pixels);
        }

        if(e.getKeyChar() == 'l'){
            loadStuff("");
        }

        if(e.getKeyChar() == 'i'){
            iterationDescSelected=true;
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

        if(e.getKeyChar() == 'p'){
            rp.postProcess=!rp.postProcess;
            clearframe();
            gamefunc();
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

        if(e.getKeyChar() == '6'){
            recordsKeeper rk = new recordsKeeper();
            rk.getPotentials(this);
        }

        if(e.getKeyChar() == '3'){
            rp.threeD=!rp.threeD;
            clearframe();
            gamefunc();
            System.out.println("3d projection: " + rp.threeD);
        }
    }

    public void focusGained(FocusEvent focusevent){}
    public void focusLost(FocusEvent focusevent){}

}
