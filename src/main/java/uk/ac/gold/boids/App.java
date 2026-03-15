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
    private Flock flock = new Flock();
    private SimulationParameters params = new SimulationParameters(
            width,
            height,
            800,  // number of boids
            260,  // max speed (pixels/sec)
            280,  // max force (pixels/sec²)
            150,  // separation radius (pixels)
            150,  // align radius (pixels)
            150); // cohesion radius (pixels)

    @Override
    public void start(Stage stage) {

        Canvas canvas = new Canvas(width, height);
        GraphicsContext g = canvas.getGraphicsContext2D();

        for (int i = 0; i < params.numberOfBoids(); i++) {
            flock.addBoid(Boid.createRandomBoid(params));
        }

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

                    // Render each boid on the canvas.
                    for (Boid b : flock.getBoids()) {
                        drawBoid(g, b);
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

    private void drawBoid(GraphicsContext g, Boid b) {

        double angle = Math.toDegrees(b.direction());

        // Push graphics attributes on to a stack.
        g.save();

        // Move origin to boid position.
        g.translate(b.position.x(), b.position.y());

        // Rotate toward velocity.
        g.rotate(angle);

        double front = 12;
        double side = 6;

        double[] xs = { front, -side, -side };
        double[] ys = { 0, -side, side };

        double speed = b.velocity.magnitude();
        g.setFill(Color.hsb((speed / params.maxSpeed()) * 360, 1, 1, 0.8));

        g.fillPolygon(xs, ys, 3);

        // Restore previous graphics attributes.
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
