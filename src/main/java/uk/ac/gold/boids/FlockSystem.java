package uk.ac.gold.boids;

import java.util.stream.IntStream;

// flocking behaviour for each boid

public class FlockSystem {

    private final ObstacleSystem obstacleSystem = new ObstacleSystem();

    public void computeForces(Boid boids, PredatorComponent predators, ObstacleComponent obstacles, SimulationParameters params) {
        IntStream.range(0, boids.size).parallel().forEach(i -> {
            Vector2 acc = separation(i, boids, params)
                    .add(alignment(i, boids, params))
                    .add(cohesion(i, boids, params))
                    .add(avoidEdges(i, boids, params))
                    .add(fleeFromPredators(i, boids, predators, params))
                    .add(obstacleSystem.avoid(boids.posX[i], boids.posY[i], params.maxForce(), obstacles, params.separationRadius() * 0.75));
            boids.accX[i] = acc.x;
            boids.accY[i] = acc.y;
        });
    }

    public void updatePositions(Boid boids, ObstacleComponent obstacles, SimulationParameters params, double dt) {
        for (int i = 0; i < boids.size; i++) {
            Vector2 vel = new Vector2(boids.velX[i], boids.velY[i]);
            Vector2 acc = new Vector2(boids.accX[i], boids.accY[i]);

            vel.add(acc.multiply(dt)).limit(params.maxSpeed());

            boids.velX[i] = vel.x;
            boids.velY[i] = vel.y;
            boids.posX[i] += vel.x * dt;
            boids.posY[i] += vel.y * dt;

            // Reset acceleration vector to zero for next frame.
            boids.accX[i] = 0;
            boids.accY[i] = 0;

            obstacleSystem.resolveCollision(boids.posX, boids.posY, boids.velX, boids.velY, i, obstacles, 4);
        }
    }

    private Vector2 separation(int i, Boid boids, SimulationParameters params) {
        Vector2 pos = new Vector2(boids.posX[i], boids.posY[i]);
        Vector2 vel = new Vector2(boids.velX[i], boids.velY[i]);
        Vector2 steering = new Vector2(0, 0);
        Vector2 diff = new Vector2(0, 0);
        int count = 0;

        for (int j = 0; j < boids.size; j++) {
            double d = pos.distance(boids.posX[j], boids.posY[j]);
            if (d > 0 && d < params.separationRadius()) {
                diff.set(pos.x - boids.posX[j], pos.y - boids.posY[j]).normalize().divide(d);
                steering.add(diff);
                count++;
            }
        }

        if (count > 0) {
            return steering.divide(count)
                    .normalize()
                    .multiply(params.maxSpeed())
                    .subtract(vel)
                    .limit(params.maxForce());
        }
        return steering.set(0, 0);
    }

    private Vector2 alignment(int i, Boid boids, SimulationParameters params) {
        Vector2 pos = new Vector2(boids.posX[i], boids.posY[i]);
        Vector2 vel = new Vector2(boids.velX[i], boids.velY[i]);
        Vector2 steering = new Vector2(0, 0);
        Vector2 neighbourVel = new Vector2(0, 0);
        int count = 0;

        for (int j = 0; j < boids.size; j++) {
            double d = pos.distance(boids.posX[j], boids.posY[j]);
            if (d > 0 && d < params.alignRadius()) {
                steering.add(neighbourVel.set(boids.velX[j], boids.velY[j]));
                count++;
            }
        }

        if (count > 0) {
            return steering.divide(count)
                    .normalize()
                    .multiply(params.maxSpeed())
                    .subtract(vel)
                    .limit(params.maxForce());
        }
        return steering.set(0, 0);
    }

    private Vector2 cohesion(int i, Boid boids, SimulationParameters params) {
        Vector2 pos = new Vector2(boids.posX[i], boids.posY[i]);
        Vector2 vel = new Vector2(boids.velX[i], boids.velY[i]);
        Vector2 centroid = new Vector2(0, 0);
        int count = 0;

        for (int j = 0; j < boids.size; j++) {
            double d = pos.distance(boids.posX[j], boids.posY[j]);
            if (d > 0 && d < params.cohesionRadius()) {
                centroid.x += boids.posX[j];
                centroid.y += boids.posY[j];
                count++;
            }
        }

        if (count > 0) {
            return centroid.divide(count)
                    .subtract(pos)
                    .normalize()
                    .multiply(params.maxSpeed())
                    .subtract(vel)
                    .limit(params.maxForce());
        }
        return centroid.set(0, 0);
    }

    private Vector2 fleeFromPredators(int i, Boid boids, PredatorComponent predators, SimulationParameters params) {
        Vector2 pos = new Vector2(boids.posX[i], boids.posY[i]);
        Vector2 vel = new Vector2(boids.velX[i], boids.velY[i]);
        Vector2 steering = new Vector2(0, 0);
        int count = 0;

        for (int j = 0; j < predators.size; j++) {
            double d = pos.distance(predators.posX[j], predators.posY[j]);
            if (d > 0 && d < params.fleeRadius()) {
                Vector2 diff = new Vector2(pos.x - predators.posX[j], pos.y - predators.posY[j])
                        .normalize()
                        .divide(d);
                steering.add(diff);
                count++;
            }
        }

        if (count > 0) {
            return steering.divide(count)
                    .normalize()
                    .multiply(params.maxSpeed())
                    .subtract(vel)
                    .limit(params.maxForce() * 1.2);
        }
        return new Vector2(0, 0);
    }

    private Vector2 avoidEdges(int i, Boid boids, SimulationParameters params) {
        double margin = params.width() * 0.1;
        double turnForce = params.maxForce() / 2.5;
        double sx = 0, sy = 0;
        if (boids.posX[i] < margin)                        sx += turnForce;
        else if (boids.posX[i] > params.width() - margin)  sx -= turnForce;
        if (boids.posY[i] < margin)                        sy += turnForce;
        else if (boids.posY[i] > params.height() - margin) sy -= turnForce;
        return new Vector2(sx, sy);
    }
}
