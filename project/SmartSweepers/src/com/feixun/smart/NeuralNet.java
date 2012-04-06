package com.feixun.smart;

import java.util.ArrayList;

//import java.util.Vector;

public class NeuralNet {

    private int mNumInputs;

    private int mNumOutputs;

    private int mNumHiddenLayers;

    private int mNeuronsPerHiddenLyr;

    // storage for each layer of neurons including the output layer
    private ArrayList<NeuronLayer> mLayers = new ArrayList<NeuronLayer>();

    // ************************ methods forCNeuralNet ************************

    // ------------------------------default ctor ----------------------------
    //
    // creates a ANN based on the default values in params.ini
    // -----------------------------------------------------------------------
    public NeuralNet() {
        mNumInputs = Params.numInputs;
        mNumOutputs = Params.numOutputs;
        mNumHiddenLayers = Params.numHidden;
        mNeuronsPerHiddenLyr = Params.neuronsPerHiddenLayer;

        createNet();

    }

    // ------------------------------createNet()------------------------------
    //
    // this method builds the ANN. The weights are all initially set to
    // random values -1 < w < 1
    // ------------------------------------------------------------------------
    private void createNet() {
        // create the layers of the network
        if (mNumHiddenLayers > 0) {
            // create first hidden layer
            mLayers.add(new NeuronLayer(mNeuronsPerHiddenLyr, mNumInputs));

            for (int i = 0; i < mNumHiddenLayers - 1; ++i) {

                mLayers.add(new NeuronLayer(mNeuronsPerHiddenLyr,
                        mNeuronsPerHiddenLyr));
            }

            // create output layer
            mLayers.add(new NeuronLayer(mNumOutputs, mNeuronsPerHiddenLyr));
        }

        else {
            // create output layer
            mLayers.add(new NeuronLayer(mNumOutputs, mNumInputs));
        }
    }

    // ---------------------------------GetWeights-----------------------------
    //
    // returns a vector containing the weights
    //
    // ------------------------------------------------------------------------
    ArrayList<Double> getWeights() {
        // this will hold the weights
        ArrayList<Double> weights = new ArrayList<Double>();

        // for each layer
        for (int i = 0; i < mNumHiddenLayers + 1; ++i) {

            // for each neuron
            for (int j = 0; j < mLayers.get(i).mNumNeurons; ++j) {
                // for each weight
                for (int k = 0; k < mLayers.get(i).mNeurons.get(j).mNumInputs; ++k) {
                    weights.add(mLayers.get(i).mNeurons.get(j).mWeight.get(k));
                }
            }
        }

        return weights;
    }

    // -----------------------------------PutWeights---------------------------
    //
    // given a vector of doubles this function replaces the weights in the NN
    // with the new values
    //
    // ------------------------------------------------------------------------
    void putWeights(ArrayList<Double> weights) // weights是引用
    {
        int cWeight = 0;

        // for each layer
        for (int i = 0; i < mNumHiddenLayers + 1; ++i) {

            // for each neuron
            for (int j = 0; j < mLayers.get(i).mNumNeurons; ++j) {
                // for each weight
                for (int k = 0; k < mLayers.get(i).mNeurons.get(j).mNumInputs; ++k) {
                    mLayers.get(i).mNeurons.get(j).mWeight.set(k,
                            weights.get(cWeight++));
                }
            }
        }

        return;
    }

    // ---------------------------------GetNumberOfWeights---------------------
    //
    // returns the total number of weights needed for the net
    //
    // ------------------------------------------------------------------------
    int getNumberOfWeights() {

        int weights = 0;

        // for each layer
        for (int i = 0; i < mNumHiddenLayers + 1; ++i) {

            // for each neuron
            for (int j = 0; j < mLayers.get(i).mNumNeurons; ++j) {
                // for each weight
                for (int k = 0; k < mLayers.get(i).mNeurons.get(j).mNumInputs; ++k)

                    weights++;
            }
        }

        return weights;
    }

