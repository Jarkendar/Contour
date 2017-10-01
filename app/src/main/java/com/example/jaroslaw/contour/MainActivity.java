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
            float xAvg = 0, yAvg = 0, zAvg = 0;
            if (averageWork) {
                for (int i = 0; i < MAXITERATION; i++) {
                    xAvg += measurements.get(i)[0];
                    yAvg += measurements.get(i)[1];
                    zAvg += measurements.get(i)[2];
                }
                xAvg /= MAXITERATION;
                yAvg /= MAXITERATION;
                zAvg /= MAXITERATION;
                measurements.addLast(vectorsXYZ);
                measurements.removeFirst();
            } else {
                measurements.addLast(vectorsXYZ);
                counterAverageWork++;
                if (counterAverageWork == MAXITERATION) {
                    averageWork = true;
                }
            }

            double x = xAvg;
            double y = yAvg;
            double z = zAvg;
            double angleXYtoZ = Math.toDegrees(Math.acos((x * x + y * y) / (Math.sqrt(x * x + y * y + z * z) * Math.sqrt(x * x + y * y))));//field xy to axis z
            double angleYZtoX = Math.toDegrees(Math.acos((y * y + z * z) / (Math.sqrt(x * x + y * y + z * z) * Math.sqrt(y * y + z * z))));//field yz to axis x
            double angleZXtoY = Math.toDegrees(Math.acos((z * z + x * x) / (Math.sqrt(x * x + y * y + z * z) * Math.sqrt(z * z + x * x))));//field zx to axis y

            double angleCtoX;//angle c to x, c is hypotenuse
            if (x != 0) {
                angleCtoX = Math.toDegrees(Math.atan(Math.abs(y) / Math.abs(x)));
            } else {
                angleCtoX = 90;
            }

            double angleDtoY;
            if (y!=0){
                angleDtoY = Math.toDegrees(Math.atan(x/y));
                Log.d(TAG, "onSensorChanged: DtoY" + angleDtoY);
                if (angleDtoY > 0 && angleDtoY < 90-maxAngle){
                    angleDtoY = 90-maxAngle;
                }else if (angleDtoY < 0 && angleDtoY > -90+maxAngle){
                    angleDtoY = -90+maxAngle;
                }
            }else {
                angleDtoY = 90;
            }

            if (x < 0) {
                angleDtoY = -angleDtoY;
            }

            if (angleIsAboveLimit(angleXYtoZ)) angleXYtoZ = 90 - maxAngle;
            if (angleIsAboveLimit(angleYZtoX)) angleYZtoX = 90 - maxAngle;
            if (angleIsAboveLimit(angleZXtoY)) angleZXtoY = 90 - maxAngle;

            doAreaXY(xAvg, yAvg, zAvg, angleCtoX, angleXYtoZ);
            doVertical(-angleDtoY);
        }
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
