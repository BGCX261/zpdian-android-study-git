package com.gangle;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class RotateLayout extends RelativeLayout {

    
    public RotateLayout(Context context) {
        super(context);
    }

    public RotateLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    protected void onDraw(Canvas canvas) {
        float degrees = GsensorAngleActivity.mNewOrientationAngleGyro;
        float px = GsensorAngleActivity.sCenter.x;
        float py = GsensorAngleActivity.sCenter.y;
        canvas.rotate(degrees, px, py);
        super.onDraw(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return false;
    }

}