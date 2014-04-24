import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;

public class ifsys extends Applet
    implements MouseListener, MouseMotionListener, KeyListener, FocusListener
{
    mainthread game;
    boolean quit;
    boolean infoHidden;
    boolean ptsHidden;
    int screenwidth;
    int screenheight;
    int pixels[];
    Image render;
    Graphics rg;
    double pi;
    double pi2;
    int mousex;
    int mousey;
    int sampletotal;
    int iterations;
    int maxPoints;

    //point vars
        ifsPt pts[];
        int pointnumber;
        int pointselected;
        double centerx;
        double centery;

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
        game = new mainthread();
        quit = false;
        infoHidden = false;
        ptsHidden = false;
        screenwidth = 1024;
        screenheight = 1024;
        pixels = new int[screenwidth * screenheight];
        pi = 3.1415926535897931D;
        pi2 = pi / 2D;
        sampletotal = 1000;
        iterations = 10;
        maxPoints = 100;
        mousemode = 0;

        pts = new ifsPt[maxPoints];
        for(int a=0; a< maxPoints; a++){
            pts[a] = new ifsPt();
        }

        pointnumber = 0;
        centerx = screenwidth / 2;
        centery = screenheight / 2;
    }

    public void findSelectedPoint(){
        double olddist = 1000D;
        for(int a = 0; a < pointnumber; a++)
        {
            double currentdist = distance2((double)mousex - pts[a].x, (double)mousey - pts[a].y);
            if(currentdist < olddist){
                olddist = currentdist;
                pointselected = a;
            }
        }
    }

    public void addPoint(){
        pts[pointnumber].x = mousex;
        pts[pointnumber].y = mousey;
        pts[pointnumber].scale = 0.5D;
        pts[pointnumber].rotation = 0.0D;
        pointnumber++;
        updateCenter();
        clearframe();
        gamefunc();
    }

    public void deletePoint(){
        for(int a = pointselected; a < pointnumber; a++){
            pts[a].x = pts[a + 1].x;
            pts[a].y = pts[a + 1].y;

            pts[a].scale = pts[a + 1].scale;
            pts[a].rotation = pts[a + 1].rotation;
        }

        pts[pointnumber].x = 0.0D;
        pts[pointnumber].y = 0.0D;

        pts[pointnumber].scale = 0.5D;
        pts[pointnumber].rotation = 0.0D;
        pointnumber--;

        updateCenter();
        clearframe();
        gamefunc();
    }

    public void updateCenter(){
        double x = 0;
        double y = 0;

        if(pointnumber != 0){
            for(int a = 0; a < pointnumber; a++){
                x += pts[a].x;
                y +=  pts[a].y;
            }

            centerx = x / pointnumber;
            centery = y / pointnumber;
        } else{
            centerx = pts[0].x;
            centery = pts[0].y;
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

    public class ifsPt{
        public double x;
        public double y;
        public double scale;
        public double degrees;
        public double radius;
        public double rotation;

        public ifsPt(){
            x = 0D;
            y = 0D;
            scale = 0.5D;
            rotation = 0.0D;

            degrees = 0D;
            radius = 1D;
        }
    }

    public double distance2(double x2, double y2){
        return Math.sqrt(x2 * x2 + y2 * y2);
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
        settopreset();
        game.start();
    }

    public void settopreset(){
        switch(preset){
        case 1: // '\001'
            pointnumber = 3;
            iterations = 9;
            sampletotal = 500;
            pts[0].x = 320D;
            pts[0].y = 160D;
            pts[0].scale = 0.5D;
            pts[1].x = 420D;
            pts[1].y = 160D + 100D * Math.sqrt(3D);
            pts[1].scale = 0.5D;
            pts[2].x = 220D;
            pts[2].y = 160D + 100D * Math.sqrt(3D);
            pts[2].scale = 0.5D;
            break;
        }
        updateCenter();
    }

    public void update(Graphics gr){
        paint(gr);
    }

    public void paint(Graphics gr){
        rg.drawImage(createImage(new MemoryImageSource(screenwidth, screenheight, pixels, 0, screenwidth)), 0, 0, screenwidth, screenheight, this);
        if(!infoHidden){
            rg.setColor(Color.white);
            rg.drawString("Point " + String.valueOf(pointselected + 1), 5, 15);
            rg.drawString("X: " + String.valueOf((double)(int)(pts[pointselected].x * 1000D) / 1000D), 5, 30);
            rg.drawString("Y: " + String.valueOf((double)(int)(pts[pointselected].y * 1000D) / 1000D), 5, 45);
            rg.drawString("Scale (;' [] -=): " + String.valueOf((double)(int)(pts[pointselected].scale * 1000D) / 1000D), 5, 60);
            rg.drawString("Rotation (kl op 90): " + String.valueOf((double)(int)((((pts[pointselected].rotation / pi) * 180D + 36000000D) % 360D) * 1000D) / 1000D), 5, 75);
            rg.drawString("Iterations (. /): " + String.valueOf(iterations), 5, screenheight - 7);
            rg.drawString("Samples (nm): " + String.valueOf(sampletotal), 4, screenheight - 37);
        }
        gr.drawImage(render, 0, 0, screenwidth, screenheight, this);
    }

    public void clearframe(){
        for(int a = 0; a < screenwidth * screenheight; a++)
            pixels[a] = 0xff000000;
    }

    public void gamefunc(){
        for(int a = 0; a < pointnumber; a++){
            pts[a].degrees = Math.atan2(pts[a].x - centerx, pts[a].y - centery);
            pts[a].radius = distance2(pts[a].x - centerx, pts[a].y - centery);
        }

        if(pointnumber != 0){
            for(int a = 0; a < sampletotal; a++){
                int c = (int)(Math.random() * (double)pointnumber);
                double dx = pts[c].x;
                double dy = pts[c].y;
                double e = 1.0D;
                double extra = pts[c].rotation;
                for(int d = 1; d < iterations; d++){
                    c = (int)(Math.random() * (double)pointnumber);
                    e *= pts[c].scale;
                    extra += pts[c].rotation;
                    dx += Math.cos((pi2 - pts[c].degrees) + extra) * pts[c].radius * e;
                    dy += Math.sin((pi2 - pts[c].degrees) + extra) * pts[c].radius * e;
                }

                if(dx > (double)(screenwidth - 1))
                    dx = screenwidth - 1;
                if(dy > (double)(screenheight - 1))
                    dy = screenheight - 1;
                if(dx < 0.0D)
                    dx = 0.0D;
                if(dy < 0.0D)
                    dy = 0.0D;
                pixels[(int)dx + (int)dy * screenwidth] = -1;
            }

            if(!ptsHidden){
                for(int a = 0; a < pointnumber; a++){
                   drawPtDot(a);
                }
            }
        }
    }

    public void drawPtDot(int pointIndex){
        int pointx1 = (int)pts[pointIndex].x;
        int pointy1 = (int)pts[pointIndex].y;
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
        } else{
            pixels[pointx1 + pointy1 * screenwidth] = 0xffff0000;
            pixels[pointx1 + 1 + pointy1 * screenwidth] = 0xffff0000;
            pixels[pointx1 + (pointy1 + 1) * screenwidth] = 0xffff0000;
            pixels[pointx1 + 1 + (pointy1 + 1) * screenwidth] = 0xffff0000;
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
                addPoint();
            }else if(mousemode == 3){ //remove point w/ double right click
                deletePoint();
            }
        }else{
            startDragX = arg0.getX();
            startDragY = arg0.getY();
            startDragPX = pts[pointselected].x;
            startDragPY = pts[pointselected].y;
            startDragDist = distance2(arg0.getX() - pts[pointselected].x, arg0.getY() - pts[pointselected].y);
            startDragAngle = pts[pointselected].rotation + Math.atan2(arg0.getX() - pts[pointselected].x, arg0.getY() - pts[pointselected].y);
            startDragScale = pts[pointselected].scale;

            requestFocus();
            updateCenter();
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
        if(mousemode == 1){ //left click to move a point
            setCursor (Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            pts[pointselected].x = startDragPX + (e.getX() - startDragX);
            pts[pointselected].y = startDragPY + (e.getY() - startDragY);
        }
        else if(mousemode == 3){ //right click to rotate point
            setCursor (Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
            pts[pointselected].rotation = pi * 2 - (Math.atan2(e.getX() - pts[pointselected].x, e.getY() - pts[pointselected].y)- startDragAngle);
            pts[pointselected].scale = startDragScale*distance2(e.getX() - pts[pointselected].x, e.getY() - pts[pointselected].y)/startDragDist;
        }

        updateCenter();
        clearframe();
        gamefunc();
    }

    public void mouseMoved(MouseEvent e){
    }
    public void keyTyped(KeyEvent e){
    }

    public void keyPressed(KeyEvent e){
        if(e.getKeyChar() == '=')
            pts[pointselected].scale *= 1.01D;
        if(e.getKeyChar() == '-')
            pts[pointselected].scale *= 0.98999999999999999D;
        if(e.getKeyChar() == ']'){
            for(int a = 0; a < pointnumber; a++)
                pts[a].scale *= 1.01D;

        }
        if(e.getKeyChar() == '['){
            for(int a = 0; a < pointnumber; a++)
                pts[a].scale *= 0.98999999999999999D;
        }
        updateCenter();
        clearframe();
        gamefunc();
    }

    public void keyReleased(KeyEvent e){
        if(e.getKeyChar() == '/')
            iterations++;
        if(e.getKeyChar() == '.' && iterations > 1)
            iterations--;
        if(e.getKeyChar() == 'h')
            infoHidden = !infoHidden;
        if(e.getKeyChar() == 'g')
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
