
public class ifsys$mainthread extends Thread
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

    public ifsys$mainthread()
    {
    }
}
