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

    intPt sampleMin, sampleMax;

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

    public pdf3D(){
        width = 512;
        height = 512;
        depth = 512;
        volume = new float[width*height*depth];
        samplePixels = new int[width*height];

        loadImgs3D("circle2.png", "circle2b.png", "flat2.png");
        //loadImgs3D("g.png", "e.png", "b.png");
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

        int edgeUnit = 1;

        updateVolumePixels(edgeUnit);

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

    public void updateVolumePixels(int edgeUnit){
        for(int y=sampleMin.y; y<sampleMax.y; y++){
            for(int x=sampleMin.x; x<sampleMax.x; x++){
                for(int z=sampleMin.z; z<sampleMax.z; z++){
                    switch(thePdfComboMode){
                        case ADD:
                            volume[x+y*width+z*width*height] = samplePixelsX[y+z*width]+
                                    samplePixelsY[x+z*width]+
                                    samplePixelsZ[x+y*width];
                            break;
                        case AVERAGE:
                                        volume[x+y*width+z*width*height] = (samplePixelsX[y+z*width]+
                                                samplePixelsY[x+z*width]+
                                                samplePixelsZ[x+y*width])/3;
                            break;
                        case MULTIPLY:
                            volume[x+y*width+z*width*height] = samplePixelsX[y+z*width]*
                                    samplePixelsY[x+z*width]*
                                    samplePixelsZ[x+y*width]/255/255;
                            break;
                        case MAX:
                            volume[x+y*width+z*width*height] = Math.max(Math.max(samplePixelsX[y+z*width],
                                    samplePixelsY[x+z*width]), samplePixelsZ[x+y*width]);
                            break;
                        case MIN:
                            volume[x+y*width+z*width*height] = Math.min(Math.min(samplePixelsX[y + z * width],
                                    samplePixelsY[x + z * width]), samplePixelsZ[x + y * width]);
                            break;

                    }

                    if(pointValid(x,y,z)){
                        if(isNearEdge(x,y,z, edgeUnit)){
                            edgePts[edgeValues]=new intPt(x,y,z);
                            edgeValues++;
                        }
                    }
                }
            }
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

                sampleMin=new intPt(width,width,width);
                sampleMax=new intPt(width,width,width);

                for(int x=0; x<width; x++){
                    for(int y=0; y<height; y++){
                        samplePixelsX[x+y*width] = samplePixelsX[x+y*width]&0xFF;
                        samplePixelsY[x+y*width] = samplePixelsY[x+y*width]&0xFF;
                        samplePixelsZ[x+y*width] = samplePixelsZ[x+y*width]&0xFF;
                        if(samplePixelsX[x+y*width]>0){
                            if(x<sampleMin.x){
                                sampleMin.x=x;
                            }
                            if(x>sampleMax.x){
                                sampleMax.x=x;
                            }
                        }
                        if(samplePixelsY[x+y*width]>0){
                            if(x<sampleMin.y){
                                sampleMin.y=x;
                            }
                            if(x>sampleMax.y){
                                sampleMax.y=x;
                            }
                        }
                        if(samplePixelsZ[x+y*width]>0){
                            if(x<sampleMin.z){
                                sampleMin.z=x;
                            }
                            if(x>sampleMax.z){
                                sampleMax.z=x;
                            }
                        }
                    }
                }

                sampleMax.x = Math.min(sampleMax.x, width-1);
                sampleMax.y = Math.min(sampleMax.y, width-1);
                sampleMax.z = Math.min(sampleMax.z, width-1);
                sampleMin.x = Math.max(sampleMin.x, 1);
                sampleMin.y = Math.max(sampleMin.y, 1);
                sampleMin.z = Math.max(sampleMin.z, 1);

                updateVolume();
                System.out.println("built " + sampleWidth + " " + sampleHeight + " " + sampleDepth);
                System.out.println("Range " + sampleMin.x + "-" + sampleMax.x + " , " + sampleMin.y + "-" + sampleMax.y + " , " + sampleMin.z + "-" + sampleMax.z);
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
