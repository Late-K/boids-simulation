package uk.ac.gold.boids;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {

    private Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
    private double width = primaryScreenBounds.getWidth();
    private double height = primaryScreenBounds.getHeight();

    // Visualisation timing
    private long lastTime = 0;                    // Seconds
    private double elapsedSinceLastFpsUpdate = 0; // Cumulative time
    private String currentFpsDisplay = "0";       // String to display
    private int frameCount = 0;                   // Counter for averaging

    // Simulation parameters
    private SimulationParameters params = new SimulationParameters(
            width,
            height,
            800,  // number of boids
            260,  // max speed (pixels/sec)
            280,  // max force (pixels/sec²)
            150,  // separation radius (pixels)
            150,  // align radius (pixels)
            150,  // cohesion radius (pixels)
            3,    // number of predators
            320,  // predator max speed (pixels/sec)
            350,  // predator max force (pixels/sec²)
            200,  // flee radius (pixels) - how close a boid must be to flee
            400); // predator chase radius (pixels) - how far predator can sense boids
    private Flock flock = new Flock(params);

    // Mouse drag for square obstacle creation.
    private boolean drawingObstacle = false;
    private double drawStartX;
    private double drawStartY;
    private double drawEndX;
    private double drawEndY;

    @Override
    public void start(Stage stage) {

        Canvas canvas = new Canvas(width, height);
        GraphicsContext g = canvas.getGraphicsContext2D();

        canvas.setOnMousePressed(e -> {
            drawingObstacle = true;
            drawStartX = e.getX();
            drawStartY = e.getY();
            drawEndX = drawStartX;
            drawEndY = drawStartY;
        });

        canvas.setOnMouseDragged(e -> {
            if (!drawingObstacle) {
                return;
            }
            drawEndX = e.getX();
            drawEndY = e.getY();
        });

        canvas.setOnMouseReleased(e -> {
            if (!drawingObstacle) {
                return;
            }
            drawEndX = e.getX();
            drawEndY = e.getY();
            flock.addObstacleFromCorners(drawStartX, drawStartY, drawEndX, drawEndY);
            drawingObstacle = false;
        });

        new AnimationTimer() {

            @Override
            public void handle(long now) {

                if (lastTime > 0) {

                    // Delta-time in seconds: used to calculate force in units
                    // of seconds rather than frames.
                    double dt = (now - lastTime) / 1e9; // Convert ns to seconds.

                    // Run flocking algorithm.
                    flock.update(dt);

                    // Draw background.
                    g.setFill(Color.BLACK);
                    g.fillRect(0, 0, width, height);

                    // Render obstacles.
                    ObstacleComponent obstacles = flock.getObstacles();
                    for (int i = 0; i < obstacles.size; i++) {
                        drawObstacle(g, obstacles, i);
                    }

                    // Render each boid on the canvas.
                    Boid boids = flock.getBoids();
                    for (int i = 0; i < boids.size; i++) {
                        drawBoid(g, boids, i);
                    }

                    // Render predators.
                    PredatorComponent predators = flock.getPredators();
                    for (int i = 0; i < predators.size; i++) {
                        drawPredator(g, predators, i);
                    }

                    if (drawingObstacle) {
                        drawObstaclePreview(g, drawStartX, drawStartY, drawEndX, drawEndY);
                    }

                    // Display rendering frames per seconds.
                    updateAndDrawFps(g, dt);
                }
                lastTime = now;
            }
        }.start();

        stage.setScene(new Scene(new StackPane(canvas)));
        stage.setTitle("Boids simulation");
        stage.show();
    }

    private void drawBoid(GraphicsContext g, Boid boids, int i) {

        double angle = Math.toDegrees(Math.atan2(boids.velY[i], boids.velX[i]));

        // Push graphics attributes on to a stack.
        g.save();

        // Move origin to boid position.
        g.translate(boids.posX[i], boids.posY[i]);

        // Rotate toward velocity.
        g.rotate(angle);

        double front = 12;
        double side = 6;

        double[] xs = { front, -side, -side };
        double[] ys = { 0, -side, side };

        double speed = Math.sqrt(boids.velX[i] * boids.velX[i] + boids.velY[i] * boids.velY[i]);
        g.setFill(Color.hsb((speed / params.maxSpeed()) * 360, 1, 1, 0.8));

        g.fillPolygon(xs, ys, 3);

        // Restore previous graphics attributes.
        g.restore();
    }

    private void drawObstacle(GraphicsContext g, ObstacleComponent obstacles, int i) {
        double x = obstacles.minX[i];
        double y = obstacles.minY[i];
        double w = obstacles.maxX[i] - obstacles.minX[i];
        double h = obstacles.maxY[i] - obstacles.minY[i];

        g.save();
        g.setFill(Color.rgb(180, 180, 180, 0.35));
        g.fillRect(x, y, w, h);
        g.setStroke(Color.rgb(230, 230, 230, 0.85));
        g.setLineWidth(2);
        g.strokeRect(x, y, w, h);
        g.restore();
    }

    private void drawObstaclePreview(GraphicsContext g, double startX, double startY, double endX, double endY) {
        double[] bounds = toRectangleBounds(startX, startY, endX, endY);
        g.save();
        g.setFill(Color.rgb(120, 180, 255, 0.2));
        g.fillRect(bounds[0], bounds[1], bounds[2], bounds[3]);
        g.setStroke(Color.rgb(120, 180, 255, 0.9));
        g.setLineWidth(2);
        g.strokeRect(bounds[0], bounds[1], bounds[2], bounds[3]);
        g.restore();
    }

    private double[] toRectangleBounds(double startX, double startY, double endX, double endY) {
        double[] rectangle = ObstacleComponent.toBoundsFromCorners(startX, startY, endX, endY);
        return new double[] {
                rectangle[0],
                rectangle[1],
                rectangle[2] - rectangle[0],
                rectangle[3] - rectangle[1]
        };
    }

    private void drawPredator(GraphicsContext g, PredatorComponent predators, int i) {
        double angle = Math.toDegrees(Math.atan2(predators.velY[i], predators.velX[i]));

        g.save();
        g.translate(predators.posX[i], predators.posY[i]);
        g.rotate(angle);

        double front = 20;
        double side = 10;

        double[] xs = { front, -side, -side };
        double[] ys = { 0, -side, side };

        g.setFill(Color.rgb(220, 30, 30, 0.9));
        g.fillPolygon(xs, ys, 3);

        g.restore();
    }

    public void updateAndDrawFps(GraphicsContext g, double dt) {
        elapsedSinceLastFpsUpdate += dt;
        frameCount++;

        // Only update the display string every 0.5 seconds.
        if (elapsedSinceLastFpsUpdate >= 0.5) {
            double averageFps = frameCount / elapsedSinceLastFpsUpdate;
            currentFpsDisplay = String.format("FPS: %.1f", averageFps);

            // Reset counters.
            elapsedSinceLastFpsUpdate = 0;
            frameCount = 0;
        }

        g.save();
        g.setFill(Color.WHITE);
        g.fillText(currentFpsDisplay, 20, 30);
        g.restore();
    }

    public static void main(String[] args) {
        launch();
    }
}
