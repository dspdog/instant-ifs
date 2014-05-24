import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.net.URL;

public class ifsys extends Panel
    implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, FocusListener, ActionListener,
        ItemListener
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

    Image sampleImage;

    //user params
        boolean framesHidden;
        boolean leavesHidden;
        boolean antiAliasing;
        boolean trailsHidden;
        boolean spokesHidden;
        boolean infoHidden;
        boolean imgSamples;
        boolean guidesHidden;
        boolean invertColors;
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
    double shapeArea;
    double shapeAreaDelta;

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
        spokesHidden = true;
        trailsHidden = true;
        leavesHidden = true;
        infoHidden = false;
        imgSamples = true;
        guidesHidden = false;
        invertColors = false;
        screenwidth = 1024;
        screenheight = 1024;
        pixels = new int[screenwidth * screenheight];
        samplePixels = new int[screenwidth * screenheight];
        pixelsData = new double[screenwidth * screenheight];
        sampletotal = 512;
        iterations = 8;
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

        MenuBar menuBar;
        Menu fileMenu, renderMenu, shapeMenu, guidesMenu, viewMenu;

        menuBar = new MenuBar();
        fileMenu = new Menu("File");
        renderMenu = new Menu("Render");
        shapeMenu = new Menu("Shape");
        guidesMenu = new Menu("Guides");
        viewMenu = new Menu("View");

        //RENDER MENU
            CheckboxMenuItem aaButton = new CheckboxMenuItem("Anti-Aliasing"); //anti-aliasing toggle
            aaButton.setState(is.antiAliasing);
            aaButton.addItemListener(is);
            renderMenu.add(aaButton);

            CheckboxMenuItem inButton = new CheckboxMenuItem("Invert"); //invert toggle
            inButton.setState(is.invertColors);
            inButton.addItemListener(is);
            renderMenu.add(inButton);

        //SHAPE MENU
            CheckboxMenuItem autoScaleButton = new CheckboxMenuItem("AutoScale Points"); //autoscale toggle
            autoScaleButton.setState(is.shape.autoScale);
            autoScaleButton.addItemListener(is);
            shapeMenu.add(autoScaleButton);

            CheckboxMenuItem imgButton = new CheckboxMenuItem("Img Samples"); //img samples toggle
            imgButton.setState(is.imgSamples);
            imgButton.addItemListener(is);
            shapeMenu.add(imgButton);

            CheckboxMenuItem leavesButton = new CheckboxMenuItem("Leaves"); //leaves toggle
            leavesButton.setState(!is.leavesHidden);
            leavesButton.addItemListener(is);
            shapeMenu.add(leavesButton);

            CheckboxMenuItem spokesButton = new CheckboxMenuItem("Spokes"); //spokes toggle
            spokesButton.setState(!is.spokesHidden);
            spokesButton.addItemListener(is);
            shapeMenu.add(spokesButton);

            CheckboxMenuItem framesButton = new CheckboxMenuItem("Frames"); //frames toggle
            framesButton.setState(!is.framesHidden);
            framesButton.addItemListener(is);
            shapeMenu.add(framesButton);

            CheckboxMenuItem trailsButton = new CheckboxMenuItem("Point Trails"); //trails toggle
            trailsButton.setState(!is.trailsHidden);
            trailsButton.addItemListener(is);
            shapeMenu.add(trailsButton);

        //GUIDES MENU
            CheckboxMenuItem infoButton = new CheckboxMenuItem("Info Box"); //info box toggle
            infoButton.setState(!is.infoHidden);
            infoButton.addItemListener(is);
            guidesMenu.add(infoButton);

            CheckboxMenuItem guidesButton = new CheckboxMenuItem("Point Markers"); //scale markers toggle
            guidesButton.setState(!is.guidesHidden);
            guidesButton.addItemListener(is);
            guidesMenu.add(guidesButton);

        menuBar.add(fileMenu);
        menuBar.add(renderMenu);
        menuBar.add(shapeMenu);
        menuBar.add(guidesMenu);
        menuBar.add(viewMenu);

        f.setMenuBar(menuBar);
    }

    public void init() {
        start();
        shape.updateCenter();
        clearframe();
        gamefunc();
    }


    public void findSelectedPoint(){
        pointselected = shape.getNearestPtIndex(mousex, mousey);
        selectedPt = shape.pts[pointselected];
    }

    public void actionPerformed(ActionEvent e) {

    }

    public void itemStateChanged(ItemEvent e) {
        //RENDER MENU
            if(e.getItem()=="Anti-Aliasing"){
                antiAliasing = e.getStateChange()==1;
            }
            if(e.getItem()=="Invert"){
                invertColors = e.getStateChange()==1;
            }
        //GUIDES MENU
            if(e.getItem()=="Info Box"){
                infoHidden = e.getStateChange()==2;
            }
            if(e.getItem()=="Point Markers"){
                guidesHidden = e.getStateChange()==2;
            }
        //SHAPE MENU
            if(e.getItem()=="AutoScale Points"){
                shape.autoScale = e.getStateChange()==1;
            }
            if(e.getItem()=="Img Samples"){
                imgSamples = e.getStateChange()==1;
            }
            if(e.getItem()=="Leaves"){
                leavesHidden = e.getStateChange()==2;
            }
            if(e.getItem()=="Frames"){
                framesHidden = e.getStateChange()==2;
            }
            if(e.getItem()=="Spokes"){
                spokesHidden = e.getStateChange()==2;
            }
            if(e.getItem()=="Point Trails"){
                spokesHidden = e.getStateChange()==2;
            }
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
        preset = 1;
        clearframe();
        game.start();
        shape.setToPreset(1);
        setSampleImg("serp.jpg");
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
        rg.drawImage(sampleImage, getWidth() - 50, 0, 50, 50, this);

        int circleWidth;

        rg.setColor(Color.blue);
        ifsPt thePt;

        if(!guidesHidden)
        for(int i=0;i<shape.pointsInUse;i++){
            if(i==pointselected){
                rg.setColor(Color.LIGHT_GRAY);
            }else{
                if(i==0){
                    rg.setColor(Color.BLUE);
                    rg.setPaintMode();
                }else{
                    rg.setColor(Color.darkGray);
                }
            }

            thePt = shape.pts[i];
            circleWidth = (int)(thePt.scale*thePt.radius*2);
            rg.drawOval((int)thePt.x-circleWidth/2, (int)thePt.y-circleWidth/2, circleWidth, circleWidth);
            rg.drawLine((int)thePt.x, (int)thePt.y, (int)(thePt.x + Math.sin(thePt.degrees-thePt.rotation)*thePt.scale*thePt.radius),
                                                    (int)(thePt.y + Math.cos(thePt.degrees-thePt.rotation)*thePt.scale*thePt.radius));
        }

        if(!infoHidden && pointselected>=0){
            rg.setColor(Color.white);
            rg.setColor(invertColors ? Color.black : Color.white);
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

            rg.drawString("Area " + String.valueOf((int)shapeArea), 5, 195);
            rg.drawString("AreaDelta " + String.valueOf((int)shapeAreaDelta), 5, 210);
            rg.drawString("DataMax " + String.valueOf((int)dataMax), 5, 225);
        }

        gr.drawImage(render, 0, 0, screenwidth, screenheight, this);
    }

    public void generatePixels(){
        double scaler = 255/dataMax;
        double area = 0;
        int scaledColor = 0;

        if(invertColors){
            for(int a = 0; a < screenwidth * screenheight; a++){
                int argb = 255;
                scaledColor = (int)(255-scaler*pixelsData[a]);
                argb = (argb << 8) + scaledColor;
                argb = (argb << 8) + scaledColor;
                argb = (argb << 8) + scaledColor;
                pixels[a] = argb;
                area+=scaler*pixelsData[a];
            }
        }else{
            for(int a = 0; a < screenwidth * screenheight; a++){
                int argb = 255;
                scaledColor = (int)(scaler*pixelsData[a]);
                argb = (argb << 8) + scaledColor;
                argb = (argb << 8) + scaledColor;
                argb = (argb << 8) + scaledColor;
                pixels[a] = argb;
                area+=scaler*pixelsData[a];
            }
        }

        shapeAreaDelta = area - shapeArea;
        shapeArea = area;
    }

    public void clearframe(){
        if(invertColors){
            for(int a = 0; a < screenwidth * screenheight; a++){
                pixels[a] = 0xffffffff;
                pixelsData[a] = 1;
            }
        }else{
            for(int a = 0; a < screenwidth * screenheight; a++){
                pixels[a] = 0xff000000;
                pixelsData[a] = 0;
            }
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
                if(alpha>0.49)
                pixelsData[(int)(x) + (int)(y) * screenwidth]=Math.max(pixelsData[(int)(x) + (int)(y) * screenwidth], 1);
            }

            samplesThisFrame++;

            return true; //pixel is in screen bounds
        }else{
            return false; //pixel outside of screen bounds
        }

    }

    public void setSampleImg(String filename){
        sampleImage = loadImage(filename);

        try {
            PixelGrabber grabber =
                    new PixelGrabber(sampleImage, 0, 0, -1, -1, false);

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
        double pointDegrees = Math.atan2(sampleX - sampleWidth/2, sampleY - sampleHeight/2)+cumulativeRotation+thePt.rotation-thePt.degrees;
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


            if(!spokesHidden){ //center spokes
                for(int a=0; a<shape.pointsInUse; a++){
                    putLine(shape.pts[0].x, shape.pts[0].y, shape.pts[a].x, shape.pts[a].y, shape.pts[a].opacity);
                }
            }

            if(!framesHidden){ //center outline
                for(int a=0; a<shape.pointsInUse; a++){
                    int nextPt = (a+1)%shape.pointsInUse;
                    putLine(shape.pts[a].x, shape.pts[a].y, shape.pts[nextPt].x, shape.pts[nextPt].y, shape.pts[nextPt].opacity);
                }
            }


            for(int a = 0; a < sampletotal; a++){
                int randomIndex = 0;
                int nextIndex=0;
                double dx = shape.pts[randomIndex].x;
                double dy = shape.pts[randomIndex].y;
                double ndx;
                double ndy;
                double _dx;
                double _dy;
                double cumulativeScale = 1.0; //shape.pts[randomIndex].scale;
                double nextCumulativeScale = 1.0D;
                double cumulativeRotation = 0; //shape.pts[randomIndex].rotation;
                double nextCumulativeRotation = 0; //shape.pts[randomIndex].rotation;
                double cumulativeOpacity = 1; //shape.pts[randomIndex].opacity;

                double scaleDownMultiplier = Math.pow(shape.pointsInUse,iterations-1); //this variable is used to tone down repeated pixels so leaves and branches are equally exposed

               // double centerScale = shape.pts[0].scale;
                //double centerRadius =100;

                for(int d = 0; d < iterations; d++){
                    scaleDownMultiplier/=shape.pointsInUse;

                    randomIndex = 1 + (int)(Math.random() * (double) (shape.pointsInUse-1));

                    if(d==0){randomIndex=0;}
                    else{
                        nextIndex = 1 + (randomIndex+1)%(shape.pointsInUse-1);

                        nextCumulativeScale = cumulativeScale*shape.pts[nextIndex].scale;
                        nextCumulativeRotation = cumulativeRotation + shape.pts[nextIndex].rotation;
                    }

                    _dx = dx;
                    _dy = dy;
                    if(d!=0){
                        dx += Math.cos((Math.PI/2D - shape.pts[randomIndex].degrees) + cumulativeRotation) * shape.pts[randomIndex].radius * cumulativeScale;
                        dy += Math.sin((Math.PI/2D - shape.pts[randomIndex].degrees) + cumulativeRotation) * shape.pts[randomIndex].radius * cumulativeScale;
                    }

                    if(!framesHidden && d!=0){
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
                    cumulativeScale *= shape.pts[randomIndex].scale/shape.pts[0].scale;
                    cumulativeRotation += shape.pts[randomIndex].rotation;
                    cumulativeOpacity *= shape.pts[randomIndex].opacity;

                }
                if(!leavesHidden)
                    putPixel(dx, dy, cumulativeOpacity);
            }
        }
    }

    public Image loadImage(String name){
        try{
            //URL theImgURL = new URL("file:/C:/Users/user/workspace/instant-ifs/img/" + name);file:/C:/Users/Labrats/Documents/GitHub/
            URL theImgURL = new URL("file:/C:/Users/user/workspace/instant-ifs/img/" + name);
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
                startDragPX = shape.pts[0].x;
                startDragPY = shape.pts[0].y;
                startDragDist = shape.distance(startDragX - shape.pts[0].x, startDragY - shape.pts[0].y);
                startDragAngle = 0 + Math.atan2(startDragX - shape.pts[0].x, startDragY - shape.pts[0].y);
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
            selectedPt.x = startDragPX + (e.getX() - startDragX);
            selectedPt.y = startDragPY + (e.getY() - startDragY);
        }
        else if(mousemode == 3){ //right click to rotate point/set
            setCursor (Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));

            if(ctrlDown){ //rotate the set
                double rotationDelta = (Math.atan2(e.getX() - shape.pts[0].x , e.getY() - shape.pts[0].y )- startDragAngle);
                double scaleDelta = shape.distance(e.getX() - shape.pts[0].x , e.getY() - shape.pts[0].y )/startDragDist;

                for(int i=1; i<shape.pointsInUse; i++){
                    shape.pts[i].x = shape.pts[0].x + scaleDelta * shape.pts[i].savedradius*Math.cos(Math.PI / 2 - shape.pts[i].saveddegrees - rotationDelta);
                    shape.pts[i].y = shape.pts[0].x + scaleDelta * shape.pts[i].savedradius*Math.sin(Math.PI / 2 - shape.pts[i].saveddegrees - rotationDelta);
                }
            }else if(shiftDown){ //rotate all points in unison
                double rotationDelta = (Math.atan2(e.getX() - shape.pts[0].x, e.getY() - shape.pts[0].y)- startDragAngle);
                double scaleDelta = shape.distance(e.getX() - shape.pts[0].x, e.getY() - shape.pts[0].y)/startDragDist;

                for(int i=1; i<shape.pointsInUse; i++){
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
