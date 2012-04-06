package com.feixun.smart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
//import java.util.Vector;

import android.util.Log;

public class GenAlg {

    private ArrayList<Genome> mPop = new ArrayList<Genome>();

    // size of population
    int mPopSize;

    // amount of weights per chromo
    int mChromoLength;

    // this holds the positions of the split points in the genome for use
    // in our modified crossover operator
    ArrayList<Integer> mSplitPoints = new ArrayList<Integer>();

    // total fitness of population
    double mTotalFitness;

    // best fitness this population
    double mBestFitness;

    // average fitness
    double mAverageFitness;

    // worst
    double mWorstFitness;

    // keeps track of the best genome
    int mFittestGenome;

    // probability that a chromosones bits will mutate.
    // Try figures around 0.05 to 0.3 ish
    double mMutationRate;

    // probability of chromosones crossing over bits
    // 0.7 is pretty good
    double mCrossoverRate;

    // generation counter
    int mGeneration;

    public GenAlg() {
        super();
    }

    class Genome {
        ArrayList<Double> mWeights = new ArrayList<Double>();

        double mFitness;

        public Genome() {
            super();
        }

        public Genome(ArrayList<Double> w, double f) {
            mWeights = (ArrayList<Double>) w.clone();
            mFitness = f;
        }

        // overload '<' used for sorting
        boolean xiaoyu(Genome lhs, Genome rhs) {
            return (lhs.mFitness < rhs.mFitness);
        }
    }

    // -----------------------------------constructor-------------------------
    //
    // sets up the population with random floats
    //
    // -----------------------------------------------------------------------
    GenAlg(int popsize, double MutRat, double CrossRat, int numweights,
            ArrayList<Integer> splits) {
        mPopSize = popsize;
        mMutationRate = MutRat;
        mCrossoverRate = CrossRat;
        mChromoLength = numweights;
        mTotalFitness = 0;
        mGeneration = 0;
        mFittestGenome = 0;
        mBestFitness = 0;
        mWorstFitness = 99999999;
        mAverageFitness = 0;
        mSplitPoints = (ArrayList<Integer>) splits.clone();
        // initialise population with chromosomes consisting of random
        // weights and all fitnesses set to zero
        for (int i = 0; i < mPopSize; ++i) {
            mPop.add(new Genome());

            for (int j = 0; j < mChromoLength; ++j) {
                mPop.get(i).mWeights.add(Utils.randomClamped());
            }
        }
    }

    // ---------------------------------Mutate--------------------------------
    //
    // mutates a chromosome by perturbing its weights by an amount not
    // greater than CParams::dMaxPerturbation
    // -----------------------------------------------------------------------
    ArrayList<Double> mutate(ArrayList<Double> chromo) {
        // traverse the chromosome and mutate each weight dependent
        // on the mutation rate
        int size = chromo.size();
        for (int i = 0; i < size; ++i) {
            // do we perturb this weight?
            if (Utils.randFloat() < mMutationRate) {
                // add or subtract a small value to the weight
                chromo.set(i, chromo.get(i)
                        + (Utils.randomClamped() * Params.mMaxPerturbation));
            }
        }
        return chromo;
    }

    // ----------------------------------GetChromoRoulette()------------------
    //
    // returns a chromo based on roulette wheel sampling
    //
    // -----------------------------------------------------------------------
    Genome getChromoRoulette() {
        // generate a random number between 0 & total fitness count
        double slice = (double) (Utils.randFloat() * mTotalFitness);

        // this will be set to the chosen chromosome
        Genome theChosenOne = null;

        // go through the chromosones adding up the fitness so far
        double fitnessSoFar = 0;

        for (int i = 0; i < mPopSize; ++i) {
            fitnessSoFar += mPop.get(i).mFitness;

            // if the fitness so far > random number return the chromo at
            // this point
            if (fitnessSoFar >= slice) {
                theChosenOne = mPop.get(i);

                break;
            }
        }

        return theChosenOne;
    }

