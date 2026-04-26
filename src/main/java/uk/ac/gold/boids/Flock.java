package uk.ac.gold.boids;

// compiles boids with their flock systems

public class Flock {

    private final Boid boids;
    private final PredatorComponent predators;
    private final FlockSystem flockSystem = new FlockSystem();
    private final PredatorSystem predatorSystem = new PredatorSystem();
    private final SimulationParameters params;

    public Flock(SimulationParameters params) {
        this.params = params;
        this.boids = Boid.createRandom(params.numberOfBoids(), params);
        this.predators = PredatorComponent.createRandom(params.numberOfPredators(), params);
    }

    public Boid getBoids() {
        return boids;
    }

    public PredatorComponent getPredators() {
        return predators;
    }

    public void update(double dt) {
        flockSystem.computeForces(boids, predators, params);
        flockSystem.updatePositions(boids, params, dt);
        predatorSystem.computeForces(predators, boids, params);
        predatorSystem.updatePositions(predators, params, dt);
    }
}
