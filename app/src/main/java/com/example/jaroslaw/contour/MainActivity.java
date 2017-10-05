package com.example.jaroslaw.contour;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final static int MAX_ITERATION = 10;
    private final static float MAX_ANGLE = 25.0f;
    private final static int WAIT_ITERATION_REFRESH = 10;

    private ImageView bubbleAreaXY, bubbleHorizontal, bubbleVertical, areaXY, scaleHorizontal, scaleVertical;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private boolean averageWork = false;
    private int counterAverageWork = 0, waitTimer = 0;

    private LinkedList<float[]> measurements = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setActionBar();
        initVariable();
        initSensors();
    }

    private void setActionBar(){
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
    }

    private void initSensors() {
        sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void initVariable() {
        bubbleAreaXY = (ImageView) findViewById(R.id.imageBubbleAreaXY);
        bubbleVertical = (ImageView) findViewById(R.id.imageBubbleVertical);
        bubbleHorizontal = (ImageView) findViewById(R.id.imageBubbleHorizontal);
        areaXY = (ImageView) findViewById(R.id.imageArea);
        scaleVertical = (ImageView) findViewById(R.id.imageScaleVertical);
        scaleHorizontal = (ImageView) findViewById(R.id.imageScaleHorizontal);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        //normal = 60ms
        //fastest = 1-30ms
        //game = 1-50ms
        //ui = 60ms
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] vectorsXYZ = sensorEvent.values;

            float[] averages = calculateAverageMeasures(vectorsXYZ);
            float x = averages[0];
            float y = averages[1];
            float z = averages[2];

            if (waitTimer % WAIT_ITERATION_REFRESH == 0) {
                setPlaneXY(x, y, rotationXYAngle(x, y), anglePlaneXYtoAxisZ(x, y, z));
                setScaleVertical(calculateAngleToAxisY(x, y));
                setScaleHorizontal(calculateAngleToAxisX(x, y));
                waitTimer = WAIT_ITERATION_REFRESH - 1;
            }else{
                waitTimer--;
            }
        }
    }

    private float[] calculateAverageMeasures(float[] measure) {
        float[] averages = new float[3];
        if (averageWork) {
            for (int i = 0; i < MAX_ITERATION; i++) {
                averages[0] += measurements.get(i)[0];
                averages[1] += measurements.get(i)[1];
                averages[2] += measurements.get(i)[2];
            }
            averages[0] /= MAX_ITERATION;
            averages[1] /= MAX_ITERATION;
            averages[2] /= MAX_ITERATION;
            measurements.addLast(measure);
            measurements.removeFirst();
        } else {
            measurements.addLast(measure);
            counterAverageWork++;
            if (counterAverageWork == MAX_ITERATION) {
                averageWork = true;
            }
        }
        return averages;
    }

    private double anglePlaneXYtoAxisZ(double x, double y, double z) {
        double angle = Math.toDegrees(Math.acos((x * x + y * y) / (Math.sqrt(x * x + y * y + z * z) * Math.sqrt(x * x + y * y))));
        return (isAngleAboveLimit(angle)) ? 90 - MAX_ANGLE : angle;
    }

    private double rotationXYAngle(double x, double y) {
        double angle;
        if (x != 0) {
            angle = Math.toDegrees(Math.atan(Math.abs(y) / Math.abs(x)));
        } else {
            angle = 90;
        }
        return angle;
    }

    private double calculateAngleToAxisY(double x, double y) {
        double angle;
        if (y != 0) {
            angle = Math.toDegrees(Math.atan(x / y));
            if (angle > 0 && angle < 90 - MAX_ANGLE) {
                angle = 90 - MAX_ANGLE;
            } else if (angle < 0 && angle > -90 + MAX_ANGLE) {
                angle = -90 + MAX_ANGLE;
            }
        } else {
            angle = 90;
        }

        if (x < 0) {
            angle = -angle;
        }
        return -angle;
    }

    private double calculateAngleToAxisX(double x, double y) {
        double angle;
        if (x != 0) {
            angle = Math.toDegrees(Math.atan(y / x));
            if (angle > 0 && angle < 90 - MAX_ANGLE) {
                angle = 90 - MAX_ANGLE;
            } else if (angle < 0 && angle > -90 + MAX_ANGLE) {
                angle = -90 + MAX_ANGLE;
            }
        } else {
            angle = 90;
        }

        if (y < 0) {
            angle = -angle;
        }
        return angle;
    }

    private void setScaleHorizontal(double angle) {
        float aParam = setHorizontalParameters(scaleHorizontal.getWidth(), scaleHorizontal.getHeight(), scaleHorizontal, bubbleHorizontal);
        int shift;
        if (angle >= 0) {
            shift = (int) (aParam * (90 - angle));
        } else {
            shift = (int) (aParam * (-90 - angle));
        }
        bubbleHorizontal.setPadding(centerBubble(scaleHorizontal.getWidth(), bubbleHorizontal) + shift, centerBubble(scaleHorizontal.getHeight(), bubbleHorizontal), 0, 0);

    }

    private void setScaleVertical(double angle) {
        float aParam = setVerticalParameters(scaleVertical.getWidth(), scaleVertical.getHeight(), scaleVertical, bubbleVertical);
        int shift;
        if (angle >= 0) {
            shift = (int) (aParam * (90 - angle));
        } else {
            shift = (int) (aParam * (-90 - angle));
        }
        bubbleVertical.setPadding(centerBubble(scaleVertical.getWidth(), bubbleVertical), centerBubble(scaleVertical.getHeight(), bubbleVertical) + shift, 0, 0);

    }

    private void setPlaneXY(float x, float y, double angle, double angleToAxisZ) {
        double rotationBubbleXY = 0;

        if (x >= 0 && y >= 0) rotationBubbleXY = (90 - angle) + 270;//quarter 1
        if (x >= 0 && y <= 0) rotationBubbleXY = angle;//quarter 4
        if (x <= 0 && y >= 0) rotationBubbleXY = angle + 180;//quarter 2
        if (x <= 0 && y <= 0) rotationBubbleXY = (90 - angle) + 90;//quarter 3

        int areaXYWidth = areaXY.getWidth();
        int areaXYHeight = areaXY.getHeight();
        float aParam = setHorizontalParameters(areaXYWidth, areaXYHeight, areaXY, bubbleAreaXY);

        bubbleAreaXY.setPadding(centerBubble(areaXYWidth, bubbleAreaXY) + (int) (aParam * (90 - angleToAxisZ)), centerBubble(areaXYHeight, bubbleAreaXY), 0, 0);
        bubbleAreaXY.setRotation((float) rotationBubbleXY);
    }

    private boolean isAngleAboveLimit(double angle) {
        return angle < (90 - MAX_ANGLE);
    }

    private float setHorizontalParameters(int width, int height, ImageView background, ImageView bubble) {
        bubble.setPivotX(width / 2);
        bubble.setPivotY(height / 2);
        return calculateSlopeParameter(getMaxShift(background.getWidth(), bubble.getDrawable().getIntrinsicWidth()));
    }

    private float setVerticalParameters(int width, int height, ImageView background, ImageView bubble) {
        bubble.setPivotX(width / 2);
        bubble.setPivotY(height / 2);
        return calculateSlopeParameter(getMaxShift(background.getHeight(), bubble.getDrawable().getIntrinsicWidth()));
    }

    private int getMaxShift(int areaWidth, int bubbleWidth) {
        return areaWidth / 2 - (2 * bubbleWidth / 3);
    }

    private float calculateSlopeParameter(int maxShift) {
        float minAngle = 0.0f;
        return ((float) maxShift) / (MAX_ANGLE - minAngle);
    }

    private int centerBubble(int lenght, ImageView bubble) {
        return lenght / 2 - bubble.getDrawable().getIntrinsicWidth() / 2;
    }
}
