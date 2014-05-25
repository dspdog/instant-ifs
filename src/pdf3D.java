import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.PixelGrabber;
import java.net.URL;

public class pdf3D { //3d probabilty density function

    int width, height, depth;

    int sampleWidth;
    int sampleHeight;
    int sampleDepth;

    public double volume[][][];

    Image sampleImage;
    int samplePixels[];

    public pdf3D(){
        width = 512;
        height = 512;
        depth = 512;
        volume = new double[width][height][depth];
        samplePixels = new int[width*height];

        loadMap_3DBlob();

        System.out.println(sampleWidth + " " + sampleHeight + " " + sampleDepth);
        //loadImg("serp.jpg");
    }

    public double getSliceXY_Sum(int x, int y){
        double sum = 0;
        for(int z=0; z<depth; z++){
            sum+=volume[x][y][z];
        }
        return sum;
    }

    //MAP-LOADING FUNCTIONS:

    public void loadMap_BlobInv(){ //fill volume w values = distances from point to center of volume
        sampleHeight=height;
        sampleWidth=width;
        sampleDepth=1;
        for(int x=0; x<sampleWidth; x++){
            for(int y=0; y<sampleHeight; y++){
                for(int z=0; z<sampleDepth; z++){
                    volume[x][y][z] = distance(x-sampleWidth/2, y-sampleHeight/2, z-sampleDepth/2);
                }
            }
        }
    }

    public void loadMap_FlatBlob(){ //fill volume w values = inverse distances from point to center of volume
        sampleHeight=height;
        sampleWidth=width;
        sampleDepth=1;
        for(int x=0; x<sampleWidth; x++){
            for(int y=0; y<sampleHeight; y++){
                for(int z=0; z<sampleDepth; z++){
                    volume[x][y][z] = Math.max(0,sampleWidth/2 - distance(x-sampleWidth/2, y-sampleHeight/2, z-sampleDepth/2));
                }
            }
        }
    }

    public void loadMap_3DBlob(){ //fill volume w values = inverse distances from point to center of volume
        sampleHeight=height;
        sampleWidth=width;
        sampleDepth=depth;
        for(int x=0; x<sampleWidth; x++){
            for(int y=0; y<sampleHeight; y++){
                for(int z=0; z<sampleDepth; z++){
                    volume[x][y][z] = Math.max(0,sampleWidth/2 - distance(x-sampleWidth/2, y-sampleHeight/2, z-sampleDepth/2));
                }
            }
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
                sampleDepth = 1;

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
