package project.RayTracer;

import java.io.Serializable;
import java.util.ArrayList;

public class Vertex implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<Triangle> adjacentTriangles;
    private Vector pos;
    int index = 0;

    public Vertex(Vector Vpos, int index) {
        pos = Vpos;
        adjacentTriangles = new ArrayList<>();
        this.index = index;
    }

    public double getXCoord() {
        return pos.x;
    }

    public double getYCoord() {
        return pos.y;
    }

    public double getZCoord() {
        return pos.z;
    }

    public Vector getPos() {
        return pos;
    }

    public String toString() {
        return ("(" + pos + ")");
    }
}