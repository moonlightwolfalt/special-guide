package io.mopesbox.Utils;

import io.mopesbox.Constants;

public class Waypoint {
    private double x;
    private double y;

    public Waypoint(double x, double y) {
        this.x = Math.max(0, Math.min(x, Constants.WIDTH));
        this.y = Math.max(0, Math.min(y, Constants.HEIGHT));
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getAngle(double x, double y) {
        double dy = this.y - y;
        double dx = this.x - x;
        double theta = Math.atan2(dy, dx);
        theta *= 180 / Math.PI;
        if (theta < 0) {
            theta += 360;
        }
        // theta -= 180;
        // if(theta < 0) theta += 360;
        return theta;
    }

    public double getDistance(double x, double y) {
        return Utils.distance(this.x, x, this.y, y);
    }
}
