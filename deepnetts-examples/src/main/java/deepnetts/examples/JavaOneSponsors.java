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
import deepnetts.util.DeepNettsException;
import deepnetts.data.ImageSet;
import deepnetts.net.ConvolutionalNetwork;
import deepnetts.net.layers.activation.ActivationType;
import deepnetts.net.train.BackpropagationTrainer;
import deepnetts.net.train.opt.OptimizerType;
import deepnetts.eval.ClassifierEvaluator;
import deepnetts.net.loss.LossType;
import deepnetts.util.FileIO;
import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class JavaOneSponsors {

    int imageWidth = 128;
    int imageHeight = 128;

    String labelsFile = "D:\\datasets\\JavaOneSponsors2\\labels.txt";
    String trainingFile = "D:\\datasets\\JavaOneSponsors2\\train.txt";
   // String testFile = "datasets/JavaOneSponsors/test/test.txt";

    private static final Logger LOGGER = LogManager.getLogger(DeepNetts.class.getName());

    public void run() throws DeepNettsException, IOException {

        ImageSet imageSet = new ImageSet(imageWidth, imageHeight);

        LOGGER.info("Loading images...");

        imageSet.loadLabels(new File(labelsFile));
        imageSet.loadImages(new File(trainingFile));

        imageSet.invert();
        imageSet.zeroMean(); // standardize zero mean 1 variation
        imageSet.shuffle();

        int labelsCount = imageSet.getLabelsCount();

        LOGGER.info("Creating neural network...");

        // dodaj i bele slike kao negative u data set
        ConvolutionalNetwork javaOneNet = ConvolutionalNetwork.builder()
                                        .addInputLayer(imageWidth, imageHeight)
                                        .addConvolutionalLayer(5, 5, 3, ActivationType.TANH)
                                        .addMaxPoolingLayer(2, 2, 2)
                                        .addFullyConnectedLayer(10, ActivationType.TANH)
                                        .addOutputLayer(labelsCount, ActivationType.SOFTMAX)
                                        .lossFunction(LossType.CROSS_ENTROPY)
                                        .randomSeed(123)
                                        .build();

        LOGGER.info("Training neural network");

        // create a set of convolutional networks and do training, crossvalidation and performance evaluation
        BackpropagationTrainer trainer = new BackpropagationTrainer(javaOneNet);
        trainer.setLearningRate(0.01f)
               .setMomentum(0.7f)
               .setMaxError(0.3f)
               .setMaxEpochs(10)
               .setOptimizer(OptimizerType.SGD);
        trainer.train(imageSet);

        // Serialize network
        try {
            FileIO.writeToFile(javaOneNet, "javaonesponsors.dnet");
        } catch (IOException ex) {
           LOGGER.error(ex);
        }

        // deserialize and evaluate neural network
        ClassifierEvaluator evaluator = new ClassifierEvaluator();
        evaluator.evaluate(javaOneNet, imageSet);
        System.out.println(evaluator);

//        BufferedImage image = ImageIO.read(new File("/home/zoran/Desktop/JavaOneSet/java/java1.jpg"));
//        DeepNettsImageClassifier imageClassifier = new DeepNettsImageClassifier(javaOneNet);
//        ClassificationResults<ClassificationResult> results = imageClassifier.classify(image);
//        System.out.println(results.toString());

    }


    public static void main(String[] args) {
        try {
            (new JavaOneSponsors()).run();
        } catch (DeepNettsException | IOException ex) {
            LOGGER.error(ex);
        }
    }
}