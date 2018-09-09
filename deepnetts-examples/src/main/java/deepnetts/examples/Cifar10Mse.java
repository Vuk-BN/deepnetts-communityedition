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

import deepnetts.core.DeepNetts;
import deepnetts.data.ImageSet;
import deepnetts.net.ConvolutionalNetwork;
import deepnetts.net.layers.activation.ActivationType;
import deepnetts.net.loss.LossType;
import deepnetts.net.train.BackpropagationTrainer;
import deepnetts.net.train.OptimizerType;
import deepnetts.util.DeepNettsException;
import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Cifar10Mse {
            
    int imageWidth = 32;
    int imageHeight = 32;
         
    String labelsFile = "datasets/cifar10/labels.txt";
    String trainingFile = "datasets/cifar10/train.txt";
    //String testFile = "datasets/cifar10/test.txt";         
    
    static final Logger LOGGER = LogManager.getLogger(DeepNetts.class.getName());   
    
    
    public void run() throws DeepNettsException, IOException {
        LOGGER.info("Loading images...");
        ImageSet imageSet = new ImageSet(imageWidth, imageHeight);        
        imageSet.loadLabels(new File(labelsFile));
        imageSet.loadImages(new File(trainingFile), true, 100);
        imageSet.invert();
        imageSet.zeroMean();
        imageSet.shuffle();
            
        int labelsCount = imageSet.getLabelsCount();
               
        LOGGER.info("Creating neural network...");

        ConvolutionalNetwork cifar10Net = new ConvolutionalNetwork.Builder()
                                        .addInputLayer(imageWidth, imageHeight) 
                                        .addConvolutionalLayer(5, 1, ActivationType.TANH)
                                        .addMaxPoolingLayer(2, 2)  
                                        .addDenseLayer(40, ActivationType.TANH)          
                                        .addOutputLayer(labelsCount, ActivationType.TANH)
                                        .withLossFunction(LossType.MEAN_SQUARED_ERROR)
                                        .withRandomSeed(123)
                                        .build();
          
        LOGGER.info("Training neural network"); 
         
        BackpropagationTrainer trainer = new BackpropagationTrainer();
        trainer.setMaxError(0.03f);
        trainer.setLearningRate(0.01f); // 0.0001
        trainer.setMomentum(0.9f); 
        trainer.setOptimizer(OptimizerType.SGD); 
       // trainer.setBatchMode(false); // false by default
        trainer.train(cifar10Net, imageSet);                                     
    }
        
    public static void main(String[] args) throws DeepNettsException, IOException {                                
            (new Cifar10Mse()).run();                
    }
}