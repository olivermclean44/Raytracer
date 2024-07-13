package project.RayTracer;
public class IntersectionInfo {
    private Vector intersectionPoint;
    private Vector normal;
    private Triangle closestTriangle;
    private double distance;

    public IntersectionInfo(Vector intersectionPoint, Vector normal) {
        this.intersectionPoint = intersectionPoint;
        this.normal = normal;
    }

    public Vector getIntersectionPoint() {
        return intersectionPoint;
    }

    public Vector getNormal() {
        return normal;
    }

    public double getDistance() {
        return distance;
    }
}