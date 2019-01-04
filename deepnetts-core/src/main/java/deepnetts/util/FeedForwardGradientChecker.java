/**
 *  DeepNetts is pure Java Deep Learning Library with support for Backpropagation
 *  based learning and image recognition.
 *
 *  Copyright (C) 2017  Zoran Sevarac <sevarac@gmail.com>
 *
 * This file is part of DeepNetts.
 *
 * DeepNetts is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.package
 * deepnetts.core;
 */
package deepnetts.util;

import deepnetts.data.BasicDataSet;
import deepnetts.data.DataSet;
import deepnetts.data.DataSetItem;
import deepnetts.net.FeedForwardNetwork;
import deepnetts.net.layers.AbstractLayer;
import deepnetts.net.layers.activation.ActivationType;
import deepnetts.net.layers.InputLayer;
import deepnetts.net.loss.LossType;
import deepnetts.net.train.BackpropagationTrainer;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: values for epsilon and precision how to store gradients, this will also
 * be needed for diagnostics
 *
 * http://ufldl.stanford.edu/wiki/index.php/Gradient_checking_and_advanced_optimization
 * http://ufldl.stanford.edu/tutorial/supervised/DebuggingGradientChecking/
 *
 * Use relative error for the comparison
 * http://cs231n.github.io/neural-networks-3/#gradcheck
 *
 *
 * relative error > 1e-2 usually means the gradient is probably wrong 1e-2 >
 * relative error > 1e-4 should make you feel uncomfortable 1e-4 > relative
 * error is usually okay for objectives with kinks. But if there are no kinks
 * (e.g. use of tanh nonlinearities and softmax), then 1e-4 is too high. 1e-7
 * and less you should be happy.
 *
 * h = 1e-4 or 1e-6 (ako je suvise mali uci cu u precision problem) relative err
 * idealno < 1e-7 treba da dude manja od 1e-4 Float isto ume da bude uzrok
 * greske
 *
 * FIX: Primetio sam da prva 4 u svakom lejeru nisu dobra! i to da bas dosta
 * mase. Moguce da ima veze sto je i broj ulaza 4! TODO: Sigurano je greska u
 * implementaciji. Testiraj i jednostavniju mrezu i data set.
 *
 *
 * @author Zoran Sevarac
 * <zoran.sevarac@deepnetts.com>
 */
public class FeedForwardGradientChecker {

    public final static float EPSILON = 1e-4f;
    public final static float ERROR_THRESHOLD = 1e-2f;

    // 1. instantiate network, with specified random seed 123, and init small e=1E-8 mislim
    // save all network weights ? da mogu ponovo da ih setujem posle svake promene. daj metode setWeights i getWeights
    //
    // feed network with some input and do forward pass. | Use 10 rows of random generated inputs or use some data set - iris or mnist
    // do backward pass
    //      calculate loss function value E(w)
    //      calculate gradients for all weights dE/dw (and save them)
    //
    // for all weights in each layer in the network (iterate layers and all weights in each layer)
    //   increase current weight for +e
    //   calculate loss for E(wij+e) and estimate dE/dwij
    //   decrease original weight for -e
    //   calculate loss for E(wij-e) and estimate dE/dwij
    //   calculate numeric gradient as (E(wij+e) - E(wij-e)) / 2e
    //   check if the estimated gradients are the same (theri difference is smaller then some specifid value)
    private void run() {
        FeedForwardNetwork nnet = createNetwork();

        // we dont need bacprop details in neural network
        DataSet<?> dataSet = createDataSet();

        //  for each item in data set ...
        for (DataSetItem dataSetItem : dataSet) {

            // for each layer in network
            for (AbstractLayer layer : nnet.getLayers()) {
                if (layer instanceof InputLayer) {
                    continue; // skip input layer
                }

                // for each weight in layer
                Tensor weights = layer.getWeights();
                Tensor gradients = layer.getGradients();

                for (int row = 0; row < weights.getRows(); row++) {
                    for (int col = 0; col < weights.getCols(); col++) {

                        final float weight = weights.get(row, col);

                        nnet.setInput(dataSetItem.getInput());
                        float[] outputError = nnet.getLossFunction().addPatternError(nnet.getOutput(), dataSetItem.getTargetOutput());
                        nnet.setOutputError(outputError);
                        nnet.backward();
                        nnet.getLossFunction().reset(); // reset loss to zero

                        float gradient = gradients.get(row, col); // get analytical backpropagated gradient

                        // calculate loss fo (w+eps)
                        weights.set(row, col, weight + EPSILON);
                        nnet.setInput(dataSetItem.getInput());
                        outputError = nnet.getLossFunction().addPatternError(nnet.getOutput(), dataSetItem.getTargetOutput());
                        nnet.setOutputError(outputError);
                        nnet.backward();
                        float lossPlusEps = nnet.getLossFunction().getTotal();
                        nnet.getLossFunction().reset();

                        // calculate loss fo (w-eps)
                        weights.set(row, col, weight - EPSILON);
                        nnet.setInput(dataSetItem.getInput());
                        outputError = nnet.getLossFunction().addPatternError(nnet.getOutput(), dataSetItem.getTargetOutput());
                        nnet.setOutputError(outputError);
                        nnet.backward();
                        float lossMinusEps = nnet.getLossFunction().getTotal();
                        nnet.getLossFunction().reset();

                        // should we check biases too? why are lossPlusEps - lossMinusEps Nan
                        float numericGradient = (lossPlusEps - lossMinusEps) / (2 * EPSILON);
                        // relative error
                        float error = Math.abs(gradient - numericGradient) / Math.max(Math.abs(gradient), Math.abs(numericGradient));
                        //float error = Math.abs(gradient - numericGradient); // absolute error
                        if (error <= ERROR_THRESHOLD) {
                            System.out.println("gradient:" + gradient + " numeric grad: " + numericGradient + " error:" + error + " good");
                        } else {
                            System.out.println("gradient:" + gradient + " numeric grad: " + numericGradient + " error:" + error + " >>>>>>>>> not good!");
                        }
                    }
                }
            }
            System.out.println("---- Next Layer -----------------------------------------------------------------------------");
        }
    }

    public static void main(String[] args) {
        (new FeedForwardGradientChecker()).run();
    }

    private FeedForwardNetwork createNetwork() {
        FeedForwardNetwork neuralNet = FeedForwardNetwork.builder()
                .addInputLayer(4)
                .addDenseLayer(10, ActivationType.SIGMOID)
                .addOutputLayer(3, ActivationType.SOFTMAX)
                .lossFunction(LossType.CROSS_ENTROPY)
                .randomSeed(123)
                .build();

        return neuralNet;
    }

    private DataSet createDataSet() {
        try {
            DataSet dataSet = BasicDataSet.fromCSVFile("../deepnetts-examples/datasets/iris_data_normalised.txt", 4, 3);
            return dataSet;
        } catch (IOException ex) {
            Logger.getLogger(FeedForwardGradientChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
