package uk.ac.gold.boids;

import java.util.stream.IntStream;

// chasing behaviour for each predator

public class PredatorSystem {

    public void computeForces(PredatorComponent predators, Boid boids, SimulationParameters params) {
        IntStream.range(0, predators.size).parallel().forEach(i -> {
            Vector2 acc = steer(i, predators, boids, params)
                    .add(separatePredators(i, predators, params))
                    .add(avoidEdges(i, predators, params));
            predators.accX[i] = acc.x;
            predators.accY[i] = acc.y;
        });
    }

    public void updatePositions(PredatorComponent predators, SimulationParameters params, double dt) {
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

            clampToBounds(i, predators, params);
        }
    }

    private Vector2 steer(int i, PredatorComponent predators, Boid boids, SimulationParameters params) {
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
            // Chase
            Vector2 target = new Vector2(boids.posX[nearestIdx], boids.posY[nearestIdx]);
            return target.subtract(pos)
                    .normalize()
                    .multiply(params.predatorMaxSpeed())
                    .subtract(vel)
                    .limit(params.predatorMaxForce());
        } else {
            // Wander
            return wander(i, predators, params);
        }
    }

    private Vector2 wander(int i, PredatorComponent predators, SimulationParameters params) {
        predators.wanderAngle[i] += (Math.random() - 0.5) * 0.4;

        double vx = predators.velX[i];
        double vy = predators.velY[i];
        double speed = Math.sqrt(vx * vx + vy * vy);

        double cx = speed > 1e-6 ? predators.posX[i] + (vx / speed) * 150 : predators.posX[i] + 150;
        double cy = speed > 1e-6 ? predators.posY[i] + (vy / speed) * 150 : predators.posY[i];

        double tx = cx + Math.cos(predators.wanderAngle[i]) * 80;
        double ty = cy + Math.sin(predators.wanderAngle[i]) * 80;

        Vector2 pos = new Vector2(predators.posX[i], predators.posY[i]);
        Vector2 vel = new Vector2(vx, vy);
        return new Vector2(tx - pos.x, ty - pos.y)
                .normalize()
                .multiply(params.predatorMaxSpeed() * 0.6)
                .subtract(vel)
                .limit(params.predatorMaxForce() * 0.5);
    }

    private Vector2 separatePredators(int i, PredatorComponent predators, SimulationParameters params) {
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

    private Vector2 avoidEdges(int i, PredatorComponent predators, SimulationParameters params) {
        double margin = params.width() * 0.12;
        double strength = params.predatorMaxForce() * 3.0;
        double sx = 0, sy = 0;
        if (predators.posX[i] < margin) {
            double t = 1.0 - (predators.posX[i] / margin);
            sx += strength * t;
        } else if (predators.posX[i] > params.width() - margin) {
            double t = 1.0 - ((params.width() - predators.posX[i]) / margin);
            sx -= strength * t;
        }
        if (predators.posY[i] < margin) {
            double t = 1.0 - (predators.posY[i] / margin);
            sy += strength * t;
        } else if (predators.posY[i] > params.height() - margin) {
            double t = 1.0 - ((params.height() - predators.posY[i]) / margin);
            sy -= strength * t;
        }
        return new Vector2(sx, sy);
    }

    private void clampToBounds(int i, PredatorComponent predators, SimulationParameters params) {
        if (predators.posX[i] < 0) { predators.posX[i] = 0; predators.velX[i] = Math.abs(predators.velX[i]); }
        else if (predators.posX[i] > params.width()) { predators.posX[i] = params.width(); predators.velX[i] = -Math.abs(predators.velX[i]); }
        if (predators.posY[i] < 0) { predators.posY[i] = 0; predators.velY[i] = Math.abs(predators.velY[i]); }
        else if (predators.posY[i] > params.height()) { predators.posY[i] = params.height(); predators.velY[i] = -Math.abs(predators.velY[i]); }
    }
}
