package uk.ac.gold.boids;

import java.util.ArrayList;
import java.util.List;

public class Flock {

    private final List<Boid> boids = new ArrayList<>();

    public void addBoid(Boid b) {
        boids.add(b);
    }

    public List<Boid> getBoids() {
        return boids;
    }

    public void update(double dt) {
        computeForces();
        updatePositions(dt);
    }

    private void computeForces() {
        for (Boid b : boids) {
            b.computeForce(boids);
        }
    }

    private void updatePositions(double dt) {
        for (Boid b : boids) {
            b.updatePosition(dt);
        }
    }
}
