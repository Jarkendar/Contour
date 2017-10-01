package com.example.jaroslaw.contour;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    String TAG = "*********";

    private TextView axisX, axisY, axisZ;
    private ImageView bubbleAreaXY, bubbleHorizontal, bubbleVertical, areaXY, scaleHorizontal, scaleVertical;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private float areaParameter;
    private float maxAngle = 25.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bubbleAreaXY = (ImageView) findViewById(R.id.imageBubbleAreaXY);
        bubbleVertical = (ImageView) findViewById(R.id.imageBubbleVertical);
        bubbleHorizontal = (ImageView) findViewById(R.id.imageBubbleHorizontal);
        areaXY = (ImageView) findViewById(R.id.imageArea);
        scaleVertical = (ImageView) findViewById(R.id.imageScaleVertical);
        scaleHorizontal = (ImageView) findViewById(R.id.imageScaleHorizontal);

        sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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

    private boolean averageWork = false;
    private int counterAverageWork = 0;
    private final int MAXITERATION = 10;
    private LinkedList<float[]> measurements = new LinkedList<>();

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] vectorsXYZ = sensorEvent.values;

            float[] averages = calculateAverageMeasures(vectorsXYZ);
            float x = averages[0];
            float y = averages[1];
            float z = averages[2];

            doAreaXY(x, y, z, rotationAngle(x,y), anglePlaneXYtoAxisZ(x,y,z));
            doVertical(angleToAxisY(x,y));
            doHorizontal(angleToAxisX(x,y));
        }
    }

    private float[] calculateAverageMeasures(float[] measure){
        float[] averages = new float[3];
        if (averageWork) {
            for (int i = 0; i < MAXITERATION; i++) {
                averages[0] += measurements.get(i)[0];
                averages[1] += measurements.get(i)[1];
                averages[2] += measurements.get(i)[2];
            }
            averages[0] /= MAXITERATION;
            averages[1] /= MAXITERATION;
            averages[2] /= MAXITERATION;
            measurements.addLast(measure);
            measurements.removeFirst();
        } else {
            measurements.addLast(measure);
            counterAverageWork++;
            if (counterAverageWork == MAXITERATION) {
                averageWork = true;
            }
        }
        return averages;
    }

    private double anglePlaneXYtoAxisZ(double x, double y, double z){
        double angle = Math.toDegrees(Math.acos((x * x + y * y) / (Math.sqrt(x * x + y * y + z * z) * Math.sqrt(x * x + y * y))));
        return (angleIsAboveLimit(angle)) ? 90-maxAngle : angle;
    }

    private double rotationAngle(double x, double y){
        double angle;
        if (x != 0) {
            angle = Math.toDegrees(Math.atan(Math.abs(y) / Math.abs(x)));
        } else {
            angle = 90;
        }
        return angle;
    }

    private double angleToAxisY(double x, double y){
        double angle;
        if (y!=0){
            angle = Math.toDegrees(Math.atan(x/y));
            if (angle > 0 && angle < 90-maxAngle){
                angle = 90-maxAngle;
            }else if (angle < 0 && angle > -90+maxAngle){
                angle = -90+maxAngle;
            }
        }else {
            angle = 90;
        }

        if (x < 0) {
            angle = -angle;
        }
        return -angle;
    }

    private double angleToAxisX(double x, double y){
        double angle;
        if (x!=0){
            angle = Math.toDegrees(Math.atan(y/x));
            if (angle > 0 && angle < 90-maxAngle){
                angle = 90-maxAngle;
            }else if (angle < 0 && angle > -90+maxAngle){
                angle = -90+maxAngle;
            }
        }else {
            angle = 90;
        }

        if (y < 0) {
            angle = -angle;
        }
        return angle;
    }

    private void doHorizontal(double angle){
        float aParam = setStartParameters(scaleHorizontal.getWidth(), scaleHorizontal.getHeight(), scaleHorizontal, bubbleHorizontal);
        int shift = 0;
        if (angle >= 0){
            shift = (int) (aParam * (90 - angle));
        }else {
            shift =  (int) (aParam * (-90 - angle));
        }
        bubbleHorizontal.setPadding(centerBubble(scaleHorizontal.getWidth(), bubbleHorizontal) + shift, centerBubble(scaleHorizontal.getHeight(), bubbleHorizontal), 0, 0);

    }

    private void doVertical(double angle) {
        float aParam = setStartParametersVertical(scaleVertical.getWidth(), scaleVertical.getHeight(), scaleVertical, bubbleVertical);
        int shift = 0;
        if (angle >= 0){
            shift = (int) (aParam * (90 - angle));
        }else {
            shift =  (int) (aParam * (-90 - angle));
        }
        bubbleVertical.setPadding(centerBubble(scaleVertical.getWidth(), bubbleVertical), centerBubble(scaleVertical.getHeight(), bubbleVertical) + shift, 0, 0);

    }

    private void doAreaXY(float x, float y, float z, double angle, double angleArea) {
        double rotationOnXY = 0;

        if (x >= 0 && y >= 0) rotationOnXY = (90 - angle) + 270;//quarter 1
        if (x >= 0 && y <= 0) rotationOnXY = angle;//quarter 4
        if (x <= 0 && y >= 0) rotationOnXY = angle + 180;//quarter 2
        if (x <= 0 && y <= 0) rotationOnXY = (90 - angle) + 90;//quarter 3

        int areaXYWidth = areaXY.getWidth();
        int areaXYHeight = areaXY.getHeight();
        float aParam = setStartParameters(areaXYWidth, areaXYHeight, areaXY, bubbleAreaXY);

        bubbleAreaXY.setPadding(centerBubble(areaXYWidth, bubbleAreaXY) + (int) (aParam * (90 - angleArea)), centerBubble(areaXYHeight, bubbleAreaXY), 0, 0);
        bubbleAreaXY.setRotation((float) rotationOnXY);
    }

    private boolean angleIsAboveLimit(double angle) {
        return angle < (90 - maxAngle);
    }

    private float setStartParameters(int width, int height, ImageView background, ImageView bubble) {
        bubble.setPivotX(width / 2);
        bubble.setPivotY(height / 2);
        return calculateAParameter(getMaxTransition(background.getWidth(), bubble.getDrawable().getIntrinsicWidth()));
    }

    private float setStartParametersVertical(int width, int height, ImageView background, ImageView bubble) {
        bubble.setPivotX(width / 2);
        bubble.setPivotY(height / 2);
        return calculateAParameter(getMaxTransition(background.getHeight(), bubble.getDrawable().getIntrinsicWidth()));
    }

    private int getMaxTransition(int areaWidth, int bubbleWidth) {
        return areaWidth / 2 - (2 * bubbleWidth / 3);
    }

    private float calculateAParameter(int maxTransition) {
        float minAngle = 0.0f;
        return ((float) maxTransition) / (maxAngle - minAngle);
    }

    private int centerBubble(int lenght, ImageView bubble) {
        return lenght / 2 - bubble.getDrawable().getIntrinsicWidth() / 2;
    }
}