    // -------------------------------Update-----------------------------------
    //
    // given an input vector this function calculates the output vector
    //
    // ------------------------------------------------------------------------
    ArrayList<ArrayList<Double>> update(ArrayList<Double> inputs) // inputs 是引用
    {
        ArrayList<ArrayList<Double>> inAndOut = new ArrayList<ArrayList<Double>>();
        // stores the resultant outputs from each layer
        ArrayList<Double> outputs = new ArrayList<Double>();

        int cWeight = 0;

        // first check that we have the correct amount of inputs
        if (inputs.size() != mNumInputs) {
            // just return an empty vector if incorrect.
            inAndOut.add(inputs);
            inAndOut.add(outputs);
            return inAndOut;
        }

        // For each layer....
        for (int i = 0; i < mNumHiddenLayers + 1; ++i) {

            if (i > 0) {
                inputs = (ArrayList<Double>) outputs.clone(); // must clone
            }

            outputs.clear();

            cWeight = 0;

            // for each neuron sum the (inputs * corresponding weights).Throw
            // the total at our sigmoid function to get the output.
            for (int j = 0; j < mLayers.get(i).mNumNeurons; ++j) {
                double netinput = 0;

                int numInputs = mLayers.get(i).mNeurons.get(j).mNumInputs;
                // for each weight
                for (int k = 0; k < numInputs - 1; ++k) {
                    // sum the weights x inputs
                    netinput += mLayers.get(i).mNeurons.get(j).mWeight.get(k)
                            * inputs.get(cWeight++);
                }

                // add in the bias
                netinput += mLayers.get(i).mNeurons.get(j).mWeight
                        .get(numInputs - 1) * Params.bias;

                // we can store the outputs from each layer as we generate them.
                // The combined activation is first filtered through the sigmoid
                // function
                outputs.add(sigmoid(netinput, Params.activationResponse));

                cWeight = 0;
            }
        }
        inAndOut.add(inputs);
        inAndOut.add(outputs);
        return inAndOut;
    }

    // -------------------------------Sigmoid function-------------------------
    //
    // ------------------------------------------------------------------------
    private Double sigmoid(double netinput, double response) {
        return (1 / (1 + Math.exp(-netinput / response)));
    }

    // --------------------------- CalculateSplitPoints -----------------------
    //
    // this method calculates all points in the vector of weights which
    // represent the start and end points of individual neurons
    // ------------------------------------------------------------------------
    ArrayList<Integer> calculateSplitPoints() {
        ArrayList<Integer> splitPoints = new ArrayList<Integer>();

        int weightCounter = 0;

        // for each layer
        for (int i = 0; i < mNumHiddenLayers + 1; ++i) {
            // for each neuron
            for (int j = 0; j < mLayers.get(i).mNumNeurons; ++j) {
                // for each weight
                for (int k = 0; k < mLayers.get(i).mNeurons.get(j).mNumInputs; ++k) {
                    ++weightCounter;
                }
                splitPoints.add(weightCounter - 1);
            }
        }

        return splitPoints;
    }

    public class Neuron {
        public int mNumInputs;
        public ArrayList<Double> mWeight = new ArrayList<Double>();

        public Neuron(int numInputs) {
            mNumInputs = numInputs + 1;
            for (int i = 0; i < numInputs + 1; ++i) {
                // set up the weights with an initial random value
                mWeight.add(Utils.randomClamped());
            }
        }

    }

    public class NeuronLayer {
        // the number of neurons in this layer
        public int mNumNeurons;

        // the layer of neurons
        ArrayList<Neuron> mNeurons = new ArrayList<Neuron>();

        // ************************ methods for NeuronLayer
        // **********************

        // -----------------------------------------------------------------------
        // ctor creates a layer of neurons of the required size by calling the
        // SNeuron ctor the rqd number of times
        // -----------------------------------------------------------------------
        NeuronLayer(int numNeurons, int numInputsPerNeuron) {
            mNumNeurons = numNeurons;
            for (int i = 0; i < numNeurons; ++i)

                mNeurons.add(new Neuron(numInputsPerNeuron));
        }

    }

}
