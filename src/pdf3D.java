import javax.imageio.ImageIO;
import javax.swing.*;
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

    public int validValues = 0;
    public int[] validX;
    public int[] validY;
    public int[] validZ;

    Image sampleImage;
    Image sampleImageX;
    Image sampleImageY;
    Image sampleImageZ;
    int samplePixels[];
    int samplePixelsX[];
    int samplePixelsY[];
    int samplePixelsZ[];

    comboMode thePdfComboMode = comboMode.ADD;

    public pdf3D(){
        width = 512;
        height = 512;
        depth = 512;
        volume = new double[width][height][depth];
        samplePixels = new int[width*height];

        //loadMap_XYGradient();
        //loadMap_3DBlob();
        //loadImg3D("serp2.jpg");
        //loadImg("serp.png");
        
        //loadImgs3D("_x.png", "_y.png", "_z.png");
        loadImgs3D("serp.png", "serp.png", "serp.png");
    }

    public void sampleImg(File file, Image sampleImage, int missingDimension){
        try {
            System.out.println("loading" + file.getCanonicalPath());
            //sampleImage = getImage(file);
            PixelGrabber grabber = new PixelGrabber(sampleImage, 0, 0, -1, -1, false);

            if (grabber.grabPixels()) {

                switch (missingDimension){
                                case 0: //X
                                    samplePixelsX = (int[]) grabber.getPixels();

                                    for(int i=0; i<width*height; i++){
                                        samplePixelsX[i] = samplePixelsX[i]&0xFF;
                                    }
                                    break;
                                case 1: //Y
                                    samplePixelsY = (int[]) grabber.getPixels();

                                    for(int i=0; i<width*height; i++){
                                        samplePixelsY[i] = samplePixelsY[i]&0xFF;
                                    }
                                    break;
                                case 2: //Z
                                    samplePixelsZ = (int[]) grabber.getPixels();

                                    for(int i=0; i<width*height; i++){
                                        samplePixelsZ[i] = samplePixelsZ[i]&0xFF;
                                    }
                                    break;
                            }
                System.out.println("built " + width + " " + height + " " + depth );
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSampleImageX(File file){
        System.out.println("Img X: " + file.getAbsolutePath());
        sampleImageX = getImage(file);
        sampleImg(file, sampleImageX, 0);
        updateVolume();
    }

    public void setSampleImageY(File file){
        System.out.println("Img Y: " + file.getAbsolutePath());
        sampleImageY = getImage(file);
        sampleImg(file, sampleImageY, 1);
        updateVolume();
    }

    public void setSampleImageZ(File file){
        System.out.println("Img Z: " + file.getAbsolutePath());
        sampleImageZ = getImage(file);
        sampleImg(file, sampleImageZ, 2);
        updateVolume();
    }

    public void updateVolume(){
        int i=0;
        validValues = 0;
        validX = new int[width*width*width];
        validY = new int[width*width*width];
        validZ = new int[width*width*width];
        for(int y=0; y<width; y++){
            for(int x=0; x<height; x++){
                for(int z=0; z<depth; z++){
                    updateVolumePixel(x, y, z);
                    i++;
                }
            }
        }

        int edgeUnit = 1;

        for(int y=edgeUnit; y<width-edgeUnit; y++){
            for(int x=edgeUnit; x<height-edgeUnit; x++){
                for(int z=edgeUnit; z<depth-edgeUnit; z++){
                    if(volume[x][y][z]>0 && isNearEdge(x,y,z, edgeUnit)){
                        validX[validValues]=x;
                        validY[validValues]=y;
                        validZ[validValues]=z;
                        validValues++;
                    }
                    i++;
                }
            }

        }

        System.out.println(validValues + " valid %" + (100.0*validValues/512/512/512));
    }

    public boolean isNearEdge(int x, int y, int z, int unit){

        for(int x2=-unit; x2<unit+1; x2++){
            for(int y2=-unit; y2<unit+1; y2++){
                for(int z2=-unit; z2<unit+1; z2++){
                    if(volume[x+x2][y+y2][z+z2]==0){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void updateVolumePixel(int x, int y, int z){
        switch(thePdfComboMode){
            case ADD:
                volume[x][y][z] = samplePixelsX[y+z*width]+
                                  samplePixelsY[x+z*width]+
                                  samplePixelsZ[x+y*width];
                break;
            case AVERAGE:
                volume[x][y][z] = (samplePixelsX[y+z*width]+
                                    samplePixelsY[x+z*width]+
                                    samplePixelsZ[x+y*width])/3;
                break;
            case MULTIPLY:
                volume[x][y][z] = samplePixelsX[y+z*width]*
                                  samplePixelsY[x+z*width]*
                                  samplePixelsZ[x+y*width]/255/255;
                break;
            case MAX:
                volume[x][y][z] = Math.max(Math.max(samplePixelsX[y+z*width],
                                                    samplePixelsY[x+z*width]), samplePixelsZ[x+y*width]);
                break;
            case MIN:
                volume[x][y][z] = Math.min(Math.min(samplePixelsX[y + z * width],
                        samplePixelsY[x + z * width]), samplePixelsZ[x + y * width]);
                break;

        }

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

                updateVolume();
                System.out.println("built " + sampleWidth + " " + sampleHeight + " " + sampleDepth);
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        JPanel j = new JPanel();

    }

    public Image getImage(String name){
            //URL theImgURL = new URL("file:/C:/Users/user/workspace/instant-ifs/img/" + name);
            // file:/C:/Users/Labrats/Documents/GitHub/instant-ifs/img/

            String root;

            try{
                root= "file:/C:/Users/user/workspace/instant-ifs/img/";
                URL theImgURL = new URL(root + name);
                return ImageIO.read(theImgURL);
            }catch (Exception e){

            }


            try{
                root="file:/C:/Users/Labrats/Documents/GitHub/instant-ifs/img/";
                URL theImgURL = new URL(root + name);
                return ImageIO.read(theImgURL);
            }catch (Exception e){

            }
            return null;
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

    public static enum comboMode {
        AVERAGE, ADD, MULTIPLY, MAX, MIN
    }
}
