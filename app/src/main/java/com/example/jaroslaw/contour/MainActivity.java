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
    private float aParameter;
    private float maxAngle = 25.0f;

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
            String xstring, ystring, zstring;

            if (vectorsXYZ[0] > 0) {
                xstring = "left";
            } else {
                xstring = "right";
            }
            if (vectorsXYZ[1] > 0) {
                ystring = "down";
            } else {
                ystring = "up";
            }
            if (vectorsXYZ[2] > 0) {
                zstring = "forward";
            } else {
                zstring = "back";
            }
            axisX.setText(xstring + vectorsXYZ[0]);
            axisY.setText(ystring + vectorsXYZ[1]);
            axisZ.setText(zstring + vectorsXYZ[2]);

            double x = vectorsXYZ[0];
            double y = vectorsXYZ[1];
            double z = vectorsXYZ[2];
            double angleXYtoZ = Math.toDegrees(Math.acos((x * x + y * y) / (Math.sqrt(x * x + y * y + z * z) * Math.sqrt(x * x + y * y))));//field xy to axis z
            double angleYZtoX = Math.toDegrees(Math.acos((y * y + z * z) / (Math.sqrt(x * x + y * y + z * z) * Math.sqrt(y * y + z * z))));//field yz to axis x
            double angleZXtoY = Math.toDegrees(Math.acos((z * z + x * x) / (Math.sqrt(x * x + y * y + z * z) * Math.sqrt(z * z + x * x))));//field zx to axis y

            double angleCtoX;//angle c to x, c is hypotenuse
            if (x != 0) {
                angleCtoX = Math.toDegrees(Math.atan( Math.abs(y) / Math.abs(x)));
            } else {
                angleCtoX = 90;
            }

            if (angleIsAboveLimit(angleXYtoZ)) angleXYtoZ = 90 - maxAngle;
            if (angleIsAboveLimit(angleYZtoX)) angleYZtoX = 90 - maxAngle;
            if (angleIsAboveLimit(angleZXtoY)) angleZXtoY = 90 - maxAngle;

            double rotationOnXY = 0;

            if (vectorsXYZ[0] >= 0 && vectorsXYZ[1] >= 0) rotationOnXY = (90-angleCtoX) + 270;//quarter 1
            if (vectorsXYZ[0] >= 0 && vectorsXYZ[1] <= 0) rotationOnXY = angleCtoX;//quarter 4
            if (vectorsXYZ[0] <= 0 && vectorsXYZ[1] >= 0) rotationOnXY = angleCtoX + 180;//quarter 2
            if (vectorsXYZ[0] <= 0 && vectorsXYZ[1] <= 0) rotationOnXY = (90-angleCtoX) + 90;//quarter 3

            int areaXYWidth = areaXY.getWidth();
            int areaXYHeight = areaXY.getHeight();
            setStartParameters(areaXYWidth, areaXYHeight);

            bubble.setPadding(centerBubbleXY(areaXYWidth) + (int) (aParameter * (90 - angleXYtoZ)), centerBubbleXY(areaXYHeight), 0, 0);
            bubble.setRotation((float) rotationOnXY);

        }
    }

    private boolean angleIsAboveLimit(double angle) {
        return angle < (90 - maxAngle);
    }


    private void setStartParameters(int areaXYWidth, int areaXYHeight) {
        aParameter = calculateAParameter(getMaxTransition(areaXY.getWidth(), bubble.getDrawable().getIntrinsicWidth()));
        bubble.setPivotX(areaXYWidth / 2);
        bubble.setPivotY(areaXYHeight / 2);
    }

    private int getMaxTransition(int areaWidth, int bubbleWidth) {
        return areaWidth / 2 - (2 * bubbleWidth / 3);
    }

    private float calculateAParameter(int maxTransition) {
        float minAngle = 0.0f;
        return ((float) maxTransition) / (maxAngle - minAngle);
    }

    private int centerBubbleXY(int lenght) {
        return lenght / 2 - bubble.getDrawable().getIntrinsicWidth() / 2;
    }
}
