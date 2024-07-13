package project.RayTracer;

import java.util.ArrayList;
import java.util.Comparator;


public class BVHTreeBuilder {
    private static ArrayList<ArrayList<Triangle>> splitTriangles(ArrayList<Triangle> triangles, BoundingBox nodeBoundingBox) {
        int longestAxis = nodeBoundingBox.getLongestAxis();
        ArrayList<Vector> centroids = new ArrayList<>();

        for (Triangle triangle : triangles) {
            centroids.add(triangle.getCenter());
        }

        Comparator<Vector> comparator;
        switch (longestAxis) {
            case 0: comparator = Comparator.comparingDouble(v -> v.x); break;
            case 1: comparator = Comparator.comparingDouble(v -> v.y); break;
            case 2: comparator = Comparator.comparingDouble(v -> v.z); break;
            default: comparator = Comparator.comparingDouble(v -> v.x); break;
        }
        centroids.sort(comparator);

        Vector medianCentroid = centroids.get(centroids.size() / 2);
        double splitPosition = getCoordinateByAxis(medianCentroid, longestAxis);

        ArrayList<Triangle> leftTriangles = new ArrayList<>();
        ArrayList<Triangle> rightTriangles = new ArrayList<>();

        for (Triangle triangle : triangles) {
            double centroidPosition = getCoordinateByAxis(triangle.getCenter(), longestAxis);
            if (centroidPosition < splitPosition) {
                leftTriangles.add(triangle);
            } else if (centroidPosition > splitPosition) {
                rightTriangles.add(triangle);
            } else {
                if (leftTriangles.size() <= rightTriangles.size()) {
                    leftTriangles.add(triangle);
                } else {
                    rightTriangles.add(triangle);
                }
            }
        }

        ArrayList<ArrayList<Triangle>> splitTriangles = new ArrayList<>();
        splitTriangles.add(leftTriangles);
        splitTriangles.add(rightTriangles);
        return splitTriangles;
    }

    // Helper method to get coordinate based on axis
    private static double getCoordinateByAxis(Vector vector, int axis) {
        switch (axis) {
            case 0: // X-axis
                return vector.x;
            case 1: // Y-axis
                return vector.y;
            case 2: // Z-axis
                return vector.z;
            default: // Default to X-axis
                return vector.x;
        }
    }

private static BoundingBox computeBoundingBox(ArrayList<Triangle> triangles) {
    Vector minCorner = new Vector(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    Vector maxCorner = new Vector(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

    for (Triangle triangle : triangles) {
        for (int i = 0; i < 3; i++) {
            Vector vertex = triangle.accessVertices(i + 1).getPos();
            minCorner = Vector.min(minCorner, vertex);
            maxCorner = Vector.max(maxCorner, vertex);
        }
    }

    // Validate that all vertices are within the bounding box
    for (Triangle triangle : triangles) {
        for (int i = 0; i < 3; i++) {
            Vector vertex = triangle.accessVertices(i + 1).getPos();
            if (!isWithinBounds(vertex, minCorner, maxCorner)) {
                throw new IllegalStateException("Bounding box does not encompass all vertices.");
            }
        }
    }

    return new BoundingBox(minCorner, maxCorner);
}

    private static boolean isWithinBounds(Vector vertex, Vector minCorner, Vector maxCorner) {
        return (vertex.x >= minCorner.x && vertex.x <= maxCorner.x) &&
                (vertex.y >= minCorner.y && vertex.y <= maxCorner.y) &&
                (vertex.z >= minCorner.z && vertex.z <= maxCorner.z);
    }

    public static BVHNode buildBVHTree(ArrayList<Triangle> triangles) {
        // Compute the bounding box for all triangles
        BoundingBox nodeBoundingBox = computeBoundingBox(triangles);

        // Split the triangles into two groups based on the node's bounding box
        ArrayList<ArrayList<Triangle>> splitTriangles = splitTriangles(triangles, nodeBoundingBox);
        ArrayList<Triangle> leftTriangles = splitTriangles.get(0);
        ArrayList<Triangle> rightTriangles = splitTriangles.get(1);

        // Compute the bounding box for the left child node
        BoundingBox leftBoundingBox = computeBoundingBox(leftTriangles);

        // Compute the bounding box for the right child node
        BoundingBox rightBoundingBox = computeBoundingBox(rightTriangles);

        // Recursively build the BVH tree
        BVHNode leftNode = buildBVHTreeRecursive(leftTriangles, leftBoundingBox);
        BVHNode rightNode = buildBVHTreeRecursive(rightTriangles, rightBoundingBox);
        // Create root node
        BVHNode RootNode = new BVHNode(leftNode, rightNode, nodeBoundingBox);
        System.out.println("Model's Bounding Box Dimensions: " + RootNode.getBoundingBox().toString());
        return new BVHNode(leftNode, rightNode, nodeBoundingBox);
    }



    private static BVHNode buildBVHTreeRecursive(ArrayList<Triangle> triangles, BoundingBox nodeBoundingBox) {
        // Base case: if there are few triangles, create a leaf node
        if (triangles.size() <= 50) {
            return new BVHNode(triangles, nodeBoundingBox);

        }
        // Split the triangles into two groups based on the node's bounding box
        ArrayList<ArrayList<Triangle>> splitTriangles = splitTriangles(triangles, nodeBoundingBox);
        ArrayList<Triangle> leftTriangles = splitTriangles.get(0);
        ArrayList<Triangle> rightTriangles = splitTriangles.get(1);

        // Compute the bounding box for the left child node
        BoundingBox leftBoundingBox = computeBoundingBox(leftTriangles);

        // Compute the bounding box for the right child node
        BoundingBox rightBoundingBox = computeBoundingBox(rightTriangles);

        // Recursively build the BVH tree for left and right subtrees
        BVHNode leftNode = buildBVHTreeRecursive(leftTriangles, leftBoundingBox);
        BVHNode rightNode = buildBVHTreeRecursive(rightTriangles, rightBoundingBox);

        // Create an internal BVH node
        return new BVHNode(leftNode, rightNode, nodeBoundingBox);
    }
}