package com.feixun.smart;

import android.graphics.Canvas;

import android.view.SurfaceHolder;

public class UpdateThread extends Thread {

    private long mTime;

    private final int mFps = 60;

    private boolean mToRun = false;

    private MovementView mMovementView;

    private SurfaceHolder mSurfaceHolder;

    public UpdateThread(MovementView rMovementView) {

        mMovementView = rMovementView;

        mSurfaceHolder = mMovementView.getHolder();

    }

    public void setRunning(boolean run) {

        mToRun = run;

    }

    public void run() {

        Canvas c;
        while (mToRun) {
            if (!mMovementView.mIsTankStatus) {
                c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    mMovementView.update(c);
                } finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                        continue;
                    }
                }
            }

            long cTime = System.currentTimeMillis();

            if ((cTime - mTime) <= (1000 / mFps)) {

                c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    mMovementView.update(c);
                } finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
            mTime = cTime;
        }
    }
}
