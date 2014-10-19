package ifs.flat;

//irregular trianglular prism volume http://darrenirvine.blogspot.com/2011/10/volume-of-irregular-triangular-prism.html
//tetrahedron volume http://darrenirvine.blogspot.com/2013/12/volume-of-tetrahedon.html

//java version of the algo from http://paulbourke.net/geometry/polygonise/

import com.alee.utils.ArrayUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;

public class TetraMarcher { //marching tetrahedrons as in http://paulbourke.net/geometry/polygonise/

    public Map<Long, Integer[]> theEdges = new HashMap<Long, Integer[]>();

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
        //public long[] edgeHash;
        public int[] vertHash;
        public boolean windingProcessed;
        public boolean flipped;

        public long id = 0;

        public Triangle(){
            id = (int)(Math.random() * 100000) + System.currentTimeMillis();
            flipped = false;
            windingProcessed=false;
            p = new xyz[3];
            //edgeHash = new long[3];
            vertHash = new int[3];
            p[0] = new xyz();
            p[1] = new xyz();
            p[2] = new xyz();
        }

        public boolean hashesInOrder(int hash1, int hash2){ //AKA "hash1AppearsBeforeHash2"
            int a = getHashIndex(hash1);
            int b = getHashIndex(hash2);
            return ((b-a+3)%3)==1;
        }

        public int getHashIndex(int hash){
            for(int i=0; i<3; i++){
                if(vertHash[i]==hash){
                    return i;
                }
            }
            return -1;
        }

