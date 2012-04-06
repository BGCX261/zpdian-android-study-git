package com.feixun.smart;

import java.util.ArrayList;
//import java.util.Vector;

public class Minesweeper {
    private NeuralNet mItsBrain = new NeuralNet();

    // its position in the world
    private Vector2D mPosition;

    // direction sweeper is facing
    private Vector2D mLookAt;

    // how much it is rotated from its starting position
    public double mRotation;

    private double mSpeed;

    // to store output from the ANN
    private double mLTrack;
    private double mRTrack;

    // the sweeper's fitness score.
    private double mFitness;

    // the scale of the sweeper when drawn
//    private double mScale;

    // index position of closest mine
    private int mClosestMine;

    // -----------------------------------constructor-------------------------
    //
    // -----------------------------------------------------------------------
    public Minesweeper()

    {
        mRotation = Utils.randFloat() * Params.twoPi;
        mLTrack = 0.16;
        mRTrack = 0.16;
        mFitness = Params.startEnergy;
//        mScale = Params.sweeperScale;
        mClosestMine = 0;
        // create a random start position
        mLookAt = new Vector2D(0.0, 0.0);
        mPosition = new Vector2D((Utils.randFloat() * Params.windowWidth),
                (Utils.randFloat() * Params.windowHeight));

    }

    // -------------------------------------------Reset()--------------------
    //
    // Resets the sweepers position, energy level and rotation
    //
    // ----------------------------------------------------------------------
    void reset() {
        // reset the sweepers positions
        mPosition = new Vector2D((Utils.randFloat() * Params.windowWidth),
                (Utils.randFloat() * Params.windowHeight));

        // and the energy level
        mFitness = Params.startEnergy;

        // and the rotation
        mRotation = Utils.randFloat() * Params.twoPi;

        return;
    }

    // ---------------------WorldTransform--------------------------------
    //
    // sets up a translation matrix for the sweeper according to its
    // scale, rotation and position. Returns the transformed vertices.
    // -------------------------------------------------------------------
    // void worldTransform(ArrayList<Utils.Point> sweeper) // sweeper 原为引用
    // {
    // // create the world transformation matrix
    // C2DMatrix matTransform = new C2DMatrix();
    //
    // // scale
    // matTransform.scale(mScale, mScale);
    //
    // // rotate
    // matTransform.rotate(mRotation);
    //
    // // and translate
    // matTransform.translate(mPosition.x, mPosition.y);
    //
    // // now transform the ships vertices
    // matTransform.transformSPoints(sweeper);
    // }

    // -------------------------------Update()--------------------------------
    //
    // First we take sensor readings and feed these into the sweepers brain.
    //
    // The inputs are:
    //
    // a signed angle to the closest mine
    //
    // We receive two outputs from the brain.. lTrack & rTrack.
    // So given a force for each track we calculate the resultant rotation
    // and acceleration and apply to current velocity vector.
    //
    // -----------------------------------------------------------------------
    boolean update(ArrayList<Vector2D> mines) // mines 原为引用
    {

        // this will store all the inputs for the NN
        ArrayList<Double> inputs = new ArrayList<Double>();

        // get vector to closest mine
        Vector2D vlosestMine = getClosestMine(mines);

        // normalise it
        vlosestMine = vlosestMine.vec2DNormalize(vlosestMine);

        // calculate dot product of the look at vector and Closest mine
        // vector. This will give us the angle we need turn to face
        // the closest mine
        // vlosestMine);
        double dot = vlosestMine.vec2DDot(mLookAt, vlosestMine);

        // calculate sign
        int sign = vlosestMine.vec2DSign(mLookAt, vlosestMine);

        inputs.add(dot * sign);

        // update the brain and get feedback
        ArrayList<ArrayList<Double>> inAndOut = mItsBrain.update(inputs);
        inputs = inAndOut.get(0);
        ArrayList<Double> output = inAndOut.get(1);

        // make sure there were no errors in calculating the
        // output
        if (output.size() < Params.numOutputs) {
            return false;
        }

        // assign the outputs to the sweepers left & right tracks
        mLTrack = output.get(0);
        mRTrack = output.get(1);

        // calculate steering forces
        double rotForce = mLTrack - mRTrack;

        // clamp rotation
        rotForce = Utils.clamp(rotForce, -Params.maxTurnRate,
                Params.maxTurnRate);

        mRotation += rotForce;

        mSpeed = (mLTrack + mRTrack);

        // update Look At
        mLookAt.x = -Math.sin(mRotation);
        mLookAt.y = Math.cos(mRotation);

        // update position
        mPosition = mPosition.zijia(Vector2D.chen(mLookAt, mSpeed));

        // wrap around window limits
        if (mPosition.x > Params.windowWidth)
            mPosition.x = 0;
        if (mPosition.x < 0)
            mPosition.x = Params.windowWidth;
        if (mPosition.y > Params.windowHeight)
            mPosition.y = 0;
        if (mPosition.y < 0)
            mPosition.y = Params.windowHeight;

        return true;
    }

    // ----------------------GetClosestObject()---------------------------------
    //
    // returns the vector from the sweeper to the closest mine
    //
    // -----------------------------------------------------------------------
    private Vector2D getClosestMine(ArrayList<Vector2D> mines) // mines 原为引用
    {
        double closestSoFar = 99999;

        Vector2D closestObject = new Vector2D(0, 0);
        
        int size = mines.size();
        // cycle through mines to find closest
        for (int i = 0; i < size; i++) {
            double lenToObject = Vector2D.vec2DLength(Vector2D.jian(
                    mines.get(i), mPosition));
            if (lenToObject < closestSoFar) {
                closestSoFar = lenToObject;
                closestObject = Vector2D.jian(mPosition, mines.get(i));
                mClosestMine = i;
            }
        }

        return closestObject;
    }

    // ----------------------------- CheckForMine -----------------------------
    //
    // this function checks for collision with its closest mine (calculated
    // earlier and stored in m_iClosestMine)
    // -----------------------------------------------------------------------
    int checkForMine(ArrayList<Vector2D> mines, double size) // mines 为引用
    {
        Vector2D distToObject = Vector2D.jian(mPosition,
                mines.get(mClosestMine));

        if (Vector2D.vec2DLength(distToObject) < (size + 5)) {
            return mClosestMine;
        }

        return -1;
    }

    Vector2D position() {
        return mPosition;
    }

    void incrementFitness() {
        ++mFitness;
    }

    double fitness() {
        return mFitness;
    }

    void putWeights(ArrayList<Double> w) {
        mItsBrain.putWeights(w);
    }

    int getNumberOfWeights() {
        return mItsBrain.getNumberOfWeights();
    }

    ArrayList<Integer> calculateSplitPoints() {
        return mItsBrain.calculateSplitPoints();
    }
}
