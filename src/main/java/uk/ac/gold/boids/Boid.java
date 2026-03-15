package uk.ac.gold.boids;

import java.util.List;
import java.util.Random;

public class Boid {

    public Vector2 position, velocity;
    public Vector2 acceleration = new Vector2(0, 0);

    private SimulationParameters params;

    private static Random random = new Random(35);

    public static Boid createRandomBoid(
            SimulationParameters params) {
        Vector2 position = new Vector2(
                random.nextDouble(0, params.width()),
                random.nextDouble(0, params.height()));
        Vector2 velocity = new Vector2(
                random.nextDouble(-params.maxSpeed(), params.maxSpeed()),
                random.nextDouble(-params.maxSpeed(), params.maxSpeed()));
        return new Boid(position, velocity, params);
    }

    public Boid(
            Vector2 position,
            Vector2 velocity,
            SimulationParameters params) {
        this.position = position;
        this.velocity = velocity;
        this.params = params;
    }

    public double direction() {
        return Math.atan2(velocity.y(), velocity.x());
    }

    public void computeForce(List<Boid> boids) {

        acceleration = acceleration.add(separation(boids))
                .add(alignment(boids))
                .add(cohesion(boids))
                .add(avoidEdges());
    }

    public void updatePosition(double dt) {
        velocity = velocity.add(acceleration.multiply(dt)).limit(params.maxSpeed());
        position = position.add(velocity.multiply(dt));

        // Reset acceleration vector to zero for next frame.
        acceleration = new Vector2(0, 0);
    }

    private Vector2 separation(List<Boid> boids) {
        Vector2 separation = new Vector2(0, 0);
        int count = 0;

        for (Boid other : boids) {
            double d = position.distance(other.position);

            if (d > 0 && d < params.separationRadius()) {
                Vector2 diff = position.subtract(other.position).normalize().divide(d);
                separation = separation.add(diff);
                count++;
            }
        }

        if (count > 0) {
            separation = separation.divide(count)
                    .normalize()
                    .multiply(params.maxSpeed())
                    .subtract(velocity)
                    .limit(params.maxForce());
            return separation;
        }
        return new Vector2(0, 0);
    }

    private Vector2 alignment(List<Boid> boids) {
        Vector2 alignment = new Vector2(0, 0);
        int count = 0;

        for (Boid other : boids) {
            double d = position.distance(other.position);

            if (d > 0 && d < params.alignRadius()) {
                alignment = alignment.add(other.velocity);
                count++;
            }
        }

        if (count > 0) {
            alignment = alignment.divide(count)
                    .normalize()
                    .multiply(params.maxSpeed())
                    .subtract(velocity)
                    .limit(params.maxForce());
            return alignment;
        }
        return new Vector2(0, 0);
    }

    private Vector2 cohesion(List<Boid> boids) {
        Vector2 cohesion = new Vector2(0, 0);
        int count = 0;

        for (Boid other : boids) {
            double d = position.distance(other.position);

            if (d > 0 && d < params.cohesionRadius()) {
                cohesion = cohesion.add(other.position);
                count++;
            }
        }

        if (count > 0) {
            cohesion = cohesion.divide(count)
                    .subtract(position)
                    .normalize()
                    .multiply(params.maxSpeed())
                    .subtract(velocity)
                    .limit(params.maxForce());
            return cohesion;
        }
        return new Vector2(0, 0);
    }

    private Vector2 avoidEdges() {
        double margin = params.width() * 0.1;       // 10% screen width
        double turnForce = params.maxForce() / 2.5; // pixels/sec²
        Vector2 steer = new Vector2(0, 0);
        if (position.x() < margin)
            steer = steer.add(new Vector2(turnForce, 0));
        else if (position.x() > params.width() - margin)
            steer = steer.add(new Vector2(-turnForce, 0));
        if (position.y() < margin)
            steer = steer.add(new Vector2(0, turnForce));
        else if (position.y() > params.height() - margin)
            steer = steer.add(new Vector2(0, -turnForce));
        return steer;
    }
}
