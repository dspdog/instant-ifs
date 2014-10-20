package ifs.flat;

//irregular trianglular prism volume http://darrenirvine.blogspot.com/2011/10/volume-of-irregular-triangular-prism.html
//tetrahedron volume http://darrenirvine.blogspot.com/2013/12/volume-of-tetrahedon.html

//java version of the algo from http://paulbourke.net/geometry/polygonise/

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;

public class TetraMarcher implements Serializable{ //marching tetrahedrons as in http://paulbourke.net/geometry/polygonise/

    public class xyz {
        public double x,y,z;
        public xyz(){
            x=0;y=0;z=0;
        }
        public void setTo(xyz target){
            x=target.x;
            y=target.y;
            z=target.z;
        }
    }

    public class Triangle{
        public xyz[] p;
        public Triangle(){
            p = new xyz[3];
            //edgeHash = new long[3];

            p[0] = new xyz();
            p[1] = new xyz();
            p[2] = new xyz();
        }
    }

    public class GridCell{
        public xyz[] p;
        public double[] val;

        public GridCell(){
            p = new xyz[8];
            val = new double[8];
        }
    }

    public ArrayList<Triangle> triangleList;

    public TetraMarcher(){
        triangleList = new ArrayList<>();
    }

    /*
       Polygonise a tetrahedron given its vertices within a cube
       This is an alternative algorithm to polygonisegrid.
       It results in a smoother surface but more triangular facets.

                          + 0
                         /|\
                        / | \
                       /  |  \
                      /   |   \
                     /    |    \
                    /     |     \
                   +-------------+ 1
                  3 \     |     /
                     \    |    /
                      \   |   /
                       \  |  /
                        \ | /
                         \|/
                          + 2

       It's main purpose is still to polygonise a gridded dataset and
       would normally be called 6 times, one for each tetrahedron making
       up the grid cell.
       Given the grid labelling as in PolygniseGrid one would call
          PolygoniseTri(grid,iso,triangles,0,2,3,7);
          PolygoniseTri(grid,iso,triangles,0,2,6,7);
          PolygoniseTri(grid,iso,triangles,0,4,6,7);
          PolygoniseTri(grid,iso,triangles,0,6,1,2);
          PolygoniseTri(grid,iso,triangles,0,6,1,4);
          PolygoniseTri(grid,iso,triangles,5,6,1,4);
    */

    public int addTetrahedronSlices(GridCell grid, double iso, Triangle[] triangles, int triIndex, int v0, int v1, int v2, int v3){
        Triangle[] temp_tris = new Triangle[2];

        int contrib=PolygoniseTri(grid,iso,temp_tris,v0,v1,v2,v3);

        switch (contrib){
            case 0:
                return 0;
            case 1:
                triangles[triIndex] = new Triangle();
                triangles[triIndex].p[0].x = temp_tris[0].p[0].x;
                triangles[triIndex].p[0].y = temp_tris[0].p[0].y;
                triangles[triIndex].p[0].z = temp_tris[0].p[0].z;
                triangles[triIndex].p[1].x = temp_tris[0].p[1].x;
                triangles[triIndex].p[1].y = temp_tris[0].p[1].y;
                triangles[triIndex].p[1].z = temp_tris[0].p[1].z;
                triangles[triIndex].p[2].x = temp_tris[0].p[2].x;
                triangles[triIndex].p[2].y = temp_tris[0].p[2].y;
                triangles[triIndex].p[2].z = temp_tris[0].p[2].z;

                triangleList.add(triangles[triIndex]);
                return 1;
            case 2:
                triangles[triIndex] = new Triangle();
                triangles[triIndex+1] = new Triangle();
                triangles[triIndex].p[0].x = temp_tris[0].p[0].x;
                triangles[triIndex].p[0].y = temp_tris[0].p[0].y;
                triangles[triIndex].p[0].z = temp_tris[0].p[0].z;
                triangles[triIndex].p[1].x = temp_tris[0].p[1].x;
                triangles[triIndex].p[1].y = temp_tris[0].p[1].y;
                triangles[triIndex].p[1].z = temp_tris[0].p[1].z;
                triangles[triIndex].p[2].x = temp_tris[0].p[2].x;
                triangles[triIndex].p[2].y = temp_tris[0].p[2].y;
                triangles[triIndex].p[2].z = temp_tris[0].p[2].z;

                triangles[triIndex+1].p[0].x = temp_tris[1].p[0].x;
                triangles[triIndex+1].p[0].y = temp_tris[1].p[0].y;
                triangles[triIndex+1].p[0].z = temp_tris[1].p[0].z;
                triangles[triIndex+1].p[1].x = temp_tris[1].p[1].x;
                triangles[triIndex+1].p[1].y = temp_tris[1].p[1].y;
                triangles[triIndex+1].p[1].z = temp_tris[1].p[1].z;
                triangles[triIndex+1].p[2].x = temp_tris[1].p[2].x;
                triangles[triIndex+1].p[2].y = temp_tris[1].p[2].y;
                triangles[triIndex+1].p[2].z = temp_tris[1].p[2].z;

                triangleList.add(triangles[triIndex]);
                triangleList.add(triangles[triIndex+1]);
                return 2;
        }

        return 0;
    }

