package uk.ac.gold.boids;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {

    private final Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
    private final double width = primaryScreenBounds.getWidth();
    private final double height = primaryScreenBounds.getHeight();
    private final SimulationController simulationController = new SimulationController(width, height);
    private final SimulationRenderer simulationRenderer = new SimulationRenderer();

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
        SimulationControlsPane controls = new SimulationControlsPane(simulationController);
        StackPane controlsOverlay = new StackPane(controls);
        controlsOverlay.setPickOnBounds(false);
        StackPane.setAlignment(controls, Pos.TOP_LEFT);

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
            simulationController.addObstacleFromCorners(drawStartX, drawStartY, drawEndX, drawEndY);
            drawingObstacle = false;
        });

        new AnimationTimer() {

            private long lastTime = 0;

            @Override
            public void handle(long now) {

                if (lastTime > 0) {

                    // Delta-time in seconds: used to calculate force in units
                    // of seconds rather than frames.
                    double dt = (now - lastTime) / 1e9; // Convert ns to seconds.
                    simulationController.update(dt);
                    simulationRenderer.render(
                            g,
                            simulationController.getFlock(),
                            simulationController.getParams(),
                            width,
                            height,
                            dt,
                            drawingObstacle,
                            drawStartX,
                            drawStartY,
                            drawEndX,
                            drawEndY);
                }
                lastTime = now;
            }
        }.start();

        StackPane root = new StackPane(canvas, controlsOverlay);

        stage.setScene(new Scene(root));
        stage.setTitle("Boids simulation");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
