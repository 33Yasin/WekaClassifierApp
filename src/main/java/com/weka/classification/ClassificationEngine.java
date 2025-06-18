package com.weka.classification;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Instances;

public class ClassificationEngine {
    
    private Map<String, Classifier> trainedClassifiers;
    private Map<String, Instances> processedDatasets;
    
    public enum AlgorithmType {
        NAIVE_BAYES,
        LOGISTIC_REGRESSION,
        KNN,
        DECISION_TREE,
        RANDOM_FOREST,
        RANDOM_TREES,
        MULTILAYER_PERCEPTRON,
        SVM
    }
    
    public ClassificationEngine() {
        trainedClassifiers = new HashMap<>();
        processedDatasets = new HashMap<>();
    }
    
    /**
     * Tests all algorithms and returns accuracy rates
     */
    public Map<String, Double> testAllAlgorithms(Instances originalData) throws Exception {
        Map<String, Double> results = new HashMap<>();
        
        // List of algorithms to be tested
        Map<String, AlgorithmType> algorithms = new HashMap<>();
        algorithms.put("Naive Bayes", AlgorithmType.NAIVE_BAYES);
        algorithms.put("Logistic Regression", AlgorithmType.LOGISTIC_REGRESSION);
        algorithms.put("K-Nearest Neighbor", AlgorithmType.KNN);
        algorithms.put("Decision Tree (J48)", AlgorithmType.DECISION_TREE);
        algorithms.put("Random Forest", AlgorithmType.RANDOM_FOREST);
        algorithms.put("Random Trees", AlgorithmType.RANDOM_TREES);
        algorithms.put("Multilayer Perceptron", AlgorithmType.MULTILAYER_PERCEPTRON);
        algorithms.put("Support Vector Machine", AlgorithmType.SVM);
        
        // Testing for each algorithm
        for (Map.Entry<String, AlgorithmType> entry : algorithms.entrySet()) {
            String algorithmName = entry.getKey();
            AlgorithmType algorithmType = entry.getValue();
            
            try {
                // Prepare data according to algorithm requirements
                Instances processedData = DataPreprocessor.prepareDataForAlgorithm(originalData, algorithmType);
                processedDatasets.put(algorithmName, processedData);
                
                // Create classifier
                Classifier classifier = createClassifier(algorithmType);
                
                // Train the model
                classifier.buildClassifier(processedData);
                trainedClassifiers.put(algorithmName, classifier);
                
                // Test with 10-fold cross validation
                Evaluation evaluation = new Evaluation(processedData);
                evaluation.crossValidateModel(classifier, processedData, 10, new Random(1));
                
                // Get accuracy rate
                double accuracy = evaluation.pctCorrect();
                results.put(algorithmName, accuracy);
                
            } catch (Exception e) {
                System.err.println("Algoritma " + algorithmName + " için hata: " + e.getMessage());
                results.put(algorithmName, null);
            }
        }
        
        return results;
    }
    
    /**
     * Classifies a new instance with the specified algorithm
     */
    public String classifyInstance(Instances originalData, String algorithmName, double[] instanceValues) throws Exception {
        if (!trainedClassifiers.containsKey(algorithmName)) {
            throw new Exception("The algorithm has not yet been trained: " + algorithmName);
        }
        
        Classifier classifier = trainedClassifiers.get(algorithmName);
        Instances processedData = processedDatasets.get(algorithmName);
        
        // Prepare instance according to algorithm requirements
        AlgorithmType algorithmType = getAlgorithmType(algorithmName);
        double[] processedValues = DataPreprocessor.prepareInstanceForAlgorithm(
            originalData, instanceValues, algorithmType);
        
        // Create new instance
        weka.core.DenseInstance newInstance = new weka.core.DenseInstance(1.0, 
            addDummyClassValue(processedValues));
        newInstance.setDataset(processedData);
        
        // Classify
        double classIndex = classifier.classifyInstance(newInstance);
        
        // Return class name
        return processedData.classAttribute().value((int) classIndex);
    }
    
    /**
     * Creates classifier based on algorithm type
     */
    private Classifier createClassifier(AlgorithmType algorithmType) throws Exception {
        switch (algorithmType) {
            case NAIVE_BAYES:
                return new NaiveBayes();
                
            case LOGISTIC_REGRESSION:
                Logistic logistic = new Logistic();
                logistic.setMaxIts(100);
                return logistic;
                
            case KNN:
                IBk knn = new IBk();
                knn.setKNN(3);
                return knn;
                
            case DECISION_TREE:
                J48 j48 = new J48();
                j48.setUnpruned(false);
                j48.setConfidenceFactor(0.25f);
                return j48;
                
            case RANDOM_FOREST:
                RandomForest rf = new RandomForest();
                rf.setNumIterations(100);
                return rf;
                
            case RANDOM_TREES:
                RandomTree rt = new RandomTree();
                return rt;
                
            case MULTILAYER_PERCEPTRON:
                MultilayerPerceptron mlp = new MultilayerPerceptron();
                mlp.setLearningRate(0.3);
                mlp.setMomentum(0.2);
                mlp.setTrainingTime(500);
                mlp.setHiddenLayers("a"); 
                return mlp;
                
            case SVM:
                SMO svm = new SMO();
                svm.setC(1.0);
                return svm;
                
            default:
                throw new Exception("Bilinmeyen algoritma tipi: " + algorithmType);
        }
    }
    
    /**
     * Returns the algorithm type from the algorithm name
     */
    private AlgorithmType getAlgorithmType(String algorithmName) {
        switch (algorithmName) {
            case "Naive Bayes":
                return AlgorithmType.NAIVE_BAYES;
            case "Logistic Regression":
                return AlgorithmType.LOGISTIC_REGRESSION;
            case "K-Nearest Neighbor":
                return AlgorithmType.KNN;
            case "Decision Tree (J48)":
                return AlgorithmType.DECISION_TREE;
            case "Random Forest":
                return AlgorithmType.RANDOM_FOREST;
            case "Random Trees":
                return AlgorithmType.RANDOM_TREES;
            case "Multilayer Perceptron":
                return AlgorithmType.MULTILAYER_PERCEPTRON;
            case "Support Vector Machine":
                return AlgorithmType.SVM;
            default:
                return AlgorithmType.DECISION_TREE; 
        }
    }
    
    private double[] addDummyClassValue(double[] values) {
        double[] newValues = new double[values.length + 1];
        System.arraycopy(values, 0, newValues, 0, values.length);
        newValues[newValues.length - 1] = 0; 
        return newValues;
    }
}