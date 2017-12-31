package com.junsung.wpi3;

public class SensorDataSet {
    double[] sensorData = new double[3];
    int dataLength = sensorData.length;

    SensorDataSet(double[] sensorData) {
        for(int i = 0; i < dataLength; i++)
            this.sensorData[i] = this.cutDouble(sensorData[i]);
    }

    private double cutDouble(double target) {
        String s = String.format("%.2f", target);
        return Double.parseDouble(s);
    }
}

