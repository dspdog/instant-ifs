import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;

public class ifsys extends Applet
    implements MouseListener, MouseMotionListener, KeyListener, FocusListener
{
    mainthread game;
    boolean quit;
    int screenwidth;
    int screenheight;
    double pixelsData[];
    double dataMax = 0;
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
        boolean ptsHidden;
        int sampletotal;
        int iterations;
        int pointselected;
        boolean ctrlDown;
        int mousex;
        int mousey;

    ifsShape shape;
    int maxPoints;
    int maxLineLength;

    //drag vars
        int mousemode; //current mouse button
        double startDragX;
        double startDragY;
        double startDragPX;
        double startDragPY;
        double startDragDist;
        double startDragAngle;
        double startDragScale;

    String presetstring;
    int preset;

    public ifsys(){
        samplesThisFrame=0;
        oneSecondAgo =0;
        framesThisSecond = 0;
        ctrlDown=false;
        game = new mainthread();
        quit = false;
        antiAliasing = true;
        framesHidden = true;
        centerHidden = false;
        spokesHidden = true;
        trailsHidden = true;
        leavesHidden = false;
        infoHidden = false;
        ptsHidden = false;
        screenwidth = 1024;
        screenheight = 1024;
        pixels = new int[screenwidth * screenheight];
        pixelsData = new double[screenwidth * screenheight];
        sampletotal = 1000;
        iterations = 8;
        mousemode = 0;
        samplesNeeded = 1;
        maxLineLength = screenwidth;
        maxPoints = 100;
        shape = new ifsShape(maxPoints);
    }

    public void findSelectedPoint(){
        double olddist = 1000D;
        for(int a = 0; a < shape.pointsInUse; a++)
        {
            double currentdist = shape.distance((double) mousex - shape.pts[a].x, (double) mousey - shape.pts[a].y);
            if(currentdist < olddist){
                olddist = currentdist;
                pointselected = a;
            }
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
                catch(InterruptedException interruptedexception) { }
        }

        public mainthread(){
        }
    }

    public void start(){
        setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        render = createImage(screenwidth, screenheight);
        rg = render.getGraphics();
        presetstring = getParameter("preset");
        preset = 1;//Integer.parseInt(presetstring);
        clearframe();
        game.start();
        shape.setToPreset(1);
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
        if(!infoHidden){
            rg.setColor(Color.white);
            rg.drawString("Point " + String.valueOf(pointselected + 1), 5, 15);
            rg.drawString("X: " + String.valueOf((double)(int)(shape.pts[pointselected].x * 1000D) / 1000D), 5, 30);
            rg.drawString("Y: " + String.valueOf((double)(int)(shape.pts[pointselected].y * 1000D) / 1000D), 5, 45);
            rg.drawString("Scale ([] -=): " + String.valueOf((double)(int)(shape.pts[pointselected].scale * 1000D) / 1000D), 5, 60);
            rg.drawString("Rotation: " + String.valueOf((double)(int)((((shape.pts[pointselected].rotation / Math.PI) * 180D + 36000000D) % 360D) * 1000D) / 1000D), 5, 75);
            rg.drawString("Iterations (. /): " + String.valueOf(iterations), 5, 90);
            rg.drawString("Samples (nm): " + String.valueOf(sampletotal), 4, 105);
            rg.drawString("Expected Done %" + String.valueOf((int)Math.min(100*samplesThisFrame/samplesNeeded/Math.E, 100)), 5, 120); //TODO is dividing by E the right thing to do here?
            rg.drawString("FPS " + String.valueOf(fps), 5, 135);
        }
        gr.drawImage(render, 0, 0, screenwidth, screenheight, this);
    }

    public void generatePixels(){
        //System.out.println(dataMax);
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

    public boolean putPixel(double x, double y, boolean isPartOfLine){
        double decX, decY; //decimal parts of coordinates

        if(x < (double)(screenwidth - 1) &&
            y < (double)(screenheight - 1) &&
            x > 0.0D && y > 0.0D){

            decX = x - Math.floor(x);
            decY = y - Math.floor(y);

            if(antiAliasing){
                //each point contributes to 4 pixels
                if(isPartOfLine){//line contributions are clamped not added
                    pixelsData[(int)(x) + (int)(y) * screenwidth]=Math.max(dataMax*(1.0-decX)*(1.0-decY), pixelsData[(int)(x) + (int)(y) * screenwidth]);
                    pixelsData[(int)(x+1) + (int)(y) * screenwidth]=Math.max(dataMax*decX*(1.0-decY),pixelsData[(int)(x+1) + (int)(y) * screenwidth]);
                    pixelsData[(int)(x) + (int)(y+1) * screenwidth]=Math.max(dataMax*decY*(1.0-decX),pixelsData[(int)(x) + (int)(y+1) * screenwidth]);
                    pixelsData[(int)(x+1) + (int)(y+1) * screenwidth]=Math.max(dataMax*decY*decX,pixelsData[(int)(x+1) + (int)(y+1) * screenwidth]);
                }else{
                    pixelsData[(int)(x) + (int)(y) * screenwidth]+=(1.0-decX)*(1.0-decY);
                    pixelsData[(int)(x+1) + (int)(y) * screenwidth]+=decX*(1.0-decY);
                    pixelsData[(int)(x) + (int)(y+1) * screenwidth]+=decY*(1.0-decX);
                    pixelsData[(int)(x+1) + (int)(y+1) * screenwidth]+=decY*decX;
                }
                if(dataMax<pixelsData[(int)x + (int)y * screenwidth]){dataMax = pixelsData[(int)x + (int)y * screenwidth];}
            }else{
                pixelsData[(int)(x) + (int)(y) * screenwidth]=1;
            }
            if(!isPartOfLine){
                samplesThisFrame++;
            }

            return true; //pixel is in screen bounds
        }else{
            return false; //pixel outside of screen bounds
        }

    }

    public void putLine(double x0, double y0, double x1, double y1){
        double steps = (int)shape.distance(x0-x1, y0-y1);
        double dx, dy;

        boolean startedInScreen = false;

        if(steps>maxLineLength){steps=maxLineLength;}

        samplesThisFrame++;

        for(int i=0; i<steps; i++){
            dx = x0 + i*(x1-x0)/steps;
            dy = y0 + i*(y1-y0)/steps;

            if(putPixel(dx, dy, true)){ //stop drawing if pixel is outside bounds
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
                        putLine(shape.centerx, shape.centery, shape.pts[a].x, shape.pts[a].y);
                    }
                }

                if(!framesHidden){ //center outline
                    for(int a=0; a<shape.pointsInUse; a++){
                        int nextPt = (a+1)%shape.pointsInUse;
                        putLine(shape.pts[a].x, shape.pts[a].y, shape.pts[nextPt].x, shape.pts[nextPt].y);
                    }
                }
            }

            for(int a = 0; a < sampletotal; a++){
                int c = (int)(Math.random() * (double) shape.pointsInUse);
                int nextPt = (c+1)%shape.pointsInUse;
                double dx = shape.pts[c].x;
                double dy = shape.pts[c].y;
                double ndx;
                double ndy;
                double _dx;
                double _dy;
                double e = 1.0D;
                double nextE = 1.0D;
                double extra = shape.pts[c].rotation;
                double nextExtra = shape.pts[c].rotation;

                for(int d = 0; d < iterations; d++){
                    c = (int)(Math.random() * (double) shape.pointsInUse);
                    nextPt = (c+1)%shape.pointsInUse;

                    nextE = e*shape.pts[nextPt].scale;
                    nextExtra = extra + shape.pts[nextPt].rotation;

                    e *= shape.pts[c].scale;
                    extra += shape.pts[c].rotation;

                    _dx = dx;
                    _dy = dy;
                    dx += Math.cos((Math.PI/2D - shape.pts[c].degrees) + extra) * shape.pts[c].radius * e;
                    dy += Math.sin((Math.PI/2D - shape.pts[c].degrees) + extra) * shape.pts[c].radius * e;

                    if(!framesHidden){
                        ndx = _dx + Math.cos((Math.PI/2D - shape.pts[nextPt].degrees) + nextExtra) * shape.pts[nextPt].radius * nextE;
                        ndy = _dy + Math.sin((Math.PI/2D - shape.pts[nextPt].degrees) + nextExtra) * shape.pts[nextPt].radius * nextE;

                        putLine(dx, dy, ndx, ndy);
                    }
                    if(!trailsHidden && d < iterations-1)
                        putPixel(dx, dy, false);
                    if(!spokesHidden)
                        putLine(_dx, _dy, dx, dy);
                }
                if(!leavesHidden)
                    putPixel(dx, dy, false);
            }

            if(!ptsHidden){
                for(int a = 0; a < shape.pointsInUse; a++){
                   drawPtDot(a);
                }
                if(!centerHidden || ctrlDown)
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

    public void mouseClicked(MouseEvent mouseevent){
    }



    public void mousePressed(MouseEvent arg0){
        mousemode = arg0.getButton();

        mousex = arg0.getX();
        mousey = arg0.getY();
        findSelectedPoint();

        if(arg0.getClickCount()==2){
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
            startDragX = arg0.getX();
            startDragY = arg0.getY();
            shape.updateCenter();

            if(ctrlDown){
                shape.saveState();
                startDragPX = shape.centerx;
                startDragPY = shape.centery;
                startDragDist = shape.distance(startDragX - shape.centerx, startDragY - shape.centery);
                startDragAngle = 0 + Math.atan2(startDragX - shape.centerx, startDragY - shape.centery);
                startDragScale = 1.0; //shape.pts[pointselected].scale;
            }else{
                startDragPX = shape.pts[pointselected].x;
                startDragPY = shape.pts[pointselected].y;
                startDragDist = shape.distance(startDragX - shape.pts[pointselected].x, startDragY - shape.pts[pointselected].y);
                startDragAngle = shape.pts[pointselected].rotation + Math.atan2(startDragX - shape.pts[pointselected].x, startDragY - shape.pts[pointselected].y);
                startDragScale = shape.pts[pointselected].scale;
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
            if(ctrlDown){ //move the set -- need "startDragX" per pt
                for(int i=0; i<shape.pointsInUse; i++){
                    shape.pts[i].x = shape.pts[i].savedx + (e.getX() - startDragX);
                    shape.pts[i].y = shape.pts[i].savedy + (e.getY() - startDragY);
                }
            }else{ //move a single point
                shape.pts[pointselected].x = startDragPX + (e.getX() - startDragX);
                shape.pts[pointselected].y = startDragPY + (e.getY() - startDragY);
            }
        }
        else if(mousemode == 3){ //right click to rotate point/set
            setCursor (Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));

            if(ctrlDown){ //rotate the set
                double rotationDelta = (Math.atan2(e.getX() - shape.centerx, e.getY() - shape.centery)- startDragAngle);
                double scaleDelta = shape.distance(e.getX() - shape.centerx, e.getY() - shape.centery)/startDragDist;

                for(int i=0; i<shape.pointsInUse; i++){
                    shape.pts[i].x = shape.centerx + scaleDelta * shape.pts[i].savedradius*Math.cos(Math.PI / 2 - shape.pts[i].saveddegrees - rotationDelta);
                    shape.pts[i].y = shape.centery + scaleDelta * shape.pts[i].savedradius*Math.sin(Math.PI / 2 - shape.pts[i].saveddegrees - rotationDelta);
                }
            }else{ //move a single point
                double rotationDelta = (Math.atan2(e.getX() - shape.pts[pointselected].x, e.getY() - shape.pts[pointselected].y)- startDragAngle);
                double scaleDelta = shape.distance(e.getX() - shape.pts[pointselected].x, e.getY() - shape.pts[pointselected].y)/startDragDist;

                shape.pts[pointselected].rotation = Math.PI * 2 - rotationDelta;
                shape.pts[pointselected].scale = startDragScale*scaleDelta;
            }
        }

        shape.updateCenter();
        clearframe();
        gamefunc();
    }

    public void mouseMoved(MouseEvent e){
    }
    public void keyTyped(KeyEvent e){
    }

    public void keyPressed(KeyEvent e){
        if(e.getKeyCode()==KeyEvent.VK_CONTROL)
            ctrlDown=true;
        if(e.getKeyChar() == '=')
            shape.pts[pointselected].scale *= 1.01D;
        if(e.getKeyChar() == '-')
            shape.pts[pointselected].scale *= 0.98999999999999999D;
        if(e.getKeyChar() == ']'){
            for(int a = 0; a < shape.pointsInUse; a++)
                shape.pts[a].scale *= 1.01D;

        }
        if(e.getKeyChar() == '['){
            for(int a = 0; a < shape.pointsInUse; a++)
                shape.pts[a].scale *= 0.98999999999999999D;
        }
        shape.updateCenter();
        clearframe();
        gamefunc();
    }

    public void keyReleased(KeyEvent e){
        if(e.getKeyCode()==KeyEvent.VK_CONTROL)
            ctrlDown=false;
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
        if(e.getKeyChar() == 't')
            trailsHidden = !trailsHidden;
        if(e.getKeyChar() == 'i')
            infoHidden = !infoHidden;
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
