package com.weka.classification;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.Normalize;

public class DataPreprocessor {
    
    /**
     * Converts nominal data to binary numeric data
     */
    public static Instances nominalToBinary(Instances data) throws Exception {
        NominalToBinary filter = new NominalToBinary();
        filter.setInputFormat(data);
        return Filter.useFilter(data, filter);
    }
    
    /**
     * Converts numeric data to nominal data (discretization)
     */
    public static Instances numericToNominal(Instances data) throws Exception {
        Discretize filter = new Discretize();
        filter.setInputFormat(data);
        return Filter.useFilter(data, filter);
    }
    
    /**
     * Normalizes numeric data in the range [0,1]
     */
    public static Instances normalizeData(Instances data) throws Exception {
        Normalize filter = new Normalize();
        filter.setInputFormat(data);
        return Filter.useFilter(data, filter);
    }
    
    /**
     * Checks the data type of the Dataset
     */
    public static DatasetType getDatasetType(Instances data) {
        boolean hasNominal = false;
        boolean hasNumeric = false;
        
        for (int i = 0; i < data.numAttributes(); i++) {
            if (i == data.classIndex()) continue;
            
            if (data.attribute(i).isNominal()) {
                hasNominal = true;
            } else if (data.attribute(i).isNumeric()) {
                hasNumeric = true;
            }
        }
        
        if (hasNominal && hasNumeric) {
            return DatasetType.MIXED;
        } else if (hasNominal) {
            return DatasetType.NOMINAL;
        } else if (hasNumeric) {
            return DatasetType.NUMERIC;
        } else {
            return DatasetType.UNKNOWN;
        }
    }
    
    /**
     * If the class attribute is numeric, it converts it to nominal.
     */
    public static Instances ensureNominalClass(Instances data) throws Exception {
        if (data.classAttribute().isNumeric()) {
            Discretize filter = new Discretize();
            filter.setAttributeIndices("" + (data.classIndex() + 1)); 
            filter.setInputFormat(data);
            Instances filtered = Filter.useFilter(data, filter);
            String className = data.classAttribute().name();
            for (int i = 0; i < filtered.numAttributes(); i++) {
                if (filtered.attribute(i).name().equals(className)) {
                    filtered.setClassIndex(i);
                    return filtered;
                }
            }
            filtered.setClassIndex(filtered.numAttributes() - 1);
            return filtered;
        }
        return data;
    }
    
    /**
     * Prepares the dataset according to the algorithm requirements
     */
    public static Instances prepareDataForAlgorithm(Instances originalData, ClassificationEngine.AlgorithmType algorithmType) throws Exception {
        // Make class nominal
        Instances processedData = ensureNominalClass(originalData);
        DatasetType datasetType = getDatasetType(processedData);
        
        switch (algorithmType) {
            case NAIVE_BAYES:
                // Only nominal data required
                if (datasetType == DatasetType.NUMERIC || datasetType == DatasetType.MIXED) {
                    processedData = numericToNominal(processedData);
                }
                break;
                
            case LOGISTIC_REGRESSION:
            case KNN:
            case MULTILAYER_PERCEPTRON:
            case SVM:
                // Only numeric data is required
                if (datasetType == DatasetType.NOMINAL || datasetType == DatasetType.MIXED) {
                    processedData = nominalToBinary(processedData);
                }
                // Normalization for numeric data
                if (getDatasetType(processedData) == DatasetType.NUMERIC || 
                    getDatasetType(processedData) == DatasetType.MIXED) {
                    processedData = normalizeData(processedData);
                }
                break;
                
            case DECISION_TREE:
            case RANDOM_FOREST:
            case RANDOM_TREES:
                break;
        }
        
        return processedData;
    }
    

    public static double[] prepareInstanceForAlgorithm(Instances originalData, double[] instanceValues, 
                                                      ClassificationEngine.AlgorithmType algorithmType) throws Exception {
        Instances tempData = new Instances(originalData);
        
        double[] tempValues = new double[instanceValues.length + 1];
        System.arraycopy(instanceValues, 0, tempValues, 0, instanceValues.length);
        tempValues[tempValues.length - 1] = 0; 
        
        weka.core.DenseInstance newInstance = new weka.core.DenseInstance(1.0, tempValues);
        tempData.add(newInstance);
        
        Instances processedData = prepareDataForAlgorithm(tempData, algorithmType);
        
        weka.core.Instance lastInstance = processedData.lastInstance();
        double[] processedValues = new double[lastInstance.numAttributes() - 1];
        
        int index = 0;
        for (int i = 0; i < lastInstance.numAttributes(); i++) {
            if (i != processedData.classIndex()) {
                processedValues[index++] = lastInstance.value(i);
            }
        }
        
        return processedValues;
    }
    
    public enum DatasetType {
        NOMINAL,
        NUMERIC,
        MIXED,
        UNKNOWN
    }
}