    public int PolygoniseGrid(GridCell grid, double iso, Triangle[] triangles){
        int numTri=0;

        numTri+= addTetrahedronSlices(grid, iso, triangles, numTri, 0, 2, 3, 7);
        numTri+= addTetrahedronSlices(grid, iso, triangles, numTri, 0, 2, 6, 7);
        numTri+= addTetrahedronSlices(grid, iso, triangles, numTri, 0, 4, 6, 7);
        numTri+= addTetrahedronSlices(grid, iso, triangles, numTri, 0, 6, 1, 2);
        numTri+= addTetrahedronSlices(grid, iso, triangles, numTri, 0, 6, 1, 4);
        numTri+= addTetrahedronSlices(grid, iso, triangles, numTri, 5, 6, 1, 4);

        return numTri;
    }

    public Triangle[] generateTriangleArray(){
        return new Triangle[12];
    }

    public GridCell generateCell(double x, double y, double z, double size, double[] linePot1, double[] linePot2){
        GridCell thisCell = new GridCell();
        thisCell.p[0] = new xyz();
        thisCell.p[1] = new xyz();
        thisCell.p[2] = new xyz();
        thisCell.p[3] = new xyz();
        thisCell.p[4] = new xyz();
        thisCell.p[5] = new xyz();
        thisCell.p[6] = new xyz();
        thisCell.p[7] = new xyz();
        thisCell.p[0].x = x;
        thisCell.p[0].y = y;
        thisCell.p[0].z = z;
        thisCell.p[1].x = x+size;
        thisCell.p[1].y = y;
        thisCell.p[1].z = z;
        thisCell.p[2].x = x+size;
        thisCell.p[2].y = y;
        thisCell.p[2].z = z+size;
        thisCell.p[3].x = x;
        thisCell.p[3].y = y;
        thisCell.p[3].z = z+size;
        thisCell.p[4].x = x;
        thisCell.p[4].y = y+size;
        thisCell.p[4].z = z;
        thisCell.p[5].x = x+size;
        thisCell.p[5].y = y+size;
        thisCell.p[5].z = z;
        thisCell.p[6].x = x+size;
        thisCell.p[6].y = y+size;
        thisCell.p[6].z = z+size;
        thisCell.p[7].x = x;
        thisCell.p[7].y = y+size;
        thisCell.p[7].z = z+size;

        int _x = (int)Math.min(1024-size,Math.max(x,size));
        int _y = (int)Math.min(1024-size,Math.max(y,size));
        int _z = (int)Math.min(1024-size,Math.max(z,size));

        thisCell.val[0] = linePot1[_x+_y*1024];
        thisCell.val[1] = linePot1[(_x+(int)size)+_y*1024];
        thisCell.val[4] = linePot1[_x+(_y+(int)size)*1024];
        thisCell.val[5] = linePot1[(_x+(int)size)+(_y+(int)size)*1024];

        thisCell.val[3] = linePot2[_x+_y*1024];
        thisCell.val[2] = linePot2[(_x+(int)size)+_y*1024];
        thisCell.val[7] = linePot2[_x+(_y+(int)size)*1024];
        thisCell.val[6] = linePot2[(_x+(int)size)+(_y+(int)size)*1024];

        return thisCell;
    }

