package project.RayTracer;

import java.io.Serializable;
import java.util.ArrayList;

public class Triangle implements Serializable {
    private static final long serialVersionUID = 1L;
    ArrayList<Vertex> vertices = new ArrayList<>();
    private int id;
    public static int intersectionTests = 0;

    public Triangle(Vertex vOne, Vertex vTwo, Vertex vThree, int count) {
        vertices.add(vOne);
        vertices.add(vTwo);
        vertices.add(vThree);
        id = count;
    }

    public Vertex accessVertices(int listPos) {
        Vertex result;
        if (listPos == 1) {
            result = vertices.get(0);
        } else if (listPos == 2) {
            result = vertices.get(1);
        } else if (listPos == 3) {
            result = vertices.get(2);
        } else {
            result = new Vertex(new Vector(0,0,0), 0);
        }
        return result;
    }

    public Vector getCenter(){
        Vector center = new Vector(((vertices.get(0).getXCoord() + vertices.get(1).getXCoord() + vertices.get(2).getXCoord()) / 3),((vertices.get(0).getYCoord() + vertices.get(1).getYCoord() + vertices.get(2).getYCoord()) / 3), ((vertices.get(0).getZCoord() + vertices.get(1).getZCoord() + vertices.get(2).getZCoord()) / 3));
        return center;
    }

    public Vector getNormal() {
        Vector normal = ((vertices.get(1).getPos().sub(vertices.get(2).getPos())).cross(vertices.get(1).getPos().sub(vertices.get(0).getPos())));
        return normal;
    }

    public String toString() {
        return (vertices.get(0).toString() + vertices.get(1).toString() + vertices.get(2).toString());
    }

    public IntersectionInfo intersect(Ray ray) {
        intersectionTests += 1;

        Vector viewDir = ray.getDirection();
        Vector pixelSceneVector = ray.getOrigin();
        Vector normal = getNormal().normaliseR();
        // Check if the triangle is facing away from the ray
        double normalDotRayDir = normal.dot(viewDir);
        if (normalDotRayDir > 0) {
            // Triangle is facing away from the ray (backface), skip intersection
            return null;
        }

        double distanceToIntersection = (getCenter().sub(pixelSceneVector)).dot(getNormal()) / viewDir.dot(getNormal());

        if (distanceToIntersection < 0) {
            return null; // Intersection behind ray origin
        }

        //Exact intersection point
        Vector intersection = pixelSceneVector.add(viewDir.mul(distanceToIntersection));
        //Sides of the triangle
        Vector ac = accessVertices(3).getPos().sub(accessVertices(1).getPos());
        Vector ab = accessVertices(2).getPos().sub(accessVertices(1).getPos());
        //Corner of triangle to intersection point
        Vector ap = intersection.sub(accessVertices(1).getPos());
        //Calculate dot products
        double dotAC_AC = ac.dot(ac);
        double dotAC_AB = ac.dot(ab);
        double dotAB_AB = ab.dot(ab);
        double dotAC_AP = ac.dot(ap);
        double dotAB_AP = ab.dot(ap);
        //Compute inverse denominator
        double invDenom = 1 / (dotAC_AC * dotAB_AB - dotAC_AB * dotAC_AB);
        //Compute Barycentric coordinates
        double u = (dotAB_AB * dotAC_AP - dotAC_AB * dotAB_AP) * invDenom;
        double v = (dotAC_AC * dotAB_AP - dotAC_AB * dotAC_AP) * invDenom;

        if (u >= 0 && v >= 0 && (u + v) <= 1) {
            // Intersection is within the triangle
            return new IntersectionInfo(intersection, getNormal());
        }

        return null; // No intersection within triangle
    }

    public Vector computeCentroid() {
        double centerX = (vertices.get(0).getXCoord() + vertices.get(1).getXCoord() + vertices.get(2).getXCoord()) / 3.0;
        double centerY = (vertices.get(0).getYCoord() + vertices.get(1).getYCoord() + vertices.get(2).getYCoord()) / 3.0;
        double centerZ = (vertices.get(0).getZCoord() + vertices.get(1).getZCoord() + vertices.get(2).getZCoord()) / 3.0;
        return new Vector(centerX, centerY, centerZ);
    }
}