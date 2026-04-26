package uk.ac.gold.boids;

// obstacles 

public class ObstacleComponent {

    public final int capacity;
    public int size;

    public final double[] minX;
    public final double[] minY;
    public final double[] maxX;
    public final double[] maxY;

    public ObstacleComponent(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        minX = new double[capacity];
        minY = new double[capacity];
        maxX = new double[capacity];
        maxY = new double[capacity];
    }

    public boolean addFromCorners(double startX, double startY, double endX, double endY, double minSpan) {
        if (size >= capacity) {
            return false;
        }

        double[] bounds = toBoundsFromCorners(startX, startY, endX, endY);
        double width = bounds[2] - bounds[0];
        double height = bounds[3] - bounds[1];
        if (width < minSpan || height < minSpan) {
            return false;
        }

        int i = size;
        minX[i] = bounds[0];
        minY[i] = bounds[1];
        maxX[i] = bounds[2];
        maxY[i] = bounds[3];
        size++;
        return true;
    }

    public static double[] toBoundsFromCorners(double startX, double startY, double endX, double endY) {
        return new double[] {
                Math.min(startX, endX),
                Math.min(startY, endY),
                Math.max(startX, endX),
                Math.max(startY, endY)
        };
    }
}