        public void flipWinding(int i1, int i2){
            //reverse pt order
            if(!flipped && !windingProcessed){
                xyz ptTemp1 = new xyz(); int hashTemp1 = 0;
                xyz ptTemp2 = new xyz(); int hashTemp2 = 0;
                xyz ptTemp3 = new xyz(); int hashTemp3 = 0;
                ptTemp1.setTo(p[0]); hashTemp1=vertHash[0];
                ptTemp2.setTo(p[1]); hashTemp2=vertHash[1];
                ptTemp3.setTo(p[2]); hashTemp3=vertHash[2];

                vertHash[0] = hashTemp3;
                vertHash[1] = hashTemp2;
                vertHash[2] = hashTemp1;
                p[0].setTo(ptTemp3);
                p[1].setTo(ptTemp2);
                p[2].setTo(ptTemp1);

                flipped=true;
            }
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
        triangleList = new ArrayList<Triangle>();
        theEdges = new HashMap<Long, Integer[]>();
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
            ByteBuffer bb= ByteBuffer.allocate(10000).order(ByteOrder.LITTLE_ENDIAN);
            bb.put(title); // Header (80 bytes)
            bb.putInt(totalTriangles); // Number of triangles (UINT32)
            int triNo=0;
            for(Triangle tri : triangleList){
                //if(!tri.flipped)
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
                    if(triNo%10240==0){
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

    long trisProcessed=0;
    long trisFlipped=0;
    long trisRemoved=0;
    void fixWinding(){ //TODO this doesnt work well...
        System.out.println("FIX WINDING...");
        //create list of all triangle edges, removing duplicates
        trisProcessed=0;
        trisFlipped=0;
        trisRemoved=0;
        for(int triIndex=0; triIndex<triangleList.size(); triIndex++){
            Triangle tri = triangleList.get(triIndex);

            tri.flipped=false;
            tri.windingProcessed=false;

            boolean badTri = false;

            for(int v=0;v<3;v++){
                xyz thisPt = tri.p[v];
                xyz nextPt = tri.p[(v+1)%3];
                tri.vertHash[v] = vectorhash(thisPt.x, thisPt.y, thisPt.z);
                tri.vertHash[(v+1)%3] = vectorhash(nextPt.x, nextPt.y, nextPt.z);
                if(tri.vertHash[v] == tri.vertHash[(v+1)%3]){
                    badTri=true;
                }
            }

            if(badTri){
                triangleList.remove(triIndex);
                triIndex--;
                trisRemoved++;
            }else{
                for(int v=0;v<3;v++){
                    xyz thisPt = tri.p[v];
                    xyz nextPt = tri.p[(v+1)%3];
                    addEdge(edgehash(thisPt.x, thisPt.y, thisPt.z, nextPt.x, nextPt.y, nextPt.z), triIndex);
                }
            }
        }

        System.out.println("PROCESSING...");
        trisProcessed=0;
        processTriangle(triangleList.get((int)(Math.random() * triangleList.size())));
        System.out.println("tris processed " + trisProcessed + "/" + triangleList.size() + " flipped " + trisFlipped + " removed " + trisRemoved);
    }


    void processTriangle(Triangle host){
        //given some "host" triangle:

        //mark it as processed
        host.windingProcessed=true;
        trisProcessed++;
        //if(trisProcessed%1000==0){
           // System.out.println("tris processed " + trisProcessed + "/" + triangleList.size() + " flipped " + trisFlipped);
        //}

        //for each of the possible bordering triangles
        for(int v=0;v<3;v++){
            xyz thisPt = host.p[v];
            xyz nextPt = host.p[(v+1)%3];

            long edgehash = edgehash(thisPt.x, thisPt.y, thisPt.z, nextPt.x, nextPt.y, nextPt.z);
            Integer[] edgeTris = theEdges.get(edgehash);

            triangleFlip(triangleList.get(edgeTris[0]), host);
            if(edgeTris[1]!=-1){
                triangleFlip(triangleList.get(edgeTris[1]), host);
            }
        }
    }

    void triangleFlip(Triangle tri, Triangle host){
        if(!tri.windingProcessed && tri.id != host.id){
            //--flip them if they disagree with "host"

            windingAgree(tri, host);

            //repeat this function with them as hosts
            processTriangle(tri);
        }
    }

    void windingAgree(Triangle tri, Triangle host){
        int sharedPtA = -1;
        int sharedPtB = -1;
        int sharedPtAI = -1;
        int sharedPtBI = -1;

        for(int v = 0; v<3; v++){
            if(tri.vertHash[v] == host.vertHash[0] || tri.vertHash[v] == host.vertHash[1] || tri.vertHash[v] == host.vertHash[2]){
                if(sharedPtA==-1){
                    sharedPtA = tri.vertHash[v];
                    sharedPtAI=v;
                }else{
                    sharedPtB = tri.vertHash[v];
                    sharedPtBI=v;
                }
            }
        }

        if(sharedPtA!=-1 && sharedPtB!=-1){
            boolean AWoundLikeB = (tri.hashesInOrder(sharedPtA, sharedPtB) == host.hashesInOrder(sharedPtA, sharedPtB));

            if(AWoundLikeB){
                tri.flipWinding(sharedPtAI, sharedPtBI);
                trisFlipped++;
            }
        }


    }

    void addEdge(long edgeHash, int triIndex){
        Integer[] theList = theEdges.get(edgeHash);

        if (theList != null) {
            theList[1]=triIndex;
        } else {
            theList = new Integer[2];
            theList[0]=triIndex;
            theList[1]=-1;
        }

        theEdges.put(edgeHash, theList);
    }

    long edgehash(double x1, double y1, double z1, double x2, double y2, double z2){ //returns a long hash value unique for each edge
        int v1 = vectorhash(x1,y1,z1);
        int v2 = vectorhash(x2,y2,z2);

        if(v1 == v2){
            return -1;
        }

        if(v1<v2){
            return v1<<32 + v2;
        }else{
            return v2<<32 + v1;
        }
    }

    private int vectorhash (double x, double y, double z) //via http://forum.devmaster.net/t/removal-of-duplicate-vertices/11550/2
    {
        int ix = (int)((x-512f)*2000f);
        int iy = (int)((y-512f)*2000f);
        int iz = (int)((z-512f)*2000f);

        int f = (ix+iy*11-(iz*17))&0x7fffffff;     // avoid problems with +-0
        return (f>>22)^(f>>12)^(f);
    }

    public double tetraVolume(Triangle base, xyz pinnacle){ //http://www.had2know.com/academics/tetrahedron-volume-4-vertices.html
        double a,b,c,
               d,e,f,
               g,h,i,
               p,q,r;
        a=base.p[0].x;b=base.p[0].y;c=base.p[0].z;
        d=base.p[1].x;e=base.p[1].y;f=base.p[1].z;
        g=base.p[1].x;h=base.p[1].y;i=base.p[1].z;
        p=pinnacle.x;q=pinnacle.y;r= pinnacle.z;

        double determinant = (a-p) * ((e-q)*(i-r) - (h-q)*(f-r)) - (d-p) * ((i-r)*(b-q)-(c-r)*(h-q)) + (g-p) * ((b-q)*(f-r)-(e-q)*(c-r));

        return determinant*1/6.0f;
    }

    public double getSurfaceArea(Triangle t){
        //TODO use cross product instead -- //http://darrenirvine.blogspot.com/2011/06/area-of-triangle-given-coordinates.html
        // http://math.stackexchange.com/questions/128991/how-to-calculate-area-of-3d-triangle
        double a = distance3d(t.p[0].x, t.p[0].y, t.p[0].z, t.p[1].x, t.p[1].y, t.p[1].z);
        double b = distance3d(t.p[1].x, t.p[1].y, t.p[1].z, t.p[2].x, t.p[2].y, t.p[2].z);
        double c = distance3d(t.p[2].x, t.p[2].y, t.p[2].z, t.p[0].x, t.p[0].y, t.p[0].z);
        return surfaceArea(a,b,c);
    }

    private double distance3d(double X1, double Y1, double Z1, double X2, double Y2, double Z2){
        return Math.sqrt((X2 - X1) * (X2 - X1) + (Y2 - Y1) * (Y2 - Y1) + (Z2 - Z1)*(Z2 - Z1));
    }

    double surfaceArea(double a, double b, double c){
        double s = (a+b+c)/2;
        return Math.sqrt(s*(s-a)*(s-b)*(s-c));
    }

    double dot_product(double x0, double y0, double z0, double x1, double y1, double z1){
        return x0*x1 + y0*y1 + z0*z1;
    }

    xyz cross_product(double x0, double y0, double z0, double x1, double y1, double z1){
        xyz res = new xyz();
        res.x = y0*z1 - z0*y1;
        res.y = z0*x1 - x0*z1;
        res.z = x0*y1 - y0*x1;
        return res;
    }

    public void getPotentials(ShapeAnalyzer shapeAnalyzer){
        TetraMarcher.Triangle[] tri = this.generateTriangleArray();

        double maxDist = 16;
        double x,y,z;
        double iso = 1/(maxDist*maxDist); //make this smaller to make the tube thicker

        long numPolys=0;

        int big_inc = 4;

        long startTime = System.currentTimeMillis();

        double[] oldLinePot;

        shapeAnalyzer.updateGeometry();

        for(z=big_inc; z<1024-big_inc; z+=big_inc){

            if(z%10==0)System.out.println("scanning " + z + "/1024  Triangles: " + numPolys);

            shapeAnalyzer.getAllPotentialsByZ(z,big_inc, (int)maxDist);
            oldLinePot = shapeAnalyzer.linePot.clone();
            shapeAnalyzer.getAllPotentialsByZ(z+big_inc,big_inc, (int)maxDist);

            for(x=big_inc; x<1024-big_inc; x+=big_inc){
                for(y=big_inc; y<1024-big_inc; y+=big_inc){

                    //if(shapeAnalyzer.linePot[(int)x+(int)y*1024]>iso*iso){
                        numPolys += this.PolygoniseGrid(
                                this.generateCell(x, y, z, big_inc, oldLinePot, shapeAnalyzer.linePot),
                                iso,
                                tri);
                    //}

                }
            }

        }

        System.out.println("scan time " + (System.currentTimeMillis() - startTime));

        System.out.println(this.triangleList.size() + " TRIS " + numPolys);

        long buildTime = System.currentTimeMillis() - startTime;

        //this.fixWinding();

        String fileName = this.saveToBinarySTL(this.triangleList.size());


        this.meshLabFix(fileName);
        System.out.println("SURFACE " + theSurfaceArea + " VOLUME " + theVolume + " RATIO s/v " + (theSurfaceArea/(theVolume+0.00001d)));

        long saveTime = (System.currentTimeMillis() - startTime)-buildTime;

        System.out.println("done - built in " + buildTime/1000.0 + "s, saved in " + saveTime/1000.0 + "s");
      
    }
    double theVolume=0;
    double theSurfaceArea=0;
    boolean shapeInvalid = false;
    public void meshLabFix(String fileName){
        try {
            Runtime runTime = Runtime.getRuntime();
            Process process = runTime.exec("C:\\Program Files\\VCG\\MeshLab\\meshlabserver.exe -s ./instant-ifs/myscript2.mlx -i "+fileName+".stl -o " + fileName + "B.stl");

            InputStream inputStream = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ( (line = br.readLine()) != null){
                System.out.println(line);
                if(line.indexOf("watertight") != -1){
                    shapeInvalid=true;
                }
                if(line.indexOf("Mesh Volume") != -1){
                    theVolume = Double.parseDouble(line.substring(line.lastIndexOf(" "),line.length()));
                }
                if(line.indexOf("Mesh Surface") != -1){
                    theSurfaceArea = Double.parseDouble(line.substring(line.lastIndexOf(" "),line.length()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
