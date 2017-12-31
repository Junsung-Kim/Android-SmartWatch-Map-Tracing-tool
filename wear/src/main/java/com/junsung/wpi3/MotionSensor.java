package com.junsung.wpi3;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class MotionSensor implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private String mOption;

    private float[] mResultValues = new float[3];

    private SensorEventListener mAccelListener;
    private float mAccelValue = 0f;

    // Kalman filter
    private float[] mAxis = new float[3];
    private KalmanFilter[] mKalmanFilter = new KalmanFilter[3];

    MotionSensor(SensorManager sensorManager, int sensorType, String option) {
        mSensorManager = sensorManager;
        mSensor = sensorManager.getDefaultSensor(sensorType);
        mOption = option;

        // for step detecting
        for(int i = 0 ; i < 3; i++)
            mKalmanFilter[i] = new KalmanFilter(0.0f);
        mAccelListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float accelValueResult = 1f;
                for(int i = 0 ; i < 3; i++) {
                    float[] accelValues = new float[3];
                    accelValues[i] = event.values[i];
                    // disable the comment to use kalman filter
                    // accelValues[i] = (float)mKalmanFilter[i].update(accelValues[i]);
                    accelValueResult += (accelValues[i] * accelValues[i]);
                }
                mAccelValue = accelValueResult;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        mSensorManager.registerListener(mAccelListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_UI);
    }


    /**
     * call in onResume, MainActivity of wear
     */
    void register() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
    }

    void register(int sensorType, String option) {
        mSensor = mSensorManager.getDefaultSensor(sensorType);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        mOption = option;
    }

    /**
     * call in onPause, MainActivity of wear
     */
    void unregister() {
        mSensorManager.unregisterListener(this);
        mSensorManager.unregisterListener(mAccelListener);
    }

    void unregister(int sensorType) {
        mSensor = mSensorManager.getDefaultSensor(sensorType);
        mSensorManager.unregisterListener(this, mSensor);
    }

    public float[] getResultValues() {
        return mResultValues;
    }

    float[] getResult() {
        float[] tmp = new float[4];

        System.arraycopy(mResultValues, 0, tmp, 0, 3);

        tmp[3] = mAccelValue;

        return tmp;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                float[] rotationMatrix = new float[16];
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                mResultValues = determineOrientation(rotationMatrix);
                break;
            default:
                Log.d("onSensorChanged", event.sensor.getType() + " is not available");
        }

        switch (mOption) {
            case "rad2deg":
                for(int i = 0 ; i < 3; i++)
                    mResultValues[i] = (int)Math.toDegrees(mResultValues[i]);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private float[] determineOrientation(float[] rotationMatrix) {
        float[] orientationValues = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientationValues);

        return orientationValues;
    }
}
