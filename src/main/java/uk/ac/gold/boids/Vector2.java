package uk.ac.gold.boids;

public final class Vector2 {

    public double x;
    public double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2 add(Vector2 v) {
        x += v.x;
        y += v.y;
        return this;
    }

    public Vector2 subtract(Vector2 v) {
        x -= v.x;
        y -= v.y;
        return this;
    }

    public Vector2 multiply(double m) {
        x *= m;
        y *= m;
        return this;
    }

    public Vector2 divide(double d) {
        x /= d;
        y /= d;
        return this;
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2 normalize() {
        double m = magnitude();
        if (m > 0) {
            x /= m;
            y /= m;
        }
        return this;
    }

    public Vector2 limit(double max) {
        double m2 = x * x + y * y;
        if (m2 > max * max) {
            double m = Math.sqrt(m2);
            x = x / m * max;
            y = y / m * max;
        }
        return this;
    }

    public double distance(Vector2 other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distance(double ox, double oy) {
        double dx = x - ox;
        double dy = y - oy;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