    // -------------------------------------Crossover()-----------------------
    //
    // given parents and storage for the offspring this method performs
    // crossover according to the GAs crossover rate
    // -----------------------------------------------------------------------
    void crossover(ArrayList<Double> mum, ArrayList<Double> dad,
            ArrayList<Double> baby1, ArrayList<Double> baby2) // 所有参数为引用
    {
        // just return parents as offspring dependent on the rate
        // or if parents are the same
        if ((Utils.randFloat() > mCrossoverRate) || (mum.equals(dad))) {
            baby1 = (ArrayList<Double>) mum.clone();
            baby2 = (ArrayList<Double>) dad.clone();

            return;
        }

        // determine a crossover point
        int cp = Utils.randInt(0, mChromoLength - 1);

        // create the offspring
        for (int i = 0; i < cp; ++i) {
            baby1.add(mum.get(i));
            baby2.add(dad.get(i));
        }

        int size = mum.size();
        for (int i = cp; i < size; ++i) {
            baby1.add(dad.get(i));
            baby2.add(mum.get(i));
        }

        return;
    }

    // ---------------------------- CrossoverAtSplits -------------------------
    //

    // -------------------------------------------------------------------------
    ArrayList<ArrayList<Double>> crossoverAtSplits(ArrayList<Double> mum,
            ArrayList<Double> dad, ArrayList<Double> baby1,
            ArrayList<Double> baby2) // 所有变量为引用
    {
        ArrayList<ArrayList<Double>> babys = new ArrayList<ArrayList<Double>>();
        // just return parents as offspring dependent on the rate
        // or if parents are the same
        if ((Utils.randFloat() > mCrossoverRate) || (mum.equals(dad))) {
            baby1 = (ArrayList<Double>) mum.clone();
            baby2 = (ArrayList<Double>) dad.clone();
            babys.add(baby1);
            babys.add(baby2);
            return babys;
        }
        // determine two crossover points
        int index1 = Utils.randInt(0, mSplitPoints.size() - 2);
        int index2 = Utils.randInt(index1, mSplitPoints.size() - 1);
        int cp1 = mSplitPoints.get(index1);
        int cp2 = mSplitPoints.get(index2);

        int size = mum.size();
        // create the offspring
        for (int i = 0; i < size; ++i) {
            if ((i < cp1) || (i >= cp2)) {
                // keep the same genes if outside of crossover points
                baby1.add(mum.get(i));
                baby2.add(dad.get(i));
            }

            else {
                // switch over the belly block
                baby1.add(dad.get(i));
                baby2.add(mum.get(i));
            }

        }
        babys.add(baby1);
        babys.add(baby2);
        return babys;
    }

    // -----------------------------------Epoch()-----------------------------
    //
    // takes a population of chromosones and runs the algorithm through one
    // cycle.
    // Returns a new population of chromosones.
    //
    // -----------------------------------------------------------------------
    ArrayList<Genome> epoch(ArrayList<Genome> oldPop) // old_pop 为引用
    {
        // assign the given population to the classes population
        mPop = (ArrayList<Genome>) oldPop.clone();

        // reset the appropriate variables
        reset();

        // sort the population (for scaling and elitism)
        // sort(mPop.begin(), mPop.end());
        Mycomparator comparator = new Mycomparator();
        Collections.sort(mPop, comparator);
        for (int i = 0; i < mPop.size(); i++) {
            Log.e("myTag", "mPop.fitness = " + mPop.get(i).mFitness);
        }
        // calculate best, worst, average and total fitness
        calculateBestWorstAvTot();

        // create a temporary vector to store new chromosones
        ArrayList<Genome> newPop = new ArrayList<Genome>();

        // Now to add a little elitism we shall add in some copies of the
        // fittest genomes. Make sure we add an EVEN number or the roulette
        // wheel sampling will crash
        if ((Params.mNumCopiesElite * Params.mNumElite % 2) == 0) {
            newPop = grabNBest(Params.mNumElite, Params.mNumCopiesElite, newPop);
        }

        Log.w("myTag", "newPop.size() ==== " + newPop.size());
        // now we enter the GA loop

        // repeat until a new population is generated
        while (newPop.size() < mPopSize) {
            // grab two chromosones
            Genome mum = getChromoRoulette();
            Genome dad = getChromoRoulette();

            // create some offspring via crossover
            ArrayList<Double> baby1 = new ArrayList<Double>();
            ArrayList<Double> baby2 = new ArrayList<Double>();
            ArrayList<ArrayList<Double>> babys = null;
            babys = crossoverAtSplits(mum.mWeights, dad.mWeights, baby1, baby2);
            baby1 = babys.get(0);
            baby2 = babys.get(1);
            // now we mutate
            baby1 = mutate(baby1);
            baby1 = mutate(baby2);

            // now copy into vecNewPop population
            newPop.add(new Genome(baby1, 0));
            newPop.add(new Genome(baby2, 0));
        }

        // finished so assign new pop back into m_vecPop
        mPop = (ArrayList<Genome>) newPop.clone();

        return mPop;
    }

