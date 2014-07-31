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

    int sampleXMin, sampleXMax;
    int sampleYMin, sampleYMax;
    int sampleZMin, sampleZMax;

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

        loadImgs3D("circle2b.png", "circle2b.png", "flat2.png");
        //buildVolumeEdgePixels_Disk(50);
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

    public void buildVolumeEdgePixels_Disk(float radius){
        samplePixelsX=new int[width*width];
        samplePixelsY=new int[width*width];
        samplePixelsZ=new int[width*width];

        for(int y=0; y<width; y++){
            for(int x=0; x<height; x++){
                if(ifsPt.dist(x-height/2,y-width/2,0)<=radius){
                    volume[x+y*width+256*width*height]=255;
                    samplePixelsX[y+256*width]=255;
                    samplePixelsY[x+256*width]=255;
                    samplePixelsZ[x+y*width]=255;
                }else{
                    volume[x+y*width+256*width*height]=0;
                    samplePixelsX[y+256*width]=0;
                    samplePixelsY[x+256*width]=0;
                    samplePixelsZ[x+y*width]=0;
                }
            }
        }
    }

    public void updateVolumePixels(int edgeUnit){
        for(int y=sampleYMin; y<sampleYMax; y++){
            for(int x=sampleXMin; x<sampleXMax; x++){
                for(int z=sampleZMin; z<sampleZMax; z++){
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

                sampleXMin=512;sampleYMin=512;sampleZMin=512;
                sampleXMax=0;sampleYMax=0;sampleZMax=0;

                for(int x=0; x<width; x++){
                    for(int y=0; y<height; y++){
                        samplePixelsX[x+y*width] = samplePixelsX[x+y*width]&0xFF;
                        samplePixelsY[x+y*width] = samplePixelsY[x+y*width]&0xFF;
                        samplePixelsZ[x+y*width] = samplePixelsZ[x+y*width]&0xFF;
                        if(samplePixelsX[x+y*width]>0){
                            if(x<sampleXMin){
                                sampleXMin=x;
                            }
                            if(x>sampleXMax){
                                sampleXMax=x;
                            }
                        }
                        if(samplePixelsY[x+y*width]>0){
                            if(x<sampleYMin){
                                sampleYMin=x;
                            }
                            if(x>sampleYMax){
                                sampleYMax=x;
                            }
                        }
                        if(samplePixelsZ[x+y*width]>0){
                            if(x<sampleZMin){
                                sampleZMin=x;
                            }
                            if(x>sampleZMax){
                                sampleZMax=x;
                            }
                        }
                    }
                }

                sampleXMax = Math.min(sampleXMax, 511);
                sampleYMax = Math.min(sampleYMax, 511);
                sampleZMax = Math.min(sampleZMax, 511);
                sampleXMin = Math.max(sampleXMin, 1);
                sampleYMin = Math.max(sampleYMin, 1);
                sampleZMin = Math.max(sampleZMin, 1);

                updateVolume();
                System.out.println("built " + sampleWidth + " " + sampleHeight + " " + sampleDepth);
                System.out.println("Range " + sampleXMin + "-" + sampleXMax + " , " + sampleYMin + "-" + sampleYMax + " , " + sampleZMin + "-" + sampleZMax);
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
