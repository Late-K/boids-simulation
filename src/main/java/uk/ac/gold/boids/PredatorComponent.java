package uk.ac.gold.boids;

import java.util.Random;

// predators

public class PredatorComponent {

    public final int size;

    public final double[] posX;
    public final double[] posY;
    public final double[] velX;
    public final double[] velY;
    public final double[] accX;
    public final double[] accY;
    public final double[] wanderAngle;

    private static Random random = new Random(99);

    public static PredatorComponent createRandom(int size, SimulationParameters params) {
        PredatorComponent predators = new PredatorComponent(size);
        for (int i = 0; i < size; i++) {
            predators.posX[i] = random.nextDouble(0, params.width());
            predators.posY[i] = random.nextDouble(0, params.height());
            predators.velX[i] = random.nextDouble(-params.predatorMaxSpeed(), params.predatorMaxSpeed());
            predators.velY[i] = random.nextDouble(-params.predatorMaxSpeed(), params.predatorMaxSpeed());
            predators.wanderAngle[i] = random.nextDouble(0, Math.PI * 2);
        }
        return predators;
    }

    public PredatorComponent(int size) {
        this.size = size;
        posX = new double[size];
        posY = new double[size];
        velX = new double[size];
        velY = new double[size];
        accX = new double[size];
        accY = new double[size];
        wanderAngle = new double[size];
    }
}