    public class Mycomparator implements Comparator<Object> {

        public int compare(Object o1, Object o2) {
            Genome p1 = (Genome) o1;
            Genome p2 = (Genome) o2;
            if (p1.mFitness > p2.mFitness) {
                return 1;
            } else if (p1.mFitness < p2.mFitness) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    // -----------------------------FitnessScaleRank----------------------
    //
    // This type of fitness scaling sorts the population into ascending
    // order of fitness and then simply assigns a fitness score based
    // on its position in the ladder. (so if a genome ends up last it
    // gets score of zero, if best then it gets a score equal to the size
    // of the population. You can also assign a multiplier which will
    // increase the 'seperation' of genomes on the ladder and allow the
    // population to converge much quicker
    // ---------------------------------------------------------------------
    void fitnessScaleRank() {
        int fitnessMultiplier = 1;

        // assign fitness according to the genome's position on
        // this new fitness 'ladder'
        for (int i = 0; i < mPopSize; i++) {
            mPop.get(i).mFitness = i * fitnessMultiplier;
        }

        // recalculate values used in selection
        calculateBestWorstAvTot();
    }

    // -------------------------GrabNBest----------------------------------
    //
    // This works like an advanced form of elitism by inserting NumCopies
    // copies of the NBest most fittest genomes into a population vector
    // --------------------------------------------------------------------
    ArrayList<Genome> grabNBest(int nBest, int numCopies, ArrayList<Genome> pop) {
        // add the required amount of copies of the n most fittest
        // to the supplied vector
        while (nBest > 0) {
            for (int i = 0; i < numCopies; ++i) {
                pop.add(mPop.get((mPopSize - 1) - nBest));
            }
            nBest--;
        }
        return pop;
    }

    // -----------------------CalculateBestWorstAvTot-----------------------
    //
    // calculates the fittest and weakest genome and the average/total
    // fitness scores
    // ---------------------------------------------------------------------
    void calculateBestWorstAvTot() {
        mTotalFitness = 0;

        double highestSoFar = 0;
        double lowestSoFar = 9999999;

        for (int i = 0; i < mPopSize; ++i) {
            // update fittest if necessary
            if (mPop.get(i).mFitness > highestSoFar) {
                highestSoFar = mPop.get(i).mFitness;

                mFittestGenome = i;

                mBestFitness = highestSoFar;
            }

            // update worst if necessary
            if (mPop.get(i).mFitness < lowestSoFar) {
                lowestSoFar = mPop.get(i).mFitness;

                mWorstFitness = lowestSoFar;
            }

            mTotalFitness += mPop.get(i).mFitness;

        }// next chromo

        mAverageFitness = mTotalFitness / mPopSize;
    }

    // -------------------------Reset()------------------------------
    //
    // resets all the relevant variables ready for a new generation
    // --------------------------------------------------------------
    void reset() {

        mTotalFitness = 0;
        mBestFitness = 0;
        mWorstFitness = 9999999;
        mAverageFitness = 0;

    }

    ArrayList<Genome> getChromos() {
        return mPop;
    }

    double averageFitness() {
        return mTotalFitness / mPopSize;
    }

    double bestFitness() {
        return mBestFitness;
    }

}
