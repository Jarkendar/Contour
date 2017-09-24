package com.example.jaroslaw.contour;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    String TAG = "*********";

    private TextView axisX, axisY, axisZ;
    private ImageView bubble, areaXY;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private float[] rotationVector;
    private float function;
    private float minAngle = 0.0f, maxAngle = 25.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        axisX = (TextView) findViewById(R.id.axis_X);
        axisY = (TextView) findViewById(R.id.axis_Y);
        axisZ = (TextView) findViewById(R.id.axis_Z);
        bubble = (ImageView) findViewById(R.id.imageBubble);
        areaXY = (ImageView) findViewById(R.id.imageArea);

        sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        for (Sensor x : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            Log.d(TAG, "onCreate: " + x.toString());
        }
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

    boolean count = false;
    long start;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            if (count) {
                Log.d(TAG, "onSensorChanged: time " + (System.currentTimeMillis() - start));
                start = System.currentTimeMillis();
            } else {
                count = true;
                start = System.currentTimeMillis();
            }
            rotationVector = sensorEvent.values;
            String xstring, ystring, zstring;

            if (rotationVector[0] > 0) {
                xstring = "left";
            } else {
                xstring = "right";
            }
            if (rotationVector[1] > 0) {
                ystring = "down";
            } else {
                ystring = "up";
            }
            if (rotationVector[2] > 0) {
                zstring = "forward";
            } else {
                zstring = "back";
            }
            axisX.setText(xstring + rotationVector[0]);
            axisY.setText(ystring + rotationVector[1]);
            axisZ.setText(zstring + rotationVector[2]);

            double x = (double) Math.abs(rotationVector[0]);
            double y = (double) Math.abs(rotationVector[1]);
            double z = (double) Math.abs(rotationVector[2]);
            double angleAlpha = Math.toDegrees(Math.acos((x * x + y * y) / (Math.sqrt(x * x + y * y + z * z) * Math.sqrt(x * x + y * y))));//field xy to axis z
            double angleBeta = Math.toDegrees(Math.acos((y * y + z * z) / (Math.sqrt(x * x + y * y + z * z) * Math.sqrt(y * y + z * z))));//field yz to axis x
            double angleGamma = Math.toDegrees(Math.acos((z * z + x * x) / (Math.sqrt(x * x + y * y + z * z) * Math.sqrt(z * z + x * x))));//field zx to axis y

            double angleDelta;//angle x to y
            if (x != 0) {
                angleDelta = Math.toDegrees(Math.atan(y / x));
            } else {
                angleDelta = 90;
            }

            if (angleAlpha < (90 - maxAngle)) angleAlpha = 90 - maxAngle;
            if (angleBeta < (90 - maxAngle)) angleBeta = 90 - maxAngle;
            if (angleGamma < (90 - maxAngle)) angleGamma = 90 - maxAngle;

            double angleRotation = 0;

            if (rotationVector[0] >= 0 && rotationVector[1] >= 0) angleRotation = (90 - angleDelta) + 270;
            if (rotationVector[0] >= 0 && rotationVector[1] <= 0) angleRotation = angleDelta;
            if (rotationVector[0] <= 0 && rotationVector[1] >= 0) angleRotation = angleDelta + 180;
            if (rotationVector[0] <= 0 && rotationVector[1] <= 0)
                angleRotation = (90 - angleDelta) + 90;

            int areaXYWidth = areaXY.getWidth();
            int areaXYHeight = areaXY.getHeight();
            setStartParameters(areaXYWidth, areaXYHeight);

            bubble.setPadding(centerBubbleXY(areaXYWidth) + (int) (function * (90 - angleAlpha)), centerBubbleXY(areaXYHeight), 0, 0);
            bubble.setRotation((float) angleRotation);
        }
    }



    private void setStartParameters(int areaXYWidth, int areaXYHeight) {
        function = calculateAParameter(getMaxTransition(areaXY.getWidth(), bubble.getDrawable().getIntrinsicWidth()));
        bubble.setPivotX(areaXYWidth / 2);
        bubble.setPivotY(areaXYHeight / 2);
    }

    private int getMaxTransition(int areaWidth, int bubbleWidth) {
        return areaWidth / 2 - (2 * bubbleWidth / 3);
    }

    private float calculateAParameter(int maxTransition) {
        return ((float) maxTransition) / (maxAngle - minAngle);
    }

    private int centerBubbleXY(int lenght) {
        return lenght / 2 - bubble.getDrawable().getIntrinsicWidth() / 2;
    }
}
