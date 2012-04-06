package com.feixun.smart;

//import java.util.Vector;

import java.util.ArrayList;

import com.feixun.smart.GenAlg.Genome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MovementView extends SurfaceView implements SurfaceHolder.Callback {

    private Paint mPaint = new Paint();

    private UpdateThread mUpdateThread;

    private ArrayList<Genome> mThePopulation = new ArrayList<Genome>();

    private ArrayList<Minesweeper> mSweepers = new ArrayList<Minesweeper>();

    private ArrayList<Float> mLastRotation = new ArrayList<Float>();

    // vector of mines
    private ArrayList<Vector2D> mMines = new ArrayList<Vector2D>();

    // pointer to the GA
    private GenAlg mGA;

    private int mNumSweepers;

    private int mNumMines;

    // number of weights required for the neural net
    private int mNumWeightsInNN;

    // vertex buffer for the sweeper shape's vertices
//    private ArrayList<Point> mSweeperVB = new ArrayList<Point>();

    // vertex buffer for the mine shape's vertices
//    private ArrayList<Point> mMineVB = new ArrayList<Point>();

    // stores the average fitness per generation for use
    // in graphing.
    private ArrayList<Double> mAvFitness = new ArrayList<Double>();

    // stores the best fitness per generation
    private ArrayList<Double> mBestFitness = new ArrayList<Double>();

    // toggles the speed at which the simulation runs
    // private boolean mFastRender;

    // cycles per generation
    private int mTicks;

    // generation counter
    private int mGenerations;

    // window dimensions
    private int mXClient;
    private int mYClient;

//    private static final int NUM_SWEEPER_VERTS = 16;
//    private Point[] mSweeper = { new Point(-1, -1), new Point(-1, 1),
//            new Point(-0.5f, 1), new Point(-0.5f, -1),
//
//            new Point(0.5f, -1), new Point(1, -1), new Point(1, 1),
//            new Point(0.5f, 1),
//
//            new Point(-0.5f, -0.5f), new Point(0.5f, -0.5f),
//
//            new Point(-0.5f, 0.5f), new Point(-0.25f, 0.5f),
//            new Point(-0.25f, 1.75f), new Point(0.25f, 1.75f),
//            new Point(0.25f, 0.5f), new Point(0.5f, 0.5f) };
//
//    private static final int NUM_MIND_VERTS = 4;
//    private Point[] mMine = { new Point(-1, -1), new Point(-1, 1),
//            new Point(1, 1), new Point(1, -1) };;

    private Bitmap mTank;
    private RectF mRect;
    public boolean mIsTankStatus = true;

    private void init() {
        mNumSweepers = Params.mNumSweepers;
        mGA = null;
        // mFastRender = false;
        mTicks = 0;
        mNumMines = Params.mNumMines;
        // m_hwndMain = hwndMain;

        mGenerations = 0;
        mXClient = Params.mWindowWidth;
        mYClient = Params.mWindowHeight;

        for (int i = 0; i < mNumSweepers; ++i) {
            mSweepers.add(new Minesweeper());
        }

        // get the total number of weights used in the sweepers
        // NN so we can initialise the GA
        mNumWeightsInNN = mSweepers.get(0).getNumberOfWeights();

        // calculate the neuron placement in the weight vector
        ArrayList<Integer> splitPoints = mSweepers.get(0)
                .calculateSplitPoints();

        // initialize the Genetic Algorithm class
        mGA = new GenAlg(mNumSweepers, Params.mMutationRate,
                Params.mCrossoverRate, mNumWeightsInNN, splitPoints);

        // Get the weights from the GA and insert into the sweepers brains
        mThePopulation = mGA.getChromos();

        for (int i = 0; i < mNumSweepers; i++) {
            mSweepers.get(i).putWeights(mThePopulation.get(i).mWeights);
            mLastRotation.add(0f);
        }

        // initialize mines in random positions within the application window
        for (int i = 0; i < mNumMines; ++i) {
            mMines.add(new Vector2D(Utils.randFloat() * mXClient, Utils
                    .randFloat() * mYClient));
        }

        // fill the vertex buffers
//        for (int i = 0; i < NUM_SWEEPER_VERTS; ++i) {
//            mSweeperVB.add(mSweeper[i]);
//        }
//
//        for (int i = 0; i < NUM_MIND_VERTS; ++i) {
//            mMineVB.add(mMine[i]);
//        }
        mAvFitness.add(mGA.averageFitness());
        mBestFitness.add(mGA.bestFitness());

        mTank = BitmapFactory.decodeResource(getResources(), R.drawable.tank);
        mRect = new RectF();
    }

    class Point {
        float x, y;

        Point() {
        }

        Point(float a, float b) {
            x = a;
            y = b;
        }
    };

    public MovementView(Context context) {

        super(context);
        getHolder().addCallback(this);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        if (mIsTankStatus) {
            drawTanks(canvas);
        } else {
            plotStats(canvas);
        }
        // drawTanks(canvas);
        // plotStats(canvas);
        super.onDraw(canvas);
    }

    private void drawTanks(Canvas canvas) {
        mPaint.setColor(Color.RED);
        for (int i = 0; i < mMines.size(); i++) {
            Vector2D mine = mMines.get(i);
            float curX = (float) mine.x;
            float curY = (float) mine.y;
            canvas.drawRect(curX - 3, curY - 3, curX + 3, curY + 3, mPaint);
        }
        // mPaint.setColor(Color.BLUE);
        for (int i = 0; i < mSweepers.size(); i++) {
            Minesweeper sweeper = mSweepers.get(i);
            float curX = (float) sweeper.position().x;
            float curY = (float) sweeper.position().y;
            float rotation = (float) sweeper.mRotation + 3.14f;
            mRect.set(curX - 7, curY - 9, curX + 7, curY + 9);
            canvas.rotate(Utils.degrees(rotation), curX, curY);
            canvas.drawBitmap(mTank, null, mRect, mPaint);
            canvas.rotate(Utils.degrees(-rotation), curX, curY);
        }
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(18);
        mPaint.setAntiAlias(true);
        // canvas.drawText("Best fitness: " + mGA.bestFitness(), 20, 25,
        // mPaint);
        // canvas.drawText("Avg fitness: " + mGA.averageFitness(), 20, 50,
        // mPaint);
        canvas.drawText("Generation: " + mGenerations, 20, 25, mPaint);
    }

    private void plotStats(Canvas canvas) {

        // render the graph
        float hSlice = (float) mXClient / (mGenerations + 1);
        float vSlice = (float) ((float) mYClient / ((mGA.bestFitness() + 1) * 2));

        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(18);
        mPaint.setAntiAlias(true);
        canvas.drawText("Best fitness: " + mGA.bestFitness(), 20, 25, mPaint);
        canvas.drawText("Avg fitness: " + mGA.averageFitness(), 20, 50, mPaint);
        canvas.drawText("Generation: " + mGenerations, 20, 75, mPaint);
        // plot the graph for the best fitness
        float x = 0;
        mPaint.setColor(Color.RED);
        int size = mBestFitness.size();
        for (int i = 0; i < size - 1; ++i) {
            canvas.drawLine(x, (int) (mYClient - vSlice * mBestFitness.get(i)),
                    x + hSlice,
                    (int) (mYClient - vSlice * mBestFitness.get(i + 1)), mPaint);
            x += hSlice;
        }

        // plot the graph for the average fitness
        x = 0;
        mPaint.setColor(Color.BLUE);
        size = mAvFitness.size();
        for (int i = 0; i < size - 1; ++i) {
            canvas.drawLine(x, (int) (mYClient - vSlice * mAvFitness.get(i)), x
                    + hSlice,
                    (int) (mYClient - vSlice * mAvFitness.get(i + 1)), mPaint);
            x += hSlice;
        }
    }

    public boolean update(Canvas canvas) {
        if (mTicks++ < Params.mNumTicks) {
            for (int i = 0; i < mNumSweepers; ++i) {
                // update the NN and position
                if (!mSweepers.get(i).update(mMines)) {
                    // error in processing the neural net
                    // MessageBox(m_hwndMain, "Wrong amount of NN inputs!",
                    // "Error", MB_OK);
                    return false;
                }

                // see if it's found a mine
                int grabHit = mSweepers.get(i).checkForMine(mMines,
                        Params.mMineScale);
//                if (grabHit != -1) {
//                    Log.d("myTag", "GrabHit ======= " + grabHit);
//                }

                if (grabHit >= 0) {
                    // we have discovered a mine so increase fitness
                    mSweepers.get(i).incrementFitness();

                    // mine found so replace the mine with another at a random
                    // position
                    mMines.set(grabHit, new Vector2D(Utils.randFloat()
                            * mXClient, Utils.randFloat() * mYClient));
                }

                // update the chromos fitness score
                mThePopulation.get(i).mFitness = mSweepers.get(i).fitness();

            }
            onDraw(canvas);
        }

        // Another generation has been completed.
        // Time to run the GA and update the sweepers with their new NNs
        else {
//            Log.e("myTag", "best fitness == " + mGA.bestFitness());
//            Log.e("myTag", "avg fitness == " + mGA.averageFitness());
//            Log.e("myTag", "m_iGenerations == " + mGenerations);

            // reset cycles
            mTicks = 0;

            // run the GA to create a new population
            mThePopulation = mGA.epoch(mThePopulation);
            // update the stats to be used in our stat window
            mAvFitness.add(mGA.averageFitness());
            mBestFitness.add(mGA.bestFitness());

            // increment the generation counter
            ++mGenerations;

            // insert the new (hopefully)improved brains back into the sweepers
            // and reset their positions etc
            for (int i = 0; i < mNumSweepers; ++i) {
                mSweepers.get(i).putWeights(mThePopulation.get(i).mWeights);

                mSweepers.get(i).reset();
            }
            onDraw(canvas);
        }

        return true;
    }

    public void surfaceCreated(SurfaceHolder holder) {

        mUpdateThread = new UpdateThread(this);
        mUpdateThread.setRunning(true);
        mUpdateThread.start();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

        boolean retry = true;

        mUpdateThread.setRunning(false);
        while (retry) {
            try {
                mUpdateThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
}