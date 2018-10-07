package com.mongoml.ml;


import com.mongoml.models.DbTrainingSet;

import java.util.List;

public interface NeuralNetworkManager {

    double[] evaluate(double[] dataSet);
    void train(List<DbTrainingSet> dataSet);
    void save();

}
