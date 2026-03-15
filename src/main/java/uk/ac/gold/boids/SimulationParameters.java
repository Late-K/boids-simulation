package uk.ac.gold.boids;

public record SimulationParameters(
        double width,
        double height,
        int numberOfBoids,
        double maxSpeed,
        double maxForce,
        double separationRadius,
        double alignRadius,
        double cohesionRadius) {
}
