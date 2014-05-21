import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.net.URL;

public class ifsys extends Panel
    implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, FocusListener
{
    mainthread game;
    boolean quit;
    int screenwidth;
    int screenheight;

    int samplePixels[];
    int sampleWidth;
    int sampleHeight;

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

    //user params
        boolean framesHidden;
        boolean centerHidden;
        boolean leavesHidden;
        boolean antiAliasing;
        boolean trailsHidden;
        boolean spokesHidden;
        boolean infoHidden;
        boolean imgSamples;
        boolean guidesHidden;
        boolean ptsHidden;
        int sampletotal;
        int iterations;
        int pointselected;
        ifsPt selectedPt;

        boolean shiftDown;
        boolean ctrlDown;
        boolean altDown;
        int mousex;
        int mousey;
        int mouseScroll;

    ifsShape shape;
    int maxPoints;
    int maxLineLength;

    //drag vars
        int mousemode; //current mouse button
        double startDragX;
        double startDragY;
        double startDragPX;
        double startDragPY;
        double startDragCenterX;
        double startDragCenterY;
        double startDragDist;
        double startDragAngle;
        double startDragScale;

    String presetstring;
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
        centerHidden = false;
        spokesHidden = true;
        trailsHidden = true;
        leavesHidden = true;
        infoHidden = false;
        imgSamples = true;
        guidesHidden = false;
        ptsHidden = false;
        screenwidth = 1024;
        screenheight = 1024;
        pixels = new int[screenwidth * screenheight];
        samplePixels = new int[screenwidth * screenheight];
        pixelsData = new double[screenwidth * screenheight];
        sampletotal = 1000;
        iterations = 2;
        mousemode = 0;
        samplesNeeded = 1;
        maxLineLength = screenwidth;
        maxPoints = 100;
        shape = new ifsShape(maxPoints);
        mouseScroll = 0;
        gamma = 1.0D;
        pointselected=-1;
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
    }

    public void init() {
       // add(new Button("Real's"));
        start();
    }


    public void findSelectedPoint(){
        pointselected = shape.getNearestPtIndex(mousex, mousey);
        selectedPt = shape.pts[pointselected];
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
       // presetstring = getParameter("preset");
        preset = 1;//Integer.parseInt(presetstring);
        clearframe();
        game.start();
        shape.setToPreset(1);
        setSampleImg("meerkat.jpg");
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
        rg.drawImage(loadImage("meerkat.jpg"), getWidth() - 50, 0, 50, 50, this);

        int circleWidth;

        rg.setColor(Color.blue);
        ifsPt thePt;

        if(!guidesHidden)
        for(int i=0;i<shape.pointsInUse;i++){
            if(i==pointselected){
                rg.setColor(Color.LIGHT_GRAY);
            }else{
                rg.setColor(Color.darkGray);
            }
            thePt = shape.pts[i];
            circleWidth = (int)(thePt.scale*thePt.radius*2);
            rg.drawOval((int)thePt.x-circleWidth/2, (int)thePt.y-circleWidth/2, circleWidth, circleWidth);
            rg.drawLine((int)thePt.x, (int)thePt.y, (int)(thePt.x + Math.sin(thePt.degrees-thePt.rotation)*thePt.scale*thePt.radius),
                                                    (int)(thePt.y + Math.cos(thePt.degrees-thePt.rotation)*thePt.scale*thePt.radius));
            if(!centerHidden){
                rg.setColor(Color.blue);
                rg.drawLine((int)shape.centerx, (int)shape.centery, (int)(shape.centerx + Math.sin(thePt.degrees)*thePt.radius),
                                                                    (int)(shape.centery + Math.cos(thePt.degrees)*thePt.radius));
            }
        }

        if(!infoHidden && pointselected>=0){
            rg.setColor(Color.white);
            rg.drawString("Point " + String.valueOf(pointselected + 1), 5, 15);
            rg.drawString("X: " + String.valueOf((double)(int)(selectedPt.x * 1000D) / 1000D), 5, 30);
            rg.drawString("Y: " + String.valueOf((double)(int)(selectedPt.y * 1000D) / 1000D), 5, 45);
            rg.drawString("Scale: " + String.valueOf((double)(int)(selectedPt.scale * 1000D) / 1000D), 5, 60);
            rg.drawString("Rotation: " + String.valueOf((double)(int)((((selectedPt.rotation / Math.PI) * 180D + 36000000D) % 360D) * 1000D) / 1000D), 5, 75);
            rg.drawString("Opacity: " + String.valueOf(selectedPt.opacity), 5, 90);
            rg.drawString("Iterations (. /): " + String.valueOf(iterations), 5, 105);
            rg.drawString("Samples (nm): " + String.valueOf(sampletotal), 4, 120);
            rg.drawString("Expected Done %" + String.valueOf((int)Math.min(100*samplesThisFrame/samplesNeeded/Math.E, 100)), 5, 135); //TODO is dividing by E the right thing to do here?
            rg.drawString("FPS " + String.valueOf(fps), 5, 150);
            rg.drawString("Gamma " + String.valueOf(gamma), 5, 165);
        }

        gr.drawImage(render, 0, 0, screenwidth, screenheight, this);
    }

    public void generatePixels(){
        double scaler = 255/dataMax;
        int scaledColor = 0;

        for(int a = 0; a < screenwidth * screenheight; a++){
            int argb = 255;
            scaledColor = (int)(scaler*pixelsData[a]);
            argb = (argb << 8) + scaledColor;
            argb = (argb << 8) + scaledColor;
            argb = (argb << 8) + scaledColor;
            pixels[a] = argb;
        }
    }

    public void clearframe(){
        for(int a = 0; a < screenwidth * screenheight; a++){
            pixels[a] = 0xff000000;
            pixelsData[a] = 0;
        }
        samplesThisFrame=0;
        dataMax = 0;
    }

    public boolean putPixel(double x, double y, double alpha){ 
        double decX, decY; //decimal parts of coordinates

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
                pixelsData[(int)(x) + (int)(y) * screenwidth]=1;
            }

            samplesThisFrame++;

            return true; //pixel is in screen bounds
        }else{
            return false; //pixel outside of screen bounds
        }

    }

    public void setSampleImg(String filename){
        Image image = loadImage("meerkat.jpg");

        try {
            PixelGrabber grabber =
                    new PixelGrabber(image, 0, 0, -1, -1, false);

            if (grabber.grabPixels()) {
                sampleWidth = grabber.getWidth();
                sampleHeight = grabber.getHeight();
                samplePixels = (int[]) grabber.getPixels();

                for(int i=0; i<sampleHeight*sampleWidth; i++){
                    samplePixels[i] = samplePixels[i]&0xFF;
                }
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public double getSampleValue(double x, double y){ //TODO bilinear filtering
        int index = (int)x+sampleWidth*(int)y;
        if(index < sampleWidth*sampleHeight && index>0){
            return samplePixels[(int)x + (int)y*sampleWidth] / 255.0;
        }else{
            return 0;
        }
    }

    public void putImgSample(double x, double y, double cumulativeRotation, double cumulativeScale, double cumulativeOpacity, ifsPt thePt, double scaleDown){
        //generate random coords
        double sampleX = Math.random()*sampleWidth;
        double sampleY = Math.random()*sampleHeight;

        //modulate with image
        double exposureAdjust = cumulativeScale*thePt.scale*thePt.radius;
        double ptColor = getSampleValue(sampleX,  sampleY)*cumulativeOpacity/scaleDown*exposureAdjust*exposureAdjust;

        //rotate/scale the point
        double pointDegrees = Math.atan2(sampleX - sampleWidth/2, sampleY - sampleHeight/2)+cumulativeRotation+thePt.rotation;
        double pointDist = shape.distance(sampleX - sampleWidth/2, sampleY - sampleHeight/2)*cumulativeScale*thePt.scale*thePt.radius/sampleWidth;
        double placedX = Math.cos(pointDegrees)*pointDist;
        double placedY = Math.sin(pointDegrees)*pointDist;

        //put pixel
        putPixel(x+placedX,y+placedY, ptColor);
    }

    public void putLine(double x0, double y0, double x1, double y1, double alpha){ //TODO start/end alpha values?
        double steps = (int)shape.distance(x0-x1, y0-y1);
        double dx, dy;

        boolean startedInScreen = false;

        if(steps>maxLineLength){steps=maxLineLength;}

        samplesThisFrame++;

        for(int i=0; i<steps; i++){
            dx = x0 + i*(x1-x0)/steps;
            dy = y0 + i*(y1-y0)/steps;

            if(putPixel(dx, dy, alpha)){ //stop drawing if pixel is outside bounds
                startedInScreen = true;
            }else{
                if(startedInScreen)break;
            };
        }
    }

    public void gamefunc(){
        samplesNeeded = Math.pow(shape.pointsInUse, iterations);

        if(shape.pointsInUse != 0){

            if(!centerHidden){
                if(!spokesHidden){ //center spokes
                    for(int a=0; a<shape.pointsInUse; a++){
                        putLine(shape.centerx, shape.centery, shape.pts[a].x, shape.pts[a].y, shape.pts[a].opacity);
                    }
                }

                if(!framesHidden){ //center outline
                    for(int a=0; a<shape.pointsInUse; a++){
                        int nextPt = (a+1)%shape.pointsInUse;
                        putLine(shape.pts[a].x, shape.pts[a].y, shape.pts[nextPt].x, shape.pts[nextPt].y, shape.pts[nextPt].opacity);
                    }
                }
            }

            for(int a = 0; a < sampletotal; a++){
                int randomIndex = (int)(Math.random() * (double) shape.pointsInUse);
                int nextIndex = (randomIndex+1)%shape.pointsInUse;
                double dx = shape.pts[randomIndex].x;
                double dy = shape.pts[randomIndex].y;
                double ndx;
                double ndy;
                double _dx;
                double _dy;
                double cumulativeScale = 1.0D;
                double nextCumulativeScale = 1.0D;
                double cumulativeRotation = shape.pts[randomIndex].rotation;
                double nextCumulativeRotation = shape.pts[randomIndex].rotation;
                double cumulativeOpacity = 1.0D;

                double scaleDownMultiplier = Math.pow(shape.pointsInUse,iterations-1); //this variable is used to tone down repeated pixels so leaves and branches are equally exposed

                for(int d = 0; d < iterations; d++){
                    scaleDownMultiplier/=shape.pointsInUse;

                    randomIndex = (int)(Math.random() * (double) shape.pointsInUse);
                    nextIndex = (randomIndex+1)%shape.pointsInUse;

                    nextCumulativeScale = cumulativeScale*shape.pts[nextIndex].scale;
                    nextCumulativeRotation = cumulativeRotation + shape.pts[nextIndex].rotation;

                    cumulativeScale *= shape.pts[randomIndex].scale;
                    cumulativeRotation += shape.pts[randomIndex].rotation;
                    cumulativeOpacity *= shape.pts[randomIndex].opacity;

                    _dx = dx;
                    _dy = dy;
                    dx += Math.cos((Math.PI/2D - shape.pts[randomIndex].degrees) + cumulativeRotation) * shape.pts[randomIndex].radius * cumulativeScale;
                    dy += Math.sin((Math.PI/2D - shape.pts[randomIndex].degrees) + cumulativeRotation) * shape.pts[randomIndex].radius * cumulativeScale;

                    if(!framesHidden){
                        ndx = _dx + Math.cos((Math.PI/2D - shape.pts[nextIndex].degrees) + nextCumulativeRotation) * shape.pts[nextIndex].radius * nextCumulativeScale;
                        ndy = _dy + Math.sin((Math.PI/2D - shape.pts[nextIndex].degrees) + nextCumulativeRotation) * shape.pts[nextIndex].radius * nextCumulativeScale;

                        putLine(dx, dy, ndx, ndy, cumulativeOpacity/scaleDownMultiplier); //TODO proper transparent lines?
                    }
                    if(!trailsHidden && d < iterations-1)
                        putPixel(dx, dy, shape.pts[randomIndex].opacity);
                    if(!spokesHidden)
                        putLine(_dx, _dy, dx, dy, cumulativeOpacity/scaleDownMultiplier);
                    if(imgSamples)
                        putImgSample(dx, dy, cumulativeRotation, cumulativeScale, cumulativeOpacity, shape.pts[randomIndex], scaleDownMultiplier);
                }
                if(!leavesHidden)
                    putPixel(dx, dy, cumulativeOpacity);
            }

            if(!ptsHidden){
                for(int a = 0; a < shape.pointsInUse; a++){
                   drawPtDot(a);
                }
                if(!centerHidden || ctrlDown || shiftDown)
                    drawPtDot(-1);
            }
        }
    }

    public void drawPtDot(int pointIndex){
        int pointx1;
        int pointy1;

        if(pointIndex==-1){//center pt
            pointx1 = (int)shape.centerx;
            pointy1 = (int)shape.centery;
        }else{
            pointx1 = (int)shape.pts[pointIndex].x;
            pointy1 = (int)shape.pts[pointIndex].y;
        }

        if(pointx1 > screenwidth - 2)
            pointx1 = screenwidth - 2;
        if(pointy1 > screenheight - 2)
            pointy1 = screenheight - 2;
        if(pointx1 < 0)
            pointx1 = 0;
        if(pointy1 < 0)
            pointy1 = 0;
        if(pointIndex == pointselected){
            pixels[pointx1 + pointy1 * screenwidth] = 0xff00ff00;
            pixels[pointx1 + 1 + pointy1 * screenwidth] = 0xff00ff00;
            pixels[pointx1 + (pointy1 + 1) * screenwidth] = 0xff00ff00;
            pixels[pointx1 + 1 + (pointy1 + 1) * screenwidth] = 0xff00ff00;
        } else if(pointIndex != -1){ //non selected non central pt
            pixels[pointx1 + pointy1 * screenwidth] = 0xffff0000;
            pixels[pointx1 + 1 + pointy1 * screenwidth] = 0xffff0000;
            pixels[pointx1 + (pointy1 + 1) * screenwidth] = 0xffff0000;
            pixels[pointx1 + 1 + (pointy1 + 1) * screenwidth] = 0xffff0000;
        } else { //central pt
            pixels[pointx1 + pointy1 * screenwidth] = 0xff00ffff;
            pixels[pointx1 + 1 + pointy1 * screenwidth] = 0xff00ffff;
            pixels[pointx1 + (pointy1 + 1) * screenwidth] = 0xff00ffff;
            pixels[pointx1 + 1 + (pointy1 + 1) * screenwidth] = 0xff00ffff;
        }
    }

    public Image loadImage(String name){
        try{
            //URL theImgURL = new URL("file:/C:/Users/user/workspace/instant-ifs/img/" + name);

            URL theImgURL = new URL("file:/C:/Users/Labrats/Documents/GitHub/instant-ifs/img/" + name);
            return ImageIO.read(theImgURL);
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void mouseClicked(MouseEvent mouseevent){
    }

    public void mousePressed(MouseEvent e){
        mousemode = e.getButton();

        mousex = e.getX();
        mousey = e.getY();
        findSelectedPoint();

        if(e.getClickCount()==2){
            if(mousemode == 1){ //add point w/ double click
                shape.addPoint(mousex, mousey);
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
            shape.updateCenter();

            if(ctrlDown || shiftDown){
                shape.saveState();
                startDragPX = shape.centerx;
                startDragPY = shape.centery;
                startDragDist = shape.distance(startDragX - shape.centerx, startDragY - shape.centery);
                startDragAngle = 0 + Math.atan2(startDragX - shape.centerx, startDragY - shape.centery);
                startDragScale = 1.0;
            }else{
                startDragPX = selectedPt.x;
                startDragPY = selectedPt.y;
                startDragDist = shape.distance(startDragX - selectedPt.x, startDragY - selectedPt.y);
                startDragAngle = selectedPt.rotation + Math.atan2(startDragX - selectedPt.x, startDragY - selectedPt.y);
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
            if(ctrlDown){
                for(int i=0; i<shape.pointsInUse; i++){
                    shape.pts[i].x = shape.pts[i].savedx + (e.getX() - startDragX);
                    shape.pts[i].y = shape.pts[i].savedy + (e.getY() - startDragY);
                }
                shape.centerx = startDragPX + (e.getX() - startDragX);
                shape.centery = startDragPY + (e.getY() - startDragY);
            }else if(shiftDown){ //move the center
                shape.centerx = startDragPX + (e.getX() - startDragX);
                shape.centery = startDragPY + (e.getY() - startDragY);
            }else{ //move a single point
                selectedPt.x = startDragPX + (e.getX() - startDragX);
                selectedPt.y = startDragPY + (e.getY() - startDragY);
            }
        }
        else if(mousemode == 3){ //right click to rotate point/set
            setCursor (Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));

            if(ctrlDown){ //rotate the set
                double rotationDelta = (Math.atan2(e.getX() - shape.centerx , e.getY() - shape.centery )- startDragAngle);
                double scaleDelta = shape.distance(e.getX() - shape.centerx , e.getY() - shape.centery )/startDragDist;

                for(int i=0; i<shape.pointsInUse; i++){
                    shape.pts[i].x = shape.centerx + scaleDelta * shape.pts[i].savedradius*Math.cos(Math.PI / 2 - shape.pts[i].saveddegrees - rotationDelta);
                    shape.pts[i].y = shape.centery + scaleDelta * shape.pts[i].savedradius*Math.sin(Math.PI / 2 - shape.pts[i].saveddegrees - rotationDelta);
                }
            }else if(shiftDown){ //rotate all points in unison
                double rotationDelta = (Math.atan2(e.getX() - shape.centerx, e.getY() - shape.centery)- startDragAngle);
                double scaleDelta = shape.distance(e.getX() - shape.centerx, e.getY() - shape.centery)/startDragDist;

                for(int i=0; i<shape.pointsInUse; i++){
                    shape.pts[i].rotation = shape.pts[i].savedrotation + (Math.PI * 2 - rotationDelta);
                    shape.pts[i].scale = shape.pts[i].savedscale*scaleDelta;
                }
            }else{ //move a single point
                double rotationDelta = (Math.atan2(e.getX() - selectedPt.x, e.getY() - selectedPt.y)- startDragAngle);
                double scaleDelta = shape.distance(e.getX() - selectedPt.x, e.getY() - selectedPt.y)/startDragDist;

                selectedPt.rotation = Math.PI * 2 - rotationDelta;
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
        if(e.getKeyChar() == 'a')
            antiAliasing = !antiAliasing;
        if(e.getKeyChar() == 'l')
            leavesHidden = !leavesHidden;
        if(e.getKeyChar() == 's')
            spokesHidden = !spokesHidden;
        if(e.getKeyChar() == 'c')
            centerHidden = !centerHidden;
        if(e.getKeyChar() == 'f')
            framesHidden = !framesHidden;
        if(e.getKeyChar() == 'q')
            imgSamples = !imgSamples;
        if(e.getKeyChar() == 't')
            trailsHidden = !trailsHidden;
        if(e.getKeyChar() == 'i')
            infoHidden = !infoHidden;
        if(e.getKeyChar() == 'g')
            guidesHidden = !guidesHidden;
        if(e.getKeyChar() == 'p')
            ptsHidden = !ptsHidden;
        if(e.getKeyChar() == 'm')
            sampletotal += 100;
        if(e.getKeyChar() == 'n' && sampletotal > 1)
            sampletotal -= 100;
        clearframe();
        gamefunc();
    }

    public void focusGained(FocusEvent focusevent){}
    public void focusLost(FocusEvent focusevent){}
}
