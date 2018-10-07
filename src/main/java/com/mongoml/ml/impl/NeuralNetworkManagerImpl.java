package com.mongoml.ml.impl;

import com.mongoml.dao.NeuralNetworkDao;
import com.mongoml.ml.NeuralNetworkManager;
import com.mongoml.models.DbNeuralNetwork;
import com.mongoml.models.DbTrainingSet;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Sgd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

public class NeuralNetworkManagerImpl implements NeuralNetworkManager {

    private static final Logger LOG = LoggerFactory.getLogger(NeuralNetworkManagerImpl.class);
    private static final Integer EPOCH = 8000;

    private final NeuralNetworkDao dao;

    private MultiLayerNetwork model;
    private DbNeuralNetwork dbNeuralNetwork;


    public NeuralNetworkManagerImpl(NeuralNetworkDao dao) {
        this.dao = dao;
    }

    public NeuralNetworkManagerImpl(NeuralNetworkDao dao, String name, DbNeuralNetwork.NetworkType networkType, List<Integer> layers) {
        this(dao);

        NeuralNetConfiguration.ListBuilder builder = new NeuralNetConfiguration.Builder()
            .seed(123L)
            .biasInit(0)
            .miniBatch(true)
            .weightInit(WeightInit.DISTRIBUTION)
            .activation(Activation.SOFTMAX)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .updater(new Sgd(0.1))
            .list()
            .pretrain(false)
            .backprop(true);


        for (int layer = 0; layer < layers.size() - 1; layer++) {
            if (layer == layers.size() - 2) {
                OutputLayer nextLayer = new OutputLayer.Builder()
                    .nIn(layers.get(layer))
                    .nOut(layers.get(layer + 1))
                    .build();

                builder.layer(layer, nextLayer);
            } else {
                DenseLayer nextLayer = new DenseLayer.Builder()
                    .nIn(layers.get(layer))
                    .nOut(layers.get(layer + 1))
                    .build();

                builder.layer(layer, nextLayer);
            }

        }

        model = new MultiLayerNetwork(builder.build());
        model.init();

        this.dbNeuralNetwork = new DbNeuralNetwork();
        this.dbNeuralNetwork.setName(name);
        this.dbNeuralNetwork.setLayers(layers);
        this.dbNeuralNetwork.setVersion(1);
        this.dbNeuralNetwork.setNetworkType(networkType);
        this.dbNeuralNetwork.setInputs(layers.get(0));
        this.dbNeuralNetwork.setOutputs(layers.get(layers.size() - 1));
        dao.create(dbNeuralNetwork);
        this.save();
    }

    public NeuralNetworkManagerImpl(NeuralNetworkDao dao, DbNeuralNetwork network) {
        this(dao);
        this.dbNeuralNetwork = network;
        this.load(network);
    }

    @Override
    public double[] evaluate(double[] data) {
        INDArray input = toIndArray(data, data.length + dbNeuralNetwork.getOutputs());
        return model.output(input).toDoubleVector();
    }

    @Override
    public void train(List<DbTrainingSet> dataSet) {
        INDArray input = Nd4j.zeros(dataSet.size(), dbNeuralNetwork.getInputs());
        INDArray labels = Nd4j.zeros(dataSet.size(), dbNeuralNetwork.getOutputs());

        int[] indexes = new int[dataSet.size()];
        for (int i = 0; i < dataSet.size() - 1; i++) {
            indexes[i] = i;
        }

        for (int i = 0; i < dataSet.size() - 1; i++) {
            int next = (int)Math.round(Math.random() * (dataSet.size() - 1));
            int temp = indexes[next];
            indexes[next] = indexes[i];
            indexes[i] = temp;
        }

        // Create datasets
        for (int i = 0; i < dataSet.size() - 1; i++) {
            int index = indexes[i];
            DbTrainingSet data = dataSet.get(index);
            input.putRow(index, toIndArray(data.getInput(), dbNeuralNetwork.getInputs()));
            labels.putRow(index, toIndArray(data.getOutput()));
        }

        DataSet ds = new DataSet(input, labels);

        // Actual training
        LOG.info(
            String.format(
                "About to train the neural network model with configuration trainingSets=%s and epoch=%s",
                dataSet.size(),
                EPOCH
            )
        );

        int epoch = EPOCH;
        do {
            model.fit(ds);
        } while (epoch-- > 0);

        this.save();

        LOG.info(
            String.format(
                "Completed training neural network model with epoch=%s",
                EPOCH
            )
        );
    }

    @Override
    public void save() {
        dbNeuralNetwork.setVersion(this.dbNeuralNetwork.getVersion() + 1);
        try {
            final String fileName = "./samples/" + dbNeuralNetwork.getName() + ".nnet";
            File file = new File(fileName);
            model.save(file);

            // Load the file bytes
            byte[] bytes = Files.readAllBytes(file.toPath());
            dbNeuralNetwork.setBytes(bytes);
            dao.update(dbNeuralNetwork);
            file.delete();
            LOG.info(
                String.format(
                    "Saved version=%s of neural network %s",
                    dbNeuralNetwork.getVersion(),
                    dbNeuralNetwork.getName()
                )
            );

        } catch (Exception error) {
            LOG.error(
                "Failed to save neural network",
                error
            );
        }
    }

    private void load(DbNeuralNetwork databaseModel) {
        try {
            final String fileName = "./samples/" + dbNeuralNetwork.getName() + ".nnet";
            File file = new File(fileName);

            try (OutputStream os = new FileOutputStream(file)) {
                os.write(databaseModel.getBytes());
            }

            model = MultiLayerNetwork.load(file, false);
            file.delete();
        } catch (Exception error) {
            LOG.error(
                "Failed to load neural network",
                error
            );
        }
    }

    private INDArray toIndArray(double[] values, int count) {
        INDArray result = Nd4j.zeros(count);
        for (int index = 0; index < values.length; index++) {
            result.putScalar(index, values[index]);
        }
        return result;
    }

    private INDArray toIndArray(double[] values) {
        return toIndArray(values, values.length);
    }
}
