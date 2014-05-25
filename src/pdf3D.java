import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.PixelGrabber;
import java.net.URL;

public class pdf3D { //3d probabilty density function

    int width, height, depth;
    int centerx, centery, centerz;

    int sampleWidth;
    int sampleHeight;

    public double volume[][][];
    Image sampleImage;
    int samplePixels[];

    public pdf3D(){
        width = 512;
        height = 512;
        depth = 1;
        volume = new double[width][height][depth];
        samplePixels = new int[width*height];

        centerx=width/2;
        centery=height/2;
        centerz=1;

        //loadMap_DistanceFromCenter();
        loadImg("serp.jpg");
    }

    public void loadMap_DistanceFromCenter(){ //fill volume w values = distances from point to center of volume
        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                for(int z=0; z<depth; z++){
                    volume[x][y][z] = distance(x-centerx, y-centery, z-centerz);
                }
            }
        }
    }

    public double getSampleValue(double x, double y){ //TODO bilinear filtering
        if(x>0 && y>0 && y<sampleHeight && x<sampleWidth){
            return volume[(int)x][(int)y][0] / 255.0;
        }else{
            return 0;
        }
    }



    public void loadImg(String filename){
        System.out.println("loading " + filename);
        sampleImage = getImage(filename);

        try {
            PixelGrabber grabber =
                    new PixelGrabber(sampleImage, 0, 0, -1, -1, false);

            if (grabber.grabPixels()) {
                sampleWidth = grabber.getWidth();
                sampleHeight = grabber.getHeight();
                System.out.println(sampleWidth + " " + sampleHeight);
                samplePixels = (int[]) grabber.getPixels();

                for(int i=0; i<sampleWidth*sampleHeight; i++){
                    samplePixels[i] = samplePixels[i]&0xFF;

                }

                int i=0;
                for(int x=0; x<sampleWidth; x++){
                    for(int y=0; y<sampleHeight; y++){
                        volume[x][y][0] = (double)samplePixels[i];
                        i++;
                    }
                }
                System.out.println("loaded");
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Image getImage(String name){
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

    public double distance(double x, double y, double z){
        return Math.sqrt(x * x + y * y + z * z);
    }
}
