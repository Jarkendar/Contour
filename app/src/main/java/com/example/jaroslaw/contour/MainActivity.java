package com.example.jaroslaw.contour;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    String TAG = "*********";

    private TextView axisX, axisY, axisZ;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private float[] rotationVector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        axisX = (TextView) findViewById(R.id.axis_X);
        axisY = (TextView) findViewById(R.id.axis_Y);
        axisZ = (TextView) findViewById(R.id.axis_Z);

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
                xstring = "w lewo";
            }else{
                xstring = "w prawo";
            }
            if(rotationVector[1]>0){
                ystring = "w dół";
            }else{
                ystring = "w górę";
            }
            if(rotationVector[2]>0){
                zstring = "wierch";
            }else{
                zstring = "tył";
            }
            axisX.setText(xstring);
            axisY.setText(ystring);
            axisZ.setText(zstring);
            double x = (double) Math.abs(rotationVector[0]);
            double y = (double) Math.abs(rotationVector[1]);
            double z = (double) Math.abs(rotationVector[2]);
            double angle = Math.acos((x*x+y*y)/(Math.sqrt(x*x+y*y+z*z)*Math.sqrt(x*x+y*y)));//field xy to axis z
            Log.d(TAG, "onSensorChanged: angle "+ Math.toDegrees(angle));
        }

    }
}
