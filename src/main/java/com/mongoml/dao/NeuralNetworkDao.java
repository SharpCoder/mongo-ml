package com.mongoml.dao;

import com.mongoml.models.DbNeuralNetwork;
import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

public class NeuralNetworkDao extends BaseDao<DbNeuralNetwork> {

    public NeuralNetworkDao(Datastore datastore) {
        super(DbNeuralNetwork.class, datastore);
    }

    public DbNeuralNetwork findByName(String name) {
        return datastore.find(klass)
            .field("name").equal(name)
            .get();
    }


    public void update(DbNeuralNetwork object) {
        object.setLastUpdated(DateTime.now().getMillis());
        Query<DbNeuralNetwork> query = datastore.createQuery(klass);
        query.field("name").equal(object.getName());

        UpdateOperations<DbNeuralNetwork> op = datastore.createUpdateOperations(klass);
        op.set("name", object.getName());
        op.set("layers", object.getLayers());
        op.set("version", object.getVersion());
        op.set("bytes", object.getBytes());
        op.set("networkType", object.getNetworkType());
        datastore.update(query, op);
    }
}
