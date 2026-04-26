package uk.ac.gold.boids;

public class ObstacleSystem {

    public Vector2 avoid(double x, double y, double maxForce, ObstacleComponent obstacles, double avoidRadius) {
        Vector2 steering = new Vector2(0, 0);

        for (int i = 0; i < obstacles.size; i++) {
            double closestX = clamp(x, obstacles.minX[i], obstacles.maxX[i]);
            double closestY = clamp(y, obstacles.minY[i], obstacles.maxY[i]);
            double dx = x - closestX;
            double dy = y - closestY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 1e-6 && distance < avoidRadius) {
                double strength = (1.0 - distance / avoidRadius) * maxForce;
                steering.x += (dx / distance) * strength;
                steering.y += (dy / distance) * strength;
            } else if (distance <= 1e-6
                    && x >= obstacles.minX[i] && x <= obstacles.maxX[i]
                    && y >= obstacles.minY[i] && y <= obstacles.maxY[i]) {
                Vector2 out = insidePushDirection(x, y, obstacles.minX[i], obstacles.minY[i], obstacles.maxX[i], obstacles.maxY[i]);
                steering.add(out.multiply(maxForce));
            }
        }

        return steering.limit(maxForce);
    }

    public void resolveCollision(double[] posX, double[] posY, double[] velX, double[] velY, int i,
            ObstacleComponent obstacles, double collisionRadius) {
        for (int j = 0; j < obstacles.size; j++) {
            double left = obstacles.minX[j] - collisionRadius;
            double right = obstacles.maxX[j] + collisionRadius;
            double top = obstacles.minY[j] - collisionRadius;
            double bottom = obstacles.maxY[j] + collisionRadius;

            double x = posX[i];
            double y = posY[i];

            if (x < left || x > right || y < top || y > bottom) {
                continue;
            }

            double penLeft = x - left;
            double penRight = right - x;
            double penTop = y - top;
            double penBottom = bottom - y;

            double minPen = Math.min(Math.min(penLeft, penRight), Math.min(penTop, penBottom));

            if (minPen == penLeft) {
                posX[i] = left;
                velX[i] = -Math.abs(velX[i]);
            } else if (minPen == penRight) {
                posX[i] = right;
                velX[i] = Math.abs(velX[i]);
            } else if (minPen == penTop) {
                posY[i] = top;
                velY[i] = -Math.abs(velY[i]);
            } else {
                posY[i] = bottom;
                velY[i] = Math.abs(velY[i]);
            }
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private Vector2 insidePushDirection(double x, double y, double minX, double minY, double maxX, double maxY) {
        double toLeft = Math.abs(x - minX);
        double toRight = Math.abs(maxX - x);
        double toTop = Math.abs(y - minY);
        double toBottom = Math.abs(maxY - y);

        double minDistance = Math.min(Math.min(toLeft, toRight), Math.min(toTop, toBottom));

        if (minDistance == toLeft) {
            return new Vector2(-1, 0);
        }
        if (minDistance == toRight) {
            return new Vector2(1, 0);
        }
        if (minDistance == toTop) {
            return new Vector2(0, -1);
        }
        return new Vector2(0, 1);
    }
}