import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.net.URL;

public class pdf3D { //3d probabilty density function

    int width, height, depth;

    int sampleWidth;
    int sampleHeight;
    int sampleDepth;

    public double volume[][][];

    Image sampleImage;
    Image sampleImageX;
    Image sampleImageY;
    Image sampleImageZ;
    int samplePixels[];
    int samplePixelsX[];
    int samplePixelsY[];
    int samplePixelsZ[];

    public pdf3D(){
        width = 512;
        height = 512;
        depth = 512;
        volume = new double[width][height][depth];
        samplePixels = new int[width*height];

        //loadMap_XYGradient();
        //loadMap_3DBlob();
        loadImg3D("serp2.jpg");
        //loadImg("serp.png");
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
                    volume[x][y][z] = Math.max(0, sampleWidth / 2 - distance(x - sampleWidth / 2, y - sampleHeight / 2, z - sampleDepth / 2));
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

    public void loadMap_XYGradient(){ //fill volume w linear gradient (all vals = x)
        sampleHeight=height;
        sampleWidth=width;
        sampleDepth=depth;
        for(int x=0; x<sampleWidth; x++){
            for(int y=0; y<sampleHeight; y++){
                for(int z=0; z<sampleDepth; z++){
                    volume[x][y][z] = x;
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
                sampleHeight=height;
                sampleWidth=width;
                sampleDepth=depth;

                samplePixels = (int[]) grabber.getPixels();

                for(int i=0; i<sampleWidth*sampleHeight; i++){
                    samplePixels[i] = samplePixels[i]&0xFF;
                }

                int i=0;
                for(int y=0; y<sampleWidth; y++){
                    for(int x=0; x<sampleHeight; x++){
                        for(int z=0; z<sampleDepth; z++){
                            volume[x][y][z] = (double)samplePixels[i];
                        }
                        i++;
                    }
                }
                System.out.println("loaded " + sampleWidth + " " + sampleHeight);
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void loadImg3D(String filename){
        loadImgs3D(filename,filename,filename);
    }

    public void sampleImg(File file, Image sampleImage, int[] samplePixels){
        try {
            System.out.println("loading" + file.getCanonicalPath());
            sampleImage = getImage(file);
            PixelGrabber grabber = new PixelGrabber(sampleImage, 0, 0, -1, -1, false);

            if (grabber.grabPixels()) {
                samplePixels = (int[]) grabber.getPixels();

                for(int i=0; i<width*height; i++){
                    samplePixels[i] = samplePixels[i]&0xFF;
                }

                int i=0;
                for(int y=0; y<width; y++){
                    for(int x=0; x<height; x++){
                        for(int z=0; z<depth; z++){
                            addToVolume(x,y,z,samplePixels[y+z*width]);
                            i++;
                        }
                    }
                }
                System.out.println("built " + width + " " + height + " " + depth + " " + i);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSampleImageX(File file){
        int samplePixels[] = samplePixelsX;
        Image sampleImage = sampleImageX;
        System.out.println("Img X: " + file.getAbsolutePath());
        sampleImg(file, sampleImage, samplePixels);
    }

    public void setSampleImageY(File file){
        int samplePixels[] = samplePixelsY;
        Image sampleImage = sampleImageY;
        System.out.println("Img Y: " + file.getAbsolutePath());
        sampleImg(file, sampleImage, samplePixels);
    }

    public void setSampleImageZ(File file){
        int samplePixels[] = samplePixelsZ;
        Image sampleImage = sampleImageZ;
        System.out.println("Img Z: " + file.getAbsolutePath());
        sampleImg(file, sampleImage, samplePixels);
    }

    public void addToVolume(int x, int y, int z, double value){
        volume[x][y][z] = value;
        //samplePixelsY[x+z*width]*
        //samplePixelsZ[x+y*width]/255/255;
    }

    public void loadImgs3D(String filenameX, String filenameY, String filenameZ){
        System.out.println("loadingX " + filenameX);
        System.out.println("loadingY " + filenameY);
        System.out.println("loadingZ " + filenameZ);

        sampleImageX = getImage(filenameX);
        sampleImageY = getImage(filenameY);
        sampleImageZ = getImage(filenameZ);

        try {
            PixelGrabber grabberX = new PixelGrabber(sampleImageX, 0, 0, -1, -1, false);
            PixelGrabber grabberY = new PixelGrabber(sampleImageY, 0, 0, -1, -1, false);
            PixelGrabber grabberZ = new PixelGrabber(sampleImageZ, 0, 0, -1, -1, false);

            if (grabberX.grabPixels() && grabberY.grabPixels() && grabberZ.grabPixels()) {
                sampleHeight=height;
                sampleWidth=width;
                sampleDepth=depth;

                samplePixelsX = (int[]) grabberX.getPixels();
                samplePixelsY = (int[]) grabberY.getPixels();
                samplePixelsZ = (int[]) grabberZ.getPixels();

                for(int i=0; i<sampleWidth*sampleHeight; i++){
                    samplePixelsX[i] = samplePixelsX[i]&0xFF;
                    samplePixelsY[i] = samplePixelsY[i]&0xFF;
                    samplePixelsZ[i] = samplePixelsZ[i]&0xFF;
                }

                int i=0;
                for(int y=0; y<sampleWidth; y++){
                    for(int x=0; x<sampleHeight; x++){
                        for(int z=0; z<sampleDepth; z++){
                            volume[x][y][z] = samplePixelsX[y+z*width]*
                                              samplePixelsY[x+z*width]*
                                              samplePixelsZ[x+y*width]/255/255;
                            i++;
                        }
                    }
                }
                System.out.println("built " + sampleWidth + " " + sampleHeight + " " + sampleDepth + " " + i);
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Image getImage(String name){
        try{
            //URL theImgURL = new URL("file:/C:/Users/user/workspace/instant-ifs/img/" + name);
            // file:/C:/Users/Labrats/Documents/GitHub/instant-ifs/img/
            URL theImgURL = new URL("file:/C:/Users/user/workspace/instant-ifs/img/" + name);
            return ImageIO.read(theImgURL);
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Image getImage(File file){
        try{
            return ImageIO.read(file);
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
