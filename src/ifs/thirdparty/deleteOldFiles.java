package ifs.thirdparty;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by user on 10/24/14.
 */
public class deleteOldFiles {
    public static void deleteFilesOlderThanNMin(final int minutesBack, final String dirWay) { //modified from http://stackoverflow.com/questions/6866694/java-delete-files-older-than-n-days

        System.out.println(dirWay);
        System.out.println(minutesBack);

        final File directory = new File(dirWay);
        if(directory.exists()){
            //System.out.println(" Directory Exists");
            final File[] listFiles = directory.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".stl") || //3d models only...
                           name.toLowerCase().endsWith(".ply");
                }
            });
            final long purgeTime =
                    System.currentTimeMillis() - minutesBack * 60000;

            for(File listFile : listFiles) {
                //System.out.println("Length : "+ listFiles.length);
                //System.out.println("listFile.getName() : " +listFile.getName());
                //System.out.println("listFile.lastModified() :"+
                //       listFile.lastModified());

                if(listFile.lastModified() < purgeTime) {
                    if(listFile.delete()){
                        System.out.println("deleted old file " + listFile.getName());
                    }
                }
            }
        }
        else
        {
        }
    }
}
