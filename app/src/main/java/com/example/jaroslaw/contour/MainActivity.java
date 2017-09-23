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
    private float trans = 0;

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

            double x = (double) Math.abs(rotationVector[0]);
            double y = (double) Math.abs(rotationVector[1]);
            double z = (double) Math.abs(rotationVector[2]);
            double angleAlpha = Math.acos((x*x+y*y)/(Math.sqrt(x*x+y*y+z*z)*Math.sqrt(x*x+y*y)));//field xy to axis z
            double angleBeta = Math.acos((y*y+z*z)/(Math.sqrt(x*x+y*y+z*z)*Math.sqrt(y*y+z*z)));//field yz to axis x
            double angleGamma = Math.acos((z*z+x*x)/(Math.sqrt(x*x+y*y+z*z)*Math.sqrt(z*z+x*x)));//field zx to axis y

            int areaXYWidth = areaXY.getWidth();
            int areaXYHeight = areaXY.getHeight();
            bubble.setPivotX(areaXYWidth/2);
            bubble.setPivotY(areaXYHeight/2);
            bubble.setPadding(centerBubbleXY(areaXYWidth), centerBubbleXY(areaXYHeight),0,0);
            bubble.setRotation(trans);
            int[] padings = {bubble.getPaddingLeft(), bubble.getPaddingTop(), bubble.getPaddingRight(), bubble.getPaddingBottom()};
            trans++;
            //max left 188
            //max top
            //max right
            //max bottom

            for (int w : padings) {
                Log.d(TAG, "Paddings = " + w);
            }

            Log.d(TAG, "area: "+ areaXY.getWidth()+"  "+ areaXY.getHeight());
            Log.d(TAG, "bubble: "+bubble.getDrawable().getIntrinsicWidth()+"  "+bubble.getDrawable().getIntrinsicHeight());
            Log.d(TAG, "bubble: "+bubble.getWidth()+"  "+bubble.getHeight());
            Log.d(TAG, "Transition = "+ trans);
            Log.d(TAG, "Rotation = "+ bubble.getRotation());
            Log.d(TAG, "Pivot Point =" +bubble.getPivotX() + " "+ bubble.getPivotY());
            Log.d(TAG, "onSensorChanged: angleAlpha "+ Math.toDegrees(angleAlpha));
            Log.d(TAG, "onSensorChanged: angleBeta "+ Math.toDegrees(angleBeta));
            Log.d(TAG, "onSensorChanged: angleGamma "+ Math.toDegrees(angleGamma));

        }
    }

    private int getMaxTransition(int areaWidth, int bubbleWidth){
        return areaWidth/2-2*bubbleWidth/3;
    }

    private float makeLinearFunction(int maxTransition){
        float minAngle = 0.0f, maxAngle = 15.0f;
        return calculateAParameter(maxTransition, minAngle,maxAngle); //b always = 0,
    }

    private float calculateAParameter(int maxTransition, float minAngle, float maxAngle){
        return maxTransition/(maxAngle-minAngle);
    }

    private int centerBubbleXY(int lenght){
        return lenght/2-bubble.getDrawable().getIntrinsicWidth()/2;
    }
}
