package uk.ac.gold.boids;

public record Vector2(double x, double y) {

    public Vector2 add(Vector2 v) {
        return new Vector2(x + v.x, y + v.y);
    }

    public Vector2 subtract(Vector2 v) {
        return new Vector2(x - v.x, y - v.y);
    }

    public Vector2 multiply(double m) {
        return new Vector2(x * m, y * m);
    }

    public Vector2 divide(double d) {
        return new Vector2(x / d, y / d);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2 normalize() {
        double m = magnitude();
        return m > 0 ? new Vector2(x / m, y / m) : this;
    }

    public Vector2 limit(double max) {
        return magnitude() > max ? normalize().multiply(max) : this;
    }

    public double distance(Vector2 other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
