package uk.ac.gold.boids;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class Vector2Test {

    @Test
    void test_magnitude() {
        assertEquals(5.0, new Vector2(3.0, 4.0).magnitude());
    }

}
