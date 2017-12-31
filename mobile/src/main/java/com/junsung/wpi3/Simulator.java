package com.junsung.wpi3;

import java.util.Random;

/**
 * Created by junsung on 2017. 12. 4..
 */

public class Simulator {
    double headingDirection;
    int speedCalibrationPoint;
    double speedConstant;
    double x, y;
    Step[] steps = new Step[12];
    Path path;
    Random rand;

    public Simulator(double headingDirection, double speedConstant, double x, double y) {
        rand = new Random();
        this.headingDirection = headingDirection;
        this.speedConstant = speedConstant;
        this.x = x;
        this.y = y;

        double[] lengths = {5 * Math.sqrt(2), 5, 5, 2.5, 5, 5, 2.5, 2.5, 2.5, 5, 5, 5};
        double[] directions = {-45, -45, 0, 90, 90, 0, 90, 0, 0, 90, 0, 0};

        for(int i = 0 ; i < steps.length; i++) {
            steps[i] = new Step(lengths[i], directions[i]);
        }

        speedCalibrationPoint = rand.nextInt(10) + 30;
    }

    public void initPath() {
        path = new Path(steps, speedConstant);
    }

    public void initPath(double speedConstant) {
        path = new Path(steps, speedConstant);
    }

    public double[] getPosition() {
        double deltaX, deltaY;
        double[] position = new double[2];
        final int ERROR_RATE_PERCENTAGE = 50;

        // if there is error
        if(rand.nextInt(100) > ERROR_RATE_PERCENTAGE)
            headingDirection += (rand.nextGaussian());

        deltaX = Math.sin(Math.toRadians(headingDirection));
        deltaY = Math.cos(Math.toRadians(headingDirection));

        position[0] = deltaX;
        position[1] = deltaY;

        return position;
    }


}

class Step {
    double length;
    double directionChange;

    public Step(double length, double directionChange) {
        this.length = length;
        this.directionChange = directionChange;
    }


}

class Path {
    int lifetime;
    double[] direction;
    boolean[] points;

    public Path(Step[] steps, double speedConstant) {
        lifetime = 0;
        for (int i = 0; i < steps.length; i++)
            lifetime += (int) (steps[i].length / speedConstant);

        direction = new double[lifetime];
        points = new boolean[lifetime];
        for(int i = 0 ; i < direction.length; i++) {
            direction[i] = 0;
            points[i] = false;
        }


        direction[0] = steps[0].directionChange;
        //points[0] = true;
        int offset = -1;
        for (int i = 0 ; i < steps.length - 1; i++) {
            offset += (int) (steps[i].length / speedConstant);
            direction[offset] = steps[i+1].directionChange;
            points[offset] = true;
        }
    }
}