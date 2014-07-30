import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.PixelGrabber;
import java.io.*;
import java.net.URL;

public class pdf3D implements java.io.Serializable{ //3d probabilty density function

    int width, height, depth;

    int sampleWidth;
    int sampleHeight;
    int sampleDepth;

    public float volume[];

    public int edgeValues = 0;

    public intPt[] edgePts;

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

        loadImgs3D("g.png", "e.png", "b.png");
    }

    enum Dimension{
        X,Y,Z
    }

    public void sampleImg(File file, Image sampleImage, Dimension missingDimension){
        try {
            System.out.println("loading" + file.getCanonicalPath());
            PixelGrabber grabber = new PixelGrabber(sampleImage, 0, 0, -1, -1, false);

            if (grabber.grabPixels()) {

                switch (missingDimension){
                                case X: //X
                                    samplePixelsX = (int[]) grabber.getPixels();

                                    for(int i=0; i<width*height; i++){
                                        samplePixelsX[i] = samplePixelsX[i]&0xFF;
                                    }
                                    break;
                                case Y: //Y
                                    samplePixelsY = (int[]) grabber.getPixels();

                                    for(int i=0; i<width*height; i++){
                                        samplePixelsY[i] = samplePixelsY[i]&0xFF;
                                    }
                                    break;
                                case Z: //Z
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

    public void setSampleImage(File file, Dimension d){
        System.out.println("Img " + d.toString() + ": " + file.getAbsolutePath());
        sampleImg(file, getImage(file), d);
        updateVolume();
    }

    public void updateVolume(){
        edgeValues = 0;
        edgePts = new intPt[width*width*width];

        updateVolumePixels();

        int edgeUnit = 1;
        int edgePrune = 1;

        for(int y=edgePrune; y<width-edgePrune; y++){
            for(int x=edgePrune; x<height-edgePrune; x++){
                for(int z=edgePrune; z<depth-edgePrune; z++){
                    if(pointValid(x,y,z)){
                        if(isNearEdge(x,y,z, edgeUnit)){
                            edgePts[edgeValues]=new intPt(x,y,z);
                            edgeValues++;
                        }
                    }
                }
            }
        }

        System.out.println(edgeValues + " edges %" + (100.0* edgeValues /512/512/512));
    }

    public boolean pointValid(int x, int y, int z){
        return volume[x+y*width+z*width*height]>0;
    }

    public boolean isNearEdge(int x, int y, int z, int unit){

        for(int x2=-unit; x2<=unit; x2++){
            for(int y2=-unit; y2<=unit; y2++){
                for(int z2=-unit; z2<=unit; z2++){
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
    }

    public Image getImage(String name){
            //URL theImgURL = new URL("file:/C:/Users/user/workspace/instant-ifs/img/" + name);
            // file:/C:/Users/Labrats/Documents/GitHub/instant-ifs/img/

            String root;

            try{
                root= "file:/C:/Users/user/workspace/instant-ifs/instant-ifs/img/";
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

    public class intPt implements Serializable{
        public int x,y,z;
        public intPt(int _x, int _y, int _z){
            x=_x;y=_y;z=_z;
        }
    }
}
