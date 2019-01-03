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
package deepnetts.examples;

import deepnetts.data.BasicDataSet;
import deepnetts.data.DataSet;
import deepnetts.net.FeedForwardNetwork;
import deepnetts.net.layers.activation.ActivationType;
import deepnetts.net.loss.LossType;
import deepnetts.net.train.BackpropagationTrainer;
import deepnetts.net.train.opt.OptimizerType;
import deepnetts.util.DeepNettsException;
import java.io.File;
import java.io.IOException;

/**
 * Iris Classification Problem. Using sigmoid activation in output addLayer by
 * default and mse as a loss function.
 *
 * @author Zoran Sevarac <zoran.sevarac@deepnetts.com>
 */
public class IrisClassificationMse {

    public static void main(String[] args) throws DeepNettsException, IOException {

        // load iris data set from csv file
        DataSet dataSet = BasicDataSet.fromCSVFile(new File("datasets/iris_data_normalised.txt"), 4, 3, ",");
        dataSet.shuffle();

        // create multi layer perceptron with specified settings
        FeedForwardNetwork neuralNet = FeedForwardNetwork.builder()
                .addInputLayer(4)
                .addDenseLayer(8) // , ActivationType.SIGMOID
                .addOutputLayer(3, ActivationType.SIGMOID)
                .lossFunction(LossType.MEAN_SQUARED_ERROR)
                .randomSeed(123)
                .build();

        // create a trainer object with specified settings
        BackpropagationTrainer trainer = new BackpropagationTrainer();
        trainer.setMaxError(0.06f)
                .setLearningRate(0.5f)
                .setMomentum(0.0f)
                .setOptimizer(OptimizerType.SGD)
                .setBatchMode(false);
        //.setBatchSize(10);

        // train the network
        trainer.train(neuralNet, dataSet);
    }

}
