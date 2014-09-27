package ifs.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.BufferedWriter;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;

public final class ImageUtils extends Component {
    public void saveImg(String folder, int width, int height, int[] pixels){
        DecimalFormat df = new DecimalFormat("000000");

        BufferedWriter writer = null;
        try {

            File outputdir = new File(folder);

            if (!outputdir.exists()) {
                outputdir.mkdir();
            }

            String frameNumberStr = df.format(outputdir.list().length) + ".png"; //counting files in dir to allow for sequential use in ffmpeg later, and to chain runs together possibly
            File outputfile = new File(folder+"/"+frameNumberStr);

            ImageIO.write(toBufferedImage(createImage(new MemoryImageSource(width, height, pixels, 0, width))), "png", outputfile);

            System.out.println("saved - " + outputfile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public Image getImage(String name){
        String root;
        try{
            root= "file:/C:/Users/user/workspace/instant-ifs/instant-ifs/img/";
            URL theImgURL = new URL(root + name);
            return ImageIO.read(theImgURL);
        }catch (Exception e){

        }

        return null;
    }

}
