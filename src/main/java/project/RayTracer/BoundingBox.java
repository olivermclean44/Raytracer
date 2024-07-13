package project.RayTracer;

import java.io.Serializable;

public class BoundingBox implements Serializable {
    private static final long serialVersionUID = 1L;
    private Vector minCorner;
    private Vector maxCorner;


    public BoundingBox (Vector minCorner, Vector maxCorner) {
        this.minCorner = minCorner;
        this.maxCorner = maxCorner;
    }

    @Override
    public String toString() {
        return "BoundingBox{" +
                "minCorner=" + minCorner +
                ", maxCorner=" + maxCorner +
                '}';
    }

    public boolean intersect(Vector rayOrigin, Vector rayDirection) {
        double tMin = (minCorner.x - rayOrigin.x) / rayDirection.x;
        double tMax = (maxCorner.x - rayOrigin.x) / rayDirection.x;
        double tyMin = (minCorner.y - rayOrigin.y) / rayDirection.y;
        double tyMax = (maxCorner.y - rayOrigin.y) / rayDirection.y;
        double tzMin = (minCorner.z - rayOrigin.z) / rayDirection.z;
        double tzMax = (maxCorner.z - rayOrigin.z) / rayDirection.z;

        if ((tMin > tyMax) || (tyMin > tMax)) {
            return false;
        }

        if (tyMin > tMin) {
            tMin = tyMin;
        }

        if (tyMax < tMax) {
            tMax = tyMax;
        }

        if ((tMin > tzMax) || (tzMin > tMax)) {
            return false;
        }

        return true;
    }

    public int getLongestAxis() {
        Vector extent = maxCorner.sub(minCorner);
        if (extent.x >= extent.y && extent.x >= extent.z) {
            return 0; // X-axis
        } else if (extent.y >= extent.z) {
            return 1; // Y-axis
        } else {
            return 2; // Z-axis
        }
    }

    public Vector getMin() {
        return minCorner;
    }

    public Vector getMax() {
        return maxCorner;
    }
}
