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

    double dataMax = 0;
    double gamma = 0;
    int pixels[];
    long numPoints=0;
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
        boolean antiAliasing;
        boolean infoHidden;
        boolean usePDFSamples;
        boolean guidesHidden;
        int sampletotal;
        int iterations;
        int pointselected;
        ifsPt selectedPt;

        boolean shiftDown;
        boolean ctrlDown;
        boolean altDown;
        int mousex, mousey, mousez;
        int mouseScroll;
        int rotateMode;

    ifsShape shape;

    int maxPoints;
    int maxLineLength;

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
    int preset;

    ifsOverlays overlays;

    public ifsys(){
        started=false;
        numPoints=0;
        oneSecondAgo =0;
        framesThisSecond = 0;
        altDown=false;
        ctrlDown=false;
        shiftDown=false;
        game = new mainthread();
        quit = false;
        antiAliasing = false;
        framesHidden = true;
        infoHidden = false;
        usePDFSamples = false;
        guidesHidden = false;
        screenwidth = 512;
        screenheight = 512;
        pixels = new int[screenwidth * screenheight];
        sampletotal = 512;
        iterations = 5;
        mousemode = 0;
        maxLineLength = screenwidth;
        maxPoints = 100;
        shape = new ifsShape(maxPoints);
        mouseScroll = 0;
        gamma = 1.0D;
        pointselected=-1;
        isDragging = false;

        theVolume = new volume(screenwidth, screenheight, 5);
        theVolume.clear();
        thePdf = new pdf3D();

        rotateMode=0;
        lastMoveTime=0;
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
        pointselected = shape.getNearestPtIndexXY(mousex, mousey);
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

        overlays = new ifsOverlays(this, rg);

        preset = 1;
        clearframe();
        game.start();
        shape.setToPreset(1);

        started = true;
    }

    public void update(Graphics gr){
        paint(gr);
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
        rg.setColor(Color.blue);

        if(!guidesHidden){
            overlays.drawArcs(rg);
            overlays.drawCenterOfGravity(rg);
        }


        if(!infoHidden && pointselected>=0){
            overlays.drawInfoBox(rg);
        }

        gr.drawImage(render, 0, 0, screenwidth, screenheight, this);
    }

    public void generatePixels(){
        double scaler = 255;
        double area = 0;
        int scaledColor = 0;

        double [] pixData = theVolume.getFullSliceXY();

        for(int a = 0; a < theVolume.width * theVolume.height; a++){
            int argb = 255;
            scaledColor = (int)(scaler*pixData[a]);
            argb = (argb << 8) + scaledColor;
            argb = (argb << 8) + scaledColor;
            argb = (argb << 8) + scaledColor;
            pixels[a] = argb;
            area+=scaler*pixData[a];
        }
    }

    public void clearframe(){
        for(int a = 0; a < screenwidth * screenheight; a++){
            pixels[a] = 0xff000000;
        }
        theVolume.clear();
    }

    public boolean putPixel(ifsPt pt, double alpha){
        theVolume.putPixel(pt,alpha,antiAliasing);
        return true;
    }

    public void putPdfSample(ifsPt dpt, double cumulativeRotationYaw, double cumulativeRotationPitch, double cumulativeScale, double cumulativeOpacity, ifsPt thePt, double scaleDown){
        //generate random coords

        double x=dpt.x;
        double y=dpt.y;
        double z=dpt.z;

        double centerX = thePdf.sampleWidth/2;
        double centerY = thePdf.sampleHeight/2;
        double centerZ = thePdf.sampleDepth/2;

        double sampleX = Math.random()*thePdf.sampleWidth;
        double sampleY = Math.random()*thePdf.sampleHeight;
        double sampleZ = thePdf.sampleDepth/2;

        //modulate with image
        double exposureAdjust = cumulativeScale*thePt.scale*thePt.radius;
        double ptColor = thePdf.volume[(int)sampleX][(int)sampleY][(int)sampleZ]/255.0*cumulativeOpacity/scaleDown*exposureAdjust*exposureAdjust;

        //double ptColor = thePdf.getSliceXY_Sum((int)sampleX,(int)sampleY)/255.0*cumulativeOpacity/scaleDown*exposureAdjust*exposureAdjust;

        //rotate/scale the point
        //double pointDist = shape.distance(sampleX, sampleY, 0)*cumulativeScale*thePt.scale*thePt.radius/thePdf.sampleWidth;

        double scale = cumulativeScale*thePt.scale*thePt.radius/thePdf.sampleWidth;

        double pointDegreesYaw = thePt.rotationYaw -thePt.degreesYaw+cumulativeRotationYaw;
        double multi = 1;//pointDegreesYaw>Math.PI ? 1 : -1;
        double pointDegreesPitch = thePt.rotationPitch;//Math.PI/2+thePt.rotationPitch -thePt.degreesPitch+cumulativeRotationPitch;
        //System.out.println(Math.PI/2+thePt.rotationPitch -thePt.degreesPitch+cumulativeRotationPitch);

        ifsPt rpt = new ifsPt((sampleX-centerZ)*scale,(sampleY-centerY)*scale,(sampleZ-centerZ)*scale).getRotatedPt(-pointDegreesPitch, -pointDegreesYaw);

        double placedX = rpt.x;
        double placedY = rpt.y;
        double placedZ = rpt.z;

        //put pixel
        putPixel(new ifsPt(x+placedX,y+placedY, z+placedZ), ptColor);
    }

    public void gamefunc(){
        guidesHidden = System.currentTimeMillis() - lastMoveTime > 1000;

        if(shape.pointsInUse != 0){

            for(int a = 0; a < sampletotal; a++){
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

                    randomIndex = 1 + (int)(Math.random() * (double) (shape.pointsInUse-1));

                    if(d==0){randomIndex=0;}

                    if(d!=0){
                        size = shape.pts[randomIndex].radius * cumulativeScale;
                        yaw = Math.PI/2D - shape.pts[randomIndex].degreesYaw + cumulativeRotationYaw;
                        pitch = Math.PI/2D - shape.pts[randomIndex].degreesPitch + cumulativeRotationPitch;

                        rpt = new ifsPt(size,0,0).getRotatedPt(-pitch, -yaw);

                        dpt.x += rpt.x;
                        dpt.y += rpt.y;
                        dpt.z += rpt.z;
                    }

                    if(usePDFSamples)
                        putPdfSample(dpt, cumulativeRotationYaw,cumulativeRotationPitch, cumulativeScale, cumulativeOpacity, shape.pts[randomIndex], scaleDownMultiplier);
                    cumulativeScale *= shape.pts[randomIndex].scale/shape.pts[0].scale;
                    cumulativeOpacity *= shape.pts[randomIndex].opacity;

                    cumulativeRotationYaw += shape.pts[randomIndex].rotationYaw;
                    cumulativeRotationPitch += shape.pts[randomIndex].rotationPitch;
                }

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
            startDragX = mousex;
            startDragY = mousey;
            startDragZ = mousez;
            shape.updateCenter();

            startDragPX = selectedPt.x;
            startDragPY = selectedPt.y;
            startDragPZ = selectedPt.z;
            startDragDist = shape.distance(startDragX - selectedPt.x, startDragY - selectedPt.y, startDragZ - selectedPt.z);
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

    public void mouseDragged(MouseEvent e){
        isDragging=true;
        lastMoveTime = System.currentTimeMillis();
        if(mousemode == 1){ //left click to move a point/set
            setCursor (Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            selectedPt.x = startDragPX + (e.getX() - startDragX);
            selectedPt.y = startDragPY + (e.getY() - startDragY);
            selectedPt.z = startDragPZ + (mousez - startDragZ);
        }
        else if(mousemode == 3){ //right click to rotate point/set
            setCursor (Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));

            double scaleDelta = shape.distance(e.getX() - selectedPt.x, e.getY() - selectedPt.y, mousez - shape.pts[0].z)/startDragDist;
            if(rotateMode==0){
                double rotationDelta = (Math.atan2(e.getX() - selectedPt.x, e.getY() - selectedPt.y)- startDragAngleYaw);
                selectedPt.rotationYaw = Math.PI * 2 - rotationDelta;
            }else if(rotateMode==1){
                double rotationDelta = (Math.atan2(e.getX() - selectedPt.x, e.getY() - selectedPt.y)- startDragAnglePitch);
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
        findSelectedPoint();
        mousex = e.getX();
        mousey = e.getY();
        mousez = 0;
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
