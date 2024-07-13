package project.RayTracer;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main extends Application {

    ArrayList<Triangle> triangles = new ArrayList<Triangle>();
    WritableImage img = new WritableImage(1000,1000);
    ImageView imgV = new ImageView(img);
    PixelWriter writer = img.getPixelWriter();


    Vector camPos = new Vector(-400,800,1000);
    Vector viewDir = new Vector(0,0,-1);
    Vector lightSrc = new Vector(0, 750, 500);

    BVHTreeBuilder bvhTreeBuilder = new BVHTreeBuilder();
    static String filepath = "";
    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Give the file path, format of C:\\Users\\Oliver\\Desktop\\DISSERTATION CODE\\RayTracer\\src\\main\\java");
        // Enter data using BufferReader
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        filepath = reader.readLine();

        if (filepath.equals("admin")) {
            filepath = "C:\\Users\\Oliver\\Desktop\\DISSERTATION CODE\\RayTracer\\src\\main\\java";
        }
        // Create a VBox layout for the window
        HBox hbox = new HBox();

        // Create a ComboBox for model selection
        ComboBox<String> modelComboBox = new ComboBox<>();
        modelComboBox.setPromptText("Select a model");

        // Populate the ComboBox with names of .ply files in the directory
        try {
            List<String> plyFiles = findPlyFiles(filepath);
            modelComboBox.getItems().addAll(plyFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create a Render button
        Button renderButton = new Button("Render");

        // Create checkboxes for simulation mode and drawing BVH
        CheckBox simulationModeCheckBox = new CheckBox("Simulation Mode");
        CheckBox drawBVHCheckBox = new CheckBox("Draw BVH");

        // Add action to the Render button
        renderButton.setOnAction(event -> {
            String selectedModel = modelComboBox.getValue();
            if (selectedModel != null && !selectedModel.isEmpty()) {
                // Pass the selected model to the rendering function
                System.out.println("Model: " + selectedModel);
                boolean drawBVH = drawBVHCheckBox.isSelected();
                boolean simulationMode = simulationModeCheckBox.isSelected();
                renderStart(selectedModel, drawBVH, simulationMode);

            } else {
                // Display an error message if no model is selected
                System.out.println("Please select a model.");
            }
        });

        // Add components to the VBox
        hbox.getChildren().addAll(modelComboBox, renderButton, simulationModeCheckBox, drawBVHCheckBox);
        hbox.setAlignment(Pos.CENTER);

        // Create a Scene for the window
        Scene scene = new Scene(hbox, 300, 50);

        // Set the Scene and show the window
        primaryStage.setScene(scene);
        primaryStage.setTitle("Model Selection");
        primaryStage.show();

    }

    private void startSimulationMode(BVHNode rootNode) {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            traverseAndDraw(rootNode, scanner);
            scanner.close();
        }).start();
    }

    private void traverseAndDraw(BVHNode node, Scanner scanner) {
        if (node == null) return;

        drawBoundingBox(node.getBoundingBox(), Color.RED);
        scanner.nextLine(); // Wait for input

        if (node.getLeft() != null && node.getRight() != null) {
            System.out.println("Press Enter to draw the next two nodes or type 'exit' to exit:");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                return;
            }

            traverseAndDraw(node.getLeft(), scanner);
            traverseAndDraw(node.getRight(), scanner);
        }
        System.out.println("done");
    }

    private void renderStart(String modelName, boolean drawBVH, boolean simulationMode) {
        Stage stage = new Stage();
        stage.setTitle("Rendering");
        stage.setWidth(1000);
        stage.setHeight(1000);
        stage.show();

        GridPane centralGrid = new GridPane();
        centralGrid.setStyle("-fx-background-color: transparent;");
        centralGrid.add(imgV,0,0);
        Scene newScene = new Scene(centralGrid,1920,1080);
        stage.setScene(newScene);

        double triangleFetchStart = System.nanoTime();
        System.out.println("3D Space Created");

        long heapMaxSize = Runtime.getRuntime().maxMemory();
        System.out.println("Heap Size (recommend at least 8GB for lucy model): " + heapMaxSize);
        //create triangles from file
        triangles = fetchTriangles(modelName);
        System.out.println("File Read");

        double bvhStart = System.nanoTime();
        //construct bvh tree
        BVHNode bvhTree = bvhTreeBuilder.buildBVHTree(triangles);
        System.out.println("BVH Tree Created");

        double bvhEnd = System.nanoTime();
        render(bvhTree);
        if (simulationMode == true) {
            startSimulationMode(bvhTree);
        } else if (drawBVH == true) {
            drawBVH(bvhTree);
        }


        double renderEnd = System.nanoTime();
        double triangleFetchTime = (bvhStart - triangleFetchStart) / 1000000000;
        double bvhTimeElapsed = (bvhEnd - bvhStart) / 1000000000;
        double renderTimeElapsed = (renderEnd - bvhEnd) / 1000000000;
        double totalTimeElapsed = renderTimeElapsed + bvhTimeElapsed + triangleFetchTime;
        System.out.println("Intersection Tests: " + Triangle.intersectionTests);
        //save image
        WritableImage highQualityImage = getImageViewSnapshot(imgV);
        saveImage(highQualityImage, filepath + "\\image_result2.png");
        System.out.println("Triangle Array Construction Time: " + triangleFetchTime + " Seconds | BVH Tree Construction Time: " + bvhTimeElapsed + " Seconds |" + " Render Time: " + renderTimeElapsed + " Seconds" + " | Total Time: " + totalTimeElapsed + " Seconds" + " | Combined Time: " + (renderTimeElapsed + bvhTimeElapsed) + " Seconds");
        saveImage(img, filepath + "\\image_result.png");

    }

    private List<String> findPlyFiles(String directory) throws IOException {
        Path dirPath = Paths.get(directory);
        return Files.walk(dirPath)
                .filter(path -> path.toString().endsWith(".ply"))
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());
    }

    public WritableImage getImageViewSnapshot(ImageView imageView) {
        SnapshotParameters params = new SnapshotParameters();
        return imageView.snapshot(params, null);
    }

   private void saveImage(WritableImage image, String fileName) {
        File file = new File(fileName);
        try {
            PixelReader pixelReader = image.getPixelReader();
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            int[] pixels = new int[width * height];
            pixelReader.getPixels(0, 0, width, height, javafx.scene.image.PixelFormat.getIntArgbInstance(), pixels, 0, width);
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
            ImageIO.write(bufferedImage, "png", file);
            System.out.println("Image saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to save image: " + e.getMessage());
        }
    }

    public ArrayList<Triangle> fetchTriangles(String modelName) {
        ArrayList<Triangle> triangleList = new ArrayList<Triangle>();
        ArrayList<Vertex> vertexList = new ArrayList<Vertex>();

        int lineNumber = 0;
        String data = "";
        try {
            File plyFile = new File(filepath + "\\" + modelName);
            double scalar = 100;
            camPos = new Vector(0,750, 3000);
            if (modelName.equals("lucy_fixed.ply")) {
                camPos = new Vector(-100,400,1000);
                scalar = 0.5;
            } else if (modelName.equals("dragon_vrip.ply") || modelName.equals("dragon_vrip_res2.ply") || modelName.equals("dragon_vrip_res3.ply") || modelName.equals("dragon_vrip_res4.ply") || modelName.equals("bun_zipper.ply") || modelName.equals("bun_zipper_res4.ply")) {
                camPos = new Vector(-500,1000,1000);
                scalar = 4500;
                System.out.println(scalar);
            } else if (modelName.equals("happy_vrip.ply")) {
                camPos = new Vector(-500,1150,1000);
                scalar = 4500;
            } else if (modelName.equals("xyzrgb_statuette.ply")) {
                camPos = new Vector(-500, 500, 1000);
                scalar = 2;
            } else if (modelName.equals("MiFa.ply")) {
                camPos = new Vector(-500,500, 1000);
                lightSrc = new Vector(360, 200, 200);
                scalar = 10;
            }

            Scanner myReader = new Scanner(plyFile);

            ArrayList<Triangle> triangleListR = new ArrayList<Triangle>();
            int vertexCounter = 0;
            int faceCounter = 0;
            while (myReader.hasNextLine() && !data.equals("end_header")) {
                data = myReader.nextLine();
                lineNumber += 1;
            }

            int counter = 0;
            while (myReader.hasNextLine()) {
                data = myReader.nextLine();
                lineNumber += 1;
                //if its a triangle
                if (assessTriangle(data, modelName)) {
                    String[] splited = data.split("\\s+");
                    double xCoord = Double.parseDouble(splited[0]) * scalar;
                    double yCoord = Double.parseDouble(splited[1]) * scalar;
                    double zCoord = Double.parseDouble(splited[2]) * scalar;
                    Vector pos = new Vector(xCoord,yCoord,zCoord);
                    Vertex v = new Vertex(pos,lineNumber);
                    vertexList.add(v);
                    vertexCounter += 1;
                } else {
                    String[] splited = data.split("\\s+");
                    int lineNumberVOne = Integer.parseInt(splited[1]);
                    int lineNumberVTwo = Integer.parseInt(splited[2]);
                    int lineNumberVThree = Integer.parseInt(splited[3]);
                    Vertex v1 = vertexList.get(lineNumberVOne);
                    Vertex v2 = vertexList.get(lineNumberVTwo);
                    Vertex v3 = vertexList.get(lineNumberVThree);
                    Triangle t = new Triangle(v1, v2, v3, counter);
                    triangleList.add(t);
                    faceCounter += 1;
                    counter += 1;
                }

                }
            System.out.println("Total Triangles: " + faceCounter);
            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return triangleList;
    }

    public boolean assessTriangle(String data, String modelName) {
        ArrayList<String> elementList = new ArrayList<String>();
        int split = 3;
        if (modelName.equals("bun_zipper.ply") || modelName.equals("bun_zipper_res2.ply") || modelName.equals("bun_zipper_res3.ply") || modelName.equals("bun_zipper_res4.ply")) {
            split = 5;
        }
        String[] splited = data.split("\\s+");

        String j = "";
        for (int i = 0; i < data.length(); i++) {
            j = "";
            if (data.charAt(i) != ' ') {
                j = j.concat(data.substring(i,i+1));
            }
            if (!j.equals("")) {
                elementList.add(j);
            }
        }

        if (splited.length == split) {
            return true;
        } else {
            return false;
        }

    }

    private void drawBVH(BVHNode node) {
        if (node == null) return;

        drawBoundingBox(node.getBoundingBox(), Color.RED);

        drawBVH(node.getLeft());
        drawBVH(node.getRight());
    }

    private void drawBoundingBox(BoundingBox box, Color color) {
        Vector min = box.getMin();
        Vector max = box.getMax();

        int imageWidth = (int) img.getWidth();
        int imageHeight = (int) img.getHeight();

        int xMin = (int) Math.max(Math.min(min.x + 500, max.x + 500), 0);
        int yMin = (int) Math.max(Math.min(imageHeight - max.y, imageHeight - min.y), 0);
        int xMax = (int) Math.min(Math.max(min.x + 500, max.x + 500), imageWidth - 1);
        int yMax = (int) Math.min(Math.max(imageHeight - min.y, imageHeight - max.y), imageHeight - 1);

        for (int x = xMin; x <= xMax; x++) {
            if (yMin >= 0 && yMin < imageHeight)
                writer.setColor(x, yMin, color);

            if (yMax >= 0 && yMax < imageHeight)
                writer.setColor(x, yMax, color);
        }

        for (int y = yMin; y <= yMax; y++) {
            if (xMin >= 0 && xMin < imageWidth)
                writer.setColor(xMin, y, color);

            if (xMax >= 0 && xMax < imageWidth)
                writer.setColor(xMax, y, color);
        }
    }



    public void render(BVHNode bvhRoot) {

        for (int column = 0; column < 1000; column++) {
            for (int row = 0; row < 1000; row++) {
                Vector pixelSceneVector = new Vector(camPos.x + column, camPos.y - row, camPos.z);
                Ray ray = new Ray(pixelSceneVector, viewDir);
                IntersectionInfo intersectionInfo = bvhRoot.traverse(ray);
                if (intersectionInfo != null) {
                    Color color = calculatePhongShading(intersectionInfo);
                    writer.setColor(column, row, color);
                } else {
                    writer.setColor(column, row, Color.color(0.3333,0.6745,0.9686));
                }
            }
        }
    }


    private Color calculatePhongShading(IntersectionInfo intersectionInfo) {
        Vector lightRay = lightSrc.sub(intersectionInfo.getIntersectionPoint()).normaliseR();
        Vector viewRay = camPos.sub(intersectionInfo.getIntersectionPoint()).normaliseR();
        Vector normal = intersectionInfo.getNormal().normaliseR();
        Color lightColour = Color.WHITE;
        double ambientCoefficient = 0.1;
        double ambientLightIntensity = 0.1;
        double diffuseCoefficient = 0.7;
        double specularCoefficient = 0.6;
        double shininess = 128;

        // Ambient component
        double ambient = ambientCoefficient * ambientLightIntensity;

        // Diffuse component
        double diffuse = Math.max(normal.dot(lightRay), 0) * diffuseCoefficient;


        // Specular component
        Vector reflectedLightRay = normal.mul(2 * normal.dot(lightRay)).sub(lightRay).normaliseR();
        double specular = Math.pow(Math.max(reflectedLightRay.dot(viewRay), 0), shininess) * specularCoefficient;

        // Metallic colour tint
        double metalRed = 0.753;
        double metalGreen = 0.753;
        double metalBlue = 0.753;

        // Final colour calculation with metallic tint

        double red = ambient + diffuse * metalRed + specular * lightColour.getRed();
        double green = ambient + diffuse * metalGreen  + specular * lightColour.getGreen();
        double blue = ambient + diffuse * metalBlue + specular * lightColour.getBlue();

        return Color.color(Math.min(Math.max(red, 0), 1), Math.min(Math.max(green, 0), 1), Math.min(Math.max(blue, 0), 1));
    }
}
