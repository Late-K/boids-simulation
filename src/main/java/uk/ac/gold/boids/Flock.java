package uk.ac.gold.boids;

// compiles boids with their flock systems

public class Flock {

    private final Boid boids;
    private final FlockSystem FlockSystem = new FlockSystem();
    private final SimulationParameters params;

    public Flock(SimulationParameters params) {
        this.params = params;
        this.boids = Boid.createRandom(params.numberOfBoids(), params);
    }

    public Boid getBoids() {
        return boids;
    }

    public void update(double dt) {
        FlockSystem.computeForces(boids, params);
        FlockSystem.updatePositions(boids, params, dt);
    }
}
