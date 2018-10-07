package com.mongoml.models;

import lombok.Getter;
import lombok.Setter;
import org.mongodb.morphia.annotations.Entity;

@Getter
@Setter
@Entity("neural_network_training")
public class DbTrainingSet extends DbBaseModel {

    private String networkName;
    private DbNeuralNetwork.NetworkType networkType;
    private String label;
    private double[] input;
    private double[] output;

}
