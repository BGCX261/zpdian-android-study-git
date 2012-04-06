package com.feixun.smart;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class Main extends Activity {

    private MovementView mMovementView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        Params.mWindowWidth = metrics.widthPixels;
        Params.mWindowHeight = metrics.heightPixels;
        Params.windowWidth = metrics.widthPixels;
        Params.windowHeight = metrics.heightPixels;
        mMovementView = new MovementView(this);
        setContentView(mMovementView);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            mMovementView.mIsTankStatus = !mMovementView.mIsTankStatus;
        }
        return super.onKeyUp(keyCode, event);
    }
}