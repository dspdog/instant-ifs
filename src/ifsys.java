import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;

public class ifsys extends Applet
    implements MouseListener, MouseMotionListener, KeyListener, FocusListener
{
    public class mainthread extends Thread
    {

        public void run()
        {
            while(!quit) 
                try
                {
                    gamefunc();
                    repaint();
                    sleep(1L);
                }
                catch(InterruptedException interruptedexception) { }
        }

        public mainthread()
        {
        }
    }


    public ifsys()
    {
        game = new mainthread();
        quit = false;
        hidden = false;
        hidden2 = false;
        screenwidth = 1024;
        screenheight = 1024;
        pixels = new int[screenwidth * screenheight];
        pi = 3.1415926535897931D;
        pi2 = pi / 2D;
        sampletotal = 100;
        itertotal = 8;
        pointtotal = 100;

        mousemode = 0;

        pointx = new double[pointtotal];
        pointy = new double[pointtotal];
        pointscale = new double[pointtotal];
        pointdegrees = new double[pointtotal];
        pointradius = new double[pointtotal];
        pointrotation = new double[pointtotal];
        pointnumber = 0;
        centerx = screenwidth / 2;
        centery = screenheight / 2;
        movesize = 1.0D;
    }

    public double distance2(double x2, double y2)
    {
        return Math.sqrt(x2 * x2 + y2 * y2);
    }

    public void start()
    {
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

    public void settopreset()
    {
        switch(preset)
        {
        case 1: // '\001'
            pointnumber = 3;
            itertotal = 9;
            sampletotal = 500;
            pointx[0] = 320D;
            pointy[0] = 160D;
            pointscale[0] = 0.5D;
            pointx[1] = 420D;
            pointy[1] = 160D + 100D * Math.sqrt(3D);
            pointscale[1] = 0.5D;
            pointx[2] = 220D;
            pointy[2] = 160D + 100D * Math.sqrt(3D);
            pointscale[2] = 0.5D;
            break;

        case 2: // '\002'
            pointnumber = 4;
            itertotal = 6;
            sampletotal = 300;
            pointx[0] = 190D;
            pointy[0] = 120D;
            pointscale[0] = 0.33333333333333331D;
            pointx[1] = 430D;
            pointy[1] = 360D;
            pointscale[1] = 0.33333333333333331D;
            pointx[2] = 430D;
            pointy[2] = 120D;
            pointscale[2] = 0.33333333333333331D;
            pointx[3] = 190D;
            pointy[3] = 360D;
            pointscale[3] = 0.33333333333333331D;
            break;

        case 3: // '\003'
            pointnumber = 8;
            itertotal = 6;
            sampletotal = 2000;
            pointx[0] = 190D;
            pointy[0] = 120D;
            pointscale[0] = 0.33333333333333331D;
            pointx[1] = 430D;
            pointy[1] = 360D;
            pointscale[1] = 0.33333333333333331D;
            pointx[2] = 430D;
            pointy[2] = 120D;
            pointscale[2] = 0.33333333333333331D;
            pointx[3] = 190D;
            pointy[3] = 360D;
            pointscale[3] = 0.33333333333333331D;
            pointx[4] = 310D;
            pointy[4] = 120D;
            pointscale[4] = 0.33333333333333331D;
            pointx[5] = 310D;
            pointy[5] = 360D;
            pointscale[5] = 0.33333333333333331D;
            pointx[6] = 190D;
            pointy[6] = 240D;
            pointscale[6] = 0.33333333333333331D;
            pointx[7] = 430D;
            pointy[7] = 240D;
            pointscale[7] = 0.33333333333333331D;
            break;

        case 4: // '\004'
            pointnumber = 2;
            itertotal = 6;
            sampletotal = 200;
            pointx[0] = 430D;
            pointy[0] = 240D;
            pointscale[0] = 0.33333333333333331D;
            pointx[1] = 190D;
            pointy[1] = 240D;
            pointscale[1] = 0.33333333333333331D;
            break;

        case 5: // '\005'
            pointnumber = 7;
            itertotal = 6;
            sampletotal = 200;
            pointx[0] = Math.cos(0.0D) * 100D + (double)(screenwidth / 2);
            pointy[0] = Math.sin(0.0D) * 100D + (double)(screenheight / 2);
            pointscale[0] = 0.33333333333333331D;
            pointx[1] = Math.cos((60D * pi) / 180D) * 100D + (double)(screenwidth / 2);
            pointy[1] = Math.sin((60D * pi) / 180D) * 100D + (double)(screenheight / 2);
            pointscale[1] = 0.33333333333333331D;
            pointx[2] = Math.cos((120D * pi) / 180D) * 100D + (double)(screenwidth / 2);
            pointy[2] = Math.sin((120D * pi) / 180D) * 100D + (double)(screenheight / 2);
            pointscale[2] = 0.33333333333333331D;
            pointx[3] = Math.cos((180D * pi) / 180D) * 100D + (double)(screenwidth / 2);
            pointy[3] = Math.sin((180D * pi) / 180D) * 100D + (double)(screenheight / 2);
            pointscale[3] = 0.33333333333333331D;
            pointx[4] = Math.cos((240D * pi) / 180D) * 100D + (double)(screenwidth / 2);
            pointy[4] = Math.sin((240D * pi) / 180D) * 100D + (double)(screenheight / 2);
            pointscale[4] = 0.33333333333333331D;
            pointx[5] = Math.cos((300D * pi) / 180D) * 100D + (double)(screenwidth / 2);
            pointy[5] = Math.sin((300D * pi) / 180D) * 100D + (double)(screenheight / 2);
            pointscale[5] = 0.33333333333333331D;
            pointx[6] = screenwidth / 2;
            pointy[6] = screenheight / 2;
            pointscale[6] = 0.33333333333333331D;
            break;

        case 6: // '\006'
            pointrotation[0] = pi;
            pointrotation[1] = pi;
            pointrotation[2] = pi;
            preset = 1;
            settopreset();
            break;

        case 7: // '\007'
            pointnumber = 5;
            sampletotal = 500;
            pointx[0] = 320D;
            pointy[0] = 240D;
            pointscale[0] = 0.33333333333333331D;
            pointx[1] = 190D;
            pointy[1] = 240D;
            pointscale[1] = 0.33333333333333331D;
            pointx[2] = 450D;
            pointy[2] = 240D;
            pointscale[2] = 0.33333333333333331D;
            pointx[3] = 320D;
            pointy[3] = 370D;
            pointscale[3] = 0.33333333333333331D;
            pointx[4] = 320D;
            pointy[4] = 110D;
            pointscale[4] = 0.33333333333333331D;
            break;
        }
        if(preset != 0)
        {
            int x = 0;
            int y = 0;
            for(int a = 0; a < pointnumber; a++)
            {
                x = (int)((double)x + pointx[a]);
                y = (int)((double)y + pointy[a]);
            }

            centerx = x / pointnumber;
            centery = y / pointnumber;
        }
    }

    public void update(Graphics gr)
    {
        paint(gr);
    }

    public void paint(Graphics gr)
    {
        rg.drawImage(createImage(new MemoryImageSource(screenwidth, screenheight, pixels, 0, screenwidth)), 0, 0, screenwidth, screenheight, this);
        if(!hidden)
        {
            rg.setColor(Color.white);
            rg.drawString("Point " + String.valueOf(pointselected + 1), 5, 15);
            rg.drawString("X: " + String.valueOf((double)(int)(pointx[pointselected] * 1000D) / 1000D), 5, 30);
            rg.drawString("Y: " + String.valueOf((double)(int)(pointy[pointselected] * 1000D) / 1000D), 5, 45);
            rg.drawString("Scale (;' [] -=): " + String.valueOf((double)(int)(pointscale[pointselected] * 1000D) / 1000D), 5, 60);
            rg.drawString("Rotation (kl op 90): " + String.valueOf((double)(int)((((pointrotation[pointselected] / pi) * 180D + 36000000D) % 360D) * 1000D) / 1000D), 5, 75);
            rg.drawString("Iterations (. /): " + String.valueOf(itertotal), 5, screenheight - 7);
            rg.drawString("Sensitivity (1-6): " + String.valueOf((double)(int)(movesize * 1000D) / 1000D), 4, screenheight - 22);
            rg.drawString("Samples (nm): " + String.valueOf(sampletotal), 4, screenheight - 37);
        }
        gr.drawImage(render, 0, 0, screenwidth, screenheight, this);
    }

    public void clearframe()
    {
        for(int a = 0; a < screenwidth * screenheight; a++)
            pixels[a] = 0xff000000;

    }

    public void gamefunc()
    {
        int pointx1 = 0;
        int pointy1 = 0;
        double e = 1.0D;
        for(int a = 0; a < pointnumber; a++)
        {
            pointdegrees[a] = Math.atan2(pointx[a] - centerx, pointy[a] - centery);
            pointradius[a] = distance2(pointx[a] - centerx, pointy[a] - centery);
        }

        if(pointnumber != 0)
        {
            for(int a = 0; a < sampletotal; a++)
            {
                int c = (int)(Math.random() * (double)pointnumber);
                double dx = pointx[c];
                double dy = pointy[c];
                e = 1.0D;
                double extra = pointrotation[c];
                for(int d = 1; d < itertotal; d++)
                {
                    c = (int)(Math.random() * (double)pointnumber);
                    e *= pointscale[c];
                    extra += pointrotation[c];
                    dx += Math.cos((pi2 - pointdegrees[c]) + extra) * pointradius[c] * e;
                    dy += Math.sin((pi2 - pointdegrees[c]) + extra) * pointradius[c] * e;
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

            if(!hidden2)
            {
                for(int a = 0; a < pointnumber; a++)
                {
                    pointx1 = (int)pointx[a];
                    pointy1 = (int)pointy[a];
                    if(pointx1 > screenwidth - 2)
                        pointx1 = screenwidth - 2;
                    if(pointy1 > screenheight - 2)
                        pointy1 = screenheight - 2;
                    if(pointx1 < 0)
                        pointx1 = 0;
                    if(pointy1 < 0)
                        pointy1 = 0;
                    if(a == pointselected)
                    {
                        pixels[pointx1 + pointy1 * screenwidth] = 0xff00ff00;
                        pixels[pointx1 + 1 + pointy1 * screenwidth] = 0xff00ff00;
                        pixels[pointx1 + (pointy1 + 1) * screenwidth] = 0xff00ff00;
                        pixels[pointx1 + 1 + (pointy1 + 1) * screenwidth] = 0xff00ff00;
                    } else
                    {
                        pixels[pointx1 + pointy1 * screenwidth] = 0xffff0000;
                        pixels[pointx1 + 1 + pointy1 * screenwidth] = 0xffff0000;
                        pixels[pointx1 + (pointy1 + 1) * screenwidth] = 0xffff0000;
                        pixels[pointx1 + 1 + (pointy1 + 1) * screenwidth] = 0xffff0000;
                    }
                }

            }
        }
    }

    public void mouseClicked(MouseEvent mouseevent)
    {
    }

    public void mousePressed(MouseEvent arg0)
    {
        mousemode = arg0.getButton();

        mousex = arg0.getX();
        mousey = arg0.getY();
        double olddist = 1000D;
        for(int a = 0; a < pointnumber; a++)
        {
            double currentdist = distance2((double)mousex - pointx[a], (double)mousey - pointy[a]);
            if(currentdist < olddist)
            {
                olddist = currentdist;
                pointselected = a;
            }
        }

        startDragX = arg0.getX();
        startDragY = arg0.getY();
        startDragPX = pointx[pointselected];
        startDragPY = pointy[pointselected];
        startDragDist = distance2(arg0.getX() - pointx[pointselected], arg0.getY() - pointy[pointselected]);
        startDragAngle = pointrotation[pointselected] + Math.atan2(arg0.getX() - pointx[pointselected], arg0.getY() - pointy[pointselected]);
        startDragScale = pointscale[pointselected];

        requestFocus();
        updateCenter();
    }

    public void updateCenter(){
        int x = 0;
        int y = 0;

        for(int a = 0; a < pointnumber; a++)
        {
            x = (int)((double)x + pointx[a]);
            y = (int)((double)y + pointy[a]);
        }

        centerx = x / (pointnumber);
        centery = y / (pointnumber);
    }

    public void mouseReleased(MouseEvent mouseevent)
    {
        setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        mousemode = 0;
    }

    public void mouseEntered(MouseEvent mouseevent)
    {
        setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void mouseExited(MouseEvent mouseevent)
    {
        setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void mouseDragged(MouseEvent mouseevent)
    {
        if(mousemode == 1){ //left click to move a point
            setCursor (Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            pointx[pointselected] = startDragPX + (mouseevent.getX() - startDragX);
            pointy[pointselected] = startDragPY + (mouseevent.getY() - startDragY);
            updateCenter();
            clearframe();
            gamefunc();
        }
        else if(mousemode == 3){ //right click to rotate point
            setCursor (Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
            pointrotation[pointselected] = pi * 2 - (Math.atan2(mouseevent.getX() - pointx[pointselected], mouseevent.getY() - pointy[pointselected])- startDragAngle);
            pointscale[pointselected] = startDragScale*distance2(mouseevent.getX() - pointx[pointselected], mouseevent.getY() - pointy[pointselected])/startDragDist;

            updateCenter();
            clearframe();
            gamefunc();
        }

    }

    public void mouseMoved(MouseEvent arg0)
    {

    }

    public void keyTyped(KeyEvent keyevent)
    {
    }

    public void keyPressed(KeyEvent arg0)
    {
        double x = 0.0D;
        double y = 0.0D;
        if(arg0.getKeyChar() == '0')
            pointrotation[pointselected] += 0.050000000000000003D * movesize;
        if(arg0.getKeyChar() == '9')
            pointrotation[pointselected] -= 0.050000000000000003D * movesize;
        if(arg0.getKeyChar() == 'p')
        {
            for(int a = 0; a < pointnumber; a++)
                pointrotation[a] += 0.050000000000000003D * movesize;

        }
        if(arg0.getKeyChar() == 'o')
        {
            for(int a = 0; a < pointnumber; a++)
                pointrotation[a] -= 0.050000000000000003D * movesize;

        }
        if(arg0.getKeyChar() == 'l')
        {
            for(int a = 0; a < pointnumber; a++)
            {
                double degrees = Math.atan2(pointx[a] - centerx, pointy[a] - centery);
                double radius = distance2(pointx[a] - centerx, pointy[a] - centery);
                double dx = Math.cos((pi / 2D - degrees) + 0.050000000000000003D * movesize) * radius + centerx;
                double dy = Math.sin((pi / 2D - degrees) + 0.050000000000000003D * movesize) * radius + centery;
                pointx[a] = dx;
                pointy[a] = dy;
            }

        }
        if(arg0.getKeyChar() == 'k')
        {
            for(int a = 0; a < pointnumber; a++)
            {
                double degrees = Math.atan2(pointx[a] - centerx, pointy[a] - centery);
                double radius = distance2(pointx[a] - centerx, pointy[a] - centery);
                double dx = Math.cos(pi / 2D - degrees - 0.050000000000000003D * movesize) * radius + centerx;
                double dy = Math.sin(pi / 2D - degrees - 0.050000000000000003D * movesize) * radius + centery;
                pointx[a] = dx;
                pointy[a] = dy;
            }

        }
        if(arg0.getKeyChar() == '\'')
        {
            for(int a = 0; a < pointnumber; a++)
            {
                double degrees = Math.atan2(pointx[a] - centerx, pointy[a] - centery);
                double radius = distance2(pointx[a] - centerx, pointy[a] - centery);
                double dx = Math.cos(pi / 2D - degrees) * (radius * 1.01D) + centerx;
                double dy = Math.sin(pi / 2D - degrees) * (radius * 1.01D) + centery;
                pointx[a] = dx;
                pointy[a] = dy;
            }

        }
        if(arg0.getKeyChar() == ';')
        {
            for(int a = 0; a < pointnumber; a++)
            {
                double degrees = Math.atan2(pointx[a] - centerx, pointy[a] - centery);
                double radius = distance2(pointx[a] - centerx, pointy[a] - centery);
                double dx = Math.cos(pi / 2D - degrees) * (radius * 0.98999999999999999D) + centerx;
                double dy = Math.sin(pi / 2D - degrees) * (radius * 0.98999999999999999D) + centery;
                pointx[a] = dx;
                pointy[a] = dy;
            }

        }
        if(arg0.getKeyChar() == '=')
            pointscale[pointselected] *= 1.01D;
        if(arg0.getKeyChar() == '-')
            pointscale[pointselected] *= 0.98999999999999999D;
        if(arg0.getKeyChar() == ']')
        {
            for(int a = 0; a < pointnumber; a++)
                pointscale[a] *= 1.01D;

        }
        if(arg0.getKeyChar() == '[')
        {
            for(int a = 0; a < pointnumber; a++)
                pointscale[a] *= 0.98999999999999999D;

        }
        if(arg0.getKeyChar() == 'e')
            movesize *= 1.01D;
        if(arg0.getKeyChar() == 'q')
            movesize *= 0.98999999999999999D;
        if(arg0.getKeyChar() == 'a')
            pointx[pointselected] -= 2D * movesize;
        if(arg0.getKeyChar() == 'd')
            pointx[pointselected] += 2D * movesize;
        if(arg0.getKeyChar() == 's')
            pointy[pointselected] += 2D * movesize;
        if(arg0.getKeyChar() == 'w')
            pointy[pointselected] -= 2D * movesize;
        if(arg0.getKeyChar() == 'A')
        {
            for(int a = 0; a < pointnumber; a++)
                pointx[a] -= 2D * movesize;

        }
        if(arg0.getKeyChar() == 'D')
        {
            for(int a = 0; a < pointnumber; a++)
                pointx[a] += 2D * movesize;

        }
        if(arg0.getKeyChar() == 'W')
        {
            for(int a = 0; a < pointnumber; a++)
                pointy[a] -= 2D * movesize;

        }
        if(arg0.getKeyChar() == 'S')
        {
            for(int a = 0; a < pointnumber; a++)
                pointy[a] += 2D * movesize;

        }
        for(int a = 0; a < pointnumber; a++)
        {
            x += pointx[a];
            y += pointy[a];
        }

        centerx = x / (double)pointnumber;
        centery = y / (double)pointnumber;
        clearframe();
        gamefunc();
    }

    public void keyReleased(KeyEvent arg0)
    {
        int x = 0;
        int y = 0;

        if(arg0.getKeyChar() == '/')
            itertotal++;
        if(arg0.getKeyChar() == '.' && itertotal > 1)
            itertotal--;
        if(arg0.getKeyChar() == 'h')
            hidden = !hidden;
        if(arg0.getKeyChar() == 'g')
            hidden2 = !hidden2;
        if(arg0.getKeyChar() == '1')
            movesize = 0.01D;
        if(arg0.getKeyChar() == '2')
            movesize = 0.10000000000000001D;
        if(arg0.getKeyChar() == '3')
            movesize = 0.5D;
        if(arg0.getKeyChar() == '4')
            movesize = 1.0D;
        if(arg0.getKeyChar() == '5')
            movesize = 2D;
        if(arg0.getKeyChar() == '6')
            movesize = 5D;
        if(arg0.getKeyChar() == 'm')
            sampletotal += 100;
        if(arg0.getKeyChar() == 'n' && sampletotal > 1)
            sampletotal -= 100;
        clearframe();
        gamefunc();
    }

    public void focusGained(FocusEvent focusevent)
    {
    }

    public void focusLost(FocusEvent focusevent)
    {
    }

    mainthread game;
    boolean quit;
    boolean hidden;
    boolean hidden2;
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
    int itertotal;
    int pointtotal;

    int mousemode;

    //drag vars
        double startDragX;
        double startDragY;
        double startDragPX;
        double startDragPY;
        double startDragDist;
        double startDragAngle;
        double startDragScale;

    double pointx[];
    double pointy[];
    double pointscale[];
    double pointdegrees[];
    double pointradius[];
    double pointrotation[];
    int pointnumber;
    int pointselected;
    double centerx;
    double centery;
    double movesize;
    String presetstring;
    int preset;
}
