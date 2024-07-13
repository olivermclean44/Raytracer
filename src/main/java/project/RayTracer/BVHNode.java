package project.RayTracer;

import java.io.Serializable;
import java.util.ArrayList;

public class BVHNode implements Serializable {
    private static final long serialVersionUID = 1L;
    private BVHNode leftNode;
    private BVHNode rightNode;
    private BoundingBox boundingBox;
    private ArrayList<Triangle> triangles;

    //Constructor for internal nodes
    public BVHNode(BVHNode leftNode, BVHNode rightNode, BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    // Constructor for leaf nodes
    public BVHNode(ArrayList<Triangle> triangles, BoundingBox boundingBox) {
        this.triangles = triangles;
        this.boundingBox = boundingBox;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    @Override
    public String toString() {
        return toStringHelper(this, 0);
    }

    private String toStringHelper(BVHNode node, int depth) {
        StringBuilder sb = new StringBuilder();
        String indent = "  ".repeat(depth); // Adjust the indentation as needed

        sb.append(indent).append("Node:\n");
        sb.append(indent).append("  Bounding Box: ").append(node.boundingBox).append("\n");

        if (node.leftNode != null) {
            sb.append(indent).append("  Left Child:\n");
            sb.append(toStringHelper(node.leftNode, depth + 1));
        }

        if (node.rightNode != null) {
            sb.append(indent).append("  Right Child:\n");
            sb.append(toStringHelper(node.rightNode, depth + 1));
        }

        return sb.toString();
    }

    public IntersectionInfo traverse(Ray ray) {
        // First, check if the ray intersects the bounding box of this node
        if (!boundingBox.intersect(ray.getOrigin(), ray.getDirection())) {
            // If not, return null to indicate no intersection
            return null;
        }
        // If this is a leaf node (it contains triangles)
        if (triangles != null) {
            Triangle closestTriangle = null;
            IntersectionInfo closestIntersection = null;
            // Iterate over each triangle in this leaf node
            for (Triangle triangle : triangles) {
                // Test for intersection between the ray and the triangle
                IntersectionInfo intersection = triangle.intersect(ray);
                // If there is an intersection, update the closestIntersection
                if (intersection != null && (closestIntersection == null || intersection.getDistance() < closestIntersection.getDistance())) {
                    closestIntersection = intersection;
                    closestTriangle = triangle;
                }
            }

            return closestIntersection;
        }
        IntersectionInfo leftIntersection = leftNode.traverse(ray);
        IntersectionInfo rightIntersection = rightNode.traverse(ray);
        // Determine the closest intersection between the two children
        if (leftIntersection == null) {
            return rightIntersection;
        } else if (rightIntersection == null) {
            return leftIntersection;
        } else {
            return leftIntersection.getDistance() < rightIntersection.getDistance() ? leftIntersection : rightIntersection;
        }
    }

    public BVHNode getLeft() {
        return leftNode;
    }

    public BVHNode getRight() {
        return rightNode;
    }
}