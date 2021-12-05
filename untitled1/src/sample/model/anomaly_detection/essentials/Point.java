package sample.model.anomaly_detection.essentials;

public class Point {
    public final float x,y;
    public Point(float x, float y) {
        this.x=x;
        this.y=y;
    }

    public float distanceSquaredTo(final Point p) {
        final float DX = x - p.x;
        final float DY = y - p.y;

        return DX * DX + DY * DY;
    }

    public float distanceTo(final Point p) {
        return (float) Math.sqrt(distanceSquaredTo(p));
    }

    public static boolean areColinear(final Point p1, final Point p2, final Point p3) {
        return Math.abs(p1.x * (p2.y - p3.y) + p2.x * (p3.y - p1.y) + p3.x * (p1.y - p2.y)) == 0.0;
    }

    @Override
    public String toString() {
        return "X: " + x + ", Y: " + y;
    }
}
