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

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    String TAG = "*********";

    private TextView axisX, axisY, axisZ;
    private ImageView bubble, areaXY;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private float[] rotationVector;
    private double trans = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        axisX = (TextView) findViewById(R.id.axis_X);
        axisY = (TextView) findViewById(R.id.axis_Y);
        axisZ = (TextView) findViewById(R.id.axis_Z);
        bubble = (ImageView) findViewById(R.id.imageBubble);
        areaXY = (ImageView) findViewById(R.id.imageArea);

        sensorManager = (SensorManager)getApplicationContext().getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        for(Sensor x: sensorManager.getSensorList(Sensor.TYPE_ALL)){
            Log.d(TAG, "onCreate: "+x.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,rotationSensor,SensorManager.SENSOR_DELAY_NORMAL);
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
    public void onAccuracyChanged(Sensor sensor, int i) {   }

    boolean count = false;
    long start;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){

            if (count){
                Log.d(TAG, "onSensorChanged: time " + (System.currentTimeMillis()-start) );
                start = System.currentTimeMillis();
            }else {
                count = true;
                start = System.currentTimeMillis();
            }
            rotationVector = sensorEvent.values;
            String xstring, ystring, zstring;

            if(rotationVector[0]>0){
                xstring = "left";
            }else{
                xstring = "right";
            }
            if(rotationVector[1]>0){
                ystring = "down";
            }else{
                ystring = "up";
            }
            if(rotationVector[2]>0){
                zstring = "forward";
            }else{
                zstring = "back";
            }
            axisX.setText(xstring+rotationVector[0]);
            axisY.setText(ystring+rotationVector[1]);
            axisZ.setText(zstring+rotationVector[2]);

            bubble.setPadding(areaXY.getWidth()/2+(int)trans, areaXY.getHeight()/2,0,0);
            int[] padings = {bubble.getPaddingLeft(), bubble.getPaddingTop(), bubble.getPaddingRight(), bubble.getPaddingBottom()};
      //      trans += 0.5;

            double x = (double) Math.abs(rotationVector[0]);
            double y = (double) Math.abs(rotationVector[1]);
            double z = (double) Math.abs(rotationVector[2]);
            double angleAlpha = Math.acos((x*x+y*y)/(Math.sqrt(x*x+y*y+z*z)*Math.sqrt(x*x+y*y)));//field xy to axis z
            double angleBeta = Math.acos((y*y+z*z)/(Math.sqrt(x*x+y*y+z*z)*Math.sqrt(y*y+z*z)));//field yz to axis x
            double angleGamma = Math.acos((z*z+x*x)/(Math.sqrt(x*x+y*y+z*z)*Math.sqrt(z*z+x*x)));//field zx to axis y

            for (int w : padings) {
                Log.d(TAG, "Paddings = " + w);
            }
            Log.d(TAG, "area: "+ areaXY.getWidth()+"  "+ areaXY.getHeight());
            Log.d(TAG, "bubble: "+ bubble.getWidth()+"  "+ bubble.getHeight());
            Log.d(TAG, "Transition = "+ trans);
            Log.d(TAG, "onSensorChanged: angleAlpha "+ Math.toDegrees(angleAlpha));
            Log.d(TAG, "onSensorChanged: angleBeta "+ Math.toDegrees(angleBeta));
            Log.d(TAG, "onSensorChanged: angleGamma "+ Math.toDegrees(angleGamma));

        }

    }
}
