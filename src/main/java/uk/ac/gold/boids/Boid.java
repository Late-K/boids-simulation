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
