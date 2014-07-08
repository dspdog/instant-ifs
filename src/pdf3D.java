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

    public float volume[];

    public int validValues = 0;
    public int[] validX; //[0] is lenght of array. [1...] are x values of valid values
    public int[] validY;
    public int[] validZ;

    public ifsPt validDir[];

    Image sampleImageX;
    Image sampleImageY;
    Image sampleImageZ;
    int samplePixels[];
    int samplePixelsX[];
    int samplePixelsY[];
    int samplePixelsZ[];

    comboMode thePdfComboMode = comboMode.ADD;

    int firstZ;
    int lastZ;
    int firstY;
    int lastY;
    int firstX;
    int lastX;

    public pdf3D(){
        width = 512;
        height = 512;
        depth = 512;
        volume = new float[width*height*depth];
        samplePixels = new int[width*height];

        firstZ=depth;
        lastZ=0;
        firstY=height;
        lastY=0;
        firstX=width;
        lastX=0;

        loadImgs3D("circle2.png", "circle2.png", "circle2.png");
    }

    public void sampleImg(File file, Image sampleImage, int missingDimension){
        try {
            System.out.println("loading" + file.getCanonicalPath());
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

        validValues = 0;
        validX = new int[width*width*width];
        validY = new int[width*width*width];
        validZ = new int[width*width*width];
        validDir = new ifsPt[width*width*width];

        updateVolumePixels();

        int edgeUnit = 1;
        int edgePrune = 4;

        for(int y=edgePrune; y<width-edgePrune; y++){ //sorting by Y.....
            for(int x=edgePrune; x<height-edgePrune; x++){
                for(int z=edgePrune; z<depth-edgePrune; z++){
                    if(pointValid(x,y,z,edgeUnit)){
                        validDir[validValues]=edgeVector(x,y,z, edgeUnit);
                        validX[validValues]=x;
                        validY[validValues]=y;
                        validZ[validValues]=z;

                        validValues++;
                    }
                }
            }
        }

        System.out.println(validValues + " valid %" + (100.0*validValues/512/512/512));
    }

    public ifsPt edgeVector(int x, int y, int z, int unit){
        ifsPt thePt = new ifsPt(0,0,0);
        float validPts = 0;
        for(int x2=-unit; x2<unit+1; x2++){
            for(int y2=-unit; y2<unit+1; y2++){
                for(int z2=-unit; z2<unit+1; z2++){
                    if(volume[(x+x2)+(y+y2)*width+(z+z2)*width*height]==0){
                        thePt.add(new ifsPt(x2,y2,z2));
                        validPts++;
                    }
                }
            }
        }

        return thePt.scale(1.0f/validPts);
    }

    public boolean pointValid(int x, int y, int z, int edgeUnit){
        return volume[x+y*width+z*width*height]>0 && isNearEdge(x,y,z, edgeUnit);
    }

    public boolean isNearEdge(int x, int y, int z, int unit){

        for(int x2=-unit; x2<unit+1; x2++){
            for(int y2=-unit; y2<unit+1; y2++){
                for(int z2=-unit; z2<unit+1; z2++){
                    if(volume[(x+x2)+(y+y2)*width+(z+z2)*width*height]==0){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public float getVolumePt(double x, double y, double z){
        return volume[(int)x+(int)y*width+(int)z*width*height];
    }

    public void updateVolumePixels(){
        switch(thePdfComboMode){
            case ADD:
                for(int y=0; y<width; y++){
                    for(int x=0; x<height; x++){
                        for(int z=0; z<depth; z++){
                            volume[x+y*width+z*width*height] = samplePixelsX[y+z*width]+
                                    samplePixelsY[x+z*width]+
                                    samplePixelsZ[x+y*width];
                        }
                    }
                }
                break;
            case AVERAGE:
                for(int y=0; y<width; y++){
                    for(int x=0; x<height; x++){
                        for(int z=0; z<depth; z++){
                            volume[x+y*width+z*width*height] = (samplePixelsX[y+z*width]+
                                    samplePixelsY[x+z*width]+
                                    samplePixelsZ[x+y*width])/3;
                        }
                    }
                }
                break;
            case MULTIPLY:
                for(int y=0; y<width; y++){
                    for(int x=0; x<height; x++){
                        for(int z=0; z<depth; z++){
                            volume[x+y*width+z*width*height] = samplePixelsX[y+z*width]*
                                    samplePixelsY[x+z*width]*
                                    samplePixelsZ[x+y*width]/255/255;
                        }
                    }
                }

                break;
            case MAX:
                for(int y=0; y<width; y++){
                    for(int x=0; x<height; x++){
                        for(int z=0; z<depth; z++){
                            volume[x+y*width+z*width*height] = Math.max(Math.max(samplePixelsX[y+z*width],
                                    samplePixelsY[x+z*width]), samplePixelsZ[x+y*width]);

                        }
                    }
                }
                break;
            case MIN:
                for(int y=0; y<width; y++){
                    for(int x=0; x<height; x++){
                        for(int z=0; z<depth; z++){
                            volume[x+y*width+z*width*height] = Math.min(Math.min(samplePixelsX[y + z * width],
                                    samplePixelsY[x + z * width]), samplePixelsZ[x + y * width]);
                        }
                    }
                }
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

    public static enum comboMode {
        AVERAGE, ADD, MULTIPLY, MAX, MIN
    }
}
