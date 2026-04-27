package uk.ac.gold.boids;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// draws the current simulation state on the canvas

public class SimulationRenderer {

    private double elapsedSinceLastFpsUpdate;
    private String currentFpsDisplay = "0";
    private int frameCount;

    public void render(
            GraphicsContext g,
            Flock flock,
            SimulationParameters params,
            double width,
            double height,
            double dt,
            boolean drawingObstacle,
            double drawStartX,
            double drawStartY,
            double drawEndX,
            double drawEndY) {

        g.setFill(Color.BLACK);
        g.fillRect(0, 0, width, height);

        ObstacleComponent obstacles = flock.getObstacles();
        for (int i = 0; i < obstacles.size; i++) {
            drawObstacle(g, obstacles, i);
        }

        BoidComponent boids = flock.getBoids();
        for (int i = 0; i < boids.size; i++) {
            drawBoid(g, boids, i, params);
        }

        PredatorComponent predators = flock.getPredators();
        for (int i = 0; i < predators.size; i++) {
            drawPredator(g, predators, i);
        }

        if (drawingObstacle) {
            drawObstaclePreview(g, drawStartX, drawStartY, drawEndX, drawEndY);
        }

        updateAndDrawFps(g, dt);
    }

    private void drawBoid(GraphicsContext g, BoidComponent boids, int i, SimulationParameters params) {
        double angle = Math.toDegrees(Math.atan2(boids.velY[i], boids.velX[i]));

        g.save();
        g.translate(boids.posX[i], boids.posY[i]);
        g.rotate(angle);

        double front = 12;
        double side = 6;

        double[] xs = { front, -side, -side };
        double[] ys = { 0, -side, side };

        double speed = Math.sqrt(boids.velX[i] * boids.velX[i] + boids.velY[i] * boids.velY[i]);
        g.setFill(Color.hsb((speed / params.maxSpeed()) * 360, 1, 1, 0.8));
        g.fillPolygon(xs, ys, 3);

        g.restore();
    }

    private void drawObstacle(GraphicsContext g, ObstacleComponent obstacles, int i) {
        double x = obstacles.minX[i];
        double y = obstacles.minY[i];
        double w = obstacles.maxX[i] - obstacles.minX[i];
        double h = obstacles.maxY[i] - obstacles.minY[i];

        g.save();
        g.setFill(Color.rgb(150, 150, 150, 0.25));
        g.fillRect(x, y, w, h);
        g.setStroke(Color.rgb(250, 250, 250, 0.75));
        g.strokeRect(x, y, w, h);
        g.restore();
    }

    private void drawObstaclePreview(GraphicsContext g, double startX, double startY, double endX, double endY) {
        double[] bounds = toRectangleBounds(startX, startY, endX, endY);
        g.save();
        g.setFill(Color.rgb(150, 150, 255, 0.25));
        g.fillRect(bounds[0], bounds[1], bounds[2], bounds[3]);
        g.setStroke(Color.rgb(150, 150, 255, 0.75));
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

        g.setFill(Color.rgb(225, 50, 50, 0.8));
        g.fillPolygon(xs, ys, 3);

        g.restore();
    }

    private void updateAndDrawFps(GraphicsContext g, double dt) {
        elapsedSinceLastFpsUpdate += dt;
        frameCount++;

        if (elapsedSinceLastFpsUpdate >= 0.5) {
            double averageFps = frameCount / elapsedSinceLastFpsUpdate;
            currentFpsDisplay = String.format("FPS: %.1f", averageFps);
            elapsedSinceLastFpsUpdate = 0;
            frameCount = 0;
        }

        g.save();
        g.setFill(Color.WHITE);
        g.fillText(currentFpsDisplay, 20, 30);
        g.restore();
    }
}