    int PolygoniseTri(GridCell g,double iso,
                      Triangle[] tri,int v0,int v1,int v2,int v3)
    {
        int ntri = 0;
        int triindex;

   /*
      Determine which of the 16 cases we have given which vertices
      are above or below the isosurface
   */
        triindex = 0;
        if (g.val[v0] < iso) triindex |= 1;
        if (g.val[v1] < iso) triindex |= 2;
        if (g.val[v2] < iso) triindex |= 4;
        if (g.val[v3] < iso) triindex |= 8;

   /* Form the vertices of the triangles for each case */
        switch (triindex) {
            case 0b0000: //completely above iso value - "empty"
            case 0b1111: //completely below iso value - "full"
                break;
            case 0b1110: //same as 0001 but reversed
            case 0b0001:
                tri[0] = new Triangle();
                tri[0].p[0] = VertexInterp(iso,g.p[v0],g.p[v1],g.val[v0],g.val[v1]);
                tri[0].p[1] = VertexInterp(iso,g.p[v0],g.p[v2],g.val[v0],g.val[v2]);
                tri[0].p[2] = VertexInterp(iso,g.p[v0],g.p[v3],g.val[v0],g.val[v3]);
                ntri++;
                break;
            case 0b1101: //same as 0010 but reversed
            case 0b0010:
                tri[0] = new Triangle();
                tri[0].p[0] = VertexInterp(iso,g.p[v1],g.p[v0],g.val[v1],g.val[v0]);
                tri[0].p[1] = VertexInterp(iso,g.p[v1],g.p[v3],g.val[v1],g.val[v3]);
                tri[0].p[2] = VertexInterp(iso,g.p[v1],g.p[v2],g.val[v1],g.val[v2]);
                ntri++;
                break;
            case 0b1100: //same as 0011 but reversed
            case 0b0011:
                tri[0] = new Triangle();
                tri[1] = new Triangle();
                tri[0].p[0] = VertexInterp(iso,g.p[v0],g.p[v3],g.val[v0],g.val[v3]);
                tri[0].p[1] = VertexInterp(iso,g.p[v0],g.p[v2],g.val[v0],g.val[v2]);
                tri[0].p[2] = VertexInterp(iso,g.p[v1],g.p[v3],g.val[v1],g.val[v3]);
                ntri++;
                tri[1].p[0] = tri[0].p[2];
                tri[1].p[1] = VertexInterp(iso,g.p[v1],g.p[v2],g.val[v1],g.val[v2]);
                tri[1].p[2] = tri[0].p[1];
                ntri++;
                break;
            case 0b1011: //same as 0100 but reversed
            case 0b0100:
                tri[0] = new Triangle();
                tri[0].p[0] = VertexInterp(iso,g.p[v2],g.p[v0],g.val[v2],g.val[v0]);
                tri[0].p[1] = VertexInterp(iso,g.p[v2],g.p[v1],g.val[v2],g.val[v1]);
                tri[0].p[2] = VertexInterp(iso,g.p[v2],g.p[v3],g.val[v2],g.val[v3]);
                ntri++;
                break;
            case 0b1010: //same as 0101 but reversed
            case 0b0101:
                tri[0] = new Triangle();
                tri[1] = new Triangle();
                tri[0].p[0] = VertexInterp(iso,g.p[v0],g.p[v1],g.val[v0],g.val[v1]);
                tri[0].p[1] = VertexInterp(iso,g.p[v2],g.p[v3],g.val[v2],g.val[v3]);
                tri[0].p[2] = VertexInterp(iso,g.p[v0],g.p[v3],g.val[v0],g.val[v3]);
                ntri++;
                tri[1].p[0] = tri[0].p[0];
                tri[1].p[1] = VertexInterp(iso,g.p[v1],g.p[v2],g.val[v1],g.val[v2]);
                tri[1].p[2] = tri[0].p[1];
                ntri++;
                break;
            case 0b1001: //same as 0110 but reversed
            case 0b0110:
                tri[0] = new Triangle();
                tri[1] = new Triangle();
                tri[0].p[0] = VertexInterp(iso,g.p[v0],g.p[v1],g.val[v0],g.val[v1]);
                tri[0].p[1] = VertexInterp(iso,g.p[v1],g.p[v3],g.val[v1],g.val[v3]);
                tri[0].p[2] = VertexInterp(iso,g.p[v2],g.p[v3],g.val[v2],g.val[v3]);
                ntri++;
                tri[1].p[0] = tri[0].p[0];
                tri[1].p[1] = VertexInterp(iso,g.p[v0],g.p[v2],g.val[v0],g.val[v2]);
                tri[1].p[2] = tri[0].p[2];
                ntri++;
                break;
            case 0b1000: //same as 1000 but reversed
            case 0b0111:
                tri[0] = new Triangle();
                tri[0].p[0] = VertexInterp(iso,g.p[v3],g.p[v0],g.val[v3],g.val[v0]);
                tri[0].p[1] = VertexInterp(iso,g.p[v3],g.p[v2],g.val[v3],g.val[v2]);
                tri[0].p[2] = VertexInterp(iso,g.p[v3],g.p[v1],g.val[v3],g.val[v1]);
                ntri++;
                break;
        }

        return(ntri);
    }



