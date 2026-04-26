package uk.ac.gold.boids;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class FlockSystemTest {

    private static final SimulationParameters params = new SimulationParameters(
            800, 600, 5, 3.0, 0.5, 30.0, 50.0, 60.0, 0, 4.0, 0.6, 80.0, 120.0);

    // Tiny align/cohesion radii so only separation fires
    private static final SimulationParameters separationParams = new SimulationParameters(
            800, 600, 2, 3.0, 0.5, 30.0, 1.0, 1.0, 0, 4.0, 0.6, 80.0, 120.0);

    @Test
    void test_updatePositions_movesBoidsWithVelocity() {
        FlockSystem fs = new FlockSystem();
        Boid b = new Boid(1);
        b.posX[0] = 400; b.posY[0] = 300;
        b.velX[0] = 1.0;

        fs.updatePositions(b, new ObstacleComponent(0), params, 1.0);

        assertEquals(401.0, b.posX[0]);
    }

    @Test
    void test_computeForces_separationPushesBoidsApart() {
        FlockSystem fs = new FlockSystem();
        Boid pair = new Boid(2);
        pair.posX[0] = 400; pair.posY[0] = 300;
        pair.posX[1] = 405; pair.posY[1] = 300;

        fs.computeForces(pair, new PredatorComponent(0), new ObstacleComponent(0), separationParams);

        assertTrue(pair.accX[0] < 0);
    }
}

