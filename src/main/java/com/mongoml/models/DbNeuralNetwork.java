package com.mongoml.models;

import lombok.Getter;
import lombok.Setter;
import org.mongodb.morphia.annotations.Entity;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity("neural_network")
public class DbNeuralNetwork extends DbBaseModel {

    public enum NetworkType {
        Invalid,
        STT
    };

    private String name;
    private NetworkType networkType;
    private byte[] bytes;
    private Integer version;
    private List<Integer> layers = new ArrayList<>();
    private Integer inputs = 0;
    private Integer outputs = 0;

}
