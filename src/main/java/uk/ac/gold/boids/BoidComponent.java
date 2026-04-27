package uk.ac.gold.boids;

import java.util.Random;

// boids

public class BoidComponent {

    public final int size;

    public final double[] posX;
    public final double[] posY;
    public final double[] velX;
    public final double[] velY;
    public final double[] accX;
    public final double[] accY;

    private static Random random = new Random(35);

    public static BoidComponent createRandom(int size, SimulationParameters params) {
        BoidComponent components = new BoidComponent(size);
        for (int i = 0; i < size; i++) {
            components.posX[i] = random.nextDouble(0, params.width());
            components.posY[i] = random.nextDouble(0, params.height());
            components.velX[i] = random.nextDouble(-params.maxSpeed(), params.maxSpeed());
            components.velY[i] = random.nextDouble(-params.maxSpeed(), params.maxSpeed());
        }
        return components;
    }

    public static BoidComponent createWithCount(BoidComponent existing, int newSize, SimulationParameters params) {
        BoidComponent next = new BoidComponent(newSize);
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

    public BoidComponent(int size) {
        this.size = size;
        posX = new double[size];
        posY = new double[size];
        velX = new double[size];
        velY = new double[size];
        accX = new double[size];
        accY = new double[size];
    }
}
