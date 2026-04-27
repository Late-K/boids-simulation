package uk.ac.gold.boids;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class BoidTest {

    private static final SimulationParameters params = new SimulationParameters(
            800, 600, 10, 3.0, 0.5, 30.0, 50.0, 60.0, 0, 4.0, 0.6, 80.0, 120.0);

    @Test
    void test_createRandom_positionsWithinBounds() {
        BoidComponent b = BoidComponent.createRandom(20, params);
        for (int i = 0; i < b.size; i++) {
            assertTrue(b.posX[i] >= 0 && b.posX[i] <= params.width());
            assertTrue(b.posY[i] >= 0 && b.posY[i] <= params.height());
        }
    }
}
