package uk.ac.gold.boids;

import java.util.stream.IntStream;

// flocking and chasing behaviour for each predator

public class PredatorSystem {

    private final ObstacleSystem obstacleSystem = new ObstacleSystem();

    public void computeForces(PredatorComponent predators, BoidComponent boids, ObstacleComponent obstacles, SimulationParameters params) {
        IntStream.range(0, predators.size).parallel().forEach(i -> {
            Vector2 acc = avoidEdges(i, predators, params)
                .add(obstacleSystem.avoid(predators.posX[i], predators.posY[i], params.predatorMaxForce(), obstacles, params.fleeRadius() * 0.4));

            if (!isOutsideCanvas(i, predators, params)) {
            acc.add(chaseOrIdle(i, predators, boids, params))
                .add(separation(i, predators, params));
            }

            predators.accX[i] = acc.x;
            predators.accY[i] = acc.y;
        });
    }

        private boolean isOutsideCanvas(int i, PredatorComponent predators, SimulationParameters params) {
        return predators.posX[i] < 0
            || predators.posX[i] > params.width()
            || predators.posY[i] < 0
            || predators.posY[i] > params.height();
        }

    public void updatePositions(PredatorComponent predators, ObstacleComponent obstacles, SimulationParameters params, double dt) {
        for (int i = 0; i < predators.size; i++) {
            Vector2 vel = new Vector2(predators.velX[i], predators.velY[i]);
            Vector2 acc = new Vector2(predators.accX[i], predators.accY[i]);

            vel.add(acc.multiply(dt)).limit(params.predatorMaxSpeed());

            predators.velX[i] = vel.x;
            predators.velY[i] = vel.y;
            predators.posX[i] += vel.x * dt;
            predators.posY[i] += vel.y * dt;

            // Reset acceleration vector to zero for next frame.
            predators.accX[i] = 0;
            predators.accY[i] = 0;

            obstacleSystem.resolveCollision(predators.posX, predators.posY, predators.velX, predators.velY, i, obstacles, 8);
        }
    }

    private Vector2 separation(int i, PredatorComponent predators, SimulationParameters params) {
        double separationRadius = params.width() * 0.25;
        Vector2 pos = new Vector2(predators.posX[i], predators.posY[i]);
        Vector2 vel = new Vector2(predators.velX[i], predators.velY[i]);
        Vector2 steering = new Vector2(0, 0);
        Vector2 diff = new Vector2(0, 0);
        int count = 0;

        for (int j = 0; j < predators.size; j++) {
            double d = pos.distance(predators.posX[j], predators.posY[j]);
            if (d > 0 && d < separationRadius) {
                diff.set(pos.x - predators.posX[j], pos.y - predators.posY[j]).normalize().divide(d);
                steering.add(diff);
                count++;
            }
        }

        if (count > 0) {
            return steering.divide(count)
                    .normalize()
                    .multiply(params.predatorMaxSpeed())
                    .subtract(vel)
                    .limit(params.predatorMaxForce() * 0.8);
        }
        return steering.set(0, 0);
    }

    private Vector2 alignment(int i, PredatorComponent predators, SimulationParameters params) {
        double alignRadius = params.predatorChaseRadius() * 0.5;
        Vector2 pos = new Vector2(predators.posX[i], predators.posY[i]);
        Vector2 vel = new Vector2(predators.velX[i], predators.velY[i]);
        Vector2 steering = new Vector2(0, 0);
        Vector2 neighbourVel = new Vector2(0, 0);
        int count = 0;

        for (int j = 0; j < predators.size; j++) {
            double d = pos.distance(predators.posX[j], predators.posY[j]);
            if (d > 0 && d < alignRadius) {
                steering.add(neighbourVel.set(predators.velX[j], predators.velY[j]));
                count++;
            }
        }

        if (count > 0) {
            return steering.divide(count)
                    .normalize()
                    .multiply(params.predatorMaxSpeed())
                    .subtract(vel)
                    .limit(params.predatorMaxForce() * 0.45);
        }
        return steering.set(0, 0);
    }

    private Vector2 cohesion(int i, PredatorComponent predators, SimulationParameters params) {
        double cohesionRadius = params.predatorChaseRadius() * 0.5;
        Vector2 pos = new Vector2(predators.posX[i], predators.posY[i]);
        Vector2 vel = new Vector2(predators.velX[i], predators.velY[i]);
        Vector2 centroid = new Vector2(0, 0);
        int count = 0;

        for (int j = 0; j < predators.size; j++) {
            double d = pos.distance(predators.posX[j], predators.posY[j]);
            if (d > 0 && d < cohesionRadius) {
                centroid.x += predators.posX[j];
                centroid.y += predators.posY[j];
                count++;
            }
        }

        if (count > 0) {
            return centroid.divide(count)
                    .subtract(pos)
                    .normalize()
                    .multiply(params.predatorMaxSpeed() * 0.75)
                    .subtract(vel)
                    .limit(params.predatorMaxForce() * 0.35);
        }
        return centroid.set(0, 0);
    }

    private Vector2 chaseOrIdle(int i, PredatorComponent predators, BoidComponent boids, SimulationParameters params) {
        Vector2 pos = new Vector2(predators.posX[i], predators.posY[i]);
        Vector2 vel = new Vector2(predators.velX[i], predators.velY[i]);

        double nearestDist = Double.MAX_VALUE;
        int nearestIdx = -1;

        for (int j = 0; j < boids.size; j++) {
            double d = pos.distance(boids.posX[j], boids.posY[j]);
            if (d < nearestDist) {
                nearestDist = d;
                nearestIdx = j;
            }
        }

        if (nearestIdx >= 0 && nearestDist <= params.predatorChaseRadius()) {
            // chase nearest boid when one is close enough.
            Vector2 target = new Vector2(boids.posX[nearestIdx], boids.posY[nearestIdx]);
            return target.subtract(pos)
                    .normalize()
                    .multiply(params.predatorMaxSpeed())
                    .subtract(vel)
                    .limit(params.predatorMaxForce());
        }
        // idle using flock-style motion.
        return alignment(i, predators, params)
                .add(cohesion(i, predators, params));
    }

    private Vector2 avoidEdges(int i, PredatorComponent predators, SimulationParameters params) {
        double margin = params.width() * 0.1;
        double turnForce = params.predatorMaxForce() / 2.5;
        double sx = 0, sy = 0;
        if (predators.posX[i] < margin)                            sx += turnForce;
        else if (predators.posX[i] > params.width() - margin)      sx -= turnForce;
        if (predators.posY[i] < margin)                            sy += turnForce;
        else if (predators.posY[i] > params.height() - margin)     sy -= turnForce;
        return new Vector2(sx, sy);
    }
}
