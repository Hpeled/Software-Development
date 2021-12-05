package sample.model.anomaly_detection.essentials;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Circle {
    public Point center;
    public float radius;

    public Circle(final Point center, float radius) {
        this.center = center;
        this.radius = radius;
    }

    public Circle(float x, float y, float radius) {
        center = new Point(x, y);
        this.radius = radius;
    }

    public Circle(final Point p1, final Point p2) {
        center = new Point((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f);
        radius = center.distanceTo(p1);
    }

    public Circle(final Point p1, final Point p2, final Point p3) {
        final float P2_MINUS_P1_Y = p2.y - p1.y;
        final float P3_MINUS_P2_Y =  p3.y - p2.y;

        if (P2_MINUS_P1_Y == 0.0 || P3_MINUS_P2_Y == 0.0) {
            center = new Point(0.0f, 0.0f);
            radius = 0.0f;
        }
        else {
            final float A = -(p2.x - p1.x) / P2_MINUS_P1_Y;
            final float A_PRIME = -(p3.x - p2.x) / P3_MINUS_P2_Y;
            final float A_PRIME_MINUS_A = A_PRIME - A;

            if (A_PRIME_MINUS_A == 0.0f) {
                center = new Point(0.0f, 0.0f);
                radius = 0.0f;
            }
            else {
                final float P2_SQUARED_X = p2.x * p2.x;
                final float P2_SQUARED_Y = p2.y * p2.y;


                final float B = (float) ((float) (P2_SQUARED_X - p1.x * p1.x + P2_SQUARED_Y - p1.y * p1.y) /
                        (2.0 * P2_MINUS_P1_Y));
                final float B_PRIME = (float) ((p3.x * p3.x - P2_SQUARED_X + p3.y * p3.y - P2_SQUARED_Y) /
                        (2.0 * P3_MINUS_P2_Y));


                final float XC = (B - B_PRIME) / A_PRIME_MINUS_A;
                final float YC = A * XC + B;

                final float DXC = p1.x - XC;
                final float DYC = p1.y - YC;

                center = new Point(XC, YC);
                radius = (float) Math.sqrt(DXC * DXC + DYC * DYC);
            }
        }
    }

    public boolean containsAllPoints(final List<Point> points2d) {
        for (final Point p : points2d) {
            if (p != center && !containsPoint(p)) {
                return false;
            }
        }

        return true;
    }

    public static Circle FindMinCircle(final List<Point> points) {
        return WelezAlgo(points, new ArrayList<Point>());
    }

    private static Circle WelezAlgo(final List<Point> points, final List<Point> R) {
        Circle minimumCircle = null;

        if (R.size() == 3) {
            minimumCircle = new Circle(R.get(0), R.get(1), R.get(2));
        }
        else if (points.isEmpty() && R.size() == 2) {
            minimumCircle = new Circle(R.get(0), R.get(1));
        }
        else if (points.size() == 1 && R.isEmpty()) {
            minimumCircle = new Circle(points.get(0).x, points.get(0).y, 0.0F);
        }
        else if (points.size() == 1 && R.size() == 1) {
            minimumCircle = new Circle(points.get(0), R.get(0));
        }
        else {
            Random rand = new Random();
            Point p = points.remove(rand.nextInt(points.size()));
            minimumCircle = WelezAlgo(points, R);

            if (minimumCircle != null && !minimumCircle.containsPoint(p)) {
                R.add(p);
                minimumCircle = WelezAlgo(points, R);
                R.remove(p);
                points.add(p);
            }
        }
        return minimumCircle;
    }

    public boolean containsPoint(final Point p) {
        return p.distanceSquaredTo(center) <= radius * radius;
    }

    @Override
    public String toString() {
        return center.toString()  +  ", Radius: " + radius;
    }
}