package com.gangle;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class GsensorAngleActivity extends Activity {
    private static final int DATA_X = 0;
    private static final int DATA_Y = 1;
    private static final int DATA_Z = 2;
    public static final int INIT_X = 512;
    public static final int INIT_Y = 325;
    public static float sCenterX;
    public static float sCenterY;
    public static Point sCenter = new Point();
    public static int mScreenHeight;
    public static int mScreenWidth;
    public static float mNewOrientationAngleAcc;
    public static float mNewOrientationAngleGyro;
    private RotateLayout mLinearLayout;
    public SensorEventListener mListtenr;
    private SensorManager mSensorManager;
    private TextView mText;
    private float mNewRadiusAcc;
    private float mNewRadiusGyro;
    private Handler mHandler = new Handler();
    private ToneGenerator mTonePlayer = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);  
    private long mLastTime;
    private float mTimestamp;
    private static final float NS2S = 1.0f/1000000000.0f; 
    private float[] mAngle = new float[3];
    private boolean mUseAcc = false;
    
    private Sensor mSensorGyro;
    private Sensor mSensorAcc;
    private View mRootView;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  
        setContentView(R.layout.rotate);
        mLinearLayout = (RotateLayout) findViewById(R.id.subwindow_container);
        mSensorManager = (SensorManager) getSystemService("sensor");
        mSensorGyro = mSensorManager
        .getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorAcc = mSensorManager
        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Runnable runnable = new Runnable () {

            @Override
            public void run() {
                init();
            }
            
        };
        mHandler.postDelayed(runnable, 300);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("myTag","onKeyUp");
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            Log.i("myTag","KEYCODE_MENU");
            mUseAcc = !mUseAcc;
        }
        return super.onKeyUp(keyCode, event);
    }
    
    private void init() {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        sCenter.x = mScreenWidth/2;
        sCenter.y = mScreenHeight/2;
        mText = (TextView) findViewById(R.id.text);
        mRootView = this.findViewById(R.id.subwindow_container);
        
        mListtenr = new SensorEventListener() {
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            public void onSensorChanged(SensorEvent sensorEvent) {
                if (!mUseAcc && sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    float i = sensorEvent.values[DATA_X];
                    float j = sensorEvent.values[DATA_Y];
                    float k = sensorEvent.values[DATA_Z];
                    float f = RotateUtil.vectorMagnitude(i, j, k);
                    if (Math.abs(RotateUtil.tiltAngle(k, f)) <= 80.0F) { // z, xoy < 60
                        mNewRadiusAcc = RotateUtil.computeNewOrientation(i, j); //for pad
                        mNewOrientationAngleAcc = mNewRadiusAcc * RotateUtil.RADIANS_TO_DEGREES;
//                        mLinearLayout.invalidate();
                        return;
                    }
                    mNewOrientationAngleAcc = 0.0f;
                } else if (!mUseAcc && sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    if (mTimestamp != 0) {
                        final float dT = (sensorEvent.timestamp - mTimestamp)
                                * NS2S;
//                        mAngle[0] += sensorEvent.values[0] * dT;
//                        mAngle[1] += sensorEvent.values[1] * dT;
                        mAngle[2] += sensorEvent.values[2] * dT;
                    }

                    mTimestamp = sensorEvent.timestamp;
                    mNewRadiusGyro = mAngle[2];
                    mNewOrientationAngleGyro = mAngle[2]
                            * RotateUtil.RADIANS_TO_DEGREES;
                    if (Math.abs(mNewOrientationAngleGyro - mNewOrientationAngleAcc) > 1) {
                        mAngle[2] = mNewOrientationAngleAcc;
                        mNewRadiusGyro = mNewRadiusAcc;
                        mNewOrientationAngleGyro = mNewOrientationAngleAcc;
                    }
                    mLinearLayout.invalidate();
                }
            }
        };
        mSensorManager.registerListener(mListtenr, mSensorGyro,
                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(mListtenr, mSensorAcc,
                SensorManager.SENSOR_DELAY_FASTEST);
    } 

    private View mFoundView;
    private int [] mLocation = new int[2];
    
    private View findView(View vg, Point point) {
        Log.d("myTag","enter");
        int count = 0;
        if (!(vg instanceof ViewGroup)) {
            Log.e("myTag","!(vg instanceof ViewGroup)");
            count = 0;
        } else {
            count = ((ViewGroup)vg).getChildCount();
        }
        View child = null;
        int width;
        int height;
        int i = 0;
        for (i = 0; i < count; i++) {
            child = ((ViewGroup)vg).getChildAt(i);
            width = child.getWidth();
            height = child.getHeight();
            child.getLocationOnScreen(mLocation);
            
            if (!(child instanceof ViewGroup)) { // view 
                if (point.x > mLocation[0] && point.x < mLocation[0] + width && point.y > mLocation[1] && point.y < mLocation[1] + height) {
                    if (child.getId() != R.id.bg) {
                        Log.i("myTag","return v id = " + child.getId());
                        mFoundView = child;
                        break;
                    }
                } else {
                    Log.d("myTag","11111");
                    continue;
                }
            } else {// viewgroup
                if (point.x > mLocation[0] && point.x < mLocation[0] + width && point.y > mLocation[1] && point.y < mLocation[1] + height) {
                    Log.d("myTag","22222");
                    findView(child, point);
                } else {
                    Log.d("myTag","3333");
                    continue;
                }
            }
        }
        Log.d("myTag","4444");
        return mFoundView;
    }
    
    protected void onResume() {
        super.onResume();
        Log.d("myTag", "onResume");
    }

    protected void onStop() {
        mSensorManager.unregisterListener(mListtenr);
        super.onStop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (SystemClock.uptimeMillis() - mLastTime < 300) {
            return super.onTouchEvent(event);
        }
        Point originPoint = RotateUtil.computeNewPoint(-mNewRadiusGyro, new Point(event.getRawX(), event.getRawY()), sCenter);
        
        View v;
        int id = -1;
        mFoundView = mRootView;
        v = findView(this.findViewById(R.id.subwindow_container), originPoint);
        id = v.getId();
        
        Log.d("myTag","id for test2 ============== " + id);
        switch (id) {
        case R.id.button_one:
            playToneDTMF();
            mText.setText(mText.getText() + String.valueOf(1));
            break;
        case R.id.button_two:
            playToneDTMF();
            mText.setText(mText.getText() + String.valueOf(2));
            break;
        case R.id.button_three:
            playToneDTMF();
            mText.setText(mText.getText() + String.valueOf(3));
            break;
        case R.id.button_four:
            playToneDTMF();
            mText.setText(mText.getText() + String.valueOf(4));
            break;
        case R.id.button_five:
            playToneDTMF();
            mText.setText(mText.getText() + String.valueOf(5));
            break;
        case R.id.button_six:
            playToneDTMF();
            mText.setText(mText.getText() + String.valueOf(6));
            break;
        case R.id.button_seven:
            playToneDTMF();
            mText.setText(mText.getText() + String.valueOf(7));
            break;
        case R.id.button_eight:
            playToneDTMF();
            mText.setText(mText.getText() + String.valueOf(8));
            break;
        case R.id.button_nine:
            playToneDTMF();
            mText.setText(mText.getText() + String.valueOf(9));
            break;
        case R.id.button_zero:
            playToneDTMF();
            mText.setText(mText.getText() + String.valueOf(0));
            break;
        case R.id.button_xin:
            playToneDTMF();
            mText.setText(mText.getText() + String.valueOf("*"));
            break;
        case R.id.button_jin:
            playToneDTMF();
            mText.setText(mText.getText() + String.valueOf("#"));
            break;
        case R.id.button_delete:
            int end = mText.getText().length() -1;
            if(end == -1) {
                end = 0;
            }
            mText.setText(mText.getText().subSequence(0, end));
            break;
        default:
            break;    
        }
        
        mLastTime = SystemClock.uptimeMillis();
        return super.onTouchEvent(event);
    }
    
    private void playToneDTMF() {
        mLastTime = SystemClock.uptimeMillis();
        mTonePlayer.startTone(ToneGenerator.TONE_DTMF_0, 100);
    }

}
