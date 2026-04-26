package uk.ac.gold.boids;

import java.util.Random;

// boids

public class Boid {

    public final int size;

    public final double[] posX;
    public final double[] posY;
    public final double[] velX;
    public final double[] velY;
    public final double[] accX;
    public final double[] accY;

    private static Random random = new Random(35);

    public static Boid createRandom(int size, SimulationParameters params) {
        Boid components = new Boid(size);
        for (int i = 0; i < size; i++) {
            components.posX[i] = random.nextDouble(0, params.width());
            components.posY[i] = random.nextDouble(0, params.height());
            components.velX[i] = random.nextDouble(-params.maxSpeed(), params.maxSpeed());
            components.velY[i] = random.nextDouble(-params.maxSpeed(), params.maxSpeed());
        }
        return components;
    }

    public static Boid createWithCount(Boid existing, int newSize, SimulationParameters params) {
        Boid next = new Boid(newSize);
        int copy = Math.min(existing.size, newSize);
        System.arraycopy(existing.posX, 0, next.posX, 0, copy);
        System.arraycopy(existing.posY, 0, next.posY, 0, copy);
        System.arraycopy(existing.velX, 0, next.velX, 0, copy);
        System.arraycopy(existing.velY, 0, next.velY, 0, copy);
        for (int i = copy; i < newSize; i++) {
            next.posX[i] = random.nextDouble(0, params.width());
            next.posY[i] = random.nextDouble(0, params.height());
            next.velX[i] = random.nextDouble(-params.maxSpeed(), params.maxSpeed());
            next.velY[i] = random.nextDouble(-params.maxSpeed(), params.maxSpeed());
        }
        return next;
    }

    public Boid(int size) {
        this.size = size;
        posX = new double[size];
        posY = new double[size];
        velX = new double[size];
        velY = new double[size];
        accX = new double[size];
        accY = new double[size];
    }
}
