package ifs.flat;

import ifs.recordsKeeper;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by user on 10/24/14.
 */
public class CubeMarcher implements Serializable { //marching tetrahedrons as in http://paulbourke.net/geometry/polygonise/

        Polygoniser myPolygoniser;
        public ArrayList<Polygoniser.Triangle> triangleList;

        public CubeMarcher(){
            triangleList = new ArrayList<>();
            myPolygoniser = new Polygoniser();
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////

        public String saveToBinarySTL(){
            int totalTriangles = triangleList.size();
            Date startDate = Calendar.getInstance().getTime();
            String timeLog1 = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(startDate);
            String timeLog = timeLog1+ ".stl";
            //File logFile = new File(timeLog);

            //System.out.println("saving stl...");
            byte[] title = new byte[80];

            try(FileChannel ch=new RandomAccessFile("./models/"+timeLog , "rw").getChannel())
            {
                ByteBuffer bb= ByteBuffer.allocate(1000000).order(ByteOrder.LITTLE_ENDIAN);
                bb.put(title); // Header (80 bytes)
                bb.putInt(totalTriangles); // Number of triangles (UINT32)
                int triNo=0;
                for(Polygoniser.Triangle tri : triangleList){
                    if(triNo<totalTriangles){
                        bb.putFloat(0).putFloat(0).putFloat(0); //TODO normals?
                        bb.putFloat((float)tri.p0x).putFloat((float)tri.p0y).putFloat((float) tri.p0z);
                        bb.putFloat((float)tri.p1x).putFloat((float)tri.p1y).putFloat((float) tri.p1z);
                        bb.putFloat((float)tri.p2x).putFloat((float)tri.p2y).putFloat((float) tri.p2z);
                        bb.putShort((short)12); //TODO colors?

                        bb.flip();
                        ch.write(bb);
                        bb.clear();

                        triNo++;
                        if(triNo%102400==0){
                            System.out.println("TRI "+triNo+"/"+totalTriangles + " saved - " + (int)(100.0*triNo/totalTriangles)+"%");
                        }
                    }
                }
                ch.close();

                System.out.println("done! " + timeLog);
            }catch(Exception e){
                e.printStackTrace();
            }

            return timeLog1;
        }

        public static void setArrayUsingList(List<Integer> integers, int[] array)
        {
            int len = integers.size();
            Iterator<Integer> iterator = integers.iterator();
            for (int i = 0; i < len; i++)
            {
                array[i] = Math.min(iterator.next().intValue(), 65533);
            }
        }

        public int sumLists(ArrayList<Integer>[] zLists){
            int sum=0;
            int max = 0;
            for(int i=0; i<zLists.length; i++){
                for(Integer y : zLists[i]){
                    sum+=y;
                    max = Math.max(y,max);
                }
            }
            System.out.println("MAXIMUM " +max);
            return sum;
        }

        public void getPotentials(ShapeAnalyzer shapeAnalyzer, ArrayList<Integer>[] zLists, int xMin, int xMax, int yMin, int yMax, int zMin, int zMax) {
            triangleList = new ArrayList<>();
            myPolygoniser = new Polygoniser();

            int gridWidth = shapeAnalyzer.width;
            int stepSize = (1024/shapeAnalyzer.width);

            double maxDist = 16;
            double x,y,z;
            double iso = 1/(maxDist*maxDist); //make this smaller to make the tube thicker

            long numPolys=0;

            long startTime = System.currentTimeMillis();

            shapeAnalyzer.updateGeometry();
            long t1=0,t2=0,t3=0,t4=0;

            int _zmin = Math.max(stepSize, zMin);
            int _zmax = Math.min(1024 - stepSize, zMax);
            long totalPolyTime = 0;
            long totalZTime = 0;
            int _xmin,_xmax;
            int _ymin,_ymax;
            int newTris;
            int i;

            System.out.println("total zs " + zLists.length + " rows totalling " + sumLists(zLists));

            for(z=_zmin; z<_zmax; z+=stepSize){
                setArrayUsingList(zLists[(int)z], shapeAnalyzer.ZList);
                shapeAnalyzer.updateZList(zLists[(int)z].size());

                totalZTime+=(t3-t1);
                totalPolyTime+=(t4-t3);

                t1 = System.currentTimeMillis();
                shapeAnalyzer.getAllPotentialsByZ(z,(int)maxDist,iso);

                t3 = System.currentTimeMillis();

                _xmin = Math.max(Math.max(stepSize, xMin), shapeAnalyzer.border[0]);
                _xmax = Math.min(Math.min(1024-stepSize, xMax), shapeAnalyzer.border[1]);

                _ymin = Math.max(Math.max(stepSize, yMin), shapeAnalyzer.border[2]);
                _ymax = Math.min(Math.min(1024-stepSize, yMax),shapeAnalyzer.border[3]);

                for(x=_xmin/stepSize; x<_xmax/stepSize; x++){
                    for(y=_ymin/stepSize; y<_ymax/stepSize; y++){
                        newTris = myPolygoniser.Polygonise(x, y, z / stepSize, shapeAnalyzer.linePot, shapeAnalyzer.linePot2, gridWidth, iso);
                        numPolys += newTris;

                        for(i=0; i<newTris; i++){
                            triangleList.add(myPolygoniser.temp_triangles[i].copy());
                        }
                    }
                }

                t4 = System.currentTimeMillis();

            }

            System.out.println("scan time " + (System.currentTimeMillis() - startTime) + "ms " + triangleList.size() + " " + xMin + "-" + xMax+ " " + yMin + "-" + yMax + " " +zMin + "-" + zMax);

            t4 = System.currentTimeMillis();

            boolean badTriangleFound = false;

            double myVolume =0;
            double mySurface=0;
            for(Polygoniser.Triangle _tri: triangleList){
                if(_tri.invalid){badTriangleFound=true;}
                if(!(Double.isNaN(_tri.mySignedVolume()) || Double.isNaN(_tri.mySurface()))){ //TODO figure out where the NaNs come from...
                    myVolume += _tri.mySignedVolume();
                    mySurface += _tri.mySurface();
                    //badTriangleFound=true;
                }else{
                    //System.out.println("bad triangle!");
                }

            }

            System.out.println(triangleList.size() + "--" + numPolys + " TRIS -- found volume surface in "  + (System.currentTimeMillis() - t4) );

            myVolume=Math.abs(myVolume);

            theSurfaceArea = mySurface;
            theVolume = myVolume;


            long buildTime = System.currentTimeMillis() - startTime;

            long saveStartTime = System.currentTimeMillis();


            long saveTime = (System.currentTimeMillis() - saveStartTime);

            long meshStartTime = System.currentTimeMillis();
            //this.meshLabFix("./models/"+theFileName);
            long meshSaveTime = System.currentTimeMillis()-meshStartTime;

            totalMeshLabTime+=meshSaveTime;
            totalBuildTime+=buildTime;
            totalSaveTime+=saveTime;
            totalsubBuildTimeCPU+=totalPolyTime;
            totalsubBuildTimeGPU+=totalZTime;

            totalTime = (System.currentTimeMillis() - startTime);
            System.out.println("done - built in " + buildTime/1000.0 + "s, cpu " + totalPolyTime + " gpu " + totalZTime );

            shapeInvalid= badTriangleFound;
            if(shapeInvalid){
                System.out.println("SHAPE INVALID");
            }

            System.out.println("SURFACE " + theSurfaceArea + " VOLUME " + theVolume + " RATIO s/v " + (theSurfaceArea/(theVolume+0.00001d))
                    + " DIAG " + theDiagonal + " RATIO s/(vd) " + (theSurfaceArea/(theVolume+0.00001d)/theDiagonal)+"\n");



            System.out.println("\nTOTALS: build " + totalBuildTime/1000.0 + " (cpu "+totalsubBuildTimeCPU/1000.0 + ", gpu " + totalsubBuildTimeGPU/1000.0 + ") save " + totalSaveTime/1000.0+"\n");
        }

        public static long totalMeshLabTime = 0;
        public static long totalBuildTime = 0;
        public static long totalsubBuildTimeCPU = 0;
        public static long totalsubBuildTimeGPU = 0;
        public static long totalSaveTime = 0;

        public double theVolume=0;
        public double theSurfaceArea=0;
        public double theDiagonal=0;
        public boolean shapeInvalid = false;
        public String theFileName = "";
        public long totalTime =0;

        public void meshLabFix(String fileName){
            try {
                Runtime runTime = Runtime.getRuntime();
                Process process = runTime.exec("C:\\Program Files\\VCG\\MeshLab\\meshlabserver.exe -s ./instant-ifs/myscript2.mlx -i "+fileName+".stl -o " + fileName + "_processed.obj");

                InputStream inputStream = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(inputStream);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ( (line = br.readLine()) != null){
                    //System.out.println(line);
                    if(line.contains("watertight") || line.contains("Failed")){
                        shapeInvalid=true;
                        break;
                    }
                    if(line.contains("Mesh Volume")){
                        theVolume = Double.parseDouble(line.substring(line.lastIndexOf(" "),line.length()));
                    }
                    if(line.contains("Mesh Surface")){
                        theSurfaceArea = Double.parseDouble(line.substring(line.lastIndexOf(" "),line.length()));
                    }
                    if(line.contains("Bounding Box Diag")){
                        theDiagonal = Double.parseDouble(line.substring(line.lastIndexOf("Diag ")+5,line.length()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

