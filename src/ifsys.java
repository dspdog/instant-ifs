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

    int mousex;
    int mousey;
    int sampletotal;
    int iterations;
    int maxPoints;

    ifsShape shape;
    int pointselected;

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

        sampletotal = 1000;
        iterations = 10;
        maxPoints = 100;
        mousemode = 0;

        shape = new ifsShape(maxPoints);
    }

    public void findSelectedPoint(){
        double olddist = 1000D;
        for(int a = 0; a < shape.pointsInUse; a++)
        {
            double currentdist = distance2((double)mousex - shape.pts[a].x, (double)mousey - shape.pts[a].y);
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
            shape.pointsInUse = 3;
            iterations = 9;
            sampletotal = 500;
            shape.pts[0].x = 320D;
            shape.pts[0].y = 160D;
            shape.pts[0].scale = 0.5D;
            shape.pts[1].x = 420D;
            shape.pts[1].y = 160D + 100D * Math.sqrt(3D);
            shape.pts[1].scale = 0.5D;
            shape.pts[2].x = 220D;
            shape.pts[2].y = 160D + 100D * Math.sqrt(3D);
            shape.pts[2].scale = 0.5D;
            break;
        }
        shape.updateCenter();
    }

    public void update(Graphics gr){
        paint(gr);
    }

    public void paint(Graphics gr){
        rg.drawImage(createImage(new MemoryImageSource(screenwidth, screenheight, pixels, 0, screenwidth)), 0, 0, screenwidth, screenheight, this);
        if(!infoHidden){
            rg.setColor(Color.white);
            rg.drawString("Point " + String.valueOf(pointselected + 1), 5, 15);
            rg.drawString("X: " + String.valueOf((double)(int)(shape.pts[pointselected].x * 1000D) / 1000D), 5, 30);
            rg.drawString("Y: " + String.valueOf((double)(int)(shape.pts[pointselected].y * 1000D) / 1000D), 5, 45);
            rg.drawString("Scale (;' [] -=): " + String.valueOf((double)(int)(shape.pts[pointselected].scale * 1000D) / 1000D), 5, 60);
            rg.drawString("Rotation (kl op 90): " + String.valueOf((double)(int)((((shape.pts[pointselected].rotation / Math.PI) * 180D + 36000000D) % 360D) * 1000D) / 1000D), 5, 75);
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
        for(int a = 0; a < shape.pointsInUse; a++){
            shape.pts[a].degrees = Math.atan2(shape.pts[a].x - shape.centerx, shape.pts[a].y - shape.centery);
            shape.pts[a].radius = distance2(shape.pts[a].x - shape.centerx, shape.pts[a].y - shape.centery);
        }

        if(shape.pointsInUse != 0){
            for(int a = 0; a < sampletotal; a++){
                int c = (int)(Math.random() * (double) shape.pointsInUse);
                double dx = shape.pts[c].x;
                double dy = shape.pts[c].y;
                double e = 1.0D;
                double extra = shape.pts[c].rotation;
                for(int d = 1; d < iterations; d++){
                    c = (int)(Math.random() * (double) shape.pointsInUse);
                    e *= shape.pts[c].scale;
                    extra += shape.pts[c].rotation;
                    dx += Math.cos((Math.PI/2D - shape.pts[c].degrees) + extra) * shape.pts[c].radius * e;
                    dy += Math.sin((Math.PI/2D - shape.pts[c].degrees) + extra) * shape.pts[c].radius * e;
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
                for(int a = 0; a < shape.pointsInUse; a++){
                   drawPtDot(a);
                }
            }
        }
    }

    public void drawPtDot(int pointIndex){
        int pointx1 = (int)shape.pts[pointIndex].x;
        int pointy1 = (int)shape.pts[pointIndex].y;
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
            startDragPX = shape.pts[pointselected].x;
            startDragPY = shape.pts[pointselected].y;
            startDragDist = distance2(arg0.getX() - shape.pts[pointselected].x, arg0.getY() - shape.pts[pointselected].y);
            startDragAngle = shape.pts[pointselected].rotation + Math.atan2(arg0.getX() - shape.pts[pointselected].x, arg0.getY() - shape.pts[pointselected].y);
            startDragScale = shape.pts[pointselected].scale;

            requestFocus();
            shape.updateCenter();
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
            shape.pts[pointselected].x = startDragPX + (e.getX() - startDragX);
            shape.pts[pointselected].y = startDragPY + (e.getY() - startDragY);
        }
        else if(mousemode == 3){ //right click to rotate point
            setCursor (Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
            shape.pts[pointselected].rotation = Math.PI * 2 - (Math.atan2(e.getX() - shape.pts[pointselected].x, e.getY() - shape.pts[pointselected].y)- startDragAngle);
            shape.pts[pointselected].scale = startDragScale*distance2(e.getX() - shape.pts[pointselected].x, e.getY() - shape.pts[pointselected].y)/startDragDist;
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
