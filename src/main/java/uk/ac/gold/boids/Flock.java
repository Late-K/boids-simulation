package uk.ac.gold.boids;

// compiles boids with their flock systems

public class Flock {

    private BoidComponent boids;
    private PredatorComponent predators;
    private final ObstacleComponent obstacles;
    private final FlockSystem flockSystem = new FlockSystem();
    private final PredatorSystem predatorSystem = new PredatorSystem();

    public Flock(SimulationParameters params) {
        this.boids = BoidComponent.createRandom(params.numberOfBoids(), params);
        this.predators = PredatorComponent.createRandom(params.numberOfPredators(), params);
        this.obstacles = new ObstacleComponent(256);
    }

    public BoidComponent getBoids() {
        return boids;
    }

    public PredatorComponent getPredators() {
        return predators;
    }

    public ObstacleComponent getObstacles() {
        return obstacles;
    }

    public boolean addObstacleFromCorners(double startX, double startY, double endX, double endY) {
        return obstacles.addFromCorners(startX, startY, endX, endY, 12);
    }

    public void setBoidCount(int count, SimulationParameters params) {
        boids = BoidComponent.createWithCount(boids, count, params);
    }

    public void setPredatorCount(int count, SimulationParameters params) {
        predators = PredatorComponent.createWithCount(predators, count, params);
    }

    public void update(double dt, SimulationParameters params) {
        flockSystem.computeForces(boids, predators, obstacles, params);
        flockSystem.updatePositions(boids, obstacles, params, dt);
        predatorSystem.computeForces(predators, boids, obstacles, params);
        predatorSystem.updatePositions(predators, obstacles, params, dt);
    }
}
