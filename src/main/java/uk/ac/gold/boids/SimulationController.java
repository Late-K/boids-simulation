package uk.ac.gold.boids;

// owns simulation state and parameter updates

public class SimulationController {

    private final double width;
    private final double height;
    private final SimulationParameters defaultParams;

    private SimulationParameters params;
    private Flock flock;
    private boolean paused;

    public SimulationController(double width, double height) {
        this.width = width;
        this.height = height;
        this.defaultParams = createDefaultParameters();
        this.params = defaultParams;
        this.flock = new Flock(params);
    }

    public SimulationParameters getParams() {
        return params;
    }

    public SimulationParameters getDefaultParams() {
        return defaultParams;
    }

    public Flock getFlock() {
        return flock;
    }

    public boolean isPaused() {
        return paused;
    }

    public void update(double dt) {
        if (!paused) {
            flock.update(dt, params);
        }
    }

    public void togglePaused() {
        paused = !paused;
    }

    public void reset() {
        params = defaultParams;
        flock = new Flock(params);
        paused = false;
    }

    public boolean addObstacleFromCorners(double startX, double startY, double endX, double endY) {
        return flock.addObstacleFromCorners(startX, startY, endX, endY);
    }

    public void setBoidCount(int count) {
        params = new SimulationParameters(
                width, height,
                count,
                params.maxSpeed(), params.maxForce(),
                params.separationRadius(), params.alignRadius(), params.cohesionRadius(),
                params.numberOfPredators(),
                params.predatorMaxSpeed(), params.predatorMaxForce(),
                params.fleeRadius(), params.predatorChaseRadius());
        flock.setBoidCount(count, params);
    }

    public void setPredatorCount(int count) {
        params = new SimulationParameters(
                width, height,
                params.numberOfBoids(),
                params.maxSpeed(), params.maxForce(),
                params.separationRadius(), params.alignRadius(), params.cohesionRadius(),
                count,
                params.predatorMaxSpeed(), params.predatorMaxForce(),
                params.fleeRadius(), params.predatorChaseRadius());
        flock.setPredatorCount(count, params);
    }

    public void updateTuning(
            double maxSpeed,
            double maxForce,
            double separationRadius,
            double alignRadius,
            double cohesionRadius,
            double predatorMaxSpeed,
            double predatorMaxForce,
            double fleeRadius,
            double predatorChaseRadius) {
        params = new SimulationParameters(
                width,
                height,
                params.numberOfBoids(),
                maxSpeed,
                maxForce,
                separationRadius,
                alignRadius,
                cohesionRadius,
                params.numberOfPredators(),
                predatorMaxSpeed,
                predatorMaxForce,
                fleeRadius,
                predatorChaseRadius);
    }

    private SimulationParameters createDefaultParameters() {
        return new SimulationParameters(
                width,
                height,
                800,
                260,
                280,
                150,
                150,
                150,
                3,
                320,
                350,
                200,
                400);
    }
}