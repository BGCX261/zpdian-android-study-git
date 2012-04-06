package com.gangle;

import java.util.ArrayList;

public class RotateUtil {

    public static final float RADIANS_TO_DEGREES = 57.29578F;
    
    public static float TWO_PAI = 6.2831853F;
    
    public RotateUtil() {
        super();
    }
    
    public static float getDistanceFromButton (int button,Point point,ArrayList<Point> newPoints) {
        return (float)(Math.pow(point.x - newPoints.get(button).x, 2) + Math.pow(point.y - newPoints.get(button).y, 2));
    }
    
    public static Point computeNewPoint(float radius, Point point, Point center) {
        float x = (float) ((point.x - center.x) * Math.cos(radius) - (point.y - center.y) * Math.sin(radius) + center.x);
        float y = (float) ((point.x - center.x) * Math.sin(radius) + (point.y - center.y) * Math.cos(radius) + center.y);
        return new Point(x, y);
    }
    
    public static ArrayList<Point> computeNewPoints(float radius, ArrayList<Point> points, Point center) {
        ArrayList<Point> newPoints = new ArrayList<Point>();
        float x;
        float y;
        for (int i = 0; i < 16; i++) {
            x = (float) ((points.get(i).x - center.x) * Math.cos(radius) - (points.get(i).y - center.y) * Math.sin(radius) + center.x);
            y = (float) ((points.get(i).x - center.x) * Math.sin(radius) + (points.get(i).y - center.y) * Math.cos(radius) + center.y);
            newPoints.add(i,new Point(x,y));
        }
        return newPoints;
    }
    
    public static float computeNewOrientation(float y, float x) {
        float f = (float)(-Math.atan2(-y, x));
        if (f < 0.0F) {
            f += TWO_PAI;
        }
        return f;
    }

    public static float tiltAngle(float paramFloat1, float paramFloat2) {
        return (float) Math.asin(paramFloat1 / paramFloat2) * RADIANS_TO_DEGREES;
    }

    public static float vectorMagnitude(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }
}
