package project.RayTracer;

import java.io.Serializable;

//vectors can either represent a line/direction (ray segment) or a position
public class Vector implements Serializable {
  private static final long serialVersionUID = 1L;
  double x, y, z;

  public Vector() {}

  public Vector(double i, double j, double k) {
    x = i;
    y = j;
    z = k;
  }

  public double magnitude() {
    return Math.sqrt(x * x + y * y + z * z);
  }

  public void normalise() {
    double mag = magnitude();
    if (mag != 0) {
      x /= mag;
      y /= mag;
      z /= mag;
    }
  }

  public Vector normaliseR() {
    double mag = magnitude();
    if (mag != 0) {
      double newX = x /= mag;
      double newY = y /= mag;
      double newZ = z /= mag;
      return new Vector(newX, newY, newZ);
    }
    return new Vector (0,0,0);
  }

  public double dot(Vector a) {
    return x * a.x + y * a.y + z * a.z;
  }

  public Vector cross(Vector v2) {
    return new Vector(y*v2.z-z*v2.y, z*v2.x-x*v2.z, x*v2.y-y*v2.x);
  }


  public Vector sub(Vector a) {
    return new Vector(x - a.x, y - a.y, z - a.z);
  }
  public Vector add(Vector a) {
    return new Vector(x + a.x, y + a.y, z + a.z);
  }
  public Vector mul(double d) {
    return new Vector(d * x, d * y, d * z);
  }

  public String toString() {
    return (x + "," + y + "," + z);
  }

  public static Vector min(Vector a, Vector b) {
    double minX = Math.min(a.x, b.x);
    double minY = Math.min(a.y, b.y);
    double minZ = Math.min(a.z, b.z);
    return new Vector(minX, minY, minZ);
  }

  public static Vector max(Vector a, Vector b) {
    double maxX = Math.max(a.x, b.x);
    double maxY = Math.max(a.y, b.y);
    double maxZ = Math.max(a.z, b.z);
    return new Vector(maxX, maxY, maxZ);
  }
}