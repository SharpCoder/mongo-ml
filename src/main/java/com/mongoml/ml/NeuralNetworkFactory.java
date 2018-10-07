package com.mongoml.ml;

import com.mongoml.dao.NeuralNetworkDao;
import com.mongoml.ml.impl.NeuralNetworkManagerImpl;
import com.mongoml.models.DbNeuralNetwork;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class NeuralNetworkFactory {
    private NeuralNetworkFactory() {

    }

    public static NeuralNetworkManager create(
        NeuralNetworkDao neuralNetworkDao,
        String name,
        DbNeuralNetwork.NetworkType networkType,
        List<Integer> layers
    ) {

        // See if this neural network configuration already exists.
        String dbName = name + StringUtils.join(layers, '-');;
        DbNeuralNetwork savedNetwork = neuralNetworkDao.findByName(dbName);
        boolean layersMatch = false;

        if (savedNetwork != null && savedNetwork.getLayers().size() == layers.size()) {
            layersMatch = true;
            for (int index = 0; index < layers.size(); index++) {
                if (!layers.get(index).equals(savedNetwork.getLayers().get(index))) {
                    layersMatch = false;
                }
            }
        }

        if (savedNetwork == null || !layersMatch) {
            if (savedNetwork != null) {
                neuralNetworkDao.delete(savedNetwork);
            }

            return new NeuralNetworkManagerImpl(neuralNetworkDao, dbName, networkType,layers);
        } else {
            return new NeuralNetworkManagerImpl(neuralNetworkDao, savedNetwork);
        }
    }
}