    /*
       Linearly interpolate the position where an isosurface cuts
       an edge between two vertices, each with their own scalar value
    */
    xyz VertexInterp(double isolevel, xyz p1, xyz p2, double valp1, double valp2)
    {
        double mu;
        xyz p = new xyz();

        if (Math.abs(isolevel-valp1) < 0.00001)
            return(p1);
        if (Math.abs(isolevel-valp2) < 0.00001)
            return(p2);
        if (Math.abs(valp1-valp2) < 0.00001)
            return(p1);
        mu = (isolevel - valp1) / (valp2 - valp1);
        p.x = p1.x + mu * (p2.x - p1.x);
        p.y = p1.y + mu * (p2.y - p1.y);
        p.z = p1.z + mu * (p2.z - p1.z);

        return(p);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public String saveToBinarySTL(int totalTriangles){

        Date startDate = Calendar.getInstance().getTime();
        String timeLog1 = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(startDate);
        String timeLog = timeLog1+ ".stl";
        //File logFile = new File(timeLog);

        System.out.println("saving stl...");
        byte[] title = new byte[80];

        try(FileChannel ch=new RandomAccessFile(timeLog , "rw").getChannel())
        {
            ByteBuffer bb= ByteBuffer.allocate(1000000).order(ByteOrder.LITTLE_ENDIAN);
            bb.put(title); // Header (80 bytes)
            bb.putInt(totalTriangles); // Number of triangles (UINT32)
            int triNo=0;
            for(Triangle tri : triangleList){
                if(triNo<totalTriangles){
                    bb.putFloat(0).putFloat(0).putFloat(0); //TODO normals?
                    bb.putFloat((float)tri.p[0].x).putFloat((float)tri.p[0].y).putFloat((float) tri.p[0].z);
                    bb.putFloat((float)tri.p[1].x).putFloat((float)tri.p[1].y).putFloat((float)tri.p[1].z);
                    bb.putFloat((float)tri.p[2].x).putFloat((float)tri.p[2].y).putFloat((float)tri.p[2].z);
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
            array[i] = iterator.next().intValue();
        }
    }

    public void getPotentials(ShapeAnalyzer shapeAnalyzer, ArrayList<Integer>[] zLists, int xMin, int xMax, int yMin, int yMax, int zMin, int zMax, int stepSize){
        TetraMarcher.Triangle[] tri = this.generateTriangleArray();

        double maxDist = 16;
        double x,y,z;
        double iso = 1/(maxDist*maxDist); //make this smaller to make the tube thicker

        long numPolys=0;

        long startTime = System.currentTimeMillis();

        double[] oldLinePot;

        shapeAnalyzer.updateGeometry();
        long t1=0,t2=0,t3=0,t4=0;
        System.out.println("ZMINMAX " + zMin + " " + zMax);
        int _zmin = Math.max(stepSize, zMin);
        int _zmax = Math.min(1024 - stepSize, zMax);

        long totalPolyTime = 0;
        long totalZTime = 0;

        for(z=_zmin; z<_zmax; z+=stepSize){
            setArrayUsingList(zLists[(int)z], shapeAnalyzer.ZList);
            shapeAnalyzer.updateZList(zLists[(int)z].size());

            if((z-_zmin)%160==0)System.out.println("scanning " + z + "/1024  Triangles: " + numPolys + "  zPots " + (t3-t1) + " polygonize " + (t4-t3));

            totalZTime+=(t3-t1);
            totalPolyTime+=(t4-t3);

            t1 = System.currentTimeMillis();
            shapeAnalyzer.getAllPotentialsByZ(z,stepSize, (int)maxDist);
            t2 = System.currentTimeMillis();
            oldLinePot = shapeAnalyzer.linePot.clone();
            shapeAnalyzer.getAllPotentialsByZ(z+stepSize,stepSize, (int)maxDist);

            t3 = System.currentTimeMillis();
            float edgeThresh = 1.0f;

            int _xmin = Math.max(stepSize, xMin);
            int _xmax = Math.min(1024-stepSize, xMax);

            int _ymin = Math.max(stepSize, yMin);
            int _ymax = Math.min(1024-stepSize, yMax);

            for(x=_xmin; x<_xmax; x+=stepSize){
                for(y=_ymin; y<_ymax; y+=stepSize){
                    numPolys += this.PolygoniseGrid(
                            this.generateCell(x, y, z, stepSize, oldLinePot, shapeAnalyzer.linePot),
                            iso,
                            tri);
                }
            }

            t4 = System.currentTimeMillis();

        }

        System.out.println("scan time " + (System.currentTimeMillis() - startTime));

        System.out.println(this.triangleList.size() + " TRIS " + numPolys);

        long buildTime = System.currentTimeMillis() - startTime;

        //this.fixWinding();
        long saveStartTime = System.currentTimeMillis();
        theFileName = this.saveToBinarySTL(this.triangleList.size());

        long saveTime = (System.currentTimeMillis() - saveStartTime);

        long meshStartTime = System.currentTimeMillis();
        this.meshLabFix(theFileName);
        long meshSaveTime = System.currentTimeMillis()-meshStartTime;
        System.out.println("\n\n\tSURFACE " + theSurfaceArea + " VOLUME " + theVolume + " RATIO s/v " + (theSurfaceArea/(theVolume+0.00001d))
                + "\n\tDIAG " + theDiagonal + " RATIO s/(vd) " + (theSurfaceArea/(theVolume+0.00001d)/theDiagonal)+"\n\n");

        totalTime = (System.currentTimeMillis() - startTime);
        System.out.println("\tdone - built in " + buildTime/1000.0 + "s, saved in " + saveTime/1000.0 + "s, meshlabeded in " + meshSaveTime/1000.0 + "s" );
        System.out.println("\t\tbuild: gpu potn time elapsed " + totalZTime/1000.0 + "s");
        System.out.println("\t\tbuild: cpu poly time elapsed " + totalPolyTime/1000.0 + "s\n");
    }

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
                System.out.println(line);
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
