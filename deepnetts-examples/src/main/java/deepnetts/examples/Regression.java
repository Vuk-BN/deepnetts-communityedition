/**  
 *  DeepNetts is pure Java Deep Learning Library with support for Backpropagation 
 *  based learning and image recognition.
 * 
 *  Copyright (C) 2017  Zoran Sevarac <sevarac@gmail.com>
 *
 *  This file is part of DeepNetts.
 *
 *  DeepNetts is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.package deepnetts.core;
 */    

package deepnetts.examples;

import deepnetts.data.BasicDataSet;
import deepnetts.net.FeedForwardNetwork;
import deepnetts.net.NeuralNetwork;
import deepnetts.net.layers.activation.ActivationType;
import deepnetts.net.loss.LossType;
import deepnetts.net.train.BackpropagationTrainer;

/**
 * Minimal example for regression neural network.
 * Use MSE as a loss and linear activation in output
 * 
 * @author Zoran Sevarac <zoran.sevarac@deepnetts.com>
 */
public class Regression {
    
    public static void main(String[] args) {
        
        BasicDataSet dataSet = null;// get dataset from somewhere
        
        NeuralNetwork neuralNet = FeedForwardNetwork.builder()
                                    .addInputLayer(5)
                                    .addDenseLayer(10, ActivationType.TANH)
                                    .addOutputLayer(1, ActivationType.LINEAR)
                                    .lossFunction(LossType.MEAN_SQUARED_ERROR)          
                                    .build();
                       
        BackpropagationTrainer trainer = new BackpropagationTrainer();
                               trainer.train(neuralNet, dataSet);         
    }
    
}
