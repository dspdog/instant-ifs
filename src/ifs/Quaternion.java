package ifs;

/*************************************************************************
 *  Compilation:  javac ifs.Quaternion.java
 *  Execution:    java ifs.Quaternion
 *
 *  Data type for quaternions.
 *
 *  http://mathworld.wolfram.com/ifs.Quaternion.html
 *
 *************************************************************************/

public class Quaternion {
    double w, x, y, z;

    // create a new object with the given components
    public Quaternion(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // return a string representation of the invoking object
    public String toString() {
        return w + " + " + x + "i + " + y + "j + " + z + "k";
    }

    // return the quaternion norm
    public double norm() {
        return Math.sqrt(w * w + x * x + y * y + z * z);
    }

    // return the quaternion conjugate
    public Quaternion conjugate() {
        return new Quaternion(w, -x, -y, -z);
    }

    // return a new ifs.Quaternion whose value is (this + b)
    public Quaternion plus(Quaternion b) {
        Quaternion a = this;
        return new Quaternion(a.w +b.w, a.x +b.x, a.y +b.y, a.z +b.z);
    }

    // return a new ifs.Quaternion whose value is (this * b)
    public Quaternion times(Quaternion b) {
        Quaternion a = this;
        return new Quaternion(
                a.w *b.w - a.x *b.x - a.y *b.y - a.z *b.z,
                a.w *b.x + a.x *b.w + a.y *b.z - a.z *b.y,
                a.w *b.y - a.x *b.z + a.y *b.w + a.z *b.x,
                a.w *b.z + a.x *b.y - a.y *b.x + a.z *b.w);
    }

    // return a new ifs.Quaternion whose value is the inverse of this
    public Quaternion inverse() {
        double d = w * w + x * x + y * y + z * z;
        return new Quaternion(w /d, -x /d, -y /d, -z /d);
    }


    // return a / b
    public Quaternion divides(Quaternion b) {
        Quaternion a = this;
        return a.inverse().times(b);
    }
}